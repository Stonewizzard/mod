package com.example.examplemod;

import java.util.Random;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Growth fractions multiply the original score-adjusted roll; they never compound. */
public enum ModifierScaling {
    F(.03,.00025,0x929292),
    D(.05,.00050,0xC3A57D),
    C(.075,.00075,0x8ED879),
    B(.10,.00100,0x73BAF4),
    A(.15,.00150,0xCF9AFF),
    S(.20,.00200,0xFFD369);

    public final double ascensionGrowth,levelGrowth;
    public final int color;
    ModifierScaling(double ascensionGrowth,double levelGrowth,int color){this.ascensionGrowth=ascensionGrowth;this.levelGrowth=levelGrowth;this.color=color;}
    // F,D,C,B,A,S. Base roll strength and grade use independent seeded random streams.
    private static final int[][] WEIGHTS={
        {50,28,14,6,1,1},{35,30,20,10,4,1},{20,26,26,18,8,2},
        {10,20,27,25,14,4},{5,10,22,30,25,8},{2,6,15,28,34,15}
    };
    public static ModifierScaling roll(ArtifactProfile profile,String modifier){
        if(PickaxeTrait.byId(modifier)==null)return B;
        var random=new Random(profile.seed()^0xBB67AE8584CAA73BL^((long)profile.find().ordinal()*0x9E3779B97F4A7C15L)^((long)modifier.hashCode()*0xD1B54A32D192ED03L));
        int choice=random.nextInt(100);var weights=WEIGHTS[Math.clamp(profile.rarity(),0,5)];
        for(var grade:values()){choice-=weights[grade.ordinal()];if(choice<0)return grade;}
        throw new IllegalStateException("Scaling grade weights must total 100");
    }
    /** Missing grades are B: old ascensions retain their original +10% per stage. */
    public static ModifierScaling read(String name){for(var grade:values())if(grade.name().equals(name))return grade;return B;}
    public MutableComponent badge(){return Component.literal("["+name()+"]").withStyle(s->s.withColor(color).withItalic(false));}
}
