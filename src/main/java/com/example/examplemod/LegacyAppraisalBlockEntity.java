package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class LegacyAppraisalBlockEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items=NonNullList.withSize(1,ItemStack.EMPTY);
    public LegacyAppraisalBlockEntity(BlockPos pos,BlockState state){super(LegacyAppraisalContent.ENTITY.get(),pos,state);}
    @Override public int getContainerSize(){return 1;}
    @Override public int getMaxStackSize(){return 1;}
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override protected void setItems(NonNullList<ItemStack> items){this.items=items;}
    @Override protected Component getDefaultName(){return Component.translatable("container.barrel");}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inventory){return null;}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return false;}
    public static void tick(net.minecraft.world.level.Level level,BlockPos pos,BlockState state,LegacyAppraisalBlockEntity old){
        if(level.isClientSide()||level.getBlockEntity(pos)!=old)return;
        var stored=old.removeItemNoUpdate(0);var book=new ItemStack(FieldGuideContent.BOOK.get());
        if(!level.setBlock(pos,net.minecraft.world.level.block.Blocks.BARREL.defaultBlockState(),3)){old.setItem(0,stored);return;}
        if(level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.BarrelBlockEntity barrel){
            if(!stored.isEmpty())barrel.setItem(0,stored);barrel.setItem(stored.isEmpty()?0:1,book);barrel.setChanged();
        }else{
            if(!stored.isEmpty())net.minecraft.world.Containers.dropItemStack(level,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,stored);
            net.minecraft.world.Containers.dropItemStack(level,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,book);
        }
    }
    @Override protected void loadAdditional(ValueInput input){super.loadAdditional(input);items=NonNullList.withSize(1,ItemStack.EMPTY);ContainerHelper.loadAllItems(input,items);}
    @Override protected void saveAdditional(ValueOutput output){super.saveAdditional(output);ContainerHelper.saveAllItems(output,items);}
}
