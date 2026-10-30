"""Original, grid-authored Minecraft assets. 16 logical pixels; 32px GUI-compatible files.
Run --check to reject stale outputs. No filtering, antialiasing or random generators.
Requires Pillow (tools/requirements-assets.txt).
"""
from pathlib import Path
from io import BytesIO
import argparse
from PIL import Image, ImageDraw
ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/examplemod/textures'
O='#24242b'; D='#4b4035'; B='#91683b'; G='#c99a48'; H='#f2d38b'; T='#3b9a91'
OUTPUTS={}
def canvas(bg=None):
 im=Image.new('RGBA',(16,16),bg or (0,0,0,0));return im,ImageDraw.Draw(im)
def save(kind,name,im):
 OUTPUTS[OUT/kind/(name+'.png')]=im.resize((32,32),Image.Resampling.NEAREST)
def poly(d,points,fill,outline=O):d.polygon(points,fill=fill,outline=outline)
def metal(d,points):poly(d,points,G)
def ingot():
 im,d=canvas();poly(d,[(1,8),(5,4),(12,4),(14,7),(14,10),(10,12),(2,11)],B)
 poly(d,[(2,8),(5,5),(11,5),(13,7),(10,9),(3,9)],H)
 d.line([(3,10),(10,11),(13,9)],fill=G);d.line([(6,6),(10,6)],fill='#fff0be');d.point((10,8),fill=T)
 save('item','ancient_ingot',im)
 im,d=canvas();poly(d,[(2,7),(5,4),(8,5),(11,3),(14,6),(12,9),(13,11),(9,12),(6,11),(3,13),(1,10)],D)
 for a,b,c in [(3,7,6),(8,6,11),(5,10,8)]:d.line((a,b,c,b-1),fill=G,width=2)
 d.point((11,5),fill=H);save('item','ancient_scrap',im)
def relic(name,color,light):
 im,d=canvas();poly(d,[(2,6),(5,2),(10,2),(14,6),(13,11),(9,14),(4,13),(1,9)],D)
 poly(d,[(3,6),(6,3),(10,3),(12,6),(11,11),(8,13),(4,11)],color)
 d.line([(4,6),(7,4),(10,4)],fill=light);d.line([(5,11),(8,12),(11,10)],fill=B)
 d.line([(6,7),(9,6),(10,8),(8,10),(6,9),(6,7)],fill=G);d.point((8,8),fill=light)
 save('item',name,im)
def tool(name):
 im,d=canvas();resonant=name.startswith('resonant_');precision=name.startswith('precision_')
 m='#807095' if resonant else '#80b5b8' if precision else G
 hi='#c8a5d8' if resonant else '#d4eeeb' if precision else H
 # A solid diagonal handle, connected to each working end.
 d.line((3,13,11,5),fill=O,width=4);d.line((3,13,11,5),fill=B,width=2);d.line((4,12,10,6),fill=G)
 if 'pickaxe' in name:
  poly(d,[(3,3),(8,2),(12,4),(14,8),(13,10),(11,6),(7,5),(3,5)],m);d.line([(4,3),(8,3),(11,4)],fill=hi)
 elif 'axe' in name:
  poly(d,[(8,2),(12,2),(14,5),(12,8),(9,7),(7,5)],m);d.line([(10,3),(12,3),(13,5)],fill=hi)
 elif 'shovel' in name:
  poly(d,[(9,1),(13,2),(14,5),(12,8),(8,6),(7,3)],m);d.line([(10,2),(12,3),(13,5)],fill=hi)
 elif 'hoe' in name:
  poly(d,[(5,2),(11,2),(13,4),(11,6),(9,4),(5,4),(4,7),(3,7),(3,4)],m);d.line((5,3,10,3),fill=hi)
 elif 'sword' in name or 'spear' in name:
  poly(d,[(6,9),(11,2),(14,1),(13,5),(8,11)],m);d.line((8,8,12,3),fill=hi);d.line((5,7,9,11),fill=G,width=2)
 elif 'mace' in name or 'hammer' in name:
  poly(d,[(6,2),(9,1),(14,6),(12,9),(10,8),(5,4)],m);d.line((7,2,12,6),fill=hi)
 elif 'brush' in name:
  poly(d,[(8,3),(10,1),(14,5),(12,7),(10,7),(7,4)],H);d.line((9,3,12,6),fill=B)
 elif 'drill' in name:
  poly(d,[(8,3),(11,2),(14,4),(13,7),(10,8),(7,5)],m);poly(d,[(11,3),(13,1),(14,1),(14,4)],hi)
 elif 'lens' in name or name=='magnifying_glass':
  d.ellipse((6,1,14,9),fill=O);d.ellipse((7,2,13,8),fill=m);d.ellipse((8,3,12,7),fill='#5d9299');d.line((9,3,11,3),fill='#d6eee5')
 else:raise ValueError(name)
 save('item',name,im)
def part(name):
 im,d=canvas()
 if 'handle' in name:
  poly(d,[(5,2),(7,1),(11,13),(9,14)],B);d.line((6,3,9,12),fill=G)
 elif 'connector' in name:
  poly(d,[(2,6),(6,6),(6,2),(9,2),(9,6),(13,6),(13,9),(9,9),(9,13),(6,13),(6,9),(2,9)],G);d.rectangle((6,6,9,9),fill=D);d.point((7,7),fill=T)
 elif 'fragments' in name:
  poly(d,[(2,5),(6,3),(9,5),(8,9),(12,8),(14,11),(9,14),(5,11),(2,12)],B);d.line([(3,6),(6,4),(8,5)],fill=H);d.line((7,10,11,11),fill=G)
 elif 'bridge' in name:
  poly(d,[(2,5),(5,5),(5,4),(10,4),(10,5),(13,5),(13,10),(10,10),(10,11),(5,11),(5,10),(2,10)],G);d.rectangle((6,5,9,10),fill=D);d.line((2,6,4,6),fill=H);d.point((7,7),fill=T)
 elif 'tip' in name and 'pickaxe' in name:
  points=[(2,3),(6,4),(10,7),(13,12),(12,13),(8,10),(4,8)]
  if 'left' in name:points=[(15-x,y) for x,y in points]
  poly(d,points,G);d.line(points[:3],fill=H)
 elif name=='resonant_core':
  poly(d,[(7,1),(10,4),(10,7),(13,5),(13,10),(8,14),(3,11),(2,7),(5,8),(5,4)],'#268c87');d.line([(7,3),(8,10),(7,12)],fill='#91e5cf');d.line((10,9,11,8),fill='#91e5cf')
 else:
  # Component heads have their own silhouettes, without stray handle fragments.
  if 'pickaxe' in name:
   poly(d,[(2,6),(6,4),(10,4),(13,7),(14,11),(12,10),(10,7),(6,6),(3,8)],G);d.line([(3,6),(6,5),(10,5)],fill=H)
  elif 'axe' in name:
   poly(d,[(5,3),(10,2),(13,5),(12,10),(8,12),(6,9),(3,8)],G);d.line([(7,3),(10,3),(12,5)],fill=H);d.rectangle((5,5,6,7),fill=D)
  elif 'shovel' in name:
   poly(d,[(5,2),(10,2),(12,5),(11,10),(8,13),(4,10),(3,5)],G);d.line([(5,3),(9,3),(11,5)],fill=H);d.line((7,6,7,10),fill=B)
  elif 'hoe' in name:
   poly(d,[(3,4),(10,3),(12,5),(11,9),(9,9),(9,6),(5,6),(3,10),(2,10)],G);d.line((4,4,9,4),fill=H)
  elif 'mace' in name:
   poly(d,[(5,2),(10,2),(13,5),(12,11),(9,13),(3,10),(2,5)],B);d.rectangle((5,4,10,10),fill=G);d.line((6,4,9,4),fill=H);d.rectangle((7,6,8,8),fill=T)
  else:
   poly(d,[(4,12),(6,7),(10,2),(13,1),(12,5),(8,10),(6,14)],G);d.line((6,10,11,3),fill=H)
 save('item',name,im)
def book():
 im,d=canvas();poly(d,[(2,4),(10,1),(14,4),(14,12),(6,14),(2,11)],'#733c35')
 poly(d,[(3,5),(10,3),(12,4),(12,10),(5,12),(3,10)],'#a45441');d.line([(5,12),(13,10),(13,12),(6,13)],fill='#dfc899',width=1)
 d.line([(3,5),(5,6),(5,11)],fill=G);d.line([(6,5),(10,4),(11,5),(7,6),(6,5)],fill=G);d.rectangle((8,7,9,9),fill=T)
 save('item','ancient_field_guide',im)
def egg(name,base,spots):
 im,d=canvas();d.polygon([(6,1),(9,1),(12,5),(13,10),(11,13),(4,13),(2,10),(3,5)],fill=O)
 d.polygon([(6,2),(9,2),(11,5),(12,10),(10,12),(5,12),(3,10),(4,5)],fill=base)
 for x,y in [(6,3),(9,5),(5,7),(8,9),(10,11)]:d.rectangle((x,y,x+1,y+1),fill=spots)
 d.line((4,8,4,10),fill='#b3b2a4');save('item',name,im)
def materials():
 palettes={
 'forge_stone':['#62615b','#77766d','#85847a','#6c6c64','#55554f'],
 'forge_soot':['#343535','#424342','#4b4b46','#383a39','#292b2b']}
 for name,p in palettes.items():
  im,d=canvas(p[0]);
  for row in range(2):
   y=row*8;offset=0 if row==0 else -8
   for x in range(offset,16,16):
    d.rectangle((x+1,y+1,x+14,y+6),fill=p[1]);d.line((x+1,y+1,x+14,y+1),fill=p[2]);d.line((x+1,y+6,x+14,y+6),fill=p[4]);d.line((x+14,y+2,x+14,y+5),fill=p[3]);d.line((x+4,y+3,x+7,y+3),fill=p[3]);d.line((x+10,y+5,x+12,y+5),fill=p[0])
  d.line([(11,1),(10,3),(11,4),(10,6)],fill=p[4]);save('block',name,im)
 for name,base,mid,light in [('anvil_metal','#383b40','#42464b','#50555a'),('anvil_top','#454a50','#535a60','#646c71'),('anvil_gold',B,G,H),('forge_bronze','#735537','#96703c','#b48b4a'),('forge_ash','#514e46','#615d53','#777162'),('table_oak','#70502f','#886239','#a37b49'),('table_joinery','#573e2a','#70502f','#886239'),('table_parchment','#cfb984','#deca9a','#eddbb0'),('table_mat','#414b35','#536040','#65744b')]:
  im,d=canvas(base)
  for x,y,length in [(2,3,3),(10,5,2),(5,11,4),(12,14,2)]:d.line((x,y,x+length,y),fill=mid)
  d.line((3,2,5,2),fill=light)
  if name in ('table_oak','table_joinery'):
   for y in (0,5,10,15):d.line((0,y,15,y),fill=base);d.line((0,y+1,15,y+1),fill=light)
  if name=='anvil_top':d.line((4,6,9,6),fill=mid);d.line((7,10,11,10),fill=mid)
  save('block',name,im)
 im,d=canvas('#383b40');d.line((0,1,15,1),fill=G);d.line((0,2,15,2),fill=B)
 # One restrained maker's mark, not a wallpaper of tiny runes.
 d.line([(6,10),(8,10),(8,11),(7,12),(6,11),(6,10)],fill=G);d.point((7,11),fill=H);save('block','anvil_runes',im)
def generate():
 ingot();materials();book()
 for name,color,light in [('ancient_artifact','#876345','#c5a478'),('uncommon_ancient_artifact','#65724c','#b0be7a'),('rare_ancient_artifact','#64517c','#bc99d0'),('epic_ancient_artifact','#36777d','#80cbc2'),('legendary_ancient_artifact','#a7752d','#f0d284'),('unique_ancient_artifact','#934c4b','#e0a19a')]:relic(name,color,light)
 for name in ['archaeology_hammer','archaeology_drill','magnifying_glass','brush_guide']+[f'{tier}_{toolname}' for tier in ('precision','resonant') for toolname in ('hammer','drill','lens','brush')]+['restored_ancient_'+t for t in ('pickaxe','axe','shovel','hoe','sword','spear','mace')]:tool(name)
 for name in ['ancient_tool_handle','ancient_tool_connector','ancient_fragments','pickaxe_head_bridge','pickaxe_left_tip','pickaxe_right_tip','resonant_core']+['ancient_'+t for t in ('pickaxe_head','axe_head','shovel_head','hoe_head','sword_blade','spear_tip','mace_head')]:part(name)
 egg('cruncher_egg','#50352d','#d47d32');egg('skulk_parasite_spawn_egg','#264447','#659b91')
def main():
 parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--check',action='store_true');args=parser.parse_args();generate();stale=[]
 for path,im in OUTPUTS.items():
  buf=BytesIO();im.save(buf,format='PNG');data=buf.getvalue()
  if args.check:
   if not path.exists() or path.read_bytes()!=data:stale.append(str(path.relative_to(ROOT)))
  else:path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(data)
 if stale:raise SystemExit('Stale pixel assets; run python tools/generate_pixel_assets.py:\n'+'\n'.join(stale))
 print(f'{"Verified" if args.check else "Authored"} {len(OUTPUTS)} pixel assets.')
if __name__=='__main__':main()
