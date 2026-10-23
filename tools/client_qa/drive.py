"""Run against a real, visible Minecraft client. All input goes through xdotool/X11/SDL."""
import argparse, base64, json, os, subprocess, time, traceback
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
        if description=='the real book window' and state.get('frames',0)>90 and state.get('screen') not in ('TitleScreen','FieldGuideScreen'):
            raise AssertionError('Unexpected real startup screen: '+str(state.get('screen')))
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

def run():
    global WINDOW
    wait_for(lambda s:s.get('screen')=='FieldGuideScreen' and s.get('frames',0)>12,'the real book window',timeout=360)
    ids=subprocess.check_output(['xdotool','search','--onlyvisible','--name','Minecraft'],text=True).splitlines()
    if not ids:raise AssertionError('No actual Minecraft X11 window found')
    WINDOW=ids[-1];process('xdotool','windowfocus','--sync',WINDOW)
    process('xdotool','mousemove','--window',WINDOW,'5','5')
    capture('01-contents')
    # Pilot task: a new player follows the introduction to its first creature.
    click('welcome');expect(page='ARTICLE',selected='welcome');capture('02-getting-started')
    click('link:0');expect(page='ARTICLE',selected='sniffers');capture('03-sniffers')
    click('back');expect(selected='welcome');click('home');expect(page='HOME')
    # Pilot task: a returning player searches for a known modifier.
    s=observe();x,y=physical(s,s['searchX'],s['searchY'])
    process('xdotool','mousemove','--window',WINDOW,str(x),str(y),'click','1')
    process('xdotool','type','--clearmodifiers','--delay','100','magnet')
    expect(page='INDEX',query='magnet');capture('04-search')
    click('row:0');expect(page='ARTICLE',selected='examplemod:magnetized');capture('05-modifier')
    click('next');expect(articlePage=1);capture('06-modifier-next-page')
    click('back');expect(page='INDEX',query='magnet');click('clear');expect(page='HOME')
    # Pilot task: an equipment builder browses a topic instead of searching.
    click('category:MODIFIERS');expect(page='TOPICS');capture('07-modifier-topics')
    click('group:AREA');expect(page='INDEX',group='AREA');capture('08-area-index')
    click('row:0');expect(page='ARTICLE');capture('09-area-modifier')
    # Reflow at real GUI scales and physical window sizes; no synthetic render export.
    command('gui_scale',value=2);capture('10-scale-2')
    process('xdotool','windowsize',WINDOW,'854','480')
    wait_for(lambda s:s.get('windowWidth')==854 and s.get('windowHeight')==480,'854x480 resize');capture('11-small-window')
    click('home');expect(page='HOME');capture('12-small-contents')
    process('xdotool','key','Escape');wait_for(lambda s:s.get('screen')!='FieldGuideScreen','Escape closing the book')
    log('Escape closed book');command('open_book');expect(page='HOME');capture('13-reopened')
    click('close');wait_for(lambda s:s.get('screen')!='FieldGuideScreen','the close button');log('Close button closed book')
    (QA/'result.json').write_text(json.dumps({'pass':True,'renderer':'actual Minecraft client; X11 software OpenGL','input':'xdotool -> X11 -> SDL -> native widgets','captures':len(list(CAPTURES.glob('*.png')))},indent=2))
    print('CLIENT_VISUAL_QA_PASS: real client rendering, OS mouse/keyboard, search, links, pages, GUI scale, resize, Escape and Close',flush=True)

if __name__=='__main__':
    try:run()
    except Exception:
        try:capture('failure')
        except Exception:pass
        (QA/'result.json').write_text(json.dumps({'pass':False,'error':traceback.format_exc()},indent=2))
        raise
    finally:
        if observe():command('quit')
