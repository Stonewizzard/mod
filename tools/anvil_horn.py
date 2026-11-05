"""Native-pixel faceted horn: baked mesh, physics envelope and visible edges.

The tip is narrow AND thin. Its underside rises towards the end instead of
extruding a diamond vertically. Everything fits inside the upper front cell.
"""
from pathlib import Path
import math

TOP=[(1,29,0),(15,29,0),(14,29,-5),(10.5,29,-12),
     (9,29,-15),(7,29,-15),(5.5,29,-12),(2,29,-5)]
BEVEL=[(0,28.25,0),(16,28.25,0),(15,28.25,-5),(11.4,28.25,-12),
       (9.6,28.25,-15.4),(6.4,28.25,-15.4),(4.6,28.25,-12),(1,28.25,-5)]
BOTTOM=[(1,22.8,0),(15,22.8,0),(13,23.5,-5),(10.4,25.6,-12),
        (9,27,-15.4),(7,27,-15.4),(5.6,25.6,-12),(3,23.5,-5)]
VERTICES=TOP+BEVEL+BOTTOM
# Counterclockwise outward faces. Coplanar triangles do not become outline lines.
FACES=[('horn',tuple(range(8)))]
for i in range(8):
    n=(i+1)%8
    FACES += [('edge',(i,i+8,n+8,n)),('metal',(i+8,i+16,n+16,n+8))]
FACES += [('metal',(16,23,18,17)),('metal',(23,22,19,18)),('metal',(22,21,20,19))]
EDGES=[(ring+i,ring+(i+1)%8) for ring in (0,8,16) for i in range(8)]
EDGES += [(i,i+8) for i in range(8)]+[(i+8,i+16) for i in range(8)]+[(23,18),(22,19)]

def collision(cell):
    if cell!=(0,1,-1):return []
    # Horizontal native-pixel strips conservatively follow the actual polygon.
    # Between breakpoints both width and the underside are linear; each strip
    # overhangs the mesh by less than one native pixel, including its bevel.
    stations=[(-15.4,1.6,27),(-12,3.4,25.6),(-5,7,23.5),(0,8,22.8)]
    cuts=sorted({p[0] for p in stations}|{float(i) for i in range(-15,1)})
    def section(z):
        for (a,w,y),(b,W,Y) in zip(stations,stations[1:]):
            if a<=z<=b:
                t=(z-a)/(b-a);return w+(W-w)*t,y+(Y-y)*t
        raise ValueError(z)
    result=[]
    for a,b in zip(cuts,cuts[1:]):
        width,under=section(b)
        result.append(([8-width,under-16,a+16],[8+width,13,b+16],'metal'))
    return result

def obj(path,preview=False):
    # OBJ positions use block units. UVs keep the same native texel density on
    # the scaled item; no independently enlarged icon or blurry rendered sprite.
    lines=['# Generated from tools/anvil_horn.py; closed tapered forging horn.',
           'mtllib ancient_anvil_horn.mtl','o tapered_horn','s off']
    for x,y,z in VERTICES:
        p=(8+(x-8)/3,2+y/3,8+(z-8)/3) if preview else (x,y-16,z+16)
        lines.append('v '+' '.join(f'{v/16:.9f}' for v in p))
    uv_count=0
    for material,face in FACES:
        points=[VERTICES[i] for i in face]
        # Top and chamfer use plan projection. Side faces use longitudinal
        # distance and Y so the metal's pixel scale follows the forging.
        uvs=[(x/16,(z+16)/16) if material in ('horn','edge') else ((z+16)/16,(32-y)/16) for x,y,z in points]
        for u,v in uvs:lines.append(f'vt {u:.9f} {v:.9f}')
        lines.append('usemtl '+material)
        for j in range(1,len(face)-1):
            indices=(0,j,j+1)
            lines.append('f '+' '.join(f'{face[k]+1}/{uv_count+k+1}' for k in indices))
        uv_count+=len(face)
    path.write_text('\n'.join(lines)+'\n')

def write_models(directory):
    obj(directory/'ancient_anvil_horn.obj')
    obj(directory/'ancient_anvil_horn_item.obj',True)
    (directory/'ancient_anvil_horn.mtl').write_text(''.join(
        f'newmtl {name}\nKa 0 0 0\nKd 1 1 1\nmap_Kd #{name}\n\n' for name in ('horn','edge','metal')))

def model(preview=False):
    return {'parent':'minecraft:block/block','loader':'neoforge:obj',
            'model':'examplemod:models/block/ancient_anvil_horn'+('_item' if preview else '')+'.obj',
            'automatic_culling':False,'shade_quads':True,'flip_v':False,'emissive_ambient':False,
            'textures':{'horn':'examplemod:block/runic_anvil_horn',
                        'edge':'examplemod:block/runic_anvil_edge','metal':'examplemod:block/runic_anvil_metal'}}
