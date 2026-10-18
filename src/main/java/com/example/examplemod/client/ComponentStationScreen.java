package com.example.examplemod.client;

import java.util.ArrayList;
import java.util.List;
import com.example.examplemod.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class ComponentStationScreen extends AbstractContainerScreen<ComponentStationMenu> {
    public ComponentStationScreen(ComponentStationMenu menu,Inventory inventory,Component title){super(menu,inventory,title,320,242);}
    private ComponentStationView.State state(){
        var part=menu.getSlot(0).getItem();var kind=menu.kind();var modifiers=new ArrayList<ComponentStationView.Row>();var offers=new ArrayList<ComponentStationView.Row>();String locked=ComponentWork.locked(part);
        var rolls=ArtifactModifiers.read(part);
        for(int i=0;i<rolls.size();i++){var roll=rolls.get(i);var trait=PickaxeTrait.byId(roll.id());boolean ancient=AncientModifiers.ancient(roll);modifiers.add(new ComponentStationView.Row(ancient?"Ancient modifier":trait==null?roll.id():PickaxeTooltips.label(trait),ancient?"????????":trait==null?"Unknown":"+"+PickaxeTooltips.value(trait,roll.value()),roll.scaling(),ancient?0xB69BE8:trait==null?0x8FA2AF:PickaxeTooltips.magnitude(roll.value()/trait.cap),ancient,roll.id().equals(locked),kind==ComponentStationKind.LOCKER&&ComponentWork.can(kind,part,i)));}
        var choices=ComponentWork.offers(part);
        for(int i=0;i<choices.size();i++){var roll=choices.get(i);var trait=PickaxeTrait.byId(roll.id());if(trait!=null)offers.add(new ComponentStationView.Row(PickaxeTooltips.label(trait),"+"+PickaxeTooltips.value(trait,roll.value()),roll.scaling(),PickaxeTooltips.magnitude(roll.value()/trait.cap),false,false,ComponentWork.can(ComponentStationKind.UNVEILER,part,i)));}
        boolean valid=ComponentWork.component(part),corrupted=ComponentWork.corrupted(part);String status=valid?ComponentWork.state(part).getStringOr("LastAction",kind.description):"Recovered components only";
        if(corrupted&&status.equals(kind.description))status="Corrupted: no more modifications";
        return new ComponentStationView.State(kind,valid,Math.max(0,ArtifactQuality.score(part)),corrupted,status,List.copyOf(modifiers),List.copyOf(offers),ComponentWork.can(kind,part,0));
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){
        var state=state();ComponentStationView.draw(new ExcavationScreenView.Canvas(){
            @Override public void fill(int x,int y,int w,int h,int color){g.fill(leftPos+x,topPos+y,leftPos+x+w,topPos+y+h,color);}
            @Override public void texture(Identifier id,int x,int y,int w,int h,float u,float v,int sw,int sh,int tw,int th,int color){g.blit(RenderPipelines.GUI_TEXTURED,id,leftPos+x,topPos+y,u,v,w,h,sw,sh,tw,th,color);}
            @Override public void text(String text,int x,int y,int color){g.text(font,text,leftPos+x,topPos+y,color,false);}
            @Override public int textWidth(String text){return font.width(text);}
        },state,ComponentStationView.buttonAt(state,mouseX-leftPos,mouseY-topPos));
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY){}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){
        int action=ComponentStationView.buttonAt(state(),event.x()-leftPos,event.y()-topPos);
        if(action>=0){if(event.button()==InputConstants.MOUSE_BUTTON_LEFT&&minecraft.getConnection()!=null)minecraft.getConnection().send(new ServerboundCustomPayloadPacket(new ComponentStationPayload(menu.containerId,action,menu.getSlot(0).getItem().copy())));return true;}
        return super.mouseClicked(event,doubleClick);
    }
}
