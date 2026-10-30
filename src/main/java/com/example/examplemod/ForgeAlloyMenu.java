package com.example.examplemod;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Both forge modes share the core's six persisted slots; mode switches never clear them. */
public final class ForgeAlloyMenu extends AbstractContainerMenu {
    public static final int[][] POSITIONS={{42,61},{88,61},{134,61},{225,46},{249,46},{190,61}};
    private final Container container;
    public ForgeAlloyMenu(int id,Inventory inv){this(id,inv,new SimpleContainer(6));}
    public ForgeAlloyMenu(int id,Inventory inv,Container c){
        super(AncientAnvilContent.ALLOY_MENU.get(),id);checkContainerSize(c,6);container=c;
        for(int i=0;i<6;i++){final int n=i;addSlot(new Slot(c,i,POSITIONS[i][0],POSITIONS[i][1]){@Override public boolean mayPlace(ItemStack s){return AncientAlloy.accepts(n,s)&&(n==0||!melting());}@Override public int getMaxStackSize(){return 64;}});}
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,63+col*18,143+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inv,col,63+col*18,201));
    }
    public boolean melting(){return AncientAlloy.melting(container)||container.getItem(0).isEmpty()&&container.getItem(5).is(AncientAnvilContent.SCRAP_ITEM.get());}
    public boolean ready(){return AncientAlloy.ready(container);}
    @Override public boolean stillValid(Player p){return container.stillValid(p);}
    @Override public boolean clickMenuButton(Player player,int id){
        if(!(player instanceof net.minecraft.server.level.ServerPlayer server)||player.containerMenu!=this||!stillValid(player)||!(container instanceof ReconstructionTableBlockEntity forge)||!forge.getBlockState().is(AncientForgeContent.CORE.get()))return false;
        if(id==90){forge.alloying(false);server.openMenu(forge);return true;}
        if(id!=1||!ready())return false;
        // Validate everything before mutating any slot; failed/duplicate packets cannot lose ingredients.
        boolean melt=melting();var result=AncientAlloy.result(container);int amount=AncientAlloy.outputCount(container);
        if(melt)container.removeItem(0,1);else for(int i=0;i<3;i++)container.removeItem(i,AncientAlloy.count(i));
        var out=container.getItem(5);if(out.isEmpty())container.setItem(5,new ItemStack(result,amount));else out.grow(amount);
        container.setChanged();broadcastChanges();
        server.level().playSound(null,forge.getBlockPos(),net.minecraft.sounds.SoundEvents.ANVIL_USE,net.minecraft.sounds.SoundSource.BLOCKS,.5F,.85F);
        return true;
    }
    @Override public ItemStack quickMoveStack(Player p,int index){
        if(index<0||index>=slots.size()||!slots.get(index).hasItem())return ItemStack.EMPTY;var slot=slots.get(index);var s=slot.getItem();var old=s.copy();
        if(index<6){if(!moveItemStackTo(s,6,slots.size(),true))return ItemStack.EMPTY;}
        else{boolean moved=false;for(int i=0;i<3;i++)if(AncientAlloy.accepts(i,s)&&moveItemStackTo(s,i,i+1,false)){moved=true;break;}if(!moved)return ItemStack.EMPTY;}
        if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return old;
    }
}
