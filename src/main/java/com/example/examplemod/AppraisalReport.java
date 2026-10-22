package com.example.examplemod;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Read-only explanations shared by the screen and native tests. Never seals, rolls or unveils input. */
public final class AppraisalReport {
    public record Entry(String name,int color,List<String> lines){public Entry{lines=List.copyOf(lines);}}
    public record Report(String name,List<Entry> entries){public Report{entries=List.copyOf(entries);}}
    private static boolean artifact(ItemStack stack){return ArchaeologyContent.artifacts().stream().anyMatch(stack::is);}
    public static boolean accepts(ItemStack stack){return !stack.isEmpty()&&(artifact(stack)||ComponentWork.component(stack)||PickaxeModifiers.pattern(stack)!=null||PickaxeProgression.supports(stack));}
    public static Report read(ItemStack stack){
        if(!accepts(stack))return new Report("Appraisal Table",List.of(new Entry("Getting started",PickaxeTooltips.TEAL,List.of("Place an artifact, recovered component or restored tool in the slot.","Choose an entry to inspect its effects. Use the arrows to turn pages.","Appraisal never changes the item."))));
        var entries=new ArrayList<Entry>();boolean tool=PickaxeModifiers.pattern(stack)!=null||PickaxeProgression.supports(stack);
        var overview=new ArrayList<String>();
        if(tool){
            var state=PickaxeProgression.read(stack);overview.add("Assembly quality: "+fmt(Math.max(0,ArtifactQuality.assemblyScore(stack)))+"%.");
            if(PickaxeProgression.supports(stack)){
                overview.add("Level "+state.level()+" / 100. Ascension "+state.ascensions()+" / 10.");overview.add(state.mastered()?"Mastered.":state.needsAscension()?"Ascend at the Ascender to continue leveling.":state.xp()+" / "+PickaxeProgression.xpRequired(state.level())+" tool XP to next level.");
                overview.add("Innate mining multiplier: "+fmt(PickaxeModifiers.coreSpeed(stack))+"x native speed. Includes native Efficiency/Haste; penalties still apply.");
                overview.add("Core charge: quality-scaled 1 per primary break and 1 per four seconds held. Resonance: "+MiningRuntime.charge(stack)+" / "+(int)ModifierGameplay.CHARGE_CAPACITY+".");
                overview.add("Crouch to enable area mining. Right-click air for Recoil; sneak-right-click air for Chorus. Other modifiers trigger under their stated conditions.");
            }
            overview.add("Durability: "+(stack.getMaxDamage()-stack.getDamageValue())+" / "+stack.getMaxDamage()+".");
        }else if(artifact(stack)){
            overview.add("Rarity: "+ArtifactModifierCatalog.rarity(ArchaeologyContent.rarity(stack)).label()+".");
            if(ArtifactProfile.sealed(stack))overview.add("Rock matrix: "+ArtifactProfile.read(stack).matrix().label+".");
            overview.add("Excavate this specimen at an Archaeology Table. Hidden finds and modifiers are not revealed here.");
        }else{
            overview.add("Recovered quality: "+Math.max(0,ArtifactQuality.score(stack))+"%. Each modifier uses this piece's own quality, not the assembly average.");
            overview.add("Original excavation score: "+ComponentWork.recovered(stack).getIntOr("Quality",0)+"%.");
            overview.add("Rarity: "+ArtifactModifierCatalog.rarity(ComponentWork.recovered(stack).getIntOr("Rarity",0)).label()+".");
            var influence=ComponentInfluences.current(stack);if(influence!=ModifierInfluence.NONE)overview.add(influence.label+" influence: permanent. Matching families have 6x weight; foreign families are excluded.");
            if(ComponentWork.corrupted(stack))overview.add("Corrupted: no further component modifications.");
            if(!ComponentWork.locked(stack).isEmpty())overview.add("One modifier is permanently locked.");
        }
        entries.add(new Entry("Overview",PickaxeTooltips.GOLD,overview));
        if(tool){
            var totals=PickaxeModifiers.totals(stack);
            for(var trait:PickaxeTrait.values()){
                var sources=PickaxeModifiers.contributions(stack).stream().filter(c->c.trait()==trait).toList();if(sources.isEmpty())continue;
                var lines=describe(trait,totals.get(trait),PickaxeModifiers.stage(stack,trait),true);int asc=PickaxeProgression.read(stack).ascensions();
                for(var c:sources){lines.add("Source: "+c.key().split("\\|")[0].replace('_',' ')+". Quality "+fmt(c.quality()*100)+"%. "+c.roll().scaling().name()+" scaling, "+tier(c.roll())+". Effective "+PickaxeTooltips.value(trait,c.value(asc))+".");growth(lines,c.roll());}
                entries.add(new Entry(trait.label,PickaxeTooltips.TEAL,lines));
            }
        }else if(!artifact(stack)){
            for(var roll:ArtifactModifiers.read(stack)){
                if(AncientModifiers.ancient(roll)){entries.add(new Entry("Ancient runes",0xB69BE8,List.of("An unread ancient modifier. Reveal its fixed choices at the Unveiler. Appraisal does not expose those choices.")));continue;}
                var trait=PickaxeTrait.byId(roll.id());if(trait==null){entries.add(new Entry("Unknown modifier",PickaxeTooltips.MUTED,List.of("This saved modifier is not recognized. Its data is preserved.")));continue;}
                double quality=Math.max(0,ArtifactQuality.score(stack))/100.0,value=Math.clamp(roll.value()*trait.power,0,trait.cap)*quality;
                var lines=describe(trait,value,0,false);lines.add("Rolled base at 100% quality: "+PickaxeTooltips.value(trait,roll.value()*trait.power)+". "+tier(roll)+".");growth(lines,roll);
                if(roll.id().equals(ComponentWork.locked(stack)))lines.add("Locked: cannot be annulled or modified.");
                entries.add(new Entry(trait.label,PickaxeTooltips.TEAL,lines));
            }
        }
        return new Report(stack.getHoverName().getString(),entries);
    }
    private static ArrayList<String> describe(PickaxeTrait trait,double effective,int stage,boolean tool){
        var lines=new ArrayList<String>();lines.add(ModifierDescriptions.description(trait));
        lines.add(trait.growth==PickaxeTrait.Growth.MILESTONE?"Activation: "+PickaxeTooltips.percent(Math.min(1,effective))+". "+(tool?"Current stage: "+stage+" / 3.":"Starts at stage 0 when assembled."):"Current contribution: "+PickaxeTooltips.value(trait,effective)+".");
        if(effective<=0)lines.add("Dormant: zero excavation quality gives no effect.");
        for(var rule:ModifierTuning.rules(trait).entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()){
            boolean time=rule.getKey()==ModifierTuning.Key.COOLDOWN||rule.getKey()==ModifierTuning.Key.DURATION;
            String values=rule.getValue().stream().map(v->fmt(time?v/20:v)).collect(java.util.stream.Collectors.joining(" / "));
            lines.add(rule.getKey().name().toLowerCase(Locale.ROOT).replace('_',' ')+": "+values+(time?" seconds":"")+" (stages 0 / 1 / 2 / 3).");
        }
        if(trait.unit!=PickaxeTrait.Unit.PERK)lines.add("Combined cap: "+PickaxeTooltips.value(trait,trait.cap)+".");
        lines.add("Pool: "+(trait.utility?"universal handle/connector":"pickaxe head")+". Earliest rarity: "+ArtifactModifierCatalog.rarity(ArtifactModifierCatalog.spec(trait).minimumRarity()).label()+".");
        for(var tier:ArtifactModifierCatalog.spec(trait).tiers())lines.add("T"+tier.rank()+": "+PickaxeTooltips.value(trait,tier.minimum()*trait.power)+" to "+PickaxeTooltips.value(trait,tier.maximum()*trait.power)+" at 100% quality.");
        var resource=AscensionResources.BINDINGS.get(trait);lines.add("Ascension: "+resource.baseCount()+" x upcoming ascension of "+resource.item().getName(new ItemStack(resource.item())).getString()+" per occurrence; repeated costs combine.");
        return lines;
    }
    private static void growth(List<String> lines,ArtifactModifiers.Roll roll){
        var grade=roll.scaling();lines.add("Scaling "+grade.name()+": +"+PickaxeTooltips.percent(grade.ascensionGrowth)+" of base per ascension, +"+PickaxeTooltips.percent(grade.levelGrowth)+" per selected level for numeric effects.");
        int offset=grade.ordinal()<=1?1:grade.ordinal()>=4?-1:0;lines.add("Perk stages unlock at ascensions "+(3+offset)+" / "+(6+offset)+" / "+(9+offset)+"; perks do not grow numerically.");
    }
    private static String tier(ArtifactModifiers.Roll roll){return roll.tierRank()>0?"value tier T"+roll.tierRank():"legacy value tier";}
    static String fmt(double value){return String.format(Locale.ROOT,"%.2f",value).replaceAll("0+$","").replaceAll("\\.$","");}
    private AppraisalReport(){}
}
