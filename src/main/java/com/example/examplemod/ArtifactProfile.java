package com.example.examplemod;

import java.util.Random;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** A sealed specimen. All randomness is fixed before excavation or reward claiming. */
public record ArtifactProfile(long seed,int rarity,Matrix matrix,Find find) {
    public static final String KEY="examplemod:artifact_profile";
    public enum Matrix {
        SANDSTONE("Sandstone",3,"sandstone"),SHALE("Shale",4,"stone"),DEEPSLATE("Deepslate",5,"deepslate"),BASALT("Basalt",6,"basalt_side"),OBSIDIAN("Obsidian",7,"obsidian");
        public final String label,texture;public final int depth;
        Matrix(String label,int depth,String texture){this.label=label;this.depth=depth;this.texture=texture;}
    }
    public enum Find {LEGACY,PICKAXE_HEAD,SWORD_BLADE,AXE_HEAD,RESONANT_CORE,ALLOY;
        public String path(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static ArtifactProfile legacy(){return new ArtifactProfile(0,0,Matrix.SANDSTONE,Find.LEGACY);}
    public static ArtifactProfile roll(long seed,int rarity){
        rarity=Math.clamp(rarity,0,3);var random=new Random(seed);
        int choice=random.nextInt(100);
        Matrix matrix=switch(rarity){case 0->choice<70?Matrix.SANDSTONE:Matrix.SHALE;case 1->choice<60?Matrix.SHALE:Matrix.DEEPSLATE;case 2->choice<35?Matrix.DEEPSLATE:Matrix.BASALT;default->choice<25?Matrix.BASALT:Matrix.OBSIDIAN;};
        int kind=random.nextInt(100);
        Find find=kind<(rarity>=2?30:0)?Find.RESONANT_CORE:kind<45?Find.ALLOY:Find.values()[1+random.nextInt(3)];
        return new ArtifactProfile(seed,rarity,matrix,find);
    }
    public String rarityName(){return new String[]{"Common","Uncommon","Rare","Epic"}[rarity];}
    public static ArtifactProfile read(ItemStack stack){
        var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var tag=root.getCompoundOrEmpty(KEY);
        if(tag.getIntOr("Version",0)!=1)return legacy();
        return new ArtifactProfile(tag.getLongOr("Seed",0),Math.clamp(tag.getIntOr("Rarity",0),0,3),Matrix.values()[Math.clamp(tag.getIntOr("Matrix",0),0,4)],Find.values()[Math.clamp(tag.getIntOr("Find",0),0,5)]);
    }
    public static boolean sealed(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(KEY).getIntOr("Version",0)==1;}
    public void write(ItemStack stack){
        var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var tag=new CompoundTag();
        tag.putInt("Version",1);tag.putLong("Seed",seed);tag.putInt("Rarity",rarity);tag.putInt("Matrix",matrix.ordinal());tag.putInt("Find",find.ordinal());root.put(KEY,tag);
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
    }
    /** Existing progressed studies retain their original footprint and score. */
    public static void seal(ItemStack stack,long seed){
        if(sealed(stack))return;
        var old=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(ExcavationStudy.KEY);
        (old.getIntOr("Version",0)==1?legacy():roll(seed,ArchaeologyContent.rarity(stack))).write(stack);
    }
}
