"""Ancient Crystal: authored native 16px facets, packed exactly 2x for item UI."""
from pathlib import Path
import json
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'tools/asset_sources/crystal'
A=ROOT/'src/main/resources/assets/examplemod';D=ROOT/'src/main/resources/data'
def read(name):
    palette=json.loads((SOURCE/'palette.json').read_text());rows=(SOURCE/(name+'.pixels')).read_text().splitlines()
    assert len(rows)==16 and all(len(row)==16 for row in rows),name
    im=Image.new('RGBA',(16,16));im.putdata([tuple(bytes.fromhex(palette[c])) for row in rows for c in row]);return im

def item_exports():return {'ancient_crystal':read('ancient-crystal').resize((32,32),Image.Resampling.NEAREST)}
def write(path,data):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,indent=2)+'\n')
def main():
    for name in ('crystal-side','crystal-top'):read(name).save(A/'textures/block'/('ancient_'+name.replace('-','_')+'.png'))
    write(A/'models/item/ancient_crystal.json',{'parent':'minecraft:item/generated','textures':{'layer0':'examplemod:item/ancient_crystal'}})
    write(A/'items/ancient_crystal.json',{'model':{'type':'minecraft:model','model':'examplemod:item/ancient_crystal'}})
    write(A/'models/block/ancient_crystal_block.json',{'parent':'minecraft:block/cube_column','textures':{'side':'examplemod:block/ancient_crystal_side','end':'examplemod:block/ancient_crystal_top'}})
    write(A/'blockstates/ancient_crystal_block.json',{'variants':{'':{'model':'examplemod:block/ancient_crystal_block'}}})
    write(A/'items/ancient_crystal_block.json',{'model':{'type':'minecraft:model','model':'examplemod:block/ancient_crystal_block'}})
    write(D/'examplemod/loot_table/blocks/ancient_crystal_block.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'examplemod:ancient_crystal_block'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    for name in ('mineable/pickaxe','needs_iron_tool'):
        p=D/'minecraft/tags/block'/f'{name}.json';data=json.loads(p.read_text());data['values']=sorted(set(data['values'])|{'examplemod:ancient_crystal_block'});write(p,data)
    p=A/'lang/en_us.json';data=json.loads(p.read_text());data.update({'item.examplemod.ancient_crystal':'Ancient Crystal','block.examplemod.ancient_crystal_block':'Block of Ancient Crystal'});write(p,data)
    print('Generated native Ancient Crystal block assets, model/item wiring, drops and mining tags.')
if __name__=='__main__':main()
