package com.example.examplemod;

import org.slf4j.Logger;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "examplemod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<Cruncher>> CRUNCHER =
            ENTITIES.registerEntityType("cruncher", Cruncher::new, MobCategory.CREATURE,
                    builder -> builder.sized(1.9F, 1.75F).fireImmune().clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<SkulkParasite>> SKULK_PARASITE =
            ENTITIES.registerEntityType("skulk_parasite", SkulkParasite::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.75F, 0.4F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<SkulkZombie>> SKULK_ZOMBIE =
            ENTITIES.registerEntityType("skulk_zombie", SkulkZombie::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.6F, 1.95F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<SkulkCow>> SKULK_COW =
            ENTITIES.registerEntityType("skulk_cow", SkulkCow::new, MobCategory.MONSTER,
                    builder -> builder.sized(0.9F, 1.4F).clientTrackingRange(8));
    public static final DeferredItem<SpawnEggItem> SKULK_PARASITE_SPAWN_EGG = ITEMS.registerItem(
            "skulk_parasite_spawn_egg", p -> new SpawnEggItem(p.spawnEgg(SKULK_PARASITE.get())));

    // This creative spawn egg creates a Cruncher immediately; the block egg still hatches.
    public static final DeferredItem<SpawnEggItem> CRUNCHER_SPAWN_EGG = ITEMS.registerItem(
            "cruncher_spawn_egg", p -> new SpawnEggItem(p.spawnEgg(CRUNCHER.get())));
    public static final DeferredBlock<CruncherEggBlock> CRUNCHER_EGG = BLOCKS.registerBlock(
            "cruncher_egg", CruncherEggBlock::new, p -> p.strength(0.5F).noOcclusion());
    public static final DeferredItem<BlockItem> CRUNCHER_EGG_ITEM = ITEMS.registerSimpleBlockItem("cruncher_egg", CRUNCHER_EGG);
    public static final DeferredBlock<AncientGeodeBlock> ANCIENT_GEODE = BLOCKS.registerBlock(
            "ancient_geode", AncientGeodeBlock::new, p -> p.strength(3.0F, 9.0F).noOcclusion());
    public static final DeferredItem<BlockItem> ANCIENT_GEODE_ITEM = ITEMS.registerSimpleBlockItem("ancient_geode", ANCIENT_GEODE);
    public static final DeferredItem<Item> RARE_ANCIENT_ARTIFACT = ITEMS.registerSimpleItem("rare_ancient_artifact");

    // Register the codec used by our sniffer death-loot JSON.
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<SnifferHeadLootModifier>> SNIFFER_HEAD_LOOT =
            LOOT_MODIFIERS.register("sniffer_head", () -> SnifferHeadLootModifier.CODEC);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<SnifferDebrisLootModifier>> SNIFFER_DEBRIS_LOOT =
            LOOT_MODIFIERS.register("sniffer_debris", () -> SnifferDebrisLootModifier.CODEC);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<OminousGeodeLootModifier>> OMINOUS_GEODE_LOOT =
            LOOT_MODIFIERS.register("ominous_geode", () -> OminousGeodeLootModifier.CODEC);

    // Placeholder reward; future artifact components can be selected by the debris loot modifier.
    public static final DeferredItem<Item> ANCIENT_ARTIFACT = ITEMS.registerSimpleItem("ancient_artifact");

    public static final DeferredBlock<ArchaeologyTableBlock> ARCHAEOLOGY_TABLE=BLOCKS.registerBlock(
            "archaeology_table",ArchaeologyTableBlock::new,p->p.strength(2.0F).sound(net.minecraft.world.level.block.SoundType.WOOD).noOcclusion());
    public static final DeferredItem<BlockItem> ARCHAEOLOGY_TABLE_ITEM=ITEMS.registerSimpleBlockItem("archaeology_table",ARCHAEOLOGY_TABLE);
    public static final DeferredItem<Item> ARCHAEOLOGY_DRILL=ITEMS.registerSimpleItem("archaeology_drill",p->p.stacksTo(1));
    public static final DeferredItem<Item> ARCHAEOLOGY_HAMMER=ITEMS.registerSimpleItem("archaeology_hammer",p->p.stacksTo(1));
    public static final DeferredItem<Item> MAGNIFYING_GLASS=ITEMS.registerSimpleItem("magnifying_glass",p->p.stacksTo(1));
    public static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,MODID);
    public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<ArchaeologyTableBlockEntity>> ARCHAEOLOGY_TABLE_ENTITY=
            BLOCK_ENTITIES.register("archaeology_table",()->new net.minecraft.world.level.block.entity.BlockEntityType<>(ArchaeologyTableBlockEntity::new,ARCHAEOLOGY_TABLE.get()));
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,MODID);
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>,net.minecraft.world.inventory.MenuType<ArchaeologyTableMenu>> ARCHAEOLOGY_TABLE_MENU=
            MENUS.register("archaeology_table",()->new net.minecraft.world.inventory.MenuType<>(ArchaeologyTableMenu::new,net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));

    // Keep the original saved ID so existing Example Blocks become Sniffer Heads.
    public static final DeferredBlock<SnifferSkullBlock> SNIFFER_SKULL = BLOCKS.registerBlock(
            "example_block", SnifferSkullBlock::new,
            p -> p.mapColor(MapColor.COLOR_RED).strength(1.5f, 6.0f).noOcclusion());
    // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> SNIFFER_SKULL_ITEM = ITEMS.registerSimpleBlockItem("example_block", SNIFFER_SKULL);

    // Creates a new food item with the id "examplemod:example_id", nutrition 1 and saturation 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", p -> p.food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.examplemod")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(SNIFFER_SKULL_ITEM.get()); // Our placeable block also appears in this mod tab.
                output.accept(EXAMPLE_ITEM.get());
                output.accept(ANCIENT_ARTIFACT.get());
                output.accept(CRUNCHER_EGG_ITEM.get());
                output.accept(CRUNCHER_SPAWN_EGG.get());
                output.accept(SKULK_PARASITE_SPAWN_EGG.get());
                output.accept(ANCIENT_GEODE_ITEM.get());
                output.accept(RARE_ANCIENT_ARTIFACT.get());
                output.accept(ARCHAEOLOGY_TABLE_ITEM.get());
                output.accept(ARCHAEOLOGY_DRILL.get());
                output.accept(ARCHAEOLOGY_HAMMER.get());
                output.accept(MAGNIFYING_GLASS.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener((EntityAttributeCreationEvent event) -> {
            event.put(CRUNCHER.get(), Cruncher.createAttributes().build());
            event.put(SKULK_PARASITE.get(), SkulkParasite.createAttributes().build());
            event.put(SKULK_ZOMBIE.get(), SkulkZombie.createAttributes().build());
            event.put(SKULK_COW.get(), SkulkCow.createAttributes().build());
        });
        modEventBus.addListener((net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event) ->
                event.register(SKULK_PARASITE.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                        net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        SkulkParasite::checkSpawnRules,
                        net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE));
        ENTITIES.register(modEventBus);
        SkulkStatus.register(modEventBus);
        modEventBus.addListener(ExcavationPayload::register);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        if (Boolean.getBoolean("examplemod.dropRegressionTests")) {
            modEventBus.addListener(SnifferHeadDropTests::register);
            modEventBus.addListener(SnifferDebrisTests::register);
            modEventBus.addListener(CruncherTests::register);
            modEventBus.addListener(SkulkParasiteTests::register);
            modEventBus.addListener(ArchaeologyTableTests::register);
        }

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        LOOT_MODIFIERS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.LOCAL, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Add the Sniffer Head item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(SNIFFER_SKULL_ITEM);
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ANCIENT_ARTIFACT);
            event.accept(RARE_ANCIENT_ARTIFACT);
        } else if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(CRUNCHER_SPAWN_EGG);
            event.accept(SKULK_PARASITE_SPAWN_EGG);
        } else if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(CRUNCHER_EGG_ITEM);
            event.accept(ANCIENT_GEODE_ITEM);
        } else if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ARCHAEOLOGY_TABLE_ITEM);
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ARCHAEOLOGY_DRILL);
            event.accept(ARCHAEOLOGY_HAMMER);
            event.accept(MAGNIFYING_GLASS);
        }
    }


    // Vanilla skips normal death-loot generation for baby mobs.
    @SubscribeEvent
    public void onBabySnifferDrops(LivingDropsEvent event) {
        var entity = event.getEntity();
        if (entity.getType() != EntityTypes.SNIFFER || !entity.isBaby()
                || !(entity.level() instanceof ServerLevel level)
                || !level.getGameRules().get(GameRules.MOB_DROPS)) {
            return;
        }
        // Avoid duplicates if another mod enables normal loot for this baby.
        if (event.getDrops().stream().anyMatch(drop -> drop.getItem().is(SNIFFER_SKULL_ITEM.get()))) {
            return;
        }
        event.getDrops().add(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(),
                new ItemStack(SNIFFER_SKULL_ITEM.get())));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}


