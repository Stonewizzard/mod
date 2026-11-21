package com.example.examplemod.client;

import com.example.examplemod.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;

/** Physical Expansion workpieces stay visible even though there is no inventory screen. */
public final class ForgeRuneRenderer
    implements BlockEntityRenderer<ForgeRuneBlockEntity, ForgeRuneRenderer.State> {
  private final ItemModelResolver resolver;

  public static final class State extends BlockEntityRenderState {
    public ItemStackRenderState item;
    public boolean block;
  }

  public ForgeRuneRenderer(BlockEntityRendererProvider.Context c) {
    resolver = c.itemModelResolver();
  }

  public State createRenderState() {
    return new State();
  }

  public void extractRenderState(
      ForgeRuneBlockEntity be,
      State s,
      float partial,
      Vec3 camera,
      ModelFeatureRenderer.CrumblingOverlay breaking) {
    BlockEntityRenderer.super.extractRenderState(be, s, partial, camera, breaking);
    s.item = null;
    if (be.kind() != ForgeRune.EXPANSION || be.getItem(0).isEmpty()) return;
    s.item = new ItemStackRenderState();
    s.block = be.getItem(0).getItem() instanceof BlockItem;
    resolver.updateForTopItem(
        s.item, be.getItem(0), ItemDisplayContext.FIXED, be.getLevel(), null, 0);
  }

  public void submit(State s, PoseStack p, SubmitNodeCollector c, CameraRenderState camera) {
    if (s.item == null) return;
    p.pushPose();
    p.translate(.5, 1.12, .5);
    if (!s.block) p.rotateDegrees(Axis.XP, 90);
    p.scale(.35F, .35F, .35F);
    s.item.submit(p, c, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
    p.popPose();
  }
}
