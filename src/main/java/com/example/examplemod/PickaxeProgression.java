package com.example.examplemod;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Versioned, per-tool progression. Scores and sealed part rolls remain untouched. */
public final class PickaxeProgression {
    public static final String KEY="examplemod:pickaxe_progression";
    public static final int MAX_LEVEL=100,LEVELS_PER_ASCENSION=10,MAX_ASCENSIONS=10;
    public static final double BONUS_PER_ASCENSION=.10;

    public record State(int level,int xp,int ascensions){
        public boolean needsAscension(){return level%LEVELS_PER_ASCENSION==0&&ascensions<level/LEVELS_PER_ASCENSION;}
        public boolean mastered(){return level==MAX_LEVEL&&ascensions==MAX_ASCENSIONS;}
        public int nextGate(){return Math.min(MAX_LEVEL,(ascensions+1)*LEVELS_PER_ASCENSION);}
        public double multiplier(){return 1+ascensions*BONUS_PER_ASCENSION;}
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
        var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var tag=new CompoundTag();
        tag.putInt("Version",1);tag.putInt("Level",state.level());tag.putInt("XP",state.xp());tag.putInt("Ascensions",state.ascensions());
        root.put(KEY,tag);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
    }
    public static void initialize(ItemStack stack){
        if(supports(stack)&&!stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().contains(KEY))write(stack,new State(1,0,0));
    }
    /** Returns false at a gate/cap; XP is never banked across an unpaid ascension. */
    public static boolean gain(ItemStack stack,int amount){
        if(!supports(stack)||amount<=0)return false;var before=read(stack);if(before.needsAscension()||before.level()==MAX_LEVEL)return false;
        int level=before.level();long xp=(long)before.xp()+amount;
        while(level<before.nextGate()&&xp>=xpRequired(level)){xp-=xpRequired(level);level++;}
        if(level==before.nextGate())xp=0;
        write(stack,new State(level,(int)xp,before.ascensions()));PickaxeModifiers.updateLore(stack);return true;
    }
    static boolean ascend(ItemStack stack){
        if(!supports(stack))return false;var state=read(stack);if(!state.needsAscension())return false;
        write(stack,new State(state.level(),0,state.ascensions()+1));PickaxeModifiers.applyToAssembly(stack);return true;
    }
    public static List<Component> lore(ItemStack stack){
        var state=read(stack);var lines=new ArrayList<Component>();
        lines.add(Component.literal("Level "+state.level()+"/100 | Ascension "+state.ascensions()+"/10").withStyle(ChatFormatting.GOLD));
        lines.add(Component.literal(state.mastered()?"Mastered":state.needsAscension()?"Ascension required":state.xp()+" / "+xpRequired(state.level())+" tool XP").withStyle(state.needsAscension()?ChatFormatting.YELLOW:ChatFormatting.GRAY));
        if(state.ascensions()>0)lines.add(Component.literal("Ascension modifier bonus: +"+(state.ascensions()*10)+"%").withStyle(ChatFormatting.DARK_AQUA));
        return lines;
    }
    private PickaxeProgression(){}
}
