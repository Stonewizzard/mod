package com.example.examplemod;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.Vec3;

/** A small sculk creature which leaps onto a supported host and merges with it. */
public class SkulkParasite extends Monster {
    public static final int NATURAL_SPAWN_ROLL = 10;
    public static final int MAX_NEARBY_PARASITES = 2;
    public static final int NATURAL_SPAWN_RADIUS = 32;
    public SkulkParasite(EntityType<? extends SkulkParasite> type, Level level) {
        super(type, level);
        xpReward = 3;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28).add(Attributes.FOLLOW_RANGE, 12.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new InfectHostGoal());
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    protected net.minecraft.world.entity.Entity.MovementEmission getMovementEmission() {
        return net.minecraft.world.entity.Entity.MovementEmission.NONE;
    }

    @Override
    public boolean dampensVibrations() {
        return true;
    }

    static boolean hasNaturalSpawnCapacity(ServerLevelAccessor level, BlockPos pos) {
        return level.getLevel().getEntitiesOfClass(SkulkParasite.class,
                new net.minecraft.world.phys.AABB(pos).inflate(NATURAL_SPAWN_RADIUS),
                SkulkParasite::isAlive).size() < MAX_NEARBY_PARASITES;
    }

    public static boolean isSupportedHost(Mob mob) {
        return SkulkStatus.canInfect(mob);
    }

    public static boolean checkSpawnRules(EntityType<SkulkParasite> type, ServerLevelAccessor level,
            EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        // City bounds can extend beyond the Deep Dark biome. Both habitats are allowed,
        // while accidental spawn-list additions elsewhere still fail this predicate.
        var city = level.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getOrThrow(net.minecraft.world.level.levelgen.structure.BuiltinStructures.ANCIENT_CITY);
        boolean habitat = level.getBiome(pos).is(Biomes.DEEP_DARK)
                || level.getLevel().structureManager().getStructureAt(pos, city.value()).isValid();
        return habitat && Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                && hasNaturalSpawnCapacity(level, pos) && random.nextInt(NATURAL_SPAWN_ROLL) == 0;
    }

    private final class InfectHostGoal extends Goal {
        private Mob host;
        private int leapCooldown;

        InfectHostGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            host = level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(12.0), SkulkParasite::isSupportedHost)
                    .stream().filter(candidate -> getSensing().hasLineOfSight(candidate))
                    .min(java.util.Comparator.comparingDouble(SkulkParasite.this::distanceToSqr)).orElse(null);
            return host != null;
        }

        @Override
        public boolean canContinueToUse() {
            return host != null && isSupportedHost(host) && distanceToSqr(host) < 256.0 && isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            leapCooldown = 0;
        }

        @Override
        public void tick() {
            if (host == null || !isSupportedHost(host) || !isAlive()) return;
            getLookControl().setLookAt(host, 30, 30);
            double distance = distanceToSqr(host);
            if (leapCooldown > 0) leapCooldown--;
            if (distance <= 9.0 && onGround() && leapCooldown == 0 && getSensing().hasLineOfSight(host)) {
                getNavigation().stop();
                Vec3 toward = host.position().subtract(position()).normalize().scale(0.55);
                setDeltaMovement(toward.x, 0.42, toward.z);
                leapCooldown = 25;
            } else if (onGround()) {
                getNavigation().moveTo(host, 1.25);
            }
            if (getBoundingBox().inflate(0.25).intersects(host.getBoundingBox())
                    && getSensing().hasLineOfSight(host)) {
                SkulkInfection.infect(SkulkParasite.this, host);
            }
        }

        @Override
        public void stop() {
            getNavigation().stop();
            host = null;
        }
    }
}

