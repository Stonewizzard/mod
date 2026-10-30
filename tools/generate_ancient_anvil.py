"""Author the starter Ancient Anvil's explicit-UV block model, collision and ordinary recipes."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';ASSETS=RES/'assets/examplemod';DATA=RES/'data/examplemod'
def j(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2)+'\n')
# A broad working face, tapered horn and small foot. Whole-pixel geometry only.
# Keep the starter anvil in its owned cell; larger future tiers can own more cells.
boxes=[
 ([3,0,3],[13,2,14],'metal'),
 ([4,2,5],[12,3,13],'metal'),
 ([5,3,7],[11,7,12],'runes'),
 ([4,7,6],[12,9,14],'metal'),
 ([3,9,6],[13,12,16],'metal'),
 ([3,12,6],[13,13,16],'top'),
 ([4,9,4],[12,12,6],'metal'),
 ([5,10,2],[11,12,4],'metal'),
 ([6,10,0],[10,11,2],'metal')
]
tex={k:'examplemod:block/anvil_'+k for k in ('metal','gold','runes','top')};tex['particle']=tex['metal']
def element(box):
 a,B,t=box;x,y,z=a;X,Y,Z=B
 uv={'north':[0,0,16,16],'south':[0,0,16,16],'west':[0,0,16,16],'east':[0,0,16,16],'up':[x,z,X,Z],'down':[x,16-Z,X,16-z]}
 uv.update(north=[16-X,16-Y,16-x,16-y],south=[x,16-Y,X,16-y],west=[z,16-Y,Z,16-y],east=[16-Z,16-Y,16-z,16-y])
 return {'from':a,'to':B,'faces':{f:{'texture':'#'+t,'uv':v} for f,v in uv.items()}}
j(ASSETS/'models/block/ancient_anvil.json',{'parent':'minecraft:block/block','textures':tex,'elements':[element(b) for b in boxes],'display':{'gui':{'rotation':[30,225,0],'translation':[0,1,0],'scale':[0.7,0.7,0.7]}}})
j(ASSETS/'blockstates/ancient_anvil.json',{'variants':{f'facing={d},tier={t}':{'model':'examplemod:block/ancient_anvil','y':rot} for t in (0,1) for d,rot in [('north',0),('east',90),('south',180),('west',270)]}})
j(ASSETS/'models/block/ancient_metal_block.json',{'parent':'minecraft:block/cube_bottom_top','textures':{'side':'examplemod:block/anvil_runes','bottom':'examplemod:block/anvil_metal','top':'examplemod:block/anvil_gold'}})
j(ASSETS/'blockstates/ancient_metal_block.json',{'variants':{'':{'model':'examplemod:block/ancient_metal_block'}}})
for name in ('ancient_anvil','ancient_metal_block'):
 j(ASSETS/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'examplemod:block/'+name}})
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
java=['package com.example.examplemod;','import net.minecraft.core.Direction;','import net.minecraft.world.phys.shapes.VoxelShape;','/** Generated with the block model by tools/generate_ancient_anvil.py. */','public final class AncientAnvilGeometry {','    private static final java.util.Map<Direction,VoxelShape> SHAPES=WorkshopShapes.rotations(new double[][]{']
java += ['        {'+','.join(str(n) for n in a+B)+'}'+(',' if i<len(boxes)-1 else '') for i,(a,B,t) in enumerate(boxes)]
java += ['    });','    public static VoxelShape shape(Direction facing){return SHAPES.get(facing);}','    private AncientAnvilGeometry(){}','}']
(ROOT/'src/main/java/com/example/examplemod/AncientAnvilGeometry.java').write_text('\n'.join(java)+'\n')
print('Authored Ancient Anvil model/collision, metal foundation tag, scrap input tag and five crafting recipes.')

# Nugget-like reversible conversion. Exactly nine small ancient pieces equal one vanilla scrap.
j(DATA/'recipe/netherite_scrap_from_ancient_scrap.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['SSS','SSS','SSS'],'key':{'S':'examplemod:ancient_scrap'},'result':{'id':'minecraft:netherite_scrap','count':1}})
j(DATA/'recipe/ancient_scrap_from_netherite_scrap.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':['minecraft:netherite_scrap'],'result':{'id':'examplemod:ancient_scrap','count':9}})
