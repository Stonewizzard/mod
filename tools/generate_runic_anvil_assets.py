"""Compile the indexed, native-grid art for the runic smithing centerpiece."""
from pathlib import Path
import json
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'tools/asset_sources/runic-anvil'

def exports():
    palette=json.loads((SOURCE/'palette.json').read_text())
    colours={k:tuple(bytes.fromhex(v)) for k,v in palette.items()}
    for source,name,size,glow in [
        ('metal','runic_anvil_metal',(16,16),False),
        ('edge','runic_anvil_edge',(16,16),False),
        ('frame','runic_anvil_frame',(16,16),False),
        ('top','runic_anvil_top',(48,48),True),
        ('panel','runic_anvil_panel',(16,16),True),
        ('horn','runic_anvil_horn',(16,16),False),
        ('ember','runic_anvil_ember',(16,16),False),
    ]:
        rows=[r for r in (SOURCE/(source+'.pixels')).read_text().splitlines() if r and not r.startswith('#')]
        assert len(rows)==size[1] and all(len(row)==size[0] for row in rows),(source,size)
        assert all(c in colours for row in rows for c in row),source
        image=Image.new('RGB',size);image.putdata([colours[c] for row in rows for c in row])
        if source=='ember':
            # Cooled crust and moving hot cores, using the same sharp native
            # texels as the inlay rather than a flat gold/neon stripe.
            image=Image.new('RGB',(size[0],size[1]*5))
            for frame in range(5):
                tile=Image.new('RGB',size)
                tile.putdata([colours[rows[y][(x+frame)%size[0]]] for y in range(size[1]) for x in range(size[0])])
                image.paste(tile,(0,frame*size[1]))
        yield name,image
        if source in ('horn','edge','metal'):
            horizontal=source=='metal'
            long=Image.new('RGB',(32,16) if horizontal else (16,32))
            long.paste(image,(0,0));long.paste(image,(16,0) if horizontal else (0,16))
            yield name+'_long',long
        if source=='ember':
            falling=Image.new('RGB',(16,16*16))
            for frame in range(16):
                tile=Image.new('RGB',(16,16));tile.putdata([colours[rows[(y-frame)%16][x]] for y in range(16) for x in range(16)])
                falling.paste(tile,(0,frame*16))
            yield 'runic_anvil_fall',falling
        if glow:
            # A slow native texture animation breathes gently, without block
            # ticks, particle spam or changing the sharp binary-alpha silhouette.
            mask=Image.new('RGBA',(size[0],size[1]*5))
            for frame,factor in enumerate((.84,.88,.94,.98,1.0)):
                tile=Image.new('RGBA',size);tile.putdata([(*(round(v*factor) for v in colours[c]),255) if c in 'BCD' else (0,0,0,0) for row in rows for c in row])
                mask.paste(tile,(0,frame*size[1]))
            yield name+'_glow',mask

def metadata():
    for name,size in [('runic_anvil_top_glow',48),('runic_anvil_panel_glow',16),('runic_anvil_ember',16)]:
        yield name,{'animation':{'width':size,'height':size,'frametime':8,'frames':[0,1,2,3,4,3,2,1],'interpolate':True}}
    yield 'runic_anvil_fall',{'animation':{'width':16,'height':16,'frametime':3,'frames':list(range(16)),'interpolate':True}}

if __name__=='__main__':
    output=ROOT/'src/main/resources/assets/examplemod/textures/block'
    for name,image in exports():image.save(output/(name+'.png'))
    print('Exported runic anvil metal, seal, side glyph, horn armor and amber circuits.')
