package com.example.examplemod;

import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import static com.example.examplemod.PickaxeTrait.*;
import static com.example.examplemod.ModifierTuning.Key.*;

/** A quality-scaled core makes spenders useful alone; specialized generators support combinations. */
final class MiningChargeEffects {
    static void prepare(MiningContext c){if(c.primary)c.charge(PickaxeEvents.stochastic(c.player.getRandom(),PickaxeModifiers.headQuality(c.tool)*ModifierGameplay.CORE_CHARGE_PER_BREAK));}
    static void breaks(MiningContext c){
        if(!c.primary)return;
        c.charge(c.amount(CHARGE_BATTERY));if(c.gem)c.charge(c.amount(GEM_BATTERY));
        if(c.smelted>0)c.charge(c.amount(CINDER_CHAIN));
        if(c.get(MAGMA_RESERVOIR)>0&&MiningConditions.lava(c.level,c.event.getPos()))c.charge(c.amount(MAGMA_RESERVOIR));
        var state=MiningRuntime.read(c.tool);
        if(MiningRuntime.chain(c.tool,c.now)>0&&state.getBooleanOr("LastOre",c.ore)!=c.ore&&c.chance(RECIPROCAL_CHAIN))c.charge(c.count(RECIPROCAL_CHAIN,AMOUNT));
        if(c.perk(OVERCHARGE))c.player.addEffect(new MobEffectInstance(MobEffects.HASTE,c.count(OVERCHARGE,DURATION),c.count(OVERCHARGE,AMPLIFIER)));
        if(c.tool.isDamaged()&&c.perk(RESTORATIVE_DISCHARGE))MiningRuntime.repair(c.tool,c.count(RESTORATIVE_DISCHARGE,AMOUNT));
        if(c.perk(AURA_DISCHARGE)){
            double radius=c.rule(AURA_DISCHARGE,RADIUS);
            for(var p:c.level.players())if((p==c.player||c.player.isAlliedTo(p))&&p.distanceToSqr(c.player)<=radius*radius)p.addEffect(new MobEffectInstance(MobEffects.HASTE,c.count(AURA_DISCHARGE,DURATION),c.count(AURA_DISCHARGE,AMPLIFIER)));
        }
        MiningRuntime.noteBreak(c.tool,c.now,c.block,c.ore);
    }
    /** Refunds never trigger spending/refunds themselves, and can never exceed the cost. */
    static boolean pay(ServerPlayer player,ItemStack tool,int cost){
        if(cost==0)return true;if(!MiningRuntime.spend(tool,cost))return false;
        var stats=PickaxeModifiers.totals(tool);int stage=PickaxeModifiers.stage(tool,ENTANGLEMENT);
        if(stats.get(ENTANGLEMENT)>0&&player.getRandom().nextDouble()<stats.get(ENTANGLEMENT)){
            int refund=Math.min(cost,PickaxeEvents.stochastic(player.getRandom(),cost*ModifierTuning.value(ENTANGLEMENT,MULTIPLIER,stage)));
            MiningRuntime.addCharge(tool,refund);double radius=ModifierTuning.value(ENTANGLEMENT,RADIUS,stage);
            for(var p:player.level().players())if(p!=player&&player.isAlliedTo(p)&&p.distanceToSqr(player)<=radius*radius&&PickaxeProgression.supports(p.getMainHandItem()))MiningRuntime.addCharge(p.getMainHandItem(),refund);
        }return true;
    }
    static void movement(ServerPlayer p,ItemStack tool){
        if(!PickaxeProgression.supports(tool))return;long now=p.level().getGameTime();
        if(now%(int)ModifierGameplay.CORE_CHARGE_INTERVAL==0)MiningRuntime.addCharge(tool,PickaxeEvents.stochastic(p.getRandom(),PickaxeModifiers.headQuality(tool)));
        if(now%20!=0)return;var stats=PickaxeModifiers.totals(tool);if(stats.get(KINETIC_BATTERY)<=0)return;
        int stage=PickaxeModifiers.stage(tool,KINETIC_BATTERY);var state=MiningRuntime.read(tool);String dimension=p.level().dimension().identifier().toString();
        if(!state.getStringOr("MoveDimension","").equals(dimension)){MiningRuntime.edit(tool,s->{s.putString("MoveDimension",dimension);s.putDouble("MoveX",p.getX());s.putDouble("MoveZ",p.getZ());});return;}
        double dx=p.getX()-state.getDoubleOr("MoveX",p.getX()),dz=p.getZ()-state.getDoubleOr("MoveZ",p.getZ());
        if(dx*dx+dz*dz>=1&&p.getRandom().nextDouble()<stats.get(KINETIC_BATTERY)&&MiningRuntime.cooldown(tool,"kinetic",now,ModifierTuning.integer(KINETIC_BATTERY,COOLDOWN,stage))){
            MiningRuntime.addCharge(tool,ModifierTuning.integer(KINETIC_BATTERY,AMOUNT,stage));MiningRuntime.edit(tool,s->{s.putDouble("MoveX",p.getX());s.putDouble("MoveZ",p.getZ());});
        }
    }
    private MiningChargeEffects(){}
}
