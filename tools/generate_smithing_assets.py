"""Small authored native-grid assets. No filtering, rescaling or noise textures."""
from pathlib import Path
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'tools/asset_sources/smithing'
PALETTE={'.':(0,0,0,0),'#':(38,40,43,255),'d':(68,73,76,255),'m':(115,122,123,255),'s':(173,181,177,255),'h':(223,229,219,255),'w':(85,55,33,255),'b':(134,89,46,255),'t':(183,132,65,255),'g':(187,118,30,255),'y':(240,189,66,255),'l':(255,226,126,255)}
def read(name):
 rows=[x.strip() for x in (SOURCE/(name+'.pixels')).read_text().splitlines() if x.strip() and not x.startswith(';')]
 assert len(rows)==16 and all(len(r)==16 for r in rows),name
 im=Image.new('RGBA',(16,16));im.putdata([PALETTE[c] for r in rows for c in r]);return im

def item_exports():return {'iron_forge_hammer':read('iron-forge-hammer').resize((32,32),Image.Resampling.NEAREST)}
def block_exports():yield 'runestone_inlay',read('runestone-inlay')
