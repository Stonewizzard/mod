package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import static com.example.examplemod.PickaxeTrait.*;

/** Native catalog, equipment, loot, live-tool persistence and guarded area-mining regressions. */
public final class ModifierExpansionTests {
    public static void register(RegisterGameTestsEvent event){
        var env=event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"modifier_expansion_environment"));
        for(int i=0;i<3;i++)event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"modifier_expansion_"+i),new Test(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),600,0,true,Rotation.NONE,false,1,1,false,0),i));
    }
    private static final class Test extends GameTestInstance {
        final int mode;Test(TestData<Holder<TestEnvironmentDefinition<?>>> data,int mode){super(data);this.mode=mode;}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("120-modifier component and runtime integration");}
        @Override public void run(GameTestHelper h){
            if(mode==0)catalog(h);else if(mode==1)equipment(h);else gameplay(h);
            System.out.println("MODIFIER_EXPANSION_CHECK_"+mode+": PASS");h.succeed();
        }
    }
    private static void catalog(GameTestHelper h){
        check(h,PickaxeTrait.values().length==120,"Exactly 120 modifiers must be registered");int ordinary=0,nether=0,end=0,utility=0;
        for(var trait:PickaxeTrait.values()){
            var spec=ArtifactModifierCatalog.spec(trait);if(!spec.influenceOnly())ordinary++;else if(spec.influence()==ModifierInfluence.NETHER)nether++;else if(spec.influence()==ModifierInfluence.END)end++;
            if(trait.utility)utility++;check(h,AscensionResources.BINDINGS.containsKey(trait),"Every modifier needs a material binding");
            check(h,trait.growth!=PickaxeTrait.Growth.MILESTONE||trait.unit==PickaxeTrait.Unit.PERK&&!trait.milestones.isEmpty(),"Milestones must declare their actual upgrades");
        }
        check(h,ordinary==100&&nether==10&&end==10&&utility==12,"Pool counts must stay 100 ordinary, 10+10 exclusive and 12 shared");
        for(var find:CreativePickaxeParts.FINDS)for(var influence:ModifierInfluence.values())for(int rarity=0;rarity<6;rarity++){
            var pool=ArtifactModifierCatalog.pool(find,rarity,influence);boolean shared=find==ArtifactProfile.Find.TOOL_HANDLE||find==ArtifactProfile.Find.TOOL_CONNECTOR;
            for(var definition:pool.entries())check(h,PickaxeTrait.byId(definition.id()).utility==shared,"Shared pieces must never roll mining effects through any influence");
            final int r=rarity;for(int seed=0;seed<40;seed++){
                var profile=new ArtifactProfile(seed*0x9E3779B97F4A7C15L,rarity,ArtifactProfile.Matrix.SHALE,find);
                for(var roll:ArtifactModifiers.roll(profile,100,pool))check(h,pool.entries().stream().anyMatch(d->d.contains(roll,r)),"Every rolled band must be legal for its exact role, influence and rarity");
            }
        }
        for(var grade:ModifierScaling.values())for(int quality:new int[]{0,50,100,120}){
            var tool=tool(quality,Map.of(VEIN_MINER,1.0),grade);PickaxeProgression.write(tool,new PickaxeProgression.State(100,0,10));
            check(h,PickaxeModifiers.totals(tool).get(VEIN_MINER)==Math.min(1,quality/100.0),"Milestone activation must scale with quality, never with numeric growth");
            check(h,PickaxeModifiers.stage(tool,VEIN_MINER)==(quality==0?0:3),"All grades must unlock final milestones by the final ascension");
            PickaxeProgression.write(tool,new PickaxeProgression.State(30,0,2));
            check(h,PickaxeModifiers.stage(tool,VEIN_MINER)==(quality>0&&grade.ordinal()>=4?1:0),"A/S grades must unlock milestone one at ascension two");
            var saved=tool.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY).copy();PickaxeGrowth.awardLevel(tool,31);
            check(h,tool.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(PickaxeProgression.KEY).getCompoundOrEmpty(PickaxeGrowth.COUNTERS).isEmpty()&&tool.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY).equals(saved),"Milestone-only builds must not gain fake numerical growth or mutate recovered parts");
        }
        var view=new PickaxeTooltips.ToolView(new PickaxeProgression.State(20,0,2),100,Arrays.stream(PickaxeTrait.values()).map(t->new PickaxeTooltips.Stat(t,.5,ModifierScaling.B,ModifierScaling.S)).toList());
        var seen=new HashSet<String>();for(int page=0;page<PickaxeTooltips.pages(view);page++){
            var lines=PickaxeTooltips.tool(view,page);check(h,lines.size()<=14,"Every tooltip page must remain compact");lines.forEach(l->seen.add(l.getString()));
        }
        for(var trait:PickaxeTrait.values())check(h,seen.stream().anyMatch(s->s.startsWith(PickaxeTooltips.label(trait)+"  ")),"Paging must expose every modifier instead of truncating it");
    }
    private static void equipment(GameTestHelper h){
        var player=h.makeMockServerPlayerInLevel();player.setGameMode(GameType.SURVIVAL);
        for(var pattern:ReconstructionPattern.values()){
            var pieces=parts(pattern,100,Map.of(DURABILITY,.2,RESERVE,100.0,UNBREAKING,.75,SWIFT_GRIP,.05,GUARD,2.0),ModifierScaling.B);
            // A legacy mining roll on a handle must remain saved but must not affect another tool family.
            var last=pieces.getLast();set(last,List.of(new ArtifactModifiers.Roll(SPEED.id,.5,1,ModifierScaling.B),new ArtifactModifiers.Roll(DURABILITY.id,.2,1,ModifierScaling.B),new ArtifactModifiers.Roll(UNBREAKING.id,.75,1,ModifierScaling.B)));
            var tool=ArtifactQuality.assemble(pattern,pieces);player.setItemInHand(InteractionHand.MAIN_HAND,tool);
            check(h,PickaxeModifiers.totals(tool).get(SPEED)==(pattern==ReconstructionPattern.PICKAXE?.5:0),"Legacy mining rolls must be ignored on shared non-pickaxe equipment");
            check(h,tool.getMaxDamage()>1800&&tool.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.MOVEMENT_SPEED,.1,EquipmentSlot.MAINHAND)>.1,"Universal durability and movement attributes must work on every reconstructed equipment type");
            int spent=0;for(int i=0;i<1000;i++)spent+=tool.getItem().damageItem(tool,1,player,s->{});
            check(h,spent>170&&spent<330,"Native durability hook must apply shared Hardwearing on swords, maces and tools");
            var ops=h.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
            var decoded=ItemStack.CODEC.parse(ops,ItemStack.CODEC.encodeStart(ops,tool).getOrThrow()).getOrThrow();check(h,ItemStack.matches(tool,decoded),"All equipment must retain modifiers and attributes through native saves");
        }
        var ordinary=new ItemStack(Items.DIAMOND_SWORD);check(h,SharedToolEffects.damage(ordinary,10,player)==10,"Ordinary equipment must not acquire reconstructed tool effects");
        var repair=tool(100,Map.of(SELF_REPAIR,2.0,RAIN_REPAIR,2.0,METAL_MAINTENANCE,20.0),ModifierScaling.B);repair.setDamageValue(100);MiningRuntime.repair(repair,20);check(h,repair.getDamageValue()==80,"Utility repairs must reduce damage without changing maximum durability");
    }
    private static void gameplay(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();p.setGameMode(GameType.SURVIVAL);var pos=h.absolutePos(new BlockPos(3,1,3));p.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        record Conversion(PickaxeTrait trait,Block block,Item input,Item output){}
        for(var conversion:List.of(new Conversion(GRAVEL_SIFTER,Blocks.GRAVEL,Items.GRAVEL,Items.FLINT),new Conversion(SAND_GLASS,Blocks.SAND,Items.SAND,Items.GLASS),new Conversion(CLAY_FIRING,Blocks.CLAY,Items.CLAY_BALL,Items.BRICK),new Conversion(STONE_SMOOTHER,Blocks.STONE,Items.COBBLESTONE,Items.STONE),new Conversion(DEEPSLATE_POLISHER,Blocks.DEEPSLATE,Items.COBBLED_DEEPSLATE,Items.POLISHED_DEEPSLATE))){
            boolean converted=false;var tool=tool(100,Map.of(conversion.trait(),conversion.trait().cap),ModifierScaling.S);
            for(int i=0;i<30;i++){var e=drop(h,p,pos,conversion.block(),tool,new ItemStack(conversion.input(),3),0);converted|=e.getDrops().stream().anyMatch(d->d.getItem().is(conversion.output())&&d.getItem().getCount()==3);}
            check(h,converted,"Each sediment conversion must use its actual final native drop and preserve counts");
        }
        var condenser=tool(100,Map.of(XP_CONDENSER,1.0),ModifierScaling.B);int released=0;
        for(int i=0;i<10;i++)released+=drop(h,p,pos,Blocks.DIAMOND_ORE,condenser,new ItemStack(Items.DIAMOND),10).getDroppedExperience();
        check(h,released==100&&MiningRuntime.read(condenser).getIntOr("XPBank",0)==0,"The XP condenser must conserve XP across complete release cycles");
        var charge=tool(100,Map.of(CHARGE_BATTERY,2.0,GEM_BATTERY,5.0,XP_BATTERY,1.0),ModifierScaling.B);
        var event=drop(h,p,pos,Blocks.DIAMOND_ORE,charge,new ItemStack(Items.DIAMOND),10);
        check(h,MiningRuntime.charge(charge)==8&&event.getDroppedExperience()==9,"Charge generators must combine while XP battery consumes exactly one real XP");
        var frozen=MiningRuntime.read(charge);MiningRuntime.begin(p.getUUID());try{drop(h,p,pos,Blocks.DIAMOND_ORE,charge,new ItemStack(Items.DIAMOND),10);}finally{MiningRuntime.end(p.getUUID());}
        check(h,MiningRuntime.read(charge).equals(frozen),"Secondary breaks must not farm resonance or mutate primary cooldowns");
        MiningRuntime.addCharge(charge,1000);check(h,MiningRuntime.charge(charge)==100&&!MiningRuntime.spend(charge,101)&&MiningRuntime.charge(charge)==100,"Charge capacity and failed spending must be atomic");
        var overflow=tool(100,Map.of(PHASEBOUND,1.0,ENDER_DISPATCH,1.0,DIMENSIONAL_BATTERY,1.0),ModifierScaling.B);PickaxeProgression.write(overflow,new PickaxeProgression.State(100,0,10));
        p.getInventory().clearContent();for(int slot=0;slot<p.getInventory().getContainerSize();slot++)p.getInventory().setItem(slot,new ItemStack(Items.DIRT,64));p.getEnderChestInventory().clearContent();
        var e=drop(h,p,pos,Blocks.DIAMOND_ORE,overflow,new ItemStack(Items.DIAMOND,6),0);check(h,e.getDrops().isEmpty()&&p.getEnderChestInventory().countItem(Items.DIAMOND)==6,"Ender dispatch must retain exact overflow counts");
        for(int slot=0;slot<p.getEnderChestInventory().getContainerSize();slot++)p.getEnderChestInventory().setItem(slot,new ItemStack(Items.DIRT,64));
        e=drop(h,p,pos,Blocks.DIAMOND_ORE,overflow,new ItemStack(Items.DIAMOND,6),0);check(h,e.getDrops().stream().mapToInt(d->d.getItem().getCount()).sum()==6,"Full inventory and Ender Chest must leave every overflow item in the world");
        p.getInventory().clearContent();p.getInventory().setItem(5,new ItemStack(Items.IRON_NUGGET,18));MiningCollectionEffects.compact(p,0);
        check(h,p.getInventory().countItem(Items.IRON_NUGGET)==9&&p.getInventory().countItem(Items.IRON_INGOT)==1,"Compaction must consume exactly nine ingredients");
        var named=new ItemStack(Items.IRON_NUGGET,9);named.set(DataComponents.CUSTOM_NAME,Component.literal("Keep me"));p.getInventory().clearContent();p.getInventory().setItem(5,named);MiningCollectionEffects.compact(p,0);check(h,p.getInventory().countItem(Items.IRON_INGOT)==0,"Compaction must never consume named or customized ingredients");
        var ward=tool(100,Map.of(SPOILS_SHIELD,1.0),ModifierScaling.B);
        var protectedDrops=drop(h,p,pos,Blocks.STONE,ward,new ItemStack(Items.COBBLESTONE),0);var protectedItem=protectedDrops.getDrops().getFirst();
        var protection=new net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent(protectedItem,p.damageSources().generic(),false);NeoForge.EVENT_BUS.post(protection);
        check(h,protection.isInvulnerable()&&!protectedItem.isInvulnerable(),"Spoils Ward must protect native damage checks without permanently changing entity flags");
        MiningCollectionEffects.stop();protection=new net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent(protectedItem,p.damageSources().generic(),false);NeoForge.EVENT_BUS.post(protection);check(h,!protection.isInvulnerable(),"Clearing temporary protection must leave native damage behavior intact");
        var utility=tool(100,Map.of(ANVIL_THRIFT,.5,MENDING_BOOST,1.0),ModifierScaling.B);utility.setDamageValue(100);p.setItemInHand(InteractionHand.MAIN_HAND,utility);
        var anvil=new net.neoforged.neoforge.event.AnvilUpdateEvent(utility,new ItemStack(Items.IRON_INGOT,4),null,utility.copy(),10,4,p);NeoForge.EVENT_BUS.post(anvil);
        check(h,anvil.getXpCost()==5&&anvil.getMaterialCost()==2,"Frugal Repairs must change real native anvil event costs");
        var orb=new net.minecraft.world.entity.ExperienceOrb(h.getLevel(),p.getX(),p.getY(),p.getZ(),5);NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerXpEvent.PickupXp(p,orb));
        check(h,utility.getDamageValue()==95,"Mending Affinity must repair the held tool through native XP pickup");
        placement(h,p,pos);
        area(h,p,pos);
    }
    private static void placement(GameTestHelper h,ServerPlayer p,BlockPos origin){
        var tool=tool(100,Map.of(RECLAMATION,0.0),ModifierScaling.B);p.getInventory().clearContent();p.getInventory().setItem(5,new ItemStack(Items.COBBLESTONE,4));p.setItemInHand(InteractionHand.MAIN_HAND,tool);
        var drops=new ArrayList<ItemEntity>();var e=new BlockDropsEvent(h.getLevel(),origin,Blocks.STONE.defaultBlockState(),null,drops,p,tool);var c=new MiningContext(e,p);var target=origin.west(2);h.getLevel().setBlockAndUpdate(target,Blocks.AIR.defaultBlockState());
        boolean[] cancel={true};NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent event)->{if(cancel[0]&&event.getPos().equals(target))event.setCanceled(true);});
        check(h,!MiningPlacementEffects.place(c,target,Blocks.COBBLESTONE,Items.COBBLESTONE,false)&&h.getLevel().getBlockState(target).isAir()&&p.getInventory().countItem(Items.COBBLESTONE)==4,"Canceled utility placement must restore the world and consume no material");cancel[0]=false;
        check(h,MiningPlacementEffects.place(c,target,Blocks.COBBLESTONE,Items.COBBLESTONE,false)&&p.getInventory().countItem(Items.COBBLESTONE)==3,"Successful utility placement must consume exactly one inventory item");
        h.getLevel().setBlockAndUpdate(target,Blocks.CHEST.defaultBlockState());check(h,!MiningPlacementEffects.place(c,target,Blocks.COBBLESTONE,Items.COBBLESTONE,false),"Utility placement must never overwrite a block entity");
        var chargeTool=tool(100,Map.of(CHARGE_BATTERY,1.0),ModifierScaling.B);boolean[] blockDrops={true};NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGH,(BlockDropsEvent event)->{if(blockDrops[0]&&event.getPos().equals(origin))event.setCanceled(true);});
        drop(h,p,origin,Blocks.STONE,chargeTool,new ItemStack(Items.COBBLESTONE),0);blockDrops[0]=false;check(h,MiningRuntime.charge(chargeTool)==0,"Canceled drops must not run late modifier side effects");
    }
    private static void area(GameTestHelper h,ServerPlayer p,BlockPos pos){
        p.getInventory().clearContent();var tool=tool(100,Map.of(VEIN_MINER,1.0,CHARGE_BATTERY,1.0),ModifierScaling.B);p.setItemInHand(InteractionHand.MAIN_HAND,tool);p.setShiftKeyDown(true);
        var level=h.getLevel();for(int x=0;x<4;x++)level.setBlockAndUpdate(pos.east(x),Blocks.DIAMOND_ORE.defaultBlockState());
        boolean[] active={true};NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.level.block.BreakBlockEvent event)->{if(active[0]&&event.getPos().equals(pos.east(2)))event.setCanceled(true);});
        check(h,p.gameMode.destroyBlock(pos),"The actual primary break must succeed");active[0]=false;
        check(h,level.getBlockState(pos.east()).isAir()&&level.getBlockState(pos.east(2)).is(Blocks.DIAMOND_ORE),"Native extra breaks must work and respect a canceled neighboring break");
        check(h,PickaxeProgression.read(tool).xp()==1&&MiningRuntime.charge(tool)==1,"Real native loot snapshots must persist charge on the live tool while extra breaks grant no extra tool XP or charge");
        p.setShiftKeyDown(false);level.setBlockAndUpdate(pos,Blocks.DIAMOND_ORE.defaultBlockState());level.setBlockAndUpdate(pos.east(),Blocks.DIAMOND_ORE.defaultBlockState());p.gameMode.destroyBlock(pos);
        check(h,level.getBlockState(pos.east()).is(Blocks.DIAMOND_ORE),"Standing mining must remain single-block precise");
        p.setShiftKeyDown(true);level.setBlockAndUpdate(pos,Blocks.DIAMOND_ORE.defaultBlockState());p.setItemInHand(InteractionHand.MAIN_HAND,tool(0,Map.of(VEIN_MINER,1.0),ModifierScaling.S));p.gameMode.destroyBlock(pos);check(h,level.getBlockState(pos.east()).is(Blocks.DIAMOND_ORE),"Zero-quality milestones must never activate");p.setShiftKeyDown(false);
    }
    private static ItemStack tool(int quality,Map<PickaxeTrait,Double> traits,ModifierScaling grade){return ArtifactQuality.assemble(parts(ReconstructionPattern.PICKAXE,quality,traits,grade));}
    private static List<ItemStack> parts(ReconstructionPattern pattern,int quality,Map<PickaxeTrait,Double> traits,ModifierScaling grade){
        var result=new ArrayList<ItemStack>();for(int i=0;i<pattern.parts.size();i++){
            var part=new ItemStack(pattern.part(pattern.slots.get(i)));var root=new CompoundTag();var recovered=new CompoundTag();recovered.putInt("Quality",quality);recovered.putInt("Score",quality);recovered.putInt("Rarity",5);root.put(ArtifactQuality.RECOVERED,recovered);part.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
            var rolls=new ArrayList<ArtifactModifiers.Roll>();boolean shared=pattern.parts.get(i).contains("connector")||pattern.parts.get(i).contains("handle");
            for(var entry:traits.entrySet())if((entry.getKey().utility?i==pattern.parts.size()-2:i==0))rolls.add(new ArtifactModifiers.Roll(entry.getKey().id,entry.getValue(),quality/100.0,grade));
            set(part,rolls);result.add(part);
        }return result;
    }
    private static void set(ItemStack part,List<ArtifactModifiers.Roll> rolls){var root=part.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var record=new CompoundTag();record.putInt("Version",1);root.put(ArtifactModifiers.KEY,ArtifactModifiers.writeRolls(record,rolls));part.set(DataComponents.CUSTOM_DATA,CustomData.of(root));}
    private static BlockDropsEvent drop(GameTestHelper h,ServerPlayer p,BlockPos pos,Block block,ItemStack tool,ItemStack output,int xp){
        p.setItemInHand(InteractionHand.MAIN_HAND,tool);var drops=new ArrayList<ItemEntity>();drops.add(new ItemEntity(h.getLevel(),pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,output));
        var e=new BlockDropsEvent(h.getLevel(),pos,block.defaultBlockState(),null,drops,p,tool.copy());e.setDroppedExperience(xp);NeoForge.EVENT_BUS.post(e);return e;
    }
    private static void check(GameTestHelper h,boolean condition,String message){if(!condition)throw h.assertionException(message);}
    private ModifierExpansionTests(){}
}
