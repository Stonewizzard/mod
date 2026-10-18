package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class AscenderBlockEntity extends BaseContainerBlockEntity {
    public static final int SIZE=19;
    private NonNullList<ItemStack> items=NonNullList.withSize(SIZE,ItemStack.EMPTY);
    public AscenderBlockEntity(BlockPos pos,BlockState state){super(AscenderContent.ENTITY.get(),pos,state);}
    public static void tick(Level level,BlockPos pos,BlockState state,AscenderBlockEntity ascender){
        if(level instanceof net.minecraft.server.level.ServerLevel&&!ascender.isRemoved())AscensionResources.tryAscend(ascender);
    }
    @Override public int getContainerSize(){return SIZE;}
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override protected void setItems(NonNullList<ItemStack> items){this.items=items;}
    @Override protected Component getDefaultName(){return Component.translatable("container.examplemod.ascender");}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inventory){return new AscenderMenu(id,inventory,this);}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return AscenderMenu.accepts(slot,stack);}
    @Override protected void loadAdditional(ValueInput input){super.loadAdditional(input);items=NonNullList.withSize(SIZE,ItemStack.EMPTY);ContainerHelper.loadAllItems(input,items);}
    @Override protected void saveAdditional(ValueOutput output){super.saveAdditional(output);ContainerHelper.saveAllItems(output,items);}
}
