package com.example.examplemod.client;
import com.example.examplemod.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
/** Client-only opening and session bookmark; no inventory menu or server payload is needed to read. */
public final class FieldGuideClient {
    static final FieldGuideNavigation BOOKMARK=new FieldGuideNavigation();
    public static void open(PlayerInteractEvent.RightClickItem event){
        if(!event.getLevel().isClientSide()||!FieldGuideContent.isGuide(event.getItemStack()))return;
        Minecraft.getInstance().gui.setScreen(new FieldGuideScreen(BOOKMARK));event.setCancellationResult(InteractionResult.SUCCESS);event.setCanceled(true);
    }
    private FieldGuideClient(){}
}
