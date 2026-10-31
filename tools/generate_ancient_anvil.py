"""Author the starter Ancient Anvil's explicit-UV block model, collision and ordinary recipes."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';ASSETS=RES/'assets/examplemod';DATA=RES/'data/examplemod'
def j(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2)+'\n')
# A full work face and long horn span 3 x 1 x 2 REAL owned cells. Units are vanilla pixels.
boxes=[
 ([2,0,-2],[14,4,22],'foot'),([3,4,0],[13,6,20],'trim'),
 ([4,6,3],[12,9,17],'metal'),([5,9,5],[11,16,15],'metal'),
 ([3,16,0],[13,19,27],'metal'),([2,19,-3],[14,24,30],'metal'),
 ([2,23,-3],[14,24,30],'face'),
 ([4,18,-8],[12,22,-3],'metal'),([5,18,-12],[11,21,-8],'metal'),([6,18,-16],[10,20,-12],'metal'),
 # One recessed-looking maker's seal on the front of the waist, rather than rune wallpaper.
 ([6,10,4],[10,11,5],'gold'),([6,14,4],[10,15,5],'gold'),([6,11,4],[7,14,5],'gold'),([9,11,4],[10,14,5],'gold')
]
compact=[([2,0,2],[14,3,14],'foot'),([5,3,5],[11,10,12],'metal'),([3,10,4],[13,15,16],'metal'),([3,14,4],[13,15,16],'face'),([5,11,0],[11,14,4],'metal')]
tex={'metal':'minecraft:block/netherite_block','face':'minecraft:block/anvil_top','foot':'minecraft:block/polished_blackstone','trim':'minecraft:block/cut_copper','gold':'minecraft:block/gold_block','particle':'minecraft:block/netherite_block'}
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
