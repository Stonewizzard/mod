package com.example.examplemod.client;

import com.example.examplemod.SkulkParasite;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class SkulkParasiteRenderer extends MobRenderer<SkulkParasite, LivingEntityRenderState, SkulkParasiteModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("examplemod", "textures/entity/skulk_parasite.png");

    public SkulkParasiteRenderer(EntityRendererProvider.Context context) {
        super(context, new SkulkParasiteModel(context.bakeLayer(SkulkParasiteModel.LAYER)), 0.35F);
    }

    @Override
    public LivingEntityRenderState createRenderState() { return new LivingEntityRenderState(); }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) { return TEXTURE; }
}
