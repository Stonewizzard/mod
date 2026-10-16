package com.example.examplemod.client;

import java.util.Map;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Immutable native bone snapshots keep deferred growth draws on the correct host. */
final class AncientHostGrowthState extends LivingEntityRenderState {
    record Scale(float x,float y,float z) {}
    Map<String,PartPose> poses=Map.of();
    Map<String,Boolean> visibility=Map.of();
    Map<String,Scale> scales=Map.of();
    float activity;
    boolean cruncher;
}
