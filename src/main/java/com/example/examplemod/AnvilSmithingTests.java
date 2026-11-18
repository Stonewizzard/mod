package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;

/** Actual world storage, reload, interactions and a timed hammer craft. */
public final class AnvilSmithingTests {
    public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent e){var env=e.registerEnvironment(id("anvil_smithing_environment"));e.registerTest(id("anvil_smithing"),new Test(new TestData<>(env,Level.OVERWORLD,id("empty_drop_test"),150,0,true,Rotation.NONE,false,1,1,false,0)));}
    private static Identifier id(String n){return Identifier.fromNamespaceAndPath(ExampleMod.MODID,n);}
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> d){super(d);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Physical anvil smithing: thirteen spots, exact ingredients, saved work, hammer cooldown, result and recovery");}
        @Override public void run(GameTestHelper h){
            var l=h.getLevel();var core=h.absolutePos(new BlockPos(3,2,3));var player=h.makeMockServerPlayerInLevel();player.setGameMode(GameType.SURVIVAL);player.snapTo(core.getX()+6,core.getY()+2,core.getZ()+6);
            for(var facing:Direction.Plane.HORIZONTAL){
                l.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState().setValue(AncientAnvilBlock.FACING,facing),3);AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));
                var be=(AncientAnvilBlockEntity)l.getBlockEntity(core);
                for(var slot:AnvilSlot.values()){
                    var at=slot.world(core,facing);check(h,AnvilSlot.nearest(core,facing,at)==slot,"Every position maps correctly in all four facings");
                    var hit=new BlockHitResult(at,Direction.UP,BlockPos.containing(at),false);player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND,2));
                    AnvilWork.interact(l,core,player,InteractionHand.MAIN_HAND,hit);check(h,be.input(slot).is(Items.DIAMOND)&&player.getMainHandItem().getCount()==1,"Placing one workpiece consumes exactly one");
                    player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.EMERALD));AnvilWork.interact(l,core,player,InteractionHand.MAIN_HAND,hit);check(h,be.input(slot).is(Items.EMERALD),"Swapping a workpiece replaces the intended position");
                    player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);AnvilWork.interact(l,core,player,InteractionHand.MAIN_HAND,hit);check(h,be.input(slot).isEmpty(),"An empty hand retrieves every position");
                }
                l.destroyBlock(core,false);
            }
            l.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState(),3);AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));var be=(AncientAnvilBlockEntity)l.getBlockEntity(core);
            var hammer=new ItemStack(AncientAnvilContent.IRON_FORGE_HAMMER.get());player.setItemInHand(InteractionHand.MAIN_HAND,hammer);
            check(h,!AnvilWork.strike(be,player,InteractionHand.MAIN_HAND)&&hammer.getDamageValue()==0,"Invalid recipes never damage the hammer");
            for(var slot:List.of(AnvilSlot.NORTH_WEST,AnvilSlot.NORTH_EAST,AnvilSlot.SOUTH_WEST,AnvilSlot.SOUTH_EAST))be.placeInput(slot,new ItemStack(slot==AnvilSlot.NORTH_WEST?Items.DEEPSLATE:Items.COBBLED_DEEPSLATE));
            be.placeInput(AnvilSlot.CENTER,new ItemStack(Items.GOLD_INGOT));
            check(h,l.getBlockState(core).getValue(AncientAnvilBlock.TIER)==0&&AnvilRecipes.match(be)!=null,"The datapack recipe loads, accepts both deepslates and works at tier zero");
            be.placeInput(AnvilSlot.NORTH_INNER,new ItemStack(Items.STICK));check(h,!AnvilWork.strike(be,player,InteractionHand.MAIN_HAND)&&be.inputCount()==6,"Extra ingredients block a recipe without losing anything");be.takeInput(AnvilSlot.NORTH_INNER);
            check(h,AnvilWork.strike(be,player,InteractionHand.MAIN_HAND)&&be.work()==1&&be.output().isEmpty(),"The first hammer hit starts work without consuming the center ingredient");
            check(h,!AnvilWork.strike(be,player,InteractionHand.MAIN_HAND)&&be.work()==1&&hammer.getDamageValue()==1,"Repeated packets in the same tick cannot craft faster or damage twice");
            var saved=be.saveWithFullMetadata(l.registryAccess());var restored=BlockEntity.loadStatic(core,be.getBlockState(),saved,l.registryAccess());check(h,restored instanceof AncientAnvilBlockEntity loaded&&loaded.work()==1&&loaded.inputCount()==5&&loaded.input(AnvilSlot.CENTER).is(Items.GOLD_INGOT),"All positions, center ingredient and partial forging work survive reload");
            h.runAtTickTime(12,()->{check(h,AnvilWork.strike(be,player,InteractionHand.MAIN_HAND)&&be.work()==2,"Second accepted strike advances work");});
            h.runAtTickTime(24,()->{check(h,AnvilWork.strike(be,player,InteractionHand.MAIN_HAND)&&be.work()==3,"Third accepted strike advances work");});
            h.runAtTickTime(36,()->{
                check(h,AnvilWork.strike(be,player,InteractionHand.MAIN_HAND)&&be.output().is(AncientAnvilContent.RUNESTONE.get().asItem())&&be.inputCount()==0&&hammer.getDamageValue()==4,"Four strikes consume exactly the five ingredients and show one Runestone at center");
                check(h,!AnvilWork.strike(be,player,InteractionHand.MAIN_HAND),"Uncollected output prevents duplicate crafting");
                var loaded=BlockEntity.loadStatic(core,be.getBlockState(),be.saveWithFullMetadata(l.registryAccess()),l.registryAccess());check(h,loaded instanceof AncientAnvilBlockEntity stored&&stored.output().is(AncientAnvilContent.RUNESTONE_ITEM.get()),"Completed output survives reload");
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.GOLD_INGOT,3));var hit=new BlockHitResult(AnvilSlot.CENTER.world(core,Direction.NORTH),Direction.UP,core.above(),false);
                AnvilWork.interact(l,core,player,InteractionHand.MAIN_HAND,hit);check(h,be.output().isEmpty()&&be.input(AnvilSlot.CENTER).isEmpty()&&player.getMainHandItem().getCount()==3,"Picking up output while holding ingredients cannot overwrite it or consume held items");
                int runeCount=0;for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(AncientAnvilContent.RUNESTONE_ITEM.get()))runeCount+=player.getInventory().getItem(i).getCount();check(h,runeCount==1,"Exactly one finished block reaches inventory");
                be.placeInput(AnvilSlot.NORTH_OUTER,new ItemStack(Items.IRON_INGOT));be.placeInput(AnvilSlot.SOUTH_OUTER,new ItemStack(Items.GOLD_INGOT));
                int before=drops(l,core,Items.IRON_INGOT)+drops(l,core,Items.GOLD_INGOT);l.destroyBlock(AncientAnvilStructure.CELLS.get(23).at(core,Direction.NORTH),true);check(h,drops(l,core,Items.IRON_INGOT)+drops(l,core,Items.GOLD_INGOT)==before+2,"Breaking a remote horn cell recovers stored workpieces exactly once");
                System.out.println("ANVIL_SMITHING_CHECK: PASS: 13 positions x4 rotations, placement/retrieval/swaps, loaded recipe, tier0, normal/cobbled deepslate, exact ingredients, saved work/output, timed four-strike craft, cooldown, durability, result pickup and break recovery");h.succeed();
            });
        }
    }
    private static int drops(net.minecraft.server.level.ServerLevel l,BlockPos p,Item item){return l.getEntitiesOfClass(ItemEntity.class,new AABB(p).inflate(6)).stream().mapToInt(e->e.getItem().is(item)?e.getItem().getCount():0).sum();}
    private static void check(GameTestHelper h,boolean ok,String why){if(!ok)throw h.assertionException(why);}
    private AnvilSmithingTests(){}
}
