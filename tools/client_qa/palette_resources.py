"""Disposable native-QA material carriers; never registers or ships gameplay blocks."""
from pathlib import Path
import json,shutil,struct
ROOT=Path(__file__).resolve().parents[2]
PALETTES=[('a','warm_charcoal','white_concrete'),('b','sootstone','orange_concrete'),('c','dark_umber','magenta_concrete'),('d','volcanic_stone','light_blue_concrete')]
def main():
    assets=ROOT/'src/main/resources/assets'
    for letter,name,carrier in PALETTES:
        source=ROOT/f'tools/asset_sources/runestone/palette_{name}_16.png'
        data=source.read_bytes();assert data[:8]==b'\x89PNG\r\n\x1a\n' and struct.unpack('>II',data[16:24])==(16,16)
        texture=assets/f'examplemod/textures/block/qa_runestone_palette_{letter}.png';texture.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(source,texture)
        model=assets/f'minecraft/models/block/{carrier}.json';model.parent.mkdir(parents=True,exist_ok=True)
        model.write_text(json.dumps({'parent':'minecraft:block/cube_all','textures':{'all':f'examplemod:block/qa_runestone_palette_{letter}'}},indent=2)+'\n')
    print('Prepared four QA-only cube materials A-D for a single native Minecraft comparison')
if __name__=='__main__':main()
