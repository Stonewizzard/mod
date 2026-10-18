package com.example.examplemod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;

/** One veiled roll replaces a normal Epic+ roll; its three alternatives are saved at discovery. */
public final class AncientModifiers {
    public static final String ID="examplemod:ancient_modifier",OFFERS="AncientOffers";
    private static final double[] CHANCES={0,0,0,.25,.35,.50};
    public static boolean ancient(ArtifactModifiers.Roll roll){return roll.id().equals(ID);}
    public static List<ArtifactModifiers.Definition> eligible(ArtifactProfile.Find find,int rarity,List<ArtifactModifiers.Roll> rolls){
        var pool=ArtifactModifierCatalog.pool(find,rarity);var ids=new HashSet<String>();var groups=new HashSet<String>();
        for(var roll:rolls)if(!ancient(roll)){ids.add(roll.id());for(var entry:pool.entries())if(entry.id().equals(roll.id())&&!entry.exclusiveGroup().isEmpty())groups.add(entry.exclusiveGroup());}
        return pool.entries().stream().filter(d->!ids.contains(d.id())&&(d.exclusiveGroup().isEmpty()||!groups.contains(d.exclusiveGroup()))).toList();
    }
    public static CompoundTag discover(CompoundTag record,ArtifactProfile profile,ArtifactModifiers.Pool pool){
        if(profile.rarity()<3||!ArtifactModifierCatalog.pickaxePart(profile.find())||!pool.equals(ArtifactModifierCatalog.pool(profile.find(),profile.rarity())))return record;
        var random=new Random(profile.seed()^0x3C6EF372FE94F82BL^((long)profile.find().ordinal()<<32));
        if(random.nextDouble()>=CHANCES[Math.clamp(profile.rarity(),0,5)])return record;
        var rolls=new ArrayList<>(ArtifactModifiers.read(record));var indexes=new ArrayList<Integer>();
        for(int i=0;i<rolls.size();i++){var others=new ArrayList<>(rolls);others.remove(i);if(eligible(profile.find(),profile.rarity(),others).size()>=3)indexes.add(i);}
        if(indexes.isEmpty())return record;
        int index=indexes.get(random.nextInt(indexes.size()));var others=new ArrayList<>(rolls);others.remove(index);
        var offers=alternatives(profile,others,random.nextLong());if(offers.size()!=3)return record;
        rolls.set(index,new ArtifactModifiers.Roll(ID,0,Math.clamp(record.getIntOr("ExcavationScore",0),0,100)/100.0,ModifierScaling.B));
        ArtifactModifiers.writeRolls(record,rolls);var choices=new CompoundTag();choices.putInt("Version",1);record.put(OFFERS,ArtifactModifiers.writeRolls(choices,offers));return record;
    }
    public static List<ArtifactModifiers.Roll> alternatives(ArtifactProfile profile,List<ArtifactModifiers.Roll> existing,long seed){
        var available=new ArrayList<>(eligible(profile.find(),profile.rarity(),existing));var random=new Random(seed);var result=new ArrayList<ArtifactModifiers.Roll>();
        while(result.size()<3&&!available.isEmpty()){
            int total=available.stream().mapToInt(ArtifactModifiers.Definition::weight).sum();int choice=random.nextInt(total),index=0;
            while(choice>=available.get(index).weight()){choice-=available.get(index).weight();index++;}
            var entry=available.remove(index);long rollSeed=random.nextLong();var gradeProfile=new ArtifactProfile(rollSeed,profile.rarity(),profile.matrix(),profile.find());
            result.add(new ArtifactModifiers.Roll(entry.id(),entry.minimumValue()+random.nextDouble()*(entry.maximumValue()-entry.minimumValue()),1,ModifierScaling.roll(gradeProfile,entry.id())));
        }
        return List.copyOf(result);
    }
    public static List<ArtifactModifiers.Roll> offers(CompoundTag record){return ArtifactModifiers.read(record.getCompoundOrEmpty(OFFERS));}
    private AncientModifiers(){}
}
