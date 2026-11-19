"""Native 16px Rune Keystone, sourced from explicit pixel rows and the approved anvil palette."""
from pathlib import Path
import json
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/examplemod'
D=ROOT/'src/main/resources/data/examplemod'
def write(path,value):
    path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(value,indent=2)+'\n')
palette=json.loads((ROOT/'tools/asset_sources/runic-anvil/palette.json').read_text())
rows=(ROOT/'tools/asset_sources/canopy/keystone.pixels').read_text().splitlines()
assert len(rows)==16 and all(len(r)==16 for r in rows)
im=Image.new('RGBA',(16,16));im.putdata([tuple(bytes.fromhex(palette[p]))+(255,) for row in rows for p in row])
(A/'textures/block').mkdir(parents=True,exist_ok=True);im.save(A/'textures/block/canopy_keystone.png')
write(A/'models/block/canopy_keystone.json',{'parent':'minecraft:block/cube_all','textures':{'all':'examplemod:block/canopy_keystone'}})
write(A/'blockstates/canopy_keystone.json',{'variants':{'':{'model':'examplemod:block/canopy_keystone'}}})
write(A/'items/canopy_keystone.json',{'model':{'type':'minecraft:model','model':'examplemod:block/canopy_keystone'}})
write(D/'tags/block/ancient_anvil_keystones.json',{'replace':False,'values':['examplemod:canopy_keystone']})
write(D/'loot_table/blocks/canopy_keystone.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'examplemod:canopy_keystone'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
for name in ('mineable/pickaxe','needs_iron_tool'):
    p=ROOT/'src/main/resources/data/minecraft/tags/block'/f'{name}.json'
    data=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
    if 'examplemod:canopy_keystone' not in data['values']:data['values'].append('examplemod:canopy_keystone')
    write(p,data)
ingredients={s:{'item':'examplemod:runestone'} for s in ('north_west','north_east','south_west','south_east')}
ingredients.update({s:{'item':'examplemod:ancient_ingot'} for s in ('north_inner','east_inner','south_inner','west_inner')})
ingredients['center']={'item':'minecraft:glowstone_dust'}
write(D/'anvil_recipes/canopy_keystone.json',{'anvil_tier':4,'hammer_tier':1,'work':8,'ingredients':ingredients,'result':{'id':'examplemod:canopy_keystone','count':4}})
p=A/'lang/en_us.json';data=json.loads(p.read_text());data['block.examplemod.canopy_keystone']='Canopy Keystone';write(p,data)
print('Generated Canopy Keystone: native 16px block, model/item wiring, recipe, tags and loot')
