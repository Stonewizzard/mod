package com.example.examplemod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import static com.example.examplemod.PickaxeTrait.*;

/** Only general-purpose rolls are read on non-pickaxe tools. No mining effect leaks into a sword. */
final class SharedToolEffects {
    static int damage(ItemStack stack,int amount,LivingEntity entity){
        if(amount<=0||entity==null||PickaxeModifiers.pattern(stack)==null)return amount;
        double save=PickaxeModifiers.totals(stack).get(UNBREAKING);if(save<=0)return amount;
        int spent=0;for(int i=0;i<Math.min(10000,amount);i++)if(entity.getRandom().nextDouble()>=save)spent++;
        return spent+Math.max(0,amount-10000);
    }
    static boolean consume(ServerPlayer player,Item item,int count){
        int found=0;for(int i=0;i<player.getInventory().getContainerSize();i++){var s=player.getInventory().getItem(i);if(plain(s,item))found+=s.getCount();}
        if(found<count)return false;
        for(int i=0;i<player.getInventory().getContainerSize()&&count>0;i++){var s=player.getInventory().getItem(i);if(!plain(s,item))continue;int take=Math.min(count,s.getCount());s.shrink(take);count-=take;}player.getInventory().setChanged();return true;
    }
    static boolean owns(ServerPlayer player,Item item){for(int i=0;i<player.getInventory().getContainerSize();i++)if(plain(player.getInventory().getItem(i),item))return true;return false;}
    static boolean plain(ItemStack stack,Item item){return stack.is(item)&&ItemStack.isSameItemSameComponents(stack,new ItemStack(item));}
    static void tick(ServerPlayer player,ItemStack tool){
        if(PickaxeModifiers.pattern(tool)==null||!tool.isDamaged())return;
        long now=player.level().getGameTime();var stats=PickaxeModifiers.totals(tool);int repair=0;
        if(now%ModifierTuning.integer(SELF_REPAIR,ModifierTuning.Key.COOLDOWN,PickaxeModifiers.stage(tool,SELF_REPAIR))==0)repair+=PickaxeEvents.stochastic(player.getRandom(),stats.get(SELF_REPAIR));
        if((player.isInWater()||player.level().isRainingAt(player.blockPosition()))&&now%ModifierTuning.integer(RAIN_REPAIR,ModifierTuning.Key.COOLDOWN,PickaxeModifiers.stage(tool,RAIN_REPAIR))==0)repair+=PickaxeEvents.stochastic(player.getRandom(),stats.get(RAIN_REPAIR));
        if(now%ModifierTuning.integer(METAL_MAINTENANCE,ModifierTuning.Key.COOLDOWN,PickaxeModifiers.stage(tool,METAL_MAINTENANCE))==0){
            int metal=PickaxeEvents.stochastic(player.getRandom(),stats.get(METAL_MAINTENANCE));
            if(player.isShiftKeyDown()&&metal>0&&consume(player,Items.IRON_NUGGET,1))repair+=metal;
        }
        MiningRuntime.repair(tool,repair);
    }
    static void anvil(AnvilUpdateEvent event){
        if(event.getOutput().isEmpty())return;double discount=PickaxeModifiers.totals(event.getLeft()).get(ANVIL_THRIFT);if(discount<=0)return;
        if(event.getXpCost()>0)event.setXpCost(Math.max(1,(int)Math.ceil(event.getXpCost()*(1-discount))));
        if(event.getMaterialCost()>0)event.setMaterialCost(Math.max(1,(int)Math.ceil(event.getMaterialCost()*(1-discount))));
    }
    static void xp(PlayerXpEvent.PickupXp event){
        var player=event.getEntity();if(!(player instanceof ServerPlayer))return;var tool=player.getMainHandItem();
        double bonus=PickaxeModifiers.totals(tool).get(MENDING_BOOST);if(bonus>0)MiningRuntime.repair(tool,PickaxeEvents.stochastic(player.getRandom(),Math.min(100,event.getOrb().getValue())*bonus));
    }
    private SharedToolEffects(){}
}
