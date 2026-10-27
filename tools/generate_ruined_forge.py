"""Reproducible tier-one model and ruined world-template authoring. No copied mod artwork."""
from pathlib import Path
import gzip,json,struct
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources'
def j(path,obj):
 p=RES/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2)+'\n')
def cube(a,b,tex):return {'from':a,'to':b,'faces':{f:{'texture':'#'+tex} for f in ('up','down','north','south','east','west')}}
base=Path('assets/examplemod')
textures={'stone':'minecraft:block/cracked_deepslate_bricks','top':'minecraft:block/deepslate_tiles','bronze':'minecraft:block/exposed_copper','dark':'minecraft:block/polished_blackstone','particle':'minecraft:block/cracked_deepslate_bricks'}
elements=[cube([1,0,1],[15,3,15],'stone'),cube([0,3,0],[16,4,16],'bronze'),cube([2,4,2],[4,12,14],'stone'),cube([12,4,2],[14,12,14],'stone'),cube([4,4,12],[12,12,14],'stone'),cube([5,4,5],[11,6,11],'dark'),cube([4,6,4],[12,7,12],'top')]
for x in (2,12):elements.append(cube([x,10,2],[x+2,11,14],'bronze'))
j(base/'models/block/ruined_ancient_forge.json',{'parent':'minecraft:block/block','textures':textures,'elements':elements,'display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[0.625,0.625,0.625]}}})
j(base/'models/block/ruined_forge_bricks.json',{'parent':'minecraft:block/cube_all','textures':{'all':'minecraft:block/cracked_deepslate_bricks'}})
for name in ('ruined_ancient_forge','ruined_forge_bricks'):
 variants={f'facing={d}':{'model':f'examplemod:block/{name}','y':rot} for d,rot in [('north',0),('east',90),('south',180),('west',270)]} if name=='ruined_ancient_forge' else {'':{'model':f'examplemod:block/{name}'}}
 j(base/f'blockstates/{name}.json',{'variants':variants});j(base/f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'examplemod:block/{name}'}})
 j(Path(f'data/examplemod/loot_table/blocks/{name}.json'),{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'examplemod:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
j(Path('data/examplemod/tags/block/ruined_forge_masonry.json'),{'replace':False,'values':['examplemod:ruined_forge_bricks']})
p=Path('data/minecraft/tags/block/mineable/pickaxe.json')
mining=json.loads((RES/p).read_text()) if (RES/p).exists() else {'replace':False,'values':[]}
for value in ['examplemod:ruined_ancient_forge','examplemod:ruined_forge_bricks']:
 if value not in mining['values']:mining['values'].append(value)
j(p,mining)
j(Path('data/examplemod/recipe/ruined_forge_bricks.json'),{'type':'minecraft:crafting_shaped','category':'building','pattern':['DD','DD'],'key':{'D':'#minecraft:stone_crafting_materials'},'result':{'id':'examplemod:ruined_forge_bricks','count':4}})
j(Path('data/examplemod/recipe/ruined_ancient_forge.json'),{'type':'minecraft:crafting_shaped','category':'misc','pattern':['BCB','IFI','BCB'],'key':{'B':'examplemod:ruined_forge_bricks','C':'minecraft:copper_block','I':'minecraft:iron_ingot','F':'minecraft:blast_furnace'},'result':{'id':'examplemod:ruined_ancient_forge','count':1}})
for name,ingredient in [('ruined_forge_bricks','#minecraft:stone_crafting_materials'),('ruined_ancient_forge','minecraft:blast_furnace')]:
 predicate={'items':ingredient} if not ingredient.startswith('#') else {'items':ingredient}
 j(Path(f'data/examplemod/advancement/recipes/misc/{name}.json'),{'parent':'minecraft:recipes/root','criteria':{'has_material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[predicate]}}},'requirements':[['has_material']],'rewards':{'recipes':['examplemod:'+name]}})
j(Path('data/examplemod/tags/worldgen/biome/has_structure/ruined_ancient_forge.json'),{'replace':False,'values':['minecraft:plains','minecraft:sunflower_plains','minecraft:forest','minecraft:birch_forest','minecraft:old_growth_birch_forest','minecraft:taiga','minecraft:savanna']})
j(Path('data/examplemod/worldgen/structure/ruined_ancient_forge.json'),{'type':'minecraft:jigsaw','biomes':'#examplemod:has_structure/ruined_ancient_forge','step':'surface_structures','spawn_overrides':{},'terrain_adaptation':'beard_thin','start_pool':'examplemod:ruined_ancient_forge','size':1,'start_height':{'absolute':0},'project_start_to_heightmap':'WORLD_SURFACE_WG','max_distance_from_center':80,'use_expansion_hack':False})
j(Path('data/examplemod/worldgen/structure_set/ruined_ancient_forge.json'),{'structures':[{'structure':'examplemod:ruined_ancient_forge','weight':1}],'placement':{'type':'minecraft:random_spread','salt':61720341,'spacing':48,'separation':24}})
j(Path('data/examplemod/worldgen/template_pool/ruined_ancient_forge.json'),{'name':'examplemod:ruined_ancient_forge','fallback':'minecraft:empty','elements':[{'weight':1,'element':{'element_type':'minecraft:single_pool_element','location':'examplemod:ruined_ancient_forge','processors':'minecraft:empty','projection':'rigid'}}]})
# Minimal typed NBT encoder for the Minecraft structure format.
def string(s):b=s.encode();return struct.pack('>H',len(b))+b
def payload(t,v):
 if t==3:return struct.pack('>i',v)
 if t==8:return string(v)
 if t==9:
  child,values=v;return bytes([child])+struct.pack('>i',len(values))+b''.join(payload(child,x) for x in values)
 if t==10:return b''.join(bytes([typ])+string(k)+payload(typ,x) for k,(typ,x) in v.items())+b'\0'
 raise ValueError(t)
def ints(v):return (9,(3,list(v)))
palette=[{'Name':(8,n)} for n in ['minecraft:air','examplemod:ruined_forge_bricks','minecraft:gravel','minecraft:cobbled_deepslate','minecraft:mossy_stone_bricks','minecraft:stone_brick_slab']]
palette.append({'Name':(8,'examplemod:ruined_ancient_forge'),'Properties':(10,{'facing':(8,'north')})})
blocks={}
# Sunken, irregular work floor and isolated remnants, no loot chest or mob spawns.
for x in range(7):
 for z in range(7):
  if (x,z) in [(0,0),(0,6),(6,0),(6,6)]:continue
  blocks[x,0,z]=2 if (x+z)%3 else 3
parts=[(x,-1,z) for x in (-1,0,1) for z in range(3)]+[(x,y,1) for x in (-1,1) for y in (0,1)]+[(x,0,2) for x in (-1,0,1)]+[(0,y,2) for y in (1,2)]
for x,y,z in parts:
 if (x,y,z) not in [(-1,1,1),(0,2,2)]:blocks[x+3,y+2,z+2]=1
blocks[3,2,2]=6
# Foundation/ground supports, so the core sits above a raised ancient plinth.
for x in range(2,5):
 for z in range(2,5):blocks[x,0,z]=3
for p in [(0,1,2),(0,2,2),(6,1,5),(5,1,6)]:blocks[p]=4
blocks[1,1,5]=5
# Explicit air clears the small work area in generated terrain; do not overwrite foundation.
for x in range(1,6):
 for z in range(1,6):
  for y in range(1,5):blocks.setdefault((x,y,z),0)
nbt={'size':ints([7,5,7]),'palette':(9,(10,palette)),'blocks':(9,(10,[{'pos':ints(pos),'state':(3,state)} for pos,state in sorted(blocks.items())])),'entities':(9,(10,[]))}
out=RES/'data/examplemod/structure/ruined_ancient_forge.nbt';out.parent.mkdir(parents=True,exist_ok=True)
with out.open('wb') as f:
 with gzip.GzipFile(filename='',mode='wb',fileobj=f,mtime=0) as g:g.write(b'\x0a'+string('')+payload(10,nbt))
print('Generated ruined forge models, recipes, sparse worldgen, and a two-block-damaged template.')
