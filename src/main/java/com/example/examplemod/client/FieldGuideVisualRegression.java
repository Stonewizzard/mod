package com.example.examplemod.client;
import com.example.examplemod.*;
import java.util.*;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
/** Native font layout and the same navigation state used by the live screen. */
public final class FieldGuideVisualRegression {
    public static void run(ResourceManager resources)throws Exception{
        java.awt.image.BufferedImage font;try(var input=resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/font/ascii.png")).open()){font=ImageIO.read(input);}
        int scenes=0;var preview=new com.google.gson.JsonArray();
        for(var entry:FieldGuide.entries()){
            var nav=new FieldGuideNavigation();nav.open(entry.id());var measure=new ExcavationVisualRegression.RecordingCanvas(font,320,228);int pages=FieldGuideView.pages(measure,entry);
            for(int page=0;page<pages;page++){
                var canvas=new ExcavationVisualRegression.RecordingCanvas(font,320,228);FieldGuideView.draw(canvas,nav,-1);scenes++;
                for(var command:canvas.commands){var c=command.getAsJsonObject();if(c.has("text")&&c.get("x").getAsInt()==170&&c.get("y").getAsInt()>=80&&c.get("y").getAsInt()<208&&c.get("x").getAsInt()+c.get("w").getAsInt()>305)throw new IllegalStateException("Guide text escaped its article page");}
                if(page==0&&List.of("welcome","cruncher_egg",PickaxeTrait.SPEED.id).contains(entry.id())){var scene=new com.google.gson.JsonObject();scene.addProperty("id",entry.id());scene.add("commands",canvas.commands);preview.add(scene);}
                nav.turnPage(1,pages);
            }
            nav.related(true);FieldGuideView.draw(new ExcavationVisualRegression.RecordingCanvas(font,320,228),nav,-1);
            for(int i=0;i<entry.links().size();i++)if(FieldGuideView.hit(170,103+i*25,nav)!=FieldGuideView.LINK+i)throw new IllegalStateException("Related-entry hitbox mismatch");
        }
        for(var category:FieldGuide.Category.values()){
            var nav=new FieldGuideNavigation();nav.category(category);for(int group=0;group<(category==FieldGuide.Category.MODIFIERS?FieldGuide.Group.values().length:1);group++){
                FieldGuideView.draw(new ExcavationVisualRegression.RecordingCanvas(font,320,228),nav,-1);if(category==FieldGuide.Category.MODIFIERS)nav.nextGroup();
            }
        }
        var empty=new FieldGuideNavigation();empty.query("zzzznotpresentzzzz");FieldGuideView.draw(new ExcavationVisualRegression.RecordingCanvas(font,320,228),empty,-1);
        var nav=new FieldGuideNavigation();for(int i=0;i<8;i++)if(FieldGuideView.hit(15+i%2*70,58+i/2*15,nav)!=FieldGuideView.TAB+i)throw new IllegalStateException("Category tab hitbox mismatch");
        if(FieldGuideView.hit(18,135,nav)!=FieldGuideView.ROW||FieldGuideView.hit(135,215,nav)!=FieldGuideView.LIST_NEXT)throw new IllegalStateException("Index navigation hitbox mismatch");
        var custom=net.minecraft.network.chat.Component.literal("My heirloom").withStyle(style->style.withBold(true).withItalic(true));var frozen=custom.copy();
        for(var influence:List.of(ModifierInfluence.NETHER,ModifierInfluence.END)){
            var first=InfluenceNameAnimation.animate(custom,influence,0);var second=InfluenceNameAnimation.animate(custom,influence,2);
            if(!first.getString().equals(custom.getString())||!custom.equals(frozen)||first.equals(second))throw new IllegalStateException("Name-only shimmer lost its text or animation");
            first.visit((style,value)->{if(!style.isBold()||!style.isItalic()||style.getColor()==null)throw new IllegalStateException("Name shimmer lost custom styling");return Optional.empty();},net.minecraft.network.chat.Style.EMPTY);
        }
        for(String name:List.of("items/ancient_field_guide.json","models/item/ancient_field_guide.json"))resources.getResourceOrThrow(Identifier.fromNamespaceAndPath(ExampleMod.MODID,name));
        resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/item/written_book.png"));
        var export=new com.google.gson.JsonObject();export.add("scenes",preview);var png=new java.io.ByteArrayOutputStream();ImageIO.write(font,"png",png);export.addProperty("font",Base64.getEncoder().encodeToString(png.toByteArray()));var bytes=new java.io.ByteArrayOutputStream();try(var zip=new java.util.zip.DeflaterOutputStream(bytes)){zip.write(new com.google.gson.Gson().toJson(export).getBytes(java.nio.charset.StandardCharsets.UTF_8));}System.out.println("FIELD_GUIDE_PREVIEW_Z:"+Base64.getEncoder().encodeToString(bytes.toByteArray()));
        System.out.println("FIELD_GUIDE_UI_CHECK: PASS: "+scenes+" article pages across "+FieldGuide.entries().size()+" entries, all categories and topic filters, empty results, native font bounds, related links and item resources");
    }
    private FieldGuideVisualRegression(){}
}
