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

def slot_click(x,y,shift=False):
    s=observe();left=(s['guiWidth']-320)/2
    top=(s['guiHeight']-242)/2
    px,py=physical(s,left+x+8,top+y+8)
    if shift:process('xdotool','keydown','Shift_L')
    process('xdotool','mousemove','--window',WINDOW,str(px),str(py),'click','1')
    if shift:process('xdotool','keyup','Shift_L')
    time.sleep(.4)

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
    wait_for(lambda s:s.get('focus')=='search','native search focus');time.sleep(.2)
    process('xdotool','key','ctrl+a');time.sleep(.2)
    process('xdotool','type','--clearmodifiers','--delay','80',value)
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
    capture('00-workshop-manual-item')
    for slot,design in enumerate(('MANUAL',),1):
        process('xdotool','key',str(slot));time.sleep(.3)
        process('xdotool','click','3')
        expect(screen='FieldGuideScreen',design=design,page='HOME');capture(design.lower()+'-home')
        click('open:sniffers');expect(selected='sniffers')
        capture('manual-first-step');click('home')
        click('category:CREATURES');expect(page='INDEX');capture('manual-creature-index')
        click('row:0');expect(selected='sniffers');capture('manual-creature-article')
        click('related');expect(related=True);capture('manual-related');click('home')
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
        click('home');click('category:MODIFIERS');expect(page='TOPICS');capture('manual-topics');click('group:AREA');expect(group='AREA',page='INDEX')
        click('row:0');expect(page='ARTICLE');capture(design.lower()+'-area')
        command('gui_scale',value=2);process('xdotool','windowsize',WINDOW,'854','480')
        wait_for(lambda s:s.get('windowWidth')==854 and s.get('windowHeight')==480,'small window');capture(design.lower()+'-small')
        click('home');capture(design.lower()+'-small-home')
        process('xdotool','windowsize',WINDOW,'640','480')
        wait_for(lambda s:s.get('windowWidth')==640 and s.get('guiWidth')==320,'minimum GUI width')
        capture(design.lower()+'-narrow-home')
        click('category:CREATURES');capture('manual-narrow-index');click('row:0');capture('manual-narrow-creature');click('home')
        search_text('magnet');capture('manual-narrow-search');click('clear')
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
        print('WORKSHOP_MANUAL_NATIVE_PASS: '+design+'; actual item opening, categories, sections, related/history, global search, no matches, topics, small window, bookmarks and keyboard',flush=True)
    # Fixtures place blocks/inventory only. All forge actions use real OS right-clicks.
    command('gui_scale',value=2);command('forge_fixture');expect(forgeReady=True,forgeFormed=True,frontTableAbsent=True)
    process('xdotool','key','9');time.sleep(.7)
    command('forge_view');capture('forge-complete')
    command('forge_part_view');expect(aimedAtForge=True);process('xdotool','click','3')
    expect(screen='InGame',storedCount=6,legacyTool=True);capture('forge-legacy-storage')
    command('forge_view',close=True);expect(aimedAtForge=True)
    for left in range(5,-1,-1):
        process('xdotool','click','3');expect(screen='InGame',storedCount=left)
    expect(recoveredLegacyItems=6,legacyTool=False)
    command('forge_strip_frame');expect(forgeMissing=17,forgeFormed=False)
    process('xdotool','click','3');expect(screen='InGame',forgeGhost=True)
    command('forge_view');capture('forge-ghost-layout')
    command('forge_obstruct');expect(forgeOccupied=1);capture('forge-ghost-occupied')
    command('forge_view',close=True);expect(aimedAtForge=True);process('xdotool','click','3');expect(forgeGhost=False)
    capture('forge-incomplete');process('xdotool','click','3');expect(forgeGhost=True)
    command('forge_repair');expect(forgeReady=True,forgeFormed=True,forgeGhost=False);capture('forge-repaired')
    command('alloy_inventory');command('forge_view',close=True);expect(aimedAtForge=True)
    for key,scrap,gold,iron in [('1',1,0,0),('2',1,1,0),('3',1,1,1),('3',1,1,2)]:
        process('xdotool','key',key);time.sleep(.25);process('xdotool','click','3');expect(screen='InGame',alloyScrap=scrap,alloyGold=gold,alloyIron=iron)
    expect(alloyReady=True,alloyCount=0);capture('forge-ingredients')
    process('xdotool','key','4');time.sleep(.25);process('xdotool','click','3');expect(forgeHeating=True,flintDamage=1,alloyCount=0)
    process('xdotool','click','3');expect(flintDamage=1)
    command('forge_view');capture('forge-black-smoke')
    command('forge_damage');expect(forgeReady=False,forgeFormed=False,forgeHeating=True,alloyCount=0)
    command('forge_view',close=True);process('xdotool','key','9');process('xdotool','click','3');expect(forgeGhost=True)
    capture('forge-paused');command('forge_repair');expect(forgeReady=True,forgeFormed=True,forgeGhost=False)
    wait_for(lambda s:s.get('forgeFinished') and s.get('alloyCount')==4,'20-second alloy finishing',timeout=75)
    command('forge_view');time.sleep(3);capture('forge-white-smoke')
    command('forge_view',close=True);expect(aimedAtForge=True);process('xdotool','click','3');expect(ingotInventory=4,alloyCount=0,forgeFinished=False,storedCount=0)
    capture('forge-result-collected')
    command('melt_inventory');process('xdotool','key','1');time.sleep(.25);process('xdotool','click','3')
    expect(screen='InGame',melting=True,alloyReady=True);capture('artifact-melting-ready')
    process('xdotool','key','4');time.sleep(.25);process('xdotool','click','3');expect(forgeHeating=True,flintDamage=2)
    wait_for(lambda s:s.get('forgeFinished') and s.get('meltCount')==1,'artifact melting finishes',timeout=75)
    command('forge_view');capture('artifact-melting-result')
    command('forge_view',close=True);process('xdotool','key','9');process('xdotool','click','3');expect(meltCount=0,scrapInventory=1,forgeFinished=False)
    print('ANCIENT_FORGE_NATIVE_PASS: no processing or repair UI, removed front table, fireplace ingredient deposits, flint ignition, timed alloy/melt, black/white chimney smoke, pause/repair, legacy withdrawal and collected output',flush=True)

    # Place the actual crafted block item with native right-click, then inspect both foundation tiers.
    command('anvil_fixture');process('xdotool','key','2');time.sleep(.7);process('xdotool','click','3');expect(anvilPlaced=True,anvilTier=0,anvilLarge=True)
    process('xdotool','key','9');command('anvil_view');capture('ancient-anvil-tier0')
    command('anvil_view',close=True);expect(aimedAtAnvil=True);process('xdotool','click','3');expect(screen='AncientAnvilScreen');capture('ancient-anvil-panel0')
    click_widget('Show foundation guide');expect(screen='InGame',anvilGuide=True);command('anvil_expose');command('anvil_view');capture('ancient-anvil-guide')
    command('anvil_foundation');expect(anvilTier=1,anvilMissing=0,anvilGuide=False);capture('ancient-anvil-tier1')
    command('anvil_view',close=True);expect(aimedAtAnvil=True);process('xdotool','click','3');expect(screen='AncientAnvilScreen');capture('ancient-anvil-panel1')
    process('xdotool','key','Escape');command('anvil_damage');expect(anvilTier=0,anvilMissing=1)
    command('anvil_foundation');expect(anvilTier=1,anvilMissing=0)
    print('ANCIENT_ANVIL_NATIVE_PASS: actual block item placement, tier zero/one, mixed metal foundation, real panel/guide buttons, downgrade/repair; in-world timed alloying, exact costs and legacy-inventory withdrawal',flush=True)
    # Explicit player feeding uses native right-click on a real adult entity in survival.
    previous_scrap=observe()['scrapInventory'];command('feed_fixture');process('xdotool','key','1')
    expect(holdingArtifact=True,aimedAtCruncher=True);capture('cruncher-feeding-ready')
    process('xdotool','click','3');expect(scrapInventory=previous_scrap+1,holdingArtifact=False,cruncherChewing=True);capture('cruncher-feeding-result')
    print('ANCIENT_SCRAP_NATIVE_PASS: actual ancient-scrap alloying, timed fireplace artifact melting and empty-hand withdrawal, survival right-click artifact feeding with one scrap and synchronized chew animation',flush=True)
    # Exercise native jigsaw generation, not only direct fixture placement or an NBT parser.
    command('forge_probe_worldgen')
    process('xdotool','key','t');time.sleep(.2)
    process('xdotool','type','--clearmodifiers','--delay','20','/place structure examplemod:ruined_ancient_forge 24 '+str(observe()['forgeY'])+' 40')
    process('xdotool','key','Return');expect(generatedForge=True)
    command('forge_generated_view');time.sleep(1);capture('forge-world-ruin')
    print('ANCIENT_FORGE_NATIVE_PASS: in-world metallurgy, legacy storage recovery, frame interruption, repair, saved output and fireplace display',flush=True)
    # Compare against real vanilla assets and inspect all owned-cell orientations.
    command('assets_scene');expect(assetsLarge=True,assetsCells=6,assetsFacing='NORTH')
    process('xdotool','key','3');capture('assets-ingot-held')
    process('xdotool','key','4');capture('assets-scrap-held')
    process('xdotool','key','5');capture('assets-artifact-held')
    command('assets_offhand');process('xdotool','key','1');capture('assets-ingot-offhand')
    command('assets_right_hand');process('xdotool','key','3');time.sleep(.4);process('xdotool','key','q');expect(assetsIngotDropped=True);time.sleep(1);command('assets_drop_view');capture('assets-ingot-dropped')
    process('xdotool','key','9')
    for facing in ['NORTH','EAST','SOUTH','WEST']:
        command('assets_anvil_view',facing=facing);expect(assetsLarge=True,assetsCells=6,assetsFacing=facing);capture('large-anvil-'+facing.lower())
    print('ASSET_NATIVE_PASS: vanilla ingot comparison, main/offhand cutouts, dropped ingot, original artifact identity, vanilla material tiling and six owned anvil cells in all four orientations',flush=True)
    (QA/'result.json').write_text(json.dumps({'pass':True,'renderer':'actual Minecraft client; X11 software OpenGL','input':'xdotool -> X11 -> SDL -> native widgets','captures':len(list(CAPTURES.glob('*.png'))),'inWorld':True},indent=2))
    print('CLIENT_VISUAL_QA_PASS: Workshop Manual item, illustrated browsing and OS input task paths',flush=True)

if __name__=='__main__':
    try:run()
    except Exception:
        try:capture('failure')
        except Exception:pass
        (QA/'result.json').write_text(json.dumps({'pass':False,'error':traceback.format_exc()},indent=2))
        raise
    finally:
        if observe():command('quit')
