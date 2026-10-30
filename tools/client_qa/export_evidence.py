"""Publish a few actual window captures through text logs for connector-based inspection.
All captures, live state, action history and video also remain in the Actions artifact.
"""
from pathlib import Path
import base64,json
root=Path('build/client-qa')
for diagnostic in ('graphics.txt','vulkan.txt'):
    path=root/diagnostic
    if path.exists():print('CLIENT_QA_DIAGNOSTIC '+diagnostic+'\n'+path.read_text(errors='replace')[:16000])
for path in Path('run/visual-qa/crash-reports').glob('*.txt'):
    print('NATIVE_CLIENT_CRASH '+path.name+'\n'+path.read_text(errors='replace')[:24000])
    (root/path.name).write_bytes(path.read_bytes())
log=root/'client.log'
if log.exists():
    lines=log.read_text(errors='replace').splitlines();selected=set()
    for i,line in enumerate(lines):
        if any(word in line for word in ('Exception','Caused by:','FATAL','CLIENT_VISUAL_QA','Rendering screen')):
            selected.update(range(max(0,i-3),min(len(lines),i+25)))
    print('CLIENT_QA_RUNTIME_DIAGNOSTICS\n'+'\n'.join(lines[i] for i in sorted(selected)))
print('CLIENT_QA_RESULT', (root/'result.json').read_text() if (root/'result.json').exists() else 'No result')
for name in ['artifact-melting-ready','artifact-melting-result','cruncher-feeding-ready','cruncher-feeding-result','ancient-anvil-tier0','ancient-anvil-tier1','ancient-anvil-panel0','ancient-anvil-panel1','ancient-anvil-guide','forge-alloy-ready','forge-alloy-result','00-workshop-manual-item','failure','forge-complete','forge-legacy-storage','forge-incomplete','forge-repaired','forge-repair-panel','forge-ghost-layout','forge-ghost-occupied','forge-display','forge-display-close','forge-world-ruin']+['manual-'+view for view in ('home','first-step','creature-index','creature-article','related','excavation','search','modifier','tiers','topics','small','narrow-home','narrow-index','narrow-search','narrow-creature','narrow')]:
    path=root/'captures'/(name+'.png')
    if not path.exists():continue
    payload=base64.b64encode(path.read_bytes()).decode();chunks=[payload[i:i+7000] for i in range(0,len(payload),7000)]
    for i,chunk in enumerate(chunks):print(f'MC_QA_IMAGE:{name}:{i+1}/{len(chunks)}:{chunk}')
