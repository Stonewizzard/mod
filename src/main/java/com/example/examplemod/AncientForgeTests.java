package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Actual tier-one world blocks, shared reconstruction, repair and world-template compatibility. */
public final class AncientForgeTests {
    public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent event){
        var env=event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ancient_forge_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ruined_ancient_forge"),new Test(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ruined_ancient_forge_template"),new TemplateTest(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ruined_ancient_forge"),100,0,true,Rotation.NONE,false,1,1,false,0)));
    }
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> data){super(data);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Ruined Ancient Forge: repairable multiblock, preserved components and independent acquisition");}
        @Override public void run(GameTestHelper h){
            var level=h.getLevel();var core=h.absolutePos(new BlockPos(3,2,3));var player=h.makeMockServerPlayerInLevel();player.snapTo(core.getX()+.5,core.getY()+1,core.getZ()+.5);
            check(h,RuinedForgeStructure.PARTS.size()==18,"The starter frame must use exactly eighteen masonry blocks");
            for(var front:Direction.Plane.HORIZONTAL){
                for(var part:RuinedForgeStructure.PARTS)level.setBlock(part.at(core,front),Blocks.AIR.defaultBlockState(),3);
                level.setBlock(core,AncientForgeContent.CORE.get().defaultBlockState().setValue(RuinedAncientForgeBlock.FACING,front),3);
                check(h,!RuinedForgeStructure.inspect(level,core,front).complete(),"A lone core cannot act as a finished multiblock");
                for(var part:RuinedForgeStructure.PARTS)level.setBlock(part.at(core,front),AncientForgeContent.BRICKS.get().defaultBlockState(),3);
                var forge=(ReconstructionTableBlockEntity)level.getBlockEntity(core);var menu=new ReconstructionTableMenu(51,player.getInventory(),forge);player.containerMenu=menu;
                check(h,menu.forge()&&menu.stillValid(player)&&RuinedForgeStructure.inspect(level,core,front).complete(),"Every orientation must validate and use the forge presentation");
                check(h,menu.clickMenuButton(player,11),"The validated forge must allow a pickaxe pattern");
                var pieces=new ArrayList<ItemStack>();var finds=new ArtifactProfile.Find[]{ArtifactProfile.Find.PICKAXE_LEFT,ArtifactProfile.Find.PICKAXE_BRIDGE,ArtifactProfile.Find.PICKAXE_RIGHT,ArtifactProfile.Find.TOOL_CONNECTOR,ArtifactProfile.Find.TOOL_HANDLE};
                for(int i=0;i<5;i++){var part=ArchaeologyContent.reward(new ExcavationStudy(new ArtifactProfile(710+i,1,ArtifactProfile.Matrix.SHALE,finds[i])));forge.setItem(i,part);pieces.add(part.copy());}
                var missing=RuinedForgeStructure.PARTS.getLast().at(core,front);level.setBlock(missing,Blocks.AIR.defaultBlockState(),3);
                check(h,!menu.stillValid(player)&&!menu.clickMenuButton(player,1)&&forge.getItem(5).isEmpty(),"Breaking the structure must immediately invalidate assembly without consuming components");
                for(int i=0;i<5;i++)check(h,ItemStack.matches(forge.getItem(i),pieces.get(i)),"A broken frame must preserve every stored part");
                level.setBlock(missing,AncientForgeContent.BRICKS.get().defaultBlockState(),3);
                check(h,menu.stillValid(player)&&menu.clickMenuButton(player,1)&&forge.getItem(5).is(ArchaeologyContent.PICKAXE.get()),"Repair must restore assembly using the saved parts");
                check(h,!menu.clickMenuButton(player,1),"Assembly cannot duplicate an output");
                var saved=forge.saveWithFullMetadata(level.registryAccess());var loaded=BlockEntity.loadStatic(core,forge.getBlockState(),saved,level.registryAccess());check(h,loaded instanceof ReconstructionTableBlockEntity restored&&ItemStack.matches(restored.getItem(5),forge.getItem(5))&&restored.pattern()==1,"Core inventory and pattern must survive native block-entity serialization");
                int before=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(core).inflate(3)).stream().mapToInt(e->e.getItem().is(ArchaeologyContent.PICKAXE.get())?e.getItem().getCount():0).sum();
                level.destroyBlock(core,true);int after=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(core).inflate(3)).stream().mapToInt(e->e.getItem().is(ArchaeologyContent.PICKAXE.get())?e.getItem().getCount():0).sum();check(h,after-before==1,"Breaking the core returns its inventory exactly once");
                for(var part:RuinedForgeStructure.PARTS)level.setBlock(part.at(core,front),Blocks.AIR.defaultBlockState(),3);
            }
            recipe(h,3,3,List.of(AncientForgeContent.BRICKS_ITEM.get(),net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("copper_block")),AncientForgeContent.BRICKS_ITEM.get(),Items.IRON_INGOT,Items.BLAST_FURNACE,Items.IRON_INGOT,AncientForgeContent.BRICKS_ITEM.get(),net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("copper_block")),AncientForgeContent.BRICKS_ITEM.get()),AncientForgeContent.CORE_ITEM.get(),1);
            recipe(h,2,2,Collections.nCopies(4,Items.COBBLESTONE),AncientForgeContent.BRICKS_ITEM.get(),4);
            var id=Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ruined_ancient_forge");
            check(h,level.registryAccess().lookupOrThrow(Registries.STRUCTURE).get(net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE,id)).isPresent()&&level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET).get(net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE_SET,id)).isPresent()&&level.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL).get(net.minecraft.resources.ResourceKey.create(Registries.TEMPLATE_POOL,id)).isPresent(),"Native worldgen registries must load the structure, sparse placement and start pool");
            System.out.println("ANCIENT_FORGE_CHECK: PASS: four facings, real frame validation, repair/assembly, native persistence and drops, standalone recipes, loaded worldgen");h.succeed();
        }
    }
    /** Let the native GameTest loader read and place the exact same template used by the jigsaw pool. */
    private static final class TemplateTest extends GameTestInstance {
        TemplateTest(TestData<Holder<TestEnvironmentDefinition<?>>> data){super(data);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("World-found forge template repairs into the same tier-one multiblock");}
        @Override public void run(GameTestHelper h){
            var level=h.getLevel();var found=h.absolutePos(new BlockPos(3,2,2));
            check(h,level.getBlockState(found).is(AncientForgeContent.CORE.get()),"The world template must contain a recoverable forge core");
            var damage=RuinedForgeStructure.inspect(level,found,Direction.NORTH);check(h,damage.missing().size()==2,"A found forge must require exactly two repairs rather than granting a complete workshop");
            for(var part:damage.missing())level.setBlock(part.at(found,Direction.NORTH),AncientForgeContent.BRICKS.get().defaultBlockState(),3);
            check(h,RuinedForgeStructure.inspect(level,found,Direction.NORTH).complete(),"World-found and player-built forges must use the same repair rules");
            System.out.println("ANCIENT_FORGE_TEMPLATE_CHECK: PASS: native structure loading, missing sections and repaired world-found hearth");h.succeed();
        }
    }
    private static void recipe(GameTestHelper h,int w,int z,List<Item> items,Item output,int count){var input=net.minecraft.world.item.crafting.CraftingInput.of(w,z,items.stream().map(ItemStack::new).toList());var found=h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,h.getLevel());check(h,found.isPresent()&&found.get().value().assemble(input).is(output)&&found.get().value().assemble(input).getCount()==count,"Non-exploration recipes must actually assemble the expected item");}
    private static void check(GameTestHelper h,boolean value,String message){if(!value)throw h.assertionException(message);}
    private AncientForgeTests(){}
}
