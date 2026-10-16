package com.example.examplemod;

import net.minecraft.client.Minecraft;
import com.example.examplemod.client.CruncherModel;
import com.example.examplemod.client.CruncherRenderer;
import com.example.examplemod.client.SkulkParasiteModel;
import com.example.examplemod.client.SkulkAttachmentModel;
import com.example.examplemod.client.SkulkParasiteRenderer;
import com.example.examplemod.client.SkulkZombieRenderer;
import com.example.examplemod.client.SkulkCowRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ExampleMod.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ExampleModClient {
    public ExampleModClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CruncherModel.LAYER, CruncherModel::createBodyLayer);
        event.registerLayerDefinition(SkulkParasiteModel.LAYER, SkulkParasiteModel::createBodyLayer);
        event.registerLayerDefinition(SkulkAttachmentModel.ZOMBIE_LAYER, () -> SkulkAttachmentModel.createLayer(false));
        event.registerLayerDefinition(SkulkAttachmentModel.COW_LAYER, () -> SkulkAttachmentModel.createLayer(true));
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ExampleMod.ARCHAEOLOGY_TABLE_ENTITY.get(),com.example.examplemod.client.ArchaeologyTableRenderer::new);
        event.registerEntityRenderer(ExampleMod.CRUNCHER.get(), CruncherRenderer::new);
        event.registerEntityRenderer(ExampleMod.SKULK_PARASITE.get(), SkulkParasiteRenderer::new);
        event.registerEntityRenderer(ExampleMod.SKULK_ZOMBIE.get(), SkulkZombieRenderer::new);
        event.registerEntityRenderer(ExampleMod.SKULK_COW.get(), SkulkCowRenderer::new);
    }

    @SubscribeEvent
    static void addInfectionLayers(EntityRenderersEvent.AddLayers event) {
        com.example.examplemod.client.AdaptiveSculkLayer.addLayers(event);
    }

    @SubscribeEvent
    static void infectionRenderState(net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent event) {
        com.example.examplemod.client.AdaptiveSculkLayer.modifiers(event);
    }

    @SubscribeEvent
    static void checkSculkRendering(net.neoforged.neoforge.data.event.GatherDataEvent.Client event) {
        if (Boolean.getBoolean("examplemod.sculkRenderCheck")) {
            com.example.examplemod.client.ExcavationApiProbe.run();
            try {
                com.example.examplemod.client.ArchaeologyTableVisualRegression.run(
                        event.getResourceManager(net.minecraft.server.packs.PackType.CLIENT_RESOURCES));
            } catch(Exception failure){throw new IllegalStateException("Archaeology table visual check failed",failure);}
            com.example.examplemod.client.SculkRenderRegression.run(
                    event.getResourceManager(net.minecraft.server.packs.PackType.CLIENT_RESOURCES));
        }
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        ExampleMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        ExampleMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void registerTableScreen(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event){
        event.register(ExampleMod.ARCHAEOLOGY_TABLE_MENU.get(),com.example.examplemod.client.ArchaeologyTableScreen::new);
    }
}



