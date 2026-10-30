package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Native world, menu, recipe and persistence coverage for the first anvil/alloying progression. */
public final class AncientAnvilTests {
    public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent event){
        var env=event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ancient_anvil_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ancient_anvil_and_alloying"),new Test(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));
    }
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> d){super(d);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Ancient Anvil tiers, metal foundation, forge-only alloying and crafting");}
        @Override public void run(GameTestHelper h){
            var l=h.getLevel();var core=h.absolutePos(new BlockPos(3,2,3));
            for(var facing:Direction.Plane.HORIZONTAL){
                for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)l.setBlock(core.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);
                l.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState().setValue(AncientAnvilBlock.FACING,facing),3);
                AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));check(h,l.getBlockState(core).getValue(AncientAnvilBlock.TIER)==0&&AncientAnvilBlockEntity.missing(l,core).size()==9,"A standalone anvil is tier zero");
                for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)l.setBlock(core.offset(x,-1,z),((x+z)%2==0?Blocks.IRON_BLOCK:Blocks.GOLD_BLOCK).defaultBlockState(),3);
                AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));check(h,l.getBlockState(core).getValue(AncientAnvilBlock.TIER)==1,"A complete mixed-metal 3x3 foundation upgrades every facing");
                var missing=core.offset(1,-1,1);l.setBlock(missing,Blocks.STONE.defaultBlockState(),3);AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));check(h,l.getBlockState(core).getValue(AncientAnvilBlock.TIER)==0&&AncientAnvilBlockEntity.missing(l,core).size()==1,"Removing one metal block must downgrade the anvil");
                l.setBlock(missing,AncientAnvilContent.METAL.get().defaultBlockState(),3);AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));check(h,l.getBlockState(core).getValue(AncientAnvilBlock.TIER)==1&&l.getBlockState(missing).is(AncientAnvilContent.METAL.get()),"Ancient metal qualifies without being consumed or converted");
                l.destroyBlock(core,false);check(h,l.getBlockState(missing).is(AncientAnvilContent.METAL.get()),"Removing the anvil must leave the foundation intact");
            }
            check(h,net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace("copper_block")).defaultBlockState().is(AncientAnvilContent.FOUNDATION),"Copper metal blocks must qualify without a mod dependency");
            for(var part:RuinedForgeStructure.PARTS)l.setBlock(part.at(core,Direction.NORTH),AncientForgeContent.BRICKS.get().defaultBlockState(),3);
            l.setBlock(core,AncientForgeContent.CORE.get().defaultBlockState(),3);check(h,ForgeAssembly.form(l,core),"Alloying uses a real formed Ruined Forge");
            var forge=(ReconstructionTableBlockEntity)l.getBlockEntity(core);forge.alloying(true);
            var player=h.makeMockServerPlayerInLevel();player.snapTo(core.getX()+.5,core.getY()+1,core.getZ()+.5);var menu=new ForgeAlloyMenu(75,player.getInventory(),forge);player.containerMenu=menu;
            for(var source:List.of(AncientAnvilContent.SCRAP_ITEM.get(),Items.ANCIENT_DEBRIS)){
                forge.setItem(0,new ItemStack(source));forge.setItem(1,ItemStack.EMPTY);forge.setItem(2,new ItemStack(Items.IRON_INGOT,2));forge.setItem(5,ItemStack.EMPTY);
                check(h,!menu.clickMenuButton(player,1)&&forge.getItem(0).getCount()==1&&forge.getItem(2).getCount()==2,"Incomplete recipes preserve all ingredients");
                forge.setItem(1,new ItemStack(Items.GOLD_INGOT));check(h,menu.ready()&&menu.clickMenuButton(player,1)&&forge.getItem(5).is(AncientAnvilContent.INGOT.get())&&forge.getItem(5).getCount()==4,"Either ancient scrap or debris alloys to four ancient ingots");
                check(h,forge.getItem(0).isEmpty()&&forge.getItem(1).isEmpty()&&forge.getItem(2).isEmpty()&&!menu.clickMenuButton(player,1),"Alloying consumes exactly one batch and duplicate packets cannot produce extra ingots");
            }
            forge.setItem(0,new ItemStack(AncientAnvilContent.SCRAP_ITEM.get(),4));forge.setItem(1,new ItemStack(Items.GOLD_INGOT,4));forge.setItem(2,new ItemStack(Items.IRON_INGOT,8));forge.setItem(5,new ItemStack(AncientAnvilContent.INGOT.get(),61));
            check(h,!menu.clickMenuButton(player,1)&&forge.getItem(2).getCount()==8,"Output overflow rejects the whole batch");
            forge.setItem(5,new ItemStack(Items.DIAMOND));check(h,!menu.clickMenuButton(player,1)&&forge.getItem(0).getCount()==4,"Existing foreign outputs cannot be overwritten");forge.setItem(5,ItemStack.EMPTY);
            forge.setItem(3,new ItemStack(ReconstructionContent.HANDLE.get()));check(h,!menu.ready(),"Switching modes preserves spare components and requires removing them before alloying");forge.setItem(3,ItemStack.EMPTY);
            var damage=RuinedForgeStructure.PARTS.getLast().at(core,Direction.NORTH);l.destroyBlock(damage,false);check(h,!menu.stillValid(player)&&!menu.clickMenuButton(player,1)&&forge.getItem(0).getCount()==4,"Breaking the forge must block alloying without consuming ingredients");
            l.setBlock(damage,AncientForgeContent.BRICKS.get().defaultBlockState(),3);check(h,ForgeAssembly.form(l,core)&&menu.clickMenuButton(player,1)&&forge.getItem(5).getCount()==4,"Repair resumes the same saved alloy project");
            var saved=forge.saveWithFullMetadata(l.registryAccess());var loaded=BlockEntity.loadStatic(core,forge.getBlockState(),saved,l.registryAccess());check(h,loaded instanceof ReconstructionTableBlockEntity restored&&restored.alloying()&&restored.getItem(2).getCount()==6&&restored.getItem(5).getCount()==4,"Forge mode, ingredient stack counts and output persist across reloads");
            var slot=player.getInventory().getItem(9);player.getInventory().setItem(9,new ItemStack(Items.IRON_INGOT,2));check(h,!menu.quickMoveStack(player,6).isEmpty()&&forge.getItem(2).getCount()==8,"Shift-click routes iron to its correct input");player.getInventory().setItem(9,slot);
            var ingot=AncientAnvilContent.INGOT.get();var metal=AncientAnvilContent.METAL_ITEM.get();
            craft(h,3,3,List.of(metal,metal,metal,Items.AIR,ingot,Items.AIR,ingot,ingot,ingot),AncientAnvilContent.ANVIL_ITEM.get(),1);
            craft(h,3,3,Collections.nCopies(9,ingot),metal,1);craft(h,1,1,List.of(metal),ingot,9);
            System.out.println("ANCIENT_ANVIL_CHECK: PASS: four facings, tier 0/1, mixed 3x3 metals, downgrade, unchanged foundation, both alloy sources, exact costs/output, overflow/duplicate/frame guards, stacked persistence, shift-click and native crafting");h.succeed();
        }
    }
    private static void craft(GameTestHelper h,int w,int z,List<Item> in,Item out,int count){var input=net.minecraft.world.item.crafting.CraftingInput.of(w,z,in.stream().map(ItemStack::new).toList());var recipe=h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,h.getLevel());check(h,recipe.isPresent()&&recipe.get().value().assemble(input).is(out)&&recipe.get().value().assemble(input).getCount()==count,"Crafting must resolve to the expected ancient metal item/count");}
    private static void check(GameTestHelper h,boolean ok,String why){if(!ok)throw h.assertionException(why);}
    private AncientAnvilTests(){}
}
