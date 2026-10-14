package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

/** Headless runtime regression; enabled only by the gameTestServer run configuration. */
public final class SnifferHeadDropTests {
    private SnifferHeadDropTests() {}

    public static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Identifier.fromNamespaceAndPath(ExampleMod.MODID, "head_drop_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "sniffer_head_drops"),
                new DropTest(new TestData<>(environment, Level.OVERWORLD,
                        Identifier.fromNamespaceAndPath(ExampleMod.MODID, "empty_drop_test"),
                        100, 0, true, Rotation.NONE, false, 1, 1, false, 0)));
    }

    private static final class DropTest extends GameTestInstance {
        DropTest(TestData<Holder<TestEnvironmentDefinition<?>>> data) {
            super(data);
        }

        @Override
        public MapCodec<? extends GameTestInstance> codec() {
            return MapCodec.unit(this);
        }

        @Override
        protected MutableComponent typeDescription() {
            return Component.literal("Sniffer head death-drop regression");
        }

        @Override
        public void run(GameTestHelper helper) {
            ServerLevel level = helper.getLevel();
            boolean originalMobDrops = level.getGameRules().get(GameRules.MOB_DROPS);
            try {
                level.getGameRules().set(GameRules.MOB_DROPS, true, level.getServer());
                var sniffer = helper.spawn(EntityTypes.SNIFFER, new BlockPos(2, 2, 2));
                kill(level, sniffer);
                requireHeads(helper, 1, "Adult sniffer must drop exactly one head");
                clearItems(helper);

                var baby = helper.spawn(EntityTypes.SNIFFER, new BlockPos(2, 2, 2));
                baby.setBaby(true);
                kill(level, baby);
                requireHeads(helper, 1, "Baby sniffer must drop exactly one head");
                clearItems(helper);

                var pig = helper.spawn(EntityTypes.PIG, new BlockPos(2, 2, 2));
                kill(level, pig);
                requireHeads(helper, 0, "Other mobs must not gain sniffer head drops");
                clearItems(helper);

                level.getGameRules().set(GameRules.MOB_DROPS, false, level.getServer());
                var disabled = helper.spawn(EntityTypes.SNIFFER, new BlockPos(2, 2, 2));
                kill(level, disabled);
                requireHeads(helper, 0, "Disabled mob loot must prevent the head drop");
                var disabledBaby = helper.spawn(EntityTypes.SNIFFER, new BlockPos(2, 2, 2));
                disabledBaby.setBaby(true);
                kill(level, disabledBaby);
                requireHeads(helper, 0, "Disabled mob loot must also prevent baby head drops");
                helper.succeed();
            } finally {
                level.getGameRules().set(GameRules.MOB_DROPS, originalMobDrops, level.getServer());
            }
        }
    }

    private static void kill(ServerLevel level, LivingEntity entity) {
        entity.setHealth(0);
        entity.die(level.damageSources().generic());
    }

    private static void requireHeads(GameTestHelper helper, int expected, String message) {
        int count = helper.getLevel().getEntitiesOfClass(ItemEntity.class, bounds(helper)).stream()
                .filter(item -> item.getItem().is(ExampleMod.SNIFFER_SKULL_ITEM.get()))
                .mapToInt(item -> item.getItem().getCount()).sum();
        if (count != expected) {
            throw helper.assertionException(Component.literal(message + "; expected " + expected + ", got " + count));
        }
    }

    private static void clearItems(GameTestHelper helper) {
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, bounds(helper)).forEach(ItemEntity::discard);
    }

    private static AABB bounds(GameTestHelper helper) {
        return helper.absoluteAABB(new AABB(0, 0, 0, 8, 8, 8));
    }
}
