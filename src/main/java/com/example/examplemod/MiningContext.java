package com.example.examplemod;

import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** One authoritative primary-break context, reused by each bounded effect family. */
final class MiningContext {
    final BlockDropsEvent event;final ServerPlayer player;final ServerLevel level;final ItemStack tool;
    final PickaxeModifiers.Bonuses stats;final boolean primary,ore,gem;final long now;final String block;
    boolean silk;int smelted;
    MiningContext(BlockDropsEvent event,ServerPlayer player){this.event=event;this.player=player;this.level=event.getLevel();this.tool=event.getTool();this.stats=PickaxeModifiers.totals(tool);this.primary=!MiningRuntime.secondary(player.getUUID());this.now=level.getGameTime();this.block=MiningConditions.id(event.getState());this.ore=MiningConditions.ore(event.getState());this.gem=MiningConditions.gem(block);}
    double get(PickaxeTrait trait){return stats.get(trait);}
    int stage(PickaxeTrait trait){return PickaxeModifiers.stage(tool,trait);}
    boolean chance(PickaxeTrait trait){double value=get(trait);return primary&&value>0&&player.getRandom().nextDouble()<Math.min(1,value);}
    boolean perk(PickaxeTrait trait,int cooldown,int cost){if(!primary||get(trait)<=0||MiningRuntime.charge(tool)<cost)return false;var s=MiningRuntime.read(tool);long last=s.getLongOr("CD_"+trait.name(),Long.MIN_VALUE/2);if(last<=now&&now-last<cooldown)return false;if(!chance(trait))return false;if(!MiningRuntime.cooldown(tool,trait.name(),now,cooldown))return false;return MiningRuntime.spend(tool,cost);}
    int amount(PickaxeTrait trait){return primary?PickaxeEvents.stochastic(player.getRandom(),get(trait)):0;}
    void charge(int amount){if(primary)MiningRuntime.addCharge(tool,amount);}
}
