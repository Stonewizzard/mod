package com.example.examplemod.client;

import com.google.gson.*;
import com.example.examplemod.AncientAnvilStructure;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class AncientAnvilVisualRegression {
    private static Identifier id(String p){return Identifier.fromNamespaceAndPath("examplemod",p);}
    public static void run(ResourceManager r)throws Exception{
        for(String name:new String[]{"anvil_iron","anvil_workface","anvil_workface_edge","forge_bronze","forge_soot"})try(var in=r.getResourceOrThrow(id("textures/block/"+name+".png")).open()){var png=ImageIO.read(in);if(png==null||png.getWidth()!=16||png.getHeight()!=(name.equals("anvil_workface")?48:16))throw new IllegalStateException("Invalid anvil material "+name);}
        for(String sprite:new String[]{"ancient_ingot","ancient_scrap"})try(var in=r.getResourceOrThrow(id("textures/item/"+sprite+".png")).open()){var png=ImageIO.read(in);if(png==null||png.getWidth()!=32||png.getHeight()!=32)throw new IllegalStateException("Invalid ancient ingot sprite");for(int i=0;i<32;i++)if((png.getRGB(0,i)>>>24)!=0||(png.getRGB(31,i)>>>24)!=0||(png.getRGB(i,0)>>>24)!=0||(png.getRGB(i,31)>>>24)!=0)throw new IllegalStateException("Ingot sprite clips its transparent margin");}
        for(int cell=0;cell<6;cell++)try(var in=r.getResourceOrThrow(id("models/block/ancient_anvil_cell_"+cell+".json")).open()){
            var model=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
            if(model.getAsJsonArray("elements").isEmpty())throw new IllegalStateException("Empty owned anvil cell");
            for(var value:model.getAsJsonArray("elements")){
                var e=value.getAsJsonObject();ownedGeometry(e,cell);
                for(var face:e.getAsJsonObject("faces").entrySet()){var uv=face.getValue().getAsJsonObject().getAsJsonArray("uv");if(uv==null||uv.size()!=4)throw new IllegalStateException("Missing explicit UV");for(var n:uv)if(n.getAsDouble()<0||n.getAsDouble()>16)throw new IllegalStateException("Invalid anvil UV");}
            }
        }
        // The top atlas is three blocks long. Verify texel density and global
        // continuity rather than accepting any in-bounds, potentially stretched UVs.
        for(int cell=0;cell<6;cell++)try(var in=r.getResourceOrThrow(id("models/block/ancient_anvil_cell_"+cell+".json")).open()){
            var model=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
            for(var value:model.getAsJsonArray("elements")){
                var e=value.getAsJsonObject();var up=e.getAsJsonObject("faces").getAsJsonObject("up");
                if(!up.get("texture").getAsString().equals("#workface"))continue;
                var a=e.getAsJsonArray("from");var b=e.getAsJsonArray("to");var uv=up.getAsJsonArray("uv");
                double worldZ=AncientAnvilStructure.CELLS.get(cell).z()*16;
                if(Math.abs(uv.get(0).getAsDouble()-a.get(0).getAsDouble())>1e-5||Math.abs(uv.get(2).getAsDouble()-b.get(0).getAsDouble())>1e-5
                    ||Math.abs(uv.get(1).getAsDouble()*3-(a.get(2).getAsDouble()+worldZ+16))>1e-5||Math.abs(uv.get(3).getAsDouble()*3-(b.get(2).getAsDouble()+worldZ+16))>1e-5)
                    throw new IllegalStateException("Stretched or discontinuous workface texels in cell "+cell);
            }
        }
        for(var facing:net.minecraft.core.Direction.Plane.HORIZONTAL){
            int[] diagonal={0};var outline=AncientAnvilOutline.shape(facing);
            outline.forAllEdges((x,y,z,X,Y,Z)->{if(Math.abs(X-x)>.01&&Math.abs(Z-z)>.01)diagonal[0]++;});
            if(diagonal[0]!=8||outline.bounds().maxY<1.7)throw new IllegalStateException("Anvil outline lost its true nose diagonals or striking face");
        }
        System.out.println("ANCIENT_ANVIL_VISUAL_CHECK: PASS: continuous workface at 16 texels per block, true-diagonal whole-assembly outlines in all four facings, native-resolution edge materials, six owned bounded explicit-UV cells, transparent ingot/scrap margins");
    }
    private static void ownedGeometry(JsonObject e,int cell){
        if(!e.has("rotation")){
            for(String corner:new String[]{"from","to"})for(var n:e.getAsJsonArray(corner))if(n.getAsDouble()<0||n.getAsDouble()>16)throw new IllegalStateException("Unowned anvil geometry");
            return;
        }
        var r=e.getAsJsonObject("rotation");
        if(!r.get("axis").getAsString().equals("y")||r.get("angle").getAsDouble()!=45||r.get("rescale").getAsBoolean())throw new IllegalStateException("Unverified anvil rotation");
        var origin=r.getAsJsonArray("origin");double ox=origin.get(0).getAsDouble(),oz=origin.get(2).getAsDouble(),c=Math.sqrt(.5);
        var offset=AncientAnvilStructure.CELLS.get(cell);
        for(int mask=0;mask<8;mask++){
            double x=e.getAsJsonArray((mask&1)==0?"from":"to").get(0).getAsDouble();
            double y=e.getAsJsonArray((mask&2)==0?"from":"to").get(1).getAsDouble()+offset.y()*16;
            double z=e.getAsJsonArray((mask&4)==0?"from":"to").get(2).getAsDouble();
            double X=ox+(x-ox)*c+(z-oz)*c,Z=oz-(x-ox)*c+(z-oz)*c+offset.z()*16;
            if(X<0||X>16||y<0||y>32||Z< -16||Z>32)throw new IllegalStateException("Rotated anvil mesh escapes its six owned cells");
        }
    }
    private AncientAnvilVisualRegression(){}
}
