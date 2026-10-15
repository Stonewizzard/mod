package com.example.examplemod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/** A passive, fire-immune sniffer relative which approaches geodes from the side. */
public class Cruncher extends Sniffer {
    public static final int CRUNCH_TICKS = 40;
    public static final int TEST_BABY_AGE = -1200;
    private static final EntityDataAccessor<Boolean> CRUNCHING =
            SynchedEntityData.defineId(Cruncher.class, EntityDataSerializers.BOOLEAN);

    public Cruncher(EntityType<? extends Cruncher> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CRUNCHING, false);
    }

    public boolean isCrunching() {
        return entityData.get(CRUNCHING);
    }

    @Override
    public boolean canSniff() {
        return false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        // Use navigation and goals below instead of the sniffer's seed-digging brain.
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new CrunchGeodeGoal());
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ExampleMod.CRUNCHER.get().create(level, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal partner) {
        Block.popResource(level, blockPosition(), new ItemStack(ExampleMod.CRUNCHER_EGG_ITEM.get()));
        finalizeSpawnChildFromBreeding(level, partner, null);
    }

    private final class CrunchGeodeGoal extends Goal {
        private BlockPos target;
        private BlockPos stand;
        private int progress;
        private int nextSearch;
        private int timeout;

        CrunchGeodeGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!(level() instanceof ServerLevel level) || isBaby() || !isAlive()
                    || tickCount < nextSearch || !EventHooks.canEntityGrief(level, Cruncher.this)) {
                return false;
            }
            nextSearch = tickCount + 40;
            ArrayList<BlockPos> candidates = new ArrayList<>();
            for (BlockPos pos : BlockPos.betweenClosed(blockPosition().offset(-8, -2, -8), blockPosition().offset(8, 2, 8))) {
                if (level.hasChunkAt(pos) && level.getBlockState(pos).is(ExampleMod.ANCIENT_GEODE.get())) {
                    candidates.add(pos.immutable());
                }
            }
            candidates.sort(Comparator.comparingDouble(pos -> position().distanceToSqr(Vec3.atCenterOf(pos))));
            for (BlockPos geode : candidates) {
                for (Direction face : Direction.Plane.HORIZONTAL) {
                    BlockPos approach = geode.relative(face, 2);
                    if (!level.getBlockState(geode.relative(face)).isAir()
                            || !level.getBlockState(approach).isAir()
                            || !level.getBlockState(approach.above()).isAir()
                            || !level.getBlockState(approach.below()).isFaceSturdy(level, approach.below(), Direction.UP)) {
                        continue;
                    }
                    var path = getNavigation().createPath(approach, 0);
                    if (path != null && path.canReach()) {
                        target = geode;
                        stand = approach;
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && !isBaby() && isAlive() && timeout < 240
                    && level().getBlockState(target).is(ExampleMod.ANCIENT_GEODE.get())
                    && level() instanceof ServerLevel level && EventHooks.canEntityGrief(level, Cruncher.this);
        }

        @Override
        public void start() {
            progress = 0;
            timeout = 0;
            Vec3 approach = Vec3.atBottomCenterOf(stand);
            getNavigation().moveTo(approach.x, approach.y, approach.z, 1.1);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            timeout++;
            Vec3 approach = Vec3.atBottomCenterOf(stand);
            Vec3 center = Vec3.atCenterOf(target);
            getLookControl().setLookAt(center.x, center.y, center.z);
            if (position().distanceToSqr(approach) > 0.8) {
                entityData.set(CRUNCHING, false);
                progress = 0;
                if (timeout % 20 == 0) {
                    getNavigation().moveTo(approach.x, approach.y, approach.z, 1.1);
                }
                return;
            }
            var hit = level().clip(new ClipContext(getEyePosition(), center,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, Cruncher.this));
            if (!hit.getBlockPos().equals(target)) {
                progress = 0;
                entityData.set(CRUNCHING, false);
                return;
            }
            getNavigation().stop();
            float yaw = (float) (Math.atan2(center.z - getZ(), center.x - getX()) * 180.0 / Math.PI) - 90.0F;
            setYRot(yaw);
            yBodyRot = yaw;
            yHeadRot = yaw;
            entityData.set(CRUNCHING, true);
            if (++progress % 10 == 0) {
                playSound(SoundEvents.STONE_BREAK, 0.7F, 0.65F);
            }
            if (progress >= CRUNCH_TICKS && level() instanceof ServerLevel level
                    && EventHooks.canEntityGrief(level, Cruncher.this)
                    && level.destroyBlock(target, false, Cruncher.this)) {
                Block.popResource(level, target, new ItemStack(ExampleMod.RARE_ANCIENT_ARTIFACT.get()));
                target = null;
            }
        }

        @Override
        public void stop() {
            getNavigation().stop();
            entityData.set(CRUNCHING, false);
            target = null;
            stand = null;
            nextSearch = tickCount + 80;
        }
    }
}
