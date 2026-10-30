package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;

/** Real entity interaction, native forge menus and reversible crafting; loot digs have their own suite. */
public final class AncientScrapTests {
    public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent e){var env=e.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ancient_scrap_environment"));e.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ancient_scrap_recycling"),new Test(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));}
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> d){super(d);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Ancient Scrap: Cruncher feeding, artifact melting, source replacement and reversible 9:1 crafting");}
        @Override public void run(GameTestHelper h){
            var l=h.getLevel();var core=h.absolutePos(new BlockPos(3,2,3));var player=h.makeMockServerPlayerInLevel();player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.snapTo(core.getX()+.5,core.getY()+1,core.getZ()+.5);
            var scrap=AncientAnvilContent.SCRAP_ITEM.get();
            check(h,AncientAlloy.accepts(0,new ItemStack(scrap))&&AncientAlloy.accepts(0,new ItemStack(Items.ANCIENT_DEBRIS))&&!AncientAlloy.accepts(0,new ItemStack(Items.NETHERITE_SCRAP)),"Ancient Scrap replaces vanilla scrap as a direct alloy input; debris remains valid");
            craft(h,3,3,Collections.nCopies(9,scrap),Items.NETHERITE_SCRAP,1);craft(h,1,1,List.of(Items.NETHERITE_SCRAP),scrap,9);
            for(int rarity=0;rarity<6;rarity++){
                player.getInventory().clearContent();var mob=h.spawn(ExampleMod.CRUNCHER.get(),new BlockPos(5,1,5));mob.setNoAi(true);
                var source=ArchaeologyContent.specimen(9000+rarity,rarity);player.setItemInHand(InteractionHand.OFF_HAND,source);
                mob.mobInteract(player,InteractionHand.OFF_HAND);
                check(h,source.isEmpty()&&count(player,scrap)==1&&mob.isCrunching()&&!mob.isInLove(),"Every artifact rarity feeds exactly once, awards one scrap and animates without breeding");
                var second=ArchaeologyContent.specimen(9100+rarity,rarity);player.setItemInHand(InteractionHand.OFF_HAND,second);mob.mobInteract(player,InteractionHand.OFF_HAND);
                check(h,second.getCount()==1&&count(player,scrap)==1,"A chewing Cruncher must not consume another artifact or duplicate its output");mob.discard();
            }
            player.getInventory().clearContent();var baby=h.spawn(ExampleMod.CRUNCHER.get(),new BlockPos(5,1,5));baby.setAge(-1200);var source=ArchaeologyContent.specimen(9200,0);player.setItemInHand(InteractionHand.OFF_HAND,source);baby.mobInteract(player,InteractionHand.OFF_HAND);check(h,source.getCount()==1&&count(player,scrap)==0,"Baby Crunchers do not recycle artifacts");baby.discard();
            for(int i=0;i<36;i++)player.getInventory().setItem(i,new ItemStack(Items.STONE,64));var full=h.spawn(ExampleMod.CRUNCHER.get(),new BlockPos(5,1,5));full.setNoAi(true);source=ArchaeologyContent.specimen(9201,0);player.setItemInHand(InteractionHand.OFF_HAND,source);int before=l.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(core).inflate(8)).stream().mapToInt(it->it.getItem().is(scrap)?it.getItem().getCount():0).sum();full.mobInteract(player,InteractionHand.OFF_HAND);int after=l.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(core).inflate(8)).stream().mapToInt(it->it.getItem().is(scrap)?it.getItem().getCount():0).sum();check(h,source.isEmpty()&&after-before==1,"A full player inventory drops exactly one scrap instead of losing the reward");full.discard();player.getInventory().clearContent();
            l.setBlock(core,AncientForgeContent.CORE.get().defaultBlockState(),3);for(var p:RuinedForgeStructure.PARTS)l.setBlock(p.at(core,Direction.NORTH),AncientForgeContent.BRICKS.get().defaultBlockState(),3);check(h,ForgeAssembly.form(l,core),"Melting requires a real completed forge");var forge=(ReconstructionTableBlockEntity)l.getBlockEntity(core);forge.alloying(true);var menu=new ForgeAlloyMenu(82,player.getInventory(),forge);player.containerMenu=menu;
            for(int rarity=0;rarity<6;rarity++){
                source=ArchaeologyContent.specimen(9300+rarity,rarity);forge.setItem(0,source);forge.setItem(5,ItemStack.EMPTY);
                check(h,menu.melting()&&menu.ready()&&menu.clickMenuButton(player,1)&&forge.getItem(0).isEmpty()&&forge.getItem(5).is(scrap)&&forge.getItem(5).getCount()==1,"Every artifact rarity melts into one scrap with no gold or iron cost");
                check(h,menu.melting()&&!menu.ready()&&!menu.clickMenuButton(player,1)&&forge.getItem(5).getCount()==1,"An empty source cannot generate free scrap from an existing output");
            }
            source=ArchaeologyContent.specimen(9400,1);forge.setItem(0,source);forge.setItem(5,new ItemStack(scrap,64));check(h,!menu.clickMenuButton(player,1)&&source.getCount()==1,"Full scrap output prevents consumption");forge.setItem(5,new ItemStack(AncientAnvilContent.INGOT.get()));check(h,!menu.clickMenuButton(player,1)&&source.getCount()==1,"Melting never overwrites an ingot output");forge.setItem(5,ItemStack.EMPTY);
            forge.setItem(1,new ItemStack(Items.GOLD_INGOT));check(h,!menu.clickMenuButton(player,1)&&forge.getItem(1).getCount()==1&&source.getCount()==1,"Melting preserves leftover metals and asks for their removal");forge.setItem(1,ItemStack.EMPTY);
            var missing=RuinedForgeStructure.PARTS.getLast().at(core,Direction.NORTH);l.destroyBlock(missing,false);check(h,!menu.clickMenuButton(player,1)&&source.getCount()==1,"Incomplete frames cannot melt artifacts");l.setBlock(missing,AncientForgeContent.BRICKS.get().defaultBlockState(),3);check(h,ForgeAssembly.form(l,core)&&menu.clickMenuButton(player,1)&&forge.getItem(5).is(scrap),"Repair safely resumes recycling");
            check(h,!AncientAlloy.accepts(0,new ItemStack(ReconstructionContent.HANDLE.get()))&&!AncientAlloy.accepts(0,new ItemStack(ArchaeologyContent.FRAGMENTS.get())),"Recovered parts and fragments must not be accidentally fed into artifact melting");
            System.out.println("ANCIENT_SCRAP_CHECK: PASS: ancient-only alloy source, reversible9:1 recipes, all6 artifact rarities for feeding/melting, chew/age/full-inventory guards, no breeding, overflow/foreign-output/metals/frame protection and no empty-source duplication");h.succeed();
        }
    }
    private static int count(net.minecraft.world.entity.player.Player p,Item item){int n=0;for(int i=0;i<36;i++)if(p.getInventory().getItem(i).is(item))n+=p.getInventory().getItem(i).getCount();return n;}
    private static void craft(GameTestHelper h,int w,int z,List<Item> in,Item out,int count){var input=net.minecraft.world.item.crafting.CraftingInput.of(w,z,in.stream().map(ItemStack::new).toList());var recipe=h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,h.getLevel());check(h,recipe.isPresent()&&recipe.get().value().assemble(input).is(out)&&recipe.get().value().assemble(input).getCount()==count,"Native9:1 conversion must produce the exact expected item/count");}
    private static void check(GameTestHelper h,boolean ok,String msg){if(!ok)throw h.assertionException(msg);}
    private AncientScrapTests(){}
}
