package com.example.examplemod.client;

import com.example.examplemod.Cruncher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class CruncherRenderer extends MobRenderer<Cruncher, CruncherRenderState, CruncherModel> {
    // Entity skins do not use block-atlas .mcmeta animation. Select the client
    // frame here; this does not change the entity, its AI, or its network state.
    private static final Identifier[] TEXTURES = new Identifier[16];

    static {
        TEXTURES[0] = Identifier.fromNamespaceAndPath("examplemod", "textures/entity/cruncher.png");
        for (int i = 1; i < TEXTURES.length; i++) {
            String frame = i < 10 ? "0" + i : Integer.toString(i);
            TEXTURES[i] = Identifier.fromNamespaceAndPath(
                    "examplemod", "textures/entity/cruncher/frame_" + frame + ".png");
        }
    }

    public CruncherRenderer(EntityRendererProvider.Context context) {
        super(context, new CruncherModel(context.bakeLayer(CruncherModel.LAYER)), 1.1F);
    }

    @Override
    public CruncherRenderState createRenderState() {
        return new CruncherRenderState();
    }

    @Override
    public Identifier getTextureLocation(CruncherRenderState state) {
        return TEXTURES[Math.floorMod((int) (state.ageInTicks / 4.0F), TEXTURES.length)];
    }

    @Override
    public void extractRenderState(Cruncher entity, CruncherRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.crunching = entity.isCrunching();
    }
}
