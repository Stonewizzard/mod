package com.example.examplemod;

import java.util.ArrayList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class ReconstructionTableMenu extends AbstractContainerMenu {
    public static final int[][] POSITIONS={{157,46},{181,46},{205,46},{181,73},{181,100},{277,73}};
    private final Container container;
    private int clientPattern;
    public ReconstructionTableMenu(int id,Inventory inventory){this(id,inventory,new SimpleContainer(6));}
    public ReconstructionTableMenu(int id,Inventory inventory,Container container){
        super(ReconstructionContent.MENU.get(),id);checkContainerSize(container,6);this.container=container;
        addDataSlot(new DataSlot(){@Override public int get(){return pattern();}@Override public void set(int value){clientPattern=value;}});
        for(int i=0;i<6;i++){final int slot=i;addSlot(new Slot(container,i,POSITIONS[i][0],POSITIONS[i][1]){
            @Override public boolean mayPlace(ItemStack stack){return accepts(pattern(),slot,stack);}
            // Spare pieces stay visible/removable when a different pattern is selected.
            @Override public boolean isActive(){return slot==5||pattern()!=0||hasItem();}
            @Override public int getMaxStackSize(){return 1;}
        });}
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,9+row*9+col,79+col*18,161+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,79+col*18,219));
    }
    public int pattern(){return container instanceof ReconstructionTableBlockEntity table?table.pattern():clientPattern;}
    public static boolean accepts(int pattern,int slot,ItemStack stack){var p=ReconstructionPattern.byId(pattern);return p!=null&&slot>=0&&slot<5&&p.part(slot)!=null&&!stack.isEmpty()&&stack.is(p.part(slot));}
    /** Legacy callers default to the original five-piece pickaxe. */
    public static boolean accepts(int slot,ItemStack stack){return accepts(1,slot,stack);}
    public int inserted(){int result=0;for(int i=0;i<5;i++)if(accepts(pattern(),i,container.getItem(i)))result++;return result;}
    public double previewScore(){var p=ReconstructionPattern.byId(pattern());if(p==null)return 0;var pieces=new ArrayList<ItemStack>();for(int slot:p.slots)pieces.add(container.getItem(slot));return ArtifactQuality.average(pieces);}
    public boolean hasSpareParts(){var p=ReconstructionPattern.byId(pattern());if(p==null)return false;for(int i=0;i<5;i++)if(p.part(i)==null&&!container.getItem(i).isEmpty())return true;return false;}
    public boolean ready(){var p=ReconstructionPattern.byId(pattern());return p!=null&&inserted()==p.parts.size()&&!hasSpareParts()&&container.getItem(5).isEmpty();}
    @Override public boolean stillValid(Player player){return container.stillValid(player);}
    @Override public boolean clickMenuButton(Player player,int button){
        if(!(player instanceof net.minecraft.server.level.ServerPlayer)||player.containerMenu!=this||!stillValid(player)||!(container instanceof ReconstructionTableBlockEntity table))return false;
        var selected=ReconstructionPattern.byId(button==0?1:button-10);
        if(selected!=null){table.pattern(selected.id);broadcastChanges();return true;}
        if(button!=1||!ready())return false;
        var p=ReconstructionPattern.byId(pattern());var pieces=new ArrayList<ItemStack>();for(int slot:p.slots)pieces.add(container.getItem(slot).copy());
        var result=ArtifactQuality.assemble(p,pieces);
        for(int slot:p.slots)container.setItem(slot,ItemStack.EMPTY);container.setItem(5,result);container.setChanged();broadcastChanges();return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        var stack=slot.getItem();var old=stack.copy();
        if(index<6){if(!moveItemStackTo(stack,6,slots.size(),true))return ItemStack.EMPTY;}
        else {boolean moved=false;for(int i=0;i<5;i++)if(accepts(pattern(),i,stack)){moved=moveItemStackTo(stack,i,i+1,false);if(moved)break;}if(!moved)return ItemStack.EMPTY;}
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return old;
    }
}
