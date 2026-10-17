package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class ArchaeologyTableBlockEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items=NonNullList.withSize(6,ItemStack.EMPTY);
    public ArchaeologyTableBlockEntity(BlockPos pos,BlockState state){super(ExampleMod.ARCHAEOLOGY_TABLE_ENTITY.get(),pos,state);}
    @Override public int getContainerSize(){return 6;}
    @Override public int getMaxStackSize(){return 64;}
    @Override public int getMaxStackSize(ItemStack stack){return ArchaeologyTableMenu.accepts(0,stack)||ArchaeologyContent.role(stack)>0?1:64;}
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override protected void setItems(NonNullList<ItemStack> items){this.items=items;}
    @Override protected Component getDefaultName(){return Component.translatable("container.examplemod.archaeology_table");}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inventory){return new ArchaeologyTableMenu(id,inventory,this);}
    @Override public void setItem(int slot,ItemStack stack){
        if(slot==0&&!stack.isEmpty()&&level instanceof ServerLevel&&ArchaeologyTableMenu.accepts(0,stack))ArtifactProfile.seal(stack,level.getRandom().nextLong());
        super.setItem(slot,stack);
    }
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return ArchaeologyTableMenu.accepts(slot,stack);}
    @Override public boolean stillValid(Player player){
        if(!super.stillValid(player)||level==null)return false;
        var state=getBlockState();var other=level.getBlockState(ArchaeologyTableBlock.partnerPos(worldPosition,state));
        return other.is(ExampleMod.ARCHAEOLOGY_TABLE.get()) && other.getValue(ArchaeologyTableBlock.PART)==ArchaeologyTableBlock.Part.RIGHT
                && other.getValue(ArchaeologyTableBlock.FACING)==state.getValue(ArchaeologyTableBlock.FACING);
    }
    @Override public void setChanged(){
        super.setChanged();
        if(level instanceof ServerLevel)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);
    }
    @Override protected void loadAdditional(ValueInput input){super.loadAdditional(input);items=NonNullList.withSize(6,ItemStack.EMPTY);ContainerHelper.loadAllItems(input,items);}
    @Override protected void saveAdditional(ValueOutput output){super.saveAdditional(output);ContainerHelper.saveAllItems(output,items);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveCustomOnly(registries);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
