package com.example.examplemod;

import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** One authoritative primary-break context, reused by each bounded effect family. */
final class MiningContext {
    final BlockDropsEvent event;final ServerPlayer player;final ServerLevel level;final ItemStack tool;
    final PickaxeModifiers.Bonuses stats;final boolean primary,ore,gem;final long now;final String block;
    boolean silk,soulFurnace;int smelted;
    MiningContext(BlockDropsEvent event,ServerPlayer player){this.event=event;this.player=player;this.level=event.getLevel();this.tool=liveTool(event.getTool(),player);this.stats=PickaxeModifiers.totals(tool);this.primary=!MiningRuntime.secondary(player.getUUID());this.now=level.getGameTime();this.block=MiningConditions.id(event.getState());this.ore=MiningConditions.ore(event.getState());this.gem=MiningConditions.gem(block);}
    /** Native block loot receives a pre-damage tool copy. Persist charge/cooldowns on the matching live held tool. */
    private static ItemStack liveTool(ItemStack snapshot,ServerPlayer player){
        var held=player.getMainHandItem();if(!held.is(snapshot.getItem()))return snapshot;
        var a=held.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY);
        var b=snapshot.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY);
        return !a.isEmpty()&&a.equals(b)?held:snapshot;
    }
    double get(PickaxeTrait trait){return stats.get(trait);}
    int stage(PickaxeTrait trait){return PickaxeModifiers.stage(tool,trait);}
    double rule(PickaxeTrait trait,ModifierTuning.Key key){return ModifierTuning.value(trait,key,stage(trait));}
    int count(PickaxeTrait trait,ModifierTuning.Key key){return (int)rule(trait,key);}
    boolean roll(PickaxeTrait trait){double value=get(trait);return value>0&&player.getRandom().nextDouble()<Math.min(1,value);}
    boolean chance(PickaxeTrait trait){return primary&&roll(trait);}
    boolean perk(PickaxeTrait trait){return perk(trait,count(trait,ModifierTuning.Key.COOLDOWN),count(trait,ModifierTuning.Key.COST));}
    boolean perk(PickaxeTrait trait,int cooldown,int cost){if(!primary||get(trait)<=0||MiningRuntime.charge(tool)<cost)return false;var s=MiningRuntime.read(tool);long last=s.getLongOr("CD_"+trait.name(),Long.MIN_VALUE/2);if(last<=now&&now-last<cooldown)return false;if(!chance(trait))return false;if(!MiningRuntime.cooldown(tool,trait.name(),now,cooldown))return false;return MiningChargeEffects.pay(player,tool,cost);}
    int amount(PickaxeTrait trait){return primary?PickaxeEvents.stochastic(player.getRandom(),get(trait)):0;}
    void charge(int amount){if(primary)MiningRuntime.addCharge(tool,amount);}
}
