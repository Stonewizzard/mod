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
PROFILES={
 'base':(TOP,BEVEL,BOTTOM),
 'foundry':(
  [(0.5,29,4),(15.5,29,4),(15,29,-3),(11,29,-11.5),(9.5,29,-15),(6.5,29,-15),(5,29,-11.5),(1,29,-3)],
  [(0,28.25,4),(16,28.25,4),(16,28.25,-3),(12,28.25,-11.5),(10,28.25,-15.5),(6,28.25,-15.5),(4,28.25,-11.5),(0,28.25,-3)],
  [(1,22.6,4),(15,22.6,4),(13.8,23.5,-3),(10.8,25.7,-11.5),(9.5,27,-15.5),(6.5,27,-15.5),(5.2,25.7,-11.5),(2.2,23.5,-3)]),
 'spillway':(
  [(1,29,2),(15,29,2),(15,29,-6),(13,29,-12),(11,29,-15),(5,29,-15),(3,29,-12),(1,29,-6)],
  [(0,28.25,2),(16,28.25,2),(16,28.25,-6),(14,28.25,-12),(12,28.25,-15.5),(4,28.25,-15.5),(2,28.25,-12),(0,28.25,-6)],
  [(1,22.2,2),(15,22.2,2),(14,23,-6),(12.6,25,-12),(11,26.8,-15.5),(5,26.8,-15.5),(3.4,25,-12),(2,23,-6)]),
 'columns':(
  [(0.5,29,4),(15.5,29,4),(15.5,29,-4),(13,29,-10),(10,29,-15),(6,29,-15),(3,29,-10),(0.5,29,-4)],
  [(0,28.25,4),(16,28.25,4),(16,28.25,-4),(14,28.25,-10),(11,28.25,-15.5),(5,28.25,-15.5),(2,28.25,-10),(0,28.25,-4)],
  [(1,22.3,4),(15,22.3,4),(14,23,-4),(12.5,24.7,-10),(10,26.6,-15.5),(6,26.6,-15.5),(3.5,24.7,-10),(2,23,-4)])}

def vertices(design='base'):return sum((list(ring) for ring in PROFILES[design]),[])
def suffix(design):return '' if design=='base' else '_'+design
# Counterclockwise outward faces. Coplanar triangles do not become outline lines.
FACES=[('horn',tuple(range(8)))]
for i in range(8):
    n=(i+1)%8
    FACES += [('edge',(i,i+8,n+8,n)),('metal',(i+8,i+16,n+16,n+8))]
FACES += [('metal',(16,23,18,17)),('metal',(23,22,19,18)),('metal',(22,21,20,19))]
EDGES=[(ring+i,ring+(i+1)%8) for ring in (0,8,16) for i in range(8)]
EDGES += [(i,i+8) for i in range(8)]+[(i+8,i+16) for i in range(8)]+[(23,18),(22,19)]

def collision(cell,design='base'):
    if cell[0]!=0 or cell[1]!=1:return []
    # Horizontal native-pixel strips conservatively follow the actual polygon.
    # Between breakpoints both width and the underside are linear; each strip
    # overhangs the mesh by less than one native pixel, including its bevel.
    _,bevel,bottom=PROFILES[design]
    stations=[(bevel[i][2],bevel[i][0]-8,bottom[i][1]) for i in (4,3,2,1)]
    lo=max(stations[0][0],cell[2]*16);hi=min(stations[-1][0],cell[2]*16+16)
    if lo>=hi:return []
    cuts=sorted({lo,hi}|{p[0] for p in stations if lo<p[0]<hi}|{float(i) for i in range(math.ceil(lo),math.floor(hi)+1)})
    def section(z):
        for (a,w,y),(b,W,Y) in zip(stations,stations[1:]):
            if a<=z<=b:
                t=(z-a)/(b-a);return w+(W-w)*t,y+(Y-y)*t
        raise ValueError(z)
    result=[]
    for a,b in zip(cuts,cuts[1:]):
        width,under=section(b)
        result.append(([8-width,under-16,a-cell[2]*16],[8+width,13,b-cell[2]*16],'metal'))
    return result

def obj(path,preview=False,design='base'):
    # OBJ positions use block units. UVs keep the same native texel density on
    # the scaled item; no independently enlarged icon or blurry rendered sprite.
    lines=['# Generated from tools/anvil_horn.py; closed tapered forging horn.',
           'mtllib ancient_anvil_horn.mtl','o tapered_horn','s off']
    points_all=vertices(design)
    for x,y,z in points_all:
        p=(8+(x-8)/3,2+y/3,8+(z-8)/3) if preview else (x,y-16,z+16)
        lines.append('v '+' '.join(f'{v/16:.9f}' for v in p))
    uv_count=0
    for material,face in FACES:
        points=[points_all[i] for i in face]
        # Top and chamfer use plan projection. Side faces use longitudinal
        # distance and Y so the metal's pixel scale follows the forging.
        length=16 if design=='base' else 32
        uvs=[(x/16,(z+16)/length) if material in ('horn','edge') else ((z+16)/length,(32-y)/16) for x,y,z in points]
        for u,v in uvs:lines.append(f'vt {u:.9f} {v:.9f}')
        lines.append('usemtl '+material)
        for j in range(1,len(face)-1):
            indices=(0,j,j+1)
            lines.append('f '+' '.join(f'{face[k]+1}/{uv_count+k+1}' for k in indices))
        uv_count+=len(face)
    path.write_text('\n'.join(lines)+'\n')

def write_models(directory,design='base'):
    obj(directory/('ancient_anvil_horn'+suffix(design)+'.obj'),False,design)
    obj(directory/('ancient_anvil_horn'+suffix(design)+'_item.obj'),True,design)
    (directory/'ancient_anvil_horn.mtl').write_text(''.join(
        f'newmtl {name}\nKa 0 0 0\nKd 1 1 1\nmap_Kd #{name}\n\n' for name in ('horn','edge','metal')))

def model(preview=False,design='base'):
    return {'parent':'minecraft:block/block','loader':'neoforge:obj',
            'model':'examplemod:models/block/ancient_anvil_horn'+suffix(design)+('_item' if preview else '')+'.obj',
            'automatic_culling':False,'shade_quads':True,'flip_v':False,'emissive_ambient':False,
            'textures':{name:'examplemod:block/runic_anvil_'+texture+('_long' if design!='base' else '') for name,texture in [('horn','horn'),('edge','edge'),('metal','metal')]}}
