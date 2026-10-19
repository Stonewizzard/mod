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

/** Personal buffs, bounded hostile-only pulses, and collision-checked same-dimension mobility. */
final class MiningStatusEffects {
    static void apply(MiningContext c){
        if(!c.primary)return;
        if(c.perk(NIGHT_SIGHT,200,0))buff(c,MobEffects.NIGHT_VISION,200+40*c.stage(NIGHT_SIGHT));
        if(c.ore&&c.perk(MINERS_SPRINT,200,0))buff(c,MobEffects.SPEED,60+20*c.stage(MINERS_SPRINT));
        if(MiningConditions.stone(c.block)&&c.perk(STONE_SKIN,200,0))buff(c,MobEffects.RESISTANCE,60+20*c.stage(STONE_SKIN));
        if(c.gem&&c.perk(ABSORPTION,200,0))buff(c,MobEffects.ABSORPTION,80+20*c.stage(ABSORPTION));
        if(!c.player.onGround()&&c.perk(SOFT_LANDING,200,0))buff(c,MobEffects.SLOW_FALLING,60+20*c.stage(SOFT_LANDING));
        if(c.player.isInWater())c.player.setAirSupply(Math.min(c.player.getMaxAirSupply(),c.player.getAirSupply()+c.amount(OXYGEN)));
        if(c.player.getFoodData().getFoodLevel()<20&&c.perk(HUNGER_SAVER,200,0))c.player.getFoodData().eat(1,0);
        if(c.get(FIRE_WARD)>0&&MiningConditions.lava(c.level,c.event.getPos())&&c.perk(FIRE_WARD,200,0))buff(c,MobEffects.FIRE_RESISTANCE,60+20*c.stage(FIRE_WARD));
        if(MiningConditions.netherStone(c.block)&&c.perk(BASALT_HEART,200,0)){buff(c,MobEffects.RESISTANCE,40+20*c.stage(BASALT_HEART));MiningRuntime.repair(c.tool,1+c.stage(BASALT_HEART));}
        if((c.block.equals("soul_sand")||c.block.equals("soul_soil"))&&c.perk(SOUL_CLEANSE,200,0)){
            int left=1+c.stage(SOUL_CLEANSE);for(var effect:java.util.List.of(MobEffects.POISON,MobEffects.WITHER,MobEffects.SLOWNESS,MobEffects.WEAKNESS))if(c.player.hasEffect(effect)){c.player.removeEffect(effect);if(--left<=0)break;}
        }
        if(c.ore){pulse(c,ECHO_CHAMBER,MobEffects.SLOWNESS,0);pulse(c,QUARRY_MARK,MobEffects.GLOWING,0);pulse(c,REPULSOR,null,0);}
        if(MiningConditions.soft(c.block))pulse(c,DUST_CLOUD,MobEffects.WEAKNESS,0);
        if(c.player.getHealth()<8)pulse(c,FALLING_STAR,null,0);
        pulse(c,GHAST_BREATH,null,5);pulse(c,SHULKER_PULSE,MobEffects.LEVITATION,5);
        if(c.player.isShiftKeyDown()){
            if(c.get(RECOIL_STEP)>0&&c.perk(RECOIL_STEP,200,0)){double distance=1+.5*c.stage(RECOIL_STEP);safeTeleport(c.player,c.player.position().subtract(c.player.getLookAngle().multiply(distance,0,distance)));}
            if(c.get(CHORUS_HOP)>0&&c.perk(CHORUS_HOP,200,4)){double distance=2+c.stage(CHORUS_HOP);safeTeleport(c.player,c.player.position().add(c.player.getLookAngle().multiply(distance,0,distance)));}
        }
    }
    private static void buff(MiningContext c,Holder<MobEffect> effect,int ticks){c.player.addEffect(new MobEffectInstance(effect,ticks,0));}
    private static void pulse(MiningContext c,PickaxeTrait trait,Holder<MobEffect> effect,int cost){
        if(c.get(trait)<=0)return;int stage=c.stage(trait);double radius=3+stage;
        var mobs=c.level.getEntitiesOfClass(Monster.class,c.player.getBoundingBox().inflate(radius),m->m.isAlive()&&m.distanceToSqr(c.player)<=radius*radius);
        if(mobs.isEmpty()||!c.perk(trait,200,cost))return;
        for(var mob:mobs.stream().limit(16).toList()){
            if(effect!=null)mob.addEffect(new MobEffectInstance(effect,(trait==SHULKER_PULSE?1+stage:3+stage)*20,0));
            else{var delta=mob.position().subtract(c.player.position());if(delta.horizontalDistanceSqr()>.01)mob.knockback(.4+.1*stage,-delta.x,-delta.z);}
            if(trait==GHAST_BREATH)mob.igniteForSeconds(2+stage);
        }
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
        }else if(p.getY()<level.getMinY()-2&&state.getStringOr("SafeDimension","").equals(dimension)&&MiningRuntime.charge(tool)>=20&&p.getRandom().nextDouble()<stats.get(VOID_ANCHOR)&&MiningRuntime.cooldown(tool,"void",level.getGameTime(),1200)){
            var target=new Vec3(state.getDoubleOr("SafeX",p.getX()),state.getDoubleOr("SafeY",p.getY()),state.getDoubleOr("SafeZ",p.getZ()));
            // Rescue distance may exceed normal blink range, but both endpoints must be in this dimension.
            var pos=BlockPos.containing(target);
            if(level.hasChunkAt(pos)&&level.getWorldBorder().isWithinBounds(pos)&&level.getBlockState(pos.below()).isSolidRender()&&level.noCollision(p,p.getBoundingBox().move(target.subtract(p.position())))){
                MiningRuntime.spend(tool,20);p.teleportTo(target.x,target.y,target.z);p.fallDistance=0;
            }
        }
    }
    private MiningStatusEffects(){}
}
