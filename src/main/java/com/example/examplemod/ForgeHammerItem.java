package com.example.examplemod;

import net.minecraft.world.item.Item;

/** Future hammer tiers can vary required tier and work per accepted strike. */
public final class ForgeHammerItem extends Item {
    private final int tier,work;
    public ForgeHammerItem(Properties properties,int tier,int work){super(properties);this.tier=tier;this.work=work;}
    public int forgeTier(){return tier;}
    public int workPerStrike(){return work;}
}
