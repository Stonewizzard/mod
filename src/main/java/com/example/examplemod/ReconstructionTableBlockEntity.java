package com.example.examplemod;

import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class ReconstructionTableBlockEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items=NonNullList.withSize(6,ItemStack.EMPTY);
    private int pattern;
    public ReconstructionTableBlockEntity(BlockPos pos,BlockState state){super(ReconstructionContent.ENTITY.get(),pos,state);}
    public int pattern(){return pattern;}
    public void pattern(int value){pattern=ReconstructionPattern.byId(value)==null?0:value;setChanged();}
    @Override public int getContainerSize(){return 6;}
    @Override public int getMaxStackSize(){return 1;}
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override protected void setItems(NonNullList<ItemStack> items){this.items=items;}
    @Override protected Component getDefaultName(){return Component.translatable("container.examplemod.reconstruction_table");}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inventory){return new ReconstructionTableMenu(id,inventory,this);}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return ReconstructionTableMenu.accepts(slot,stack);}
    @Override protected void loadAdditional(ValueInput input){super.loadAdditional(input);items=NonNullList.withSize(6,ItemStack.EMPTY);ContainerHelper.loadAllItems(input,items);pattern=input.read("Pattern",com.mojang.serialization.Codec.INT).filter(v->ReconstructionPattern.byId(v)!=null).orElse(0);}
    @Override protected void saveAdditional(ValueOutput output){super.saveAdditional(output);ContainerHelper.saveAllItems(output,items);output.putInt("Pattern",pattern);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveCustomOnly(registries);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
