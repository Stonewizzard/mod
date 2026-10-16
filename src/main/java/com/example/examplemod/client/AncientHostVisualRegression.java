package com.example.examplemod.client;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;
import javax.imageio.ImageIO;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.sniffer.SnifferModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SnifferRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Vector3f;

/** Native adult/baby animation, UV and deferred-pass verification without a graphics device. */
final class AncientHostVisualRegression {
    static void run(ResourceManager resources) throws Exception {
        var roots=LayerDefinitions.createRoots();
        var sniffers=roots.entrySet().stream().filter(e->e.getKey().toString().startsWith("minecraft:sniffer")
                && e.getKey().toString().endsWith("#main")).toList();
        require(!sniffers.isEmpty(),"Missing native Sniffer model");
        System.out.println("ANCIENT_HOST_NATIVE: "+sniffers.stream().map(e->e.getKey().toString()).toList());
        for(var entry:sniffers) {
            boolean baby=entry.getKey().toString().contains("baby");
            var model=new SnifferModel(entry.getValue().bakeRoot());
            var state=new SnifferRenderState();state.isBaby=baby;
            verify(model,state,false,baby,resources);
        }
        // The vanilla renderer can scale snifflets outside the model; test that case as well.
        if(sniffers.size()==1) {
            var model=new SnifferModel(sniffers.getFirst().getValue().bakeRoot());
            var state=new SnifferRenderState();state.isBaby=true;
            verify(model,state,false,true,resources);
        }
        for(boolean baby:new boolean[]{false,true}) {
            var state=new CruncherRenderState();state.isBaby=baby;
            verify(new CruncherModel(CruncherModel.createBodyLayer().bakeRoot()),state,true,baby,resources);
        }
        System.out.println("ANCIENT_HOST_VISUAL_CHECK: PASS: adult/baby native bones, active jaws, changing tendrils/veins, crisp variant-safe masks, emissive passes, real outline and invisibility");
    }

    private static <S extends LivingEntityRenderState,M extends EntityModel<? super S>> void verify(
            M model,S state,boolean cruncher,boolean baby,ResourceManager resources) throws Exception {
        Identifier skin=cruncher?Identifier.fromNamespaceAndPath("examplemod","textures/entity/cruncher.png"):
                Identifier.withDefaultNamespace("textures/entity/sniffer/sniffer.png");
        if(!cruncher && baby) {
            var nativeBaby=resources.listResources("textures/entity/sniffer",p->p.getPath().endsWith(".png")
                    && (p.getPath().contains("baby")||p.getPath().contains("snifflet"))).keySet().stream().findFirst();
            if(nativeBaby.isPresent())skin=nativeBaby.get();
        }
        var uploaded=new HashMap<Identifier,BufferedImage>();
        var bundle=AncientHostInfectionLayer.Bundle.create(resources,model.root(),skin,cruncher,(name,image)->{
            var id=Identifier.fromNamespaceAndPath("examplemod","test/"+name);uploaded.put(id,image);return id;
        });
        checkMasks(bundle);
        RenderLayerParent<S,M> parent=()->model;
        var layer=new AncientHostInfectionLayer<S,M>(parent,cruncher,root->bundle);
        state.setRenderData(AdaptiveSculkLayer.INFECTED,true);
        var oldMeshes=new ArrayList<String>();
        for(int frame=0;frame<16;frame++) {
            state.ageInTicks=frame*4;state.walkAnimationPos=frame*.75F;state.walkAnimationSpeed=.6F;
            if(state instanceof CruncherRenderState active)active.crunching=frame>=8;
            if(frame>=8)state.walkAnimationSpeed=0;
            if(frame==8 && state instanceof SnifferRenderState sniffer) {
                for(var field:SnifferRenderState.class.getFields()) {
                    if(field.getName().toLowerCase(Locale.ROOT).contains("dig")
                            && field.get(sniffer) instanceof net.minecraft.world.entity.AnimationState animation) animation.start(0);
                }
            }
            state.outlineColor=frame%2==0?0:0xff36a8c4;
            var draws=new ArrayList<Draw>();
            layer.submit(new PoseStack(),collector(draws,0,uploaded),0,state,0,0);
            require(draws.size()==4,"Expected tissue, raised growth and two emissive passes");
            for(int i=0;i<draws.size();i++) {
                var draw=draws.get(i);
                require(draw.order==i+1 && draw.tint==-1 && draw.outline==state.outlineColor,"Wrong pass/tint/outline state");
                require(uploaded.containsKey(draw.texture),"A draw referenced an unbuilt texture");
                if(i>=2)require(draw.light==0xf000f0,"Veins and nodes must remain emissive in darkness");
            }
            var snapshot=bundle.growth.snapshot(state,cruncher);
            bundle.growth.setupAnim(snapshot);
            for(var entry:snapshot.poses.entrySet())require(AncientHostGrowthModel.resolve(bundle.growth.root(),entry.getKey()).storePose().equals(entry.getValue()),
                    "Growth lost a native bone's pose: "+entry.getKey());
            for(var entry:snapshot.scales.entrySet()) {
                var part=AncientHostGrowthModel.resolve(bundle.growth.root(),entry.getKey());var scale=entry.getValue();
                require(part.xScale==scale.x() && part.yScale==scale.y() && part.zScale==scale.z(),
                        "Growth lost a native bone's scale: "+entry.getKey());
            }
            oldMeshes.add(posedFaces(bundle.growth.root()).toString());
            if(Boolean.getBoolean("examplemod.ancientHostVisualExport") && (!baby || frame==12)) {
                model.setupAnim(state);bundle.growth.setupAnim(snapshot);
                var composite=copy(bundle.nativeSkin);
                var graphics=composite.createGraphics();graphics.drawImage(uploaded.get(draws.get(0).texture),0,0,null);
                graphics.dispose();
                var armor=copy(uploaded.get(draws.get(1).texture));
                var json=new JsonObject();json.addProperty("name",(cruncher?"cruncher":"sniffer")+(baby?"_baby":"_adult"));
                json.addProperty("frame",frame);json.addProperty("active",frame>=8);json.addProperty("skin",png(composite));json.addProperty("parasiteSkin",png(armor));
                json.addProperty("skinGlow",png(uploaded.get(draws.get(2).texture)));
                json.addProperty("parasiteGlow",png(uploaded.get(draws.get(3).texture)));
                json.add("host",posedFaces(model.root()));json.add("parasite",posedFaces(bundle.growth.root()));
                var compressed=new ByteArrayOutputStream();
                try(var zip=new java.util.zip.DeflaterOutputStream(compressed)) {
                    zip.write(new Gson().toJson(json).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
                System.out.println("ANCIENT_HOST_VISUAL_EXPORT_Z:"+Base64.getEncoder().encodeToString(compressed.toByteArray()));
            }
        }
        require(new HashSet<>(oldMeshes).size()>8,"Colony geometry must animate");
        require(!Arrays.equals(uploaded.get(bundle.skinGlow[0]).getRGB(0,0,bundle.nativeSkin.getWidth(),bundle.nativeSkin.getHeight(),null,0,bundle.nativeSkin.getWidth()),
                uploaded.get(bundle.skinGlow[8]).getRGB(0,0,bundle.nativeSkin.getWidth(),bundle.nativeSkin.getHeight(),null,0,bundle.nativeSkin.getWidth())),"Vein frames must visibly change");
        state.isInvisible=true;var invisible=new ArrayList<Draw>();layer.submit(new PoseStack(),collector(invisible,0,uploaded),0,state,0,0);
        require(invisible.isEmpty(),"Invisible hosts must hide the entire infection");
        state.isInvisible=false;state.setRenderData(AdaptiveSculkLayer.INFECTED,false);
        var clean=new ArrayList<Draw>();layer.submit(new PoseStack(),collector(clean,0,uploaded),0,state,0,0);
        require(clean.isEmpty(),"Uninfected hosts must remain unchanged");
    }

    private static void checkMasks(AncientHostInfectionLayer.Bundle bundle) {
        int exposed=0,growth=0,glow=0;
        for(int y=0;y<bundle.nativeSkin.getHeight();y++)for(int x=0;x<bundle.nativeSkin.getWidth();x++) {
            int alpha=bundle.painted.growth().getRGB(x,y)>>>24;
            require(alpha==0||alpha==255,"Growth alpha must be crisp");
            require(alpha==0||(bundle.nativeSkin.getRGB(x,y)>>>24)>=128,"Growth must respect native alpha holes");
            if((bundle.nativeSkin.getRGB(x,y)>>>24)>=128 && alpha==0)exposed++;
            if(alpha==255)growth++;
            if((bundle.painted.veins().getRGB(x,y)>>>24)>0)glow++;
        }
        require(exposed>300 && growth>500 && glow>30 && glow<growth,"Native skin, sculk tissue and localized veins must all be present");
    }

    private static SubmitNodeCollector collector(List<Draw> draws,int order,Map<Identifier,BufferedImage> uploaded) {
        return (SubmitNodeCollector)Proxy.newProxyInstance(SubmitNodeCollector.class.getClassLoader(),new Class<?>[]{SubmitNodeCollector.class},(proxy,method,args)->{
            if(method.getName().equals("order"))return collector(draws,(Integer)args[0],uploaded);
            if(method.isDefault())return InvocationHandler.invokeDefault(proxy,method,args);
            if(method.getName().equals("submitModel")) {
                require(args.length==9,"Use the full native submission signature");
                var type=(net.minecraft.client.renderer.rendertype.RenderType)args[3];
                var texture=uploaded.keySet().stream().filter(id->
                        type==net.minecraft.client.renderer.rendertype.RenderTypes.entityCutoutZOffset(id)
                        || type==net.minecraft.client.renderer.rendertype.RenderTypes.eyes(id)).findFirst().orElseThrow();
                draws.add(new Draw(order,texture,(Integer)args[4],(Integer)args[6],(Integer)args[8]));return null;
            }
            throw new AssertionError("Unexpected collector method "+method);
        });
    }

    private record Draw(int order,Identifier texture,int light,int tint,int outline) {}
    private static BufferedImage copy(BufferedImage source){var copy=new BufferedImage(source.getWidth(),source.getHeight(),BufferedImage.TYPE_INT_ARGB);var g=copy.createGraphics();g.drawImage(source,0,0,null);g.dispose();return copy;}
    private static String png(BufferedImage image)throws Exception{var out=new ByteArrayOutputStream();ImageIO.write(image,"png",out);return Base64.getEncoder().encodeToString(out.toByteArray());}
    private static JsonArray posedFaces(ModelPart root) {
        var faces=new JsonArray();
        root.visit(new PoseStack(),(pose,path,index,cube)->{
            var part=root;if(!part.visible)return;
            for(String token:path.split("/"))if(!token.isEmpty()){part=part.getChild(token);if(!part.visible)return;}
            for(var polygon:cube.polygons){var vertices=new JsonArray();for(var vertex:polygon.vertices()) {
                var p=pose.pose().transformPosition(new Vector3f(vertex.x(),vertex.y(),vertex.z()).div(16)).mul(16);
                var v=new JsonArray();v.add(p.x);v.add(p.y);v.add(p.z);v.add(vertex.u());v.add(vertex.v());vertices.add(v);
            }faces.add(vertices);}
        });return faces;
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
