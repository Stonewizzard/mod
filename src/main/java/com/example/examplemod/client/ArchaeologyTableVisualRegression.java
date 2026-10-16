package com.example.examplemod.client;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.imageio.ImageIO;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Exercise actual table item submissions without requiring a graphics device. */
public final class ArchaeologyTableVisualRegression {
    public static void run(ResourceManager resources) throws Exception {
        var blockstates=json(resources,"blockstates/archaeology_table.json").getAsJsonObject("variants");
        require(blockstates.size()==8,"Table needs both halves in all four directions");
        for(var entry:blockstates.entrySet()){
            var model=entry.getValue().getAsJsonObject().get("model").getAsString().split(":",2)[1];
            var geometry=json(resources,"models/"+model+".json");
            require(geometry.getAsJsonArray("elements").size()>8,"Table geometry must include its top, apron, shelf and legs");
            for(var texture:geometry.getAsJsonObject("textures").entrySet()){
                String name=texture.getValue().getAsString();
                if(name.startsWith("examplemod:"))try(var input=resources.getResourceOrThrow(id("textures/"+name.split(":",2)[1]+".png")).open()){
                    require(ImageIO.read(input)!=null,"Invalid table material "+name);
                }
            }
        }
        for(String item:new String[]{"archaeology_table","archaeology_drill","archaeology_hammer","magnifying_glass"}){
            var definition=json(resources,"items/"+item+".json");
            require(definition.getAsJsonObject("model").get("model").getAsString().equals("examplemod:item/"+item),"Missing item model reference");
            json(resources,"models/item/"+item+".json");
        }
        var renderer=new ArchaeologyTableRenderer((ItemModelResolver)null);
        for(var facing:Direction.Plane.HORIZONTAL){
            var state=renderer.createRenderState();state.facing=facing;state.lightCoords=0x123456;
            var draws=new ArrayList<Matrix4f>();var items=new ArrayList<ItemStackRenderState>();
            for(int i=0;i<5;i++)items.add(new ItemStackRenderState(){
                @Override public boolean isEmpty(){return false;}
                @Override public void submit(PoseStack pose,SubmitNodeCollector unused,int light,int overlay,int outline){
                    require(light==0x123456 && overlay==OverlayTexture.NO_OVERLAY && outline==0,"Table items need world light and neutral overlay");
                    draws.add(new Matrix4f(pose.last().pose()));
                }
            });
            state.items=items;var stack=new PoseStack();renderer.submit(state,stack,null,null);
            require(draws.size()==5 && stack.last().pose().equals(new Matrix4f()),"Every occupied slot must draw once without leaking its transform");
            for(int i=0;i<5;i++){
                var center=draws.get(i).transformPosition(new Vector3f());var p=ArchaeologyTableRenderer.PLACES[i];
                var right=facing.getClockWise();var back=facing.getOpposite();
                float x=.5F+right.getStepX()*p[0]+back.getStepX()*p[1],z=.5F+right.getStepZ()*p[0]+back.getStepZ()*p[1];
                require(Math.abs(center.x-x)<.0001 && Math.abs(center.z-z)<.0001 && center.y>1.01F,"A tabletop item detached or rotated to the wrong half");
                var normal=draws.get(i).transformDirection(new Vector3f(0,0,1)).normalize();
                require(Math.abs(normal.y)>.99F,"Table items must lie flat on the top");
            }
            state.items=List.of(new ItemStackRenderState());draws.clear();renderer.submit(state,stack,null,null);
            require(draws.isEmpty(),"Empty slots must not leave stale display items");
        }
        if(Boolean.getBoolean("examplemod.tableVisualExport"))for(String path:new String[]{"textures/item/brush.png","textures/item/arms_up_pottery_sherd.png","textures/item/prize_pottery_sherd.png"}){
            var id=Identifier.withDefaultNamespace(path);
            if(resources.getResource(id).isPresent())try(var input=resources.getResourceOrThrow(id).open()){
                System.out.println("TABLE_ITEM_TEXTURE:"+path+":"+Base64.getEncoder().encodeToString(input.readAllBytes()));
            }
        }
        System.out.println("ARCHAEOLOGY_RENDER_CHECK: PASS: eight block variants, packaged models/materials, five flat synchronized slot displays, four rotations, neutral overlays and empty slots");
    }
    private static Identifier id(String path){return Identifier.fromNamespaceAndPath("examplemod",path);}
    private static JsonObject json(ResourceManager resources,String path) throws Exception {
        try(var input=new InputStreamReader(resources.getResourceOrThrow(id(path)).open(),StandardCharsets.UTF_8)){
            return JsonParser.parseReader(input).getAsJsonObject();
        }
    }
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
