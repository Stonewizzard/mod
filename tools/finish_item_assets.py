"""Technical finishing of original artwork: solid cutouts and intentional item envelopes.
This does not repaint every item into one style. Original sources remain versioned.
"""
from pathlib import Path
import argparse,json,math
from collections import deque
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];SOURCES=ROOT/'tools/asset_sources';OUT=ROOT/'src/main/resources/assets/examplemod/textures/item'
def pixels(im):return list(im.get_flattened_data() if hasattr(im,'get_flattened_data') else im.getdata())
def regions(points,diagonal=False):
 remaining=set(points);result=[]
 steps=[(-1,0),(1,0),(0,-1),(0,1)]+([(-1,-1),(-1,1),(1,-1),(1,1)] if diagonal else [])
 while remaining:
  seed=remaining.pop();group={seed};q=[seed]
  for x,y in q:
   for dx,dy in steps:
    k=x+dx,y+dy
    if k in remaining:remaining.remove(k);group.add(k);q.append(k)
  result.append(group)
 return result

def opaque(im):return {(x,y) for y in range(im.height) for x in range(im.width) if im.getpixel((x,y))[3]==255}
def holes(im):return [g for g in regions({(x,y) for y in range(im.height) for x in range(im.width)}-opaque(im)) if all(0<x<im.width-1 and 0<y<im.height-1 for x,y in g)]
def cutout(im):
 im=im.convert('RGBA');im.putdata([(r,g,b,255) if a>=128 else (0,0,0,0) for r,g,b,a in pixels(im)])
 # One-pixel alpha debris is not a second physical item. Keep real separated fragments.
 for group in regions(opaque(im),True):
  if len(group)==1:
   for p in group:im.putpixel(p,(0,0,0,0))
 for group in holes(im):
  if len(group)<=2:
   for x,y in group:
    neighbors=[im.getpixel(p) for p in [(x-1,y),(x+1,y),(x,y-1),(x,y+1)] if im.getpixel(p)[3]==255]
    if neighbors:im.putpixel((x,y),neighbors[0])
 return im

def envelope(im,longest=None):
 im=cutout(im);box=im.getbbox()
 if not box:raise ValueError('Empty sprite')
 crop=im.crop(box);longest=longest or min(max(crop.size),28)
 factor=longest/max(crop.size);size=tuple(max(1,round(n*factor)) for n in crop.size)
 crop=crop.resize(size,Image.Resampling.NEAREST);out=Image.new('RGBA',(32,32));out.alpha_composite(crop,((32-size[0])//2,(32-size[1])//2));return cutout(out)

def metals():
 atlas=Image.open(SOURCES/'ancient-metals-source.png').convert('RGBA');result={}
 for name,part,longest in [('ancient_ingot',(0,0,atlas.width//2,atlas.height),14),('ancient_scrap',(atlas.width//2,0,atlas.width,atlas.height),11)]:
  im=cutout(atlas.crop(part));im=im.crop(im.getbbox());size=tuple(max(1,round(n*longest/max(im.size))) for n in im.size);im=im.resize(size,Image.Resampling.NEAREST)
  palette=[(43,42,46),(68,65,71),(96,82,72),(117,81,56),(145,99,52),(178,123,58),(206,148,70),(238,185,91),(255,224,152),(49,107,104),(78,152,144),(131,182,173)];rgb=Image.new('RGBA',im.size);rgb.putdata([(*min(palette,key=lambda c:sum((c[i]-p[i])**2 for i in range(3))),p[3]) if p[3] else (0,0,0,0) for p in pixels(im)])
  out=Image.new('RGBA',(16,16));out.alpha_composite(cutout(rgb),((16-size[0])//2,(16-size[1])//2));result[name]=cutout(out.resize((32,32),Image.Resampling.NEAREST))
 return result

def build():
 result={}
 for p in sorted((SOURCES/'legacy-items').glob('*.png')):
  if p.stem in ['ancient_ingot','ancient_scrap']:continue
  # Keep the approved original artwork and color detail. Stone specimens are intentionally readable.
  target=24 if p.stem.endswith('ancient_artifact') or p.stem=='ancient_artifact' else None
  result[p.stem]=envelope(Image.open(p),target)
 result.update(metals());return result

def main():
 parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args();result=build();bad=[]
 for name,im in result.items():
  p=OUT/(name+'.png')
  if args.check:
   if not p.exists() or Image.open(p).convert('RGBA').tobytes()!=im.tobytes():bad.append(name)
  else:im.save(p)
 if bad:raise SystemExit('Stale finished sprites: '+', '.join(bad))
 print(('Checked' if args.check else 'Finished')+f' {len(result)} original item sprites; no blanket artwork replacement.')
if __name__=='__main__':main()
