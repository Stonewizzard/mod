package com.example.examplemod;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class AncientAnvilBlockEntity extends BlockEntity {
    private NonNullList<ItemStack> items=NonNullList.withSize(AnvilSlot.values().length+1,ItemStack.EMPTY);
    private boolean tierSixBuild;
    public boolean tierSixBuild(){return tierSixBuild;}
    public long energyUntil,energyStarted;public int energyColor=0xffffff;
    int work;String workRecipe="";long lastStrike=-100,lastFoundationPlacement=-100;
    public AncientAnvilBlockEntity(BlockPos p,BlockState s){super(AncientAnvilContent.ENTITY.get(),p,s);}
    public ItemStack input(AnvilSlot slot){return items.get(slot.ordinal());}
    public ItemStack output(){return items.get(AnvilSlot.values().length);}
    public int work(){return work;}
    public int inputCount(){return (int)Arrays.stream(AnvilSlot.values()).filter(s->!input(s).isEmpty()).count();}
    public void placeInput(AnvilSlot slot,ItemStack stack){items.set(slot.ordinal(),stack.copyWithCount(1));changedInputs();}
    public ItemStack takeInput(AnvilSlot slot){var stack=input(slot);items.set(slot.ordinal(),ItemStack.EMPTY);if(!stack.isEmpty())changedInputs();return stack;}
    public ItemStack takeOutput(){var stack=output();items.set(AnvilSlot.values().length,ItemStack.EMPTY);sync();return stack;}
    private void changedInputs(){work=0;workRecipe="";sync();}
    void finish(AnvilRecipes.Recipe recipe){
        for(var slot:recipe.inputs().keySet())items.set(slot.ordinal(),ItemStack.EMPTY);
        items.set(AnvilSlot.values().length,recipe.result().copy());work=0;workRecipe="";sync();
    }
    public void sync(){setChanged();if(level instanceof ServerLevel)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);}
    @Override protected void loadAdditional(ValueInput input){super.loadAdditional(input);tierSixBuild=input.read("AnvilTierSixBuild",com.mojang.serialization.Codec.BOOL).orElse(false);items=NonNullList.withSize(AnvilSlot.values().length+1,ItemStack.EMPTY);ContainerHelper.loadAllItems(input,items);work=Math.max(0,input.read("ForgeWork",com.mojang.serialization.Codec.INT).orElse(0));workRecipe=input.read("ForgeRecipe",com.mojang.serialization.Codec.STRING).orElse("");lastStrike=-100;energyStarted=input.read("AnvilEnergyStarted",com.mojang.serialization.Codec.LONG).orElse(0L);energyUntil=input.read("AnvilEnergyUntil",com.mojang.serialization.Codec.LONG).orElse(0L);energyColor=input.read("AnvilEnergyColor",com.mojang.serialization.Codec.INT).orElse(0xffffff);}
    @Override protected void saveAdditional(ValueOutput output){super.saveAdditional(output);output.putBoolean("AnvilTierSixBuild",tierSixBuild);ContainerHelper.saveAllItems(output,items);output.putInt("ForgeWork",work);output.putString("ForgeRecipe",workRecipe);output.putLong("AnvilEnergyUntil",energyUntil);output.putLong("AnvilEnergyStarted",energyStarted);output.putInt("AnvilEnergyColor",energyColor);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveCustomOnly(registries);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){
        if(level instanceof ServerLevel){var removed=items;items=NonNullList.withSize(AnvilSlot.values().length+1,ItemStack.EMPTY);for(var stack:removed)if(!stack.isEmpty())Containers.dropItemStack(level,pos.getX()+.5,pos.getY()+1.5,pos.getZ()+.5,stack);}
        super.preRemoveSideEffects(pos,state);
    }
    public static List<BlockPos> missing(Level level,BlockPos core){
        var result=new ArrayList<BlockPos>();
        var state=level.getBlockState(core);var facing=state.hasProperty(AncientAnvilBlock.FACING)?state.getValue(AncientAnvilBlock.FACING):Direction.NORTH;
        for(var p:AncientAnvilStructure.foundation(core,facing))if(!level.hasChunkAt(p)||!level.getWorldBorder().isWithinBounds(p)||!level.getBlockState(p).is(AncientAnvilContent.FOUNDATION)||level.getBlockEntity(p)!=null)result.add(p);
        return List.copyOf(result);
    }
    public static void refresh(ServerLevel level,BlockPos pos,BlockState state){
        if(!state.is(AncientAnvilContent.ANVIL.get()))return;
        AncientAnvilStructure.form(level,pos);
        var current=level.getBlockState(pos);var front=current.getValue(AncientAnvilBlock.FACING);
        int tier=missing(level,pos).isEmpty()?(AnvilBasins.inspect(level,pos,front).complete()?2:1):0;
        if(tier==2)tier=AnvilStages.tier(level,pos,front,tier);
        if(current.getValue(AncientAnvilBlock.TIER)!=tier)level.setBlock(pos,current.setValue(AncientAnvilBlock.TIER,tier),3);
        // Remember construction intent separately; only the actual structure grants crafting tiers.
        if(level.getBlockEntity(pos) instanceof AncientAnvilBlockEntity be&&!be.tierSixBuild
                &&(current.getValue(AncientAnvilBlock.TIER)>=5||tier>=5||AnvilTierSix.started(level,pos,front))){
            be.tierSixBuild=true;be.sync();
        }
    }
    public static void tick(Level l,BlockPos p,BlockState s,AncientAnvilBlockEntity be){if(l instanceof ServerLevel server&&l.getGameTime()%10==0)refresh(server,p,s);}
}
