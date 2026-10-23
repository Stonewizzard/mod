"""Decode actual screenshots exported by a GitHub Actions job log. No image reconstruction."""
import argparse,base64,re
from pathlib import Path
parser=argparse.ArgumentParser();parser.add_argument('log',type=Path);parser.add_argument('output',type=Path);args=parser.parse_args()
images={}
for name,index,total,data in re.findall(r'MC_QA_IMAGE:([A-Za-z0-9_-]+):(\d+)/(\d+):([A-Za-z0-9+/=]+)',args.log.read_text()):
    image=images.setdefault(name,{'total':int(total),'parts':{}})
    if image['total']!=int(total):raise ValueError('Inconsistent image chunk count')
    image['parts'][int(index)]=data
args.output.mkdir(parents=True,exist_ok=True)
for name,image in images.items():
    if set(image['parts'])!=set(range(1,image['total']+1)):raise ValueError('Incomplete screenshot: '+name)
    png=base64.b64decode(''.join(image['parts'][i] for i in range(1,image['total']+1)),validate=True)
    if not png.startswith(b'\x89PNG\r\n\x1a\n'):raise ValueError('Not a PNG screenshot')
    path=args.output/(name+'.png');path.write_bytes(png);print(path)
if not images:raise ValueError('No real screenshot evidence found in this log')
