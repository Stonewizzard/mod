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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

/** Exercises the sniffer's real digging timer and loaded loot modifiers. */
public final class SnifferDebrisTests {
    private static final BlockPos TARGET = new BlockPos(4, 1, 4);

    private SnifferDebrisTests() {}

    public static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Identifier.fromNamespaceAndPath(ExampleMod.MODID, "debris_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "exposed_debris"),
                new DigTest(new TestData<>(environment, Level.OVERWORLD,
                        Identifier.fromNamespaceAndPath(ExampleMod.MODID, "empty_drop_test"),
                        700, 0, true, Rotation.NONE, false, 1, 1, false, 0)));
    }

    private static final class DigTest extends GameTestInstance {
        DigTest(TestData<Holder<TestEnvironmentDefinition<?>>> data) {
            super(data);
        }

        @Override
        public MapCodec<? extends GameTestInstance> codec() {
            return MapCodec.unit(this);
        }

        @Override
        protected MutableComponent typeDescription() {
            return Component.literal("Sniffer exposed-debris excavation");
        }

        @Override
        public void run(GameTestHelper helper) {
            require(helper, Blocks.ANCIENT_DEBRIS.defaultBlockState().is(BlockTags.SNIFFER_DIGGABLE_BLOCK),
                    "Ancient debris must be in the sniffer's loaded diggable tag");
            boolean original = helper.getLevel().getGameRules().get(GameRules.MOB_GRIEFING);
            var sniffer = helper.spawn(EntityTypes.SNIFFER, new BlockPos(4, 2, 2));
            sniffer.setPos(helper.absoluteVec(new Vec3(4.5, 2, 2.5)));
            sniffer.setYRot(0);
            sniffer.setXRot(0);
            sniffer.setNoAi(true);
            sniffer.setNoGravity(true);
            helper.setBlock(new BlockPos(4, 1, 2), Blocks.DIRT);
            runCase(helper, sniffer, 0, original);
        }
    }

    private static void runCase(GameTestHelper helper, Sniffer sniffer, int index, boolean originalRule) {
        helper.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, index != 3, helper.getLevel().getServer());
        items(helper).forEach(ItemEntity::discard);
        helper.setBlock(TARGET.above(), Blocks.AIR);
        helper.setBlock(TARGET.below(), index == 2 ? Blocks.ANCIENT_DEBRIS : Blocks.STONE);
        helper.setBlock(TARGET, index == 0 || index == 3 ? Blocks.ANCIENT_DEBRIS : Blocks.DIRT);
        sniffer.transitionTo(Sniffer.State.IDLING);
        sniffer.transitionTo(Sniffer.State.DIGGING);
        helper.runAfterDelay(130, () -> {
            try {
                boolean targetIsAir = helper.getLevel().getBlockState(helper.absolutePos(TARGET)).isAir();
                if (index == 0) {
                    require(helper, targetIsAir, "Digging must consume exposed ancient debris; items=" + items(helper).stream().map(e -> e.getItem().toString()).toList() + " sniffer=" + sniffer.position() + " tick=" + sniffer.tickCount);
                    require(helper, count(helper, Items.NETHERITE_SCRAP) == 1, "Must drop exactly one netherite scrap");
                    require(helper, count(helper, ExampleMod.ANCIENT_ARTIFACT.get()) == 1, "Must drop exactly one artifact");
                    require(helper, count(helper, ExampleMod.CRUNCHER_EGG_ITEM.get()) == 1, "Testing egg chance must be 100 percent");
                    require(helper, count(helper, Items.ANCIENT_DEBRIS) == 0, "Must not also drop the debris block");
                    require(helper, items(helper).stream().mapToInt(e -> e.getItem().getCount()).sum() == 3,
                            "Debris rewards must replace ordinary seeds and include the test egg");
                } else if (index == 1 || index == 2) {
                    require(helper, !targetIsAir, "Ordinary digging must leave dirt in place");
                    require(helper, count(helper, Items.TORCHFLOWER_SEEDS) + count(helper, Items.PITCHER_POD) == 1,
                            "Ordinary digging must retain its vanilla seed reward");
                    require(helper, count(helper, Items.NETHERITE_SCRAP) == 0
                            && count(helper, ExampleMod.ANCIENT_ARTIFACT.get()) == 0, "No excavation rewards for dirt");
                    if (index == 2) {
                        require(helper, helper.getLevel().getBlockState(helper.absolutePos(TARGET.below())).is(Blocks.ANCIENT_DEBRIS),
                                "Buried ancient debris must remain untouched");
                    }
                } else {
                    require(helper, !targetIsAir, "Disabled mob griefing must protect ancient debris");
                    require(helper, items(helper).isEmpty(), "Protected debris must not generate free rewards");
                }
                if (index < 3) {
                    runCase(helper, sniffer, index + 1, originalRule);
                } else {
                    helper.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, originalRule, helper.getLevel().getServer());
                    sniffer.discard();
                    helper.succeed();
                }
            } catch (RuntimeException failure) {
                helper.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, originalRule, helper.getLevel().getServer());
                sniffer.discard();
                throw failure;
            }
        });
    }

    private static java.util.List<ItemEntity> items(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                helper.absoluteAABB(new AABB(0, 0, 0, 8, 8, 8)));
    }

    private static int count(GameTestHelper helper, Item item) {
        return items(helper).stream().filter(e -> e.getItem().is(item)).mapToInt(e -> e.getItem().getCount()).sum();
    }

    private static void require(GameTestHelper helper, boolean value, String message) {
        if (!value) {
            throw helper.assertionException(Component.literal(message));
        }
    }
}
