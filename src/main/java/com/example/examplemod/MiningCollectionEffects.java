package com.example.examplemod;

import java.lang.ref.WeakReference;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static com.example.examplemod.PickaxeTrait.*;
import static com.example.examplemod.ModifierTuning.Key.*;

final class MiningCollectionEffects {
    private static final Map<ItemEntity,Long> SHIELDS=new WeakHashMap<>();
    static void protect(net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent event){
        if(!(event.getEntity() instanceof ItemEntity item)||item.level().isClientSide())return;Long expiry=SHIELDS.get(item);
        if(expiry!=null&&item.level().getGameTime()<expiry)event.setInvulnerable(true);
    }
    static void collect(MiningContext c){
        int before=c.event.getDrops().stream().mapToInt(e->e.getItem().getCount()).sum();
        double direct=Math.max(c.get(PHASEBOUND),c.get(DIMENSIONAL_BATTERY));
        if(c.get(ENDER_DISPATCH)>0)direct=Math.max(direct,c.get(ENDER_DISPATCH));
        PickaxeEvents.collect(c.event.getDrops(),c.player,direct);
        if(c.get(ENDER_DISPATCH)>0){
            for(var drop:new ArrayList<>(c.event.getDrops()))if(c.player.getRandom().nextDouble()<c.get(ENDER_DISPATCH)){
                var remainder=drop.getItem().copy();int count=remainder.getCount();remainder=c.player.getEnderChestInventory().addItem(remainder);
                if(remainder.getCount()==count)continue;c.player.getEnderChestInventory().setChanged();
                if(remainder.isEmpty()){c.event.getDrops().remove(drop);drop.discard();}else drop.setItem(remainder);
            }
        }
        int after=c.event.getDrops().stream().mapToInt(e->e.getItem().getCount()).sum();
        if(before>after&&c.chance(DIMENSIONAL_BATTERY))c.charge(c.count(DIMENSIONAL_BATTERY,AMOUNT));
        if(c.primary&&c.chance(SPOILS_SHIELD))for(var drop:c.event.getDrops()){
            if(SHIELDS.size()>=2048)break;SHIELDS.put(drop,c.now+c.count(SPOILS_SHIELD,DURATION));
        }
        if(c.perk(COMPACTOR))compact(c.player,c.stage(COMPACTOR));
    }
    static void remote(ServerPlayer p,ItemStack tool){
        if(!PickaxeProgression.supports(tool)||p.level().getGameTime()%10!=0)return;var stats=PickaxeModifiers.totals(tool);
        double radius=Math.max(stats.get(DIMENSIONAL_BATTERY)>0?ModifierTuning.value(DIMENSIONAL_BATTERY,RADIUS,PickaxeModifiers.stage(tool,DIMENSIONAL_BATTERY)):0,stats.get(ENDER_DISPATCH)>0?ModifierTuning.value(ENDER_DISPATCH,RADIUS,PickaxeModifiers.stage(tool,ENDER_DISPATCH)):0);
        if(radius<=0)return;int budget=64;long now=p.level().getGameTime();String owner=p.getUUID().toString();
        for(var item:p.level().getEntitiesOfClass(ItemEntity.class,p.getBoundingBox().inflate(radius))){
            if(budget--<=0)break;var data=item.getPersistentData();
            if(!item.isAlive()||item.distanceToSqr(p)>radius*radius||!data.getStringOr("examplemod:mined_by","").equals(owner)||data.getLongOr("examplemod:magnet_until",0)<now||item.getTarget()!=null&&!item.getTarget().equals(p.getUUID()))continue;
            double chance=Math.max(stats.get(DIMENSIONAL_BATTERY),stats.get(ENDER_DISPATCH));if(p.getRandom().nextDouble()>=chance)continue;
            var remainder=item.getItem().copy();p.getInventory().add(remainder);
            if(!remainder.isEmpty()&&stats.get(ENDER_DISPATCH)>0&&p.getRandom().nextDouble()<stats.get(ENDER_DISPATCH))remainder=p.getEnderChestInventory().addItem(remainder);
            if(remainder.isEmpty())item.discard();else item.setItem(remainder);
        }
        p.getInventory().setChanged();p.getEnderChestInventory().setChanged();p.inventoryMenu.broadcastChanges();
    }
    private record Compact(Item from,Item to,int input,int output){}
    static void compact(ServerPlayer p,int stage){
        var pairs=new ArrayList<Compact>(List.of(new Compact(Items.RAW_IRON,Items.RAW_IRON_BLOCK,9,1),new Compact(Items.RAW_GOLD,Items.RAW_GOLD_BLOCK,9,1),new Compact(Items.RAW_COPPER,Items.RAW_COPPER_BLOCK,9,1),new Compact(Items.IRON_NUGGET,Items.IRON_INGOT,9,1),new Compact(Items.GOLD_NUGGET,Items.GOLD_INGOT,9,1)));
        if(stage>=1){pairs.add(new Compact(Items.COAL,Items.COAL_BLOCK,9,1));pairs.add(new Compact(Items.REDSTONE,Items.REDSTONE_BLOCK,9,1));pairs.add(new Compact(Items.LAPIS_LAZULI,Items.LAPIS_BLOCK,9,1));}
        if(stage>=2){pairs.add(new Compact(Items.DIAMOND,Items.DIAMOND_BLOCK,9,1));pairs.add(new Compact(Items.EMERALD,Items.EMERALD_BLOCK,9,1));pairs.add(new Compact(Items.QUARTZ,Items.QUARTZ_BLOCK,4,1));pairs.add(new Compact(Items.AMETHYST_SHARD,Items.AMETHYST_BLOCK,4,1));}
        if(stage>=3)pairs.add(new Compact(Items.NETHERITE_INGOT,Items.NETHERITE_BLOCK,9,1));
        int remaining=ModifierTuning.integer(COMPACTOR,BATCH,stage);
        for(var pair:pairs)while(remaining>0&&SharedToolEffects.consume(p,pair.from(),pair.input())){
            remaining--;var output=new ItemStack(pair.to(),pair.output());p.getInventory().add(output);if(!output.isEmpty())p.level().addFreshEntity(new ItemEntity(p.level(),p.getX(),p.getY()+.5,p.getZ(),output));
        }
    }
    static void expiry(ServerTickEvent.Post event){SHIELDS.entrySet().removeIf(e->e.getKey().isRemoved()||e.getKey().level().getGameTime()>=e.getValue());}
    static void stop(){SHIELDS.clear();}
    private MiningCollectionEffects(){}
}
