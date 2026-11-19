package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.ArrayDeque;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;

/** Actual datapack stages in a live server world, not a separate copy of their coordinates. */
public final class AnvilStageTests {
    private static Identifier id(String s){return Identifier.fromNamespaceAndPath(ExampleMod.MODID,s);}
    public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent e){var env=e.registerEnvironment(id("anvil_stage_environment"));e.registerTest(id("anvil_stages"),new Test(new TestData<>(env,Level.OVERWORLD,id("empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));}
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> d){super(d);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Anvil tiers 3/4, symmetric pillars, materials, downgrade and preservation");}
        @Override public void run(GameTestHelper h){
            var l=h.getLevel();var core=h.absolutePos(new BlockPos(20,6,20));
            check(h,AnvilStages.all().size()==3,"All three datapack workshop stages load");
            var t3=AnvilStages.all().get(0);var t4=AnvilStages.all().get(1);
            check(h,t3.tier()==3&&t3.blocks().size()==88&&t4.tier()==4&&t4.blocks().size()==244,"Reference counts: 72+16 and 240+4");
            var offsets=t4.blocks().stream().map(AnvilStages.Requirement::offset).collect(java.util.stream.Collectors.toSet());
            for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)if(Math.max(Math.abs(x),Math.abs(z))==8){
                check(h,offsets.contains(new BlockPos(x,-3,z))&&!offsets.contains(new BlockPos(x,-2,z)),"Main ring is one block below its previous height");
            }
            for(var dir:Direction.Plane.HORIZONTAL){
                int x=dir.getStepX()*16,z=dir.getStepZ()*16;
                for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)check(h,offsets.contains(new BlockPos(x+dx,-4,z+dz)),"Each pillar base is two blocks below its previous height");
                for(int y=-3;y<=3;y++)check(h,offsets.contains(new BlockPos(x,y,z)),"Pillar shaft connects the lowered base to the unchanged cap");
                for(int dx:new int[]{-1,1})check(h,!offsets.contains(new BlockPos(x+dx,-2,z)),"The previous high pillar-base ring is no longer required");
            }
            var outline=offsets.stream().filter(p->p.getY()==-4).collect(java.util.stream.Collectors.toSet());
            var reached=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();queue.add(new BlockPos(16,-4,0));reached.add(queue.peek());
            while(!queue.isEmpty()){var next=queue.removeFirst();for(var side:Direction.Plane.HORIZONTAL){var neighbor=next.relative(side);if(outline.contains(neighbor)&&reached.add(neighbor))queue.addLast(neighbor);}}
            check(h,reached.equals(outline),"Circular outline connects all four bases by block faces");
            check(h,!outline.contains(new BlockPos(0,-4,0))&&!outline.contains(new BlockPos(8,-4,0)),"Outer circle remains hollow");
            check(h,outline.contains(new BlockPos(11,-4,12))&&outline.contains(new BlockPos(-12,-4,11)),"The outer requirement follows circular arcs between pillars");
            for(var facing:Direction.Plane.HORIZONTAL){
                for(var stage:AnvilStages.all())for(var req:stage.blocks())l.setBlock(req.at(core,facing),Blocks.AIR.defaultBlockState(),3);
                for(var p:AncientAnvilStructure.foundation(core,facing))l.setBlock(p,AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
                for(var p:AnvilBasins.frame(core,facing))l.setBlock(p,AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
                for(var side:AnvilBasins.Side.values())for(var p:AnvilBasins.pool(core,facing,side))l.setBlock(p,Blocks.WATER.defaultBlockState(),3);
                l.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState().setValue(AncientAnvilBlock.FACING,facing),3);
                refresh(l,core);tier(h,l,core,2,"Tier 2 remains valid without higher structures");
                fill(l,core,facing,t4);refresh(l,core);tier(h,l,core,2,"Tier 4 cannot skip Tier 3");
                fill(l,core,facing,t3);refresh(l,core);tier(h,l,core,4,"Both symmetric stages form Tier 4 in every facing");
                for(var req:t3.blocks())check(h,req.matches(l,core,facing),"Every Tier 3 position matches");
                for(var req:t4.blocks())check(h,req.matches(l,core,facing),"Every Tier 4 position matches");
                // Every cap material is interchangeable; one cap can mix eligible blocks.
                var cap=t3.blocks().stream().filter(r->r.tag().equals(AnvilStages.CAPS)).findFirst().orElseThrow();
                for(var block:java.util.List.of(Blocks.IRON_BLOCK,Blocks.GOLD_BLOCK,Blocks.DIAMOND_BLOCK,Blocks.EMERALD_BLOCK,Blocks.NETHERITE_BLOCK)){
                    l.setBlock(cap.at(core,facing),block.defaultBlockState(),3);refresh(l,core);tier(h,l,core,4,"Interchangeable caps preserve the tier");
                }
                l.setBlock(cap.at(core,facing),Blocks.STONE.defaultBlockState(),3);refresh(l,core);tier(h,l,core,2,"Invalid Tier 3 cap blocks all higher tiers");l.setBlock(cap.at(core,facing),Blocks.DIAMOND_BLOCK.defaultBlockState(),3);
                var arc=core.relative(facing.getClockWise(),11).relative(facing.getOpposite(),12).below(4);
                l.setBlock(arc,Blocks.AIR.defaultBlockState(),3);refresh(l,core);tier(h,l,core,3,"Breaking a circular arc downgrades Tier 4");check(h,AnvilGuide.next(l,core).missing(l).size()==1,"Guide highlights the missing arc for repair");l.setBlock(arc,AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);refresh(l,core);tier(h,l,core,4,"Repairing the circular arc restores Tier 4");
                // Each side's outer pillar is mandatory.
                for(var req:t4.blocks().stream().filter(r->r.tag().equals(AnvilStages.CAPS)).toList()){
                    var p=req.at(core,facing);l.setBlock(p,Blocks.AIR.defaultBlockState(),3);refresh(l,core);tier(h,l,core,3,"Each of four outer pillars is required");l.setBlock(p,Blocks.IRON_BLOCK.defaultBlockState(),3);
                }
                var ring=t4.blocks().getFirst();var at=ring.at(core,facing);l.setBlock(at,Blocks.CHEST.defaultBlockState(),3);
                var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)l.getBlockEntity(at);chest.setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND,7));refresh(l,core);tier(h,l,core,3,"Wrong ring block downgrades, is never overwritten");check(h,chest.getItem(0).getCount()==7,"Detection preserves containers");
                l.destroyBlock(at,false);fill(l,core,facing,t4);refresh(l,core);tier(h,l,core,4,"Repair restores Tier 4");
                var pool=AnvilBasins.pool(core,facing,AnvilBasins.Side.FRONT).getFirst();l.setBlock(pool,Blocks.STONE.defaultBlockState(),3);refresh(l,core);tier(h,l,core,1,"All higher stages still require complete fluid basins");l.setBlock(pool,Blocks.WATER.defaultBlockState(),3);
                l.setBlock(core.below(),Blocks.STONE.defaultBlockState(),3);refresh(l,core);tier(h,l,core,0,"All higher stages still require Tier 1");l.setBlock(core.below(),AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
                refresh(l,core);tier(h,l,core,4,"Restored base restores highest complete tier");
                l.destroyBlock(core,false);for(var req:t3.blocks())check(h,req.matches(l,core,facing),"Anvil removal preserves corner pillars");for(var req:t4.blocks())check(h,req.matches(l,core,facing),"Anvil removal preserves ring and side pillars");
                for(var stage:AnvilStages.all())for(var req:stage.blocks())l.setBlock(req.at(core,facing),Blocks.AIR.defaultBlockState(),3);
            }
            System.out.println("ANVIL_STAGES_CHECK: PASS: all facings, additive tiers, all four outer pillars, interchangeable caps, fluid/base prerequisites, live downgrade/repair, container and structure preservation");h.succeed();
        }
    }
    public static void fill(net.minecraft.server.level.ServerLevel l,BlockPos core,Direction facing,AnvilStages.Stage stage){for(var req:stage.blocks())l.setBlock(req.at(core,facing),(req.tag().equals(AnvilStages.KEYSTONES)?AncientAnvilContent.CANOPY_KEYSTONE.get():req.tag().equals(AnvilStages.CAPS)?Blocks.IRON_BLOCK:AncientAnvilContent.RUNESTONE.get()).defaultBlockState(),3);}
    private static void refresh(net.minecraft.server.level.ServerLevel l,BlockPos p){AncientAnvilBlockEntity.refresh(l,p,l.getBlockState(p));}
    private static void tier(GameTestHelper h,Level l,BlockPos p,int expected,String message){check(h,l.getBlockState(p).getValue(AncientAnvilBlock.TIER)==expected,message);}
    private static void check(GameTestHelper h,boolean ok,String message){if(!ok)throw h.assertionException(message);}
    private AnvilStageTests(){}
}
