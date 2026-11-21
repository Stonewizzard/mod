package com.example.examplemod;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class AncientAnvilBlockEntity extends BlockEntity {
    private NonNullList<ItemStack> items=NonNullList.withSize(AnvilSlot.values().length+1,ItemStack.EMPTY);
    private boolean tierSixBuild;
    private int storedExperience;
    double xpCredit,wearCredit;String planSignature="",lastCompleted="";int familiarStreak,alternateStreak,batchCursor;
    private long transferTick=-1;private int imported,exported;
    private java.util.List<ItemStack> overflow=new java.util.ArrayList<>();
    public int transferBudget(boolean incoming,int requested,boolean simulate){
        if(level==null)return 0;long now=level.getGameTime();if(now!=transferTick){transferTick=now;imported=exported=0;}
        int cap=40*(1+ForgeRunes.counts(this).getOrDefault(ForgeRune.CONDUIT,0));
        int n=Math.min(Math.max(0,requested),Math.max(0,cap-(incoming?imported:exported)));
        if(!simulate){if(incoming)imported+=n;else exported+=n;}return n;
    }
    int[] transferSnapshot(){return new int[]{storedExperience,imported,exported};}
    void restoreTransfer(int[] s){storedExperience=s[0];imported=s[1];exported=s[2];}
    public void takeOutputAmount(int amount){var stack=output();stack.shrink(Math.min(stack.getCount(),amount));if(stack.isEmpty()&&!overflow.isEmpty())items.set(AnvilSlot.values().length,overflow.removeFirst());sync();}
    public void queueOutput(ItemStack stack){if(stack.isEmpty())return;while(!stack.isEmpty()){var part=stack.copyWithCount(Math.min(stack.getCount(),stack.getMaxStackSize()));if(output().isEmpty())items.set(AnvilSlot.values().length,part);else overflow.add(part);stack.shrink(part.getCount());}}

    long lastExperienceTransfer=-100;
    public boolean tierSixBuild(){return tierSixBuild;}
    public long energyUntil,energyStarted;public int energyColor=0xffffff;
    int work;String workRecipe="";long lastStrike=-100,lastFoundationPlacement=-100;
    public AncientAnvilBlockEntity(BlockPos p,BlockState s){super(AncientAnvilContent.ENTITY.get(),p,s);}
    public ItemStack input(AnvilSlot slot){return items.get(slot.ordinal());}
    public ItemStack output(){return items.get(AnvilSlot.values().length);}
    public int work(){return work;}
    public int storedExperience(){return storedExperience;}
    public int experienceCapacity(){return (int)Math.min(Integer.MAX_VALUE,(long)experienceCapacity(getBlockState().getValue(AncientAnvilBlock.TIER))+1000L*ForgeRunes.counts(this).getOrDefault(ForgeRune.STORAGE,0));}
    public static int experienceCapacity(int tier){return switch(Math.clamp(tier,0,6)){case 0->250;case 1->1000;case 2->2500;case 3->5000;case 4->10000;case 5->25000;default->50000;};}
    /** Downgrading pauses deposits above the new capacity; existing XP is never truncated. */
    public int insertExperience(int points,boolean simulate){
        int accepted=Math.min(Math.max(0,points),Math.max(0,experienceCapacity()-storedExperience));
        if(!simulate&&accepted>0){storedExperience+=accepted;sync();}return accepted;
    }
    public int extractExperience(int points,boolean simulate){
        int extracted=Math.min(Math.max(0,points),storedExperience);
        if(!simulate&&extracted>0){storedExperience-=extracted;sync();}return extracted;
    }
    /** Check the entire price before spending either wallet. Creative spends neither. */
    public boolean payExperience(net.minecraft.world.entity.player.Player player,int points){
        if(points<0)throw new IllegalArgumentException("XP cost cannot be negative");
        if(player.getAbilities().instabuild||points==0)return true;
        int fromStore=Math.min(storedExperience,points),fromPlayer=points-fromStore;
        if(!AnvilExperience.spend(player,fromPlayer))return false;
        extractExperience(fromStore,false);return true;
    }
    public int depositExperience(net.minecraft.world.entity.player.Player player,int limit){
        if(player.getAbilities().instabuild||player.isSpectator())return 0;
        int amount=insertExperience(Math.min(Math.max(0,limit),AnvilExperience.available(player)),true);
        if(amount>0&&AnvilExperience.spend(player,amount))return insertExperience(amount,false);
        return 0;
    }
    public int inputCount(){return (int)Arrays.stream(AnvilSlot.values()).filter(s->!input(s).isEmpty()).count();}
    public void placeInput(AnvilSlot slot,ItemStack stack){items.set(slot.ordinal(),stack.copyWithCount(1));changedInputs();}
    public ItemStack takeInput(AnvilSlot slot){var stack=input(slot);items.set(slot.ordinal(),ItemStack.EMPTY);if(!stack.isEmpty())changedInputs();return stack;}
    public ItemStack takeOutput(){var stack=output();items.set(AnvilSlot.values().length,overflow.isEmpty()?ItemStack.EMPTY:overflow.removeFirst());sync();return stack;}
    private void changedInputs(){work=0;workRecipe="";planSignature="";sync();}
    void finish(AnvilRecipes.Recipe recipe){
        var remainders=new ArrayList<ItemStack>();
        for(var slot:recipe.inputs().keySet()){var input=items.get(slot.ordinal());var remainder=input.getItem().getCraftingRemainder();if(remainder!=null)remainders.add(remainder.create());items.set(slot.ordinal(),ItemStack.EMPTY);}
        var extras=ForgeRuneCrafting.expansions(this);for(int i=0;i<recipe.extras().size();i++){var stack=extras.get(i).removeItem(0,1);var rem=stack.getItem().getCraftingRemainder();if(rem!=null)remainders.add(rem.create());}
        var counts=ForgeRunes.counts(this);var result=recipe.result();
        if(recipe.mutationItem()!=Items.AIR&&level.getRandom().nextDouble()<ForgeRuneMath.mutationChance(counts.getOrDefault(ForgeRune.MUTATION,0)))result=new ItemStack(recipe.mutationItem(),recipe.mutationCount());
        int mastery=recipe.bonus()?counts.getOrDefault(ForgeRune.MASTER_CRAFTSMANSHIP,0):0;
        int batches=1+mastery/100+(level.getRandom().nextInt(100)<mastery%100?1:0);
        for(int i=0;i<batches;i++)queueOutput(result.copy());
        for(var rem:remainders)queueOutput(rem);
        familiarStreak=lastCompleted.equals(recipe.id().toString())?familiarStreak+1:1;
        alternateStreak=!lastCompleted.isEmpty()&&!lastCompleted.equals(recipe.id().toString())?alternateStreak+1:0;
        lastCompleted=recipe.id().toString();work=0;workRecipe="";planSignature="";sync();
    }
    public void sync(){setChanged();if(level instanceof ServerLevel)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);}
    @Override protected void loadAdditional(ValueInput input){super.loadAdditional(input);xpCredit=input.read("RuneXpCredit",com.mojang.serialization.Codec.DOUBLE).orElse(0d);wearCredit=input.read("RuneWearCredit",com.mojang.serialization.Codec.DOUBLE).orElse(0d);lastCompleted=input.read("RuneLastRecipe",com.mojang.serialization.Codec.STRING).orElse("");planSignature=input.read("RunePlan",com.mojang.serialization.Codec.STRING).orElse("");familiarStreak=input.read("RuneFamiliar",com.mojang.serialization.Codec.INT).orElse(0);alternateStreak=input.read("RuneAlternate",com.mojang.serialization.Codec.INT).orElse(0);batchCursor=input.read("RuneBatchCursor",com.mojang.serialization.Codec.INT).orElse(0);overflow=new ArrayList<>(input.read("RuneOverflow",ItemStack.CODEC.listOf()).orElse(List.of()));storedExperience=Math.max(0,input.read("StoredExperience",com.mojang.serialization.Codec.INT).orElse(0));lastExperienceTransfer=-100;tierSixBuild=input.read("AnvilTierSixBuild",com.mojang.serialization.Codec.BOOL).orElse(false);items=NonNullList.withSize(AnvilSlot.values().length+1,ItemStack.EMPTY);ContainerHelper.loadAllItems(input,items);work=Math.max(0,input.read("ForgeWork",com.mojang.serialization.Codec.INT).orElse(0));workRecipe=input.read("ForgeRecipe",com.mojang.serialization.Codec.STRING).orElse("");lastStrike=-100;energyStarted=input.read("AnvilEnergyStarted",com.mojang.serialization.Codec.LONG).orElse(0L);energyUntil=input.read("AnvilEnergyUntil",com.mojang.serialization.Codec.LONG).orElse(0L);energyColor=input.read("AnvilEnergyColor",com.mojang.serialization.Codec.INT).orElse(0xffffff);}
    @Override protected void saveAdditional(ValueOutput output){super.saveAdditional(output);output.putDouble("RuneXpCredit",xpCredit);output.putDouble("RuneWearCredit",wearCredit);output.putString("RuneLastRecipe",lastCompleted);output.putString("RunePlan",planSignature);output.putInt("RuneFamiliar",familiarStreak);output.putInt("RuneAlternate",alternateStreak);output.putInt("RuneBatchCursor",batchCursor);output.store("RuneOverflow",ItemStack.CODEC.listOf(),overflow);output.putInt("StoredExperience",storedExperience);output.putBoolean("AnvilTierSixBuild",tierSixBuild);ContainerHelper.saveAllItems(output,items);output.putInt("ForgeWork",work);output.putString("ForgeRecipe",workRecipe);output.putLong("AnvilEnergyUntil",energyUntil);output.putLong("AnvilEnergyStarted",energyStarted);output.putInt("AnvilEnergyColor",energyColor);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveCustomOnly(registries);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){
        if(level instanceof ServerLevel server){int xp=storedExperience;storedExperience=0;if(xp>0)net.minecraft.world.entity.ExperienceOrb.award(server,net.minecraft.world.phys.Vec3.atCenterOf(pos),xp);for(var stack:overflow)Containers.dropItemStack(level,pos.getX()+.5,pos.getY()+1.5,pos.getZ()+.5,stack);overflow.clear();var removed=items;items=NonNullList.withSize(AnvilSlot.values().length+1,ItemStack.EMPTY);for(var stack:removed)if(!stack.isEmpty())Containers.dropItemStack(level,pos.getX()+.5,pos.getY()+1.5,pos.getZ()+.5,stack);}
        super.preRemoveSideEffects(pos,state);
    }
    public static List<BlockPos> missing(Level level,BlockPos core){
        var result=new ArrayList<BlockPos>();
        var state=level.getBlockState(core);var facing=state.hasProperty(AncientAnvilBlock.FACING)?state.getValue(AncientAnvilBlock.FACING):Direction.NORTH;
        for(var p:AncientAnvilStructure.foundation(core,facing))if(!level.hasChunkAt(p)||!level.getWorldBorder().isWithinBounds(p)||!level.getBlockState(p).is(AncientAnvilContent.FOUNDATION)||!ForgeRunes.allowedEntity(level,p))result.add(p);
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
    public static void tick(Level l,BlockPos p,BlockState s,AncientAnvilBlockEntity be){if(l instanceof ServerLevel server&&l.getGameTime()%10==0){refresh(server,p,s);ForgeRuneCrafting.tick(be);}}
}
