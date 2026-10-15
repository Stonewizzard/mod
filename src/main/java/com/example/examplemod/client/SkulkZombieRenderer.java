package com.example.examplemod.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;

public class SkulkZombieRenderer extends ZombieRenderer {
    public SkulkZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
        addLayer(new SkulkAttachmentLayer<>(this, context, false));
    }
}
