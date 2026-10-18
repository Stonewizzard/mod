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

public final class ComponentStationBlockEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items=NonNullList.withSize(1,ItemStack.EMPTY);
    public ComponentStationBlockEntity(BlockPos pos,BlockState state){super(ComponentStationContent.ENTITY.get(),pos,state);}
    public ComponentStationKind kind(){return ((ComponentStationBlock)getBlockState().getBlock()).kind;}
    @Override public int getContainerSize(){return 1;}
    @Override public int getMaxStackSize(){return 1;}
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override protected void setItems(NonNullList<ItemStack> items){this.items=items;}
    @Override protected Component getDefaultName(){return Component.translatable("block.examplemod."+kind().id);}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inventory){return new ComponentStationMenu(id,inventory,this);}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return slot==0&&ComponentWork.component(stack);}
    @Override protected void loadAdditional(ValueInput input){super.loadAdditional(input);items=NonNullList.withSize(1,ItemStack.EMPTY);ContainerHelper.loadAllItems(input,items);}
    @Override protected void saveAdditional(ValueOutput output){super.saveAdditional(output);ContainerHelper.saveAllItems(output,items);}
}
