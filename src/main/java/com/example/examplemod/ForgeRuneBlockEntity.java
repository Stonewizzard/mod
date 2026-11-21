package com.example.examplemod;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

/** Local inventories belong to their rune and survive removal; no menu is needed. */
public final class ForgeRuneBlockEntity extends BlockEntity implements Container {
    private NonNullList<ItemStack> items=NonNullList.withSize(27,ItemStack.EMPTY);
    public BlockPos owner; // Derived, revalidated for every automation access. Not saved across worlds.
    public String recipeId="",category="";
    private int cursor;
    public ForgeRuneBlockEntity(BlockPos p,BlockState s){super(ForgeRunes.ENTITY.get(),p,s);}
    public ForgeRune kind(){return ((ForgeRuneBlock)getBlockState().getBlock()).kind;}
    public AncientAnvilBlockEntity core(){
        if(level==null||owner==null||!level.hasChunkAt(owner)||!(level.getBlockEntity(owner) instanceof AncientAnvilBlockEntity be))return null;
        return be.getBlockState().getValue(AncientAnvilBlock.TIER)>=kind().tier&&ForgeRunes.sockets(owner,be.getBlockState().getValue(AncientAnvilBlock.FACING),be.getBlockState().getValue(AncientAnvilBlock.TIER)).contains(worldPosition)?be:null;
    }
    public AnvilRecipes.Recipe recipe(){return AnvilRecipes.all().stream().filter(r->r.id().toString().equals(recipeId)).findFirst().orElse(null);}
    public void interact(Player p,InteractionHand hand){
        var held=p.getItemInHand(hand);var be=core();
        if(kind()==ForgeRune.GRAND_PATTERN&&p.isShiftKeyDown()){GrandPatternItem.pack(this,p,hand);return;}
        if((kind()==ForgeRune.BATCHING||kind()==ForgeRune.SPECIALIZATION)&&p.isShiftKeyDown()){
            if(held.isEmpty()){recipeId="";category="";sync();AnvilWork.message(p,"Rune template cleared");return;}
            var options=AnvilRecipes.all().stream().filter(r->r.resultItem()==held.getItem()).toList();
            if(options.isEmpty()){AnvilWork.message(p,"Hold the desired recipe result to select a template");return;}
            int old=-1;for(int i=0;i<options.size();i++)if(options.get(i).id().toString().equals(recipeId))old=i;
            var r=options.get((old+1)%options.size());recipeId=r.id().toString();category=r.category();sync();AnvilWork.message(p,"Selected "+recipeId+" (repeat to cycle variants)");return;
        }
        if(kind()==ForgeRune.EXPANSION){
            if(held.isEmpty()){give(p,removeItem(0,1));return;}
            if(getItem(0).isEmpty()){setItem(0,held.copyWithCount(1));if(!p.getAbilities().instabuild)held.shrink(1);}return;
        }
        if(kind()==ForgeRune.BATCHING||kind()==ForgeRune.COLLECTION){
            if(held.isEmpty()){for(int i=0;i<27;i++)if(!getItem(i).isEmpty()){give(p,removeItem(i,getItem(i).getCount()));break;}return;}
            if(kind()==ForgeRune.BATCHING){var left=insert(held.copy());if(!p.getAbilities().instabuild)held.setCount(left.getCount());}return;
        }
        AnvilWork.message(p,be==null?"Place this rune in a completed Tier "+kind().tier+"+ anvil structure":kind().title()+" | XP: "+be.storedExperience()+" / "+be.experienceCapacity());
    }
    private static void give(Player p,ItemStack s){if(!s.isEmpty()&&!p.getInventory().add(s))Containers.dropItemStack(p.level(),p.getX(),p.getY(),p.getZ(),s);}
    public ItemStack insert(ItemStack offered){var left=offered.copy();for(int i=0;i<27&&!left.isEmpty();i++)if(kind()==ForgeRune.COLLECTION||canPlaceItem(i,left)){
        var old=getItem(i);if(!old.isEmpty()&&!ItemStack.isSameItemSameComponents(old,left))continue;
        int n=Math.min(left.getCount(),left.getMaxStackSize()-old.getCount());if(n<=0)continue;setItem(i,left.copyWithCount(old.getCount()+n));left.shrink(n);
    }return left;}
    @Override public boolean canPlaceItem(int i,ItemStack s){if(kind()!=ForgeRune.BATCHING)return false;var r=recipe();return r!=null&&r.acceptsIngredient(s);}
    @Override public int getContainerSize(){return items.size();}
    @Override public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
    @Override public ItemStack getItem(int i){return items.get(i);}
    @Override public ItemStack removeItem(int i,int n){var out=ContainerHelper.removeItem(items,i,n);if(!out.isEmpty())sync();return out;}
    @Override public ItemStack removeItemNoUpdate(int i){return ContainerHelper.takeItem(items,i);}
    @Override public void setItem(int i,ItemStack s){items.set(i,s);sync();}
    @Override public void clearContent(){items.clear();sync();}
    @Override public boolean stillValid(Player p){return Container.stillValidBlockEntity(this,p);}
    public void sync(){setChanged();if(level instanceof ServerLevel)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);ContainerHelper.saveAllItems(out,items);out.putString("RuneRecipe",recipeId);out.putString("RuneCategory",category);}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);items=NonNullList.withSize(27,ItemStack.EMPTY);ContainerHelper.loadAllItems(in,items);recipeId=in.read("RuneRecipe",com.mojang.serialization.Codec.STRING).orElse("");category=in.read("RuneCategory",com.mojang.serialization.Codec.STRING).orElse("");owner=null;}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveCustomOnly(r);}
    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
    @Override public void preRemoveSideEffects(BlockPos p,BlockState s){if(level instanceof ServerLevel){Containers.dropContents(level,p,this);clearContent();}super.preRemoveSideEffects(p,s);}
    public static void tick(Level l,BlockPos p,BlockState s,ForgeRuneBlockEntity rune){
        if(!(l instanceof ServerLevel server)||l.getGameTime()%10!=0)return;var be=rune.core();if(be==null)return;
        int speed=10*(1+ForgeRunes.counts(be).getOrDefault(ForgeRune.CONDUIT,0));
        if(rune.kind()==ForgeRune.EXTRACTION||rune.kind()==ForgeRune.INPUT)for(var player:server.getEntitiesOfClass(Player.class,new net.minecraft.world.phys.AABB(p.above()).inflate(0,.15,0))){
            if(player.isSpectator()||player.getAbilities().instabuild)continue;int rate=player.isShiftKeyDown()?speed*4:speed;
            if(rune.kind()==ForgeRune.EXTRACTION)be.depositExperience(player,be.transferBudget(true,rate,false));
            else {int n=be.extractExperience(be.transferBudget(false,rate,false),false);player.giveExperiencePoints(n);}
        }
        if(rune.kind()==ForgeRune.BATCHING)rune.pullChest();
    }
    private void pullChest(){
        var r=recipe();if(r==null||!(level.getBlockEntity(worldPosition.above()) instanceof Container chest))return;
        // One real source removal at a time: shared chests cannot be reserved twice by multiple runes.
        for(int n=0;n<chest.getContainerSize();n++){int i=(cursor+n)%chest.getContainerSize();var s=chest.getItem(i);if(s.isEmpty()||!r.acceptsIngredient(s))continue;
            var left=insert(s.copyWithCount(1));if(left.isEmpty()){chest.removeItem(i,1);chest.setChanged();cursor=(i+1)%chest.getContainerSize();return;}}
    }
}
