"""One geometric source for explicit-UV formed block models and matching server collision shapes."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';OUT=RES/'assets/examplemod'
def j(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2)+'\n')
parts=[(x,-1,z) for x in (-1,0,1) for z in range(3)]+[(x,y,1) for x in (-1,1) for y in (0,1)]+[(x,0,2) for x in (-1,0,1)]+[(0,y,2) for y in (1,2)]
textures={'stone':'minecraft:block/deepslate_bricks','soot':'minecraft:block/polished_blackstone','bronze':'minecraft:block/cut_copper','ash':'minecraft:block/basalt_top'};textures['particle']=textures['stone']
def b(a,B,t):return [a,B,t]
geometry=[]
for x,y,z in parts:
 if y==-1:boxes=[b([0,0,0],[16,11,16],'stone'),b([0,11,0],[16,12,16],'bronze'),b([0,12,0],[16,16,16],'soot')]
 elif z==1:
  lo,hi=(2,16) if x<0 else (0,14)
  if y==0:boxes=[b([lo,0,2],[hi,11,16],'stone'),b([lo,11,2],[hi,12,16],'bronze'),b([max(0,lo-1),12,1],[min(16,hi+1),16,16],'stone')]
  else:boxes=[b([lo+2,0,4],[hi-2,10,16],'stone'),b([lo+1,10,3],[hi-1,12,16],'bronze'),b([lo+2,12,4],[hi-2,14,15],'stone')]
 elif y==0 and x!=0:boxes=[b([0,0,0],[16,6,14],'soot'),b([0,6,0],[16,16,14],'stone')]
 elif y==0:boxes=[b([0,0,0],[16,4,14],'stone'),b([3,4,0],[13,5,12],'ash'),b([0,4,0],[3,16,14],'soot'),b([13,4,0],[16,16,14],'soot'),b([3,5,12],[13,16,14],'soot')]
 elif y==1:boxes=[b([0,0,0],[16,1,14],'bronze'),b([0,1,0],[16,4,14],'stone'),b([2,4,2],[14,10,14],'stone'),b([4,10,4],[12,16,14],'soot')]
 else:boxes=[b([4,0,4],[6,12,14],'soot'),b([10,0,4],[12,12,14],'soot'),b([6,0,4],[10,12,6],'soot'),b([6,0,12],[10,12,14],'soot'),b([3,12,3],[6,16,15],'stone'),b([10,12,3],[13,14,15],'stone'),b([6,12,3],[10,14,6],'stone'),b([6,12,12],[10,15,15],'stone')]
 geometry.append(boxes)
# The firebox replaces the old front work table. All other formed cells are unchanged.
geometry.append(geometry[14])
geometry.append(geometry[14])
def element(box):
 (x,y,z),(X,Y,Z),t=box
 uv={'north':[16-X,16-Y,16-x,16-y],'south':[x,16-Y,X,16-y],'west':[z,16-Y,Z,16-y],'east':[16-Z,16-Y,16-z,16-y],'up':[x,z,X,Z],'down':[x,16-Z,X,16-z]}
 return {'from':[x,y,z],'to':[X,Y,Z],'faces':{f:{'texture':'#'+t,'uv':v} for f,v in uv.items()}}
for i,boxes in enumerate(geometry):
 name='formed_forge_'+str(i) if i<18 else 'ruined_ancient_forge_formed' if i==18 else 'ruined_ancient_forge'
 j(OUT/f'models/block/{name}.json',{'parent':'minecraft:block/block','ambientocclusion':True,'textures':textures,'elements':[element(box) for box in boxes]})
j(OUT/'models/block/ruined_forge_bricks.json',{'parent':'minecraft:block/cube_all','textures':{'all':textures['stone']}})
rots=[('north',0),('east',90),('south',180),('west',270)]
j(OUT/'blockstates/formed_ruined_forge.json',{'variants':{f'facing={d},part={i}':{'model':f'examplemod:block/formed_forge_{i}','y':rot} for i in range(18) for d,rot in rots}})
j(OUT/'blockstates/ruined_ancient_forge.json',{'variants':{f'facing={d},formed={str(formed).lower()}':{'model':'examplemod:block/ruined_ancient_forge'+('_formed' if formed else ''),'y':rot} for formed in (False,True) for d,rot in rots}})
p=RES/'data/minecraft/tags/block/mineable/pickaxe.json';data=json.loads(p.read_text());v='examplemod:formed_ruined_forge'
if v not in data['values']:data['values'].append(v)
j(p,data)
java=['package com.example.examplemod;','import net.minecraft.core.Direction;','import net.minecraft.world.phys.shapes.VoxelShape;','/** Generated from tools/generate_formed_forge.py; cell shapes and UV models share one definition. */','public final class ForgeGeometry {','    private static final java.util.List<java.util.Map<Direction,VoxelShape>> SHAPES=java.util.List.of(']
for i,boxes in enumerate(geometry):
 coords=','.join('{'+','.join(str(v) for v in a+B)+'}' for a,B,t in boxes)
 java.append('        WorkshopShapes.rotations(new double[][]{'+coords+'})'+(',' if i<len(geometry)-1 else ''))
java+=['    );','    public static VoxelShape shape(int cell,Direction facing){return SHAPES.get(cell).get(facing);}','    private ForgeGeometry(){}','}']
p=ROOT/'src/main/java/com/example/examplemod/ForgeGeometry.java';p.write_text('\n'.join(java)+'\n')
print('Generated twenty explicit-UV model cells and matching rotated collision shapes.')
