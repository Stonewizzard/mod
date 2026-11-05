"""Author the starter Ancient Anvil's explicit-UV block model, collision and ordinary recipes."""
from pathlib import Path
import json,math,copy
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';ASSETS=RES/'assets/examplemod';DATA=RES/'data/examplemod'
def j(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2)+'\n')
# A solid forged head with a descending horn spans 3 x 1 x 2 owned cells.
# Keep the existing pedestal. Head silhouettes/collision share the same boxes.
boxes=[
 ([1,0,-3],[15,3,23],'foot'),([2,3,-2],[14,4,22],'metal'),
 ([3,4,0],[13,5,20],'trim'),([3,5,1],[13,7,20],'metal'),
 ([4,7,4],[12,17,18],'metal'),([3,15,3],[13,18,22],'metal'),
 ([3,18,1],[13,21,24],'metal'),([2,21,0],[14,23,27],'metal'),
 # One thick head, without the separate terraced cap/teeth on the old horn.
 ([1,23,-1],[15,25,28],'metal'),
 ([1,25,-1],[15,26,21],'metal'),([1,25,24],[15,26,28],'metal'),
 ([1,25,21],[9,26,24],'metal'),([12,25,21],[15,26,24],'metal'),
 # The flat striking face is set into a dark chamfered rim. The heel has one
 # recessed square hardie hole, kept clear through both cap and cheek layers.
 ([2,26,0],[14,28,21],'face'),([2,26,24],[14,28,27],'face'),
 ([2,26,21],[9,28,24],'face'),([12,26,21],[14,28,24],'face'),
 ([9,25,21],[12,25.5,24],'foot'),
]
# Recessed maker's seals on both cheeks of the waist. A bronze stepped ring and
# split chevron are geometry, never repeated rune wallpaper stretched onto a face.
for x,X in ((3.5,4),(12,12.5)):
 boxes.append(([x,9,8],[X,17,16],'foot'))
 lo,hi=(x-.25,x) if x<8 else (X,X+.25)
 for y,z,Y,Z in ((10,10,11,14),(15,10,16,14),(11,9,15,10),(11,14,15,15),
                  (11,11,12,13),(12,11,14,12),(14,12,15,13)):
  boxes.append(([lo,y,z],[hi,Y,Z],'trim'))
compact=[([2,0,2],[14,3,14],'foot'),([5,3,5],[11,10,12],'metal'),([3,10,4],[13,14,16],'metal'),([3,14,4],[13,15,16],'face'),([5,11,0],[11,14,4],'metal')]
tex={'metal':'examplemod:block/anvil_iron','face':'examplemod:block/anvil_workface_edge','workface':'examplemod:block/anvil_workface','foot':'examplemod:block/forge_soot','trim':'examplemod:block/forge_bronze','particle':'examplemod:block/anvil_iron'}
cells=[(0,0),(0,-1),(0,1),(1,-1),(1,0),(1,1)]
def element(box,cell_z=0):
 a,B,t=box;x,y,z=a;X,Y,Z=B
 uv={'north':[16-X,16-Y,16-x,16-y],'south':[x,16-Y,X,16-y],'west':[z,16-Y,Z,16-y],'east':[16-Z,16-Y,16-z,16-y],'up':[x,z,X,Z],'down':[x,16-Z,X,16-z]}
 faces={f:{'texture':'#'+t,'uv':v} for f,v in uv.items()}
 if t=='face':
  # A continuous 16x48 top: U stays 1:1; V spans 48 native pixels in 16 UV units.
  # Keep the global Z coordinate across owned-cell splits, including the horn.
  faces['up']={'texture':'#workface','uv':[x,(z+cell_z*16+16)/3,X,(Z+cell_z*16+16)/3]}
 return {'from':a,'to':B,'faces':faces}
def clip(box,y,z):
 a,B,t=box;lo=[max(a[0],0),max(a[1],y*16),max(a[2],z*16)];hi=[min(B[0],16),min(B[1],y*16+16),min(B[2],z*16+16)]
 if any(l>=h for l,h in zip(lo,hi)):return None
 return ([lo[0],lo[1]-y*16,lo[2]-z*16],[hi[0],hi[1]-y*16,hi[2]-z*16],t)
# One native 45-degree element makes a true pointed nose without stair-step teeth.
# Its rear half embeds in the head. Its forward half occupies the owned north
# upper cell. Input coordinates follow Minecraft's supported -16..32 model range.
horn=element(([3,7,-5],[13,9.75,5],'face'))
for direction,face in horn['faces'].items():face['uv']=[3,3,13,13] if direction in ('up','down') else [3,7,13,9.75]
horn['faces']['up']={'texture':'#face','uv':[3,3,13,13]}
horn['rotation']={'origin':[8,8.375,0],'axis':'y','angle':45,'rescale':False}
radius=math.sqrt(50)
def horn_collision(y,z):
 # Conservative native-pixel strips follow the same rotated square. The largest
 # departure from the diagonal is one model pixel, like vanilla non-cubic shapes.
 # Keep every strip clipped to its own cell; no collision through an empty cell.
 if y!=1:return []
 lo=max(-radius,z*16);hi=min(radius,z*16+16)
 if lo>=hi:return []
 cuts=sorted(set([lo,hi]+[float(n) for n in range(math.ceil(lo),math.floor(hi)+1)]));result=[]
 for a,b in zip(cuts,cuts[1:]):
  nearest=0 if a<=0<=b else min(abs(a),abs(b));half=radius-nearest
  result.append(([8-half,7,a-z*16],[8+half,9.75,b-z*16],'face'))
 return result
meshes=[];collision_meshes=[];model_elements=[]
for i,(y,z) in enumerate(cells):
 mesh=[c for b in boxes if (c:=clip(b,y,z))];meshes.append(mesh)
 collision_meshes.append(mesh+horn_collision(y,z))
 elements=[element(b,z) for b in mesh]
 if i==4:elements.append(copy.deepcopy(horn))
 model_elements.append(elements)
 j(ASSETS/f'models/block/ancient_anvil_cell_{i}.json',{'parent':'minecraft:block/block','textures':tex,'elements':elements})
j(ASSETS/'models/block/ancient_anvil.json',{'parent':'minecraft:block/block','textures':tex,'elements':[element(b) for b in compact]})
# A dedicated item preview centers/scales the complete silhouette; held blocks must fit an ordinary item envelope.
preview=[]
for (y,z),elements in zip(cells,model_elements):
 for source in elements:
  # Keep the WORLD face UVs. Shrinking geometry must not stretch an entire tile
  # across every tiny bronze mark or change the item's material pattern.
  e=copy.deepcopy(source)
  def scaled(corner):return [8+(corner[0]-8)/3,4+(corner[1]+y*16)/3,(corner[2]+z*16)/3+16/3]
  for key in ('from','to'):
   corner=e[key]
   e[key]=[8+(corner[0]-8)/3,4+(corner[1]+y*16)/3,(corner[2]+z*16)/3+16/3]
  if 'rotation' in e:e['rotation']['origin']=scaled(e['rotation']['origin'])
  preview.append(e)
j(ASSETS/'models/block/ancient_anvil_item.json',{'parent':'minecraft:block/block','textures':tex,'elements':preview,'display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.625,.625,.625]}}})
rots=[('north',0),('east',90),('south',180),('west',270)]
j(ASSETS/'blockstates/ancient_anvil.json',{'variants':{f'facing={d},tier={tier},large={str(large).lower()}':{'model':'examplemod:block/'+('ancient_anvil_cell_0' if large else 'ancient_anvil'),'y':rot} for tier in (0,1) for large in (False,True) for d,rot in rots}})
j(ASSETS/'blockstates/ancient_anvil_part.json',{'variants':{f'facing={d},cell={i}':{'model':f'examplemod:block/ancient_anvil_cell_{i}','y':rot} for i in range(1,6) for d,rot in rots}})
j(ASSETS/'models/block/ancient_metal_block.json',{'parent':'minecraft:block/cube_bottom_top','textures':{'side':'minecraft:block/netherite_block','bottom':'minecraft:block/polished_blackstone','top':'minecraft:block/cut_copper'}})
j(ASSETS/'blockstates/ancient_metal_block.json',{'variants':{'':{'model':'examplemod:block/ancient_metal_block'}}})
for name in ('ancient_anvil','ancient_metal_block'):
 j(ASSETS/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'examplemod:block/'+('ancient_anvil_item' if name=='ancient_anvil' else name)}})
 j(DATA/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'examplemod:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
j(ASSETS/'items/ancient_scrap.json',{'model':{'type':'minecraft:model','model':'examplemod:item/ancient_scrap'}})
j(ASSETS/'models/item/ancient_scrap.json',{'parent':'minecraft:item/generated','textures':{'layer0':'examplemod:item/ancient_scrap'}})
j(ASSETS/'items/ancient_ingot.json',{'model':{'type':'minecraft:model','model':'examplemod:item/ancient_ingot'}})
j(ASSETS/'models/item/ancient_ingot.json',{'parent':'minecraft:item/generated','textures':{'layer0':'examplemod:item/ancient_ingot'}})
def recipe(name,pattern,key,result,count=1):j(DATA/f'recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':key,'result':{'id':'examplemod:'+result,'count':count}})
recipe('ancient_anvil',['BBB',' I ','III'],{'B':'examplemod:ancient_metal_block','I':'examplemod:ancient_ingot'},'ancient_anvil')
recipe('ancient_metal_block',['III','III','III'],{'I':'examplemod:ancient_ingot'},'ancient_metal_block')
j(DATA/'recipe/ancient_ingots_from_block.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':['examplemod:ancient_metal_block'],'result':{'id':'examplemod:ancient_ingot','count':9}})
metals=['minecraft:copper_block','minecraft:iron_block','minecraft:gold_block','minecraft:netherite_block','examplemod:ancient_metal_block',{'id':'c:storage_blocks/copper','required':False},{'id':'c:storage_blocks/iron','required':False},{'id':'c:storage_blocks/gold','required':False},{'id':'c:storage_blocks/netherite','required':False}]
# Common storage tags are nested tags, so pack-added copper oxidation/waxing and metals can opt in safely.
for i,v in enumerate(metals):
 if isinstance(v,dict):v['id']='#'+v['id']
j(DATA/'tags/block/ancient_anvil_foundation.json',{'replace':False,'values':metals})
j(DATA/'tags/item/ancient_alloy_scrap.json',{'replace':False,'values':['examplemod:ancient_scrap','minecraft:ancient_debris']})
p=RES/'data/minecraft/tags/block/mineable/pickaxe.json';d=json.loads(p.read_text());d['values']=list(dict.fromkeys(d['values']+['examplemod:ancient_anvil','examplemod:ancient_metal_block']));j(p,d)
java=['package com.example.examplemod;','import net.minecraft.core.Direction;','import net.minecraft.world.phys.shapes.VoxelShape;','/** Generated with six owned cell models by tools/generate_ancient_anvil.py. */','public final class AncientAnvilGeometry {']
def shape(mesh):return 'WorkshopShapes.rotations(new double[][]{'+','.join('{'+','.join(str(n) for n in a+B)+'}' for a,B,t in mesh)+'})'
java+=['    private static final java.util.Map<Direction,VoxelShape> COMPACT='+shape(compact)+';','    private static final java.util.List<java.util.Map<Direction,VoxelShape>> CELLS=java.util.List.of('+','.join(shape(mesh) for mesh in collision_meshes)+');','    public static VoxelShape shape(Direction facing){return COMPACT.get(facing);}','    public static VoxelShape cell(int cell,Direction facing){return CELLS.get(cell).get(facing);}','    private AncientAnvilGeometry(){}','}']
(ROOT/'src/main/java/com/example/examplemod/AncientAnvilGeometry.java').write_text('\n'.join(java)+'\n')
p=RES/'data/minecraft/tags/block/mineable/pickaxe.json';d=json.loads(p.read_text());d['values']=list(dict.fromkeys(d['values']+['examplemod:ancient_anvil_part']));j(p,d)
print('Authored Ancient Anvil model/collision, metal foundation tag, scrap input tag and five crafting recipes.')

# Nugget-like reversible conversion. Exactly nine small ancient pieces equal one vanilla scrap.
j(DATA/'recipe/netherite_scrap_from_ancient_scrap.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['SSS','SSS','SSS'],'key':{'S':'examplemod:ancient_scrap'},'result':{'id':'minecraft:netherite_scrap','count':1}})
j(DATA/'recipe/ancient_scrap_from_netherite_scrap.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':['minecraft:netherite_scrap'],'result':{'id':'examplemod:ancient_scrap','count':9}})
