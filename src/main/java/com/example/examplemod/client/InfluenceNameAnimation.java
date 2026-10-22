package com.example.examplemod.client;
import com.example.examplemod.*;
import java.util.Optional;
import net.minecraft.network.chat.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
/** A slow name-only shimmer. No item, icon, custom name text or saved data is changed. */
public final class InfluenceNameAnimation {
    public static void tooltip(ItemTooltipEvent event){
        var stack=event.getItemStack();if(!(stack.getItem() instanceof AncientComponentItem)||event.getToolTip().isEmpty())return;
        var influence=ComponentInfluences.current(stack);if(influence==ModifierInfluence.NONE)return;
        event.getToolTip().set(0,animate(event.getToolTip().getFirst(),influence,System.nanoTime()/1_000_000_000.0));
    }
    public static Component animate(Component name,ModifierInfluence influence,double seconds){
        if(influence==ModifierInfluence.NONE)return name;
        int dim=influence==ModifierInfluence.NETHER?0xEE804C:0xA784EB,bright=influence==ModifierInfluence.NETHER?0xFFE09A:0xE4C8FF;
        MutableComponent result=Component.empty();int[] index={0};
        name.visit((style,value)->{
            value.codePoints().forEach(cp->{double wave=.5+.5*Math.sin(seconds*Math.PI/3-index[0]++*.16);int color=mix(dim,bright,wave);result.append(Component.literal(new String(Character.toChars(cp))).setStyle(style.withColor(color)));});return Optional.empty();
        },Style.EMPTY);return result;
    }
    private static int mix(int a,int b,double t){int color=0;for(int shift:new int[]{16,8,0})color|=(int)Math.round(((a>>shift)&255)*(1-t)+((b>>shift)&255)*t)<<shift;return color;}
    private InfluenceNameAnimation(){}
}
