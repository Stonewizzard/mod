package com.example.examplemod.client;

import com.example.examplemod.*;
import com.google.gson.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/** Snapshot the real drawing plan and exercise the shared hit regions and data-driven specimen. */
public final class ExcavationVisualRegression {
    public static void run(ResourceManager resources) throws Exception {
        JsonObject language;
        try(var reader=new InputStreamReader(resources.getResourceOrThrow(Identifier.fromNamespaceAndPath("examplemod","lang/en_us.json")).open(),StandardCharsets.UTF_8)){
            language=JsonParser.parseReader(reader).getAsJsonObject();
        }
        BufferedImage font;
        try(var input=resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/font/ascii.png")).open()){font=ImageIO.read(input);}
        var assets=new JsonObject();var scenes=new JsonArray();var animation=new JsonArray();
        var covered=new ExcavationStudy();var loose=new ExcavationStudy();
        for(int cell=0;cell<ExcavationStudy.CELLS;cell++)if(ExcavationStudy.coveredCell(cell)){loose.work(1,cell);loose.work(1,cell);}
        var partial=copy(loose);for(int cell=0;cell<ExcavationStudy.CELLS;cell++)if(cell%ExcavationStudy.COLS<13)partial.work(2,cell);
        var complete=copy(loose);for(int cell=0;cell<ExcavationStudy.CELLS;cell++)complete.work(2,cell);
        var damaged=copy(loose);damaged.work(3,6*ExcavationStudy.COLS+14);
        var labels=(ExcavationScreenView.Labels)(key,args)->String.format(Locale.ROOT,language.get("gui.examplemod.excavation."+key).getAsString(),args);
        for(String name:new String[]{"covered","partial","inspected","complete","damaged","rare","inventory","empty","empty_hands","held_drill","held_hammer"}){
            var study=switch(name){case "covered"->covered;case "complete"->complete;case "damaged"->damaged;case "empty"->null;default->partial;};
            boolean rare=name.equals("rare"),inventory=name.equals("inventory");
            int held=name.equals("empty")||inventory||name.equals("empty_hands")?0:name.equals("held_drill")?1:name.equals("inspected")?4:name.equals("covered")||name.equals("damaged")||name.equals("held_hammer")?3:2;
            int hover=held>0?6*ExcavationStudy.COLS+(name.equals("inspected")?19:13):-1;
            var state=new ExcavationScreenView.State(study,rare,inventory,held,hover,storedTools(held),name.equals("empty")?"":rare?"Rare Ancient Artifact":"Ancient Artifact");
            scenes.add(snapshot(name,state,labels,resources,font,assets));
        }
        animation.add(snapshot("covered",new ExcavationScreenView.State(covered,false,false,3,-1,storedTools(3),"Ancient Artifact"),labels,resources,font,assets));
        animation.add(snapshot("loose",new ExcavationScreenView.State(loose,false,false,2,-1,storedTools(2),"Ancient Artifact"),labels,resources,font,assets));
        for(int step=0;step<=14;step++){
            var state=copy(loose);int limit=step*2;
            for(int cell=0;cell<ExcavationStudy.CELLS;cell++)if(cell%ExcavationStudy.COLS<limit)state.work(2,cell);
            animation.add(snapshot("brush_"+step,new ExcavationScreenView.State(state,false,false,2,6*ExcavationStudy.COLS+Math.min(27,limit),storedTools(2),"Ancient Artifact"),labels,resources,font,assets));
        }
        for(int i=0;i<5;i++){
            var p=ExcavationLayout.SLOTS[i];require(p[0]>=0&&p[0]+16<=320&&p[1]+16<ExcavationLayout.BOARD_Y,"A field-kit slot overlaps the board");
        }
        require(ExcavationLayout.cellAt(20,72)==0&&ExcavationLayout.cellAt(299.99,191.99)==ExcavationStudy.CELLS-1
                &&ExcavationLayout.cellAt(19.9,72)==-1&&ExcavationLayout.cellAt(300,72)==-1,"Board hit bounds must be precise");
        for(int i=1;i<=4;i++)require(ExcavationLayout.toolAt(160+(i-1)*38,59)==i
                &&ExcavationLayout.toolAt(ExcavationLayout.SLOTS[i][0],ExcavationLayout.SLOTS[i][1])==0,"Help labels must not overlap native inventory slots");
        for(int held=0;held<=4;held++){
            var canvas=new RecordingCanvas(font);
            ExcavationScreenView.draw(canvas,new ExcavationScreenView.State(partial,false,false,held,6*ExcavationStudy.COLS+14,storedTools(held),"Ancient Artifact"),labels);
            long highlighted=canvas.commands.asList().stream().map(JsonElement::getAsJsonObject)
                    .filter(c->c.get("op").getAsString().equals("fill")&&c.get("color").getAsInt()==0xff468473&&c.get("y").getAsInt()==65).count();
            require(highlighted==(held==0?0:1),"Only a carried tool may highlight a field-kit card, including when its stored slot is empty");
            boolean hint=canvas.commands.asList().stream().map(JsonElement::getAsJsonObject).anyMatch(c->c.has("text")&&c.get("text").getAsString().equals(labels.get("hold_tool_hint")));
            require(hint==(held==0),"Empty hands must show pickup instructions instead of defaulting to the brush");
        }
        if(Boolean.getBoolean("examplemod.excavationVisualExport")){
            var root=new JsonObject();root.add("assets",assets);root.add("scenes",scenes);root.add("animation",animation);
            try(var input=resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/font/ascii.png")).open()){root.addProperty("font",Base64.getEncoder().encodeToString(input.readAllBytes()));}
            var output=new ByteArrayOutputStream();try(var zip=new DeflaterOutputStream(output)){zip.write(new Gson().toJson(root).getBytes(StandardCharsets.UTF_8));}
            System.out.println("EXCAVATION_PREVIEW_Z:"+Base64.getEncoder().encodeToString(output.toByteArray()));
        }
        System.out.println("EXCAVATION_UI_CHECK: PASS: real drawing plan, eleven study/inventory states, four held-tool highlights with empty kit slots, empty-hand instructions, safe bounds and full grid coverage");
    }
    private static boolean[] storedTools(int held){var tools=new boolean[]{false,true,true,true,true};if(held>0)tools[held]=false;return tools;}
    private static ExcavationStudy copy(ExcavationStudy study){return study.copy();}
    private static JsonObject snapshot(String name,ExcavationScreenView.State state,ExcavationScreenView.Labels labels,ResourceManager resources,BufferedImage font,JsonObject assets)throws Exception{
        var canvas=new RecordingCanvas(font);ExcavationScreenView.draw(canvas,state,labels);
        // Native slot extraction draws these after the background; include their actual GUI textures in the review.
        if(state.study()!=null){canvas.image(Identifier.withDefaultNamespace(state.rare()?"textures/item/prize_pottery_sherd.png":"textures/item/arms_up_pottery_sherd.png"),26,40,16,16,16,16,-1);}
        for(int i=1;i<=4;i++)if(state.tools()[i]){
            var id=i==2?Identifier.withDefaultNamespace("textures/item/brush.png"):ExcavationScreenView.texture("item/"+switch(i){case 1->"archaeology_drill";case 3->"archaeology_hammer";default->"magnifying_glass";});
            var p=ExcavationLayout.SLOTS[i];canvas.image(id,p[0],p[1],16,16,i==2?16:32,i==2?16:32,-1);
        }
        // Native container rendering draws the actual carried item over the work surface.
        if(state.heldTool()>0&&state.hoveredCell()>=0){
            int tool=state.heldTool();var id=tool==2?Identifier.withDefaultNamespace("textures/item/brush.png"):ExcavationScreenView.texture("item/"+switch(tool){case 1->"archaeology_drill";case 3->"archaeology_hammer";default->"magnifying_glass";});
            canvas.image(id,ExcavationLayout.BOARD_X+state.hoveredCell()%ExcavationStudy.COLS*10-3,
                    ExcavationLayout.BOARD_Y+state.hoveredCell()/ExcavationStudy.COLS*10-3,16,16,tool==2?16:32,tool==2?16:32,-1);
        }
        for(var id:canvas.textures)if(!assets.has(id.toString()))try(var input=resources.getResourceOrThrow(id).open()){
            byte[] bytes=input.readAllBytes();require(ImageIO.read(new ByteArrayInputStream(bytes))!=null,"Invalid excavation image "+id);
            assets.addProperty(id.toString(),Base64.getEncoder().encodeToString(bytes));
        }
        var result=new JsonObject();result.addProperty("name",name);result.addProperty("width",320);result.addProperty("height",236);
        result.addProperty("progress",state.study()==null?0:state.study().progress());result.addProperty("condition",state.study()==null?100:state.study().condition());result.add("commands",canvas.commands);return result;
    }
    private static final class RecordingCanvas implements ExcavationScreenView.Canvas {
        final JsonArray commands=new JsonArray();final Set<Identifier> textures=new HashSet<>();final BufferedImage font;
        RecordingCanvas(BufferedImage font){this.font=font;}
        @Override public void fill(int x,int y,int w,int h,int color){bounds(x,y,w,h);var c=command("fill",x,y,w,h);c.addProperty("color",color);commands.add(c);}
        @Override public void texture(Identifier id,int x,int y,int w,int h,float u,float v,int sw,int sh,int tw,int th,int color){
            bounds(x,y,w,h);require(u>=0&&v>=0&&u+sw<=tw&&v+sh<=th,"Texture region escaped its asset");
            var c=command("texture",x,y,w,h);c.addProperty("id",id.toString());c.addProperty("u",u);c.addProperty("v",v);c.addProperty("sw",sw);c.addProperty("sh",sh);c.addProperty("tw",tw);c.addProperty("th",th);c.addProperty("color",color);commands.add(c);textures.add(id);
        }
        @Override public void text(String text,int x,int y,int color){bounds(x,y,textWidth(text),8);var c=command("text",x,y,textWidth(text),8);c.addProperty("text",text);c.addProperty("color",color);commands.add(c);}
        @Override public int textWidth(String text){int width=0,unit=font.getWidth()/16;for(int i=0;i<text.length();i++){
            char ch=text.charAt(i);if(ch==' '){width+=4;continue;}int right=0;
            if(ch<256)for(int x=0;x<unit;x++)for(int y=0;y<unit;y++)if((font.getRGB(ch%16*unit+x,ch/16*unit+y)>>>24)>0)right=Math.max(right,x+1);
            width+=right*8/unit+1;
        }return width;}
        private static JsonObject command(String op,int x,int y,int w,int h){var c=new JsonObject();c.addProperty("op",op);c.addProperty("x",x);c.addProperty("y",y);c.addProperty("w",w);c.addProperty("h",h);return c;}
        private static void bounds(int x,int y,int w,int h){require(x>=0&&y>=0&&w>=0&&h>=0&&x+w<=ExcavationLayout.WIDTH&&y+h<=ExcavationLayout.HEIGHT,"An excavation control escaped the compact screen: "+x+","+y+" "+w+"x"+h);}
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
