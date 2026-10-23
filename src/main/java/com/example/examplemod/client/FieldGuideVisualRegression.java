package com.example.examplemod.client;
import com.example.examplemod.*;
import java.util.*;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
/** Font/layout/resource checks. Real Minecraft screenshots remain the visual acceptance evidence. */
public final class FieldGuideVisualRegression {
    public static void run(ResourceManager resources)throws Exception{
        java.awt.image.BufferedImage font;try(var input=resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/font/ascii.png")).open()){font=ImageIO.read(input);}
        GuideBookRegression.run(font);
        FieldGuideInputRegression.run(new ExcavationVisualRegression.RecordingCanvas(font,396,224));
        for(var design:GuideDesign.values()){
            for(String path:List.of("items/"+design.itemId+".json","models/item/"+design.itemId+".json"))resources.getResourceOrThrow(Identifier.fromNamespaceAndPath(ExampleMod.MODID,path));
            try(var input=resources.getResourceOrThrow(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"textures/item/"+design.itemId+".png")).open()){
                var icon=ImageIO.read(input);if(icon==null||icon.getWidth()!=32||icon.getHeight()!=32||!icon.getColorModel().hasAlpha())throw new IllegalStateException("Guide edition needs a transparent 32px icon: "+design);
            }
        }
        var custom=net.minecraft.network.chat.Component.literal("My heirloom").withStyle(style->style.withBold(true).withItalic(true));var frozen=custom.copy();
        for(var influence:List.of(ModifierInfluence.NETHER,ModifierInfluence.END)){
            var first=InfluenceNameAnimation.animate(custom,influence,0);var second=InfluenceNameAnimation.animate(custom,influence,2);
            if(!first.getString().equals(custom.getString())||!custom.equals(frozen)||first.equals(second))throw new IllegalStateException("Name-only shimmer lost its text or animation");
            first.visit((style,value)->{if(!style.isBold()||!style.isItalic()||style.getColor()==null)throw new IllegalStateException("Name shimmer lost custom styling");return Optional.empty();},net.minecraft.network.chat.Style.EMPTY);
        }
        System.out.println("FIELD_GUIDE_UI_CHECK: PASS: Workshop Manual resources, native widget input, font-measured semantic sections and item-name style preservation");
    }
    private FieldGuideVisualRegression(){}
}
