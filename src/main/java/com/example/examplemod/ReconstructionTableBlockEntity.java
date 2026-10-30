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
    private int forgeLayout=2;
    public final ForgeProcess process=new ForgeProcess(this);
    public boolean legacyForge(){return getBlockState().is(AncientForgeContent.CORE.get())&&forgeLayout<2;}
    public void currentForge(){forgeLayout=2;setChanged();}
    public ReconstructionTableBlockEntity(BlockPos pos,BlockState state){super(ReconstructionContent.ENTITY.get(),pos,state);}
    public int pattern(){return pattern;}
    public void pattern(int value){pattern=ReconstructionPattern.byId(value)==null?0:value;setChanged();}
    @Override public int getContainerSize(){return 6;}
    @Override public int getMaxStackSize(){return getBlockState().is(AncientForgeContent.CORE.get())?64:1;}
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override protected void setItems(NonNullList<ItemStack> items){this.items=items;}
    @Override protected Component getDefaultName(){return Component.translatable(getBlockState().is(AncientForgeContent.CORE.get())?"block.examplemod.ruined_ancient_forge":"container.examplemod.reconstruction_table");}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inventory){return getBlockState().is(AncientForgeContent.CORE.get())?null:new ReconstructionTableMenu(id,inventory,this);}
    @Override public boolean stillValid(net.minecraft.world.entity.player.Player player){
        return super.stillValid(player)&&(!getBlockState().is(AncientForgeContent.CORE.get())||level!=null&&RuinedForgeStructure.inspect(level,worldPosition,getBlockState().getValue(RuinedAncientForgeBlock.FACING)).complete());
    }
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return getBlockState().is(AncientForgeContent.CORE.get())?!legacyForge()&&!process.heating()&&!process.finished()&&AncientAlloy.accepts(slot,stack):ReconstructionTableMenu.accepts(pattern,slot,stack);}
    @Override public boolean canTakeItem(net.minecraft.world.Container destination,int slot,ItemStack stack){return !getBlockState().is(AncientForgeContent.CORE.get())||!process.heating();}
    @Override public void setChanged(){
        super.setChanged();
        if(level instanceof net.minecraft.server.level.ServerLevel&&getBlockState().is(AncientForgeContent.CORE.get()))level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
    }
    @Override protected void loadAdditional(ValueInput input){super.loadAdditional(input);items=NonNullList.withSize(6,ItemStack.EMPTY);ContainerHelper.loadAllItems(input,items);pattern=input.read("Pattern",com.mojang.serialization.Codec.INT).filter(v->ReconstructionPattern.byId(v)!=null).orElse(0);forgeLayout=input.read("ForgeLayout",com.mojang.serialization.Codec.INT).orElse(1);process.load(input);}
    @Override protected void saveAdditional(ValueOutput output){super.saveAdditional(output);ContainerHelper.saveAllItems(output,items);output.putInt("Pattern",pattern);if(getBlockState().is(AncientForgeContent.CORE.get())){output.putInt("ForgeLayout",forgeLayout);process.save(output);}}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveCustomOnly(registries);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}

