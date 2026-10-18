package com.example.examplemod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Versioned, per-tool progression. Scores and sealed part rolls remain untouched. */
public final class PickaxeProgression {
    public static final String KEY="examplemod:pickaxe_progression";
    public static final int MAX_LEVEL=100,LEVELS_PER_ASCENSION=10,MAX_ASCENSIONS=10;

    public record State(int level,int xp,int ascensions){
        public boolean needsAscension(){return level%LEVELS_PER_ASCENSION==0&&ascensions<level/LEVELS_PER_ASCENSION;}
        public boolean mastered(){return level==MAX_LEVEL&&ascensions==MAX_ASCENSIONS;}
        public int nextGate(){return Math.min(MAX_LEVEL,(ascensions+1)*LEVELS_PER_ASCENSION);}
    }
    public static boolean supports(ItemStack stack){return stack.is(ArchaeologyContent.PICKAXE.get());}
    public static int xpRequired(int level){return 20+2*(Math.clamp(level,1,MAX_LEVEL)-1);}
    public static State read(ItemStack stack){
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(KEY);
        int level=Math.clamp(tag.getIntOr("Level",1),1,MAX_LEVEL);
        int ascensions=Math.clamp(tag.getIntOr("Ascensions",0),0,Math.min(MAX_ASCENSIONS,level/LEVELS_PER_ASCENSION));
        level=Math.min(level,Math.min(MAX_LEVEL,(ascensions+1)*LEVELS_PER_ASCENSION));
        boolean gated=level%LEVELS_PER_ASCENSION==0&&ascensions<level/LEVELS_PER_ASCENSION;
        int xp=gated||level==MAX_LEVEL?0:Math.clamp(tag.getIntOr("XP",0),0,xpRequired(level)-1);
        return new State(level,xp,ascensions);
    }
    static void write(ItemStack stack,State state){
        var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var tag=root.getCompoundOrEmpty(KEY).copy();
        tag.putInt("Version",2);tag.putInt("Level",state.level());tag.putInt("XP",state.xp());tag.putInt("Ascensions",state.ascensions());
        root.put(KEY,tag);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
    }
    public static void initialize(ItemStack stack){
        if(supports(stack)&&!stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().contains(KEY))write(stack,new State(1,0,0));
    }
    /** Returns false at a gate/cap; XP is never banked across an unpaid ascension. */
    public static boolean gain(ItemStack stack,int amount){
        if(!supports(stack)||amount<=0)return false;var before=read(stack);if(before.needsAscension()||before.level()==MAX_LEVEL)return false;
        int level=before.level();long xp=(long)before.xp()+amount;
        while(level<before.nextGate()&&xp>=xpRequired(level)){xp-=xpRequired(level);level++;PickaxeGrowth.awardLevel(stack,level);}
        if(level==before.nextGate())xp=0;
        write(stack,new State(level,(int)xp,before.ascensions()));if(level>before.level())PickaxeModifiers.applyToAssembly(stack);else PickaxeModifiers.updateLore(stack);return true;
    }
    static boolean ascend(ItemStack stack){
        if(!supports(stack))return false;var state=read(stack);if(!state.needsAscension())return false;
        write(stack,new State(state.level(),0,state.ascensions()+1));PickaxeModifiers.applyToAssembly(stack);return true;
    }
    private PickaxeProgression(){}
}

