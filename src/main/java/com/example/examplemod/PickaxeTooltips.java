package com.example.examplemod;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

/** Shared styled components: compact labels, separate value colors, and no inherited lore italics. */
public final class PickaxeTooltips {
    public static final String REVISION_KEY="examplemod:pickaxe_tooltip_revision";
    public static final int REVISION=2;
    public static final int LABEL=0xB9C5CD,MUTED=0x788B98,TEAL=0x77D5CB,GOLD=0xFFD369;
    public record Stat(PickaxeTrait trait,double value,ModifierScaling low,ModifierScaling high){}
    public record ToolView(PickaxeProgression.State progression,double score,List<Stat> stats){}

    public static MutableComponent text(String value,int color){return Component.literal(value).withStyle(s->s.withColor(color).withItalic(false));}
    public static int magnitude(double fraction){
        if(fraction<=0)return MUTED;if(fraction<.15)return 0xC1CBD0;if(fraction<.35)return 0x8ED879;
        if(fraction<.60)return 0x70DACA;if(fraction<.85)return 0xA5ACFF;return 0xFFD369;
    }
    public static int qualityColor(double score){return score<40?0xF18F86:score<70?0xE8C978:score<90?0x8ED879:0x70DACA;}
    public static String percent(double value){return number(value*100)+"%";}
    private static String number(double value){
        String result=String.format(Locale.ROOT,"%.3f",value);while(result.endsWith("0"))result=result.substring(0,result.length()-1);if(result.endsWith("."))result=result.substring(0,result.length()-1);return result;
    }
    public static String value(PickaxeTrait trait,double value){return switch(trait.unit){case PERCENT->percent(value);case LEVEL->number(value)+" lv";case BLOCKS->number(value)+" blocks";};}
    public static String label(PickaxeTrait trait){return switch(trait){case EXPERIENCE->"Mining XP";case AQUATIC->"Underwater recovery";case AIRBORNE->"Airborne recovery";case SILK->"Silk touch";case SMELTING->"Smelting";default->trait.label;};}
    private static MutableComponent gradeRange(ModifierScaling low,ModifierScaling high){
        if(low==high)return low.badge();return text("[",MUTED).append(text(low.name(),low.color)).append(text("-",MUTED)).append(text(high.name(),high.color)).append(text("]",MUTED));
    }
    public static ToolView view(ItemStack stack){
        var totals=PickaxeModifiers.totals(stack);var contributions=PickaxeModifiers.contributions(stack);var stats=new ArrayList<Stat>();
        for(var trait:PickaxeTrait.values()){
            double total=totals.get(trait);if(total<=0)continue;ModifierScaling low=ModifierScaling.S,high=ModifierScaling.F;
            for(var part:contributions)if(part.trait()==trait&&part.base()>0){var grade=part.roll().scaling();if(grade.ordinal()<low.ordinal())low=grade;if(grade.ordinal()>high.ordinal())high=grade;}
            stats.add(new Stat(trait,total,low,high));
        }
        return new ToolView(PickaxeProgression.read(stack),Math.max(0,ArtifactQuality.assemblyScore(stack)),List.copyOf(stats));
    }
    public static List<Component> tool(ItemStack stack){return tool(view(stack));}
    public static List<Component> tool(ToolView view){
        var p=view.progression();var lines=new ArrayList<Component>();
        lines.add(text("Level ",LABEL).append(text(p.level()+"/100",GOLD)).append(text("   Ascension ",LABEL)).append(text(p.ascensions()+"/10",0xCF9AFF)));
        if(p.mastered())lines.add(text("Mastered",GOLD));
        else if(p.needsAscension())lines.add(text("Ascension required",GOLD));
        else {
            int filled=10*p.xp()/PickaxeProgression.xpRequired(p.level());
            lines.add(text("[",MUTED).append(text("|".repeat(filled),TEAL)).append(text("|".repeat(10-filled),0x3D535F)).append(text("] ",MUTED)).append(text(p.xp()+"/"+PickaxeProgression.xpRequired(p.level())+" XP",LABEL)));
        }
        lines.add(text("Assembly quality  ",LABEL).append(text(number(view.score())+"%",qualityColor(view.score()))));
        lines.add(text("",MUTED));lines.add(text("MODIFIERS",MUTED));
        if(view.stats().isEmpty())lines.add(text("No effective bonuses",MUTED));
        for(var stat:view.stats())lines.add(text(label(stat.trait())+"  ",LABEL).append(text("+"+value(stat.trait(),stat.value()),magnitude(stat.value()/stat.trait().cap))).append(text("  ",MUTED)).append(gradeRange(stat.low(),stat.high())));
        return List.copyOf(lines);
    }
    public static List<Component> piece(ItemStack stack){
        int score=ArtifactQuality.score(stack);if(score<0)return List.of();
        var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(ArtifactQuality.RECOVERED);
        return piece(score,data.getIntOr("Rarity",0),ArtifactModifiers.read(stack));
    }
    public static List<Component> piece(int score,int rarity,List<ArtifactModifiers.Roll> rolls){
        var lines=new ArrayList<Component>();
        int tierColor=new int[]{0xC1CBD0,0x8ED879,0x73BAF4,0xCF9AFF,0xFFD369,0xFF8BAB}[Math.clamp(rarity,0,5)];
        lines.add(text(ArtifactModifierCatalog.tier(rarity).label()+" part",tierColor).append(text("   Quality ",LABEL)).append(text(score+"%",qualityColor(score))));
        for(var roll:rolls){
            var trait=PickaxeTrait.byId(roll.id());if(trait==null)continue;double effective=roll.value()*score/100.0;var grade=roll.scaling();
            lines.add(text(label(trait)+"  ",LABEL).append(text("+"+value(trait,effective),magnitude(effective/trait.cap))).append(text("  ",MUTED)).append(grade.badge()));
            lines.add(text("  Base ",MUTED).append(text("+"+value(trait,roll.value()),magnitude(roll.value()/trait.cap))).append(text("  Lv ",MUTED)).append(text("+"+percent(grade.levelGrowth),grade.color)).append(text("  Asc ",MUTED)).append(text("+"+percent(grade.ascensionGrowth),grade.color)));
        }
        return List.copyOf(lines);
    }
    public static void markCurrent(ItemStack stack){var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();root.putInt(REVISION_KEY,REVISION);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));}
    /** One-time refresh for owned old items; no seeds, rolls, quality, or earned levels change. */
    public static boolean refresh(ItemStack stack){
        if(stack.isEmpty())return false;
        boolean tool=PickaxeProgression.supports(stack),part=false;
        if(!tool)for(var find:CreativePickaxeParts.FINDS)if(stack.is(ReconstructionContent.reward(find))){part=true;break;}
        if(!tool&&!part)return false;
        var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if(root.getIntOr(REVISION_KEY,0)>=REVISION||root.getBooleanOr(CreativePickaxeParts.PENDING,false))return false;
        if(tool){PickaxeModifiers.applyToAssembly(stack);return true;}
        if(ArtifactQuality.score(stack)>=0){stack.set(DataComponents.LORE,new ItemLore(piece(stack)));markCurrent(stack);return true;}
        return false;
    }
    private PickaxeTooltips(){}
}
