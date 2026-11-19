"""Export the selected blank Ancient Slate tile without repainting its pixels."""
from pathlib import Path
import argparse
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'tools/asset_sources/runestone/ancient_slate_16.png'
OUT=ROOT/'src/main/resources/assets/examplemod/textures/block/runestone.png'
def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args()
    im=Image.open(SOURCE).convert('RGBA')
    assert im.size==(16,16), 'Runestone uses the native 16px block grid'
    assert all(a==255 for *_,a in im.getdata()), 'Runestone is fully opaque'
    assert len(im.getcolors(256))<=8, 'Keep the approved limited stone palette'
    if args.check:
        assert OUT.exists() and Image.open(OUT).convert('RGBA').size==(16,16) and Image.open(OUT).convert('RGBA').tobytes()==im.tobytes(), 'Stale Runestone export'
    else:
        OUT.parent.mkdir(parents=True,exist_ok=True);im.save(OUT)
    print('Checked Ancient Slate Runestone: native 16px, opaque, approved source unchanged')
if __name__=='__main__':main()
