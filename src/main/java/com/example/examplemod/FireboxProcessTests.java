package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Interaction, reservation, restart, paused frames, survival wear and old-world migration. */
public final class FireboxProcessTests {
    public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent event){
        var env=event.registerEnvironment(id("firebox_environment"));event.registerTest(id("firebox_process"),new Test(new TestData<>(env,Level.OVERWORLD,id("empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));
    }
    private static Identifier id(String name){return Identifier.fromNamespaceAndPath(ExampleMod.MODID,name);}
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> data){super(data);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("GUI-free fireplace processing and inventory-safe legacy core relocation");}
        @Override public void run(GameTestHelper h){
            var l=h.getLevel();var core=h.absolutePos(new BlockPos(3,2,3));var front=Direction.NORTH;
            l.setBlock(core,AncientForgeContent.CORE.get().defaultBlockState(),3);for(var p:RuinedForgeStructure.PARTS)l.setBlock(p.at(core,front),AncientForgeContent.BRICKS.get().defaultBlockState(),3);check(h,ForgeAssembly.form(l,core),"A firebox core and seventeen masonry cells form the forge");
            var forge=(ReconstructionTableBlockEntity)l.getBlockEntity(core);var player=h.makeMockServerPlayerInLevel();player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.snapTo(core.getX()+.5,core.getY(),core.getZ()-2);
            var scrap=new ItemStack(AncientAnvilContent.SCRAP_ITEM.get(),4);player.setItemInHand(InteractionHand.MAIN_HAND,scrap);ForgeProcess.interact(l,core,player,InteractionHand.MAIN_HAND);ForgeProcess.interact(l,core,player,InteractionHand.MAIN_HAND);
            check(h,scrap.getCount()==3&&forge.getItem(0).getCount()==1,"Each click deposits one item and cannot overfill the manual recipe");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.GOLD_INGOT));ForgeProcess.interact(l,core,player,InteractionHand.MAIN_HAND);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_INGOT,2));ForgeProcess.interact(l,core,player,InteractionHand.MAIN_HAND);ForgeProcess.interact(l,core,player,InteractionHand.MAIN_HAND);
            check(h,forge.process.ready()&&player.containerMenu==player.inventoryMenu&&forge.createMenu(88,player.getInventory())==null,"Ingredients load without any forge menu");
            var flint=new ItemStack(Items.FLINT_AND_STEEL);player.setItemInHand(InteractionHand.MAIN_HAND,flint);ForgeProcess.interact(l,core,player,InteractionHand.MAIN_HAND);ForgeProcess.interact(l,core,player,InteractionHand.MAIN_HAND);
            check(h,forge.process.heating()&&flint.getDamageValue()==1&&forge.getItem(5).isEmpty(),"One successful ignition costs one durability and repeated clicks cannot restart it");
            check(h,!forge.canPlaceItem(0,new ItemStack(AncientAnvilContent.SCRAP_ITEM.get()))&&!forge.canTakeItem(player.getInventory(),0,forge.getItem(0)),"Automation cannot alter a reserved batch");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);ForgeProcess.interact(l,core,player,InteractionHand.MAIN_HAND);check(h,forge.getItem(0).getCount()==1,"Empty-hand interaction cannot steal burning ingredients");
            for(int t=0;t<100;t++)forge.process.tick(l);
            check(h,forge.process.remaining()==300&&forge.getItem(5).isEmpty()&&forge.getItem(2).getCount()==2,"The batch cannot finish early or consume inputs before completion");
            var saved=forge.saveWithFullMetadata(l.registryAccess());var restored=(ReconstructionTableBlockEntity)BlockEntity.loadStatic(core,forge.getBlockState(),saved,l.registryAccess());l.setBlockEntity(restored);forge=restored;
            check(h,forge.process.heating()&&forge.process.remaining()==300&&forge.getItem(0).getCount()==1,"Reload preserves both the reserved batch and exact progress");
            var missing=RuinedForgeStructure.PARTS.getLast().at(core,front);l.destroyBlock(missing,false);for(int t=0;t<50;t++)forge.process.tick(l);check(h,forge.process.remaining()==300&&!forge.process.finished(),"A damaged or unloaded frame pauses progress without producing output");
            l.setBlock(missing,AncientForgeContent.BRICKS.get().defaultBlockState(),3);check(h,ForgeAssembly.form(l,core),"Repair forms the same forge");
            for(int t=0;t<299;t++)forge.process.tick(l);check(h,forge.getItem(5).isEmpty(),"Output waits for the final tick");forge.process.tick(l);
            check(h,forge.process.finished()&&forge.getItem(5).getCount()==4&&forge.getItem(0).isEmpty()&&forge.getItem(1).isEmpty()&&forge.getItem(2).isEmpty(),"Completion atomically consumes the exact recipe and makes four ingots");
            check(h,!forge.process.start(l),"Uncollected finished output cannot be processed twice");ForgeProcess.interact(l,core,player,InteractionHand.MAIN_HAND);check(h,forge.getItem(5).isEmpty()&&!forge.process.finished()&&countInventory(player,AncientAnvilContent.INGOT.get())==4,"Empty-hand collection delivers exactly four ingots without opening a UI");
            forge.setItem(0,ArchaeologyContent.specimen(1437,0));check(h,forge.process.start(l),"Artifacts use the same ignition and timer");
            int before=countWorld(l,core,ExampleMod.ANCIENT_ARTIFACT.get());l.destroyBlock(core,true);
            check(h,countWorld(l,core,ExampleMod.ANCIENT_ARTIFACT.get())==before+1&&countWorld(l,core,AncientAnvilContent.INGOT.get())==0,"Breaking a burning core returns the original artifact, never an early refined result");
            for(var facing:Direction.Plane.HORIZONTAL){
                for(var p:RuinedForgeStructure.CELLS)l.setBlock(p.at(core,facing),Blocks.AIR.defaultBlockState(),3);
                var old=core.relative(facing,2);l.setBlock(old,AncientForgeContent.CORE.get().defaultBlockState().setValue(RuinedAncientForgeBlock.FACING,facing).setValue(RuinedAncientForgeBlock.FORMED,true),3);
                for(var p:RuinedForgeStructure.CELLS){var at=p.at(core,facing);l.setBlock(at,AncientForgeContent.PART.get().defaultBlockState().setValue(FormedForgePartBlock.FACING,facing).setValue(FormedForgePartBlock.INDEX,p.cell()),3);}
                var source=(ReconstructionTableBlockEntity)l.getBlockEntity(old);var contents=new ItemStack[6];
                for(int i=0;i<6;i++){contents[i]=new ItemStack(i==5?ArchaeologyContent.PICKAXE.get():ReconstructionContent.pieces().get(i));contents[i].set(DataComponents.CUSTOM_NAME,Component.literal("Saved component "+i));source.setItem(i,contents[i].copy());}
                var legacy=source.saveWithFullMetadata(l.registryAccess());legacy.remove("ForgeLayout");source=(ReconstructionTableBlockEntity)BlockEntity.loadStatic(old,source.getBlockState(),legacy,l.registryAccess());l.setBlockEntity(source);
                check(h,source.legacyForge(),"Pre-update saved cores are recognized");int bricks=countWorld(l,core,AncientForgeContent.BRICKS_ITEM.get()),cores=countWorld(l,core,AncientForgeContent.CORE_ITEM.get());
                check(h,ForgeAssembly.migrate(l,old,source)&&l.getBlockState(old).isAir()&&ForgeAssembly.formed(l.getBlockState(core)),"The old front core moves into the existing fireplace in every facing");
                var moved=(ReconstructionTableBlockEntity)l.getBlockEntity(core);for(int i=0;i<6;i++)check(h,ItemStack.matches(contents[i],moved.getItem(i)),"Relocation preserves every inventory slot and all item metadata");
                check(h,countWorld(l,core,AncientForgeContent.BRICKS_ITEM.get())==bricks+1&&countWorld(l,core,AncientForgeContent.CORE_ITEM.get())==cores,"Migration returns the displaced firebox brick once and does not duplicate the hearth");
                check(h,ForgeAssembly.migrate(l,core,moved)&&countWorld(l,core,AncientForgeContent.BRICKS_ITEM.get())==bricks+1,"Migration is idempotent");
                moved.clearContent();l.destroyBlock(core,false);for(var p:RuinedForgeStructure.CELLS)l.setBlock(p.at(core,facing),Blocks.AIR.defaultBlockState(),3);
            }
            System.out.println("FIREBOX_PROCESS_CHECK: PASS: GUI-free deposits/collection, exact ignition wear, reservation, timing, reload, pause/repair, atomic output, burning drops and four-facing inventory-safe legacy migration");h.succeed();
        }
    }
    private static int countInventory(net.minecraft.world.entity.player.Player p,Item item){int n=0;for(int i=0;i<36;i++)if(p.getInventory().getItem(i).is(item))n+=p.getInventory().getItem(i).getCount();return n;}
    private static int countWorld(net.minecraft.server.level.ServerLevel l,BlockPos p,Item item){return l.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(p).inflate(8)).stream().mapToInt(e->e.getItem().is(item)?e.getItem().getCount():0).sum();}
    private static void check(GameTestHelper h,boolean condition,String why){if(!condition)throw h.assertionException(why);}
    private FireboxProcessTests(){}
}
