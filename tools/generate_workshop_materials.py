"""Warm workshop materials with a native 16-texel-per-block art grid.

The forge uses palette-only native tiles. The anvil top has its own indexed source.
Source layouts and explicit palettes are versioned; this is not a noise generator.
Run with --check in CI to catch stale material exports.
"""
from pathlib import Path
import argparse
import io
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'tools/asset_sources/workshop-materials'
OUTPUT = ROOT / 'src/main/resources/assets/examplemod/textures/block'
# Darkest to lightest, one colour per original colour. Never change the pixel layout.
MATERIALS = {
    'forge_stone': ('deepslate_bricks', ['211d1a','302820','41362a','524435','635340','76644d','89775e']),
    'forge_soot': ('polished_blackstone', ['171512','211c18','2b241e','352e26','41372d','4d4235']),
    'forge_ash': ('basalt_top', ['2a2824','39352e','4c4539','615746','756a56','8b7e65','a3967e']),
    'forge_bronze': ('cut_copper', ['493524','60462b','795a34','94703e','ad874c','c29c60']),
    'anvil_iron': ('anvil', ['342e28','3d362e','463e34','50473c','5a5144','655c4e','716855']),
    'anvil_workface_edge': ('anvil', ['41423e','4d4f49','5a5c55','676b62','767b70','878c80','9a9f91']),
}
WORKFACE_PALETTE = ['41423e','4d4f49','5a5c55','676b62','767b70','878c80','9a9f91']

def exports():
    for name, (source, palette) in MATERIALS.items():
        tile = Image.open(SOURCE / (source + '.png')).convert('RGB')
        assert tile.size == (16, 16), source
        pixels = [tile.getpixel((x,y)) for y in range(16) for x in range(16)]
        colours = sorted(set(pixels), key=lambda c: (sum(c), c))
        assert len(colours) == len(palette), (source, len(colours))
        mapping = dict(zip(colours, [tuple(bytes.fromhex(c)) for c in palette]))
        result = Image.new('RGB', (16, 16))
        result.putdata([mapping[c] for c in pixels])
        yield name, result
    # Three blocks long, still 16 texels per block. This is a continuous top skin,
    # not a 16px patch stretched over the whole head or repeated at each cell seam.
    rows = [r for r in (SOURCE/'anvil-workface.pixels').read_text().splitlines() if r and not r.startswith('#')]
    assert len(rows) == 48 and all(len(r) == 16 for r in rows), 'workface must be 16x48 native pixels'
    colours = [tuple(bytes.fromhex(c)) for c in WORKFACE_PALETTE]
    assert all(c in '0123456' for row in rows for c in row), 'unknown workface colour'
    result = Image.new('RGB', (16,48))
    result.putdata([colours[int(c)] for row in rows for c in row])
    yield 'anvil_workface', result

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    OUTPUT.mkdir(parents=True, exist_ok=True)
    stale = []
    for name, im in exports():
        path = OUTPUT / (name + '.png')
        buffer = io.BytesIO(); im.save(buffer, format='PNG')
        if args.check:
            if not path.exists() or path.read_bytes() != buffer.getvalue(): stale.append(name)
        else: path.write_bytes(buffer.getvalue())
    if stale: raise SystemExit('Stale workshop materials: ' + ', '.join(stale))
    print('PASS: six 16px palette-swap materials and one continuous 16x48 anvil workface; 16 texels per block.')

if __name__ == '__main__': main()
