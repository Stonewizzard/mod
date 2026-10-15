package com.example.examplemod.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** Broad carapace, six supporting legs, sensory horns, and four trailing tendrils. */
public final class SkulkParasiteGeometry {
    private SkulkParasiteGeometry() {}

    public static PartDefinition addParasite(PartDefinition parent, String name, PartPose pose) {
        var parasite = parent.addOrReplaceChild(name, CubeListBuilder.create(), pose);
        parasite.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4, -2, -5, 8, 4, 10)
                .texOffs(0, 2).addBox(-3.25F, -2.6F, -3.5F, 6.5F, 0.8F, 7.5F), PartPose.ZERO);
        parasite.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-3, -1.5F, -4, 6, 3, 4), PartPose.offset(0, 0.5F, -5));
        for (int side : new int[]{-1, 1}) {
            parasite.addOrReplaceChild("eye_" + side, CubeListBuilder.create().texOffs(52, 32)
                    .addBox(-0.2F, -0.35F, -0.7F, 0.4F, 0.7F, 1.4F),
                    PartPose.offset(side * 3.05F, -0.2F, -7.4F));
            parasite.addOrReplaceChild("pincer_" + side, CubeListBuilder.create().texOffs(40, 32)
                    .addBox(-0.6F, -0.6F, -3.1F, 1.2F, 1.2F, 3.1F),
                    PartPose.offsetAndRotation(side * 2.1F, 0.7F, -8.5F, 0, side * -0.3F, 0));
        }
        for (int i = 0; i < 6; i++) {
            int side = i < 3 ? -1 : 1;
            int row = i % 3;
            float angle = (side < 0 ? Mth.PI : 0) + side * (row - 1) * 0.32F;
            var leg = parasite.addOrReplaceChild("leg_" + i, CubeListBuilder.create().texOffs(0, 32)
                    .addBox(0, -0.65F, -0.75F, 3.8F, 1.3F, 1.5F),
                    PartPose.offsetAndRotation(side * 3.1F, 0.7F, -3.5F + row * 3.5F, 0, angle, side * 0.2F));
            leg.addOrReplaceChild("foot", CubeListBuilder.create().texOffs(12, 36)
                    .addBox(0, -0.45F, -0.5F, 2.5F, 0.9F, 1),
                    PartPose.offsetAndRotation(3.5F, 0, 0, 0, side * 0.28F, 0.5F));
        }
        for (int i = 0; i < 4; i++) {
            int side = i < 2 ? -1 : 1;
            int row = i % 2;
            float angle = (side < 0 ? Mth.PI : 0) + side * (-0.5F - row * 0.65F);
            var arm = parasite.addOrReplaceChild("tendril_" + i, CubeListBuilder.create().texOffs(0, 40)
                    .addBox(0, -0.4F, -0.4F, 2.75F, 0.8F, 0.8F),
                    PartPose.offsetAndRotation(side * 2.6F, 1, 2 + row * 2.5F, 0, angle, side * 0.05F));
            var curl = arm.addOrReplaceChild("curl", CubeListBuilder.create().texOffs(10, 40)
                    .addBox(0, -0.27F, -0.27F, 2.25F, 0.54F, 0.54F),
                    PartPose.offsetAndRotation(2.5F, 0, 0, 0, side * 0.6F, -0.2F));
            curl.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(24, 32)
                    .addBox(0, -0.18F, -0.18F, 1.6F, 0.36F, 0.36F),
                    PartPose.offsetAndRotation(2.05F, 0, 0, 0, side * 0.8F, -0.35F));
        }
        for (int i = 0; i < 2; i++) {
            int side = i == 0 ? -1 : 1;
            var feeler = parasite.addOrReplaceChild("feeler_" + i, CubeListBuilder.create().texOffs(40, 44)
                    .addBox(-0.5F, -3, -0.5F, 1, 3, 1),
                    PartPose.offsetAndRotation(side * 2.2F, -1.8F, -1.8F, -0.25F, 0, side * 0.45F));
            feeler.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(24, 36)
                    .addBox(-0.3F, -1.5F, -0.3F, 0.6F, 1.5F, 0.6F),
                    PartPose.offsetAndRotation(0, -2.8F, 0, 0.2F, 0, side * 0.35F));
        }
        return parasite;
    }

    /** Tapered surface roots join the parasite to the partially sculk-covered host. */
    public static void addRoot(PartDefinition parent, String name, float[][] points) {
        for (int i = 0; i < points.length - 1; i++) {
            float[] from = points[i], to = points[i + 1];
            float dx = to[0] - from[0], dy = to[1] - from[1], dz = to[2] - from[2];
            float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            float thickness = Math.max(0.4F, 1.1F - i * 0.12F);
            float pitch = (float) Math.atan2(-dy, Math.sqrt(dx * dx + dz * dz));
            float yaw = (float) Math.atan2(dx, dz);
            parent.addOrReplaceChild(name + "_" + i, CubeListBuilder.create().texOffs((i % 3) * 6, 48)
                    .addBox(-thickness / 2, -thickness / 2, 0, thickness, thickness, length + 0.15F),
                    PartPose.offsetAndRotation(from[0], from[1], from[2], pitch, yaw, 0));
        }
    }

    public static void animate(ModelPart parasite, float phase, float amount) {
        for (int i = 0; i < 6; i++) {
            var leg = parasite.getChild("leg_" + i);
            leg.yRot += Mth.cos(phase + (i % 3) * 1.4F + (i < 3 ? 0 : Mth.PI)) * amount * 0.6F;
            leg.getChild("foot").zRot += Mth.sin(phase + i) * amount * 0.25F;
        }
        for (int i = 0; i < 4; i++) {
            var arm = parasite.getChild("tendril_" + i);
            arm.yRot += Mth.sin(phase * 0.8F + i * 1.3F) * amount;
            arm.getChild("curl").yRot += Mth.cos(phase * 0.7F + i) * amount * 1.4F;
            arm.getChild("curl").getChild("tip").zRot += Mth.sin(phase + i) * amount;
        }
        for (int i = 0; i < 2; i++)
            parasite.getChild("feeler_" + i).zRot += Mth.sin(phase * 0.5F + i) * 0.035F;
    }
}

