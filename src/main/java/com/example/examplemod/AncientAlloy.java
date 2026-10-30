package com.example.examplemod;

import net.minecraft.world.Container;
import net.minecraft.world.item.*;

/** One centralized starter-alloy definition, shared by server validation and the screen. */
public final class AncientAlloy {
    public static final int SCRAP_COUNT=1,GOLD_COUNT=1,IRON_COUNT=2,OUTPUT_COUNT=4;
    public static boolean accepts(int slot,ItemStack s){return !s.isEmpty()&&switch(slot){case 0->s.is(AncientAnvilContent.SCRAP);case 1->s.is(Items.GOLD_INGOT);case 2->s.is(Items.IRON_INGOT);default->false;};}
    public static int count(int slot){return switch(slot){case 0->SCRAP_COUNT;case 1->GOLD_COUNT;case 2->IRON_COUNT;default->0;};}
    public static boolean ready(Container c){
        for(int i=0;i<3;i++)if(!accepts(i,c.getItem(i))||c.getItem(i).getCount()<count(i))return false;
        if(!c.getItem(3).isEmpty()||!c.getItem(4).isEmpty())return false;
        var out=c.getItem(5);return out.isEmpty()||out.is(AncientAnvilContent.INGOT.get())&&out.getCount()<=out.getMaxStackSize()-OUTPUT_COUNT;
    }
    private AncientAlloy(){}
}
