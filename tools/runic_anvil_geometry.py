"""Author the runic centerpiece, its real owned cells and matching physics.

Coordinates are native Minecraft pixels in a 3x3x2 reserved volume. Original cell
indices 0..5 are deliberately stable for old saved blockstates.
"""
from pathlib import Path
import copy,json,math

ROOT=Path(__file__).resolve().parents[1];ASSETS=ROOT/'src/main/resources/assets/examplemod'
def j(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2)+'\n')
cells=[(0,0,0),(0,0,-1),(0,0,1),(0,1,-1),(0,1,0),(0,1,1)]
cells += [(x,y,z) for x in (-1,1) for y in (0,1) for z in (-1,0,1)]
textures={'metal':'examplemod:block/runic_anvil_metal','steel':'examplemod:block/anvil_workface_edge',
 'bronze':'examplemod:block/forge_bronze','soot':'examplemod:block/forge_soot',
 'top':'examplemod:block/runic_anvil_top','top_glow':'examplemod:block/runic_anvil_top_glow',
 'panel':'examplemod:block/runic_anvil_panel','panel_glow':'examplemod:block/runic_anvil_panel_glow',
 'ember':'examplemod:block/runic_anvil_ember','horn':'examplemod:block/runic_anvil_horn',
 'particle':'examplemod:block/runic_anvil_metal'}
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
for a,b in [((-9,6.025,-7),(25,6.05,-6)),((-9,6.025,22),(25,6.05,23)),
            ((-9,6.025,-6),(-8,6.05,22)),((24,6.025,-6),(25,6.05,22))]:box(a,b,'ember',True)

# Broad shoulders and a deeply supported waist, rather than a tall narrow post.
octagon(-6,-4,22,20,7,10,3,'metal')
octagon(-4,-2,20,18,10,12,2,'metal')
octagon(-2,0,18,16,12,18,2,'metal')
octagon(-5,-3,21,19,18,21,3,'metal')
octagon(-8,-6,24,22,21,23,4,'metal')

# A substantial anvil head; the main working surface is a runic octagonal seal.
octagon(-10,-7,26,23,23,26.5,5,'metal')
octagon(-11,-8,27,24,26.5,28,5,'bronze')
octagon(-9,-7,25,23,28,29,4,'steel')
# Only the upper face receives the continuous 48x48 seal; emissive pixels sit just above it.
for a,b,t in boxes[:]:
 if t=='steel' and a[1]==28:
  boxes[boxes.index((a,b,t))]=(a,b,'workface')
  box((a[0],29.015,a[2]),(b[0],29.025,b[2]),'top_glow',True)

# The raised, square heel balances the pointed horn, with a heavy bronze cuff.
box((-3,23,23),(19,30,31),'metal')
box((-4,24,23),(20,30.5,25),'bronze')
box((-2,30,25),(18,31,30),'steel')

# Intentional raised rune plaques and corner armor. Their top/bottom attach to
# the broad shoulders; the narrower body is visible behind the flanking ribs.
box((-6.5,9,0),(-5.5,22,16),'panel_ew')
box((21.5,9,0),(22.5,22,16),'panel_ew')
box((0,9,-5.5),(16,22,-4.5),'panel_ns')
box((0,9,20.5),(16,22,21.5),'panel_ns')
for x in (-7,22):
 for z in (-1,15):box((x,9,z),(x+1,22,z+2),'bronze')
for z in (-6,21):
 for x in (-1,15):box((x,9,z),(x+2,22,z+1),'bronze')
box((-6.53,9,0),(-6.52,22,16),'rune_w',True)
box((22.52,9,0),(22.53,22,16),'rune_e',True)
box((0,9,-5.53),(16,22,-5.52),'rune_n',True)
box((0,9,21.52),(16,22,21.53),'rune_s',True)

# A bronze-bound, genuine diagonal horn, not a stack of tiny cuboid teeth.
horn={'from':[1,7,-12],'to':[15,11.5,2],
      'rotation':{'origin':[8,9.25,-5],'axis':'y','angle':45,'rescale':False},
      'faces':{f:{'texture':'#horn' if f=='up' else '#metal','uv':[1,1,15,15] if f in ('up','down') else [1,7,15,11.5]} for f in ('up','down','north','south','west','east')}}
radius=math.sqrt(98)

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
  faces['up']={'texture':'#top' if t=='workface' else '#top_glow','uv':[(x+cx+16)/3,(z+cz+16)/3,(X+cx+16)/3,(Z+cz+16)/3]}
  if t=='top_glow':faces={'up':faces['up']}
 if t.startswith('panel_') or t.startswith('rune_'):
  targets=('west','east') if t=='panel_ew' else ('north','south') if t=='panel_ns' else ({'w':'west','e':'east','n':'north','s':'south'}[t[-1]],)
  if t.startswith('rune_'):faces={}
  for f in targets:
   u,U=(z+cz,Z+cz) if f in ('west','east') else (x+cx,X+cx)
   if f in ('east','north'):u,U=16-U,16-u
   faces[f]={'texture':'#panel_glow' if t.startswith('rune_') else '#panel','uv':[u,24-(Y+cy),U,24-(y+cy)]}
 e={'from':a,'to':B,'faces':faces}
 if t in ('top_glow','ember') or t.startswith('rune_'):e.update({'shade':False,'light_emission':15})
 return e

def horn_collision(cell):
 cx,cy,cz=cell
 if cy!=1:return []
 lo=max(-5-radius,cz*16);hi=min(-5+radius,cz*16+16)
 if lo>=hi:return []
 cuts=sorted(set([lo,hi]+[float(n) for n in range(math.ceil(lo),math.floor(hi)+1)]));result=[]
 for a,b in zip(cuts,cuts[1:]):
  nearest=0 if a<=-5<=b else min(abs(a+5),abs(b+5));half=radius-nearest
  L=max(8-half,cx*16);R=min(8+half,cx*16+16)
  if L<R:result.append(([L-cx*16,7,a-cz*16],[R-cx*16,11.5,b-cz*16],'metal'))
 return result

collisions=[];models=[]
for i,cell in enumerate(cells):
 mesh=[c for b in boxes if (c:=clip(b,cell))]
 collisions.append(mesh+horn_collision(cell))
 elements=[element(b,cell) for b in mesh]
 elements += [element(c,cell) for b in decor if (c:=clip(b,cell))]
 if i==4:elements.append(copy.deepcopy(horn))
 models.append(elements)
 j(ASSETS/f'models/block/ancient_anvil_cell_{i}.json',{'parent':'minecraft:block/block','render_type':'minecraft:cutout','ambientocclusion':True,'textures':textures,'elements':elements})
j(ASSETS/'anvil_layout.json',{'version':2,'footprint':[3,3,2],'cells':[{'index':i,'x':x,'y':y,'z':z} for i,(x,y,z) in enumerate(cells)]})

# Ordinary block-item envelope: uniformly shrink the FULL broad anvil, never skew it.
preview=[]
for (cx,cy,cz),elements in zip(cells,models):
 def scaled(p):return [8+(p[0]+cx*16-8)/3,2+(p[1]+cy*16)/3,8+(p[2]+cz*16-8)/3]
 for source in elements:
  e=copy.deepcopy(source);e['from']=scaled(e['from']);e['to']=scaled(e['to'])
  if 'rotation' in e:e['rotation']['origin']=scaled(e['rotation']['origin'])
  preview.append(e)
j(ASSETS/'models/block/ancient_anvil_item.json',{'parent':'minecraft:block/block','render_type':'minecraft:cutout','textures':textures,'elements':preview,'display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.625,.625,.625]}}})
rots=[('north',0),('east',90),('south',180),('west',270)]
j(ASSETS/'blockstates/ancient_anvil.json',{'variants':{f'facing={d},tier={tier},large={str(large).lower()},monumental={str(wide).lower()}':{'model':'examplemod:block/'+('ancient_anvil_cell_0' if large and wide else 'legacy_ancient_anvil_cell_0' if large else 'ancient_anvil'),'y':rot} for tier in (0,1) for large in (False,True) for wide in (False,True) for d,rot in rots}})
j(ASSETS/'blockstates/ancient_anvil_part.json',{'variants':{f'facing={d},cell={i},monumental={str(wide).lower()}':{'model':f'examplemod:block/'+('legacy_' if not wide and i<6 else '')+f'ancient_anvil_cell_{i}','y':rot} for i in range(1,len(cells)) for wide in (False,True) for d,rot in rots}})

def shape(mesh,whole=False):return ('WorkshopShapes.wholeRotations' if whole else 'WorkshopShapes.rotations')+'(new double[][]{'+','.join('{'+','.join(str(n) for n in a+B)+'}' for a,B,t in mesh)+'})'
java=['package com.example.examplemod;','import net.minecraft.core.Direction;','import net.minecraft.world.phys.shapes.VoxelShape;',
 '/** Generated from the runic centerpiece source: eighteen physically owned cells. */','public final class AncientAnvilGeometry {',
 '    private static final java.util.List<java.util.Map<Direction,VoxelShape>> CELLS=java.util.List.of('+','.join('cell'+str(i)+'()' for i in range(len(cells)))+');',
 '    private static final java.util.Map<Direction,VoxelShape> OUTLINE='+shape([b for b in boxes if not b[2].startswith('panel_')],True)+';',
 '    public static VoxelShape shape(Direction facing){return LegacyAncientAnvilGeometry.shape(facing);}',
 '    public static VoxelShape cell(int cell,Direction facing){return CELLS.get(cell).get(facing);}',
 '    public static VoxelShape outlineBody(Direction facing){return OUTLINE.get(facing);}']
java += ['    private static java.util.Map<Direction,VoxelShape> cell'+str(i)+'(){return '+shape(mesh)+';}' for i,mesh in enumerate(collisions)]
vertices=[]
for i in range(8):
 x=horn['to' if i&1 else 'from'][0];y=horn['to' if i&2 else 'from'][1]+16;z=horn['to' if i&4 else 'from'][2]
 c=math.sqrt(.5);vertices.append((8+(x-8)*c+(z+5)*c,y,-5-(x-8)*c+(z+5)*c))
edges=[(vertices[i],vertices[i^(1<<axis)]) for i in range(8) for axis in range(3) if not i&(1<<axis)]
def rotated_point(p,turn):
 x,y,z=p
 for _ in range(turn):x,z=16-z,x
 return [v/16 for v in (x,y,z)]
java.append('    private static final java.util.List<double[][]> NOSE_EDGES=java.util.List.of('+','.join('new double[][]{'+','.join('{'+','.join(str(v) for p in edge for v in rotated_point(p,turn))+'}' for edge in edges)+'}' for turn in range(4))+');')
java+=['    public static double[][] noseEdges(Direction facing){return NOSE_EDGES.get(switch(facing){case EAST->1;case SOUTH->2;case WEST->3;default->0;});}','    private AncientAnvilGeometry(){}','}']
(ROOT/'src/main/java/com/example/examplemod/AncientAnvilGeometry.java').write_text('\n'.join(java)+'\n')
print('Generated runic centerpiece: eighteen owned cells, physical collision, true-edge outline, luminous seal and legacy variants.')
