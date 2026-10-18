package com.example.examplemod;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ComponentStationMenu extends AbstractContainerMenu {
    private final Container container;private int clientKind;
    public ComponentStationMenu(int id,Inventory inventory){this(id,inventory,new SimpleContainer(1));}
    public ComponentStationMenu(int id,Inventory inventory,Container container){
        super(ComponentStationContent.MENU.get(),id);checkContainerSize(container,1);this.container=container;
        addDataSlot(new DataSlot(){@Override public int get(){return kind().ordinal();}@Override public void set(int value){clientKind=value;}});
        addSlot(new Slot(container,0,27,47){@Override public boolean mayPlace(ItemStack stack){return ComponentWork.component(stack);}@Override public int getMaxStackSize(){return 1;}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,9+row*9+col,79+18*col,161+18*row));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,79+18*col,219));
    }
    public ComponentStationKind kind(){return container instanceof ComponentStationBlockEntity station?station.kind():ComponentStationKind.byId(clientKind);}
    @Override public boolean stillValid(Player player){return container.stillValid(player);}
    /** The full expected stack prevents stale offers, replacement-item clicks, and duplicate requests. */
    public boolean action(Player player,int action,ItemStack expected){
        if(!(player instanceof net.minecraft.server.level.ServerPlayer)||player.containerMenu!=this||!stillValid(player)||!(container instanceof ComponentStationBlockEntity))return false;
        var part=container.getItem(0);if(!ItemStack.matches(part,expected))return false;
        var kind=kind();int choice=kind==ComponentStationKind.UNVEILER?action-10:kind==ComponentStationKind.LOCKER?action-20:0;
        if(kind==ComponentStationKind.UNVEILER?(action<10||action>12):kind==ComponentStationKind.LOCKER?(action<20||action>27):action!=0)return false;
        if(!ComponentWork.apply(kind,part,choice,player.getRandom()))return false;container.setChanged();broadcastChanges();return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var before=stack.copy();
        if(index==0){if(!moveItemStackTo(stack,1,slots.size(),true))return ItemStack.EMPTY;}
        else if(!ComponentWork.component(stack)||!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return before;
    }
}
