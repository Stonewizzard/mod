package com.example.examplemod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Artifact, drill, brush, hammer, lens. Slot order is also the tabletop layout. */
public final class ArchaeologyTableMenu extends AbstractContainerMenu {
    public static final TagKey<Item> ARTIFACTS=TagKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"archaeology_artifacts"));
    public static final int[][] POSITIONS=ExcavationLayout.SLOTS;
    private final Container container;
    private final boolean clientMenu;
    private boolean inventoryView;
    private long nextStudyTick=Long.MIN_VALUE;
    public boolean inventoryView(){return inventoryView;}
    public void inventoryView(boolean visible){inventoryView=visible;}
    /** Uses the real container cursor stack, including native pickup/return synchronization. */
    public int carriedTool(){
        for(int tool=1;tool<=4;tool++)if(accepts(tool,getCarried()))return tool;
        return 0;
    }
    public ArchaeologyTableMenu(int id,Inventory inventory){this(id,inventory,new SimpleContainer(6));}
    public ArchaeologyTableMenu(int id,Inventory inventory,Container container){
        super(ExampleMod.ARCHAEOLOGY_TABLE_MENU.get(),id);checkContainerSize(container,6);this.container=container;
        clientMenu=!(container instanceof ArchaeologyTableBlockEntity);
        if(!clientMenu&&!container.getItem(0).isEmpty()&&accepts(0,container.getItem(0))){
            var prepared=container.getItem(0).copy();ArtifactProfile.seal(prepared,inventory.player.level().getRandom().nextLong());container.setItem(0,prepared);
        }
        for(int i=0;i<6;i++){
            final int slot=i;
            addSlot(new Slot(container,i,POSITIONS[i][0],POSITIONS[i][1]){
                @Override public boolean mayPlace(ItemStack stack){return accepts(slot,stack);}
                @Override public int getMaxStackSize(){return slot==5?64:1;}
            });
        }
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,9+row*9+col,79+col*18,111+row*18){
            @Override public boolean isActive(){return !clientMenu||inventoryView;}
        });
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,79+col*18,213));
    }
    /** Called only by the server payload handler; never trust the client to edit a stack or score. */
    public boolean studyAction(net.minecraft.server.level.ServerPlayer player,int tool,int cell){
        if(player.containerMenu!=this || !stillValid(player) || !(container instanceof ArchaeologyTableBlockEntity table)
                || !accepts(0,table.getItem(0)))return false;
        long now=player.level().getGameTime();
        var artifact=table.getItem(0).copy();
        ArtifactProfile.seal(artifact,player.level().getRandom().nextLong());
        var study=ExcavationStudy.read(artifact);
        if(tool==0&&cell==-1){
            if(!study.recoverable()||!table.getItem(5).isEmpty())return false;
            var reward=ArchaeologyContent.reward(study);
            // Commit exactly once on the server thread. A second viewer sees empty input or occupied output.
            artifact.shrink(1);table.setItem(0,artifact);table.setItem(5,reward);
            nextStudyTick=now+2;broadcastChanges();return true;
        }
        if(now<nextStudyTick)return false;
        if(tool<1||tool>3||carriedTool()!=tool||!ExcavationStudy.coveredCell(cell))return false;
        var held=getCarried();int tier=ArchaeologyContent.tier(held);
        if(!study.work(tool,tier,cell))return false;
        study.write(artifact);
        held.hurtAndBreak(1,player.level(),player,unused->{});
        if(held.isEmpty())setCarried(ItemStack.EMPTY);
        nextStudyTick=now+ArchaeologyContent.cooldown(tool,tier);
        table.setItem(0,artifact);broadcastChanges();return true;
    }
    @Override public boolean clickMenuButton(Player player,int button){
        return button==0&&player instanceof net.minecraft.server.level.ServerPlayer server&&studyAction(server,0,-1);
    }
    public static Item tool(int slot){return switch(slot){case 1->ExampleMod.ARCHAEOLOGY_DRILL.get();case 2->Items.BRUSH;case 3->ExampleMod.ARCHAEOLOGY_HAMMER.get();case 4->ExampleMod.MAGNIFYING_GLASS.get();default->Items.AIR;};}
    public static boolean accepts(int slot,ItemStack stack){return !stack.isEmpty() && (slot==0?stack.is(ARTIFACTS):slot>0 && slot<5 && ArchaeologyContent.role(stack)==slot);}
    @Override public boolean stillValid(Player player){return container.stillValid(player);}
    @Override public void clicked(int index,int button,ContainerInput input,Player player){
        // Menu pickup button 0 is the primary inventory action; SDL mouse codes are mapped by the screen.
        if(input==ContainerInput.PICKUP&&button==0&&index>=1&&index<=4){
            int home=carriedTool();var held=getCarried();var next=container.getItem(index);
            if(home>0&&home!=index&&held.getCount()==1&&next.getCount()==1&&accepts(index,next)&&container.getItem(home).isEmpty()){
                // Both prediction and the server use this same native inventory click, with no item copies or extra packet.
                container.setItem(home,held);container.setItem(index,ItemStack.EMPTY);setCarried(next);return;
            }
        }
        super.clicked(index,button,input,player);
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        var stack=slot.getItem();var original=stack.copy();
        if(index<6){if(!moveItemStackTo(stack,6,slots.size(),true))return ItemStack.EMPTY;}
        else {
            boolean inserted=false;
            for(int i=0;i<5;i++)if(accepts(i,stack)){inserted=moveItemStackTo(stack,i,i+1,false);break;}
            if(!inserted)return ItemStack.EMPTY;
        }
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
        slot.onTake(player,stack);return original;
    }
}


