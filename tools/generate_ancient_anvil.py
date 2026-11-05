"""Generate the runic anvil artwork/physics, keeping ordinary recipes unchanged."""
from runic_anvil_geometry import ROOT,ASSETS,j
RES=ROOT/'src/main/resources';DATA=RES/'data/examplemod'
import json
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
# Nugget-like reversible conversion. Exactly nine small ancient pieces equal one vanilla scrap.
j(DATA/'recipe/netherite_scrap_from_ancient_scrap.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['SSS','SSS','SSS'],'key':{'S':'examplemod:ancient_scrap'},'result':{'id':'minecraft:netherite_scrap','count':1}})
j(DATA/'recipe/ancient_scrap_from_netherite_scrap.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':['minecraft:netherite_scrap'],'result':{'id':'examplemod:ancient_scrap','count':9}})
