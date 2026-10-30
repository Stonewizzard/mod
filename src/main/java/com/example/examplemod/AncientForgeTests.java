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

/** Actual tier-one world blocks, dedicated metallurgy, repair and world-template compatibility. */
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
            check(h,RuinedForgeStructure.PARTS.size()==17,"The starter frame must use exactly seventeen masonry blocks; the core occupies the eighteenth cell");
            for(var front:Direction.Plane.HORIZONTAL){
                for(var part:RuinedForgeStructure.PARTS)level.setBlock(part.at(core,front),Blocks.AIR.defaultBlockState(),3);
                level.setBlock(core,AncientForgeContent.CORE.get().defaultBlockState().setValue(RuinedAncientForgeBlock.FACING,front),3);
                check(h,!RuinedForgeStructure.inspect(level,core,front).complete(),"A lone core cannot act as a finished multiblock");
                for(var part:RuinedForgeStructure.PARTS)level.setBlock(part.at(core,front),AncientForgeContent.BRICKS.get().defaultBlockState(),3);
                check(h,ForgeAssembly.form(level,core)&&ForgeAssembly.formed(level.getBlockState(core)),"Repairing the frame must form the connected forge");
                for(int i=0;i<RuinedForgeStructure.PARTS.size();i++){var at=RuinedForgeStructure.PARTS.get(i).at(core,front);check(h,FormedForgePartBlock.belongs(level,at,core,front,RuinedForgeStructure.PARTS.get(i).cell()),"Every frame cell must form with the correct facing and ownership");}
                var broken=RuinedForgeStructure.PARTS.getFirst().at(core,front);var bounds=new net.minecraft.world.phys.AABB(core).inflate(5);
                int beforeBricks=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,bounds).stream().mapToInt(e->e.getItem().is(AncientForgeContent.BRICKS_ITEM.get())?e.getItem().getCount():0).sum();
                level.destroyBlock(broken,true);
                int afterBricks=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,bounds).stream().mapToInt(e->e.getItem().is(AncientForgeContent.BRICKS_ITEM.get())?e.getItem().getCount():0).sum();
                check(h,afterBricks-beforeBricks==1&&!ForgeAssembly.formed(level.getBlockState(core)),"Breaking a formed cell returns one original brick and unforms the forge");
                for(int i=1;i<RuinedForgeStructure.PARTS.size();i++)check(h,level.getBlockState(RuinedForgeStructure.PARTS.get(i).at(core,front)).is(AncientForgeContent.BRICKS.get()),"Surviving masonry must restore in place without extra drops");
                level.setBlock(broken,AncientForgeContent.BRICKS.get().defaultBlockState(),3);check(h,ForgeAssembly.form(level,core),"The repaired forge can form again");
                var forge=(ReconstructionTableBlockEntity)level.getBlockEntity(core);
                var legacyMenu=new ReconstructionTableMenu(51,player.getInventory(),forge);player.containerMenu=legacyMenu;
                var pieces=new ArrayList<ItemStack>();var finds=new ArtifactProfile.Find[]{ArtifactProfile.Find.PICKAXE_LEFT,ArtifactProfile.Find.PICKAXE_BRIDGE,ArtifactProfile.Find.PICKAXE_RIGHT,ArtifactProfile.Find.TOOL_CONNECTOR,ArtifactProfile.Find.TOOL_HANDLE};
                for(int i=0;i<5;i++){var part=ArchaeologyContent.reward(new ExcavationStudy(new ArtifactProfile(710+i,1,ArtifactProfile.Matrix.SHALE,finds[i])));forge.setItem(i,part);pieces.add(part.copy());}
                forge.pattern(1);
                check(h,!legacyMenu.stillValid(player)&&!legacyMenu.ready()&&!legacyMenu.clickMenuButton(player,11)&&!legacyMenu.clickMenuButton(player,1)&&forge.getItem(5).isEmpty(),"A forged or stale reconstruction menu must never assemble equipment in a forge");
                for(int i=0;i<5;i++)check(h,ItemStack.matches(forge.getItem(i),pieces.get(i)),"Removing forge assembly must preserve all legacy components");
                // Model a tool produced before this update, not a new forge assembly operation.
                var oldTool=ArtifactQuality.assemble(ReconstructionPattern.PICKAXE,pieces);forge.setItem(5,oldTool.copy());
                check(h,forge.createMenu(52,player.getInventory())==null&&!forge.process.start(level),"The forge has no menu and preserves legacy inventory for empty-hand withdrawal");
                var missing=RuinedForgeStructure.PARTS.getLast().at(core,front);level.setBlock(missing,Blocks.AIR.defaultBlockState(),3);
                check(h,!forge.process.start(level)&&ItemStack.matches(oldTool,forge.getItem(5)),"Breaking the structure preserves the existing output");
                for(int i=0;i<5;i++)check(h,ItemStack.matches(forge.getItem(i),pieces.get(i)),"A broken frame preserves every stored part");
                level.setBlock(missing,AncientForgeContent.BRICKS.get().defaultBlockState(),3);check(h,ForgeAssembly.form(level,core),"Repair restores the frame");
                var saved=forge.saveWithFullMetadata(level.registryAccess());saved.putBoolean("Alloying",false);
                var loaded=BlockEntity.loadStatic(core,forge.getBlockState(),saved,level.registryAccess());check(h,loaded instanceof ReconstructionTableBlockEntity restored&&ItemStack.matches(restored.getItem(5),oldTool)&&restored.createMenu(53,player.getInventory())==null,"Saved tools persist and obsolete flags cannot restore a forge menu");
                // Persist an alternate masonry state, then verify it restores exactly on dismantling.
                var original=Blocks.DEEPSLATE.defaultBlockState().setValue(RotatedPillarBlock.AXIS,front.getAxis());
                var customAt=RuinedForgeStructure.PARTS.getLast().at(core,front);
                var custom=(ForgePartBlockEntity)level.getBlockEntity(customAt);custom.original(original);
                var savedPart=custom.saveWithFullMetadata(level.registryAccess());
                var loadedPart=BlockEntity.loadStatic(customAt,custom.getBlockState(),savedPart,level.registryAccess());
                check(h,loadedPart instanceof ForgePartBlockEntity restoredPart&&restoredPart.original().equals(original),"Formed cells must persist their exact original block state across reloads");
                int before=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(core).inflate(3)).stream().mapToInt(e->e.getItem().is(ArchaeologyContent.PICKAXE.get())?e.getItem().getCount():0).sum();
                level.destroyBlock(core,true);int after=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(core).inflate(3)).stream().mapToInt(e->e.getItem().is(ArchaeologyContent.PICKAXE.get())?e.getItem().getCount():0).sum();check(h,after-before==1,"Breaking the core returns its inventory exactly once");
                for(var part:RuinedForgeStructure.PARTS){var at=part.at(core,front);check(h,at.equals(customAt)?level.getBlockState(at).equals(original):level.getBlockState(at).is(AncientForgeContent.BRICKS.get()),"Removing the core restores the surviving masonry, including original pack-provided states");}
                for(var part:RuinedForgeStructure.PARTS)level.setBlock(part.at(core,front),Blocks.AIR.defaultBlockState(),3);
            }
            recipe(h,3,3,List.of(AncientForgeContent.BRICKS_ITEM.get(),net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("copper_block")),AncientForgeContent.BRICKS_ITEM.get(),Items.IRON_INGOT,Items.BLAST_FURNACE,Items.IRON_INGOT,AncientForgeContent.BRICKS_ITEM.get(),net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("copper_block")),AncientForgeContent.BRICKS_ITEM.get()),AncientForgeContent.CORE_ITEM.get(),1);
            recipe(h,2,2,Collections.nCopies(4,Items.COBBLESTONE),AncientForgeContent.BRICKS_ITEM.get(),4);
            var id=Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ruined_ancient_forge");
            check(h,level.registryAccess().lookupOrThrow(Registries.STRUCTURE).get(net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE,id)).isPresent()&&level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET).get(net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE_SET,id)).isPresent()&&level.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL).get(net.minecraft.resources.ResourceKey.create(Registries.TEMPLATE_POOL,id)).isPresent(),"Native worldgen registries must load the structure, sparse placement and start pool");
            System.out.println("ANCIENT_FORGE_CHECK: PASS: four facings, real frame validation, repair/metallurgy + legacy storage, native persistence and drops, standalone recipes, loaded worldgen");h.succeed();
        }
    }
    /** Let the native GameTest loader read and place the exact same template used by the jigsaw pool. */
    private static final class TemplateTest extends GameTestInstance {
        TemplateTest(TestData<Holder<TestEnvironmentDefinition<?>>> data){super(data);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("World-found forge template repairs into the same tier-one multiblock");}
        @Override public void run(GameTestHelper h){
            var level=h.getLevel();var found=h.absolutePos(new BlockPos(3,2,4));
            check(h,level.getBlockState(found).is(AncientForgeContent.CORE.get()),"The world template must contain a recoverable forge core");
            var damage=RuinedForgeStructure.inspect(level,found,Direction.NORTH);check(h,damage.missing().size()==2,"A found forge must require exactly two repairs rather than granting a complete workshop");
            for(var part:damage.missing())level.setBlock(part.at(found,Direction.NORTH),AncientForgeContent.BRICKS.get().defaultBlockState(),3);
            check(h,RuinedForgeStructure.inspect(level,found,Direction.NORTH).complete(),"World-found and player-built forges must use the same repair rules");
            check(h,ForgeAssembly.form(level,found)&&ForgeAssembly.formed(level.getBlockState(found)),"A repaired world-found forge must use the same connected model state");
            System.out.println("ANCIENT_FORGE_TEMPLATE_CHECK: PASS: native structure loading, missing sections and repaired world-found hearth");h.succeed();
        }
    }
    private static void recipe(GameTestHelper h,int w,int z,List<Item> items,Item output,int count){var input=net.minecraft.world.item.crafting.CraftingInput.of(w,z,items.stream().map(ItemStack::new).toList());var found=h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,h.getLevel());check(h,found.isPresent()&&found.get().value().assemble(input).is(output)&&found.get().value().assemble(input).getCount()==count,"Non-exploration recipes must actually assemble the expected item");}
    private static void check(GameTestHelper h,boolean value,String message){if(!value)throw h.assertionException(message);}
    private AncientForgeTests(){}
}
