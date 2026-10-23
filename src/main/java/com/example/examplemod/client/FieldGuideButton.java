package com.example.examplemod.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import java.util.function.Function;

/** Keeps Minecraft's native SDL mouse, keyboard focus, activation and narration behavior. */
class FieldGuideButton extends Button {
    final FieldGuideView.Control control;
    private final Function<GuiGraphicsExtractor,ExcavationScreenView.Canvas> canvas;
    FieldGuideButton(FieldGuideView.Control control,int left,int top,Runnable action,Function<GuiGraphicsExtractor,ExcavationScreenView.Canvas> canvas){
        super(left+control.x(),top+control.y(),control.w(),control.h(),Component.literal(control.label()),button->action.run(),DEFAULT_NARRATION);
        this.control=control;this.canvas=canvas;this.active=control.enabled();
    }
    @Override public void extractContents(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partialTick){
        FieldGuideView.drawControl(canvas.apply(graphics),control,isHoveredOrFocused());
        if(isHovered())graphics.requestCursor(active?CursorTypes.POINTING_HAND:CursorTypes.NOT_ALLOWED);
        if(!control.icon().isEmpty()){
            var item=BuiltInRegistries.ITEM.getValue(Identifier.parse(control.icon()));
            if(item!=null&&item!=Items.AIR)graphics.item(new ItemStack(item),getX()+3,getY()+1);
        }
    }
}
