package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Real inventory-backed placement, protected blocks and native mod cancellation. */
public final class AnvilFoundationTests {
    private static Identifier id(String name){return Identifier.fromNamespaceAndPath(ExampleMod.MODID,name);}
    public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent e){var env=e.registerEnvironment(id("anvil_foundation_environment"));e.registerTest(id("anvil_foundation_building"),new Test(new TestData<>(env,Level.OVERWORLD,id("empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));}
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> data){super(data);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Anvil foundation: inventory cost, one-at-a-time placement, cooldown, obstruction and mod cancellation");}
        @Override public void run(GameTestHelper h){
            var level=h.getLevel();var core=h.absolutePos(new BlockPos(3,2,3));var player=h.makeMockServerPlayerInLevel();player.setGameMode(GameType.SURVIVAL);
            Runnable aim=()->{player.snapTo(core.getX()+.5,core.getY(),core.getZ()-3.2,0,0);float pitch=(float)Math.toDegrees(Math.atan2(player.getEyeY()-(core.getY()+.8),2.85));player.snapTo(core.getX()+.5,core.getY(),core.getZ()-3.2,0,pitch);};aim.run();
            level.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState(),3);AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));
            for(var at:AncientAnvilStructure.foundation(core,Direction.NORTH))level.setBlock(at,Blocks.AIR.defaultBlockState(),3);
            var protectedAt=AncientAnvilStructure.foundation(core,Direction.NORTH).getFirst();level.setBlock(protectedAt,Blocks.CHEST.defaultBlockState(),3);var chest=(ChestBlockEntity)level.getBlockEntity(protectedAt);chest.setItem(0,new ItemStack(Items.DIAMOND,7));
            var hammer=new ItemStack(AncientAnvilContent.IRON_FORGE_HAMMER.get());player.setItemInHand(InteractionHand.MAIN_HAND,hammer);player.getInventory().setItem(5,new ItemStack(AncientAnvilContent.RUNESTONE_ITEM.get(),3));
            check(h,!AnvilFoundation.placeNext(player,core),"Construction requires sneaking");player.setShiftKeyDown(true);
            player.snapTo(core.getX()+.5,core.getY(),core.getZ()-3.2,180,0);check(h,!AnvilFoundation.placeNext(player,core),"Looking away refuses construction");aim.run();
            player.setGameMode(GameType.ADVENTURE);check(h,!AnvilFoundation.placeNext(player,core),"Adventure mode cannot bypass build permission");player.setGameMode(GameType.SURVIVAL);
            check(h,!AnvilFoundation.placeNext(player,core.east(12)),"A remote or non-anvil position cannot be built");
            var be=(AncientAnvilBlockEntity)level.getBlockEntity(core);be.placeInput(AnvilSlot.CENTER,new ItemStack(Items.GOLD_INGOT));
            AnvilWork.interact(level,core,player,InteractionHand.MAIN_HAND,new BlockHitResult(AnvilSlot.CENTER.world(core,Direction.NORTH),Direction.UP,core.above(),false));check(h,be.inputCount()==1&&hammer.getDamageValue()==0,"Sneak/use with a held tool never alters workpieces or strikes the recipe");
            check(h,AnvilFoundation.placeNext(player,core)&&player.getInventory().getItem(5).getCount()==2&&placed(level,core)==1,"One accepted request places exactly one block and uses one inventory Runestone");
            check(h,!AnvilFoundation.placeNext(player,core)&&placed(level,core)==1&&player.getInventory().getItem(5).getCount()==2,"Same-tick requests cannot bypass placement rate");
            h.runAtTickTime(8,()->{aim.run();player.setShiftKeyDown(true);check(h,AnvilFoundation.placeNext(player,core)&&placed(level,core)==2&&player.getInventory().getItem(5).getCount()==1,"A later request places one more block");});
            h.runAtTickTime(16,()->{
                aim.run();player.setShiftKeyDown(true);boolean[] listening={true};Consumer<BlockEvent.EntityPlaceEvent> cancel=e->{if(listening[0]&&e.getEntity()==player)e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(cancel);
                try{check(h,!AnvilFoundation.placeNext(player,core)&&placed(level,core)==2&&player.getInventory().getItem(5).getCount()==1,"A cancelled native placement restores the block and item cost");}finally{listening[0]=false;}
            });
            h.runAtTickTime(24,()->{aim.run();player.setShiftKeyDown(true);check(h,AnvilFoundation.placeNext(player,core)&&placed(level,core)==3&&player.getInventory().getItem(5).isEmpty(),"The last Runestone is consumed exactly once");});
            h.runAtTickTime(32,()->{
                aim.run();player.setShiftKeyDown(true);check(h,!AnvilFoundation.placeNext(player,core)&&placed(level,core)==3,"An empty inventory never creates blocks");
                check(h,player.getMainHandItem()==hammer&&hammer.getDamageValue()==0&&be.inputCount()==1,"Inventory construction preserves the held tool and anvil workpieces");
                check(h,level.getBlockEntity(protectedAt)==chest&&chest.getItem(0).getCount()==7,"Construction preserves containers and their contents");
                for(var at:AncientAnvilStructure.foundation(core,Direction.NORTH))if(level.getBlockState(at).isAir())level.setBlock(at,Blocks.STONE.defaultBlockState(),3);
                player.getInventory().setItem(5,new ItemStack(AncientAnvilContent.RUNESTONE_ITEM.get()));
            });
            h.runAtTickTime(40,()->{
                aim.run();player.setShiftKeyDown(true);
                check(h,!AnvilFoundation.placeNext(player,core)&&placed(level,core)==3&&player.getInventory().getItem(5).getCount()==1,"Solid obstructions are never replaced or charged for");
                System.out.println("ANVIL_FOUNDATION_BUILD_CHECK: PASS: held-tool preservation, no menu-side effects, actual inventory costs, single placements, cooldown, reach/look/sneak/adventure guards, cancelled placement rollback and container/solid/empty-inventory protection");h.succeed();
            });
        }
    }
    private static long placed(net.minecraft.server.level.ServerLevel l,BlockPos core){return AncientAnvilStructure.foundation(core,Direction.NORTH).stream().filter(p->l.getBlockState(p).is(AncientAnvilContent.RUNESTONE.get())).count();}
    private static void check(GameTestHelper h,boolean ok,String why){if(!ok)throw h.assertionException(why);}
    private AnvilFoundationTests(){}
}
