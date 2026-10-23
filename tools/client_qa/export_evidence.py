"""Publish a few actual window captures through text logs for connector-based inspection.
All captures, live state, action history and video also remain in the Actions artifact.
"""
from pathlib import Path
import base64,json
root=Path('build/client-qa');print('CLIENT_QA_RESULT', (root/'result.json').read_text() if (root/'result.json').exists() else 'No result')
for name in ('01-contents','02-getting-started','05-modifier','11-small-window','failure'):
    path=root/'captures'/(name+'.png')
    if not path.exists():continue
    payload=base64.b64encode(path.read_bytes()).decode();chunks=[payload[i:i+7000] for i in range(0,len(payload),7000)]
    for i,chunk in enumerate(chunks):print(f'MC_QA_IMAGE:{name}:{i+1}/{len(chunks)}:{chunk}')
