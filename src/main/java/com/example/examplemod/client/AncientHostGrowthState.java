package com.example.examplemod.client;

import java.util.Map;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Immutable native bone snapshots keep deferred growth draws on the correct host. */
final class AncientHostGrowthState extends LivingEntityRenderState {
    Map<String,PartPose> poses=Map.of();
    Map<String,Boolean> visibility=Map.of();
    float activity;
    boolean cruncher;
}
