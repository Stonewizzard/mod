package com.example.examplemod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;

/** One veiled roll replaces a normal Epic+ roll; its three alternatives are saved at discovery. */
public final class AncientModifiers {
    public static final String ID="examplemod:ancient_modifier",OFFERS="AncientOffers";
    private static final double[] CHANCES={0,0,0,.25,.35,.50};
    public static boolean ancient(ArtifactModifiers.Roll roll){return roll.id().equals(ID);}
    public static List<ArtifactModifiers.Definition> eligible(ArtifactProfile.Find find,int rarity,List<ArtifactModifiers.Roll> rolls){
        return eligible(find,rarity,rolls,ModifierInfluence.NONE);
    }
    public static List<ArtifactModifiers.Definition> eligible(ArtifactProfile.Find find,int rarity,List<ArtifactModifiers.Roll> rolls,ModifierInfluence influence){
        var pool=ArtifactModifierCatalog.pool(find,rarity,influence);var ids=new HashSet<String>();var groups=new HashSet<String>();
        for(var roll:rolls)if(!ancient(roll)){ids.add(roll.id());for(var entry:pool.entries())if(entry.id().equals(roll.id())&&!entry.exclusiveGroup().isEmpty())groups.add(entry.exclusiveGroup());}
        return pool.entries().stream().filter(d->!ids.contains(d.id())&&(d.exclusiveGroup().isEmpty()||!groups.contains(d.exclusiveGroup()))).toList();
    }
    public static CompoundTag discover(CompoundTag record,ArtifactProfile profile,ArtifactModifiers.Pool pool){
        return discover(record,profile,pool,ModifierInfluence.NONE,Set.of());
    }
    static CompoundTag discover(CompoundTag record,ArtifactProfile profile,ArtifactModifiers.Pool pool,ModifierInfluence influence,Set<String> protectedIds){
        if(profile.rarity()<3||!ArtifactModifierCatalog.pickaxePart(profile.find())||!pool.equals(ArtifactModifierCatalog.pool(profile.find(),profile.rarity(),influence)))return record;
        var random=new Random(profile.seed()^0x3C6EF372FE94F82BL^((long)profile.find().ordinal()<<32));
        if(random.nextDouble()>=CHANCES[Math.clamp(profile.rarity(),0,5)])return record;
        var rolls=new ArrayList<>(ArtifactModifiers.read(record));var indexes=new ArrayList<Integer>();
        for(int i=0;i<rolls.size();i++){if(protectedIds.contains(rolls.get(i).id()))continue;var others=new ArrayList<>(rolls);others.remove(i);if(eligible(profile.find(),profile.rarity(),others,influence).size()>=3)indexes.add(i);}
        if(indexes.isEmpty())return record;
        int index=indexes.get(random.nextInt(indexes.size()));var others=new ArrayList<>(rolls);others.remove(index);
        var offers=alternatives(profile,others,random.nextLong(),influence);if(offers.size()!=3)return record;
        rolls.set(index,new ArtifactModifiers.Roll(ID,0,Math.clamp(record.getIntOr("ExcavationScore",0),0,100)/100.0,ModifierScaling.B));
        ArtifactModifiers.writeRolls(record,rolls);var choices=new CompoundTag();choices.putInt("Version",1);record.put(OFFERS,ArtifactModifiers.writeRolls(choices,offers));return record;
    }
    public static List<ArtifactModifiers.Roll> alternatives(ArtifactProfile profile,List<ArtifactModifiers.Roll> existing,long seed){
        return alternatives(profile,existing,seed,ModifierInfluence.NONE);
    }
    static List<ArtifactModifiers.Roll> alternatives(ArtifactProfile profile,List<ArtifactModifiers.Roll> existing,long seed,ModifierInfluence influence){
        var available=new ArrayList<>(eligible(profile.find(),profile.rarity(),existing,influence));var random=new Random(seed);var result=new ArrayList<ArtifactModifiers.Roll>();
        while(result.size()<3&&!available.isEmpty()){
            var entry=ArtifactModifiers.weighted(available,ArtifactModifiers.Definition::weight,random);available.remove(entry);
            var gradeProfile=new ArtifactProfile(random.nextLong(),profile.rarity(),profile.matrix(),profile.find());
            result.add(ArtifactModifiers.sample(entry,gradeProfile,1,random));
        }
        return List.copyOf(result);
    }
    public static List<ArtifactModifiers.Roll> offers(CompoundTag record){return ArtifactModifiers.read(record.getCompoundOrEmpty(OFFERS));}
    private AncientModifiers(){}
}
