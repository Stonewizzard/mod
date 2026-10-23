package com.example.examplemod.client;
import com.example.examplemod.*;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
/** One native widget per painted control; narration uses the full, unabridged label. */
final class GuideBookButton extends Button {
    final FieldGuideView.Control control;
    private final GuideDesign design;private final FieldGuideNavigation navigation;
    private final Function<GuiGraphicsExtractor,ExcavationScreenView.Canvas> canvas;
    GuideBookButton(FieldGuideView.Control control,int left,int top,GuideDesign design,FieldGuideNavigation navigation,Runnable action,Function<GuiGraphicsExtractor,ExcavationScreenView.Canvas> canvas){
        super(left+control.x(),top+control.y(),control.w(),control.h(),Component.literal(control.label()),button->action.run(),DEFAULT_NARRATION);
        this.control=control;this.design=design;this.navigation=navigation;this.canvas=canvas;active=control.enabled();
    }
    @Override public void extractContents(GuiGraphicsExtractor g,int mouseX,int mouseY,float partial){
        GuideBookView.drawControl(canvas.apply(g),design,navigation,control,isHoveredOrFocused());
        if(!control.icon().isEmpty()){
            var item=BuiltInRegistries.ITEM.getValue(Identifier.parse(control.icon()));
            if(item!=null&&item!=Items.AIR)g.item(new ItemStack(item),getX()+3,getY()+(getHeight()-16)/2);
        }
    }
}
