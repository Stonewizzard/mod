package com.example.examplemod;

import java.text.Normalizer;
import java.util.*;

/** Public knowledge only: the guide never reads, rolls or changes a player's artifact. */
public final class FieldGuide {
    public enum Category {
        START("Start","Getting started"),CREATURES("Creatures","Creatures and infection"),ARTIFACTS("Artifacts","Artifacts and geology"),EXCAVATION("Excavate","Excavation and field tools"),EQUIPMENT("Equipment","Reconstruction and progression"),STATIONS("Stations","Component customization"),MODIFIERS("Modifiers","Modifier reference");
        public final String tab,title;Category(String tab,String title){this.tab=tab;this.title=title;}
    }
    public enum Group {
        ALL("All topics"),SPEED("Mining speed"),HARVEST("Loot and experience"),AREA("Area mining"),RESONANCE("Resonance"),SURVIVAL("Survival"),UTILITY("Universal parts"),NETHER("Nether influence"),END("End influence");
        public final String label;Group(String label){this.label=label;}
    }
    public record Entry(String id,Category category,Group group,String title,String icon,List<String> paragraphs,List<String> links,String keywords){
        public Entry {paragraphs=List.copyOf(paragraphs);links=List.copyOf(links);}
        String searchable(){return normalize(title+" "+category.title+" "+group.label+" "+id+" "+keywords+" "+String.join(" ",paragraphs));}
    }
    private static final class Library {
        static final List<Entry> ENTRIES=create();
        static final Map<String,String> SEARCH=index();
        static Map<String,String> index(){var map=new HashMap<String,String>();for(var entry:ENTRIES){if(map.put(entry.id(),entry.searchable())!=null)throw new IllegalStateException("Duplicate field guide entry: "+entry.id());}return Map.copyOf(map);}
    }
    public static List<Entry> entries(){return Library.ENTRIES;}
    public static Entry entry(String id){return entries().stream().filter(e->e.id().equals(id)).findFirst().orElse(null);}
    public static List<Entry> search(String query,Category category,Group group){
        String cleaned=normalize(query);var words=cleaned.isEmpty()?new String[0]:cleaned.split(" ");
        return entries().stream().filter(e->category==null||e.category()==category).filter(e->group==null||group==Group.ALL||e.group()==group)
            .filter(e->{String text=Library.SEARCH.get(e.id());for(String word:words)if(!text.contains(word))return false;return true;}).sorted(Comparator.comparingInt((Entry e)->relevance(e,cleaned,words)).reversed()).toList();
    }
    private static int relevance(Entry entry,String query,String[] words){
        if(query.isEmpty())return 0;String title=normalize(entry.title()),keywords=normalize(entry.keywords()),id=normalize(entry.id());
        int score=title.equals(query)?1000:title.startsWith(query)?500:title.contains(query)?250:0;
        for(String word:words){if(title.contains(word))score+=100;if(id.contains(word))score+=75;if(keywords.contains(word))score+=25;}return score;
    }
    static String normalize(String text){return Normalizer.normalize(text==null?"":text,Normalizer.Form.NFKD).replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT).replace('_',' ').replace('-',' ').trim().replaceAll("\\s+"," ");}
    private static List<Entry> create(){
        var result=new ArrayList<Entry>(FieldGuideChapters.entries());
        for(var trait:PickaxeTrait.values()){
            var spec=ArtifactModifierCatalog.spec(trait);var lines=new ArrayList<String>();
            lines.add(ModifierDescriptions.description(trait));
            lines.add(trait.utility?"Rolls on handles and connectors. Works on every reconstructed tool type.":"Rolls on the three pickaxe head pieces. Mining effects apply to reconstructed pickaxes.");
            lines.add("Earliest artifact rarity: "+ArtifactModifierCatalog.rarity(spec.minimumRarity()).label()+". "+(spec.influenceOnly()?spec.influence().label+" influence is required.":spec.influence()==ModifierInfluence.NONE?"No influence is required.":"A "+spec.influence().label+" themed modifier; the matching influence increases its roll weight."));
            if(trait.growth==PickaxeTrait.Growth.NUMERIC){
                lines.add("Each piece contributes its base roll multiplied by excavation quality. At 50% quality, +10% becomes +5%. Normal selected-level growth and ascension growth then add to that base contribution.");
                lines.add("Combined cap: "+PickaxeTooltips.value(trait,trait.cap)+". Applicable conditions and global caps still apply.");
            }else lines.add("Activation is quality-scaled, capped at 100%. Perks start at stage 0. F/D unlock stages at ascensions 4/7/10; C/B at 3/6/9; A/S at 2/5/8. A 0% piece grants no effect.");
            for(var rule:ModifierTuning.rules(trait).entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()){
                boolean time=rule.getKey()==ModifierTuning.Key.COOLDOWN||rule.getKey()==ModifierTuning.Key.DURATION;
                String values=rule.getValue().stream().map(v->number(time?v/20:v)).collect(java.util.stream.Collectors.joining(" / "));
                lines.add(rule.getKey().name().toLowerCase(Locale.ROOT).replace('_',' ')+": "+values+(time?" seconds":"")+" at stages 0 / 1 / 2 / 3.");
            }
            lines.add("Value tiers below show base effect at 100% quality, before growth and combined caps. T1 is strongest. Rarity changes the relative weights of the tiers, not the meaning of a saved tier.");
            if(trait.unit!=PickaxeTrait.Unit.PERK)for(var tier:spec.tiers())lines.add("T"+tier.rank()+": "+PickaxeTooltips.value(trait,tier.minimum()*trait.power)+" to "+PickaxeTooltips.value(trait,tier.maximum()*trait.power)+". Rarity weights C/U/R/E/L/Unique: "+tier.rarityWeights().stream().map(Object::toString).collect(java.util.stream.Collectors.joining("/"))+".");
            var material=AscensionResources.BINDINGS.get(trait);lines.add("Ascension resource: "+new net.minecraft.world.item.ItemStack(material.item()).getHoverName().getString()+". Cost per occurrence: "+material.baseCount()+" times the upcoming ascension number. Repeated resources combine.");
            if(trait.growth==PickaxeTrait.Growth.NUMERIC)lines.add("Grade growth is relative to base. F/D/C/B/A/S add 6/10/15/22/32/45% per ascension and 0.05/0.10/0.15/0.25/0.35/0.50% per selected level.");
            result.add(new Entry(trait.id,Category.MODIFIERS,group(trait),trait.label,"examplemod:restored_ancient_pickaxe",lines,List.of("modifier_basics","scaling_and_quality","influences","ascension"),trait.name()+" "+trait.family.name()+" "+spec.influence().label));
        }return List.copyOf(result);
    }
    private static Group group(PickaxeTrait trait){
        var spec=ArtifactModifierCatalog.spec(trait);if(spec.influence()!=ModifierInfluence.NONE)return spec.influence()==ModifierInfluence.NETHER?Group.NETHER:Group.END;
        if(trait.utility)return Group.UTILITY;
        return switch(trait.family){case SPEED->Group.SPEED;case LOOT,TRANSFORM->Group.HARVEST;case AREA,PLACE->Group.AREA;case CHARGE->Group.RESONANCE;case STATUS->Group.SURVIVAL;case PROGRESSION->Group.HARVEST;case ATTRIBUTE->trait==PickaxeTrait.SPEED?Group.SPEED:trait==PickaxeTrait.REACH?Group.UTILITY:Group.SURVIVAL;case COLLECTION->Group.HARVEST;case UTILITY->Group.UTILITY;};
    }
    private static String number(double value){return String.format(Locale.ROOT,"%.2f",value).replaceAll("0+$","").replaceAll("\\.$","");}
    private FieldGuide(){}
}
