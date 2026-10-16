package com.example.examplemod.client;

import com.google.gson.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.*;

/** Opt-in CPU mesh export for reviewing real native UVs without a graphics device. */
final class SculkVisualExport {
    static void write(ResourceManager resources) throws Exception {
        var roots=net.minecraft.client.model.geom.LayerDefinitions.createRoots();
        var textures=resources.listResources("textures/entity",p->p.getPath().endsWith(".png"));
        BufferedImage material,parasiteSkin;
        try(var in=resources.getResourceOrThrow(Identifier.fromNamespaceAndPath("examplemod","textures/entity/sculk_growth_material.png")).open()) { material=ImageIO.read(in); }
        try(var in=resources.getResourceOrThrow(Identifier.fromNamespaceAndPath("examplemod","textures/entity/skulk_parasite.png")).open()) { parasiteSkin=ImageIO.read(in); }
        var mesh=new MeshDefinition(); SkulkParasiteGeometry.addParasite(mesh.getRoot(),"parasite",PartPose.ZERO);
        var parasite=new AdaptiveSculkSkin(LayerDefinition.create(mesh,64,64).bakeRoot());
        for(String kind:new String[]{"llama","iron_golem","panda","trader_llama","camel","snow_golem","ravager","horse","polar_bear","copper_golem"}) {
            String skinFamily=kind.equals("trader_llama")?"llama":kind;
            var layer=roots.entrySet().stream().filter(e->e.getKey().toString().equals("minecraft:"+kind+"#main")).findFirst();
            var skin=textures.entrySet().stream().filter(e->e.getKey().getPath().contains("/"+skinFamily+"/")
                    || e.getKey().getPath().endsWith("/"+skinFamily+".png"))
                    .filter(e->!e.getKey().getPath().contains("/equipment/"))
                    .filter(e->!kind.equals("horse") || e.getKey().getPath().contains("/horse/horse"))
                    .filter(e->!e.getKey().getPath().contains("baby") && !e.getKey().getPath().contains("eyes")
                            && !e.getKey().getPath().contains("angry") && !e.getKey().getPath().contains("tame")
                            && !e.getKey().getPath().contains("wool"))
                    .sorted(java.util.Comparator.comparingInt(e->e.getKey().getPath().endsWith("/"+kind+".png")?0:1)).findFirst();
            if(layer.isEmpty() || skin.isEmpty())continue;
            var geometry=new AdaptiveSculkSkin(layer.get().getValue().bakeRoot());
            BufferedImage base;try(var in=skin.get().getValue().open()){base=ImageIO.read(in);}
            var growth=geometry.paint(base,material);
            var composite=new BufferedImage(base.getWidth(),base.getHeight(),BufferedImage.TYPE_INT_ARGB);
            var graphics=composite.createGraphics();graphics.drawImage(base,0,0,null);graphics.drawImage(growth,0,0,null);graphics.dispose();
            var root=new JsonObject();root.addProperty("name",kind);root.addProperty("source",skin.get().getKey().toString());
            root.addProperty("skin",png(composite));root.addProperty("parasiteSkin",png(parasiteSkin));
            root.add("host",faces(geometry,new Matrix4f()));
            var type=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace(kind));
            var placement=SculkAttachmentPlacement.fit(geometry,type);
            var transform=placement.transform(geometry);
            root.add("parasite",faces(parasite,transform));
            System.out.println("SCULK_VISUAL_EXPORT:"+new Gson().toJson(root));
        }
    }

    private static String png(BufferedImage image) throws Exception {
        var output=new ByteArrayOutputStream();ImageIO.write(image,"png",output);
        return Base64.getEncoder().encodeToString(output.toByteArray());
    }
    private static JsonArray faces(AdaptiveSculkSkin mesh,Matrix4f parent) {
        var faces=new JsonArray();
        for(var surface:mesh.surfaces)for(var polygon:surface.cube().polygons){
            var vertices=new JsonArray();var transform=new Matrix4f(parent).mul(surface.transform());
            for(var vertex:polygon.vertices()){
                var point=transform.transformPosition(new Vector3f(vertex.x(),vertex.y(),vertex.z()).div(16)).mul(16);
                var data=new JsonArray();data.add(point.x);data.add(point.y);data.add(point.z);data.add(vertex.u());data.add(vertex.v());vertices.add(data);
            }
            faces.add(vertices);
        }
        return faces;
    }
}

