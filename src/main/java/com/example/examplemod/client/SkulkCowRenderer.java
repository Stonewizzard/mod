package com.example.examplemod.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.resources.Identifier;

public class SkulkCowRenderer extends CowRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("examplemod", "textures/entity/skulk_cow.png");

    public SkulkCowRenderer(EntityRendererProvider.Context context) { super(context); }

    @Override
    public Identifier getTextureLocation(CowRenderState state) { return TEXTURE; }
}
