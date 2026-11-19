package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;

/** Tier 5 geometry, native construction at height, crafting, repair and previous-tier gating. */
public final class AnvilCanopyTests {
    private static Identifier id(String n){return Identifier.fromNamespaceAndPath(ExampleMod.MODID,n);}
    public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent e){var env=e.registerEnvironment(id("anvil_canopy_environment"));e.registerTest(id("anvil_canopy"),new Test(new TestData<>(env,Level.OVERWORLD,id("empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));}
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> d){super(d);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Grand Runic Canopy: connected geometry, recipe, all facings, native aerial construction and protection");}
        @Override public void run(GameTestHelper h){
            var l=h.getLevel();var core=h.absolutePos(new BlockPos(20,8,20));var stage=AnvilStages.all().get(2);
            check(h,stage.tier()==5&&stage.blocks().size()==172,"Tier 5 has 156 Runestone and 16 keystones");
            var offsets=new HashSet<BlockPos>();for(var req:stage.blocks())offsets.add(req.offset());
            check(h,offsets.stream().mapToInt(BlockPos::getX).min().orElseThrow()==-12&&offsets.stream().mapToInt(BlockPos::getX).max().orElseThrow()==12,"Canopy spans 25 blocks");
            check(h,offsets.stream().allMatch(p->p.getY()==16)&&!offsets.contains(new BlockPos(0,16,0)),"Canopy floats high above the anvil, leaving its center open in a single flat layer");
            for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)if(Math.abs(x)+Math.abs(z)<4)check(h,!offsets.contains(new BlockPos(x,16,z)),"The complete central diamond opening stays clear for Tier 6");
            for(int x=-4;x<=4;x++)check(h,offsets.contains(new BlockPos(x,16,-12))&&!offsets.contains(new BlockPos(x,16,-11)),"The straight octagonal paths stay one block wide");
            var reached=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();queue.add(offsets.iterator().next());reached.add(queue.peek());
            while(!queue.isEmpty()){var at=queue.removeFirst();for(var d:Direction.values()){var next=at.relative(d);if(offsets.contains(next)&&reached.add(next))queue.addLast(next);}}
            check(h,reached.equals(offsets),"Rim, diamond, spokes and inset nodes connect through actual faces");
            for(var at:offsets)check(h,offsets.contains(new BlockPos(-at.getZ(),at.getY(),at.getX())),"Aerial canopy is four-way symmetric");
            check(h,AnvilGuidePayload.snapshot().decode().equals(AnvilStages.all()),"Remote guide sync includes inset keystones and all Tier 5 positions");
            for(var front:Direction.Plane.HORIZONTAL){
                prepare(l,core,front);AnvilStageTests.fill(l,core,front,stage);refresh(l,core);tier(h,l,core,5,"Canopy forms Tier 5 in every facing");
                var base=core.below();l.setBlock(base,Blocks.STONE.defaultBlockState(),3);refresh(l,core);tier(h,l,core,0,"A canopy cannot bypass the original foundation");l.setBlock(base,AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
                var node=stage.blocks().stream().filter(r->r.tag().equals(AnvilStages.KEYSTONES)).findFirst().orElseThrow();l.setBlock(node.at(core,front),Blocks.IRON_BLOCK.defaultBlockState(),3);refresh(l,core);tier(h,l,core,4,"An ordinary cap cannot replace an amber keystone");
                l.destroyBlock(core,false);for(var req:stage.blocks())l.setBlock(req.at(core,front),Blocks.AIR.defaultBlockState(),3);
            }
            prepare(l,core,Direction.NORTH);refresh(l,core);tier(h,l,core,4,"Complete ground workshop unlocks the canopy");
            var player=h.makeMockServerPlayerInLevel();player.setGameMode(GameType.SURVIVAL);player.snapTo(core.getX()+.5,core.getY()+2.5,core.getZ()+.5,0,90);
            var hammer=new ItemStack(AncientAnvilContent.IRON_FORGE_HAMMER.get());player.setItemInHand(InteractionHand.MAIN_HAND,hammer);var be=(AncientAnvilBlockEntity)l.getBlockEntity(core);
            for(var slot:List.of(AnvilSlot.NORTH_WEST,AnvilSlot.NORTH_EAST,AnvilSlot.SOUTH_WEST,AnvilSlot.SOUTH_EAST))be.placeInput(slot,new ItemStack(AncientAnvilContent.RUNESTONE_ITEM.get()));
            for(var slot:List.of(AnvilSlot.NORTH_INNER,AnvilSlot.EAST_INNER,AnvilSlot.SOUTH_INNER,AnvilSlot.WEST_INNER))be.placeInput(slot,new ItemStack(AncientAnvilContent.INGOT.get()));
            be.placeInput(AnvilSlot.CENTER,new ItemStack(Items.GLOWSTONE_DUST));
            check(h,AnvilRecipes.match(be)!=null&&AnvilRecipes.match(be).anvilTier()==4,"Keystone recipe loads and requires Tier 4");
            for(int i=0;i<8;i++){be.lastStrike=l.getGameTime()-8;check(h,AnvilWork.strike(be,player,InteractionHand.MAIN_HAND),"Iron hammer crafts a keystone batch");}
            check(h,be.output().is(AncientAnvilContent.CANOPY_KEYSTONE_ITEM.get())&&be.output().getCount()==4&&be.inputCount()==0,"Four Runestone, four ancient ingots and glowstone produce four keystones");be.takeOutput();
            be.placeInput(AnvilSlot.CENTER,new ItemStack(Items.GOLD_INGOT));int damage=hammer.getDamageValue();player.setShiftKeyDown(true);
            for(int i=1;i<=3;i++)player.getInventory().setItem(i,new ItemStack(AncientAnvilContent.RUNESTONE_ITEM.get(),i==3?28:64));
            player.getInventory().setItem(7,new ItemStack(AncientAnvilContent.CANOPY_KEYSTONE_ITEM.get(),16));
            player.getInventory().setItem(8,new ItemStack(Items.WATER_BUCKET));player.getInventory().setItem(9,new ItemStack(Items.LAVA_BUCKET));
            check(h,AnvilGuide.next(l,core).tier()==5&&AnvilGuide.next(l,core).missing(l).size()==172,"Canopy guide targets only the new aerial tier");
            for(int i=0;i<172;i++){be.lastFoundationPlacement=l.getGameTime()-AnvilFoundation.INTERVAL;check(h,AnvilFoundation.placeNext(player,core),"Native inventory placement builds canopy block "+i);}
            tier(h,l,core,5,"Inventory building reaches Tier 5");check(h,AnvilGuide.next(l,core).targets().isEmpty(),"Tier 5 is the current maximum, with no Tier 6 preview");
            check(h,player.getInventory().countItem(AncientAnvilContent.RUNESTONE_ITEM.get())==0&&player.getInventory().countItem(AncientAnvilContent.CANOPY_KEYSTONE_ITEM.get())==0,"Native aerial construction charges exactly 156 Runestone and 16 keystones");
            check(h,be.inputCount()==1&&hammer.getDamageValue()==damage&&player.getInventory().getItem(8).is(Items.WATER_BUCKET)&&player.getInventory().getItem(9).is(Items.LAVA_BUCKET),"Aerial construction preserves workpieces, tools and filled buckets");
            var node=stage.blocks().stream().filter(r->r.tag().equals(AnvilStages.KEYSTONES)).findFirst().orElseThrow();var at=node.at(core,Direction.NORTH);
            l.setBlock(at,Blocks.CHEST.defaultBlockState(),3);var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)l.getBlockEntity(at);chest.setItem(0,new ItemStack(Items.DIAMOND,7));refresh(l,core);tier(h,l,core,4,"Missing keystone downgrades to Tier 4");
            check(h,AnvilGuide.next(l,core).missing(l).size()==1&&AnvilGuide.next(l,core).missing(l).getFirst().kind()==AnvilGuide.Kind.KEYSTONE,"Guide identifies the exact keystone repair");
            player.getInventory().setItem(7,new ItemStack(AncientAnvilContent.CANOPY_KEYSTONE_ITEM.get()));be.lastFoundationPlacement=l.getGameTime()-AnvilFoundation.INTERVAL;
            check(h,!AnvilFoundation.placeNext(player,core)&&chest.getItem(0).getCount()==7&&player.getInventory().getItem(7).getCount()==1,"Automatic repair preserves obstructing containers and materials");
            l.destroyBlock(at,false);be.lastFoundationPlacement=l.getGameTime()-AnvilFoundation.INTERVAL;check(h,AnvilFoundation.placeNext(player,core),"Repair places the missing keystone");tier(h,l,core,5,"Repair restores Tier 5");
            l.destroyBlock(core,false);check(h,stage.complete(l,core,Direction.NORTH),"Removing the anvil leaves the aerial structure intact");
            System.out.println("ANVIL_CANOPY_CHECK: PASS: 25-block connected symmetric canopy, 16-block elevation, four facings, Tier4 recipe and prerequisites, synced guide, native aerial placement/exact costs, container protection and repair");h.succeed();
        }
    }
    public static void prepare(net.minecraft.server.level.ServerLevel l,BlockPos core,Direction front){
        for(var req:AnvilStages.all().get(2).blocks())l.setBlock(req.at(core,front),Blocks.AIR.defaultBlockState(),3);
        for(var p:AncientAnvilStructure.foundation(core,front))l.setBlock(p,AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
        for(var p:AnvilBasins.frame(core,front))l.setBlock(p,AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
        for(var side:AnvilBasins.Side.values())for(var p:AnvilBasins.pool(core,front,side))l.setBlock(p,Blocks.WATER.defaultBlockState(),3);
        for(var stage:AnvilStages.all())if(stage.tier()<5)AnvilStageTests.fill(l,core,front,stage);
        l.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState().setValue(AncientAnvilBlock.FACING,front),3);refresh(l,core);
    }
    private static void refresh(net.minecraft.server.level.ServerLevel l,BlockPos p){AncientAnvilBlockEntity.refresh(l,p,l.getBlockState(p));}
    private static void tier(GameTestHelper h,Level l,BlockPos p,int t,String s){check(h,l.getBlockState(p).getValue(AncientAnvilBlock.TIER)==t,s);}
    private static void check(GameTestHelper h,boolean ok,String s){if(!ok)throw h.assertionException(s);}
    private AnvilCanopyTests(){}
}
