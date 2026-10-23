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
        var home=new FieldGuideNavigation();var homeCanvas=new ExcavationVisualRegression.RecordingCanvas(font,320,228);
        draw(homeCanvas,home);addPreview(preview,"contents",homeCanvas);
        var measure=new ExcavationVisualRegression.RecordingCanvas(font,320,228);FieldGuideInputRegression.run(measure);
        for(var entry:FieldGuide.entries()){
            var nav=new FieldGuideNavigation();nav.open(entry.id());int pages=FieldGuideView.pages(measure,entry);
            for(int page=0;page<pages;page++){
                var canvas=new ExcavationVisualRegression.RecordingCanvas(font,320,228);draw(canvas,nav);scenes++;
                for(var command:canvas.commands){var text=command.getAsJsonObject();if(text.has("text")&&(text.get("x").getAsInt()==24||text.get("x").getAsInt()==168)&&text.get("y").getAsInt()>=82&&text.get("y").getAsInt()<181&&text.get("w").getAsInt()>FieldGuideView.TEXT_WIDTH)throw new IllegalStateException("Guide text escaped its reading column");}
                if(page==0&&List.of("welcome","cruncher_egg",PickaxeTrait.SPEED.id).contains(entry.id()))addPreview(preview,entry.id(),canvas);
                nav.turnPage(1,pages);
            }
        }
        for(var category:FieldGuide.Category.values()){
            var nav=new FieldGuideNavigation();nav.category(category);draw(new ExcavationVisualRegression.RecordingCanvas(font,320,228),nav);
            for(var group:FieldGuide.Group.values())if(category==FieldGuide.Category.MODIFIERS){nav.group(group);for(int page=0;page<nav.listPages();page++){draw(new ExcavationVisualRegression.RecordingCanvas(font,320,228),nav);nav.listPage(1);}}
        }
        var search=new FieldGuideNavigation();search.query("magnet");var searchCanvas=new ExcavationVisualRegression.RecordingCanvas(font,320,228);draw(searchCanvas,search);addPreview(preview,"search",searchCanvas);
        search.query("zzzznotpresentzzzz");draw(new ExcavationVisualRegression.RecordingCanvas(font,320,228),search);
        var custom=net.minecraft.network.chat.Component.literal("My heirloom").withStyle(style->style.withBold(true).withItalic(true));var frozen=custom.copy();
        for(var influence:List.of(ModifierInfluence.NETHER,ModifierInfluence.END)){
            var first=InfluenceNameAnimation.animate(custom,influence,0);var second=InfluenceNameAnimation.animate(custom,influence,2);
            if(!first.getString().equals(custom.getString())||!custom.equals(frozen)||first.equals(second))throw new IllegalStateException("Name-only shimmer lost its text or animation");
            first.visit((style,value)->{if(!style.isBold()||!style.isItalic()||style.getColor()==null)throw new IllegalStateException("Name shimmer lost custom styling");return Optional.empty();},net.minecraft.network.chat.Style.EMPTY);
        }
        for(String name:List.of("items/ancient_field_guide.json","models/item/ancient_field_guide.json"))resources.getResourceOrThrow(Identifier.fromNamespaceAndPath(ExampleMod.MODID,name));
        try(var input=resources.getResourceOrThrow(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"textures/item/ancient_field_guide.png")).open()){
            var icon=ImageIO.read(input);if(icon==null||icon.getWidth()!=32||icon.getHeight()!=32||!icon.getColorModel().hasAlpha())throw new IllegalStateException("Field guide requires a transparent 32px icon");
        }
        var export=new com.google.gson.JsonObject();export.add("scenes",preview);try(var input=resources.getResourceOrThrow(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"textures/item/ancient_field_guide.png")).open()){export.addProperty("icon",Base64.getEncoder().encodeToString(input.readAllBytes()));}var png=new java.io.ByteArrayOutputStream();ImageIO.write(font,"png",png);export.addProperty("font",Base64.getEncoder().encodeToString(png.toByteArray()));var bytes=new java.io.ByteArrayOutputStream();try(var zip=new java.util.zip.DeflaterOutputStream(bytes)){zip.write(new com.google.gson.Gson().toJson(export).getBytes(java.nio.charset.StandardCharsets.UTF_8));}System.out.println("FIELD_GUIDE_PREVIEW_Z:"+Base64.getEncoder().encodeToString(bytes.toByteArray()));
        System.out.println("FIELD_GUIDE_UI_CHECK: PASS: "+scenes+" article pages across "+FieldGuide.entries().size()+" entries, all categories/topic indexes, empty results, native font bounds, direct related links, custom icon and native widget input");
    }
    private static void draw(ExcavationVisualRegression.RecordingCanvas c,FieldGuideNavigation n){
        FieldGuideView.draw(c,n);c.text(FieldGuideView.fit(c,n.query().isEmpty()?"Search all entries...":n.query(),258),21,36,0xff76634a);var controls=FieldGuideView.controls(c,n);
        for(var b:controls){FieldGuideView.drawControl(c,b,false);for(var other:controls)if(b!=other&&b.x()<other.x()+other.w()&&b.x()+b.w()>other.x()&&b.y()<other.y()+other.h()&&b.y()+b.h()>other.y())throw new IllegalStateException("Overlapping guide controls: "+b.id()+" and "+other.id());}
    }
    private static void addPreview(com.google.gson.JsonArray preview,String id,ExcavationVisualRegression.RecordingCanvas c){var scene=new com.google.gson.JsonObject();scene.addProperty("id",id);scene.add("commands",c.commands);preview.add(scene);}
    private FieldGuideVisualRegression(){}
}
