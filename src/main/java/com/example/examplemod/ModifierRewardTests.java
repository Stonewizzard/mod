package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import static com.example.examplemod.PickaxeTrait.*;

/** Native reward benchmarks and read-only appraisal/inventory regressions. */
public final class ModifierRewardTests {
    public static void register(RegisterGameTestsEvent event){
        var env=event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"modifier_rewards_environment"));
        for(int mode=0;mode<3;mode++)event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"modifier_rewards_"+mode),new Test(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),300,0,true,Rotation.NONE,false,1,1,false,0),mode));
    }
    private static final class Test extends GameTestInstance {
        final int mode;Test(TestData<Holder<TestEnvironmentDefinition<?>>> data,int mode){super(data);this.mode=mode;}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Rewarding builds and read-only Appraisal Table");}
        @Override public void run(GameTestHelper h){if(mode==0)balance(h);else if(mode==1)appraisal(h);else runtime(h);System.out.println("MODIFIER_REWARDS_CHECK_"+mode+": PASS");h.succeed();}
    }
    private static ItemStack tool(int quality,Map<PickaxeTrait,Double> traits){return ModifierExpansionTests.tool(quality,traits,ModifierScaling.S);}
    private static void balance(GameTestHelper h){
        var player=h.makeMockServerPlayerInLevel();player.setGameMode(GameType.SURVIVAL);player.setOnGround(true);var pos=h.absolutePos(new BlockPos(3,1,3));
        var nativePick=new ItemStack(Items.NETHERITE_PICKAXE);var lookup=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var efficiency=lookup.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY);nativePick.enchant(efficiency,5);
        // Native efficiency contributes level^2+1 to a correct-tool mining speed above one.
        double vanilla=nativePick.getDestroySpeed(Blocks.STONE.defaultBlockState())+Math.pow(net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(efficiency,nativePick),2)+1;
        check(h,vanilla==35,"Benchmark must use the real native Netherite item and Efficiency V enchantment");
        for(int quality:new int[]{0,50,100,120}){
            var item=tool(quality,Map.of(SPEED,.5));var saved=item.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY).copy();player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,item);
            var event=new PlayerEvent.BreakSpeed(player,Blocks.STONE.defaultBlockState(),item.getDestroySpeed(Blocks.STONE.defaultBlockState()),pos);PickaxeEvents.speed(event);
            double expected=8*(1+2*quality/100.0)*(1+2*quality/100.0);
            check(h,Math.abs(event.getNewSpeed()-expected)<.001,"Quality and base modifier power must remain independent and predictable");
            if(quality==100)check(h,event.getNewSpeed()>vanilla*2,"An attainable unenchanted 100%-quality speed build must beat Efficiency V Netherite by over twofold");
            AppraisalReport.read(item);PickaxeTooltips.refresh(item);check(h,item.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY).equals(saved),"Reward refresh and appraisal must not rewrite any recovered raw roll or source record");
            check(h,PickaxeTooltips.tool(item).size()<=5,"The actual item tooltip must stay compact");
        }
        var bare=tool(100,Map.of());bare.enchant(efficiency,5);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,bare);var boosted=new PlayerEvent.BreakSpeed(player,Blocks.STONE.defaultBlockState(),bare.getDestroySpeed(Blocks.STONE.defaultBlockState())+26,pos);PickaxeEvents.speed(boosted);
        check(h,boosted.getNewSpeed()==102,"The quality core must multiply, rather than replace, native Efficiency speed");
        for(var trait:PickaxeTrait.values())check(h,!ModifierDescriptions.description(trait).isBlank()&&trait.power>=1&&trait.cap>0,"All 120 modifiers require a reviewed effect and inspection description");
        var fortune=tool(100,Map.of(FORTUNE,2.0));fortune.enchant(lookup.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE),3);int advanced=0,normal=0;
        var p=h.absolutePos(new BlockPos(3,1,5));var reference=new ItemStack(Items.NETHERITE_PICKAXE);reference.enchant(lookup.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE),3);
        for(int i=0;i<160;i++){
            normal+=Block.getDrops(Blocks.DIAMOND_ORE.defaultBlockState(),h.getLevel(),p,null,player,reference).stream().mapToInt(ItemStack::getCount).sum();
            var drops=new ArrayList<net.minecraft.world.entity.item.ItemEntity>();drops.add(new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),p.getX(),p.getY(),p.getZ(),new ItemStack(Items.DIAMOND)));
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,fortune);var event=new BlockDropsEvent(h.getLevel(),p,Blocks.DIAMOND_ORE.defaultBlockState(),null,drops,player,fortune);PickaxeEvents.drops(event);advanced+=drops.stream().mapToInt(e->e.getItem().getCount()).sum();
        }
        check(h,advanced>normal*1.5,"Fortune modifiers must deliver substantially more native loot than Fortune III on the same ore");
        System.out.println("REWARD_BENCHMARK: unenchanted speed build=72; Efficiency V restored core=102; Efficiency V Netherite=35; Fortune modified="+advanced+", vanilla III="+normal);
    }
    private static void appraisal(GameTestHelper h){
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(3,1,3));var p=h.makeMockServerPlayerInLevel();p.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);level.setBlockAndUpdate(pos,AppraisalContent.BLOCK.get().defaultBlockState());
        var table=(AppraisalTableBlockEntity)level.getBlockEntity(pos);var menu=new AppraisalMenu(81,p.getInventory(),table);p.containerMenu=menu;
        check(h,menu.slots.size()==37&&menu.getSlot(0).getMaxStackSize()==1&&!menu.getSlot(0).mayPlace(new ItemStack(Items.DIRT)),"Appraisal needs one restricted input and a normal player inventory");
        for(var sample:List.of(tool(100,Map.of(SPEED,.3,VEIN_MINER,1.0)),ComponentWorkTests.component(ArtifactProfile.Find.PICKAXE_LEFT,80,5,91),ArchaeologyContent.specimen(5,4),ComponentWorkTests.ancient(ArtifactProfile.Find.PICKAXE_LEFT,3))){
            p.getInventory().clearContent();var before=sample.copy();p.getInventory().setItem(9,sample);check(h,!menu.quickMoveStack(p,1).isEmpty()&&ItemStack.matches(before,table.getItem(0)),"Inspection shift-click must preserve the exact input");
            var report=AppraisalReport.read(table.getItem(0));check(h,!report.entries().isEmpty()&&ItemStack.matches(before,table.getItem(0)),"Repeated appraisal must never roll, unveil or otherwise mutate an item");
            var data=table.saveWithFullMetadata(level.registryAccess());var restored=net.minecraft.world.level.block.entity.BlockEntity.loadStatic(pos,table.getBlockState(),data,level.registryAccess());check(h,restored instanceof AppraisalTableBlockEntity t&&ItemStack.matches(before,t.getItem(0)),"Full inspection input must survive native block saves");
            if(ArchaeologyContent.artifacts().stream().anyMatch(before::is))check(h,report.entries().size()==1,"Raw specimens must not expose hidden rewards or modifiers");
            check(h,!menu.quickMoveStack(p,0).isEmpty()&&table.getItem(0).isEmpty(),"The inspected item must return normally without consuming or changing it");
        }
        for(var trait:PickaxeTrait.values()){
            var single=ComponentWorkTests.component(trait.utility?ArtifactProfile.Find.TOOL_HANDLE:ArtifactProfile.Find.PICKAXE_LEFT,50,5,71);var root=single.get(DataComponents.CUSTOM_DATA).copyTag();var record=root.getCompoundOrEmpty(ArtifactModifiers.KEY);
            var band=ArtifactModifierCatalog.spec(trait).tiers().getLast();root.put(ArtifactModifiers.KEY,ArtifactModifiers.writeRolls(record,List.of(new ArtifactModifiers.Roll(trait.id,band.maximum(),.5,ModifierScaling.S,band.id(),band.rank()))));single.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
            var saved=single.copy();var detail=AppraisalReport.read(single);
            check(h,detail.entries().size()==2&&detail.entries().get(1).lines().contains(ModifierDescriptions.description(trait))&&ItemStack.matches(saved,single),"Every one of the 120 modifier descriptions must be reachable without modifying its component");
        }
        p.snapTo(pos.getX()+30,pos.getY(),pos.getZ());check(h,!menu.stillValid(p),"Appraisal must use the native table distance check");p.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        var part=ComponentWorkTests.component(ArtifactProfile.Find.PICKAXE_LEFT,100,5,33);check(h,ComponentWork.apply(ComponentStationKind.INFLUENCER,part,0,p.getRandom()),"Test influence must assign normally");
        check(h,!part.has(DataComponents.CUSTOM_MODEL_DATA)&&part.getHoverName().getStyle().getColor().getValue()==ModifierInfluence.NETHER.color,"Nether influence must color the name without changing icon data");
        check(h,PickaxeTooltips.piece(part).size()<=3&&PickaxeTooltips.piece(part).stream().noneMatch(c->c.getString().contains("Uninfluenced")||c.getString().contains("influence")),"Influence must not add tooltip clutter");
        var end=ComponentWorkTests.component(ArtifactProfile.Find.PICKAXE_LEFT,100,5,34);end.set(DataComponents.CUSTOM_NAME,Component.literal("My heirloom"));check(h,ComponentWork.apply(ComponentStationKind.INFLUENCER,end,1,p.getRandom())&&end.getHoverName().getString().equals("My heirloom")&&!end.has(DataComponents.CUSTOM_MODEL_DATA),"End influence must preserve player custom names and icon data");
        table.setItem(0,part.copy());var bounds=new net.minecraft.world.phys.AABB(pos).inflate(2);level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,bounds).forEach(net.minecraft.world.entity.item.ItemEntity::discard);level.destroyBlock(pos,true);
        var drops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,bounds);check(h,drops.stream().mapToInt(e->e.getItem().getCount()).sum()==2&&drops.stream().anyMatch(e->ItemStack.matches(part,e.getItem())),"Breaking the Appraisal Table returns its block and exact input once");
    }
    private static void runtime(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();var pos=h.absolutePos(new BlockPos(3,1,3));p.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);var item=tool(100,Map.of(SOUL_FURNACE,1.0));p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,item);
        var drops=new ArrayList<net.minecraft.world.entity.item.ItemEntity>();drops.add(new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),pos.getX(),pos.getY(),pos.getZ(),new ItemStack(Items.RAW_IRON,4)));int xp=p.totalExperience;
        var event=new BlockDropsEvent(h.getLevel(),pos,Blocks.IRON_ORE.defaultBlockState(),null,drops,p,item);PickaxeEvents.drops(event);
        check(h,drops.stream().allMatch(e->e.getItem().is(Items.IRON_INGOT))&&p.totalExperience==xp&&MiningRuntime.charge(item)==0,"Soul Furnace must work alone using the innate core without consuming player XP");
        var low=tool(100,Map.of(RESTORATIVE_DISCHARGE,1.0));low.setDamageValue(100);MiningRuntime.addCharge(low,3);var c=new MiningContext(new BlockDropsEvent(h.getLevel(),pos,Blocks.STONE.defaultBlockState(),null,new ArrayList<>(),p,low),p);MiningChargeEffects.prepare(c);MiningChargeEffects.breaks(c);
        check(h,low.getDamageValue()==60,"An isolated charge spender must work from core generation and give its full repair reward");
        var anchor=tool(100,Map.of(VOID_ANCHOR,1.0));var target=p.position();levelFloor(h,target);MiningRuntime.edit(anchor,t->{t.putString("SafeDimension",h.getLevel().dimension().identifier().toString());t.putDouble("SafeX",target.x);t.putDouble("SafeY",target.y);t.putDouble("SafeZ",target.z);});p.setOnGround(false);p.snapTo(target.x,h.getLevel().getMinY()-3,target.z);MiningStatusEffects.tick(p,anchor);
        check(h,p.position().distanceToSqr(target)<.01&&anchor.getDamageValue()>0,"Void Anchor must have a useful emergency durability fallback without another modifier");
    }
    private static void levelFloor(GameTestHelper h,net.minecraft.world.phys.Vec3 p){var pos=BlockPos.containing(p);h.getLevel().setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());h.getLevel().setBlockAndUpdate(pos.above(),Blocks.AIR.defaultBlockState());}
    private static void check(GameTestHelper h,boolean value,String message){if(!value)throw h.assertionException(message);}
    private ModifierRewardTests(){}
}
