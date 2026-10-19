package com.example.examplemod;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import static com.example.examplemod.PickaxeTrait.*;

/** Normal native mining/drop/pickup paths, restricted to reconstructed pickaxes. */
public final class PickaxeEvents {
    private static final String MAGNET_OWNER="examplemod:mined_by",MAGNET_UNTIL="examplemod:magnet_until";
    public static void register(){
        NeoForge.EVENT_BUS.addListener(PickaxeEvents::speed);
        NeoForge.EVENT_BUS.addListener(PickaxeEvents::drops);
        NeoForge.EVENT_BUS.addListener(PickaxeEvents::tick);
        NeoForge.EVENT_BUS.addListener(SharedToolEffects::anvil);
        NeoForge.EVENT_BUS.addListener(SharedToolEffects::xp);
        NeoForge.EVENT_BUS.addListener(MiningCollectionEffects::expiry);
        NeoForge.EVENT_BUS.addListener(MiningCollectionEffects::protect);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppingEvent event)->MiningCollectionEffects.stop());
    }
    public static void speed(PlayerEvent.BreakSpeed event){
        var player=event.getEntity();var held=player.getMainHandItem();if(!held.is(ArchaeologyContent.PICKAXE.get())||event.getState().requiresCorrectToolForDrops()&&!held.isCorrectToolForDrops(event.getState()))return;
        var stats=PickaxeModifiers.totals(held);var pos=event.getPosition().orElse(player.blockPosition());
        event.setNewSpeed((float)(event.getNewSpeed()*(1+MiningConditions.extra(player,pos,event.getState(),held,stats))*speedMultiplier(stats,player.isEyeInFluid(FluidTags.WATER),player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED),player.onGround())));
    }
    public static double speedMultiplier(PickaxeModifiers.Bonuses stats,boolean underwater,double submergedMultiplier,boolean grounded){
        double factor=1+stats.get(SPEED);
        if(underwater&&submergedMultiplier>0&&submergedMultiplier<1)factor*=1+(1/submergedMultiplier-1)*stats.get(AQUATIC);
        if(!grounded)factor*=1+4*stats.get(AIRBORNE);
        return factor;
    }
    /** Fractional levels and XP are stochastic-rounded instead of rounded away at low quality. */
    public static int stochastic(RandomSource random,double value){
        value=Math.clamp(value,0,10000);int whole=(int)Math.floor(value);double fraction=value-whole;
        return whole+(fraction>0&&random.nextDouble()<fraction?1:0);
    }
    public static void drops(BlockDropsEvent event){
        var tool=event.getTool();if(!(event.getBreaker() instanceof ServerPlayer player)||!tool.is(ArchaeologyContent.PICKAXE.get()))return;
        if(event.getState().requiresCorrectToolForDrops()&&!tool.isCorrectToolForDrops(event.getState()))return;
        var c=new MiningContext(event,player);var level=event.getLevel();var random=level.getRandom();var stats=c.stats;
        var lookup=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var silk=lookup.getOrThrow(Enchantments.SILK_TOUCH);var fortune=lookup.getOrThrow(Enchantments.FORTUNE);
        boolean silkActive=EnchantmentHelper.getItemEnchantmentLevel(silk,tool)>0;
        var virtual=tool.copy();boolean changed=false;
        // Ender Chest loot has no stored inventory; other block entities can have dynamic drops.
        if((!event.getState().hasBlockEntity()||event.getState().is(Blocks.ENDER_CHEST))&&tool.isCorrectToolForDrops(event.getState())){
            if(!silkActive&&MiningLootEffects.silk(c)>0&&random.nextDouble()<MiningLootEffects.silk(c)){
                virtual.enchant(silk,1);silkActive=true;changed=true;
            }
            if(!silkActive){
                int base=EnchantmentHelper.getItemEnchantmentLevel(fortune,tool);
                int target=Math.max(base,Math.min(3,base+stochastic(random,MiningLootEffects.fortune(c))));
                if(target>base){virtual.enchant(fortune,target);changed=true;}
            }
            if(changed){
                var replacements=Block.getDrops(event.getState(),level,event.getPos(),event.getBlockEntity(),player,virtual);
                event.getDrops().clear();for(var stack:replacements)if(!stack.isEmpty())event.getDrops().add(new ItemEntity(level,event.getPos().getX()+.5,event.getPos().getY()+.5,event.getPos().getZ()+.5,stack));
            }
        }
        c.silk=silkActive;double smeltingXp=0;double smeltChance=silkActive?0:MiningLootEffects.smeltChance(c);
        if(!silkActive&&smeltChance>0&&!event.getState().hasBlockEntity()){
            for(var drop:new ArrayList<>(event.getDrops())){
                if(random.nextDouble()>=smeltChance)continue;
                var inputStack=drop.getItem().copy();inputStack.setCount(1);var input=new SingleRecipeInput(inputStack);
                var recipe=level.getServer().getRecipeManager().getRecipeFor(RecipeType.SMELTING,input,level);if(recipe.isEmpty())continue;
                var result=recipe.get().value().assemble(input);if(result.isEmpty())continue;
                int count=Math.min(4096,drop.getItem().getCount()*result.getCount());smeltingXp+=recipe.get().value().experience()*drop.getItem().getCount();
                event.getDrops().remove(drop);c.smelted++;
                while(count>0){int size=Math.min(count,result.getMaxStackSize());var stack=result.copy();stack.setCount(size);event.getDrops().add(new ItemEntity(level,drop.getX(),drop.getY(),drop.getZ(),stack));count-=size;}
            }
        }
        MiningLootEffects.bonus(c);
        MiningLootEffects.experience(c,smeltingXp);
        MiningChargeEffects.breaks(c);
        MiningCollectionEffects.collect(c);
        for(var drop:event.getDrops())markMinedDrop(drop,player);
        MiningStatusEffects.apply(c);
        MiningPlacementEffects.apply(c);
        MiningAreaEffects.apply(c);
    }
    /** Only this mining event's final drops qualify; full inventories leave the remainder in the world. */
    static void collect(List<ItemEntity> drops,ServerPlayer player,double chance){
        chance=Math.clamp(chance,0,1);if(chance<=0)return;boolean changed=false;
        for(var drop:new ArrayList<>(drops)){
            if(player.getRandom().nextDouble()>=chance)continue;
            var remainder=drop.getItem().copy();int before=remainder.getCount();
            player.getInventory().add(remainder);
            if(remainder.getCount()==before)continue;
            changed=true;
            if(remainder.isEmpty()){drops.remove(drop);drop.discard();}else drop.setItem(remainder);
        }
        if(changed){player.getInventory().setChanged();player.inventoryMenu.broadcastChanges();}
    }
    public static void markMinedDrop(ItemEntity item,ServerPlayer player){
        var data=item.getPersistentData();data.putString(MAGNET_OWNER,player.getUUID().toString());data.putLong(MAGNET_UNTIL,player.level().getGameTime()+1200);
    }
    public static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        boolean changed=false;
        for(int slot=0;slot<player.getInventory().getContainerSize();slot++){var stack=player.getInventory().getItem(slot);changed|=CreativePickaxeParts.materialize(stack,player.getRandom());changed|=PickaxeTooltips.refresh(stack);}
        changed|=CreativePickaxeParts.materialize(player.getOffhandItem(),player.getRandom());changed|=PickaxeTooltips.refresh(player.getOffhandItem());
        changed|=CreativePickaxeParts.materialize(player.containerMenu.getCarried(),player.getRandom());changed|=PickaxeTooltips.refresh(player.containerMenu.getCarried());
        if(player.containerMenu!=player.inventoryMenu)for(var slot:player.containerMenu.slots)if(PickaxeTooltips.refresh(slot.getItem())){slot.setChanged();changed=true;}
        if(changed){player.getInventory().setChanged();player.inventoryMenu.broadcastChanges();if(player.containerMenu!=player.inventoryMenu)player.containerMenu.broadcastChanges();}
        SharedToolEffects.tick(player,player.getMainHandItem());
        MiningChargeEffects.movement(player,player.getMainHandItem());
        MiningStatusEffects.tick(player,player.getMainHandItem());
        if(player.level().getGameTime()%2!=0)return;
        double radius=PickaxeModifiers.totals(player.getMainHandItem()).get(MAGNET);if(radius<=0)return;
        attract(player,radius);
    }
    public static void attract(ServerPlayer player,double radius){
        radius=Math.clamp(radius,0,12);if(radius<=0)return;
        String owner=player.getUUID().toString();long now=player.level().getGameTime();var target=new Vec3(player.getX(),player.getY()+.7,player.getZ());
        for(var item:player.level().getEntitiesOfClass(ItemEntity.class,player.getBoundingBox().inflate(radius))){
            var data=item.getPersistentData();if(!item.isAlive()||!data.getStringOr(MAGNET_OWNER,"").equals(owner)||data.getLongOr(MAGNET_UNTIL,0)<now||(item.getTarget()!=null&&!item.getTarget().equals(player.getUUID())))continue;
            var delta=target.subtract(item.position());if(delta.lengthSqr()>radius*radius||delta.lengthSqr()<.01)continue;
            item.setNoPickUpDelay();item.setDeltaMovement(item.getDeltaMovement().scale(.45).add(delta.normalize().scale(.22)));
        }
    }
    private PickaxeEvents(){}
}
