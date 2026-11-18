"""Negative controls for the exact missing-pixel, alpha and sizing regressions."""
from PIL import Image
from audit_minecraft_assets import validate_image,validate_horn_mesh,obj_mesh,ASSETS
rule={'min_longest':24,'max_longest':28,'legacy_artwork':True,'allowed_holes':[],'aspect_range':[1.3,1.9]}
good=Image.new('RGBA',(32,32));good.paste((150,110,60,255),(3,8,29,24));assert not validate_image(good,rule)
cases=[]
a=good.copy();a.putpixel((8,10),(150,110,60,127));cases.append(('partial alpha',a))
a=good.copy();a.putpixel((8,10),(0,0,0,0));cases.append(('pinhole',a))
a=good.copy();a.putpixel((1,1),(150,110,60,255));cases.append(('fleck',a))
a=good.copy();a.paste((150,110,60,255),(0,8,3,24));cases.append(('clipping',a))
a=Image.new('RGBA',(32,32));a.paste((150,110,60,255),(12,12,20,20));cases.append(('undersized',a))
a=Image.new('RGBA',(32,32));cases.append(('empty',a))
a=Image.new('RGBA',(32,32));a.paste((150,110,60,255),(3,13,29,17));cases.append(('wrong aspect',a))
for name,im in cases:assert validate_image(im,rule),name
print('PASS: valid sprite plus seven negative controls (alpha, hole, fleck, clipping, size, empty).')
mesh=obj_mesh(ASSETS/'models/block/ancient_anvil_horn.obj')
assert not validate_horn_mesh(*mesh)
vertices,uvs,faces=mesh
bad=list(vertices);bad[0]=(2,bad[0][1],bad[0][2])
assert validate_horn_mesh(bad,uvs,faces),'out-of-cell horn was accepted'
bad=[list(p) for p in vertices];front=min(p[2] for p in bad)
for p in bad:
 if abs(p[2]-front)<1e-7 and p[1]<.75:p[1]=.4
assert any('blunt' in error for error in validate_horn_mesh(bad,uvs,faces)),'full-height horn tip was accepted'
assert validate_horn_mesh(vertices,uvs,faces[:-1]),'open horn mesh was accepted'
print('PASS: tapered horn plus negative controls for ownership, blunt tip and open mesh.')
