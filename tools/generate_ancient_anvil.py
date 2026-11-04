"""Author the starter Ancient Anvil's explicit-UV block model, collision and ordinary recipes."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';ASSETS=RES/'assets/examplemod';DATA=RES/'data/examplemod'
def j(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2)+'\n')
# Broad shoulders, an inset workface and a long horn span 3 x 1 x 2 owned cells.
# All bevels are whole native pixels; silhouettes/collision come from the same boxes.
boxes=[
 ([1,0,-3],[15,3,23],'foot'),([2,3,-2],[14,4,22],'metal'),
 ([3,4,0],[13,5,20],'trim'),([3,5,1],[13,7,20],'metal'),
 ([4,7,4],[12,17,18],'metal'),([3,15,3],[13,18,22],'metal'),
 ([2,18,0],[14,20,27],'metal'),([1,20,-4],[15,22,29],'metal'),
 # The head has a lower bevel, heavy cheeks and a distinct polished working plate.
 ([1,22,-3],[15,24,30],'metal'),
 ([1,24,-3],[15,25,22],'metal'),([1,24,25],[15,25,30],'metal'),
 ([1,24,22],[9,25,25],'metal'),([12,24,22],[15,25,25],'metal'),
 # Four plates surround a real recessed hardie/tool hole in the heel.
 ([2,25,-2],[14,27,22],'face'),([2,25,25],[14,27,29],'face'),
 ([2,25,22],[9,27,25],'face'),([12,25,22],[14,27,25],'face'),
 ([9,24,22],[12,24.5,25],'foot'),
 # Broad horn root tapers gradually; the workface joins it at the same height.
 ([3,21,-8],[13,26,-3],'metal'),([4,22,-12],[12,26,-8],'metal'),
 ([5,23,-15],[11,26,-12],'metal'),([6,24,-16],[10,26,-15],'metal'),
 ([4,26,-7],[12,27,-2],'face'),([5,26,-11],[11,27,-7],'face'),
 ([6,26,-14],[10,27,-11],'face')
]
# Recessed maker's seals on both cheeks of the waist. A bronze stepped ring and
# split chevron are geometry, never repeated rune wallpaper stretched onto a face.
for x,X in ((3.5,4),(12,12.5)):
 boxes.append(([x,9,8],[X,17,16],'foot'))
 lo,hi=(x-.25,x) if x<8 else (X,X+.25)
 for y,z,Y,Z in ((10,10,11,14),(15,10,16,14),(11,9,15,10),(11,14,15,15),
                  (11,11,12,13),(12,11,14,12),(14,12,15,13)):
  boxes.append(([lo,y,z],[hi,Y,Z],'trim'))
compact=[([2,0,2],[14,3,14],'foot'),([5,3,5],[11,10,12],'metal'),([3,10,4],[13,15,16],'metal'),([3,14,4],[13,15,16],'face'),([5,11,0],[11,14,4],'metal')]
tex={'metal':'examplemod:block/anvil_iron','face':'examplemod:block/anvil_workface','foot':'examplemod:block/forge_soot','trim':'examplemod:block/forge_bronze','particle':'examplemod:block/anvil_iron'}
cells=[(0,0),(0,-1),(0,1),(1,-1),(1,0),(1,1)]
def element(box):
 a,B,t=box;x,y,z=a;X,Y,Z=B
 uv={'north':[16-X,16-Y,16-x,16-y],'south':[x,16-Y,X,16-y],'west':[z,16-Y,Z,16-y],'east':[16-Z,16-Y,16-z,16-y],'up':[x,z,X,Z],'down':[x,16-Z,X,16-z]}
 return {'from':a,'to':B,'faces':{f:{'texture':'#'+t,'uv':v} for f,v in uv.items()}}
def clip(box,y,z):
 a,B,t=box;lo=[max(a[0],0),max(a[1],y*16),max(a[2],z*16)];hi=[min(B[0],16),min(B[1],y*16+16),min(B[2],z*16+16)]
 if any(l>=h for l,h in zip(lo,hi)):return None
 return ([lo[0],lo[1]-y*16,lo[2]-z*16],[hi[0],hi[1]-y*16,hi[2]-z*16],t)
meshes=[]
for i,(y,z) in enumerate(cells):
 mesh=[c for b in boxes if (c:=clip(b,y,z))];meshes.append(mesh)
 j(ASSETS/f'models/block/ancient_anvil_cell_{i}.json',{'parent':'minecraft:block/block','textures':tex,'elements':[element(b) for b in mesh]})
j(ASSETS/'models/block/ancient_anvil.json',{'parent':'minecraft:block/block','textures':tex,'elements':[element(b) for b in compact]})
# A dedicated item preview centers/scales the complete silhouette; held blocks must fit an ordinary item envelope.
preview=[]
for a,B,t in boxes:
 e=element(([8+(a[0]-8)/3,4+a[1]/3,a[2]/3+16/3],[8+(B[0]-8)/3,4+B[1]/3,B[2]/3+16/3],t))
 for f in e['faces'].values():f['uv']=[0,0,16,16]
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
java+=['    private static final java.util.Map<Direction,VoxelShape> COMPACT='+shape(compact)+';','    private static final java.util.List<java.util.Map<Direction,VoxelShape>> CELLS=java.util.List.of('+','.join(shape(mesh) for mesh in meshes)+');','    public static VoxelShape shape(Direction facing){return COMPACT.get(facing);}','    public static VoxelShape cell(int cell,Direction facing){return CELLS.get(cell).get(facing);}','    private AncientAnvilGeometry(){}','}']
(ROOT/'src/main/java/com/example/examplemod/AncientAnvilGeometry.java').write_text('\n'.join(java)+'\n')
p=RES/'data/minecraft/tags/block/mineable/pickaxe.json';d=json.loads(p.read_text());d['values']=list(dict.fromkeys(d['values']+['examplemod:ancient_anvil_part']));j(p,d)
print('Authored Ancient Anvil model/collision, metal foundation tag, scrap input tag and five crafting recipes.')

# Nugget-like reversible conversion. Exactly nine small ancient pieces equal one vanilla scrap.
j(DATA/'recipe/netherite_scrap_from_ancient_scrap.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['SSS','SSS','SSS'],'key':{'S':'examplemod:ancient_scrap'},'result':{'id':'minecraft:netherite_scrap','count':1}})
j(DATA/'recipe/ancient_scrap_from_netherite_scrap.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':['minecraft:netherite_scrap'],'result':{'id':'examplemod:ancient_scrap','count':9}})
