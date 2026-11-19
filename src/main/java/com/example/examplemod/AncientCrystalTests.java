package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Exact crystal batch cost, input order/mixing, reservation, reload and normal block recovery. */
public final class AncientCrystalTests {
    public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent e){var id=Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ancient_crystal");var env=e.registerEnvironment(id);e.registerTest(id,new Test(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));}
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> d){super(d);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Ancient Crystal: actual fireplace batch, exact cost, reload, protected mixing and block loot");}
        @Override public void run(GameTestHelper h){
            var l=h.getLevel();var core=h.absolutePos(new BlockPos(3,2,3));var crystal=AncientAnvilContent.CRYSTAL.get();var block=AncientAnvilContent.CRYSTAL_BLOCK_ITEM.get();
            l.setBlock(core,AncientForgeContent.CORE.get().defaultBlockState(),3);for(var p:RuinedForgeStructure.PARTS)l.setBlock(p.at(core,Direction.NORTH),AncientForgeContent.BRICKS.get().defaultBlockState(),3);check(h,ForgeAssembly.form(l,core),"The completed ruined forge accepts the new recipe");
            var forge=(ReconstructionTableBlockEntity)l.getBlockEntity(core);var player=h.makeMockServerPlayerInLevel();player.setGameMode(GameType.SURVIVAL);player.snapTo(core.getX()+.5,core.getY(),core.getZ()-2);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_BLOCK,2));click(l,core,player);click(l,core,player);check(h,forge.getItem(1).getCount()==1&&player.getMainHandItem().getCount()==1,"Diamond may load first; excess stays in the hand");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_INGOT));click(l,core,player);check(h,forge.getItem(2).isEmpty()&&player.getMainHandItem().getCount()==1,"Crystal batches reject alloy iron without consuming it");
            var crystals=new ItemStack(crystal,13);player.setItemInHand(InteractionHand.MAIN_HAND,crystals);for(int i=0;i<11;i++)click(l,core,player);
            check(h,!forge.process.ready()&&!forge.process.start(l)&&forge.getItem(5).isEmpty(),"Eleven crystals are insufficient");click(l,core,player);click(l,core,player);
            check(h,forge.getItem(0).getCount()==12&&crystals.getCount()==1&&forge.process.ready(),"Exactly twelve crystals fit; overfilling cannot consume a thirteenth");
            var flint=new ItemStack(Items.FLINT_AND_STEEL);player.setItemInHand(InteractionHand.MAIN_HAND,flint);click(l,core,player);check(h,forge.process.heating()&&flint.getDamageValue()==1&&forge.getItem(0).getCount()==12,"Ignition reserves all crystals until completion");
            for(int t=0;t<100;t++)forge.process.tick(l);var saved=forge.saveWithFullMetadata(l.registryAccess());forge=(ReconstructionTableBlockEntity)BlockEntity.loadStatic(core,forge.getBlockState(),saved,l.registryAccess());l.setBlockEntity(forge);
            check(h,forge.process.remaining()==300&&!forge.canPlaceItem(0,new ItemStack(crystal)),"Reload retains the batch and locks its ingredients");
            for(int t=0;t<300;t++)forge.process.tick(l);
            check(h,forge.getItem(5).is(block)&&forge.getItem(5).getCount()==1&&forge.getItem(0).isEmpty()&&forge.getItem(1).isEmpty()&&forge.getItem(2).isEmpty(),"One block consumes twelve crystals and exactly one diamond block");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);click(l,core,player);check(h,player.getInventory().countItem(block)==1&&forge.getItem(5).isEmpty(),"Empty-hand collection gives the actual block without a menu");
            forge.setItem(0,new ItemStack(crystal,15));forge.setItem(1,new ItemStack(Items.DIAMOND_BLOCK,2));check(h,forge.process.start(l),"A surplus batch is valid");for(int t=0;t<ForgeProcess.DURATION;t++)forge.process.tick(l);check(h,forge.getItem(0).getCount()==3&&forge.getItem(1).getCount()==1&&forge.getItem(5).getCount()==1,"Surplus ingredients survive exact batch consumption");
            var pos=core.east(5);var state=AncientAnvilContent.CRYSTAL_BLOCK.get().defaultBlockState();l.setBlock(pos,state,3);var pick=new ItemStack(Items.IRON_PICKAXE);
            var drops=Block.getDrops(state,l,pos,null,player,pick);check(h,drops.size()==1&&drops.getFirst().is(block)&&drops.getFirst().getCount()==1,"The placed crystal block recovers exactly itself");check(h,pick.isCorrectToolForDrops(state)&&!new ItemStack(Items.WOODEN_PICKAXE).isCorrectToolForDrops(state),"Iron pickaxe is required for crystal block drops");
            check(h,state.getLightEmission(l,pos)==7&&state.getSoundType(l,pos,player)==SoundType.AMETHYST,"Crystal block has its declared light and crystalline sound");
            forge.clearContent();forge.setItem(0,new ItemStack(AncientAnvilContent.SCRAP_ITEM.get()));check(h,!AncientAlloy.accepts(forge,1,new ItemStack(Items.DIAMOND_BLOCK)),"Diamond cannot join a staged metal alloy");
            forge.setItem(1,new ItemStack(Items.GOLD_INGOT));forge.setItem(2,new ItemStack(Items.IRON_INGOT,2));check(h,AncientAlloy.ready(forge)&&AncientAlloy.result(forge)==AncientAnvilContent.INGOT.get(),"Original metal alloy remains valid");
            System.out.println("ANCIENT_CRYSTAL_CHECK: PASS: 12 crystals + diamond block, either input order, no overfill/mixing loss, ignition, reload, exact surplus consumption, collection, block loot and original alloy");h.succeed();
        }
    }
    private static void click(net.minecraft.server.level.ServerLevel l,BlockPos p,net.minecraft.server.level.ServerPlayer player){ForgeProcess.interact(l,p,player,InteractionHand.MAIN_HAND);}
    private static void check(GameTestHelper h,boolean ok,String text){if(!ok)throw h.assertionException(text);}
    private AncientCrystalTests(){}
}
