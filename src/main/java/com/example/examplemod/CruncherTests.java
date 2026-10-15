package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public final class CruncherTests {
    private static final BlockPos EGG = new BlockPos(2, 1, 4);
    private static final BlockPos GEODE = new BlockPos(5, 1, 4);

    public static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Identifier.fromNamespaceAndPath(ExampleMod.MODID, "cruncher_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "cruncher_progression"),
                new ProgressionTest(new TestData<>(environment, Level.OVERWORLD,
                        Identifier.fromNamespaceAndPath(ExampleMod.MODID, "empty_drop_test"),
                        1200, 0, true, Rotation.NONE, false, 1, 1, false, 0)));
    }

    private static final class ProgressionTest extends GameTestInstance {
        ProgressionTest(TestData<Holder<TestEnvironmentDefinition<?>>> data) {
            super(data);
        }

        @Override
        public MapCodec<? extends GameTestInstance> codec() {
            return MapCodec.unit(this);
        }

        @Override
        protected MutableComponent typeDescription() {
            return Component.literal("Cruncher egg, vault geode, and side crunching");
        }

        @Override
        public void run(GameTestHelper helper) {
            for (int x = 0; x < 8; x++) {
                for (int z = 0; z < 8; z++) {
                    helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
                }
            }
            verifySpawnEgg(helper);
            verifyVaultLoot(helper, "reward_ominous", true);
            verifyVaultLoot(helper, "reward", false);
            helper.setBlock(EGG, ExampleMod.CRUNCHER_EGG.get());
            helper.runAfterDelay(620, () -> {
                require(helper, helper.getLevel().getBlockState(helper.absolutePos(EGG)).isAir(), "Placed egg must hatch");
                var babies = helper.getLevel().getEntitiesOfClass(Cruncher.class, bounds(helper));
                require(helper, babies.size() == 1 && babies.getFirst().isBaby(), "Egg must hatch exactly one baby Cruncher");
                var cruncher = babies.getFirst();
                var player = helper.makeMockPlayer(GameType.SURVIVAL);
                int age = cruncher.getAge();
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WITHER_ROSE));
                cruncher.interact(player, InteractionHand.MAIN_HAND);
                require(helper, cruncher.getAge() > age, "Feeding a baby a Wither Rose must accelerate growth");
                cruncher.setAge(0);
                cruncher.setPos(helper.absoluteVec(new Vec3(1.5, 1, 4.5)));
                helper.setBlock(GEODE, ExampleMod.ANCIENT_GEODE.get());
                boolean original = helper.getLevel().getGameRules().get(GameRules.MOB_GRIEFING);
                helper.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, true, helper.getLevel().getServer());
                helper.runAfterDelay(300, () -> {
                    try {
                        require(helper, helper.getLevel().getBlockState(helper.absolutePos(GEODE)).isAir(),
                                "Cruncher must navigate beside and consume the geode; position=" + cruncher.position());
                        require(helper, cruncher.position().distanceToSqr(helper.absoluteVec(new Vec3(1.5, 1, 4.5))) > 0.25,
                                "Cruncher must actually walk toward the geode");
                        require(helper, cruncher.getY() < helper.absolutePos(GEODE).getY() + 0.5,
                                "Cruncher must stay beside the geode rather than climbing on top");
                        require(helper, countRare(helper) == 1, "Crunching must drop exactly one rare artifact");
                        require(helper, items(helper).stream().noneMatch(e -> e.getItem().is(ExampleMod.ANCIENT_GEODE_ITEM.get())),
                                "Crunching must not also drop the geode block");
                        cruncher.discard();
                        items(helper).forEach(ItemEntity::discard);
                        helper.setBlock(GEODE, ExampleMod.ANCIENT_GEODE.get());
                        helper.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, false, helper.getLevel().getServer());
                        var protectedCruncher = helper.spawn(ExampleMod.CRUNCHER.get(), new BlockPos(2, 1, 4));
                        helper.runAfterDelay(140, () -> {
                            try {
                                require(helper, helper.getLevel().getBlockState(helper.absolutePos(GEODE)).is(ExampleMod.ANCIENT_GEODE.get()),
                                        "Disabled mob griefing must protect geodes");
                                require(helper, countRare(helper) == 0, "Protected geodes must not generate rewards");
                                protectedCruncher.discard();
                                verifyBreeding(helper);
                            } finally {
                                helper.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, original, helper.getLevel().getServer());
                            }
                        });
                    } catch (RuntimeException failure) {
                        helper.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, original, helper.getLevel().getServer());
                        throw failure;
                    }
                });
            });
        }
    }

    private static void verifySpawnEgg(GameTestHelper helper) {
        helper.useBlock(new BlockPos(2, 0, 2), helper.makeMockPlayer(GameType.SURVIVAL),
                ExampleMod.CRUNCHER_SPAWN_EGG.get().getDefaultInstance(), Direction.UP);
        var spawned = helper.getLevel().getEntitiesOfClass(Cruncher.class, bounds(helper));
        require(helper, spawned.size() == 1 && !spawned.getFirst().isBaby(),
                "Using the creative spawn egg must immediately spawn exactly one adult Cruncher");
        var cruncher = spawned.getFirst();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TORCHFLOWER_SEEDS));
        cruncher.interact(player, InteractionHand.MAIN_HAND);
        require(helper, !cruncher.isInLove(), "Old sniffer food must no longer feed a Cruncher");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WITHER_ROSE));
        cruncher.interact(player, InteractionHand.MAIN_HAND);
        require(helper, cruncher.isInLove(), "Feeding a Wither Rose must enable breeding");
        spawned.forEach(Cruncher::discard);
    }

    private static void verifyBreeding(GameTestHelper helper) {
        helper.setBlock(GEODE, Blocks.AIR);
        var first = helper.spawn(ExampleMod.CRUNCHER.get(), new BlockPos(2, 1, 2));
        var second = helper.spawn(ExampleMod.CRUNCHER.get(), new BlockPos(4, 1, 2));
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WITHER_ROSE, 2));
        first.interact(player, InteractionHand.MAIN_HAND);
        second.interact(player, InteractionHand.MAIN_HAND);
        helper.runAfterDelay(100, () -> {
            require(helper, first.getAge() > 0 && second.getAge() > 0,
                    "Rose-fed Crunchers must finish breeding and enter their cooldown");
            int eggs = items(helper).stream().filter(e -> e.getItem().is(ExampleMod.CRUNCHER_EGG_ITEM.get()))
                    .mapToInt(e -> e.getItem().getCount()).sum();
            require(helper, eggs == 1, "Two rose-fed Crunchers must lay exactly one hatchable egg");
            first.discard();
            second.discard();
            helper.succeed();
        });
    }

    private static void verifyVaultLoot(GameTestHelper helper, String name, boolean ominous) {
        var key = ResourceKey.create(Registries.LOOT_TABLE,
                Identifier.parse("minecraft:chests/trial_chambers/" + name));
        var table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
        var params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(new Vec3(1, 1, 1)))
                .create(LootContextParamSets.CHEST);
        var random = RandomSource.create(0x47454F444553L);
        int total = 0;
        for (int i = 0; i < 600; i++) {
            var loot = table.getRandomItems(params, random);
            int geodes = loot.stream().filter(s -> s.is(ExampleMod.ANCIENT_GEODE_ITEM.get()))
                    .mapToInt(ItemStack::getCount).sum();
            require(helper, geodes <= (ominous ? 1 : 0), "Wrong geode count for vault table " + name);
            require(helper, loot.stream().anyMatch(s -> !s.is(ExampleMod.ANCIENT_GEODE_ITEM.get())),
                    "Vanilla vault rewards must remain present");
            total += geodes;
        }
        if (ominous) {
            require(helper, total >= 85 && total <= 155,
                    "Expected approximately 20 percent geodes over 600 seeded vault rewards, got " + total);
        }
        ExampleMod.LOGGER.info("Loot balance regression: {} Ancient Geodes from 600 seeded {} rewards", total, name);
    }

    private static java.util.List<ItemEntity> items(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, bounds(helper));
    }

    private static int countRare(GameTestHelper helper) {
        return items(helper).stream().filter(e -> e.getItem().is(ExampleMod.RARE_ANCIENT_ARTIFACT.get()))
                .mapToInt(e -> e.getItem().getCount()).sum();
    }

    private static AABB bounds(GameTestHelper helper) {
        return helper.absoluteAABB(new AABB(0, 0, 0, 8, 8, 8));
    }

    private static void require(GameTestHelper helper, boolean value, String message) {
        if (!value) {
            throw helper.assertionException(Component.literal(message));
        }
    }
}
