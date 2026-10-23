"""Run against a real, visible Minecraft client. All input goes through xdotool/X11/SDL."""
import argparse, base64, json, os, re, subprocess, time, traceback
from pathlib import Path

ROOT=Path(__file__).resolve().parents[2]
QA=ROOT/'build/client-qa'; QA.mkdir(parents=True,exist_ok=True)
CAPTURES=QA/'captures';CAPTURES.mkdir(exist_ok=True)
TIMELINE=[]

def process(*args,**kwargs):
    return subprocess.run(args,check=True,text=True,**kwargs)

def observe():
    try:return json.loads((QA/'state.json').read_text())
    except (FileNotFoundError,json.JSONDecodeError):return {}

def wait_for(predicate,description,timeout=20):
    start=time.monotonic()
    while time.monotonic()-start<timeout:
        state=observe()
        if predicate(state):return state
        client_log=QA/'client.log'
        if client_log.exists():
            errors=client_log.read_text(errors='replace')
            if 'Failed to create backend OpenGL' in errors and 'Failed to create backend Vulkan' in errors:
                raise AssertionError('Both native graphics backends failed; inspect graphics.txt, vulkan.txt and client.log')
        pid=os.environ.get('CLIENT_QA_PID')
        if pid and not (Path('/proc')/pid).exists():raise AssertionError('Minecraft launcher exited; inspect client.log')
        time.sleep(.15)
    raise AssertionError(f'Timed out waiting for {description}; last live state: {observe()}')

def log(action,state=None):
    TIMELINE.append({'time':time.time(),'action':action,'state':state or observe()})
    (QA/'timeline.json').write_text(json.dumps(TIMELINE,indent=2))

def command(action,**fields):
    serial=int(time.time()*1000);p=QA/'command.tmp';p.write_text(json.dumps({'id':serial,'action':action,**fields}));p.replace(QA/'command.json')
    if action!='quit':wait_for(lambda s:s.get('commandId')==serial,f'command {action}')

def physical(state,x,y):
    return round(x*state['windowWidth']/state['guiWidth']),round(y*state['windowHeight']/state['guiHeight'])

WINDOW=None

def click(id):
    state=observe();control=next((b for b in state.get('controls',[]) if b['id']==id),None)
    if control is None or not control['enabled']:raise AssertionError(f'Control unavailable: {id}')
    x,y=physical(state,control['x']+control['width']/2,control['y']+control['height']/2)
    seq=state['sequence'];process('xdotool','mousemove','--window',WINDOW,str(x),str(y),'click','1')
    wait_for(lambda s:s.get('sequence',0)>seq,'a rendered frame after click');time.sleep(.35);log('click '+id)

def capture(name):
    # Capture the X11 client window, after presentation. No drawing-plan reconstruction.
    time.sleep(.6);path=CAPTURES/(name+'.png')
    process('import','-window',WINDOW or 'root','-depth','8',str(path))
    (CAPTURES/(name+'.json')).write_text(json.dumps(observe(),indent=2));log('screenshot '+name)

def expect(**fields):
    return wait_for(lambda s:all(s.get(k)==v for k,v in fields.items()),str(fields))

def click_widget(label,exact=True):
    state=wait_for(lambda s:any(b['enabled'] and (b['label']==label if exact else b['label'].startswith(label)) for b in s.get('widgets',[])), 'native widget '+label)
    control=next(b for b in state['widgets'] if b['enabled'] and (b['label']==label if exact else b['label'].startswith(label)))
    x,y=physical(state,control['x']+control['width']/2,control['y']+control['height']/2)
    process('xdotool','mousemove','--window',WINDOW,str(x),str(y),'click','1');time.sleep(.5);log('native widget '+label)

def search_text(value):
    s=observe();x,y=physical(s,s['searchX'],s['searchY'])
    process('xdotool','mousemove','--window',WINDOW,str(x),str(y),'click','1')
    process('xdotool','key','ctrl+a');process('xdotool','type','--clearmodifiers','--delay','40',value)
    expect(query=value);log('type search '+value)

def find_window(state):
    # SDL3 does not consistently expose the legacy WM_NAME used by xdotool search.
    # Resolve the actual focused window, then fall back to the X11 geometry tree.
    tree=subprocess.check_output(['xwininfo','-root','-tree'],text=True,errors='replace')
    (QA/'windows.txt').write_text(tree)
    focus=subprocess.run(['xdotool','getwindowfocus','-f'],capture_output=True,text=True)
    candidates=[focus.stdout.strip()] if focus.returncode==0 else []
    for id,w,h in re.findall(r'^\s*(0x[0-9a-fA-F]+).*?\b(\d+)x(\d+)[+-]\d+[+-]\d+',tree,re.M):
        if (int(w),int(h))==(state['windowWidth'],state['windowHeight']):candidates.append(id)
    for id in candidates:
        if not id or int(id,0)<=1:continue
        result=subprocess.run(['xdotool','getwindowgeometry','--shell',id],capture_output=True,text=True)
        geometry=dict(line.split('=',1) for line in result.stdout.splitlines() if '=' in line)
        if int(geometry.get('WIDTH',0))==state['windowWidth'] and int(geometry.get('HEIGHT',0))==state['windowHeight']:return id
    raise AssertionError('Cannot resolve native Minecraft window; inspect windows.txt')

def run():
    global WINDOW
    wait_for(lambda s:s.get('screen')=='TitleScreen' and s.get('frames',0)>12,'the native title screen',timeout=360)
    WINDOW=find_window(observe());process('xdotool','windowfocus','--sync',WINDOW)
    process('xdotool','mousemove','--window',WINDOW,'5','5');capture('00-title')
    # Create a real disposable flat world through native Minecraft UI.
    click_widget('Singleplayer')
    wait_for(lambda s:s.get('screen') in ('SelectWorldScreen','CreateWorldScreen'),'world selection or creation',timeout=90)
    if observe()['screen']=='SelectWorldScreen':click_widget('Create New World')
    wait_for(lambda s:s.get('screen')=='CreateWorldScreen','world creation',timeout=90)
    for _ in range(6):
        if any('Game Mode:' in w['label'] and 'Creative' in w['label'] for w in observe().get('widgets',[])):break
        click_widget('Game Mode:',exact=False)
    click_widget('World')
    for _ in range(12):
        if any('World Type:' in w['label'] and 'Superflat' in w['label'] for w in observe().get('widgets',[])):break
        click_widget('World Type:',exact=False)
    capture('00-world-setup');click_widget('Create New World')
    wait_for(lambda s:s.get('screen')=='FieldGuideScreen' and s.get('inWorld') and s.get('frames',0)>12,'the in-world book',timeout=180)
    command('seed_books');time.sleep(1)
    process('xdotool','key','Escape');wait_for(lambda s:s.get('screen')!='FieldGuideScreen','fixture book closing')
    capture('00-five-book-items')
    for slot,design in enumerate(('JOURNAL','MANUAL','TRAIL','REFERENCE','ATLAS'),1):
        process('xdotool','key',str(slot));time.sleep(.3)
        process('xdotool','click','3')
        expect(screen='FieldGuideScreen',design=design,page='HOME');capture(design.lower()+'-home')
        # Exercise the candidate's actual first-action affordance, not only shared categories.
        if design in ('MANUAL','TRAIL'):click('open:sniffers');expect(selected='sniffers')
        elif design=='ATLAS':click('category:START');click('row:0');expect(selected='welcome')
        else:click('welcome');expect(selected='welcome')
        capture(design.lower()+'-first-step');click('home')
        # Newcomer / casual mod user: find the excavation and understand recovery.
        click('category:EXCAVATION');expect(page='INDEX');click('row:0');expect(selected='archaeology_table')
        capture(design.lower()+'-excavation');click('section:3');expect(section=3)
        click('related');expect(related=True);click('link:0');expect(selected='excavation_tools')
        click('back');expect(selected='archaeology_table',section=3)
        click('home');expect(page='HOME')
        # Returning player: exact lookup, explicit tier/growth sections and preserved search.
        search_text('magnet');expect(page='INDEX',query='magnet');capture(design.lower()+'-search')
        click('row:0');expect(selected='examplemod:magnetized');capture(design.lower()+'-modifier')
        click('section:1');expect(section=1);capture(design.lower()+'-tiers')
        click('section:2');expect(section=2);click('back');expect(page='INDEX',query='magnet')
        search_text('zzzznotpresentzzzz');expect(page='INDEX',results=0);capture(design.lower()+'-empty')
        click('clear');expect(page='HOME',query='')
        # Vanilla veteran: use natural language rather than a modifier's internal name.
        search_text('wither rose');click('row:0');expect(selected='crunchers');click('section:2');expect(section=2)
        # Experienced builder: browse modifier topic; check boundaries and small-window reading.
        click('home');click('category:MODIFIERS');expect(page='TOPICS');click('group:AREA');expect(group='AREA',page='INDEX')
        click('row:0');expect(page='ARTICLE');capture(design.lower()+'-area')
        command('gui_scale',value=2);process('xdotool','windowsize',WINDOW,'854','480')
        wait_for(lambda s:s.get('windowWidth')==854 and s.get('windowHeight')==480,'small window');capture(design.lower()+'-small')
        click('home');capture(design.lower()+'-small-home')
        process('xdotool','windowsize',WINDOW,'640','480')
        wait_for(lambda s:s.get('windowWidth')==640 and s.get('guiWidth')==320,'minimum GUI width')
        capture(design.lower()+'-narrow-home')
        click('category:EXCAVATION');click('row:0');capture(design.lower()+'-narrow')
        click('home')
        # Actual Tab + Return activation of native Close, and session bookmark reopening.
        click('category:EXCAVATION');click('row:0');click('section:3')
        process('xdotool','key','Escape');wait_for(lambda s:s.get('screen')!='FieldGuideScreen','Escape closing book')
        process('xdotool','click','3');expect(screen='FieldGuideScreen',selected='archaeology_table',section=3)
        click('home')
        for _ in range(12):
            process('xdotool','key','Tab');time.sleep(.2)
            if observe().get('focus')=='close':break
        expect(focus='close')
        process('xdotool','key','Return');wait_for(lambda s:s.get('screen')!='FieldGuideScreen','keyboard Close')
        process('xdotool','windowsize',WINDOW,'1280','720')
        wait_for(lambda s:s.get('windowWidth')==1280 and s.get('windowHeight')==720,'normal window');command('gui_scale',value=3)
        wait_for(lambda s:s.get('guiScale')==3,'normal GUI scale')
        print('FIVE_BOOK_NATIVE_PASS: '+design+'; actual item opening, categories, sections, related/history, global search, no matches, topics, small window, bookmarks and keyboard',flush=True)
    (QA/'result.json').write_text(json.dumps({'pass':True,'renderer':'actual Minecraft client; X11 software OpenGL','input':'xdotool -> X11 -> SDL -> native widgets','captures':len(list(CAPTURES.glob('*.png'))),'inWorld':True},indent=2))
    print('CLIENT_VISUAL_QA_PASS: all five book items and OS input task paths',flush=True)

if __name__=='__main__':
    try:run()
    except Exception:
        try:capture('failure')
        except Exception:pass
        (QA/'result.json').write_text(json.dumps({'pass':False,'error':traceback.format_exc()},indent=2))
        raise
    finally:
        if observe():command('quit')
