package com.example.examplemod.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/** Bespoke visual-only infection for ancient hosts, preserving native animation and skins. */
public final class AncientHostInfectionLayer<S extends LivingEntityRenderState,M extends EntityModel<? super S>> extends RenderLayer<S,M> {
    private static final Identifier CRUNCHER_SKIN=Identifier.fromNamespaceAndPath("examplemod","textures/entity/cruncher.png");
    private static final Set<Identifier> LIVE_TEXTURES=new HashSet<>();
    private final boolean cruncher;
    private final Function<S,Identifier> baseTexture;
    private final Function<ModelPart,Bundle> testBundle;
    private final Map<Key,Bundle> bundles=new HashMap<>();
    private record Key(ModelPart root,Identifier skin) {}

    public AncientHostInfectionLayer(LivingEntityRenderer<?,S,M> parent,boolean cruncher) {
        super(parent);this.cruncher=cruncher;
        baseTexture=state->cruncher?CRUNCHER_SKIN:parent.getTextureLocation(state);
        testBundle=null;
    }

    AncientHostInfectionLayer(RenderLayerParent<S,M> parent,boolean cruncher,Function<ModelPart,Bundle> bundle) {
        super(parent);this.cruncher=cruncher;this.testBundle=bundle;this.baseTexture=null;
    }

    static void clearTextures() {
        var manager=Minecraft.getInstance().getTextureManager();
        LIVE_TEXTURES.forEach(manager::release);LIVE_TEXTURES.clear();
    }

    @Override public void submit(PoseStack stack,SubmitNodeCollector collector,int light,S host,float yaw,float pitch) {
        if(host.isInvisible || !host.getRenderDataOrDefault(AdaptiveSculkLayer.INFECTED,false))return;
        M model=getParentModel();
        Bundle bundle=testBundle!=null?testBundle.apply(model.root()):bundles.computeIfAbsent(new Key(model.root(),baseTexture.apply(host)),key->
                Bundle.create(Minecraft.getInstance().getResourceManager(),key.root,key.skin,cruncher,AncientHostInfectionLayer::upload));
        // UV extraction resets model parts; restore this host's animation and baby scales afterwards.
        model.setupAnim(host);
        var state=bundle.growth.snapshot(host,cruncher);
        int frame=Math.floorMod((int)(host.ageInTicks*(.25F+state.activity*.09F)),16);
        int overlay=LivingEntityRenderer.getOverlayCoords(host,0);
        collector.order(1).submitModel(model,host,stack,RenderTypes.entityCutoutZOffset(bundle.skin[frame]),
                light,overlay,-1,null,host.outlineColor);
        collector.order(2).submitModel(bundle.growth,state,stack,RenderTypes.entityCutoutZOffset(bundle.armor[frame]),
                light,overlay,-1,null,host.outlineColor);
        // Only the circulation channels and nodes emit light, never the whole skin or outline.
        collector.order(3).submitModel(model,host,stack,RenderTypes.entityCutoutZOffset(bundle.skinGlow[frame]),
                0xf000f0,0,-1,null,host.outlineColor);
        collector.order(4).submitModel(bundle.growth,state,stack,RenderTypes.entityCutoutZOffset(bundle.armorGlow[frame]),
                0xf000f0,0,-1,null,host.outlineColor);
    }

    private static Identifier upload(String name,BufferedImage image) {
        var id=Identifier.fromNamespaceAndPath("examplemod","dynamic/ancient_infection/"+name);
        var pixels=new NativeImage(image.getWidth(),image.getHeight(),false);
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)pixels.setPixel(x,y,image.getRGB(x,y));
        Minecraft.getInstance().getTextureManager().register(id,new DynamicTexture(()->"Ancient host infection: "+name,pixels));
        LIVE_TEXTURES.add(id);return id;
    }

    static final class Bundle {
        final AncientHostGrowthModel growth;
        final Identifier[] skin=new Identifier[16],skinGlow=new Identifier[16],armor=new Identifier[16],armorGlow=new Identifier[16];
        final AncientHostSkin.Result painted;
        final BufferedImage nativeSkin,atlas;
        private Bundle(AncientHostGrowthModel growth,AncientHostSkin.Result painted,BufferedImage nativeSkin,BufferedImage atlas) {
            this.growth=growth;this.painted=painted;this.nativeSkin=nativeSkin;this.atlas=atlas;
        }
        static Bundle create(ResourceManager resources,ModelPart nativeRoot,Identifier skin,boolean cruncher,
                BiFunction<String,BufferedImage,Identifier> uploader) {
            try {
                String species=cruncher?"cruncher":"sniffer";
                BufferedImage nativeSkin,material,atlas;
                try(var input=resources.getResourceOrThrow(skin).open()){nativeSkin=ImageIO.read(input);}
                try(var input=resources.getResourceOrThrow(resource("materials.png")).open()){material=ImageIO.read(input);}
                try(var input=resources.getResourceOrThrow(resource(species+"_growth.png")).open()){atlas=ImageIO.read(input);}
                var result=AncientHostSkin.create(new AdaptiveSculkSkin(nativeRoot),nativeSkin,material,cruncher);
                var bundle=new Bundle(AncientHostGrowthModel.create(nativeRoot,cruncher),result,nativeSkin,atlas);
                String prefix=species+"_"+Integer.toUnsignedString(System.identityHashCode(nativeRoot),36)+"_"+Integer.toUnsignedString(skin.hashCode(),36);
                for(int i=0;i<16;i++) {
                    bundle.skin[i]=i==0||cruncher?uploader.apply(prefix+"_skin_"+i,AncientHostSkin.colorFrame(result.growth(),i,cruncher)):bundle.skin[0];
                    bundle.armor[i]=i==0||cruncher?uploader.apply(prefix+"_armor_"+i,AncientHostSkin.colorFrame(atlas,i,cruncher)):bundle.armor[0];
                    bundle.skinGlow[i]=uploader.apply(prefix+"_veins_"+i,AncientHostSkin.glowFrame(result.veins(),i));
                    bundle.armorGlow[i]=uploader.apply(prefix+"_nodes_"+i,AncientHostSkin.glowFrame(atlas,i));
                }
                return bundle;
            }catch(Exception failure){throw new IllegalStateException("Could not build fitted ancient-host infection for "+skin,failure);}
        }
        private static Identifier resource(String file) {
            return Identifier.fromNamespaceAndPath("examplemod","textures/entity/ancient_infection/"+file);
        }
    }
}
