package com.example.examplemod.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Snapshot the chosen host model's pose for Minecraft's deferred model submission. */
public class SkulkAttachmentState extends LivingEntityRenderState {
    public PartPose hostRoot = PartPose.ZERO;
    public PartPose hostBody = PartPose.ZERO;
    public PartPose hostHead = PartPose.ZERO;
}
