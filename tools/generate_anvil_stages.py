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
write(4,stone,caps)
