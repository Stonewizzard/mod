package com.example.examplemod;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class AppraisalMenu extends AbstractContainerMenu {
    private final Container container;
    public AppraisalMenu(int id,Inventory inventory){this(id,inventory,new SimpleContainer(1));}
    public AppraisalMenu(int id,Inventory inventory,Container container){
        super(AppraisalContent.MENU.get(),id);checkContainerSize(container,1);this.container=container;
        addSlot(new Slot(container,0,27,43){@Override public boolean mayPlace(ItemStack stack){return AppraisalReport.accepts(stack);}@Override public int getMaxStackSize(){return 1;}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,9+row*9+col,79+18*col,161+18*row));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,79+18*col,219));
    }
    @Override public boolean stillValid(Player player){return container.stillValid(player);}
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var before=stack.copy();
        if(index==0){if(!moveItemStackTo(stack,1,slots.size(),true))return ItemStack.EMPTY;}
        else if(!AppraisalReport.accepts(stack)||!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return before;
    }
}
