package com.example.examplemod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Server-side rolls and versioned persistence shared by excavation and pickaxe assembly. */
public final class ArtifactModifiers {
    public static final String KEY="examplemod:part_modifiers";
    public record Definition(String id,int weight,int minimumRarity,int minimumScore,double minimumValue,double maximumValue,String exclusiveGroup){
        public Definition {
            if(id==null||!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")||weight<=0||minimumRarity<0||minimumRarity>5||minimumScore<0||minimumScore>100||!Double.isFinite(minimumValue)||!Double.isFinite(maximumValue)||minimumValue>maximumValue||exclusiveGroup==null)throw new IllegalArgumentException("Invalid modifier definition: "+id);
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
    public record Roll(String id,double value,double qualityScale,ModifierScaling scaling){
        public Roll(String id,double value,double qualityScale){this(id,value,qualityScale,ModifierScaling.B);}
        public Roll {if(scaling==null)scaling=ModifierScaling.B;}
    }
    public static List<Roll> roll(ArtifactProfile profile,int score,Pool pool){
        var random=new Random(profile.seed()^0x6A09E667F3BCC909L^((long)profile.find().ordinal()*0x9E3779B97F4A7C15L));
        var available=new ArrayList<>(pool.entries().stream().filter(d->profile.rarity()>=d.minimumRarity()&&score>=d.minimumScore()).toList());
        var result=new ArrayList<Roll>();var groups=new HashSet<String>();
        for(int attempt=0;attempt<pool.rolls()&&!available.isEmpty();attempt++){
            if(random.nextDouble()>=pool.chances().get(attempt))continue;
            long weight=0;for(var d:available)weight+=d.weight();double choice=random.nextDouble()*weight;Definition selected=available.getLast();
            for(var d:available){choice-=d.weight();if(choice<0){selected=d;break;}}
            result.add(new Roll(selected.id(),selected.minimumValue()+random.nextDouble()*(selected.maximumValue()-selected.minimumValue()),Math.clamp(score,0,100)/100.0,ModifierScaling.roll(profile,selected.id())));
            if(!selected.exclusiveGroup().isEmpty())groups.add(selected.exclusiveGroup());
            String chosen=selected.id();available.removeIf(d->d.id().equals(chosen)||(!d.exclusiveGroup().isEmpty()&&groups.contains(d.exclusiveGroup())));
        }
        return List.copyOf(result);
    }
    public static CompoundTag record(ArtifactProfile profile,int score,Pool pool){
        var tag=new CompoundTag();tag.putInt("Version",1);tag.putInt("CatalogRevision",ArtifactModifierCatalog.REVISION);tag.putString("Part",profile.find().name());tag.putLong("Seed",profile.seed());tag.putInt("ExcavationScore",score);
        return writeRolls(tag,roll(profile,score,pool));
    }
    public static CompoundTag writeRolls(CompoundTag tag,List<Roll> rolls){
        tag.putInt("Count",rolls.size());var entries=new CompoundTag();
        for(int i=0;i<rolls.size();i++){var roll=rolls.get(i);var data=new CompoundTag();data.putString("Id",roll.id());data.putDouble("Value",roll.value());data.putDouble("QualityScale",roll.qualityScale());data.putString("Scaling",roll.scaling().name());entries.put(Integer.toString(i),data);}
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
        for(int i=0;i<Math.clamp(tag.getIntOr("Count",0),0,8);i++){var data=entries.getCompoundOrEmpty(Integer.toString(i));String id=data.getStringOr("Id","");double value=data.getDoubleOr("Value",0),scale=data.getDoubleOr("QualityScale",0);if(!id.isEmpty()&&Double.isFinite(value)&&Double.isFinite(scale))result.add(new Roll(id,value,Math.clamp(scale,0,1),ModifierScaling.read(data.getStringOr("Scaling","B"))));}
        return List.copyOf(result);
    }
    private ArtifactModifiers(){}
}


