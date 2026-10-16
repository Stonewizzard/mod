package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

/** Exercise native infection, persistence, combat hooks, AI, spawning and vibrations. */
public final class SkulkParasiteTests {
    public static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Identifier.fromNamespaceAndPath(ExampleMod.MODID, "parasite_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "parasite_infection"),
                new InfectionTest(new TestData<>(environment, Level.OVERWORLD,
                        Identifier.fromNamespaceAndPath(ExampleMod.MODID, "empty_drop_test"),
                        400, 0, true, Rotation.NONE, false, 1, 1, false, 0)));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "parasite_vibrations"),
                new QuietTest(new TestData<>(environment, Level.OVERWORLD,
                        Identifier.fromNamespaceAndPath(ExampleMod.MODID, "empty_drop_test"),
                        100, 0, true, Rotation.NONE, false, 1, 1, false, 0)));
    }

    private static final class QuietTest extends GameTestInstance {
        QuietTest(TestData<Holder<TestEnvironmentDefinition<?>>> data) { super(data); }

        @Override
        public MapCodec<? extends GameTestInstance> codec() { return MapCodec.unit(this); }

        @Override
        protected MutableComponent typeDescription() { return Component.literal("Parasite vibration suppression"); }

        @Override
        public void run(GameTestHelper helper) {
            for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++)
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            var sensor = new BlockPos(4, 1, 4);
            var source = new BlockPos(2, 1, 4);
            helper.setBlock(sensor, Blocks.SCULK_SENSOR);
            var parasite = helper.spawn(ExampleMod.SKULK_PARASITE.get(), source);
            parasite.setNoAi(true);
            helper.getLevel().gameEvent(parasite, net.minecraft.world.level.gameevent.GameEvent.STEP,
                    helper.absolutePos(source));
            helper.getLevel().gameEvent(parasite, net.minecraft.world.level.gameevent.GameEvent.HIT_GROUND,
                    helper.absolutePos(source));
            helper.runAfterDelay(10, () -> {
                require(helper, helper.getLevel().getBlockState(helper.absolutePos(sensor))
                                .getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER) == 0,
                        "Parasite walking and landing must not activate a sculk sensor");
                parasite.discard();
                var cow = helper.spawn(EntityTypes.COW, source);
                cow.setNoAi(true);
                helper.getLevel().gameEvent(cow, net.minecraft.world.level.gameevent.GameEvent.STEP,
                        helper.absolutePos(source));
                helper.runAfterDelay(10, () -> {
                    require(helper, helper.getLevel().getBlockState(helper.absolutePos(sensor))
                                    .getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER) > 0,
                            "The same sensor must still detect a normal cow's footsteps");
                    cow.discard();
                    helper.succeed();
                });
            });
        }
    }

    private static final class InfectionTest extends GameTestInstance {
        InfectionTest(TestData<Holder<TestEnvironmentDefinition<?>>> data) { super(data); }

        @Override
        public MapCodec<? extends GameTestInstance> codec() { return MapCodec.unit(this); }

        @Override
        protected MutableComponent typeDescription() { return Component.literal("Skulk parasite infection and restricted spawning"); }

        @Override
        public void run(GameTestHelper helper) {
            for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
                helper.setBlock(new BlockPos(x, 4, z), Blocks.STONE); // Keep zombie hosts out of sunlight.
            }
            verifyBiomeSpawns(helper);
            verifySpawnEgg(helper);
            verifyConversion(helper);
            var unsupported = helper.spawn(EntityTypes.PIG, new BlockPos(1, 1, 1));
            require(helper, SkulkParasite.isSupportedHost(unsupported), "Pigs must support infection");
            unsupported.discard();
            var cow = helper.spawn(EntityTypes.COW, new BlockPos(4, 1, 4));
            cow.setNoAi(true);
            var parasite = helper.spawn(ExampleMod.SKULK_PARASITE.get(), new BlockPos(1, 1, 4));
            var start = parasite.position();
            // GameTestHelper's legacy mock always reports Creative, even after setGameMode.
            var player = new net.neoforged.neoforge.common.util.FakePlayer(helper.getLevel(),
                    new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "ParasiteTarget")) {
                // FakePlayer has no real client and refuses damage. Record successful
                // cow damage calls, while leaving targeting and melee AI unmodified.
                @Override
                public boolean hurtServer(net.minecraft.server.level.ServerLevel level,
                        net.minecraft.world.damagesource.DamageSource source, float amount) {
                    if (!(source.getEntity() instanceof net.minecraft.world.entity.Mob host) || !SkulkStatus.infected(host)) return false;
                    setHealth(getHealth() - amount);
                    return true;
                }
            };
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            player.setPermanentlyInvulnerable(false);
            player.getAbilities().invulnerable = false;
            player.setPos(helper.absoluteVec(new Vec3(6.5, 1, 4.5)));
            helper.getLevel().addNewPlayer(player);
            helper.runAfterDelay(120, () -> {
                var infected = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.animal.cow.Cow.class, bounds(helper), SkulkStatus::infected);
                require(helper, !cow.isRemoved() && parasite.isRemoved() && infected.size() == 1,
                        "Parasite AI must find, approach and merge with one cow");
                require(helper, infected.getFirst().position().distanceToSqr(start) > 1,
                        "Infection must occur at the host, not remotely");
                var hostile = infected.getFirst();
                hostile.setNoAi(false);
                float health = player.getHealth();
                helper.runAfterDelay(80, () -> {
                    require(helper, player.getHealth() < health,
                            "Infected cow must chase and damage a player; cow=" + hostile.position()
                                    + " player=" + player.position() + " target=" + hostile.getTarget()
                                    + " NoAI=" + hostile.isNoAi() + " health=" + player.getHealth());
                    require(helper, hostile.getType() == EntityTypes.COW, "Infected identity must be persistent");
                    require(helper, helper.getLevel().getEntitiesOfClass(SkulkParasite.class, bounds(helper)).isEmpty(),
                            "Consumed parasite must not remain or infect a second host");
                    hostile.discard();
                    player.discard();
                    helper.succeed();
                });
            });
        }
    }

    private static void verifySpawnEgg(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        var first = helper.spawn(ExampleMod.SKULK_PARASITE.get(), new BlockPos(1, 1, 1));
        var second = helper.spawn(ExampleMod.SKULK_PARASITE.get(), new BlockPos(3, 1, 1));
        first.setNoAi(true);
        second.setNoAi(true);
        require(helper, !SkulkParasite.hasNaturalSpawnCapacity(helper.getLevel(), helper.absolutePos(new BlockPos(2, 1, 2))),
                "Two nearby parasites must block additional natural spawns");
        var egg = ExampleMod.SKULK_PARASITE_SPAWN_EGG.get().getDefaultInstance();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, egg);
        egg.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(helper.absoluteVec(new Vec3(2.5, 1, 2.5)),
                        net.minecraft.core.Direction.UP, helper.absolutePos(new BlockPos(2, 0, 2)), false)));
        var spawned = helper.getLevel().getEntitiesOfClass(SkulkParasite.class, bounds(helper));
        require(helper, spawned.size() == 3, "Spawn egg must create one parasite outside Deep Dark even at the natural population cap");
        spawned.forEach(SkulkParasite::discard);
        player.discard();
    }

    private static void verifyConversion(GameTestHelper helper) {
        int checked = 0;
        var hits = new java.util.concurrent.atomic.AtomicInteger();
        var target = new net.neoforged.neoforge.common.util.FakePlayer(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "AllFamiliesTarget")) {
            @Override public boolean hurtServer(net.minecraft.server.level.ServerLevel level,
                    net.minecraft.world.damagesource.DamageSource source, float amount) {
                if (source.getEntity() instanceof net.minecraft.world.entity.Mob attacker
                        && SkulkStatus.infected(attacker) && amount > 0) { hits.incrementAndGet(); return true; }
                return false;
            }
        };
        target.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        target.setPermanentlyInvulnerable(false);
        target.getAbilities().invulnerable=false;
        target.setPos(helper.absoluteVec(new Vec3(2.5, 1, 2.5)));
        helper.getLevel().addNewPlayer(target);
        for (var type : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE) {
            if (!net.minecraft.world.entity.ai.attributes.DefaultAttributes.hasSupplier(type)) continue;
            var entity = type.create(helper.getLevel(), net.minecraft.world.entity.EntitySpawnReason.LOAD);
            if (!(entity instanceof net.minecraft.world.entity.Mob host)) continue;
            if (!SkulkStatus.supports(type)) {
                require(helper, !SkulkParasite.isSupportedHost(host), "Boss/custom exclusions: " + type);
                host.discard();
                continue;
            }
            host.setNoAi(true);
            host.setCustomName(Component.literal("Native host"));
            host.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            var uuid = host.getUUID();
            var original = save(helper, host);
            var speed = host.getAttributeValue(Attributes.MOVEMENT_SPEED);
            var parasite = ExampleMod.SKULK_PARASITE.get().create(helper.getLevel(), net.minecraft.world.entity.EntitySpawnReason.LOAD);
            require(helper, SkulkInfection.infect(parasite, host) == host && parasite.isRemoved() && !host.isRemoved(),
                    "Infection must preserve the exact native mob: " + type);
            require(helper, host.getUUID().equals(uuid) && host.getType() == type && host.isNoAi()
                    && host.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET), "Native identity/equipment: " + type);
            var infectedData = save(helper, host);
            // Native fields include all biome/skin variants, wool, tame owners, inventory and ages.
            for (var key : original.keySet()) {
                if (key.equals("attributes") || key.equals("NeoForgeData") || key.equals("neoforge:attachments")) continue;
                require(helper, java.util.Objects.equals(original.get(key), infectedData.get(key)),
                        "Infection changed native data " + type + "/" + key);
            }
            boolean hostile = SkulkStatus.state(host) == 2;
            require(helper, Math.abs(host.getAttributeValue(Attributes.MOVEMENT_SPEED) - speed * (hostile ? 1.3 : 1)) < 0.00001,
                    "Speed bonus must apply exactly once: " + type);
            require(helper, SkulkStatus.damageMultiplier(host) == (hostile ? 1.5F : 1)
                    && SkulkStatus.experience(host, 5) == (hostile ? 10 : 5), "Hostile rewards/damage: " + type);
            var victim = EntityTypes.COW.create(helper.getLevel(), net.minecraft.world.entity.EntitySpawnReason.LOAD);
            victim.setNoAi(true);
            float victimHealth = victim.getHealth();
            victim.hurtServer(helper.getLevel(), host.damageSources().mobAttack(host), 2);
            require(helper, Math.abs(victimHealth-victim.getHealth()-(hostile ? 3 : 2))<.0001,
                    "Actual damage pipeline must apply the hostile bonus once: " + type);
            victim.discard();
            if (type == EntityTypes.SKELETON) {
                var arrow = EntityTypes.ARROW.create(helper.getLevel(), net.minecraft.world.entity.EntitySpawnReason.LOAD);
                arrow.setOwner(host);
                var rangedVictim = EntityTypes.COW.create(helper.getLevel(), net.minecraft.world.entity.EntitySpawnReason.LOAD);
                float health=rangedVictim.getHealth();
                rangedVictim.hurtServer(helper.getLevel(),host.damageSources().arrow(arrow,host),2);
                require(helper, Math.abs(health-rangedVictim.getHealth()-3)<.0001,"Projectile owner must receive the hostile damage bonus");
                arrow.discard();rangedVictim.discard();
            }
            var xp = new net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent(host, null, 5);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(xp);
            require(helper, xp.getDroppedExperience()==(hostile ? 10 : 5), "XP event hook: " + type);
            var zero = new net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent(host, null, 0);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(zero);
            require(helper, zero.getDroppedExperience()==0, "Zero-XP drops must remain zero: " + type);
            var restored = (net.minecraft.world.entity.Mob) type.create(helper.getLevel(), net.minecraft.world.entity.EntitySpawnReason.LOAD);
            restored.load(net.minecraft.world.level.storage.TagValueInput.create(
                    net.minecraft.util.ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), infectedData));
            require(helper, SkulkStatus.infected(restored) && restored.getUUID().equals(uuid)
                    && restored.getType() == type && !SkulkStatus.canInfect(restored), "Persistent infection: " + type);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(restored));
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(restored));
            require(helper, Math.abs(restored.getAttributeValue(Attributes.MOVEMENT_SPEED)-host.getAttributeValue(Attributes.MOVEMENT_SPEED))<.00001,
                    "Loading/ticking must not stack speed modifiers: " + type);
            var second = ExampleMod.SKULK_PARASITE.get().create(helper.getLevel(), net.minecraft.world.entity.EntitySpawnReason.LOAD);
            require(helper, SkulkInfection.infect(second, host) == null && !second.isRemoved(), "Prevent repeat infection: " + type);
            if (!hostile) {
                host.setPos(target.position());host.setNoAi(false);host.tickCount=0;
                int before=hits.get();
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(host));
                require(helper, hits.get()>before,"Passive host must actually attack a nearby survival player: "+type);
                target.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                host.setTarget(null);
                before=hits.get();
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(host));
                require(helper,hits.get()==before,"Passive host must ignore Creative players: "+type);
                require(helper,host.getNavigation().isDone(),"Passive pursuit must stop when the player becomes Creative: "+type);
                target.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                host.setNoAi(true);
            }
            if (type == EntityTypes.ZOMBIE) {
                // ConversionTracker emits this event from the native conversion callback.
                var drowned = host.convertTo(EntityTypes.DROWNED, net.minecraft.world.entity.ConversionParams.single(host, true, true),
                        converted -> net.neoforged.neoforge.event.EventHooks.onLivingConvert(host, converted));
                require(helper, drowned != null && SkulkStatus.infected(drowned),
                        "Native zombie-to-drowned conversion must carry infection");
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(drowned));
                require(helper, SkulkStatus.state(drowned)==2, "Converted hostile classification must remain hostile");
                drowned.discard();
            }
            second.discard(); host.discard(); restored.discard(); checked++;
        }
        require(helper, checked > 70, "All native mob families must be exercised; tested " + checked);
        target.discard();
        ExampleMod.LOGGER.info("SCULK ALL-MOBS: {} supported mob types preserve their identity and data", checked);
    }

    private static net.minecraft.nbt.CompoundTag save(GameTestHelper helper, net.minecraft.world.entity.Mob mob) {
        var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        mob.saveWithoutId(output);
        return output.buildResult();
    }

    private static void verifyBiomeSpawns(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        biomes.listElements().forEach(biome -> {
            var entry = biome.value().getModifiedEnvironmentAttributes()
                    .get(net.minecraft.world.attribute.EnvironmentAttributes.NATURAL_MOB_SPAWNS);
            var spawns = entry == null ? null : ((net.minecraft.world.level.biome.MobSpawnSettings) entry.argument())
                    .getMobsInCategory(MobCategory.MONSTER);
            boolean contains = spawns != null && spawns.unwrap().stream()
                    .anyMatch(spawn -> spawn.value().type() == ExampleMod.SKULK_PARASITE.get());
            require(helper, contains == biome.is(Biomes.DEEP_DARK),
                    "Natural parasite spawn must be registered only in Deep Dark: " + biome.key().identifier());
        });
        var structures = helper.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE);
        structures.listElements().forEach(structure -> {
            var override = structure.value().spawnOverrides().get(MobCategory.MONSTER);
            boolean contains = override != null && override.spawns().unwrap().stream()
                    .anyMatch(spawn -> spawn.value().type() == ExampleMod.SKULK_PARASITE.get());
            require(helper, contains == structure.is(net.minecraft.world.level.levelgen.structure.BuiltinStructures.ANCIENT_CITY),
                    "Parasite structure spawning must be limited to Ancient Cities: " + structure.key().identifier());
        });
    }

    private static AABB bounds(GameTestHelper helper) { return helper.absoluteAABB(new AABB(0, 0, 0, 8, 8, 8)); }

    private static void require(GameTestHelper helper, boolean value, String message) {
        if (!value) throw helper.assertionException(Component.literal(message));
    }
}

