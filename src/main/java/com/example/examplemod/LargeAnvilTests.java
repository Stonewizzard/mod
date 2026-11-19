package com.example.examplemod;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;

/** Real ownership and native harvesting, not just a big rendered bounding box. */
public final class LargeAnvilTests {
    public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent e){var env=e.registerEnvironment(id("large_anvil_environment"));e.registerTest(id("large_anvil"),new Test(new TestData<>(env,Level.OVERWORLD,id("empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));}
    private static Identifier id(String n){return Identifier.fromNamespaceAndPath(ExampleMod.MODID,n);}
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> d){super(d);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Large Ancient Anvil: occupied mesh cells, all facings, no overwritten blocks, one recovery item");}
        @Override public void run(GameTestHelper h){
            check(h,AncientAnvilContent.ANVIL.get().asItem()==AncientAnvilContent.ANVIL_ITEM.get(),"Design samples never replace the normal block item");
            check(h,AncientAnvilDesign.of(AncientAnvilContent.ANVIL_ITEM.get())==AncientAnvilDesign.FOUNDRY&&AncientAnvilContent.ANVIL.get().defaultBlockState().getValue(AncientAnvilBlock.DESIGN)==AncientAnvilDesign.FOUNDRY,"Normal anvil placement defaults to the selected Foundry design");
            check(h,AncientAnvilContent.item(AncientAnvilDesign.FOUNDRY)==AncientAnvilContent.ANVIL_ITEM.get(),"Selected anvils recover as the normal craftable item");
            var level=h.getLevel();var core=h.absolutePos(new BlockPos(3,2,3));var player=h.makeMockServerPlayerInLevel();player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.snapTo(core.getX()+6,core.getY(),core.getZ()+6);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE));
            for(var design:java.util.List.of(AncientAnvilDesign.FOUNDRY))for(var front:Direction.Plane.HORIZONTAL)for(int mined=0;mined<AncientAnvilStructure.OCCUPIED.size();mined++){
                for(var p:AncientAnvilStructure.foundation(core,front))level.setBlock(p,AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
                level.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState().setValue(AncientAnvilBlock.FACING,front).setValue(AncientAnvilBlock.DESIGN,design),3);AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));
                check(h,level.getBlockState(core).getValue(AncientAnvilBlock.LARGE)&&level.getBlockState(core).getValue(AncientAnvilBlock.TIER)==1,"The same tier-one foundation supports the enlarged anvil");
                check(h,level.getBlockState(core).getValue(AncientAnvilBlock.DESIGN)==design,"Placement keeps the chosen visual design");
                for(var c:AncientAnvilStructure.OCCUPIED){var at=c.at(core,front);check(h,level.getBlockState(at).getValue(c.index()==0?AncientAnvilBlock.DESIGN:AncientAnvilPartBlock.DESIGN)==design,"Every part carries its owner design");if(c.index()!=0)check(h,AncientAnvilStructure.belongs(level,at,core,front,c.index()),"Every rendered cell is really owned in every facing");var shape=ExpandedAncientAnvilGeometry.cell(c.index(),front);if(shape.isEmpty())continue;var bounds=shape.bounds();check(h,bounds.minX>=0&&bounds.maxX<=1&&bounds.minY>=0&&bounds.maxY<=1&&bounds.minZ>=0&&bounds.maxZ<=1,"Collision cannot extend into unowned cells");}
                int before=drops(level,core,design);var at=AncientAnvilStructure.OCCUPIED.get(mined).at(core,front);if(mined%2==0)level.destroyBlock(at,true);else player.gameMode.destroyBlock(at);
                check(h,drops(level,core,design)==before+1,"Harvesting any anvil section must drop exactly one anvil");
                for(var c:AncientAnvilStructure.CELLS)check(h,level.getBlockState(c.at(core,front)).isAir(),"Harvesting any section clears all occupied mesh cells");
                for(var p:AncientAnvilStructure.foundation(core,front))check(h,level.getBlockState(p).is(AncientAnvilContent.FOUNDATION),"Anvil recovery preserves every foundation Runestone block");
            }
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);level.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState(),3);AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));int creativeBefore=drops(level,core);player.gameMode.destroyBlock(core.north().above());check(h,drops(level,core)==creativeBefore&&level.getBlockState(core).isAir(),"Creative removal clears the multiblock without spawning free anvil items");
            var blocked=core.north();level.setBlock(blocked,Blocks.CHEST.defaultBlockState(),3);level.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState(),3);AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));
            check(h,!level.getBlockState(core).getValue(AncientAnvilBlock.LARGE)&&level.getBlockState(blocked).is(Blocks.CHEST),"An old cramped anvil remains compact rather than erasing a neighboring container");
            level.destroyBlock(blocked,false);AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));check(h,level.getBlockState(core).getValue(AncientAnvilBlock.LARGE),"Clearing space upgrades the same saved anvil without another item");
            level.destroyBlock(core,false);
            // Old six-cell states keep their indices and meshes while a new side
            // cell is blocked; their chest and contents must never be overwritten.
            level.destroyBlock(core,false);
            level.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState().setValue(AncientAnvilBlock.LARGE,true),3);
            for(int i=1;i<6;i++){
                var c=AncientAnvilStructure.CELLS.get(i);
                level.setBlock(c.at(core,net.minecraft.core.Direction.NORTH),AncientAnvilContent.PART.get().defaultBlockState().setValue(AncientAnvilPartBlock.CELL,i),3);
            }
            var side=AncientAnvilStructure.CELLS.get(6).at(core,net.minecraft.core.Direction.NORTH);
            level.setBlock(side,Blocks.CHEST.defaultBlockState(),3);
            var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)level.getBlockEntity(side);chest.setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND,7));
            AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));
            check(h,level.getBlockState(core).getValue(AncientAnvilBlock.LARGE)&&!level.getBlockState(core).getValue(AncientAnvilBlock.MONUMENTAL)&&chest.getItem(0).getCount()==7,"Blocked legacy migration keeps its six-cell model and neighboring inventory");
            level.destroyBlock(side,false);AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));
            check(h,level.getBlockState(core).getValue(AncientAnvilBlock.MONUMENTAL),"A legacy anvil upgrades when the additional cells become safe");
            for(var c:AncientAnvilStructure.OCCUPIED)if(c.index()!=0)check(h,level.getBlockState(c.at(core,net.minecraft.core.Direction.NORTH)).getValue(AncientAnvilPartBlock.MONUMENTAL),"Every claimed model uses the new footprint");
            level.destroyBlock(core,false);
            // The eighteen-cell layout stays intact when only the NEW front row is blocked.
            level.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState().setValue(AncientAnvilBlock.LARGE,true).setValue(AncientAnvilBlock.MONUMENTAL,true),3);
            for(int i=1;i<18;i++){var c=AncientAnvilStructure.CELLS.get(i);level.setBlock(c.at(core,Direction.NORTH),AncientAnvilContent.PART.get().defaultBlockState().setValue(AncientAnvilPartBlock.CELL,i).setValue(AncientAnvilPartBlock.MONUMENTAL,true),3);}
            var newFront=AncientAnvilStructure.CELLS.get(22).at(core,Direction.NORTH);level.setBlock(newFront,Blocks.CHEST.defaultBlockState(),3);
            var savedChest=(net.minecraft.world.level.block.entity.ChestBlockEntity)level.getBlockEntity(newFront);savedChest.setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD,9));
            AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));check(h,!level.getBlockState(core).getValue(AncientAnvilBlock.EXPANDED)&&savedChest.getItem(0).getCount()==9,"Saved eighteen-cell model waits safely for new horn space");
            level.destroyBlock(newFront,false);AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));check(h,level.getBlockState(core).getValue(AncientAnvilBlock.EXPANDED),"Saved eighteen-cell model expands after clearing the obstruction");level.destroyBlock(core,false);
            // A new footprint must not intersect a living creature.
            var creature=h.spawn(ExampleMod.CRUNCHER.get(),new BlockPos(3,3,2));creature.setNoAi(true);
            level.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState(),3);AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));
            check(h,!level.getBlockState(core).getValue(AncientAnvilBlock.LARGE),"Automatic expansion waits for living entities to move away");creature.discard();AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));check(h,level.getBlockState(core).getValue(AncientAnvilBlock.LARGE),"Safe expansion resumes after the obstruction leaves");
            System.out.println("LARGE_ANVIL_CHECK: PASS: actual mesh-owned cells, four facings, all occupied-cell harvesting paths recover once, foundation preservation, old-world fallback, six/eighteen-to-thirty-two-cell migration, container/entity protection and safe expansion");h.succeed();
        }
    }
    private static int drops(net.minecraft.server.level.ServerLevel l,BlockPos p){return drops(l,p,AncientAnvilDesign.FOUNDRY);}
    private static int drops(net.minecraft.server.level.ServerLevel l,BlockPos p,AncientAnvilDesign design){return l.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(p).inflate(5)).stream().mapToInt(e->e.getItem().is(AncientAnvilContent.item(design))?e.getItem().getCount():0).sum();}
    private static void check(GameTestHelper h,boolean ok,String why){if(!ok)throw h.assertionException(why);}
    private LargeAnvilTests(){}
}
