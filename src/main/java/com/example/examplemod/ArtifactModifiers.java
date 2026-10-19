package com.example.examplemod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Server-side rolls and versioned persistence shared by excavation and pickaxe assembly. */
public final class ArtifactModifiers {
    public static final String KEY="examplemod:part_modifiers";
    public record Definition(String id, int weight, int minimumRarity, int minimumScore,
                             List<ModifierTier> tiers, String exclusiveGroup) {
        /** Single-band definitions remain useful for fixed rewards and extension code. */
        public Definition(String id, int weight, int minimumRarity, int minimumScore,
                          double minimumValue, double maximumValue, String exclusiveGroup) {
            this(id, weight, minimumRarity, minimumScore,
                List.of(new ModifierTier("flat", 1, minimumValue, maximumValue, List.of(1,1,1,1,1,1))), exclusiveGroup);
        }
        public Definition {
            tiers = List.copyOf(tiers);
            if (id == null || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || weight <= 0
                    || minimumRarity < 0 || minimumRarity > 5 || minimumScore < 0 || minimumScore > 100
                    || tiers.isEmpty() || exclusiveGroup == null) {
                throw new IllegalArgumentException("Invalid modifier definition: " + id);
            }
            var ids = new HashSet<String>();
            var ranks = new HashSet<Integer>();
            for (var tier : tiers) {
                if (!ids.add(tier.id()) || !ranks.add(tier.rank()))
                    throw new IllegalArgumentException("Duplicate modifier tier: " + id + "/" + tier.id());
            }
        }
        public boolean eligible(int rarity, int score) {
            return rarity >= minimumRarity && score >= minimumScore && tiers.stream().anyMatch(t -> t.weight(rarity) > 0);
        }
        public boolean contains(Roll roll, int rarity) {
            return rarity >= minimumRarity && id.equals(roll.id()) && tiers.stream().anyMatch(t -> t.weight(rarity) > 0
                && t.id().equals(roll.tierId()) && t.rank() == roll.tierRank()
                && roll.value() >= t.minimum() && roll.value() <= t.maximum());
        }
    }
    public record Pool(List<Double> chances,List<Definition> entries){
        public Pool(int rolls,double chance,List<Definition> entries){this(java.util.Collections.nCopies(rolls,chance),entries);}
        public Pool {
            chances=List.copyOf(chances);entries=List.copyOf(entries);
            if(chances.size()>8||chances.stream().anyMatch(c->!Double.isFinite(c)||c<0||c>1))throw new IllegalArgumentException("Invalid modifier chances");
            var ids=new HashSet<String>();for(var entry:entries)if(!ids.add(entry.id()))throw new IllegalArgumentException("Duplicate modifier: "+entry.id());
        }
        public int rolls(){return chances.size();}
    }
    /** Raw rolled value and excavation quality stay separate for future effectiveness formulas. */
    public record Roll(String id, double value, double qualityScale, ModifierScaling scaling, String tierId, int tierRank) {
        public Roll(String id, double value, double qualityScale) { this(id,value,qualityScale,ModifierScaling.B); }
        /** Old saved rolls and hand-authored comparison tools have no invented tier. */
        public Roll(String id, double value, double qualityScale, ModifierScaling scaling) { this(id,value,qualityScale,scaling,"",0); }
        public Roll {
            if (scaling == null) scaling = ModifierScaling.B;
            if (tierId == null || tierId.isEmpty() || tierRank < 1) { tierId = ""; tierRank = 0; }
        }
        public Roll withValue(double value) { return new Roll(id,value,qualityScale,scaling,tierId,tierRank); }
        public Roll withScaling(ModifierScaling grade) { return new Roll(id,value,qualityScale,grade,tierId,tierRank); }
        public Roll withQuality(double quality) { return new Roll(id,value,quality,scaling,tierId,tierRank); }
    }
    /** Shared by natural discoveries, unveiling, and corruption additions. */
    public static Roll sample(Definition definition, ArtifactProfile profile, double quality, Random random) {
        var tier = weighted(definition.tiers(), t -> t.weight(profile.rarity()), random);
        double value = tier.minimum() + random.nextDouble() * (tier.maximum() - tier.minimum());
        return new Roll(definition.id(), value, Math.clamp(quality,0,1.2), ModifierScaling.roll(profile,definition.id()), tier.id(), tier.rank());
    }
    static <T> T weighted(List<T> choices, ToIntFunction<T> weight, Random random) {
        long total = 0;
        for (var choice : choices) total = Math.addExact(total, weight.applyAsInt(choice));
        if (total <= 0) throw new IllegalArgumentException("No eligible weighted choices");
        double draw = random.nextDouble() * total;
        T last = null;
        for (var choice : choices) {
            int amount = weight.applyAsInt(choice);
            if (amount <= 0) continue;
            last = choice;
            draw -= amount;
            if (draw < 0) return choice;
        }
        return last;
    }
    public static List<Roll> roll(ArtifactProfile profile,int score,Pool pool){
        return roll(profile,score,pool,List.of());
    }
    static List<Roll> roll(ArtifactProfile profile,int score,Pool pool,List<Roll> protectedRolls){
        var random=new Random(profile.seed()^0x6A09E667F3BCC909L^((long)profile.find().ordinal()*0x9E3779B97F4A7C15L));
        var available=new ArrayList<>(pool.entries().stream().filter(d->d.eligible(profile.rarity(),score)).toList());
        if(protectedRolls.size()>pool.rolls())throw new IllegalArgumentException("Too many protected rolls");
        var result=new ArrayList<>(protectedRolls);var groups=new HashSet<String>();var fixedIds=new HashSet<String>();
        for(var fixed:protectedRolls){fixedIds.add(fixed.id());for(var entry:pool.entries())if(entry.id().equals(fixed.id())&&!entry.exclusiveGroup().isEmpty())groups.add(entry.exclusiveGroup());}
        available.removeIf(d->fixedIds.contains(d.id())||!d.exclusiveGroup().isEmpty()&&groups.contains(d.exclusiveGroup()));
        for(int attempt=protectedRolls.size();attempt<pool.rolls()&&!available.isEmpty();attempt++){
            if(random.nextDouble()>=pool.chances().get(attempt))continue;
            var selected=weighted(available,Definition::weight,random);
            result.add(sample(selected,profile,Math.clamp(score,0,100)/100.0,random));
            if(!selected.exclusiveGroup().isEmpty())groups.add(selected.exclusiveGroup());
            String chosen=selected.id();available.removeIf(d->d.id().equals(chosen)||(!d.exclusiveGroup().isEmpty()&&groups.contains(d.exclusiveGroup())));
        }
        return List.copyOf(result);
    }
    public static CompoundTag record(ArtifactProfile profile,int score,Pool pool){
        return record(profile,score,pool,ModifierInfluence.NONE,List.of());
    }
    static CompoundTag record(ArtifactProfile profile,int score,Pool pool,ModifierInfluence influence,List<Roll> protectedRolls){
        var tag=new CompoundTag();tag.putInt("Version",1);tag.putInt("CatalogRevision",ArtifactModifierCatalog.REVISION);tag.putString("Part",profile.find().name());tag.putLong("Seed",profile.seed());tag.putInt("ExcavationScore",score);
        if(influence!=ModifierInfluence.NONE)tag.putString("Influence",influence.id);
        var protectedIds=new HashSet<String>();for(var fixed:protectedRolls)protectedIds.add(fixed.id());
        return AncientModifiers.discover(writeRolls(tag,roll(profile,score,pool,protectedRolls)),profile,pool,influence,protectedIds);
    }
    public static CompoundTag writeRolls(CompoundTag tag,List<Roll> rolls){
        tag.putInt("Count",rolls.size());var entries=new CompoundTag();
        for(int i=0;i<rolls.size();i++){var roll=rolls.get(i);var data=new CompoundTag();data.putString("Id",roll.id());data.putDouble("Value",roll.value());data.putDouble("QualityScale",roll.qualityScale());data.putString("Scaling",roll.scaling().name());if(!roll.tierId().isEmpty()){data.putString("ValueTier",roll.tierId());data.putInt("TierRank",roll.tierRank());}entries.put(Integer.toString(i),data);}
        tag.put("Rolls",entries);return tag;
    }
    /** Called at completion, with extraction fallback for previously completed artifacts. Never rerolls. */
    public static void seal(ItemStack stack,ExcavationStudy study){
        if(!study.complete())return;
        var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if(root.contains(KEY))return;
        root.put(KEY,record(study.profile(),study.condition(),ArtifactModifierCatalog.pool(study.profile().find(),study.profile().rarity())));
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
    }
    public static void transfer(ItemStack source,ItemStack target){
        var data=source.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();if(!data.contains(KEY))return;
        var root=target.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();root.put(KEY,data.getCompoundOrEmpty(KEY).copy());target.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
    }
    /** Unknown future/removed IDs remain stored and readable; no registry lookup destroys old rolls. */
    public static List<Roll> read(ItemStack stack){
        return read(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(KEY));
    }
    public static List<Roll> read(CompoundTag tag){
        if(tag.getIntOr("Version",0)!=1)return List.of();
        var entries=tag.getCompoundOrEmpty("Rolls");var result=new ArrayList<Roll>();
        for(int i=0;i<Math.clamp(tag.getIntOr("Count",0),0,8);i++){var data=entries.getCompoundOrEmpty(Integer.toString(i));String id=data.getStringOr("Id","");double value=data.getDoubleOr("Value",0),scale=data.getDoubleOr("QualityScale",0);if(!id.isEmpty()&&Double.isFinite(value)&&Double.isFinite(scale))result.add(new Roll(id,value,Math.clamp(scale,0,1.2),ModifierScaling.read(data.getStringOr("Scaling","B")),data.getStringOr("ValueTier",""),data.getIntOr("TierRank",0)));}
        return List.copyOf(result);
    }
    private ArtifactModifiers(){}
}
