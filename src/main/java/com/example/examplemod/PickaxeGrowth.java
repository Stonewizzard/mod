package com.example.examplemod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Level-growth counters live beside progression, never inside immutable excavated parts. */
public final class PickaxeGrowth {
    public static final String COUNTERS="LevelGrowth";
    public static void awardLevel(ItemStack tool,int newLevel){
        var candidates=new ArrayList<>(PickaxeModifiers.contributions(tool).stream().filter(c->c.base()>0&&c.trait().growth==PickaxeTrait.Growth.NUMERIC).toList());
        if(candidates.isEmpty())return;
        var root=tool.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        var progression=root.getCompoundOrEmpty(PickaxeProgression.KEY).copy();
        if(!progression.contains("GrowthSeed"))progression.putLong("GrowthSeed",java.util.UUID.randomUUID().getLeastSignificantBits());
        var random=new Random(progression.getLongOr("GrowthSeed",0)^((long)newLevel*0x9E3779B97F4A7C15L));
        Collections.shuffle(candidates,random);int count=1+random.nextInt(Math.min(3,candidates.size()));
        var counters=progression.getCompoundOrEmpty(COUNTERS).copy();
        for(int i=0;i<count;i++){String key=candidates.get(i).key();counters.putInt(key,Math.clamp(counters.getIntOr(key,0),0,98)+1);}
        progression.put(COUNTERS,counters);root.put(PickaxeProgression.KEY,progression);tool.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
    }
    private PickaxeGrowth(){}
}
