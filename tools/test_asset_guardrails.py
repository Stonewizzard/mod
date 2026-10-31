"""Negative controls for the exact missing-pixel, alpha and sizing regressions."""
from PIL import Image
from audit_minecraft_assets import validate_image
rule={'min_longest':24,'max_longest':28,'legacy_artwork':True,'allowed_holes':[]}
good=Image.new('RGBA',(32,32));good.paste((150,110,60,255),(3,8,29,24));assert not validate_image(good,rule)
cases=[]
a=good.copy();a.putpixel((8,10),(150,110,60,127));cases.append(('partial alpha',a))
a=good.copy();a.putpixel((8,10),(0,0,0,0));cases.append(('pinhole',a))
a=good.copy();a.putpixel((1,1),(150,110,60,255));cases.append(('fleck',a))
a=good.copy();a.paste((150,110,60,255),(0,8,3,24));cases.append(('clipping',a))
a=Image.new('RGBA',(32,32));a.paste((150,110,60,255),(12,12,20,20));cases.append(('undersized',a))
a=Image.new('RGBA',(32,32));cases.append(('empty',a))
for name,im in cases:assert validate_image(im,rule),name
print('PASS: valid sprite plus six negative controls (alpha, hole, fleck, clipping, size, empty).')
