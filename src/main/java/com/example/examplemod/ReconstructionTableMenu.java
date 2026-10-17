package com.example.examplemod;

import java.util.ArrayList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class ReconstructionTableMenu extends AbstractContainerMenu {
    public static final int[][] POSITIONS={{160,44},{180,44},{200,44},{180,70},{180,96},{274,70}};
    private final Container container;
    private int clientPattern;
    public ReconstructionTableMenu(int id,Inventory inventory){this(id,inventory,new SimpleContainer(6));}
    public ReconstructionTableMenu(int id,Inventory inventory,Container container){
        super(ReconstructionContent.MENU.get(),id);checkContainerSize(container,6);this.container=container;
        addDataSlot(new DataSlot(){@Override public int get(){return pattern();}@Override public void set(int value){clientPattern=value;}});
        for(int i=0;i<6;i++){final int slot=i;addSlot(new Slot(container,i,POSITIONS[i][0],POSITIONS[i][1]){
            @Override public boolean mayPlace(ItemStack stack){return accepts(slot,stack);}
            @Override public boolean isActive(){return slot==5||pattern()==ReconstructionPattern.PICKAXE.id;}
            @Override public int getMaxStackSize(){return 1;}
        });}
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,9+row*9+col,79+col*18,147+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,79+col*18,213));
    }
    public int pattern(){return container instanceof ReconstructionTableBlockEntity table?table.pattern():clientPattern;}
    public static boolean accepts(int slot,ItemStack stack){return slot>=0&&slot<5&&!stack.isEmpty()&&stack.is(ReconstructionPattern.PICKAXE.part(slot));}
    public int inserted(){int result=0;for(int i=0;i<5;i++)if(accepts(i,container.getItem(i)))result++;return result;}
    public double previewScore(){var pieces=new ArrayList<ItemStack>();for(int i=0;i<5;i++)pieces.add(container.getItem(i));return ArtifactQuality.average(pieces);}
    public boolean ready(){return pattern()==1&&inserted()==5&&container.getItem(5).isEmpty();}
    @Override public boolean stillValid(Player player){return container.stillValid(player);}
    @Override public boolean clickMenuButton(Player player,int button){
        if(!(player instanceof net.minecraft.server.level.ServerPlayer)||player.containerMenu!=this||!stillValid(player)||!(container instanceof ReconstructionTableBlockEntity table))return false;
        if(button==0){table.pattern(ReconstructionPattern.PICKAXE.id);broadcastChanges();return true;}
        if(button!=1||!ready())return false;
        var pieces=new ArrayList<ItemStack>();for(int i=0;i<5;i++)pieces.add(container.getItem(i).copy());
        var result=ArtifactQuality.assemble(pieces);
        for(int i=0;i<5;i++)container.setItem(i,ItemStack.EMPTY);container.setItem(5,result);container.setChanged();broadcastChanges();return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        var stack=slot.getItem();var old=stack.copy();
        if(index<6){if(!moveItemStackTo(stack,6,slots.size(),true))return ItemStack.EMPTY;}
        else {if(pattern()!=1)return ItemStack.EMPTY;boolean moved=false;for(int i=0;i<5;i++)if(accepts(i,stack)){moved=moveItemStackTo(stack,i,i+1,false);break;}if(!moved)return ItemStack.EMPTY;}
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return old;
    }
}
