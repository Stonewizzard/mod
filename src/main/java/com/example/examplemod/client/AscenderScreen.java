package com.example.examplemod.client;

import java.util.ArrayList;
import com.example.examplemod.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class AscenderScreen extends AbstractContainerScreen<AscenderMenu> {
    public AscenderScreen(AscenderMenu menu,Inventory inventory,Component title){super(menu,inventory,title,320,242);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){
        var tool=menu.getSlot(0).getItem();var costs=new ArrayList<AscenderScreenView.Cost>();
        for(var cost:AscensionResources.requirements(tool))costs.add(new AscenderScreenView.Cost(new ItemStack(cost.item()).getHoverName().getString(),cost.count(),AscensionResources.available(menu.inventory(),cost.item())));
        var state=new AscenderScreenView.State(PickaxeProgression.supports(tool),PickaxeProgression.read(tool),costs);
        AscenderScreenView.draw(new ExcavationScreenView.Canvas(){
            @Override public void fill(int x,int y,int w,int h,int color){g.fill(leftPos+x,topPos+y,leftPos+x+w,topPos+y+h,color);}
            @Override public void texture(Identifier id,int x,int y,int w,int h,float u,float v,int sw,int sh,int tw,int th,int color){g.blit(RenderPipelines.GUI_TEXTURED,id,leftPos+x,topPos+y,u,v,w,h,sw,sh,tw,th,color);}
            @Override public void text(String text,int x,int y,int color){g.text(font,text,leftPos+x,topPos+y,color,false);}
            @Override public int textWidth(String text){return font.width(text);}
        },state);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY){}
}
