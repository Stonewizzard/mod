package com.example.examplemod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.model.animal.cow.BabyCowModel;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.model.monster.zombie.BabyZombieModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/** Native render-submission checks run in opt-in client-data mode without a game window. */
public final class SculkRenderRegression {
    private SculkRenderRegression() {}

    public static void run(ResourceManager resources) {
        try {
            verify(new ZombieModel<ZombieRenderState>(LayerDefinition.create(
                    HumanoidModel.createMesh(CubeDeformation.NONE, 0), 64, 64).bakeRoot()),
                    new ZombieRenderState(), false, false, resources);
            verify(new BabyZombieModel<ZombieRenderState>(BabyZombieModel.createBodyLayer(CubeDeformation.NONE).bakeRoot()),
                    new ZombieRenderState(), false, true, resources);
            verify(new CowModel(CowModel.createBodyLayer().bakeRoot()),
                    new CowRenderState(), true, false, resources);
            verify(new BabyCowModel(BabyCowModel.createBodyLayer().bakeRoot()),
                    new CowRenderState(), true, true, resources);
            Files.writeString(Path.of("sculk-render-check-passed.txt"), "PASS\n");
            System.out.println("SCULK_RENDER_CHECK: PASS: native adult/baby models, packaged textures, cutout order, tint, outline state, invisibility, and legacy negative control");
        } catch (Exception failure) {
            throw new IllegalStateException("Sculk render regression failed", failure);
        }
    }

    private static <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void verify(
            M model, S state, boolean cow, boolean baby, ResourceManager resources) throws Exception {
        RenderLayerParent<S, M> parent = new RenderLayerParent<>() {
            public M getModel() { return model; }
        };
        var layer = new SkulkAttachmentLayer<>(parent, SkulkAttachmentModel.createLayer(cow).bakeRoot(), cow);
        state.isBaby = baby;
        Identifier growth = Identifier.fromNamespaceAndPath("examplemod", "textures/entity/skulk_"
                + (cow ? "cow" : "zombie") + (baby ? "_baby" : "") + "_growth.png");
        checkTexture(resources, growth);

        // Reproduce the old seven-argument call: -1 means an always-white outline.
        var legacy = new ArrayList<Draw>();
        collector(legacy, 0).submitModel(model, state, new PoseStack(), model.renderType(growth), 15728880, 0, -1);
        require(legacy.size() == 1 && legacy.get(0).outline() == -1,
                "The legacy overload must reproduce the reported white outline");

        for (int outline : new int[]{0, 0xFF37A7D4}) {
            state.outlineColor = outline;
            var draws = new ArrayList<Draw>();
            layer.submit(new PoseStack(), collector(draws, 0), 15728880, state, 0, 0);
            require(draws.size() == 2, "Expected growth and parasite submissions");
            Draw skin = draws.get(0);
            require(skin.order() > 0, "Growth must render after the host's base skin");
            require(skin.type() == RenderTypes.entityCutoutZOffset(growth),
                    "Growth must use an offset cutout pass, not the host's opaque render type");
            for (Draw draw : draws) {
                require(draw.tint() == -1, "Textures must retain their original colors");
                require(draw.outline() == outline,
                        "Layer must preserve the actual glowing state; it must not force a white outline");
            }
            require(draws.get(1).order() > 0, "Attachment must follow the base skin");
        }
        state.isInvisible = true;
        var hidden = new ArrayList<Draw>();
        layer.submit(new PoseStack(), collector(hidden, 0), 15728880, state, 0, 0);
        require(hidden.isEmpty(), "Invisible hosts must hide the growth and attachment");
    }

    private static void checkTexture(ResourceManager resources, Identifier texture) throws Exception {
        try (var input = resources.getResourceOrThrow(texture).open()) {
            var image = ImageIO.read(input);
            require(image != null && image.getWidth() == 64 && image.getHeight() == 64,
                    "Missing or invalid host growth texture: " + texture);
            int opaque = 0, transparent = 0;
            for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++) {
                int alpha = image.getRGB(x, y) >>> 24;
                if (alpha == 0) transparent++;
                else if (alpha == 255) opaque++;
                else throw new AssertionError("Growth masks must have crisp cutout alpha");
            }
            require(opaque > 100 && transparent > 100,
                    "Growth must contain both infected and exposed skin regions");
        }
    }

    private static SubmitNodeCollector collector(List<Draw> draws, int order) {
        return (SubmitNodeCollector) Proxy.newProxyInstance(SubmitNodeCollector.class.getClassLoader(),
                new Class<?>[]{SubmitNodeCollector.class}, (proxy, method, args) -> {
                    if (method.getName().equals("order")) return collector(draws, (Integer) args[0]);
                    if (method.isDefault()) return InvocationHandler.invokeDefault(proxy, method, args);
                    if (method.getName().equals("submitModel")) {
                        require(args.length == 9, "Capture the full native submission signature");
                        draws.add(new Draw((RenderType) args[3], (Integer) args[6], (Integer) args[8], order));
                        return null;
                    }
                    throw new AssertionError("Unexpected collector call: " + method);
                });
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private record Draw(RenderType type, int tint, int outline, int order) {}
}
