package com.example.examplemod;

import net.minecraft.world.entity.player.Player;

/** Spend the player's current XP balance in points, not levels or lifetime totalExperience. */
public final class AnvilExperience {
    public static int available(Player player){
        double level=Math.max(0,player.experienceLevel);
        double base=level<=16?level*level+6*level:level<=31?(5*level*level-81*level+720)/2:(9*level*level-325*level+4440)/2;
        if(base>=Integer.MAX_VALUE)return Integer.MAX_VALUE;
        int partial=Math.max(0,Math.round(player.experienceProgress*player.getXpNeededForNextLevel()));
        return (int)Math.min(Integer.MAX_VALUE,base+partial);
    }
    static boolean spend(Player player,int points){
        if(points<0)throw new IllegalArgumentException("XP cost cannot be negative");
        if(player.getAbilities().instabuild||points==0)return true;
        if(available(player)<points)return false;
        player.giveExperiencePoints(-points);
        return true;
    }
    private AnvilExperience(){}
}
