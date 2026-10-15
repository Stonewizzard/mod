package com.example.examplemod.client;

import com.example.examplemod.Cruncher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class CruncherRenderer extends MobRenderer<Cruncher, CruncherRenderState, CruncherModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            "examplemod", "textures/entity/cruncher.png");

    public CruncherRenderer(EntityRendererProvider.Context context) {
        super(context, new CruncherModel(context.bakeLayer(CruncherModel.LAYER)), 1.1F);
    }

    @Override
    public CruncherRenderState createRenderState() {
        return new CruncherRenderState();
    }

    @Override
    public Identifier getTextureLocation(CruncherRenderState state) {
        return TEXTURE;
    }

    @Override
    public void extractRenderState(Cruncher entity, CruncherRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.crunching = entity.isCrunching();
    }
}
