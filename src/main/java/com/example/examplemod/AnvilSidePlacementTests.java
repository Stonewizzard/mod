package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;

/** Ordinary native item placement around saved anvils, including their formerly reserved cells. */
public final class AnvilSidePlacementTests {
    private static Identifier id(String n){return Identifier.fromNamespaceAndPath(ExampleMod.MODID,n);}
    public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent e){var env=e.registerEnvironment(id("anvil_side_placement_environment"));e.registerTest(id("anvil_side_placement"),new Test(new TestData<>(env,Level.OVERWORLD,id("empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));}
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> d){super(d);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Saved anvil side migration and native inventory block placement");}
        @Override public void run(GameTestHelper h){
            var l=h.getLevel();var core=h.absolutePos(new BlockPos(3,2,3));
            saved(l,core,Direction.NORTH);
            h.runAtTickTime(12,()->{
                for(var c:AncientAnvilStructure.CELLS)if(!AncientAnvilStructure.OCCUPIED.contains(c))check(h,l.getBlockState(c.at(core,Direction.NORTH)).isAir(),"Normal ticking releases saved invisible cells without replacing the anvil");
                l.destroyBlock(core,false);
                var player=h.makeMockServerPlayerInLevel();player.setGameMode(GameType.SURVIVAL);player.snapTo(core.getX()+6,core.getY()+2,core.getZ()+6);
                for(var front:Direction.Plane.HORIZONTAL){
                    saved(l,core,front);AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));
                    var stack=new ItemStack(AncientAnvilContent.RUNESTONE_ITEM.get(),64);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                    var placed=new ArrayList<BlockPos>();
                    for(int side:new int[]{-2,2})for(int z=-2;z<=2;z++)for(int y=0;y<=1;y++){
                        var at=core.relative(front.getClockWise(),side).relative(front.getOpposite(),z).above(y);
                        place(h,player,at,stack);placed.add(at);AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));
                        check(h,l.getBlockState(core).getValue(AncientAnvilBlock.EXPANDED),"Native side placement must not collapse or shrink the anvil");
                    }
                    // Lower front cells are empty; the real horn above them retains its owned cells.
                    for(int x=-1;x<=1;x++){
                        var at=core.relative(front,2).relative(front.getClockWise(),x);
                        place(h,player,at,stack);placed.add(at);
                    }
                    check(h,stack.getCount()==41,"Twenty-three normal placements consume exactly twenty-three inventory blocks");
                    for(var c:AncientAnvilStructure.OCCUPIED)if(c.z()==-2&&c.y()==1)check(h,AncientAnvilStructure.belongs(l,c.at(core,front),core,front,c.index()),"The visible horn retains ownership and collision");
                    l.destroyBlock(core,false);for(var at:placed){check(h,l.getBlockState(at).is(AncientAnvilContent.RUNESTONE.get()),"Removing the anvil preserves normally placed side/front blocks");l.setBlock(at,Blocks.AIR.defaultBlockState(),3);}
                }
                System.out.println("ANVIL_SIDE_PLACEMENT_CHECK: PASS: automatic saved-cell migration, four orientations, native left/right two-high walls and lower-front placement, exact item costs, intact horn and neighboring blocks");h.succeed();
            });
        }
    }
    private static void saved(net.minecraft.server.level.ServerLevel l,BlockPos core,Direction front){
        for(var p:AncientAnvilStructure.foundation(core,front))l.setBlock(p,AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
        l.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState().setValue(AncientAnvilBlock.FACING,front),3);AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));
        for(var c:AncientAnvilStructure.CELLS)if(!AncientAnvilStructure.OCCUPIED.contains(c))l.setBlock(c.at(core,front),AncientAnvilContent.PART.get().defaultBlockState().setValue(AncientAnvilPartBlock.FACING,front).setValue(AncientAnvilPartBlock.CELL,c.index()).setValue(AncientAnvilPartBlock.EXPANDED,true),3);
    }
    private static void place(GameTestHelper h,net.minecraft.server.level.ServerPlayer player,BlockPos at,ItemStack stack){
        var hit=new BlockHitResult(Vec3.atCenterOf(at.below()).add(0,.5,0),Direction.UP,at.below(),false);
        var beforeState=player.level().getBlockState(at);int before=stack.getCount();var result=stack.useOn(new UseOnContext(player.level(),player,InteractionHand.MAIN_HAND,stack,hit));
        check(h,player.level().getBlockState(at).is(AncientAnvilContent.RUNESTONE.get())&&stack.getCount()==before-1,"Ordinary native item placement at "+at+", result="+result+", before="+beforeState+", after="+player.level().getBlockState(at)+", count="+before+"->"+stack.getCount()+", entities="+player.level().getEntities(null,new AABB(at)).stream().map(e->e.getType()+":"+e.getBoundingBox()).toList());
    }
    private static void check(GameTestHelper h,boolean ok,String why){if(!ok)throw h.assertionException(why);}
    private AnvilSidePlacementTests(){}
}
