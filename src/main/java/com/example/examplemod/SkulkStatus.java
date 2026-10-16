package com.example.examplemod;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.EnumSet;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Infection belongs to the original entity: native variants, ownership and equipment survive. */
public final class SkulkStatus {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(
            NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ExampleMod.MODID);
    public static final Supplier<AttachmentType<Integer>> INFECTION = TYPES.register("sculk_infection", () ->
            AttachmentType.builder(() -> 0).serialize(Codec.intRange(0, 2).fieldOf("state"), v -> v != 0)
                    .sync(ByteBufCodecs.VAR_INT).build());
    private static final Identifier SPEED = Identifier.fromNamespaceAndPath(ExampleMod.MODID, "sculk_speed");
    private static final Set<Mob> INSTALLED = Collections.newSetFromMap(new WeakHashMap<>());

    private SkulkStatus() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        NeoForge.EVENT_BUS.addListener(SkulkStatus::tick);
        NeoForge.EVENT_BUS.addListener(SkulkStatus::damage);
        NeoForge.EVENT_BUS.addListener(SkulkStatus::experience);
    }

    public static boolean supports(EntityType<?> type) {
        return type != EntityTypes.ENDER_DRAGON && type != EntityTypes.WITHER && type != EntityTypes.WARDEN
                && (BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace().equals("minecraft")
                    || type == ExampleMod.CRUNCHER.get());
    }

    public static int state(LivingEntity entity) {
        return entity.hasData(INFECTION) ? entity.getData(INFECTION) : 0;
    }

    public static boolean infected(LivingEntity entity) {
        return state(entity) != 0 || entity instanceof SkulkCow || entity instanceof SkulkZombie;
    }

    public static boolean canInfect(Mob mob) {
        return mob.isAlive() && !mob.isInvulnerable() && supports(mob.getType()) && !infected(mob);
    }

    public static void infect(Mob mob) {
        mob.setData(INFECTION, mob instanceof Enemy || mob.getType().getCategory() == MobCategory.MONSTER ? 2 : 1);
        install(mob);
    }

    private static void install(Mob mob) {
        if (!INSTALLED.add(mob)) return;
        if (state(mob) == 2) {
            speed(mob.getAttribute(Attributes.MOVEMENT_SPEED));
            speed(mob.getAttribute(Attributes.FLYING_SPEED));
        } else {
            mob.goalSelector.addGoal(0, new PursuePlayer(mob));
        }
    }

    private static void speed(AttributeInstance attribute) {
        if (attribute != null && !attribute.hasModifier(SPEED)) attribute.addPermanentModifier(
                new AttributeModifier(SPEED, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    public static float damageMultiplier(LivingEntity entity) { return state(entity) == 2 ? 1.5F : 1; }
    public static int experience(LivingEntity entity, int amount) { return state(entity) == 2 ? amount * 2 : amount; }

    private static void damage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker)
            event.setAmount(event.getAmount() * damageMultiplier(attacker));
    }

    private static void experience(LivingExperienceDropEvent event) {
        event.setDroppedExperience(experience(event.getEntity(), event.getDroppedExperience()));
    }

    private static boolean targetable(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    private static void tick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)
                || state(mob) == 0 || !mob.isAlive()) return;
        install(mob); // Reinstall transient goals after loading, without stacking saved modifiers.
        if (mob.isNoAi()) return;
        var target = mob.getTarget();
        if (!(target instanceof Player player) || !targetable(player) || mob.distanceToSqr(player) > 1024) {
            target = level.getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(16), SkulkStatus::targetable)
                    .stream().min(java.util.Comparator.comparingDouble(mob::distanceToSqr)).orElse(null);
            mob.setTarget(target);
        }
        if (target == null) return;
        var brain = mob.getBrain();
        if (brain.checkMemory(MemoryModuleTypes.ATTACK_TARGET, MemoryStatus.REGISTERED))
            brain.setMemory(MemoryModuleTypes.ATTACK_TARGET, target);
        if (state(mob) != 1) return; // Hostile mobs retain their native ranged/explosion attacks.
        if (brain.checkMemory(MemoryModuleTypes.WALK_TARGET, MemoryStatus.REGISTERED))
            brain.setMemory(MemoryModuleTypes.WALK_TARGET, new WalkTarget(target, 1.2F, 1));
        mob.getLookControl().setLookAt(target, 30, 30);
        if (mob.tickCount % 10 == 0) mob.getNavigation().moveTo(target, 1.2);
        mob.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.2);
        if (mob instanceof Bat bat) {
            bat.setResting(false);
            var direction = target.getEyePosition().subtract(mob.position());
            mob.setDeltaMovement(mob.getDeltaMovement().scale(0.75).add(direction.normalize().scale(0.09)));
        }
        if (mob.tickCount % 20 == 0 && mob.getBoundingBox().inflate(0.5).intersects(target.getBoundingBox())
                && mob.getSensing().hasLineOfSight(target)) {
            if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) mob.doHurtTarget(level, target);
            else target.hurtServer(level, mob.damageSources().mobAttack(mob), 3);
        }
    }

    /** Reserve movement controls from passive panic/breeding goals while the host hunts. */
    private static final class PursuePlayer extends Goal {
        private final Mob mob;
        PursuePlayer(Mob mob) { this.mob = mob; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { return mob.getTarget() instanceof Player p && targetable(p); }
        @Override public void tick() { mob.getNavigation().moveTo(mob.getTarget(), 1.2); }
    }
}
