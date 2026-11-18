"""Data-first smithing recipe and native item/block wiring."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources'
def write(path,data):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
a='assets/examplemod/';d='data/examplemod/'
write(a+'models/item/iron_forge_hammer.json',{'parent':'minecraft:item/handheld','textures':{'layer0':'examplemod:item/iron_forge_hammer'}})
for item,model in [('iron_forge_hammer','item/iron_forge_hammer'),('runestone','block/runestone')]:write(a+'items/'+item+'.json',{'model':{'type':'minecraft:model','model':'examplemod:'+model}})
write(d+'recipe/iron_forge_hammer.json',{'type':'minecraft:crafting_shaped','category':'equipment','pattern':['III',' S ',' S '],'key':{'I':'minecraft:iron_ingot','S':'minecraft:stick'},'result':{'id':'examplemod:iron_forge_hammer','count':1}})
write(d+'anvil_recipes/runestone.json',{'anvil_tier':0,'hammer_tier':1,'work':4,'ingredients':{**{s:{'tag':'examplemod:runestone_deepslate'} for s in ['north_west','north_east','south_west','south_east']},'center':{'item':'minecraft:gold_ingot'}},'result':{'id':'examplemod:runestone','count':1}})
write(d+'tags/item/runestone_deepslate.json',{'replace':False,'values':['minecraft:deepslate','minecraft:cobbled_deepslate']})
write(a+'blockstates/runestone.json',{'variants':{'':{'model':'examplemod:block/runestone'}}})
# Familiar vanilla masonry with a crisp inlaid rune, rather than a repainted stone tile.
elements=[{'from':[0,0,0],'to':[16,16,16],'faces':{f:{'texture':'#stone','cullface':f,'uv':[0,0,16,16]} for f in ['up','down','north','south','west','east']}}]
for f,lo,hi in [('north',[0,0,-.005],[16,16,-.005]),('south',[0,0,16.005],[16,16,16.005]),('west',[-.005,0,0],[-.005,16,16]),('east',[16.005,0,0],[16.005,16,16]),('up',[0,16.005,0],[16,16.005,16])]:
 elements.append({'from':lo,'to':hi,'faces':{f:{'texture':'#rune','uv':[0,0,16,16]}},'light_emission':3})
write(a+'models/block/runestone.json',{'parent':'minecraft:block/block','render_type':'minecraft:cutout','textures':{'particle':'minecraft:block/chiseled_deepslate','stone':'minecraft:block/chiseled_deepslate','rune':'examplemod:block/runestone_inlay'},'elements':elements})
write(d+'loot_table/blocks/runestone.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'examplemod:runestone'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
for tag in ['mineable/pickaxe','needs_stone_tool']:
 p=ROOT/'data/minecraft/tags/block'/f'{tag}.json';data=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
 if 'examplemod:runestone' not in data['values']:data['values'].append('examplemod:runestone')
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
p=ROOT/a/'lang/en_us.json';data=json.loads(p.read_text());data.update({'item.examplemod.iron_forge_hammer':'Iron Forge Hammer','block.examplemod.runestone':'Runestone','anvil.examplemod.tier0':'Tier 0 - Ready for basic smithing','anvil.examplemod.tier1':'Tier 1 - Metal foundation complete','anvil.examplemod.foundation':'4 x 4 metal foundation: %s / 16','anvil.examplemod.hint':'Place 16 metal blocks one layer below.'});p.write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n')
