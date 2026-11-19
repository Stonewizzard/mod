"""Editable measurements of the player's Tier 3/4 workshop. No artwork is generated."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'src/main/resources/data/examplemod/anvil_stages'
# Relative to the anvil core: x=right, z=back, y=up.
CORNER_INNER=6
CORNER_OUTER=7
RING_RADIUS=8
SIDE_PILLAR_DISTANCE=16
RING_Y=-3
SIDE_BASE_Y=-4
CAP_Y=3
OUTER_CIRCLE_RADIUS=SIDE_PILLAR_DISTANCE
def circle_outline(radius):
    # Include cells crossed by the ideal circle. Integer half-cell bounds produce
    # a symmetric, face-connected staircase with no diagonal gaps or 2x2 patches.
    return {(x,z) for x in range(-radius,radius+1) for z in range(-radius,radius+1)
            if max(2*abs(x)-1,0)**2+max(2*abs(z)-1,0)**2 < (2*radius)**2
            < (2*abs(x)+1)**2+(2*abs(z)+1)**2}

def write(tier,stone,caps):
    assert len(stone)==len(set(stone)) and len(caps)==len(set(caps))
    assert not set(stone)&set(caps)
    groups=[{'tag':'examplemod:ancient_anvil_runes','positions':sorted(stone)},
            {'tag':'examplemod:ancient_anvil_caps','positions':sorted(caps)}]
    OUT.mkdir(parents=True,exist_ok=True)
    (OUT/f'tier_{tier}.json').write_text(json.dumps({'tier':tier,'groups':groups},indent=2)+'\n')
    print(f'Tier {tier}: {len(stone)} Runestone, {len(caps)} caps')
stone=[];caps=[]
for sx in (-1,1):
    for sz in (-1,1):
        stone += [(sx*d,-1,sz*d) for d in (4,5)]
        for x in (CORNER_INNER,CORNER_OUTER):
            for z in (CORNER_INNER,CORNER_OUTER):
                stone += [(sx*x,y,sz*z) for y in range(-1,3)]
                caps.append((sx*x,3,sz*z))
write(3,stone,caps)
stone=[(x,RING_Y,z) for x in range(-RING_RADIUS,RING_RADIUS+1) for z in range(-RING_RADIUS,RING_RADIUS+1) if max(abs(x),abs(z))==RING_RADIUS]
caps=[]
for dx,dz in ((1,0),(-1,0),(0,1),(0,-1)):
    x,z=dx*SIDE_PILLAR_DISTANCE,dz*SIDE_PILLAR_DISTANCE
    stone += [(x+i,SIDE_BASE_Y,z+k) for i in (-1,0,1) for k in (-1,0,1)]
    stone += [(x,y,z) for y in range(SIDE_BASE_Y+1,CAP_Y)]
    caps.append((x,CAP_Y,z))
# The outline shares twelve cells with the four existing 3x3 bases.
stone=sorted(set(stone)|{(x,SIDE_BASE_Y,z) for x,z in circle_outline(OUTER_CIRCLE_RADIUS)})
write(4,stone,caps)

# D: Grand Runic Canopy. The open roof floats above the anvil; it has no support posts.
CANOPY_Y=16
CANOPY_RADIUS=12
CANOPY_DIAGONAL=17
outer={(x,z) for x in range(-12,13) for z in range(-12,13)
       if max(abs(x),abs(z))<=12 and abs(x)+abs(z)<=17}
inside={(x,z) for x in range(-10,11) for z in range(-10,11) if abs(x)+abs(z)<=14}
rim=outer-inside
diamond={(x,z) for x in range(-4,5) for z in range(-4,5) if 3<=abs(x)+abs(z)<=4}
spokes={(x,z) for x in range(-9,10) for z in range(-9,10)
        if min(abs(x),abs(z))>=2 and abs(abs(x)-abs(z))<=1 and abs(x)+abs(z)<=17}
outer_nodes={(sx*a,sz*b) for sx in (-1,1) for sz in (-1,1) for a,b in ((5,11),(11,5))}
inner_nodes={(0,-4),(4,0),(0,4),(-4,0)}
spoke_nodes={(sx*5,sz*5) for sx in (-1,1) for sz in (-1,1)}
nodes=outer_nodes|inner_nodes|spoke_nodes
pads={(x+dx,z+dz) for x,z in outer_nodes|inner_nodes for dx in (-1,0,1) for dz in (-1,0,1)}
face=rim|diamond|spokes|pads
lip={(x,z) for x,z in rim if any((x+dx,z+dz) not in outer for dx,dz in ((1,0),(-1,0),(0,1),(0,-1)))}
stone=sorted({(x,CANOPY_Y,z) for x,z in face}|{(x,CANOPY_Y-1,z) for x,z in lip})
keystones=sorted((x,CANOPY_Y+1,z) for x,z in nodes)
assert not set(stone)&set(keystones)
(OUT/'tier_5.json').write_text(json.dumps({'tier':5,'groups':[
    {'tag':'examplemod:ancient_anvil_runes','positions':stone},
    {'tag':'examplemod:ancient_anvil_keystones','positions':keystones}
]},indent=2)+'\n')
print(f'Tier 5: {len(stone)} Runestone, {len(keystones)} Canopy Keystones')
