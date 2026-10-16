package com.example.examplemod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Artifact, drill, brush, hammer, lens. Slot order is also the tabletop layout. */
public final class ArchaeologyTableMenu extends AbstractContainerMenu {
    public static final TagKey<Item> ARTIFACTS=TagKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"archaeology_artifacts"));
    public static final int[][] POSITIONS={{44,52},{104,36},{132,36},{104,64},{132,64}};
    private final Container container;
    public ArchaeologyTableMenu(int id,Inventory inventory){this(id,inventory,new SimpleContainer(5));}
    public ArchaeologyTableMenu(int id,Inventory inventory,Container container){
        super(ExampleMod.ARCHAEOLOGY_TABLE_MENU.get(),id);checkContainerSize(container,5);this.container=container;
        for(int i=0;i<5;i++){
            final int slot=i;
            addSlot(new Slot(container,i,POSITIONS[i][0],POSITIONS[i][1]){
                @Override public boolean mayPlace(ItemStack stack){return accepts(slot,stack);}
                @Override public int getMaxStackSize(){return 1;}
            });
        }
        addStandardInventorySlots(inventory,8,120);
    }
    public static Item tool(int slot){return switch(slot){case 1->ExampleMod.ARCHAEOLOGY_DRILL.get();case 2->Items.BRUSH;case 3->ExampleMod.ARCHAEOLOGY_HAMMER.get();case 4->ExampleMod.MAGNIFYING_GLASS.get();default->Items.AIR;};}
    public static boolean accepts(int slot,ItemStack stack){return !stack.isEmpty() && (slot==0?stack.is(ARTIFACTS):slot>0 && slot<5 && stack.is(tool(slot)));}
    @Override public boolean stillValid(Player player){return container.stillValid(player);}
    @Override public ItemStack quickMoveStack(Player player,int index){
        var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        var stack=slot.getItem();var original=stack.copy();
        if(index<5){if(!moveItemStackTo(stack,5,slots.size(),true))return ItemStack.EMPTY;}
        else {
            boolean inserted=false;
            for(int i=0;i<5;i++)if(accepts(i,stack)){inserted=moveItemStackTo(stack,i,i+1,false);break;}
            if(!inserted)return ItemStack.EMPTY;
        }
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
        slot.onTake(player,stack);return original;
    }
}
