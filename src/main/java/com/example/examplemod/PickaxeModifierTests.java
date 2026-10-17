package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import static com.example.examplemod.PickaxeTrait.*;

public final class PickaxeModifierTests {
    public static void register(RegisterGameTestsEvent event){
        var env=event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"pickaxe_modifiers_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"pickaxe_modifiers"),new Test(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),500,0,true,Rotation.NONE,false,1,1,false,0)));
    }
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> data){super(data);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Score-scaled pickaxe effects, rarity pools and random Creative acquisition");}
        @Override public void run(GameTestHelper h){
            var p=h.makeMockServerPlayerInLevel();var pos=h.absolutePos(new BlockPos(3,1,3));p.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
            verifyPools(h);verifyCreative(h,p);verifyScaling(h,p,pos);verifyLoot(h,p,pos);verifyMagnet(h,p);
            System.out.println("PICKAXE_MODIFIERS_CHECK: PASS: all six rarity pools, independent per-part quality, native mining/drop hooks, fractional Fortune/Silk, smelting/XP, durability/reach, owned mined-item attraction, fresh Creative packets and stable saved rolls");h.succeed();
        }
    }
    private static void verifyPools(GameTestHelper h){
        for(int rarity=0;rarity<6;rarity++){
            int min=99,max=0;var counts=new HashSet<Integer>();var seen=new HashSet<PickaxeTrait>();
            for(var find:CreativePickaxeParts.FINDS)for(int i=0;i<250;i++){
                var profile=new ArtifactProfile(i*0x9E3779B97F4A7C15L,rarity,ArtifactProfile.Matrix.SHALE,find);var pool=ArtifactModifierCatalog.pool(find,rarity);var rolls=ArtifactModifiers.roll(profile,100,pool);
                check(h,rolls.equals(ArtifactModifiers.roll(profile,100,pool)),"Identical sealed seeds must produce identical rolls");check(h,!rolls.isEmpty(),"Every eligible pickaxe part must get at least one modifier");
                min=Math.min(min,rolls.size());max=Math.max(max,rolls.size());counts.add(rolls.size());int harvest=0;var unique=new HashSet<String>();
                for(var roll:rolls){var trait=PickaxeTrait.byId(roll.id());seen.add(trait);var range=ArtifactModifierCatalog.tier(rarity).ranges().get(trait);check(h,range!=null&&roll.value()>=range.minimum()&&roll.value()<=range.maximum()&&unique.add(roll.id()),"Rolls must stay in the tier's range and contain no duplicate traits");if(trait==FORTUNE||trait==SILK||trait==SMELTING)harvest++;}
                check(h,harvest<=1,"Harvest traits must be mutually exclusive on one piece");
            }
            check(h,min==1&&max==new int[]{1,2,3,3,4,4}[rarity],"Higher tiers may roll extra modifiers without guaranteeing them");
            check(h,rarity==0?counts.size()==1:counts.size()>1,"Modifier counts must have the intended rarity variation");
            check(h,rarity>=2||!seen.contains(SILK),"Common and Uncommon must never roll Silk Touch");check(h,rarity>=3||!seen.contains(SMELTING),"Smelting must be restricted to Epic and higher");
        }
        var old=new ArtifactProfile(721,0,ArtifactProfile.Matrix.SANDSTONE,ArtifactProfile.Find.PICKAXE_LEFT);var specimen=ArchaeologyContent.specimen(721,0);old.write(specimen);var study=finish(new ExcavationStudy(old));
        var root=specimen.get(DataComponents.CUSTOM_DATA).copyTag();root.put(ArtifactModifiers.KEY,ArtifactModifiers.record(old,100,new ArtifactModifiers.Pool(0,0,List.of())));specimen.set(DataComponents.CUSTOM_DATA,CustomData.of(root));ArtifactModifiers.seal(specimen,study);
        check(h,ArtifactModifiers.read(ArchaeologyContent.reward(study,specimen)).isEmpty(),"Previously sealed empty records must not reroll after balance changes");
        var half=new ItemStack(ExampleMod.ANCIENT_ARTIFACT.get());old.write(half);study.write(half);root=half.get(DataComponents.CUSTOM_DATA).copyTag();var data=root.getCompoundOrEmpty(ExcavationStudy.KEY);data.putInt("Damage",50);root.put(ExcavationStudy.KEY,data);half.set(DataComponents.CUSTOM_DATA,CustomData.of(root));var reward=ArchaeologyContent.reward(ExcavationStudy.read(half));
        check(h,reward.is(ReconstructionContent.LEFT.get())&&ArtifactQuality.score(reward)==50&&!ArtifactModifiers.read(reward).isEmpty(),"Completed damaged pickaxe parts must remain usable with score-scaled modifiers");
    }
    private static void verifyCreative(GameTestHelper h,ServerPlayer player){
        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);var random=RandomSource.create(1001);
        for(var find:CreativePickaxeParts.FINDS)for(int score:new int[]{0,50,100}){
            var a=CreativePickaxeParts.template(find,score);var b=a.copy();check(h,ArtifactModifiers.read(a).isEmpty(),"The Creative catalog itself must stay unrolled");
            new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(36,a).handle(player.connection);new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(37,b).handle(player.connection);
            NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));var first=player.getInventory().getItem(0);var second=player.getInventory().getItem(1);
            check(h,!ArtifactModifiers.read(first).isEmpty()&&!ArtifactModifiers.read(second).isEmpty()&&ArtifactQuality.score(first)==score,"Actual Creative inventory packets must materialize random modifiers at all three scores");
            var tag=first.get(DataComponents.CUSTOM_DATA).copyTag();check(h,tag.getCompoundOrEmpty(ArtifactQuality.RECOVERED).getLongOr("Seed",0)!=second.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.RECOVERED).getLongOr("Seed",0),"Two fresh grabs of the same Creative entry must get distinct seeds");
            var before=first.copy();player.getInventory().setItem(9,first);player.getInventory().setItem(0,ItemStack.EMPTY);NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));check(h,ItemStack.matches(before,player.getInventory().getItem(9))&&!CreativePickaxeParts.materialize(first,random),"Moving an acquired part must never reroll it");
        }
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
    }
    private static void verifyScaling(GameTestHelper h,ServerPlayer player,BlockPos pos){
        for(int score:new int[]{0,50,100}){
            var tool=tool(score,Map.of(SPEED,.10,DURABILITY,.40,REACH,1.0));player.setItemInHand(InteractionHand.MAIN_HAND,tool);
            var speed=new PlayerEvent.BreakSpeed(player,Blocks.STONE.defaultBlockState(),8,pos);NeoForge.EVENT_BUS.post(speed);
            check(h,Math.abs(speed.getNewSpeed()-8*(1+.1*score/100))<.0001,"A +10% speed roll must yield exactly +0/+5/+10 percent at 0/50/100 quality");
            check(h,tool.getMaxDamage()==1800+(int)(720*score/100),"Native durability must scale from the individual part's score");
            double reach=tool.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.BLOCK_INTERACTION_RANGE,4.5,EquipmentSlot.MAINHAND);check(h,Math.abs(reach-(4.5+score/100.0))<.0001,"Native main-hand block reach must scale with quality");
            var encoded=ItemStack.CODEC.encodeStart(h.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),tool).getOrThrow();var decoded=ItemStack.CODEC.parse(h.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),encoded).getOrThrow();check(h,ItemStack.matches(tool,decoded)&&PickaxeModifiers.totals(tool).equals(PickaxeModifiers.totals(decoded)),"Effects, rolls and native components must survive item serialization");
        }
        var stats=PickaxeModifiers.totals(tool(100,Map.of(AQUATIC,1.0,AIRBORNE,1.0)));check(h,PickaxeEvents.speedMultiplier(stats,true,.2,false)==25&&PickaxeEvents.speedMultiplier(stats,true,1,true)==1,"Penalty-removal modifiers must respect native underwater affinity and airborne penalties");
        var pieces=parts(0);pieces.set(0,part(ArtifactProfile.Find.PICKAXE_LEFT,100,Map.of(SPEED,.1)));var mixed=ArtifactQuality.assemble(pieces);check(h,ArtifactQuality.assemblyScore(mixed)==20&&PickaxeModifiers.totals(mixed).get(SPEED)==.1,"The assembly average must not weaken an already score-scaled part a second time");
        int rounded=0;var random=RandomSource.create(222);for(int i=0;i<4000;i++)rounded+=PickaxeEvents.stochastic(random,.5);check(h,rounded>1850&&rounded<2150,"Fractional Fortune and XP must retain their expected value rather than truncate to zero");
    }
    private static void verifyLoot(GameTestHelper h,ServerPlayer player,BlockPos pos){
        for(int score:new int[]{0,50,100}){
            var fortune=tool(score,Map.of(FORTUNE,3.0));int diamonds=0;
            for(int i=0;i<128;i++){var event=drop(h,player,pos,Blocks.DIAMOND_ORE,fortune);diamonds+=event.getDrops().stream().filter(e->e.getItem().is(Items.DIAMOND)).mapToInt(e->e.getItem().getCount()).sum();}
            check(h,score==0?diamonds==128:diamonds>160,"Native Fortune loot must activate only at positive effective quality");check(h,!fortune.has(DataComponents.ENCHANTMENTS)||fortune.get(DataComponents.ENCHANTMENTS).isEmpty(),"Virtual Fortune must never permanently enchant the real tool");
            var silk=tool(score,Map.of(SILK,1.0));int silkCount=0;
            for(int i=0;i<128;i++){var event=drop(h,player,pos,Blocks.DIAMOND_ORE,silk);if(event.getDrops().stream().anyMatch(e->e.getItem().is(Items.DIAMOND_ORE)))silkCount++;}
            check(h,score==0?silkCount==0:score==100?silkCount==128:silkCount>35&&silkCount<93,"Silk Touch must use the score-scaled activation probability");
            var smelter=tool(score,Map.of(SMELTING,1.0));int smelted=0;
            for(int i=0;i<100;i++){var event=drop(h,player,pos,Blocks.IRON_ORE,smelter);if(event.getDrops().stream().anyMatch(e->e.getItem().is(Items.IRON_INGOT)))smelted++;}
            check(h,score==0?smelted==0:score==100?smelted==100:smelted>25&&smelted<75,"Smelting must use native furnace recipes and score-scaled chance");
            var scholar=drop(h,player,pos,Blocks.DIAMOND_ORE,tool(score,Map.of(EXPERIENCE,1.0)),10);check(h,scholar.getDroppedExperience()==10+score/10,"Mining XP must scale by the original part score");
        }
        var ender=drop(h,player,pos,Blocks.ENDER_CHEST,tool(100,Map.of(SILK,1.0)));check(h,ender.getDrops().size()==1&&ender.getDrops().getFirst().getItem().is(Items.ENDER_CHEST),"Silk Touch must recover Ender Chests despite their block-entity type");
        var chest=drop(h,player,pos,Blocks.CHEST,tool(100,Map.of(SILK,1.0,FORTUNE,3.0)));check(h,chest.getDrops().size()==1&&chest.getDrops().getFirst().getItem().is(Items.CHEST),"Regular container block loot must stay intact");
        var both=tool(100,Map.of(SILK,1.0,SMELTING,1.0,FORTUNE,3.0));var drop=drop(h,player,pos,Blocks.IRON_ORE,both,10);check(h,drop.getDrops().size()==1&&drop.getDrops().getFirst().getItem().is(Items.IRON_ORE)&&drop.getDroppedExperience()==0,"Successful Silk Touch must take precedence over Fortune/smelting and suppress XP");
        h.getLevel().setBlockAndUpdate(pos,Blocks.DIAMOND_ORE.defaultBlockState());player.setItemInHand(InteractionHand.MAIN_HAND,tool(100,Map.of(SILK,1.0)));check(h,player.gameMode.destroyBlock(pos),"A real native mining action must still succeed with modifiers");
        var entities=h.getLevel().getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(1));check(h,entities.stream().anyMatch(e->e.getItem().is(Items.DIAMOND_ORE)),"The registered drop hook must affect actual mined-block drops");entities.forEach(ItemEntity::discard);
    }
    private static BlockDropsEvent drop(GameTestHelper h,ServerPlayer player,BlockPos pos,net.minecraft.world.level.block.Block block,ItemStack tool){return drop(h,player,pos,block,tool,0);}
    private static BlockDropsEvent drop(GameTestHelper h,ServerPlayer player,BlockPos pos,net.minecraft.world.level.block.Block block,ItemStack tool,int xp){
        var state=block.defaultBlockState();var items=new ArrayList<ItemEntity>();for(var stack:net.minecraft.world.level.block.Block.getDrops(state,h.getLevel(),pos,null,player,tool))items.add(new ItemEntity(h.getLevel(),pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,stack));
        var event=new BlockDropsEvent(h.getLevel(),pos,state,null,items,player,tool);event.setDroppedExperience(xp);NeoForge.EVENT_BUS.post(event);return event;
    }
    private static void verifyMagnet(GameTestHelper h,ServerPlayer player){
        var own=new ItemEntity(h.getLevel(),player.getX()+3,player.getY()+.7,player.getZ(),new ItemStack(Items.COBBLESTONE));h.getLevel().addFreshEntity(own);PickaxeEvents.markMinedDrop(own,player);
        var thrown=new ItemEntity(h.getLevel(),player.getX()+3,player.getY()+.7,player.getZ(),new ItemStack(Items.DIAMOND));h.getLevel().addFreshEntity(thrown);
        var other=h.makeMockServerPlayerInLevel();var foreign=new ItemEntity(h.getLevel(),player.getX()+3,player.getY()+.7,player.getZ(),new ItemStack(Items.IRON_INGOT));h.getLevel().addFreshEntity(foreign);PickaxeEvents.markMinedDrop(foreign,other);
        own.setDeltaMovement(0,0,0);thrown.setDeltaMovement(0,0,0);foreign.setDeltaMovement(0,0,0);PickaxeEvents.attract(player,2);check(h,own.getDeltaMovement().lengthSqr()==0,"Drops outside the effective magnet radius must stay untouched");PickaxeEvents.attract(player,4);check(h,own.getDeltaMovement().x<0&&thrown.getDeltaMovement().lengthSqr()==0&&foreign.getDeltaMovement().lengthSqr()==0,"Magnetism must pull only the owner's freshly mined drops, not arbitrary items or other players' drops");own.discard();thrown.discard();foreign.discard();
    }
    private static ArrayList<ItemStack> parts(int score){var parts=new ArrayList<ItemStack>();for(var find:CreativePickaxeParts.FINDS)parts.add(part(find,score,Map.of()));return parts;}
    private static ItemStack tool(int score,Map<PickaxeTrait,Double> traits){var parts=parts(0);parts.set(0,part(ArtifactProfile.Find.PICKAXE_LEFT,score,traits));return ArtifactQuality.assemble(parts);}
    private static ItemStack part(ArtifactProfile.Find find,int score,Map<PickaxeTrait,Double> traits){
        var stack=new ItemStack(ReconstructionContent.reward(find));var root=new CompoundTag();var data=new CompoundTag();data.putInt("Quality",score);data.putInt("Rarity",5);data.putString("Find",find.name());root.put(ArtifactQuality.RECOVERED,data);
        var profile=new ArtifactProfile(109,5,ArtifactProfile.Matrix.OBSIDIAN,find);var record=ArtifactModifiers.record(profile,score,new ArtifactModifiers.Pool(0,0,List.of()));var rolls=new ArrayList<ArtifactModifiers.Roll>();for(var trait:PickaxeTrait.values())if(traits.containsKey(trait))rolls.add(new ArtifactModifiers.Roll(trait.id,traits.get(trait),score/100.0));root.put(ArtifactModifiers.KEY,ArtifactModifiers.writeRolls(record,rolls));stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));return stack;
    }
    private static ExcavationStudy finish(ExcavationStudy study){for(int cell=0;cell<ExcavationStudy.CELLS;cell++)while(study.depth(cell)>1&&!study.complete())study.work(1,cell);for(int cell=0;cell<ExcavationStudy.CELLS;cell++)study.work(2,cell);return study;}
    private static void check(GameTestHelper helper,boolean value,String message){if(!value)throw helper.assertionException(message);}
}
