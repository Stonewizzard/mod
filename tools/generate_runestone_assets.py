"""Export the selected blank Sootstone material for Runestone without repainting its pixels."""
from pathlib import Path
import argparse,shutil
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'tools/asset_sources/runestone/sootstone_16.png'
OUT=ROOT/'src/main/resources/assets/examplemod/textures/block/runestone.png'
def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args()
    im=Image.open(SOURCE).convert('RGBA')
    assert im.size==(16,16), 'Runestone uses the native 16px block grid'
    pixels=im.get_flattened_data() if hasattr(im,'get_flattened_data') else im.getdata()
    assert all(a==255 for *_,a in pixels), 'Runestone is fully opaque'
    assert len(im.getcolors(256))<=8, 'Keep the approved limited stone palette'
    if args.check:
        assert OUT.exists() and Image.open(OUT).convert('RGBA').size==(16,16) and Image.open(OUT).convert('RGBA').tobytes()==im.tobytes(), 'Stale Runestone export'
    else:
        OUT.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(SOURCE,OUT)
    print('Checked Sootstone Runestone: native 16px, opaque, selected source unchanged')
if __name__=='__main__':main()
