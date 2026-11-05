package com.example.examplemod;

import net.minecraft.util.StringRepresentable;

/** Foundry is the selected design. Other serialized values preserve existing worlds. */
public enum AncientAnvilDesign implements StringRepresentable {
    BASE("base"),FOUNDRY("foundry"),SPILLWAY("spillway"),COLUMNS("columns");
    private final String name;
    AncientAnvilDesign(String name){this.name=name;}
    @Override public String getSerializedName(){return name;}
    public static AncientAnvilDesign of(net.minecraft.world.item.Item item){return item instanceof AncientAnvilDesignItem sample?sample.design():FOUNDRY;}
}
