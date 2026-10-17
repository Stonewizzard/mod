package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public final class ArchaeologyTableTests {
    public static void register(RegisterGameTestsEvent event){
        var env=event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"archaeology_table_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"archaeology_table_storage"),new TableTest(new TestData<>(env,Level.OVERWORLD,
                Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));
    }
    private static final class TableTest extends GameTestInstance {
        TableTest(TestData<Holder<TestEnvironmentDefinition<?>>> data){super(data);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Two-part table, storage, menu, persistence and safe drops");}
        @Override public void run(GameTestHelper helper){
            var level=helper.getLevel();
            for(int x=0;x<8;x++)for(int z=0;z<8;z++)helper.setBlock(new BlockPos(x,0,z),Blocks.STONE);
            var pos=helper.absolutePos(new BlockPos(3,1,3));var player=helper.makeMockServerPlayerInLevel();
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            verifyRecipe(helper,ExampleMod.ARCHAEOLOGY_TABLE_ITEM.get(),new Item[]{Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.STICK,Items.IRON_INGOT,Items.STICK,Items.STICK,Items.AIR,Items.STICK});
            verifyRecipe(helper,ExampleMod.ARCHAEOLOGY_DRILL.get(),new Item[]{Items.AIR,Items.IRON_INGOT,Items.REDSTONE,Items.AIR,Items.STICK,Items.IRON_INGOT,Items.AIR,Items.STICK,Items.AIR});
            verifyRecipe(helper,ExampleMod.ARCHAEOLOGY_HAMMER.get(),new Item[]{Items.IRON_INGOT,Items.IRON_INGOT,Items.AIR,Items.AIR,Items.STICK,Items.AIR,Items.AIR,Items.STICK,Items.AIR});
            verifyRecipe(helper,ExampleMod.MAGNIFYING_GLASS.get(),new Item[]{Items.GLASS_PANE,Items.IRON_INGOT,Items.AIR,Items.AIR,Items.STICK,Items.AIR,Items.AIR,Items.AIR,Items.AIR});
            var bounds=new AABB(Vec3.atLowerCornerOf(helper.absolutePos(BlockPos.ZERO)),Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(8,6,8))));
            for(var facing:Direction.Plane.HORIZONTAL)for(boolean breakRight:new boolean[]{false,true})for(boolean dropTable:new boolean[]{false,true}){
                for(var item:level.getEntitiesOfClass(ItemEntity.class,bounds))item.discard();
                player.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);player.setYRot(facing.getOpposite().toYRot());
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExampleMod.ARCHAEOLOGY_TABLE_ITEM.get(),2));
                var context=new BlockPlaceContext(new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos.below()),Direction.UP,pos.below(),false)));
                ((BlockItem)ExampleMod.ARCHAEOLOGY_TABLE_ITEM.get()).place(context);
                var state=level.getBlockState(pos);var other=pos.relative(facing.getClockWise());
                require(helper,state.is(ExampleMod.ARCHAEOLOGY_TABLE.get()) && state.getValue(ArchaeologyTableBlock.FACING)==facing,"Placement must respect all four facings");
                require(helper,player.getMainHandItem().getCount()==1,"One table item must place both halves");
                require(helper,level.getBlockState(other).is(state.getBlock()) && level.getBlockState(other).getValue(ArchaeologyTableBlock.PART)==ArchaeologyTableBlock.Part.RIGHT,"Second half must be adjacent on the right");
                var table=ArchaeologyTableBlock.inventory(level,pos);
                require(helper,table!=null && ArchaeologyTableBlock.inventory(level,other)==table && level.getBlockEntity(other)==null,"Both halves must resolve one master inventory");
                var menu=new ArchaeologyTableMenu(1,player.getInventory(),table);
                require(helper,menu.slots.size()==42 && menu.stillValid(player),"Table must expose six slots plus the player's inventory");
                for(int i=0;i<5;i++){
                    Item item=i==0?(breakRight?ExampleMod.RARE_ANCIENT_ARTIFACT.get():ExampleMod.ANCIENT_ARTIFACT.get()):ArchaeologyTableMenu.tool(i);
                    var storedStack=new ItemStack(item,i==0?4:1);
                    if(i==0)storedStack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,Component.literal("Excavation specimen"));
                    player.getInventory().setItem(9+i,storedStack);
                    require(helper,!menu.quickMoveStack(player,6+i).isEmpty() && table.getItem(i).is(item) && table.getItem(i).getCount()==1,"Shift-click must route each item to its dedicated slot");
                }
                require(helper,player.getInventory().getItem(9).getCount()==3 && menu.quickMoveStack(player,6).isEmpty(),"A full artifact slot must neither consume nor duplicate surplus artifacts");
                require(helper,!menu.getSlot(0).mayPlace(new ItemStack(Items.STONE)) && !menu.getSlot(1).mayPlace(new ItemStack(Items.BRUSH)),"Slots must reject unrelated items and tools in the wrong location");
                table.setItem(5,new ItemStack(ArchaeologyContent.FRAGMENTS.get(),3));
                var saved=table.saveWithFullMetadata(level.registryAccess());
                var restored=BlockEntity.loadStatic(pos,state,saved,level.registryAccess());
                require(helper,restored instanceof ArchaeologyTableBlockEntity,"Saved table must reload as the correct block entity");
                for(int i=0;i<6;i++)require(helper,ItemStack.matches(((ArchaeologyTableBlockEntity)restored).getItem(i),table.getItem(i)),"All six stored stacks, counts and components must survive serialization");
                require(helper,table.getUpdatePacket()!=null && !table.getUpdateTag(level.registryAccess()).isEmpty(),"World item displays need inventory update and chunk-load packets");
                player.snapTo(pos.getX()+30,pos.getY(),pos.getZ());require(helper,!menu.stillValid(player),"The server must close menus opened from too far away");
                level.destroyBlock(breakRight?other:pos,dropTable);
                require(helper,level.getBlockState(pos).isAir() && level.getBlockState(other).isAir(),"Breaking either half must remove both halves");
                var drops=level.getEntitiesOfClass(ItemEntity.class,bounds);
                int tables=drops.stream().filter(e->e.getItem().is(ExampleMod.ARCHAEOLOGY_TABLE_ITEM.get())).mapToInt(e->e.getItem().getCount()).sum();
                int stored=drops.stream().filter(e->!e.getItem().is(ExampleMod.ARCHAEOLOGY_TABLE_ITEM.get())).mapToInt(e->e.getItem().getCount()).sum();
                require(helper,tables==(dropTable?1:0) && stored==8,"Breaking either half must respect the block-drop flag and return five input/tool items and three recovered fragments; got "+tables+"/"+stored);
            }
            // A blocked partner must leave the first position and the held stack untouched.
            var other=pos.east();level.setBlockAndUpdate(other,Blocks.STONE.defaultBlockState());
            player.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);player.setYRot(0);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ExampleMod.ARCHAEOLOGY_TABLE_ITEM.get(),2));
            var blocked=new BlockPlaceContext(new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos.below()),Direction.UP,pos.below(),false)));
            ((BlockItem)ExampleMod.ARCHAEOLOGY_TABLE_ITEM.get()).place(blocked);
            require(helper,level.getBlockState(pos).isAir() && player.getMainHandItem().getCount()==2,"Blocked placement must not leave half a table or consume an item");
            helper.succeed();
        }
    }
    private static void verifyRecipe(GameTestHelper helper,Item result,Item[] items){
        var input=net.minecraft.world.item.crafting.CraftingInput.of(3,3,java.util.Arrays.stream(items).map(i->i==Items.AIR?ItemStack.EMPTY:new ItemStack(i)).toList());
        var recipe=helper.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,helper.getLevel());
        require(helper,recipe.isPresent() && recipe.get().value().assemble(input).is(result),"Table and all three tools must be craftable from their documented materials: "+result);
    }
    private static void require(GameTestHelper helper,boolean condition,String message){if(!condition)throw helper.assertionException(message);}
}
