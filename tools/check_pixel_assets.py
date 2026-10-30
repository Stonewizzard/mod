"""Check vanilla pixel scale, alpha, margins, visual size and silhouette integrity."""
from collections import deque
from pathlib import Path
from PIL import Image
from generate_pixel_assets import OUTPUTS, generate

def problems(im, item=True):
 errors=[]
 if im.size!=(32,32):return ['expected 32px file containing a 16px logical grid']
 px=im.load()
 if any(px[x,y]!=px[x//2*2,y//2*2] for x in range(32) for y in range(32)):
  errors.append('pixels must align to the 16px logical grid')
 im=im.resize((16,16),Image.Resampling.NEAREST);px=im.load()
 if any(a not in (0,255) for *_,a in im.get_flattened_data()):errors.append('partial alpha creates broken extrusion edges')
 if len(set(im.get_flattened_data()))>24:errors.append('palette exceeds 24 colors')
 solid={(x,y) for x in range(16) for y in range(16) if px[x,y][3]}
 if not item:
  if len(solid)!=256:errors.append('block material must be opaque')
  return errors
 if not solid:return errors+['empty sprite']
 if any(x in (0,15) or y in (0,15) for x,y in solid):errors.append('sprite clips its one-pixel margin')
 box=im.getbbox();longest=max(box[2]-box[0],box[3]-box[1])
 if not 11<=longest<=14:errors.append(f'visual extent {longest}px; expected 11–14px')
 def flood(start,allowed):
  seen={start};q=deque([start])
  while q:
   x,y=q.popleft()
   # Diagonal joins are valid for Minecraft tools and their extruded silhouettes.
   for dx in (-1,0,1):
    for dy in (-1,0,1):
     p=(x+dx,y+dy)
     if p in allowed and p not in seen:seen.add(p);q.append(p)
  return seen
 remaining=set(solid)
 while remaining:
  component=flood(next(iter(remaining)),remaining);remaining-=component
  if len(component)<3:errors.append('isolated loose pixel or tiny sprite fragment')
 # Four-neighbor exterior fill detects even a single fully enclosed missing pixel.
 air={(x,y) for x in range(16) for y in range(16)}-solid
 exterior={p for p in air if 0 in p or 15 in p};q=deque(exterior)
 while q:
  x,y=q.popleft()
  for p in ((x-1,y),(x+1,y),(x,y-1),(x,y+1)):
   if p in air and p not in exterior:exterior.add(p);q.append(p)
 if air-exterior:errors.append('enclosed transparent pinhole (author an explicit exception for intentional rings)')
 return errors

def main():
 generate();failures=[]
 for path in OUTPUTS:
  with Image.open(path) as im:
   errors=problems(im.convert('RGBA'),path.parent.name=='item')
  failures.extend(f'{path.name}: {e}' for e in errors)
 if failures:raise SystemExit('\n'.join(failures))
 print(f'PASS: {len(OUTPUTS)} assets: grid, palette, alpha, margins, size, loose pixels and pinholes.')
if __name__=='__main__':main()
