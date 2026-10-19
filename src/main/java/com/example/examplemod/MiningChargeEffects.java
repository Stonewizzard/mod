package com.example.examplemod;

import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import static com.example.examplemod.PickaxeTrait.*;

/** Charge generators and consumers run once per primary break; charge cannot exceed 100. */
final class MiningChargeEffects {
    static void breaks(MiningContext c){
        if(!c.primary)return;
        c.charge(c.amount(CHARGE_BATTERY));if(c.gem)c.charge(c.amount(GEM_BATTERY));
        if(c.smelted>0)c.charge(c.amount(CINDER_CHAIN));
        if(c.get(MAGMA_RESERVOIR)>0&&MiningConditions.lava(c.level,c.event.getPos()))c.charge(c.amount(MAGMA_RESERVOIR));
        var state=MiningRuntime.read(c.tool);
        if(MiningRuntime.chain(c.tool,c.now)>0&&state.getBooleanOr("LastOre",c.ore)!=c.ore&&c.chance(RECIPROCAL_CHAIN))c.charge(1+c.stage(RECIPROCAL_CHAIN));
        if(c.perk(OVERCHARGE,200,4))c.player.addEffect(new MobEffectInstance(MobEffects.HASTE,(2+c.stage(OVERCHARGE))*20,0));
        if(c.tool.isDamaged()&&c.perk(RESTORATIVE_DISCHARGE,200,4))MiningRuntime.repair(c.tool,2+2*c.stage(RESTORATIVE_DISCHARGE));
        if(c.get(AURA_DISCHARGE)>0){
            int stage=c.stage(AURA_DISCHARGE);var allies=c.level.players().stream().filter(p->p!=c.player&&c.player.isAlliedTo(p)&&p.distanceToSqr(c.player)<=Math.pow(3+stage,2)).toList();
            if(!allies.isEmpty()&&c.perk(AURA_DISCHARGE,200,6))for(var p:allies)p.addEffect(new MobEffectInstance(MobEffects.HASTE,(2+stage)*20,0));
        }
        if(c.get(ENTANGLEMENT)>0){
            int stage=c.stage(ENTANGLEMENT);
            for(var p:c.level.players())if(p!=c.player&&c.player.isAlliedTo(p)&&p.distanceToSqr(c.player)<=Math.pow(3+stage,2)&&PickaxeProgression.supports(p.getMainHandItem())&&MiningRuntime.charge(p.getMainHandItem())<100){
                if(c.perk(ENTANGLEMENT,200,2))MiningRuntime.addCharge(p.getMainHandItem(),1);break;
            }
        }
        MiningRuntime.noteBreak(c.tool,c.now,c.block,c.ore);
    }
    static void movement(ServerPlayer p,ItemStack tool){
        if(!PickaxeProgression.supports(tool)||p.level().getGameTime()%20!=0)return;var stats=PickaxeModifiers.totals(tool);if(stats.get(KINETIC_BATTERY)<=0)return;
        long now=p.level().getGameTime();var state=MiningRuntime.read(tool);String dimension=p.level().dimension().identifier().toString();
        if(!state.getStringOr("MoveDimension","").equals(dimension)){MiningRuntime.edit(tool,s->{s.putString("MoveDimension",dimension);s.putDouble("MoveX",p.getX());s.putDouble("MoveZ",p.getZ());});return;}
        double dx=p.getX()-state.getDoubleOr("MoveX",p.getX()),dz=p.getZ()-state.getDoubleOr("MoveZ",p.getZ());
        if(dx*dx+dz*dz>=1&&p.getRandom().nextDouble()<stats.get(KINETIC_BATTERY)&&MiningRuntime.cooldown(tool,"kinetic",now,60)){
            MiningRuntime.addCharge(tool,1+PickaxeModifiers.stage(tool,KINETIC_BATTERY));MiningRuntime.edit(tool,s->{s.putDouble("MoveX",p.getX());s.putDouble("MoveZ",p.getZ());});
        }
    }
    private MiningChargeEffects(){}
}
