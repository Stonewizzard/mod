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
    public static final int REVISION=7;
    public static final int LABEL=0xB9C5CD,MUTED=0x788B98,TEAL=0x77D5CB,GOLD=0xFFD369;
    public record Stat(PickaxeTrait trait,double value,ModifierScaling low,ModifierScaling high){}
    public record ToolView(PickaxeProgression.State progression,double score,List<Stat> stats){}

    public static MutableComponent text(String value,int color){return Component.literal(value).withStyle(s->s.withColor(color).withItalic(false));}
    public static int magnitude(double fraction){
        if(fraction<=0)return MUTED;if(fraction<.15)return 0xC1CBD0;if(fraction<.35)return 0x8ED879;
        if(fraction<.60)return 0x70DACA;if(fraction<.85)return 0xA5ACFF;return 0xFFD369;
    }
    public static int qualityColor(double score){return score>100?GOLD:score<40?0xF18F86:score<70?0xE8C978:score<90?0x8ED879:0x70DACA;}
    public static String percent(double value){return number(value*100)+"%";}
    public static String tierPrefix(ArtifactModifiers.Roll roll){return roll.tierRank()>0?"T"+roll.tierRank()+" ":"";}
    private static String number(double value){
        String result=String.format(Locale.ROOT,"%.3f",value);while(result.endsWith("0"))result=result.substring(0,result.length()-1);if(result.endsWith("."))result=result.substring(0,result.length()-1);return result;
    }
    public static String value(PickaxeTrait trait,double value){return switch(trait.unit){case PERCENT->percent(value);case LEVEL->number(value)+" lv";case BLOCKS->number(value)+" blocks";case POINTS->number(value);case PERK->"Milestone perk";};}
    public static String baseValue(ArtifactModifiers.Roll roll){var trait=PickaxeTrait.byId(roll.id());return trait==null?"Unknown":trait.growth==PickaxeTrait.Growth.MILESTONE?"Ascension perk":tierPrefix(roll)+"+"+value(trait,roll.value()*trait.power);}
    public static String label(PickaxeTrait trait){return switch(trait){case EXPERIENCE->"Mining XP";case AQUATIC->"Underwater mastery";case AIRBORNE->"Airborne mastery";case SILK->"Silk touch";case SMELTING->"Smelting";default->trait.label;};}
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
    public static List<Component> tool(ItemStack stack){
        var lines=new ArrayList<Component>();var p=PickaxeProgression.read(stack);
        if(PickaxeProgression.supports(stack)){
            lines.add(text("Level ",LABEL).append(text(p.level()+"/100",GOLD)).append(text("  Ascension ",LABEL)).append(text(p.ascensions()+"/10",0xCF9AFF)));
            lines.add(text(p.mastered()?"Mastered":p.needsAscension()?"Ascension required":p.xp()+"/"+PickaxeProgression.xpRequired(p.level())+" XP",p.needsAscension()?GOLD:MUTED));
        }
        double quality=Math.max(0,ArtifactQuality.assemblyScore(stack));
        lines.add(text("Quality ",LABEL).append(text(number(quality)+"%",qualityColor(quality))).append(text("  "+view(stack).stats().size()+" active bonuses",LABEL)));
        if(MiningRuntime.charge(stack)>0)lines.add(text("Resonance "+MiningRuntime.charge(stack)+"/"+(int)ModifierGameplay.CHARGE_CAPACITY,TEAL));
        lines.add(text("Inspect at an Appraisal Table",MUTED));return List.copyOf(lines);
    }
    /** Kept as a source-compatible entry point; details live at the Appraisal Table. */
    public static List<Component> tool(ItemStack stack,int page){return tool(stack);}
    public static int pages(ToolView view){return Math.max(1,(view.stats().size()+7)/8);}
    public static List<Component> tool(ToolView view){return tool(view,0);}
    public static List<Component> tool(ToolView view,int page){
        int pages=pages(view);page=Math.floorMod(page,pages);
        var p=view.progression();var lines=new ArrayList<Component>();
        lines.add(text("Level ",LABEL).append(text(p.level()+"/100",GOLD)).append(text("   Ascension ",LABEL)).append(text(p.ascensions()+"/10",0xCF9AFF)));
        if(p.mastered())lines.add(text("Mastered",GOLD));
        else if(p.needsAscension())lines.add(text("Ascension required",GOLD));
        else {
            int filled=10*p.xp()/PickaxeProgression.xpRequired(p.level());
            lines.add(text("[",MUTED).append(text("|".repeat(filled),TEAL)).append(text("|".repeat(10-filled),0x3D535F)).append(text("] ",MUTED)).append(text(p.xp()+"/"+PickaxeProgression.xpRequired(p.level())+" XP",LABEL)));
        }
        lines.add(text("Assembly quality  ",LABEL).append(text(number(view.score())+"%",qualityColor(view.score()))));
        lines.add(text("MODIFIERS",MUTED));
        if(view.stats().isEmpty())lines.add(text("No effective bonuses",MUTED));
        for(var stat:view.stats().subList(Math.min(page*8,view.stats().size()),Math.min((page+1)*8,view.stats().size())))lines.add(text(label(stat.trait())+"  ",LABEL).append(text((stat.trait().growth==PickaxeTrait.Growth.MILESTONE?"Perk":"+"+value(stat.trait(),stat.value())),magnitude(stat.value()/stat.trait().cap))).append(text("  ",MUTED)).append(gradeRange(stat.low(),stat.high())));
        if(pages>1)lines.add(text("Bonuses "+(page+1)+"/"+pages+"  Shift + wheel",MUTED));
        return List.copyOf(lines);
    }
    public static List<Component> piece(ItemStack stack){
        int score=ArtifactQuality.score(stack);if(score<0)return List.of();
        var data=ComponentWork.recovered(stack);var rolls=ArtifactModifiers.read(stack);
        var lines=new ArrayList<Component>();int rarity=Math.clamp(data.getIntOr("Rarity",0),0,5);
        int color=new int[]{0xC1CBD0,0x8ED879,0x73BAF4,0xCF9AFF,0xFFD369,0xFF8BAB}[rarity];
        lines.add(text(ArtifactModifierCatalog.rarity(rarity).label()+"  ",color).append(text("Quality ",LABEL)).append(text(score+"%",qualityColor(score))));
        String state=rolls.size()+" modifiers"+(!ComponentWork.locked(stack).isEmpty()?"  Locked":"")+(ComponentWork.corrupted(stack)?"  Corrupted":"");
        var summary=text(state,ComponentWork.corrupted(stack)?0xEE8A88:LABEL);if(rolls.stream().anyMatch(AncientModifiers::ancient))summary.append(text("  Ancient",0xB69BE8).withStyle(style->style.withObfuscated(true)));lines.add(summary);lines.add(text("Inspect at an Appraisal Table",MUTED));return List.copyOf(lines);
    }
    public static List<Component> piece(int score,int rarity,List<ArtifactModifiers.Roll> rolls){
        var lines=new ArrayList<Component>();
        int tierColor=new int[]{0xC1CBD0,0x8ED879,0x73BAF4,0xCF9AFF,0xFFD369,0xFF8BAB}[Math.clamp(rarity,0,5)];
        lines.add(text(ArtifactModifierCatalog.rarity(rarity).label()+" part",tierColor).append(text("   Quality ",LABEL)).append(text(score+"%",qualityColor(score))));
        for(var roll:rolls){
            if(AncientModifiers.ancient(roll)){lines.add(text("Ancient ",LABEL).append(text("forgotten runes",0xB69BE8).withStyle(style->style.withObfuscated(true))));continue;}
            var trait=PickaxeTrait.byId(roll.id());if(trait==null)continue;double effective=roll.value()*trait.power*score/100.0;var grade=roll.scaling();
            if(trait.growth==PickaxeTrait.Growth.MILESTONE){
                lines.add(text(label(trait)+"  ",LABEL).append(grade.badge()).append(text("  "+percent(Math.min(1,effective))+" activation",TEAL)));
                int offset=grade.ordinal()<=1?1:grade.ordinal()>=4?-1:0;
                lines.add(text("  Ascension milestones "+(3+offset)+" / "+(6+offset)+" / "+(9+offset),MUTED));continue;
            }
            var line=text(label(trait)+"  ",LABEL).append(text("+"+value(trait,effective),magnitude(effective/trait.cap))).append(text("  ",MUTED)).append(grade.badge());
            var tag=ArtifactModifierCatalog.spec(trait).influence();if(tag!=ModifierInfluence.NONE)line.append(text(" ["+tag.label+"]",tag.color));lines.add(line);
            lines.add(text("  Base ",MUTED).append(text("+"+value(trait,roll.value()*trait.power),magnitude(roll.value()/trait.cap))).append(text(roll.tierRank()>0?"  T"+roll.tierRank():"",TEAL)).append(text("  Lv ",MUTED)).append(text("+"+percent(grade.levelGrowth),grade.color)).append(text("  Asc ",MUTED)).append(text("+"+percent(grade.ascensionGrowth),grade.color)));
        }
        return List.copyOf(lines);
    }
    public static void markCurrent(ItemStack stack){var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();root.putInt(REVISION_KEY,REVISION);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));}
    /** One-time refresh for owned old items; no seeds, rolls, quality, or earned levels change. */
    public static boolean refresh(ItemStack stack){
        if(stack.isEmpty())return false;
        boolean tool=PickaxeProgression.supports(stack)||PickaxeModifiers.pattern(stack)!=null,part=false;
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
