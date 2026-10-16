package com.example.examplemod.client;

import com.example.examplemod.SkulkStatus;
import com.example.examplemod.ExampleMod;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.image.BufferedImage;
import java.util.*;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.*;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Keep each native renderer/variant and add localized tissue plus an animated parasite. */
public final class AdaptiveSculkLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S,M> {
    static final ContextKey<Boolean> INFECTED = new ContextKey<>(id("infected"));
    static final ContextKey<Boolean> WOOL = new ContextKey<>(id("wool"));
    private static final Identifier PARASITE = id("textures/entity/skulk_parasite.png");
    private static final Identifier TISSUE = id("textures/entity/sculk_growth_material.png");
    private static final Map<ModelPart,AdaptiveSculkSkin> GEOMETRY = new IdentityHashMap<>();
    private static final Map<SkinKey,Identifier> SKINS = new HashMap<>();
    private static final Set<SkinKey> FAILED = new HashSet<>();
    private static BufferedImage material;
    private final LivingEntityRenderer<?,S,M> renderer;
    private final SkulkParasiteModel parasite;
    private final SkulkAttachmentLayer<S,M> approved;
    private final List<Outer> outers;
    private final EntityType<?> hostType;
    private final Map<ModelPart,SculkAttachmentPlacement> placements = new IdentityHashMap<>();
    private record SkinKey(ModelPart root, Identifier source) {}
    private record Outer(EntityModel<?> adult, EntityModel<?> baby, Identifier adultSkin, Identifier babySkin, boolean wool) {}

    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(ExampleMod.MODID,path); }

    private AdaptiveSculkLayer(LivingEntityRenderer<?,S,M> renderer, EntityRenderersEvent.AddLayers event, EntityType<?> type) {
        super(renderer);
        this.renderer=renderer;
        this.hostType=type;
        var mesh=new MeshDefinition();
        SkulkParasiteGeometry.addParasite(mesh.getRoot(), "parasite", PartPose.ZERO);
        parasite=new SkulkParasiteModel(LayerDefinition.create(mesh,64,64).bakeRoot());
        approved = type==EntityTypes.COW || type==EntityTypes.ZOMBIE
                ? new SkulkAttachmentLayer<>(renderer,event.getContext(),type==EntityTypes.COW) : null;
        outers=findOuterLayers(renderer);
    }

    public static void modifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<LivingEntityRenderer<LivingEntity,LivingEntityRenderState,?>>() {}, (entity,state)-> {
            state.setRenderData(INFECTED, entity instanceof Mob && SkulkStatus.state(entity)!=0);
            state.setRenderData(WOOL, entity instanceof net.minecraft.world.entity.animal.sheep.Sheep sheep && !sheep.isSheared());
        });
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        AncientHostInfectionLayer.clearTextures();
        // Renderer/model recreation is part of a resource reload; discard GPU masks of old skins.
        var manager=Minecraft.getInstance().getTextureManager();
        SKINS.values().forEach(manager::release);
        SKINS.clear(); GEOMETRY.clear(); FAILED.clear(); material=null;
        for (var type:event.getEntityTypes()) {
            if (!SkulkStatus.supports(type)) continue;
            if (event.getRenderer(type) instanceof LivingEntityRenderer renderer) {
                if(type==EntityTypes.SNIFFER || type==ExampleMod.CRUNCHER.get())
                    renderer.addLayer(new AncientHostInfectionLayer(renderer,type==ExampleMod.CRUNCHER.get()));
                else renderer.addLayer(new AdaptiveSculkLayer(renderer,event,type));
            }
        }
    }

    @Override public void submit(PoseStack stack, SubmitNodeCollector collector, int light,S state,float yaw,float pitch) {
        if (state.isInvisible || !state.getRenderDataOrDefault(INFECTED,false)) return;
        if (approved!=null) { approved.submit(stack,collector,light,state,yaw,pitch); return; }
        M host=getParentModel(); host.setupAnim(state);
        EntityModel<?> attachmentHost=host;
        drawGrowth(host,renderer.getTextureLocation(state),state,stack,collector,light);
        for(var outer:outers) {
            if(outer.wool && !state.getRenderDataOrDefault(WOOL,false)) continue;
            var model=state.isBaby && outer.baby!=null ? outer.baby:outer.adult;
            var skin=state.isBaby && outer.babySkin!=null ? outer.babySkin:outer.adultSkin;
            if(model!=null && skin!=null) {
                drawOuter(model,skin,state,stack,collector,light);
                if(outer.wool) attachmentHost=model;
            }
        }
        attach(attachmentHost,state,stack,collector,light);
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private void drawOuter(EntityModel model,Identifier skin,S state,PoseStack stack,SubmitNodeCollector collector,int light) {
        model.setupAnim(state);
        drawGrowth(model,skin,state,stack,collector,light);
    }

    private <T extends LivingEntityRenderState> void drawGrowth(EntityModel<? super T> model,Identifier skin,T state,
            PoseStack stack,SubmitNodeCollector collector,int light) {
        var texture=texture(model.root(),skin);
        if(texture!=null) collector.order(2).submitModel(model,state,stack,RenderTypes.entityCutoutZOffset(texture),
                light,LivingEntityRenderer.getOverlayCoords(state,0),-1,null,state.outlineColor);
    }

    private void attach(EntityModel<?> host,S state,PoseStack stack,SubmitNodeCollector collector,int light) {
        var geometry=GEOMETRY.computeIfAbsent(host.root(),AdaptiveSculkSkin::new);
        if(geometry.anchor==null) return;
        var placement=placements.computeIfAbsent(host.root(),root->SculkAttachmentPlacement.fit(geometry,hostType));
        host.root().visit(new PoseStack(),(pose,path,index,cube)-> {
            if(!path.equals(geometry.anchor.path()) || index!=geometry.anchor.index()) return;
            stack.pushPose();
            stack.mulPose(new Matrix4f(pose.pose()));
            var anchor=new Vector3f(placement.point).fma(2.1F*placement.scale,placement.normal).div(16);
            stack.translate(anchor.x,anchor.y,anchor.z);
            stack.mulPose(new Matrix4f().rotation(new Quaternionf().rotationTo(new Vector3f(0,-1,0),placement.normal)));
            stack.scale(placement.scale,placement.scale,placement.scale);
            collector.order(3).submitModel(parasite,state,stack,RenderTypes.entityCutoutZOffset(PARASITE),
                    light,LivingEntityRenderer.getOverlayCoords(state,0),-1,null,state.outlineColor);
            stack.popPose();
        });
    }

    private static Identifier texture(ModelPart root,Identifier source) {
        var key=new SkinKey(root,source);
        if(FAILED.contains(key)) return null;
        return SKINS.computeIfAbsent(key, ignored -> {
            var minecraft=Minecraft.getInstance();
            var resources=minecraft.getResourceManager();
            try {
                if(material==null) try(var input=resources.getResourceOrThrow(TISSUE).open()) { material=ImageIO.read(input); }
                BufferedImage base;
                try(var input=resources.getResourceOrThrow(source).open()) { base=ImageIO.read(input); }
                if(base==null) throw new java.io.IOException("Invalid entity skin: "+source);
                var image=GEOMETRY.computeIfAbsent(root,AdaptiveSculkSkin::new).paint(base,material);
                var nativeImage=SculkTexturePixels.copy(image);
                var location=id("dynamic/sculk/"+SKINS.size());
                minecraft.getTextureManager().register(location,new DynamicTexture(()->"Sculk growth: "+source,nativeImage));
                return location;
            } catch(Exception failure) {
                FAILED.add(key);
                ExampleMod.LOGGER.warn("Could not build sculk overlay for {}",source,failure);
                return null;
            }
        });
    }

    /** Native fur/clothes have separate geometry. Reuse those exact models and texture IDs. */
    private static List<Outer> findOuterLayers(LivingEntityRenderer<?,?,?> renderer) {
        var result=new ArrayList<Outer>();
        for(var layer:renderer.layers) {
            String name=layer.getClass().getSimpleName();
            if(!Set.of("SheepWoolLayer","SkeletonClothingLayer","DrownedOuterLayer","SlimeOuterLayer").contains(name)) continue;
            EntityModel<?> adult=null,baby=null; Identifier skin=null,babySkin=null;
            for(Class<?> type=layer.getClass();type!=RenderLayer.class && type!=null;type=type.getSuperclass())
                for(Field field:type.getDeclaredFields()) {
                    if(!EntityModel.class.isAssignableFrom(field.getType()) && field.getType()!=Identifier.class) continue;
                    try {
                        field.setAccessible(true); var value=field.get(layer); boolean isBaby=field.getName().toLowerCase(Locale.ROOT).contains("baby");
                        if(value instanceof EntityModel<?> model) { if(isBaby)baby=model; else adult=model; }
                        if(value instanceof Identifier texture) { if(isBaby)babySkin=texture; else skin=texture; }
                    } catch(ReflectiveOperationException ignored) { /* Unknown optional layer: native renderer remains intact. */ }
                }
            if(adult!=null && skin!=null) result.add(new Outer(adult,baby,skin,babySkin,name.equals("SheepWoolLayer")));
        }
        return List.copyOf(result);
    }
}

