package com.example.examplemod;

import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import static com.example.examplemod.PickaxeTrait.*;
import static com.example.examplemod.ModifierTuning.Key.*;

/** Personal buffs, bounded hostile-only pulses, and collision-checked same-dimension mobility. */
final class MiningStatusEffects {
    static void apply(MiningContext c){
        if(!c.primary)return;
        if(c.perk(NIGHT_SIGHT))buff(c,NIGHT_SIGHT,MobEffects.NIGHT_VISION);
        if(c.ore&&c.perk(MINERS_SPRINT))buff(c,MINERS_SPRINT,MobEffects.SPEED);
        if(MiningConditions.stone(c.block)&&c.perk(STONE_SKIN))buff(c,STONE_SKIN,MobEffects.RESISTANCE);
        if(c.gem&&c.perk(ABSORPTION)){buff(c,ABSORPTION,MobEffects.ABSORPTION);c.player.setAbsorptionAmount(Math.max(c.player.getAbsorptionAmount(),4*(1+c.count(ABSORPTION,AMPLIFIER))));}
        if(!c.player.onGround()&&c.perk(SOFT_LANDING))buff(c,SOFT_LANDING,MobEffects.SLOW_FALLING);
        if(c.player.isInWater())c.player.setAirSupply(Math.min(c.player.getMaxAirSupply(),c.player.getAirSupply()+c.amount(OXYGEN)));
        if(c.player.getFoodData().getFoodLevel()<20&&c.perk(HUNGER_SAVER))c.player.getFoodData().eat(c.count(HUNGER_SAVER,AMOUNT),.6F);
        if(c.get(FIRE_WARD)>0&&(c.player.isOnFire()||MiningConditions.lava(c.level,c.event.getPos()))&&c.perk(FIRE_WARD))buff(c,FIRE_WARD,MobEffects.FIRE_RESISTANCE);
        if(MiningConditions.netherStone(c.block)&&c.perk(BASALT_HEART)){buff(c,BASALT_HEART,MobEffects.RESISTANCE);MiningRuntime.repair(c.tool,c.count(BASALT_HEART,AMOUNT));}
        if((c.block.equals("soul_sand")||c.block.equals("soul_soil")||MiningConditions.netherStone(c.block))&&c.perk(SOUL_CLEANSE)){
            int left=c.count(SOUL_CLEANSE,AMOUNT);for(var effect:java.util.List.of(MobEffects.POISON,MobEffects.WITHER,MobEffects.SLOWNESS,MobEffects.WEAKNESS))if(c.player.hasEffect(effect)){c.player.removeEffect(effect);if(--left<=0)break;}
            buff(c,SOUL_CLEANSE,MobEffects.REGENERATION);
        }
        if(c.ore){pulse(c,ECHO_CHAMBER,MobEffects.SLOWNESS);pulse(c,QUARRY_MARK,MobEffects.GLOWING);pulse(c,REPULSOR,null);}
        if(MiningConditions.soft(c.block))pulse(c,DUST_CLOUD,MobEffects.WEAKNESS);
        if(c.player.getHealth()<c.player.getMaxHealth()/2)pulse(c,FALLING_STAR,null);
        pulse(c,GHAST_BREATH,null);pulse(c,SHULKER_PULSE,MobEffects.LEVITATION);
    }
    private static void buff(MiningContext c,PickaxeTrait trait,Holder<MobEffect> effect){c.player.addEffect(new MobEffectInstance(effect,c.count(trait,DURATION),c.count(trait,AMPLIFIER)));}
    private static void pulse(MiningContext c,PickaxeTrait trait,Holder<MobEffect> effect){
        if(c.get(trait)<=0)return;double radius=c.rule(trait,RADIUS);
        var mobs=c.level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,c.player.getBoundingBox().inflate(radius),m->m instanceof net.minecraft.world.entity.monster.Enemy&&m.isAlive()&&m.distanceToSqr(c.player)<=radius*radius);
        var projectiles=trait==REPULSOR?c.level.getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class,c.player.getBoundingBox().inflate(radius),p->p.getOwner() instanceof net.minecraft.world.entity.monster.Enemy&&p.distanceToSqr(c.player)<=radius*radius):java.util.List.<net.minecraft.world.entity.projectile.Projectile>of();
        if(mobs.isEmpty()&&projectiles.isEmpty()||!c.perk(trait))return;
        for(var mob:mobs.stream().limit(32).toList()){
            if(effect!=null)mob.addEffect(new MobEffectInstance(effect,c.count(trait,DURATION),c.count(trait,AMPLIFIER)));
            else{var delta=mob.position().subtract(c.player.position());if(delta.horizontalDistanceSqr()>.01)mob.knockback(c.rule(trait,STRENGTH),-delta.x,-delta.z,c.player.damageSources().playerAttack(c.player),0);}
            if(trait==GHAST_BREATH)mob.igniteForSeconds(c.count(trait,DURATION)/20F);
        }
        if(trait==FALLING_STAR)c.player.heal(c.count(trait,AMOUNT));
        for(var projectile:projectiles.stream().limit(32).toList()){
            var away=projectile.position().subtract(c.player.getEyePosition());if(away.lengthSqr()<.01)away=c.player.getLookAngle();
            projectile.setOwner(c.player);projectile.setDeltaMovement(away.normalize().scale(Math.max(.8,projectile.getDeltaMovement().length())));
        }
    }
    /** Deliberate air-use mobility avoids teleporting players automatically during area mining. */
    static boolean use(ServerPlayer player,ItemStack tool){
        var trait=player.isShiftKeyDown()?CHORUS_HOP:RECOIL_STEP;var stats=PickaxeModifiers.totals(tool);if(stats.get(trait)<=0)return false;
        int stage=PickaxeModifiers.stage(tool,trait);double range=ModifierTuning.value(trait,RANGE,stage);var look=player.getLookAngle().multiply(range,0,range);
        var target=trait==RECOIL_STEP?player.position().subtract(look):player.position().add(look);long now=player.level().getGameTime();
        int cost=ModifierTuning.integer(trait,COST,stage),cooldown=ModifierTuning.integer(trait,COOLDOWN,stage);
        long last=MiningRuntime.read(tool).getLongOr("CD_"+trait.name(),Long.MIN_VALUE/2);
        if(MiningRuntime.charge(tool)<cost||last<=now&&now-last<cooldown||player.getRandom().nextDouble()>=stats.get(trait)||!safeTarget(player,target))return false;
        if(!MiningChargeEffects.pay(player,tool,cost))return false;MiningRuntime.cooldown(tool,trait.name(),now,cooldown);return safeTeleport(player,target);
    }
    static boolean safeTarget(ServerPlayer player,Vec3 target){
        var level=player.level();var pos=BlockPos.containing(target);
        return level.hasChunkAt(pos)&&level.getWorldBorder().isWithinBounds(pos)&&level.getBlockState(pos.below()).isSolidRender()&&level.getFluidState(pos).isEmpty()&&level.noCollision(player,player.getBoundingBox().move(target.subtract(player.position())));
    }
    static boolean safeTeleport(ServerPlayer player,Vec3 target){
        if(target.distanceToSqr(player.position())>256)return false;
        var level=player.level();var pos=BlockPos.containing(target);
        if(!level.hasChunkAt(pos)||!level.getWorldBorder().isWithinBounds(pos)||!level.getBlockState(pos.below()).isSolidRender()||!level.getFluidState(pos).isEmpty())return false;
        var box=player.getBoundingBox().move(target.subtract(player.position()));if(!level.noCollision(player,box))return false;
        player.teleportTo(target.x,target.y,target.z);return true;
    }
    static void tick(ServerPlayer p,ItemStack tool){
        if(!PickaxeProgression.supports(tool))return;var stats=PickaxeModifiers.totals(tool);if(stats.get(VOID_ANCHOR)<=0)return;
        var level=p.level();String dimension=level.dimension().identifier().toString();var state=MiningRuntime.read(tool);
        if(p.onGround()&&level.getFluidState(p.blockPosition()).isEmpty()&&level.getBlockState(p.blockPosition().below()).isSolidRender()){
            if(level.getGameTime()%20==0)MiningRuntime.edit(tool,s->{s.putString("SafeDimension",dimension);s.putDouble("SafeX",p.getX());s.putDouble("SafeY",p.getY());s.putDouble("SafeZ",p.getZ());});
            if(state.getBooleanOr("VoidFallTried",false))MiningRuntime.edit(tool,t->t.putBoolean("VoidFallTried",false));
        }else if(p.getY()<level.getMinY()-2&&!state.getBooleanOr("VoidFallTried",false)&&state.getStringOr("SafeDimension","").equals(dimension)){
            MiningRuntime.edit(tool,t->t.putBoolean("VoidFallTried",true));int stage=PickaxeModifiers.stage(tool,VOID_ANCHOR);
            var target=new Vec3(state.getDoubleOr("SafeX",p.getX()),state.getDoubleOr("SafeY",p.getY()),state.getDoubleOr("SafeZ",p.getZ()));
            int cooldown=ModifierTuning.integer(VOID_ANCHOR,COOLDOWN,stage),cost=ModifierTuning.integer(VOID_ANCHOR,COST,stage);
            if(p.getRandom().nextDouble()<stats.get(VOID_ANCHOR)&&safeTarget(p,target)&&MiningRuntime.cooldown(tool,"void",level.getGameTime(),cooldown)){
                if(!MiningChargeEffects.pay(p,tool,cost))tool.setDamageValue(Math.min(tool.getMaxDamage()-1,tool.getDamageValue()+(int)Math.ceil(tool.getMaxDamage()*ModifierTuning.value(VOID_ANCHOR,DAMAGE,stage))));
                p.teleportTo(target.x,target.y,target.z);p.fallDistance=0;
            }
        }
    }
    private MiningStatusEffects(){}
}
