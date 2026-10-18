package com.example.examplemod;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import static com.example.examplemod.PickaxeTrait.*;

/** All live roll balance in one place. Only the five pickaxe-compatible parts have active pools. */
public final class ArtifactModifierCatalog {
    public static final int REVISION=3;
    public record Range(double minimum,double maximum){}
    public record Tier(String label,List<Double> chances,Map<PickaxeTrait,Range> ranges){}
    private static Range r(double minimum,double maximum){return new Range(minimum,maximum);}
    // Each entry is one independent attempt. The first is guaranteed; extra attempts may fail.
    private static final List<Tier> TIERS=List.of(
        new Tier("Common",List.of(1.0),Map.of(
            SPEED,r(.04,.12),FORTUNE,r(.10,.35),DURABILITY,r(.05,.12),EXPERIENCE,r(.05,.15))),
        new Tier("Uncommon",List.of(1.0,.20),Map.of(
            SPEED,r(.08,.22),FORTUNE,r(.20,.55),DURABILITY,r(.08,.20),EXPERIENCE,r(.10,.25),MAGNET,r(.75,1.75),AQUATIC,r(.10,.25),AIRBORNE,r(.10,.25))),
        new Tier("Rare",List.of(1.0,.45,.10),Map.of(
            SPEED,r(.12,.40),FORTUNE,r(.30,.75),DURABILITY,r(.12,.30),EXPERIENCE,r(.15,.40),MAGNET,r(1,2.5),AQUATIC,r(.15,.40),AIRBORNE,r(.15,.40),SILK,r(.10,.25),REACH,r(.15,.40))),
        new Tier("Epic",List.of(1.0,.65,.25),Map.of(
            SPEED,r(.20,.65),FORTUNE,r(.40,.90),DURABILITY,r(.18,.40),EXPERIENCE,r(.20,.60),MAGNET,r(1.5,3.5),AQUATIC,r(.20,.55),AIRBORNE,r(.20,.55),SILK,r(.20,.45),REACH,r(.25,.60),SMELTING,r(.15,.35))),
        new Tier("Legendary",List.of(1.0,.80,.50,.15),Map.of(
            SPEED,r(.30,.90),FORTUNE,r(.55,1.10),DURABILITY,r(.25,.50),EXPERIENCE,r(.30,.80),MAGNET,r(2,4.5),AQUATIC,r(.30,.70),AIRBORNE,r(.30,.70),SILK,r(.35,.65),REACH,r(.35,.80),SMELTING,r(.25,.55))),
        new Tier("Unique",List.of(1.0,.90,.70,.35),Map.of(
            SPEED,r(.45,1.20),FORTUNE,r(.70,1.25),DURABILITY,r(.35,.65),EXPERIENCE,r(.45,1.0),MAGNET,r(3,6),AQUATIC,r(.40,.85),AIRBORNE,r(.40,.85),SILK,r(.50,.85),REACH,r(.50,1.0),SMELTING,r(.40,.75)))
    );
    /** Relative role weights are independent of tier/value ranges. Harvest traits exclude one another per piece. */
    public static Map<PickaxeTrait,Integer> weights(ArtifactProfile.Find part){return switch(part){
        case PICKAXE_LEFT->Map.of(SPEED,32,FORTUNE,30,DURABILITY,12,EXPERIENCE,15,SILK,8,SMELTING,8);
        case PICKAXE_BRIDGE->Map.of(SPEED,24,DURABILITY,30,EXPERIENCE,18,FORTUNE,14,SILK,8,SMELTING,10);
        case PICKAXE_RIGHT->Map.of(FORTUNE,30,SPEED,28,DURABILITY,14,EXPERIENCE,18,SILK,12,SMELTING,12);
        case TOOL_CONNECTOR->Map.of(DURABILITY,26,EXPERIENCE,22,MAGNET,28,FORTUNE,10,REACH,12,AQUATIC,10,SMELTING,8);
        case TOOL_HANDLE->Map.of(SPEED,24,DURABILITY,24,EXPERIENCE,14,MAGNET,22,AQUATIC,18,AIRBORNE,18,REACH,14);
        default->Map.of();
    };}
    public static Tier tier(int rarity){return TIERS.get(Math.clamp(rarity,0,5));}
    public static boolean pickaxePart(ArtifactProfile.Find part){return !weights(part).isEmpty();}
    public static ArtifactModifiers.Pool pool(ArtifactProfile.Find part){return pool(part,0);}
    public static ArtifactModifiers.Pool pool(ArtifactProfile.Find part,int rarity){
        var tier=tier(rarity);var weights=weights(part);var definitions=new ArrayList<ArtifactModifiers.Definition>();
        // Enum order, rather than Map iteration order, makes seeds stable across JVM launches.
        for(var trait:PickaxeTrait.values()){
            var range=tier.ranges().get(trait);var weight=weights.get(trait);if(range==null||weight==null)continue;
            String group=trait==SILK||trait==FORTUNE||trait==SMELTING?"harvest":"";
            definitions.add(new ArtifactModifiers.Definition(trait.id,weight,0,0,range.minimum(),range.maximum(),group));
        }
        return new ArtifactModifiers.Pool(definitions.isEmpty()?List.of():tier.chances(),definitions);
    }
    private ArtifactModifierCatalog(){}
}

