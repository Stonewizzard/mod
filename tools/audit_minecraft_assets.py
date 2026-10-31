"""Fail on rendering defects, not on whether all artwork has the same aesthetic."""
from pathlib import Path
import json
from PIL import Image
from finish_item_assets import pixels,opaque,regions,holes
ROOT=Path(__file__).resolve().parents[1];ASSETS=ROOT/'src/main/resources/assets/examplemod'

def validate_image(im,rule):
 im=im.convert('RGBA');errors=[]
 if im.size!=(32,32):return ['UI sprite must be 32x32']
 if any(0<p[3]<255 for p in pixels(im)):errors.append('partial alpha causes held-item extrusion defects')
 box=im.getbbox()
 if not box:return errors+['empty sprite']
 if min(box[0],box[1],32-box[2],32-box[3])<1:errors.append('clipped margin')
 longest=max(box[2]-box[0],box[3]-box[1])
 if not rule['min_longest']<=longest<=rule['max_longest']:errors.append('wrong visual size for item role')
 groups=regions(opaque(im),True)
 if any(len(g)==1 for g in groups):errors.append('loose one-pixel fleck')
 if len(groups)>1 and not rule.get('separate_fragments'):errors.append('unapproved disconnected silhouette')
 actual=sorted(len(g) for g in holes(im))
 if actual!=rule.get('allowed_holes',[]):errors.append('unapproved transparent pinhole/interior gap')
 if (actual or rule.get('separate_fragments')) and not rule.get('exception_reason'):errors.append('undocumented intentional exception')
 if not rule.get('legacy_artwork'):
  for y in range(0,32,2):
   for x in range(0,32,2):
    if len({im.getpixel((x+dx,y+dy)) for dx in range(2) for dy in range(2)})>1:errors.append('new sprite not on its native 16px grid');return errors
 return errors

def main():
 manifest=json.loads((ROOT/'tools/item-asset-manifest.json').read_text());fail=[]
 for name,rule in manifest.items():
  p=ASSETS/'textures/item'/(name+'.png')
  for error in validate_image(Image.open(p),rule):fail.append(name+': '+error)
  if rule.get('model'):
   item=json.loads((ASSETS/'items'/(name+'.json')).read_text());model=item.get('model',{}).get('model')
   if model and model.startswith('examplemod:'):
    path=ASSETS/'models'/(model.split(':',1)[1]+'.json')
    if not path.exists():fail.append(name+': missing item model')
 for p in (ASSETS/'textures/item').glob('*.png'):
  if Image.open(p).size==(32,32) and p.stem not in manifest:fail.append(p.name+': classify new generic item in the manifest')
 for i in range(6):
  model=json.loads((ASSETS/'models/block'/f'ancient_anvil_cell_{i}.json').read_text())
  for e in model['elements']:
   if any(v<0 or v>16 for k in ['from','to'] for v in e[k]):fail.append(f'anvil cell{i}: geometry escapes its owned block')
 if fail:raise SystemExit('\n'.join(fail))
 print(f'PASS: {len(manifest)} sprites: alpha, visual size, margins, pinholes, flecks, model wiring; six owned anvil cells.')
if __name__=='__main__':main()
