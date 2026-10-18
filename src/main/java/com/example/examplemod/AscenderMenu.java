package com.example.examplemod;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class AscenderMenu extends AbstractContainerMenu {
    public static final int STORAGE_X=16,STORAGE_Y=92,PLAYER_X=79,PLAYER_Y=161,HOTBAR_Y=219;
    private final Container container;
    public AscenderMenu(int id,Inventory inventory){this(id,inventory,new SimpleContainer(AscenderBlockEntity.SIZE));}
    public AscenderMenu(int id,Inventory inventory,Container container){
        super(AscenderContent.MENU.get(),id);checkContainerSize(container,AscenderBlockEntity.SIZE);this.container=container;
        addSlot(new Slot(container,0,18,44){@Override public boolean mayPlace(ItemStack stack){return accepts(0,stack);}@Override public int getMaxStackSize(){return 1;}});
        for(int i=0;i<18;i++){final int slot=i+1;addSlot(new Slot(container,slot,STORAGE_X+(i%6)*18,STORAGE_Y+(i/6)*18){@Override public boolean mayPlace(ItemStack stack){return accepts(slot,stack);}});}
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,9+row*9+col,PLAYER_X+18*col,PLAYER_Y+18*row));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,PLAYER_X+18*col,HOTBAR_Y));
    }
    public static boolean accepts(int slot,ItemStack stack){return slot==0?PickaxeProgression.supports(stack):slot>0&&slot<AscenderBlockEntity.SIZE&&AscensionResources.isMaterial(stack);}
    public Container inventory(){return container;}
    @Override public boolean stillValid(Player player){return container.stillValid(player);}
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        var stack=slot.getItem();var before=stack.copy();int size=AscenderBlockEntity.SIZE;
        if(index<size){if(!moveItemStackTo(stack,size,slots.size(),true))return ItemStack.EMPTY;}
        else if(PickaxeProgression.supports(stack)){if(!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;}
        else if(AscensionResources.isMaterial(stack)){if(!moveItemStackTo(stack,1,size,false))return ItemStack.EMPTY;}
        else return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return before;
    }
}
