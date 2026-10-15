package com.example.examplemod.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.CowRenderer;

public class SkulkCowRenderer extends CowRenderer {
    public SkulkCowRenderer(EntityRendererProvider.Context context) {
        super(context);
        addLayer(new SkulkAttachmentLayer<>(this, context, true));
    }
}
