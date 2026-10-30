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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
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
public class Cruncher extends Animal {
    public static final int CRUNCH_TICKS = 40;
    private int artifactChewTicks;
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

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15).add(Attributes.TEMPT_RANGE, 10.0);
    }

    /** A player deliberately feeds one artifact; deliver the reward immediately, with a cosmetic chew. */
    @Override
    public net.minecraft.world.InteractionResult mobInteract(Player player, net.minecraft.world.InteractionHand hand) {
        var artifact=player.getItemInHand(hand);
        if(artifact.is(ArchaeologyTableMenu.ARTIFACTS)) {
            if(!isAlive()||isBaby()||isCrunching())return net.minecraft.world.InteractionResult.PASS;
            if(level() instanceof ServerLevel server) {
                if(!player.getAbilities().instabuild)artifact.shrink(1);
                var scrap=new ItemStack(AncientAnvilContent.SCRAP_ITEM.get());
                if(!player.addItem(scrap))Block.popResource(server,blockPosition(),scrap);
                artifactChewTicks=CRUNCH_TICKS;
                entityData.set(CRUNCHING,true);
                getNavigation().stop();
                playSound(SoundEvents.GENERIC_EAT,.8F,.7F);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.mobInteract(player,hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if(level() instanceof ServerLevel&&artifactChewTicks>0) {
            getNavigation().stop();
            entityData.set(CRUNCHING,--artifactChewTicks>0);
        }
    }

    @Override
    public boolean isFood(ItemStack item) {
        return item.is(Items.WITHER_ROSE);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SNIFFER_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SNIFFER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SNIFFER_DEATH;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new CrunchGeodeGoal());
        goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        goalSelector.addGoal(3, new TemptGoal(this, 1.0, this::isFood, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean canMate(Animal partner) {
        return partner instanceof Cruncher && super.canMate(partner);
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
        private boolean finished;

        CrunchGeodeGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!(level() instanceof ServerLevel level) || isBaby() || !isAlive()
                    || artifactChewTicks > 0 || tickCount < nextSearch || !EventHooks.canEntityGrief(level, Cruncher.this)) {
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
                ArrayList<Direction> faces = new ArrayList<>();
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    faces.add(direction);
                }
                faces.sort(Comparator.comparingDouble(direction -> position().distanceToSqr(
                        Vec3.atBottomCenterOf(geode.relative(direction, 2)))));
                for (Direction face : faces) {
                    BlockPos approach = geode.relative(face, 2);
                    if (!level.getBlockState(geode.relative(face)).isAir()
                            || !level.getBlockState(approach).isAir()
                            || !level.getBlockState(approach.above()).isAir()
                            || !level.getBlockState(approach.below()).isFaceSturdy(level, approach.below(), Direction.UP)) {
                        continue;
                    }
                    var path = getNavigation().createPath(approach, 0);
                    boolean crossesGeode = false;
                    if (path != null) {
                        for (int nodeIndex = 0; nodeIndex < path.getNodeCount(); nodeIndex++) {
                            var node = path.getNode(nodeIndex);
                            if (node.x == geode.getX() && node.z == geode.getZ()) {
                                crossesGeode = true;
                                break;
                            }
                        }
                    }
                    if (path != null && path.canReach() && !crossesGeode) {
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
            return !finished && artifactChewTicks == 0 && target != null && !isBaby() && isAlive() && timeout < 240
                    && level().getBlockState(target).is(ExampleMod.ANCIENT_GEODE.get())
                    && level() instanceof ServerLevel level && EventHooks.canEntityGrief(level, Cruncher.this);
        }

        @Override
        public void start() {
            finished = false;
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
            // The selector may tick a finished goal before it calls stop().
            // Keep the positions intact until stop() and never crunch twice.
            if (finished) {
                return;
            }
            timeout++;
            Vec3 approach = Vec3.atBottomCenterOf(stand);
            Vec3 center = Vec3.atCenterOf(target);
            getLookControl().setLookAt(center.x, center.y, center.z);
            // Navigation may finish early for a wide mob. Guide the final short
            // step toward the side waypoint before beginning the crunch.
            if (position().distanceToSqr(approach) > 0.8) {
                entityData.set(CRUNCHING, false);
                progress = 0;
                if (getNavigation().isDone()) {
                    getMoveControl().setWantedPosition(approach.x, approach.y, approach.z, 1.1);
                }
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
                Block.popResource(level, target, ArchaeologyContent.specimen(level.getRandom(),true));
                finished = true;
                entityData.set(CRUNCHING, false);
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
