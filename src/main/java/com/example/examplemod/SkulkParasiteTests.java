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

/** Tests actual conversion and AI, not just registration or data-file shape. */
public final class SkulkParasiteTests {
    public static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Identifier.fromNamespaceAndPath(ExampleMod.MODID, "parasite_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "parasite_infection"),
                new InfectionTest(new TestData<>(environment, Level.OVERWORLD,
                        Identifier.fromNamespaceAndPath(ExampleMod.MODID, "empty_drop_test"),
                        400, 0, true, Rotation.NONE, false, 1, 1, false, 0)));
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
            require(helper, !SkulkParasite.isSupportedHost(unsupported), "Pigs must not be infected");
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
                    if (!(source.getEntity() instanceof SkulkCow)) return false;
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
                var infected = helper.getLevel().getEntitiesOfClass(SkulkCow.class, bounds(helper));
                require(helper, cow.isRemoved() && parasite.isRemoved() && infected.size() == 1,
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
                    require(helper, hostile.getType() == ExampleMod.SKULK_COW.get(), "Infected identity must be persistent");
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
        var egg = ExampleMod.SKULK_PARASITE_SPAWN_EGG.get().getDefaultInstance();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, egg);
        egg.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(helper.absoluteVec(new Vec3(2.5, 1, 2.5)),
                        net.minecraft.core.Direction.UP, helper.absolutePos(new BlockPos(2, 0, 2)), false)));
        var spawned = helper.getLevel().getEntitiesOfClass(SkulkParasite.class, bounds(helper));
        require(helper, spawned.size() == 1, "Spawn egg must create one parasite even outside Deep Dark");
        spawned.forEach(SkulkParasite::discard);
        player.discard();
    }

    private static void verifyConversion(GameTestHelper helper) {
        var pos = new BlockPos(2, 1, 2);
        var zombie = helper.spawn(EntityTypes.ZOMBIE, pos);
        zombie.setNoAi(true);
        zombie.setBaby(true);
        zombie.setCustomName(Component.literal("Named host"));
        zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        zombie.setHealth(7);
        var parasite = helper.spawn(ExampleMod.SKULK_PARASITE.get(), pos);
        var result = SkulkInfection.infect(parasite, zombie);
        require(helper, result instanceof SkulkZombie && zombie.isRemoved() && parasite.isRemoved(),
                "A zombie infection must convert exactly once and consume the parasite");
        var infected = (SkulkZombie) result;
        require(helper, infected.isBaby() && infected.isNoAi() && infected.getHealth() == 7,
                "Conversion must preserve baby status, NoAI and health");
        require(helper, infected.getCustomName() != null && infected.getCustomName().getString().equals("Named host")
                        && infected.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET),
                "Conversion must preserve custom names and equipment");
        require(helper, infected.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() > zombie.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue()
                        && infected.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() > zombie.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue(),
                "Infected zombie must be faster and stronger");
        require(helper, infected.getExperienceReward(helper.getLevel(), null) > zombie.getExperienceReward(helper.getLevel(), null),
                "Infected zombies must award more experience under the normal XP rules");
        var saved = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        infected.saveWithoutId(saved);
        var restored = ExampleMod.SKULK_ZOMBIE.get().create(helper.getLevel(), net.minecraft.world.entity.EntitySpawnReason.LOAD);
        require(helper, restored != null, "Saved infected entity type must be creatable");
        restored.load(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved.buildResult()));
        require(helper, restored.getType() == ExampleMod.SKULK_ZOMBIE.get() && restored.isBaby()
                        && restored.getHealth() == 7 && restored.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET),
                "Saving/loading must retain the infected type, age, health and equipment");
        restored.discard();
        require(helper, !SkulkParasite.isSupportedHost(infected), "Infected hosts must not be infected again");
        require(helper, helper.getLevel().getEntitiesOfClass(ItemEntity.class, bounds(helper)).isEmpty(),
                "Infection must not trigger death loot or duplicate equipment");
        infected.discard();
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
