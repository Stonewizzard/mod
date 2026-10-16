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
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Infection belongs to the original entity: native variants, ownership and equipment survive. */
public final class SkulkStatus {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(
            NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ExampleMod.MODID);
    public static final Supplier<AttachmentType<Integer>> INFECTION = TYPES.register("sculk_infection", () ->
            AttachmentType.builder(() -> 0).serialize(Codec.intRange(0, 2).fieldOf("state"), v -> v != 0)
                    .copyOnDeath().sync(ByteBufCodecs.VAR_INT).build());
    private static final Identifier SPEED = Identifier.fromNamespaceAndPath(ExampleMod.MODID, "sculk_speed");
    private static final Set<Mob> INSTALLED = Collections.newSetFromMap(new WeakHashMap<>());
    // These bodies do not support a convincing attached parasite. Keep one policy
    // for targeting, conversion, rendering, combat bonuses and old-save cleanup.
    private static final Set<EntityType<?>> IMMUNE = Set.of(
            EntityTypes.ENDER_DRAGON, EntityTypes.WITHER, EntityTypes.WARDEN,
            EntityTypes.TADPOLE, EntityTypes.GHAST, EntityTypes.HAPPY_GHAST,
            EntityTypes.ENDERMITE, EntityTypes.SILVERFISH,
            EntityTypes.COD, EntityTypes.SALMON, EntityTypes.TROPICAL_FISH, EntityTypes.PUFFERFISH,
            EntityTypes.SQUID, EntityTypes.GLOW_SQUID,
            EntityTypes.BAT, EntityTypes.ALLAY, EntityTypes.VEX,
            EntityTypes.SLIME, EntityTypes.MAGMA_CUBE, EntityTypes.BLAZE, EntityTypes.BREEZE, EntityTypes.SHULKER);

    private SkulkStatus() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        NeoForge.EVENT_BUS.addListener(SkulkStatus::tick);
        NeoForge.EVENT_BUS.addListener(SkulkStatus::damage);
        NeoForge.EVENT_BUS.addListener(SkulkStatus::onExperience);
        NeoForge.EVENT_BUS.addListener(SkulkStatus::converted);
    }

    public static boolean supports(EntityType<?> type) {
        return !IMMUNE.contains(type)
                && (BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace().equals("minecraft")
                    || type == ExampleMod.CRUNCHER.get());
    }

    public static int state(LivingEntity entity) {
        return supports(entity.getType()) ? storedState(entity) : 0;
    }

    private static int storedState(LivingEntity entity) {
        return entity.hasData(INFECTION) ? entity.getData(INFECTION) : 0;
    }

    public static boolean infected(LivingEntity entity) {
        return state(entity) != 0 || entity instanceof SkulkCow || entity instanceof SkulkZombie;
    }

    public static boolean canInfect(Mob mob) {
        return mob.isAlive() && !mob.isInvulnerable() && supports(mob.getType()) && !infected(mob);
    }

    public static void infect(Mob mob) {
        if (!supports(mob.getType())) return;
        mob.setData(INFECTION, mob instanceof Enemy || mob.getType().getCategory() == MobCategory.MONSTER ? 2 : 1);
        install(mob);
    }

    private static void install(Mob mob) {
        if (!INSTALLED.add(mob)) return;
        // Native transformations (for example zombie -> drowned) carry the marker.
        int kind = mob instanceof Enemy || mob.getType().getCategory() == MobCategory.MONSTER ? 2 : 1;
        if (state(mob) != kind) mob.setData(INFECTION, kind);
        if (state(mob) == 2) {
            speed(mob.getAttribute(Attributes.MOVEMENT_SPEED));
            speed(mob.getAttribute(Attributes.FLYING_SPEED));
            if (mob.getType() == EntityTypes.GIANT && mob instanceof PathfinderMob pathfinder)
                mob.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(pathfinder, 1, true));
        } else {
            var walking = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            var flying = mob.getAttribute(Attributes.FLYING_SPEED);
            if (walking != null) walking.removeModifier(SPEED);
            if (flying != null) flying.removeModifier(SPEED);
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

    private static void onExperience(LivingExperienceDropEvent event) {
        event.setDroppedExperience(experience(event.getEntity(), event.getDroppedExperience()));
    }

    private static void converted(LivingConversionEvent.Post event) {
        if (infected(event.getEntity()) && event.getOutcome() instanceof Mob outcome
                && outcome.level() instanceof ServerLevel && supports(outcome.getType())) infect(outcome);
    }

    private static boolean targetable(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    private static void tick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)
                || storedState(mob) == 0) return;
        if (!supports(mob.getType())) {
            restoreImmuneHost(mob);
            return;
        }
        if (!mob.isAlive()) return;
        install(mob); // Reinstall transient goals after loading, without stacking saved modifiers.
        if (mob.isNoAi()) return;
        var target = mob.getTarget();
        boolean valid = target != null && target.isAlive() && mob.distanceToSqr(target) <= 1024
                && (target instanceof Player player ? targetable(player) : state(mob) == 2);
        if (!valid) {
            target = level.getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(16), SkulkStatus::targetable)
                    .stream().min(java.util.Comparator.comparingDouble(mob::distanceToSqr)).orElse(null);
            mob.setTarget(target);
        }
        var brain = mob.getBrain();
        if (target == null) {
            if (brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED))
                brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
            if (state(mob) == 1) {
                mob.getNavigation().stop();
                if (brain.checkMemory(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED))
                    brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            }
            return;
        }
        if (brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED))
            brain.setMemory(MemoryModuleType.ATTACK_TARGET, target);
        if (state(mob) != 1) return; // Hostile mobs retain their native ranged/explosion attacks.
        if (brain.checkMemory(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED))
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, 1.2F, 1));
        mob.getLookControl().setLookAt(target, 30, 30);
        if (mob.tickCount % 10 == 0) mob.getNavigation().moveTo(target, 1.2);
        mob.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.2);
        if (mob.tickCount % 20 == 0 && mob.getBoundingBox().inflate(0.5).intersects(target.getBoundingBox())
                && mob.getSensing().hasLineOfSight(target)) {
            if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null && mob.getAttributeValue(Attributes.ATTACK_DAMAGE) > 0)
                mob.doHurtTarget(level, target);
            else target.hurtServer(level, mob.damageSources().mobAttack(mob), 3);
        }
    }

    /** Normalize previously infected excluded types in place, including NoAI hosts. */
    private static void restoreImmuneHost(Mob mob) {
        int oldState = storedState(mob);
        mob.setData(INFECTION, 0); // Synchronizes the clean appearance to tracking clients.
        var walking = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        var flying = mob.getAttribute(Attributes.FLYING_SPEED);
        if (walking != null) walking.removeModifier(SPEED);
        if (flying != null) flying.removeModifier(SPEED);
        mob.goalSelector.removeAllGoals(goal -> goal instanceof PursuePlayer);
        INSTALLED.remove(mob);
        if (oldState == 1) {
            mob.setTarget(null);
            mob.getNavigation().stop();
            var brain = mob.getBrain();
            if (brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED))
                brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
            if (brain.checkMemory(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED))
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
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

