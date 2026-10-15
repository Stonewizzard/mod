package com.example.examplemod.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** The same recognizable parasite body is used free-living and attached to a host. */
public final class SkulkParasiteGeometry {
    private SkulkParasiteGeometry() {}

    public static PartDefinition addParasite(PartDefinition parent, String name, PartPose pose) {
        var parasite = parent.addOrReplaceChild(name, CubeListBuilder.create(), pose);
        parasite.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4, -1.75F, -4.5F, 8, 3.5F, 9), PartPose.ZERO);
        parasite.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-2.5F, -1.25F, -3, 5, 2.5F, 3), PartPose.offset(0, 0.5F, -4.5F));
        for (int i = 0; i < 8; i++) {
            int side = i < 4 ? -1 : 1;
            int row = i % 4;
            float angle = (side < 0 ? Mth.PI : 0) + side * (row - 1.5F) * 0.35F;
            var arm = parasite.addOrReplaceChild("tentacle_" + i, CubeListBuilder.create().texOffs(0, 32)
                    .addBox(0, -0.65F, -0.65F, 3.5F, 1.3F, 1.3F),
                    PartPose.offsetAndRotation(side * 3.1F, 0.6F, -3 + row * 2, 0, angle, side * 0.1F));
            var curl = arm.addOrReplaceChild("curl", CubeListBuilder.create().texOffs(8, 36)
                    .addBox(0, -0.45F, -0.45F, 2.8F, 0.9F, 0.9F),
                    PartPose.offsetAndRotation(3.25F, 0, 0, 0, side * 0.6F, -0.2F));
            curl.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(24, 32)
                    .addBox(0, -0.3F, -0.3F, 2, 0.6F, 0.6F),
                    PartPose.offsetAndRotation(2.55F, 0, 0, 0, side * 0.8F, -0.45F));
        }
        for (int side : new int[]{-1, 1}) {
            parasite.addOrReplaceChild("pincer_" + side, CubeListBuilder.create().texOffs(40, 32)
                    .addBox(-0.5F, -0.5F, -2.5F, 1, 1, 2.5F),
                    PartPose.offsetAndRotation(side * 1.8F, 0.5F, -7, 0, side * -0.4F, 0));
        }
        for (int i = 0; i < 4; i++) {
            int side = i < 2 ? -1 : 1;
            var feeler = parasite.addOrReplaceChild("feeler_" + i, CubeListBuilder.create().texOffs(24, 32)
                    .addBox(-0.4F, -2, -0.4F, 0.8F, 2, 0.8F),
                    PartPose.offsetAndRotation(side * 2, -1.5F, -1.5F + (i % 2) * 3, -0.25F, 0, side * 0.4F));
            feeler.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(24, 36)
                    .addBox(-0.25F, -1.5F, -0.25F, 0.5F, 1.5F, 0.5F),
                    PartPose.offsetAndRotation(0, -1.8F, 0, 0.3F, 0, side * 0.6F));
        }
        return parasite;
    }

    /** A tapered chain of small 3D roots laid over, rather than painted into, the host. */
    public static void addRoot(PartDefinition parent, String name, float[][] points) {
        for (int i = 0; i < points.length - 1; i++) {
            float[] from = points[i], to = points[i + 1];
            float dx = to[0] - from[0], dy = to[1] - from[1], dz = to[2] - from[2];
            float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            float thickness = 0.9F - i * 0.1F;
            float pitch = (float) Math.atan2(-dy, Math.sqrt(dx * dx + dz * dz));
            float yaw = (float) Math.atan2(dx, dz);
            parent.addOrReplaceChild(name + "_" + i, CubeListBuilder.create().texOffs(0, 48)
                    .addBox(-thickness / 2, -thickness / 2, 0, thickness, thickness, length + 0.15F),
                    PartPose.offsetAndRotation(from[0], from[1], from[2], pitch, yaw, 0));
        }
    }

    public static void animate(ModelPart parasite, float phase, float amount) {
        for (int i = 0; i < 8; i++) {
            var arm = parasite.getChild("tentacle_" + i);
            arm.yRot += Mth.sin(phase + i * 0.9F) * amount;
            arm.getChild("curl").yRot += Mth.cos(phase * 0.8F + i) * amount * 1.5F;
            arm.getChild("curl").getChild("tip").zRot += Mth.sin(phase + i) * amount;
        }
        for (int i = 0; i < 4; i++) {
            parasite.getChild("feeler_" + i).zRot += Mth.sin(phase * 0.7F + i) * 0.08F;
        }
    }
}
