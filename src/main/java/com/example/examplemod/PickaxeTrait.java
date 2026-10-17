package com.example.examplemod;

import java.util.Locale;

/** Effect IDs and hard safety caps; roll ranges/weights live in ArtifactModifierCatalog. */
public enum PickaxeTrait {
    SPEED("mining_speed","Mining speed",Unit.PERCENT,6),
    FORTUNE("fortune","Fortune",Unit.LEVEL,3),
    MAGNET("magnetized","Magnetized",Unit.BLOCKS,12),
    SILK("silk_touch","Silk Touch chance",Unit.PERCENT,1),
    DURABILITY("reinforced","Durability",Unit.PERCENT,2),
    EXPERIENCE("scholar","Mining experience",Unit.PERCENT,3),
    AQUATIC("aquatic","Underwater penalty removed",Unit.PERCENT,1),
    AIRBORNE("steady_hands","Airborne penalty removed",Unit.PERCENT,1),
    REACH("long_reach","Block reach",Unit.BLOCKS,2.5),
    SMELTING("smelter","Smelting chance",Unit.PERCENT,1);
    public enum Unit {PERCENT,LEVEL,BLOCKS}
    public final String id,label;
    public final Unit unit;
    public final double cap;
    PickaxeTrait(String id,String label,Unit unit,double cap){this.id="examplemod:"+id;this.label=label;this.unit=unit;this.cap=cap;}
    public String format(double value){return switch(unit){case PERCENT->String.format(Locale.ROOT,"%.1f%%",value*100);case LEVEL->String.format(Locale.ROOT,"%.2f levels",value);case BLOCKS->String.format(Locale.ROOT,"%.2f blocks",value);};}
    public static PickaxeTrait byId(String id){for(var trait:values())if(trait.id.equals(id))return trait;return null;}
}
