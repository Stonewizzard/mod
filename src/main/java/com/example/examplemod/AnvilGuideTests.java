package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;

public final class AnvilGuideTests {
    private static Identifier id(String s){return Identifier.fromNamespaceAndPath(ExampleMod.MODID,s);}
    public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent e){var env=e.registerEnvironment(id("anvil_guide_environment"));e.registerTest(id("anvil_guide_all_tiers"),new Test(new TestData<>(env,Level.OVERWORLD,id("empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));}
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> d){super(d);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Inventory construction through all four tiers and manual fluids");}
        @Override public void run(GameTestHelper h){
            var l=h.getLevel();var core=h.absolutePos(new BlockPos(20,6,20));var front=Direction.NORTH;
            check(h,AnvilGuidePayload.snapshot().decode().equals(AnvilStages.all()),"Remote-client layout sync preserves every position and tag");
            for(int x=-18;x<=18;x++)for(int z=-18;z<=18;z++)for(int y=-4;y<=4;y++)l.setBlock(core.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);
            l.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState(),3);AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));
            var player=h.makeMockServerPlayerInLevel();player.setGameMode(GameType.SURVIVAL);player.snapTo(core.getX()+.5,core.getY()+2.5,core.getZ()+.5,0,90);player.setShiftKeyDown(true);
            var hammer=new ItemStack(AncientAnvilContent.IRON_FORGE_HAMMER.get());player.setItemInHand(InteractionHand.MAIN_HAND,hammer);
            for(int i=1;i<=4;i++)player.getInventory().setItem(i,new ItemStack(AncientAnvilContent.RUNESTONE_ITEM.get(),i==4?56:64));
            player.getInventory().setItem(5,new ItemStack(Items.DIAMOND_BLOCK,20));player.getInventory().setItem(6,new ItemStack(Items.WATER_BUCKET));player.getInventory().setItem(7,new ItemStack(Items.LAVA_BUCKET));
            var be=(AncientAnvilBlockEntity)l.getBlockEntity(core);be.placeInput(AnvilSlot.CENTER,new ItemStack(Items.GOLD_INGOT));
            check(h,AnvilGuide.next(l,core).tier()==1&&AnvilGuide.next(l,core).missing(l).size()==25,"Tier 0 shows foundation");
            build(h,player,core,25);check(h,l.getBlockState(core).getValue(AncientAnvilBlock.TIER)==1,"Foundation builds Tier 1");
            var second=AnvilGuide.next(l,core);check(h,second.tier()==2&&second.targets().size()==60&&second.targets().stream().filter(t->t.kind()==AnvilGuide.Kind.FLUID).count()==33,"Tier 2 guides frame and fluid spaces");
            build(h,player,core,27);
            be.lastFoundationPlacement=l.getGameTime()-AnvilFoundation.INTERVAL;check(h,!AnvilFoundation.placeNext(player,core)&&AnvilGuide.next(l,core).missing(l).size()==33,"Builder waits for manual fluids");
            check(h,player.getInventory().getItem(6).is(Items.WATER_BUCKET)&&player.getInventory().getItem(7).is(Items.LAVA_BUCKET),"Filled buckets are never consumed or used");
            for(var side:AnvilBasins.Side.values())for(var p:AnvilBasins.pool(core,front,side))l.setBlock(p,Blocks.WATER.defaultBlockState(),3);
            AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));check(h,AnvilGuide.next(l,core).tier()==3&&AnvilGuide.next(l,core).missing(l).size()==88,"Manual fluids unlock Tier 3 guide");
            build(h,player,core,88);check(h,l.getBlockState(core).getValue(AncientAnvilBlock.TIER)==3&&AnvilGuide.next(l,core).missing(l).size()==128,"Tier 3 places Runestone and eligible diamond caps, then shows Tier 4");
            build(h,player,core,128);check(h,l.getBlockState(core).getValue(AncientAnvilBlock.TIER)==4&&AnvilGuide.next(l,core).targets().isEmpty(),"Tier 4 completes without extra guide positions");
            check(h,player.getInventory().countItem(AncientAnvilContent.RUNESTONE_ITEM.get())==0&&player.getInventory().getItem(5).isEmpty(),"Exact inventory cost: 248 Runestone and 20 caps");
            check(h,player.getMainHandItem()==hammer&&hammer.getDamageValue()==0&&be.inputCount()==1,"Construction preserves held tools and workpieces");
            var cap=AnvilStages.all().get(1).blocks().stream().filter(r->r.tag().equals(AnvilStages.CAPS)).findFirst().orElseThrow();l.setBlock(cap.at(core,front),Blocks.AIR.defaultBlockState(),3);AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));check(h,AnvilGuide.next(l,core).tier()==4&&AnvilGuide.next(l,core).missing(l).size()==1,"Breaking a higher-tier block shows only the missing repair");
            System.out.println("ANVIL_GUIDE_ALL_TIERS_CHECK: PASS: sequential plans, native placement, exact Runestone/cap costs, manual-only fluids and unchanged buckets, sync roundtrip, held tool/workpiece preservation and repairs");h.succeed();
        }
    }
    private static void build(GameTestHelper h,net.minecraft.server.level.ServerPlayer player,BlockPos core,int count){var be=(AncientAnvilBlockEntity)player.level().getBlockEntity(core);for(int i=0;i<count;i++){be.lastFoundationPlacement=player.level().getGameTime()-AnvilFoundation.INTERVAL;check(h,AnvilFoundation.placeNext(player,core),"One native placement succeeds at construction step "+i);}}
    private static void check(GameTestHelper h,boolean ok,String message){if(!ok)throw h.assertionException(message);}
    private AnvilGuideTests(){}
}
