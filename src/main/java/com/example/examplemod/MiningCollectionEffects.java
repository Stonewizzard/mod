package com.example.examplemod;

import java.lang.ref.WeakReference;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static com.example.examplemod.PickaxeTrait.*;

final class MiningCollectionEffects {
    private record Shield(WeakReference<ItemEntity> item,long expiry,boolean previous){}
    private static final List<Shield> SHIELDS=new ArrayList<>();
    static void collect(MiningContext c){
        int before=c.event.getDrops().stream().mapToInt(e->e.getItem().getCount()).sum();
        PickaxeEvents.collect(c.event.getDrops(),c.player,c.get(PHASEBOUND));
        int after=c.event.getDrops().stream().mapToInt(e->e.getItem().getCount()).sum();
        if(before>after&&c.chance(DIMENSIONAL_BATTERY))c.charge(1+c.stage(DIMENSIONAL_BATTERY));
        if(c.get(ENDER_DISPATCH)>0){double chance=c.get(ENDER_DISPATCH)*(.25+.25*c.stage(ENDER_DISPATCH));
            for(var drop:new ArrayList<>(c.event.getDrops()))if(c.player.getRandom().nextDouble()<chance){
                var remainder=drop.getItem().copy();int count=remainder.getCount();
                remainder=c.player.getEnderChestInventory().addItem(remainder);
                if(remainder.getCount()==count)continue;
                c.player.getEnderChestInventory().setChanged();
                if(remainder.isEmpty()){c.event.getDrops().remove(drop);drop.discard();}else drop.setItem(remainder);
            }
        }
        if(c.primary&&c.chance(SPOILS_SHIELD))for(var drop:c.event.getDrops()){
            if(SHIELDS.size()>=2048)break;
            SHIELDS.add(new Shield(new WeakReference<>(drop),c.now+(3+2*c.stage(SPOILS_SHIELD))*20,drop.isInvulnerable()));drop.setInvulnerable(true);
        }
        if(c.primary&&c.perk(COMPACTOR,100,0))compact(c.player,c.stage(COMPACTOR));
    }
    static void compact(ServerPlayer p,int stage){
        var pairs=new ArrayList<Item[]>(List.of(new Item[]{Items.RAW_IRON,Items.RAW_IRON_BLOCK},new Item[]{Items.RAW_GOLD,Items.RAW_GOLD_BLOCK},new Item[]{Items.IRON_NUGGET,Items.IRON_INGOT},new Item[]{Items.GOLD_NUGGET,Items.GOLD_INGOT}));
        if(stage>=1)pairs.add(new Item[]{Items.RAW_COPPER,Items.RAW_COPPER_BLOCK});
        if(stage>=2){pairs.add(new Item[]{Items.COAL,Items.COAL_BLOCK});pairs.add(new Item[]{Items.REDSTONE,Items.REDSTONE_BLOCK});pairs.add(new Item[]{Items.LAPIS_LAZULI,Items.LAPIS_BLOCK});}
        if(stage>=3){pairs.add(new Item[]{Items.DIAMOND,Items.DIAMOND_BLOCK});pairs.add(new Item[]{Items.EMERALD,Items.EMERALD_BLOCK});}
        for(var pair:pairs)if(SharedToolEffects.consume(p,pair[0],9)){var output=new ItemStack(pair[1]);p.getInventory().add(output);if(!output.isEmpty())p.drop(output,false);break;}
    }
    static void expiry(ServerTickEvent.Post event){
        for(var iterator=SHIELDS.iterator();iterator.hasNext();){var shield=iterator.next();var item=shield.item().get();
            if(item==null||item.isRemoved()){iterator.remove();continue;}
            if(item.level().getGameTime()>=shield.expiry()){item.setInvulnerable(shield.previous());iterator.remove();}
        }
    }
    static void stop(){for(var s:SHIELDS){var e=s.item().get();if(e!=null)e.setInvulnerable(s.previous());}SHIELDS.clear();}
    private MiningCollectionEffects(){}
}
