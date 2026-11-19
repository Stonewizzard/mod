package com.example.examplemod;

import net.minecraft.world.Container;
import net.minecraft.world.item.*;

/** Shared, side-effect-free alloying and artifact-melting definitions. */
public final class AncientAlloy {
    public static final int SCRAP_COUNT=1,GOLD_COUNT=1,IRON_COUNT=2,OUTPUT_COUNT=4,MELT_COUNT=1;
    public static boolean artifact(ItemStack s){return !s.isEmpty()&&s.is(ArchaeologyTableMenu.ARTIFACTS);}
    public static boolean melting(Container c){return artifact(c.getItem(0));}
    public static final int CRYSTAL_COUNT=12;
    public static boolean crystallizing(Container c){return c.getItem(0).is(AncientAnvilContent.CRYSTAL.get())||c.getItem(1).is(Items.DIAMOND_BLOCK);}
    public static boolean crystalIngredient(ItemStack s){return s.is(AncientAnvilContent.CRYSTAL.get())||s.is(Items.DIAMOND_BLOCK);}
    /** Pick a compatible batch even when players load the diamond block first. */
    public static boolean accepts(Container c,int slot,ItemStack s){
        if(!accepts(slot,s))return false;
        if(crystalIngredient(s))return (c.getItem(0).isEmpty()||c.getItem(0).is(AncientAnvilContent.CRYSTAL.get()))&&(c.getItem(1).isEmpty()||c.getItem(1).is(Items.DIAMOND_BLOCK))&&c.getItem(2).isEmpty();
        if(crystallizing(c))return false;
        return !(artifact(s)&&(!c.getItem(1).isEmpty()||!c.getItem(2).isEmpty())||slot>0&&melting(c));
    }
    public static int capacity(int slot,ItemStack s){return crystalIngredient(s)?(slot==0?CRYSTAL_COUNT:1):count(slot);}
    public static int count(Container c,int slot){return crystallizing(c)?(slot==0?CRYSTAL_COUNT:slot==1?1:0):melting(c)?(slot==0?1:0):count(slot);}
    public static void consume(Container c){int[] amounts={count(c,0),count(c,1),count(c,2)};for(int i=0;i<3;i++)if(amounts[i]>0)c.removeItem(i,amounts[i]);}
    public static boolean accepts(int slot,ItemStack s){return !s.isEmpty()&&switch(slot){case 0->s.is(AncientAnvilContent.SCRAP)||artifact(s)||s.is(AncientAnvilContent.CRYSTAL.get());case 1->s.is(Items.GOLD_INGOT)||s.is(Items.DIAMOND_BLOCK);case 2->s.is(Items.IRON_INGOT);default->false;};}
    public static int count(int slot){return switch(slot){case 0->SCRAP_COUNT;case 1->GOLD_COUNT;case 2->IRON_COUNT;default->0;};}
    public static Item result(Container c){return crystallizing(c)?AncientAnvilContent.CRYSTAL_BLOCK_ITEM.get():melting(c)?AncientAnvilContent.SCRAP_ITEM.get():AncientAnvilContent.INGOT.get();}
    public static int outputCount(Container c){return crystallizing(c)?1:melting(c)?MELT_COUNT:OUTPUT_COUNT;}
    public static boolean ready(Container c){
        if(crystallizing(c)){if(!c.getItem(0).is(AncientAnvilContent.CRYSTAL.get())||c.getItem(0).getCount()<CRYSTAL_COUNT||!c.getItem(1).is(Items.DIAMOND_BLOCK)||!c.getItem(2).isEmpty())return false;}
        else if(melting(c)){if(!c.getItem(1).isEmpty()||!c.getItem(2).isEmpty())return false;}
        else for(int i=0;i<3;i++)if(!accepts(i,c.getItem(i))||c.getItem(i).getCount()<count(i))return false;
        if(!c.getItem(3).isEmpty()||!c.getItem(4).isEmpty())return false;
        var out=c.getItem(5);return out.isEmpty()||out.is(result(c))&&out.getCount()<=out.getMaxStackSize()-outputCount(c);
    }
    private AncientAlloy(){}
}
