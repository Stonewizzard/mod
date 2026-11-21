"""Native 16px hand-authored glyphs on the approved Sootstone; recipes are reviewed layouts."""
from pathlib import Path
import json
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]; R=ROOT/'src/main/resources';A=R/'assets/examplemod';D=R/'data/examplemod'
def write(path,value):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(value,indent=2)+'\n')
# name, tier, center, inner x4, outer x4, glyph (7 by 7). All recipes have four blank corners.
rows=[
('storage',1,'lapis_block','gold_ingot',None,['.#####.','#.....#','#.###.#','#.#.#.#','#.###.#','#.....#','.#####.']),
('extraction',1,'dispenser','redstone',None,['...#...','..###..','.#.#.#.','...#...','...#...','.#####.','.#...#.']),
('input',1,'hopper','redstone',None,['.#...#.','.#####.','...#...','...#...','.#.#.#.','..###..','...#...']),
('swift_forging',2,'clock','sugar','quartz',['....##.','...##..','..##...','.#####.','...##..','..##...','.##....']),
('tempering',2,'iron_block','examplemod:ancient_ingot','obsidian',['.#####.','.#...#.','.#.#.#.','.#.#.#.','.#.#.#.','..###..','...#...']),
('conduit',2,'copper_block',None,'redstone',['...#...','.#####.','.#.#.#.','##.#.##','.#.#.#.','.#####.','...#...']),
('restoration',3,'anvil','iron_block','examplemod:ancient_ingot',['..###..','..#.#..','###.###','#.....#','###.###','..#.#..','..###..']),
('efficiency',3,'enchanting_table','gold_ingot','examplemod:ancient_ingot',['.#####.','.#.....','.###...','.#.....','.#####.','....#..','...#...']),
('expansion',3,'crafting_table','piston','examplemod:ancient_ingot',['###.###','#.....#','#.#.#.#','...#...','#.#.#.#','#.....#','###.###']),
('transmutation',3,'emerald_block','amethyst_shard','examplemod:ancient_ingot',['...#...','..###..','.#####.','##.#.##','...#...','..###..','.#####.']),
('nether_transmutation',3,'gilded_blackstone','blaze_rod','wither_rose',['.#...#.','.#.#.#.','.#####.','..###..','...#...','..###..','.#####.']),
('end_transmutation',3,'dragon_breath','ender_pearl','shulker_shell',['..###..','.#...#.','#..#..#','#.#.#.#','#..#..#','.#...#.','..###..']),
('batching',4,'hopper','chest','comparator',['###.###','#.#.#.#','###.###','.......','###.###','#.#.#.#','###.###']),
('invocation',4,'nether_star','examplemod:ancient_ingot','blaze_rod',['#..#..#','.#.#.#.','..###..','#######','..###..','.#.#.#.','#..#..#']),
('collection',4,'dropper','hopper','chest',['#.....#','.#...#.','..#.#..','...#...','.#.#.#.','.#####.','.......']),
('deliberation',4,'clock','soul_sand','examplemod:ancient_ingot',['.#####.','..#.#..','...#...','..#.#..','.#...#.','.#####.','...#...']),
('specialization',5,'smithing_table','diamond','examplemod:ancient_ingot',['...#...','..###..','.##.##.','##...##','.##.##.','..###..','...#...']),
('familiarity',5,'writable_book','diamond_block','examplemod:ancient_metal_block',['.#####.','.#...#.','.#.###.','.#.#...','.#.###.','.#...#.','.#####.']),
('alternation',5,'comparator','emerald_block','netherite_ingot',['..#....','.#####.','#.#..#.','.......','.#..#.#','.#####.','....#..']),
('master_craftsmanship',6,'nether_star','examplemod:ancient_crystal_block','netherite_ingot',['#..#..#','#..#..#','.#####.','..###..','...#...','.#####.','.......']),
('grand_pattern',6,'ender_chest','nether_star','examplemod:ancient_crystal_block',['#######','#..#..#','#.###.#','#######','#.###.#','#..#..#','#######']),
('mutation',6,'dragon_breath','end_crystal','examplemod:energized_ancient_crystal_block',['#.....#','.#...#.','..#.#..','...#...','..#.#..','.#...#.','#.....#'])]
lang=json.loads((A/'lang/en_us.json').read_text());ids=[]
for name,tier,center,inner,outer,glyph in rows:
 id='rune_of_'+name;ids.append('examplemod:'+id)
 image=Image.open(ROOT/'tools/asset_sources/runestone/sootstone_16.png').convert('RGBA');draw=ImageDraw.Draw(image)
 for y,row in enumerate(glyph):
  for x,v in enumerate(row):
   if v=='#':draw.rectangle((4+x,4+y,5+x,5+y),fill=(24,20,18,255))
 for y,row in enumerate(glyph):
  for x,v in enumerate(row):
   if v=='#':draw.point((4+x,4+y),fill=(255,207,99,255) if (x+y)%3==0 else (222,139,39,255))
 path=A/f'textures/block/{id}.png';path.parent.mkdir(parents=True,exist_ok=True);image.save(path)
 write(A/f'models/block/{id}.json',{'parent':'minecraft:block/cube_all','textures':{'all':'examplemod:block/'+id}})
 write(A/f'blockstates/{id}.json',{'variants':{'':{'model':'examplemod:block/'+id}}})
 write(A/f'models/item/{id}.json',{'parent':'examplemod:block/'+id});write(A/f'items/{id}.json',{'model':{'type':'minecraft:model','model':'examplemod:item/'+id}})
 write(D/f'loot_table/blocks/{id}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'examplemod:'+id}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 ingredients={slot:{'item':'examplemod:runestone'} for slot in ['north_west','north_east','south_west','south_east']}
 for item,slots in [(center,['center']),(inner,['north_inner','east_inner','south_inner','west_inner']),(outer,['north_outer','east_outer','south_outer','west_outer'])]:
  if item:
   for slot in slots:ingredients[slot]={'item':item if ':' in item else 'minecraft:'+item}
 write(D/f'anvil_recipes/{id}.json',{'anvil_tier':tier,'hammer_tier':1,'work':16+4*tier,'xp_per_hit':10*tier,'category':'runes','allow_bonus':True,'ingredients':ingredients,'result':{'id':'examplemod:'+id,'count':1}})
 lang['block.examplemod.'+id]='Rune of '+' '.join(w.title() for w in name.split('_'))
for path in [D/'tags/block/ancient_anvil_foundation.json',D/'tags/item/ancient_anvil_foundation.json',D/'tags/block/mineable/pickaxe.json']:
 if path.exists():
  obj=json.loads(path.read_text());obj['values']=list(dict.fromkeys(obj['values']+ids));write(path,obj)
# Vanilla pickaxe mining tag.
p=R/'data/minecraft/tags/block/mineable/pickaxe.json';v=json.loads(p.read_text());v['values']=list(dict.fromkeys(v['values']+ids));write(p,v)
write(D/'tags/fluid/experience.json',{'values':['examplemod:experience']});write(R/'data/c/tags/fluid/experience.json',{'values':['examplemod:experience']})
# Keep saved canopy_keystone ID while replacing display name and acquisition.
lang['block.examplemod.canopy_keystone']='Keystone Rune of the Sky'
ing={slot:{'item':'examplemod:runestone'} for slot in ['north_west','north_east','south_west','south_east']};ing['center']={'item':'examplemod:forgemaster_core'}
for slot in ['north_inner','east_inner','south_inner','west_inner']:ing[slot]={'item':'examplemod:ancient_ingot'}
for slot in ['north_outer','east_outer','south_outer','west_outer']:ing[slot]={'item':'minecraft:glowstone_dust'}
write(D/'anvil_recipes/canopy_keystone.json',{'anvil_tier':4,'work':32,'xp_per_hit':40,'category':'keystones','ingredients':ing,'result':{'id':'examplemod:canopy_keystone','count':4}})
# Delete old crafting table bypass of the boss gate.
for p in (D/'recipe').glob('*keystone*'):p.unlink()
# A seal predates its own boss drop. These starter ritual ingredients and balance are provisional.
seal={'anvil_tier':4,'requires_rune':'invocation','work':24,'xp_per_hit':30,'category':'rituals','ingredients':{'center':{'item':'minecraft:blaze_powder'},'north_inner':{'item':'minecraft:iron_block'},'east_inner':{'item':'examplemod:ancient_ingot'},'south_inner':{'item':'minecraft:iron_block'},'west_inner':{'item':'examplemod:ancient_ingot'}},'result':{'id':'examplemod:forgemaster_seal','count':1}}
write(D/'anvil_recipes/forgemaster_seal.json',seal)
for name,source,result,mutant,rune in [('overworld','netherrack','cobblestone','amethyst_shard','transmutation'),('nether','cobblestone','netherrack','quartz','nether_transmutation'),('end','cobblestone','end_stone','chorus_fruit','end_transmutation')]:
 write(D/f'anvil_recipes/transmute_{name}.json',{'anvil_tier':3,'requires_rune':rune,'category':'transmutation','work':8,'xp_per_hit':8,'ingredients':{'center':{'item':'minecraft:'+source}},'result':{'id':'minecraft:'+result,'count':1},'mutation_result':{'id':'minecraft:'+mutant,'count':1}})
write(D/'loot_table/entities/forgemaster.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'examplemod:forgemaster_core','functions':[{'function':'minecraft:set_count','count':4}]}]}]})
# Core and seal use new native pixel glyph icons; preserve all existing material artwork.
for id,color in [('forgemaster_core',(255,180,45,255)),('forgemaster_seal',(230,140,40,255)),('packed_grand_pattern',(110,215,240,255))]:
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.polygon([(7,1),(13,5),(13,10),(7,14),(2,10),(2,5)],fill=(46,38,32,255));d.line([(7,2),(12,5),(12,10),(7,13),(3,10),(3,5),(7,2)],fill=color,width=1);d.line([(7,5),(7,10)],fill=color);d.line([(5,7),(9,7)],fill=color)
 source=ROOT/f'tools/asset_sources/forge-runes/{id}_16.png';source.parent.mkdir(parents=True,exist_ok=True);im.save(source)
 # Finisher consumes 32px source, preserving native clusters.
 src=source.with_name(id+'.png');im.resize((32,32),Image.Resampling.NEAREST).save(src)
 write(A/f'models/item/{id}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'examplemod:item/'+id}});write(A/f'items/{id}.json',{'model':{'type':'minecraft:model','model':'examplemod:item/'+id}})
 lang['item.examplemod.'+id]=id.replace('_',' ').title()
 manifest=json.loads((ROOT/'tools/item-asset-manifest.json').read_text());manifest[id]={'source':str(src.relative_to(ROOT)),'min_longest':24,'max_longest':28,'allowed_holes':[],'separate_fragments':False,'model':True};write(ROOT/'tools/item-asset-manifest.json',manifest)
lang['entity.examplemod.forgemaster']='The Forgemaster';lang['fluid_type.examplemod.experience']='Liquid Experience';write(A/'lang/en_us.json',lang)
print('Generated 22 Sootstone rune materials, agreed recipes and starter rituals/conversions')
