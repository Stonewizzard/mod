"""Author the runic centerpiece, its real owned cells and matching physics.

Coordinates are native Minecraft pixels in a 3x3x2 reserved volume. Original cell
indices 0..5 are deliberately stable for old saved blockstates.
"""
from pathlib import Path
import copy,json,math
import anvil_horn

ROOT=Path(__file__).resolve().parents[1];ASSETS=ROOT/'src/main/resources/assets/examplemod'
def j(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2)+'\n')
cells=[(0,0,0),(0,0,-1),(0,0,1),(0,1,-1),(0,1,0),(0,1,1)]
cells += [(x,y,z) for x in (-1,1) for y in (0,1) for z in (-1,0,1)]
TEXTURES={'metal':'examplemod:block/runic_anvil_metal','steel':'examplemod:block/runic_anvil_edge',
 'bronze':'examplemod:block/runic_anvil_frame','soot':'examplemod:block/forge_soot',
 'top':'examplemod:block/runic_anvil_top','top_glow':'examplemod:block/runic_anvil_top_glow',
 'panel':'examplemod:block/runic_anvil_panel','panel_glow':'examplemod:block/runic_anvil_panel_glow',
 'ember':'examplemod:block/runic_anvil_ember','horn':'examplemod:block/runic_anvil_horn',
 'fall':'examplemod:block/runic_anvil_fall',
 'particle':'examplemod:block/runic_anvil_metal'}
DESIGNS={'base':{'offset':0,'narrow':0},'foundry':{'offset':4,'narrow':1},
         'spillway':{'offset':2,'narrow':0},'columns':{'offset':4,'narrow':2}}

def build(design,cfg):
 n=cfg['narrow'];dz=cfg['offset'];prefix='ancient_anvil'+anvil_horn.suffix(design)
 textures=dict(TEXTURES)
 if design=='base':textures.pop('fall')
 boxes=[];decor=[]
 def box(a,b,t='metal',decoration=False):
  (decor if decoration else boxes).append((list(a),list(b),t))
 def octagon(x0,z0,x1,z1,y0,y1,cut,t):
  # Native-pixel chamfers with one-pixel steps; no arbitrary stretched texture.
  start=z0;last=None
  for z in range(z0,z1):
   inset=max(0,cut-min(z-z0,z1-z-1)-1)
   if last is not None and inset!=last:
    box((x0+last,y0,start),(x1-last,y1,z),t);start=z
   last=inset
  box((x0+last,y0,start),(x1-last,y1,z1),t)
 
 # A low armored plinth, four hollow connection sockets and an inset circuit.
 octagon(-14,-12,30,28,0,2,4,'soot')
 octagon(-13,-11,29,27,2,5,3,'metal')
 octagon(-12,-10,28,26,5,6,3,'bronze')
 octagon(-11,-9,27,25,6,7,3,'metal')
 for x in (-9,25):
  for z in (-7,23):
   box((x-4,2,z-4),(x+4,7,z+4),'metal')
   box((x-3,7,z-3),(x+3,8,z+3),'soot')
   for a,b in [((x-3,7,z-3),(x-2,10,z+3)),((x+2,7,z-3),(x+3,10,z+3)),
               ((x-2,7,z-3),(x+2,10,z-2)),((x-2,7,z+2),(x+2,10,z+3))]:box(a,b,'bronze')
   # The same bronze straps continue down the plinth, anchoring the sockets.
   box((x-1.5,0,z-4.03),(x+1.5,7,z-3.97),'bronze',True)
 for a,b in [((-9,7.025,-7),(25,7.05,-6)),((-9,7.025,22),(25,7.05,23)),
             ((-9,7.025,-6),(-8,7.05,22)),((24,7.025,-6),(25,7.05,22))]:box(a,b,'ember',True)
 
 # Broad shoulders and a deeply supported waist, rather than a tall narrow post.
 octagon(-6,-4,22,20,7,10,3,'metal')
 octagon(-3+n,-2+n,19-n,18-n,10,12,2,'metal')
 octagon(-2+n,n,18-n,16-n,12,18,2,'metal')
 octagon(-3+n,-3+n,19-n,19-n,18,21,3,'metal')
 octagon(-8,-6,24,22,21,23,4,'metal')
 
 # A substantial anvil head; the main working surface is a runic octagonal seal.
 octagon(-8,-2+dz,24,24+dz,23,27,4,'metal')
 octagon(-9,-3+dz,25,25+dz,27,28,4,'bronze')
 octagon(-7,-1+dz,23,23+dz,28,29,3,'steel')
 # Only the upper face receives the continuous 48x48 seal; emissive pixels sit just above it.
 for a,b,t in boxes[:]:
  if t=='steel' and a[1]==28:
   boxes[boxes.index((a,b,t))]=(a,b,'workface')
   box((a[0],29.015,a[2]),(b[0],29.025,b[2]),'top_glow',True)
 
 # The raised, square heel balances the pointed horn, with a heavy bronze cuff.
 box((-2,23,24+dz),(18,30,31),'metal')
 box((-3,24,23+dz),(19,30.5,25+dz),'bronze')
 box((-1,30,25+dz),(17,31,31),'steel')
 # The horn joins the working surface directly; no bulky cuff across its neck.
 
 # Intentional raised rune plaques and corner armor. Their top/bottom attach to
 # the broad shoulders; the narrower body is visible behind the flanking ribs.
 box((-4.5+n,9,0),(-3.5+n,22,16),'panel_ew')
 box((19.5-n,9,0),(20.5-n,22,16),'panel_ew')
 box((0,9,-5.5+n),(16,22,-4.5+n),'panel_ns')
 box((0,9,20.5-n),(16,22,21.5-n),'panel_ns')
 for x in (-5+n,20-n):
  for z in (-1,15):box((x,9,z),(x+1,22,z+2),'bronze')
 for z in (-6+n,21-n):
  for x in (-1,15):box((x,9,z),(x+2,22,z+1),'bronze')
 box((-4.53+n,9,0),(-4.52+n,22,16),'rune_w',True)
 box((20.52-n,9,0),(20.53-n,22,16),'rune_e',True)
 box((0,9,-5.53+n),(16,22,-5.52+n),'rune_n',True)
 box((0,9,21.52-n),(16,22,21.53-n),'rune_s',True)
 
 # Static OBJ geometry gives the horn a real bevel and rising underside. It is
 # baked once, entirely inside the upper front owned cell, with no entity renderer.
 anvil_horn.write_models(ASSETS/'models/block',design)
 
 # Four real outlets pour into the existing corner cups. Lava is baked visual
 # geometry, while the open troughs and frames share the assembly's collision.
 if design!='base':
  mouth={'foundry':26.5,'spillway':24.5,'columns':27.5}[design]
  width={'foundry':1.7,'spillway':3.4,'columns':2.2}[design]
  depth=1.5 if design!='spillway' else 1.1
  for x in (-9,25):
   for z in (-7,23):
    sign=-1 if z<8 else 1;landing=z+sign*.3
    box((x-width/2,8.65,landing-depth/2),(x+width/2,mouth+.04,landing+depth/2),'fall',True)
    box((x-1.92,8.6,z-1.92),(x+1.92,8.7,z+1.92),'ember',True)
    start=dz+3 if sign<0 else z-3;end=z-sign*.5
    lo,hi=sorted((start,end))
    box((x-2.5,mouth-1,lo),(x+2.5,mouth-.12,hi),'metal')
    for left,right in ((x-2.5,x-1.9),(x+1.9,x+2.5)):
     box((left,mouth-.4,lo-.4),(right,mouth+.55,hi+.4),'bronze')
    flowlo,flowhi=sorted((start,landing+sign*.7))
    box((x-1.8,mouth-.08,flowlo),(x+1.8,mouth+.015,flowhi),'ember',True)
    if design=='columns':
     for left,right in ((x-2.5,x-1.7),(x+1.7,x+2.5)):
      box((left,10,z-1.8),(right,mouth+.2,z+1.8),'bronze')
     box((x-3,mouth+.2,z-2.5),(x+3,mouth+.9,z+2.5),'metal')
 
 def clip(b,cell):
  a,B,t=b;offset=[v*16 for v in cell]
  lo=[max(a[i],offset[i]) for i in range(3)];hi=[min(B[i],offset[i]+16) for i in range(3)]
  if any(a>=b for a,b in zip(lo,hi)):return None
  return ([lo[i]-offset[i] for i in range(3)],[hi[i]-offset[i] for i in range(3)],t)
 
 def element(b,cell):
  a,B,t=b;x,y,z=a;X,Y,Z=B;cx,cy,cz=[v*16 for v in cell]
  uv={'north':[16-X,16-Y,16-x,16-y],'south':[x,16-Y,X,16-y],
      'west':[z,16-Y,Z,16-y],'east':[16-Z,16-Y,16-z,16-y],'up':[x,z,X,Z],'down':[x,16-Z,X,16-z]}
  material='steel' if t=='workface' else 'metal' if t.startswith('panel_') else t
  faces={f:{'texture':'#'+material,'uv':v} for f,v in uv.items()}
  if t in ('workface','top_glow'):
   faces['up']={'texture':'#top' if t=='workface' else '#top_glow','uv':[(x+cx+16)/3,(z+cz-dz+16)/3,(X+cx+16)/3,(Z+cz-dz+16)/3]}
   if t=='top_glow':faces={'up':faces['up']}
  if t.startswith('panel_') or t.startswith('rune_'):
   targets=('west','east') if t=='panel_ew' else ('north','south') if t=='panel_ns' else ({'w':'west','e':'east','n':'north','s':'south'}[t[-1]],)
   if t.startswith('rune_'):faces={}
   for f in targets:
    u,U=(z+cz,Z+cz) if f in ('west','east') else (x+cx,X+cx)
    if f in ('east','north'):u,U=16-U,16-u
    faces[f]={'texture':'#panel_glow' if t.startswith('rune_') else '#panel','uv':[u,24-(Y+cy),U,24-(y+cy)]}
  e={'from':a,'to':B,'faces':faces}
  if t in ('top_glow','ember','fall') or t.startswith('rune_'):e.update({'shade':False,'light_emission':15})
  return e
 
 collisions=[];models=[]
 for i,cell in enumerate(cells):
  mesh=[c for b in boxes if (c:=clip(b,cell))]
  collisions.append(mesh+anvil_horn.collision(cell,design))
  elements=[element(b,cell) for b in mesh]
  elements += [element(c,cell) for b in decor if (c:=clip(b,cell))]
  models.append(elements)
  result={'parent':'minecraft:block/block','render_type':'minecraft:cutout','ambientocclusion':True,'textures':textures,'elements':elements}
  if i==3:
   body=copy.deepcopy(result)
   result.pop('elements');result.update({'loader':'neoforge:composite','children':{'body':body,'horn':anvil_horn.model(False,design)}})
  j(ASSETS/f'models/block/{prefix}_cell_{i}.json',result)
 j(ASSETS/'anvil_layout.json',{'version':2,'footprint':[3,3,2],'cells':[{'index':i,'x':x,'y':y,'z':z} for i,(x,y,z) in enumerate(cells)]})
 
 # Ordinary block-item envelope: uniformly shrink the FULL broad anvil, never skew it.
 preview=[]
 for (cx,cy,cz),elements in zip(cells,models):
  def scaled(p):return [8+(p[0]+cx*16-8)/3,2+(p[1]+cy*16)/3,8+(p[2]+cz*16-8)/3]
  for source in elements:
   e=copy.deepcopy(source);e['from']=scaled(e['from']);e['to']=scaled(e['to'])
   if 'rotation' in e:e['rotation']['origin']=scaled(e['rotation']['origin'])
   preview.append(e)
 j(ASSETS/f'models/block/{prefix}_item.json',{'parent':'minecraft:block/block','render_type':'minecraft:cutout','textures':textures,
  'loader':'neoforge:composite','children':{'body':{'parent':'minecraft:block/block','textures':textures,'elements':preview},'horn':anvil_horn.model(True,design)},
  'display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.625,.625,.625]}}})
 if design!='base':j(ASSETS/f'items/{prefix}.json',{'model':{'type':'minecraft:model','model':f'examplemod:block/{prefix}_item'}})
 return boxes,collisions

def shape(mesh,whole=False):return ('WorkshopShapes.wholeRotations' if whole else 'WorkshopShapes.rotations')+'(new double[][]{'+','.join('{'+','.join(str(n) for n in a+B)+'}' for a,B,t in mesh)+'})'
def rotated_point(p,turn):
 x,y,z=p
 for _ in range(turn):x,z=16-z,x
 return [v/16 for v in (x,y,z)]

def geometry(design,boxes,collisions):
 class_name='AncientAnvil'+('' if design=='base' else design.title())+'Geometry'
 java=['package com.example.examplemod;','import net.minecraft.core.Direction;','import net.minecraft.world.phys.shapes.VoxelShape;',
  '/** Generated assembly geometry for the '+design+' visual design. */','public final class '+class_name+' {',
  '    private static final java.util.List<java.util.Map<Direction,VoxelShape>> CELLS=java.util.List.of('+','.join('cell'+str(i)+'()' for i in range(len(cells)))+');',
  '    private static final java.util.Map<Direction,VoxelShape> OUTLINE='+shape([b for b in boxes if not b[2].startswith('panel_')],True)+';',
  '    public static VoxelShape shape(Direction facing){return LegacyAncientAnvilGeometry.shape(facing);}',
  '    public static VoxelShape cell(int cell,Direction facing){return CELLS.get(cell).get(facing);}',
  '    public static VoxelShape outlineBody(Direction facing){return OUTLINE.get(facing);}']
 java += ['    private static java.util.Map<Direction,VoxelShape> cell'+str(i)+'(){return '+shape(mesh)+';}' for i,mesh in enumerate(collisions)]
 vertices=anvil_horn.vertices(design);edges=[(vertices[a],vertices[b]) for a,b in anvil_horn.EDGES]
 java.append('    private static final java.util.List<double[][]> NOSE_EDGES=java.util.List.of('+','.join('new double[][]{'+','.join('{'+','.join(str(v) for p in edge for v in rotated_point(p,turn))+'}' for edge in edges)+'}' for turn in range(4))+');')
 java.append('    public static double[][] noseEdges(Direction facing){return NOSE_EDGES.get(switch(facing){case EAST->1;case SOUTH->2;case WEST->3;default->0;});}')
 if design=='base':
  for method,kind,args,call in [('cell','VoxelShape','int cell,Direction facing','cell,facing'),('outlineBody','VoxelShape','Direction facing','facing'),('noseEdges','double[][]','Direction facing','facing')]:
   java.append('    public static '+kind+' '+method+'(AncientAnvilDesign design,'+args+'){return switch(design){case BASE->'+method+'('+call+');'+''.join('case '+d.upper()+'->AncientAnvil'+d.title()+'Geometry.'+method+'('+call+');' for d in DESIGNS if d!='base')+'};}')
 java+=['    private '+class_name+'(){}','}']
 (ROOT/f'src/main/java/com/example/examplemod/{class_name}.java').write_text('\n'.join(java)+'\n')

for design,cfg in DESIGNS.items():
 boxes,collisions=build(design,cfg);geometry(design,boxes,collisions)
j(ASSETS/'anvil_designs.json',{'designs':[{'id':d,'prefix':'ancient_anvil'+anvil_horn.suffix(d),'headOffset':cfg['offset'],'narrow':cfg['narrow']} for d,cfg in DESIGNS.items()]})
rots=[('north',0),('east',90),('south',180),('west',270)]
j(ASSETS/'blockstates/ancient_anvil.json',{'variants':{f'design={design},facing={d},tier={tier},large={str(large).lower()},monumental={str(wide).lower()}':{'model':'examplemod:block/'+('ancient_anvil'+anvil_horn.suffix(design)+'_cell_0' if large and wide else 'legacy_ancient_anvil_cell_0' if large else 'ancient_anvil'),'y':rot} for design in DESIGNS for tier in (0,1) for large in (False,True) for wide in (False,True) for d,rot in rots}})
j(ASSETS/'blockstates/ancient_anvil_part.json',{'variants':{f'design={design},facing={d},cell={i},monumental={str(wide).lower()}':{'model':'examplemod:block/'+('legacy_ancient_anvil_cell_'+str(i) if not wide and i<6 else 'ancient_anvil'+anvil_horn.suffix(design)+f'_cell_{i}'),'y':rot} for design in DESIGNS for i in range(1,len(cells)) for wide in (False,True) for d,rot in rots}})
print('Generated baseline and three anvil designs: owned meshes, tapered horns, lava falls, collision, items and outlines.')
