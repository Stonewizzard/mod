"""Fail on rendering defects, not on whether all artwork has the same aesthetic."""
from pathlib import Path
import json,math,itertools
from collections import Counter
from PIL import Image
from finish_item_assets import pixels,opaque,regions,holes
ROOT=Path(__file__).resolve().parents[1];ASSETS=ROOT/'src/main/resources/assets/examplemod'

def obj_mesh(path):
 vertices=[];uvs=[];faces=[]
 for line in path.read_text().splitlines():
  words=line.split()
  if not words:continue
  if words[0]=='v':vertices.append(tuple(map(float,words[1:4])))
  elif words[0]=='vt':uvs.append(tuple(map(float,words[1:3])))
  elif words[0]=='f':faces.append(tuple(int(v.split('/')[0])-1 for v in words[1:]))
 return vertices,uvs,faces

def validate_horn_mesh(vertices,uvs,faces,preview=False,design='base'):
 errors=[]
 if not vertices or not faces:return ['missing tapered horn mesh']
 limits=(1,1,1 if preview or design=='base' else 1.25)
 if any(v<0 or v>limits[i]+1e-8 for p in vertices for i,v in enumerate(p)):errors.append('horn escapes its owned cells or item envelope')
 if any(v<0 or v>1 for p in uvs for v in p):errors.append('horn UV escapes its native tile')
 edges=Counter();directed=Counter();volume=0
 for face in faces:
  if len(face)!=3 or any(i<0 or i>=len(vertices) for i in face):return ['invalid horn triangle']
  for a,b in zip(face,face[1:]+face[:1]):edges[tuple(sorted((a,b)))]+=1;directed[(a,b)]+=1
  a,b,c=[vertices[i] for i in face]
  volume+=a[0]*(b[1]*c[2]-b[2]*c[1])+a[1]*(b[2]*c[0]-b[0]*c[2])+a[2]*(b[0]*c[1]-b[1]*c[0])
 if any(n!=2 for n in edges.values()) or any(n!=directed[(b,a)] for (a,b),n in directed.items()):errors.append('horn mesh is open or has inconsistent winding')
 if volume<=0:errors.append('horn faces point inward')
 if not preview:
  front=min(p[2] for p in vertices);tip=[p for p in vertices if abs(p[2]-front)<1e-7]
  width={'base':3.25,'foundry':4.25,'spillway':8.25,'columns':6.25}[design]
  if (max(p[0] for p in tip)-min(p[0] for p in tip))*16>width or (max(p[1] for p in tip)-min(p[1] for p in tip))*16>(1.3 if design=='base' else 2):errors.append('horn has a blunt full-height tip')
 return errors

def validate_image(im,rule):
 im=im.convert('RGBA');errors=[]
 if im.size!=(32,32):return ['UI sprite must be 32x32']
 if any(0<p[3]<255 for p in pixels(im)):errors.append('partial alpha causes held-item extrusion defects')
 box=im.getbbox()
 if not box:return errors+['empty sprite']
 if min(box[0],box[1],32-box[2],32-box[3])<1:errors.append('clipped margin')
 longest=max(box[2]-box[0],box[3]-box[1])
 if not rule['min_longest']<=longest<=rule['max_longest']:errors.append('wrong visual size for item role')
 if 'aspect_range' in rule and not rule['aspect_range'][0]<=(box[2]-box[0])/(box[3]-box[1])<=rule['aspect_range'][1]:errors.append('wrong aspect ratio for item role')
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
  if p.stem not in manifest:fail.append(p.name+': classify new generic item in the manifest')
 layout=json.loads((ASSETS/"anvil_layout.json").read_text())["cells"]
 designs=json.loads((ASSETS/'anvil_designs.json').read_text())['designs']
 for config in designs:
  for i in range(len(layout)):
   model=json.loads((ASSETS/'models/block'/f'{config["prefix"]}_cell_{i}.json').read_text())
   body=model.get('children',{}).get('body',model)
   for e in body['elements']:
    if 'rotation' not in e:
     if any(v<0 or v>16 for k in ['from','to'] for v in e[k]):fail.append(f'anvil cell{i}: geometry escapes its owned block')
    else:
     # The single faceted horn spans two ACTUALLY owned cells. Validate its real
     # rotated corners against that eighteen-cell volume, not its unrotated input box.
     r=e['rotation'];oxCell,oy,oz=[layout[i][k] for k in ('x','y','z')]
     if r['axis']!='y' or r.get('rescale') or r['angle']!=45:fail.append(f'anvil cell{i}: unsupported unverified rotation');continue
     ox,_,oZ=r['origin'];c=math.cos(math.radians(r['angle']));s=math.sin(math.radians(r['angle']))
     for x,y,z in itertools.product(*zip(e['from'],e['to'])):
      X=ox+(x-ox)*c+(z-oZ)*s+oxCell*16;Z=oZ-(x-ox)*s+(z-oZ)*c+oz*16;Y=y+oy*16
      if not (-16<=X<=32 and 0<=Y<=32 and -16<=Z<=32):fail.append(f'anvil cell{i}: rotated geometry escapes the owned assembly')
  for preview in (False,True):
   suffix='' if config['id']=='base' else '_'+config['id']
   path=ASSETS/'models/block'/('ancient_anvil_horn'+suffix+('_item' if preview else '')+'.obj')
   fail += [path.name+': '+error for error in validate_horn_mesh(*obj_mesh(path),preview,config['id'])]
 if fail:raise SystemExit('\n'.join(fail))
 print(f'PASS: {len(manifest)} sprites: alpha, visual size, margins, pinholes, flecks, model wiring; eighteen owned anvil cells.')
if __name__=='__main__':main()
