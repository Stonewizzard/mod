package com.example.examplemod.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

public class SkulkZombieRenderer extends ZombieRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("examplemod", "textures/entity/skulk_zombie.png");

    public SkulkZombieRenderer(EntityRendererProvider.Context context) { super(context); }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) { return TEXTURE; }
}
