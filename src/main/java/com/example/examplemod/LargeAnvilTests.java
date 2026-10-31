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
        @Override protected MutableComponent typeDescription(){return Component.literal("Large Ancient Anvil: six owned cells, all facings, no overwritten blocks, one recovery item");}
        @Override public void run(GameTestHelper h){
            var level=h.getLevel();var core=h.absolutePos(new BlockPos(3,2,3));var player=h.makeMockServerPlayerInLevel();player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.snapTo(core.getX()+6,core.getY(),core.getZ()+6);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE));
            for(var front:Direction.Plane.HORIZONTAL)for(int mined=0;mined<6;mined++){
                for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)level.setBlock(core.offset(x,-1,z),((x+z)%2==0?Blocks.IRON_BLOCK:Blocks.GOLD_BLOCK).defaultBlockState(),3);
                level.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState().setValue(AncientAnvilBlock.FACING,front),3);AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));
                check(h,level.getBlockState(core).getValue(AncientAnvilBlock.LARGE)&&level.getBlockState(core).getValue(AncientAnvilBlock.TIER)==1,"The same tier-one foundation supports the enlarged anvil");
                for(var c:AncientAnvilStructure.CELLS){var at=c.at(core,front);if(c.index()!=0)check(h,AncientAnvilStructure.belongs(level,at,core,front,c.index()),"Every rendered cell is really owned in every facing");var bounds=AncientAnvilGeometry.cell(c.index(),front).bounds();check(h,bounds.minX>=0&&bounds.maxX<=1&&bounds.minY>=0&&bounds.maxY<=1&&bounds.minZ>=0&&bounds.maxZ<=1,"Collision cannot extend into unowned cells");}
                int before=drops(level,core);var at=AncientAnvilStructure.CELLS.get(mined).at(core,front);if(mined%2==0)level.destroyBlock(at,true);else player.gameMode.destroyBlock(at);
                check(h,drops(level,core)==before+1,"Harvesting any anvil section must drop exactly one anvil");
                for(var c:AncientAnvilStructure.CELLS)check(h,level.getBlockState(c.at(core,front)).isAir(),"Harvesting any section clears all six owned cells");
                for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)check(h,level.getBlockState(core.offset(x,-1,z)).is(AncientAnvilContent.FOUNDATION),"Anvil recovery preserves every foundation metal block");
            }
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);level.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState(),3);AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));int creativeBefore=drops(level,core);player.gameMode.destroyBlock(core.north().above());check(h,drops(level,core)==creativeBefore&&level.getBlockState(core).isAir(),"Creative removal clears the multiblock without spawning free anvil items");
            var blocked=core.north();level.setBlock(blocked,Blocks.CHEST.defaultBlockState(),3);level.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState(),3);AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));
            check(h,!level.getBlockState(core).getValue(AncientAnvilBlock.LARGE)&&level.getBlockState(blocked).is(Blocks.CHEST),"An old cramped anvil remains compact rather than erasing a neighboring container");
            level.destroyBlock(blocked,false);AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));check(h,level.getBlockState(core).getValue(AncientAnvilBlock.LARGE),"Clearing space upgrades the same saved anvil without another item");
            level.destroyBlock(core,false);
            // A new footprint must not intersect a living creature.
            var creature=h.spawn(ExampleMod.CRUNCHER.get(),new BlockPos(3,3,2));creature.setNoAi(true);
            level.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState(),3);AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));
            check(h,!level.getBlockState(core).getValue(AncientAnvilBlock.LARGE),"Automatic expansion waits for living entities to move away");creature.discard();AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));check(h,level.getBlockState(core).getValue(AncientAnvilBlock.LARGE),"Safe expansion resumes after the obstruction leaves");
            System.out.println("LARGE_ANVIL_CHECK: PASS: six actual owned cells, four facings, all24 harvesting paths recover once, foundation preservation, old-world fallback, container/entity protection and safe expansion");h.succeed();
        }
    }
    private static int drops(net.minecraft.server.level.ServerLevel l,BlockPos p){return l.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(p).inflate(5)).stream().mapToInt(e->e.getItem().is(AncientAnvilContent.ANVIL_ITEM.get())?e.getItem().getCount():0).sum();}
    private static void check(GameTestHelper h,boolean ok,String why){if(!ok)throw h.assertionException(why);}
    private LargeAnvilTests(){}
}
