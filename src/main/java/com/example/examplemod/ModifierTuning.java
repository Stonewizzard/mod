package com.example.examplemod;
import java.util.*;
/** Generated stage parameters; the inspector uses this exact same data. */
public final class ModifierTuning {
    public enum Key { AMOUNT, AMPLIFIER, BATCH, CHANCE, COOLDOWN, COST, DAMAGE, DURATION, INTERVAL, MULTIPLIER, RADIUS, RANGE, REPAIR, STRENGTH, YIELD_BONUS }
    private static final Map<PickaxeTrait,Map<Key,List<Double>>> RULES=rules();
    private static Map<PickaxeTrait,Map<Key,List<Double>>> rules(){
        var m=new EnumMap<PickaxeTrait,Map<Key,List<Double>>>(PickaxeTrait.class);
        m.put(PickaxeTrait.SELF_REPAIR,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(100.0,100.0,100.0,100.0))));
        m.put(PickaxeTrait.METAL_MAINTENANCE,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(100.0,100.0,100.0,100.0))));
        m.put(PickaxeTrait.RAIN_REPAIR,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(100.0,100.0,100.0,100.0))));
        m.put(PickaxeTrait.XP_CONDENSER,Map.ofEntries(Map.entry(Key.INTERVAL,List.of(4.0,3.0,2.0,2.0)),Map.entry(Key.MULTIPLIER,List.of(1.5,2.0,2.5,3.0)),Map.entry(Key.AMOUNT,List.of(2.0,4.0,8.0,12.0))));
        m.put(PickaxeTrait.GEM_ECHO,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(40.0,40.0,40.0,40.0)),Map.entry(Key.AMOUNT,List.of(2.0,4.0,6.0,8.0))));
        m.put(PickaxeTrait.LUCKY_REFUND,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(12.0,12.0,12.0,12.0))));
        m.put(PickaxeTrait.METAL_NUGGETS,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(4.0,8.0,12.0,16.0))));
        m.put(PickaxeTrait.FOSSIL_DUST,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(4.0,8.0,12.0,16.0))));
        m.put(PickaxeTrait.SALVAGE_STRING,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(4.0,8.0,12.0,16.0))));
        m.put(PickaxeTrait.PRISMATIC_SHARDS,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(4.0,8.0,12.0,16.0))));
        m.put(PickaxeTrait.MOSS_GARDENER,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(4.0,8.0,12.0,16.0))));
        m.put(PickaxeTrait.VEIN_MINER,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(8.0,16.0,32.0,48.0))));
        m.put(PickaxeTrait.TUNNELER,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(2.0,4.0,6.0,8.0))));
        m.put(PickaxeTrait.BROADHEAD,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(8.0,17.0,24.0,49.0))));
        m.put(PickaxeTrait.STAIRMAKER,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(2.0,4.0,6.0,8.0))));
        m.put(PickaxeTrait.SURFACE_SCRAPER,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(8.0,16.0,32.0,48.0))));
        m.put(PickaxeTrait.GRAVEL_VACUUM,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(8.0,16.0,24.0,32.0))));
        m.put(PickaxeTrait.ORE_BRANCH,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(4.0,8.0,16.0,32.0))));
        m.put(PickaxeTrait.CRYSTAL_HARVEST,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(4.0,8.0,16.0,24.0))));
        m.put(PickaxeTrait.OBSIDIAN_CHISEL,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(4.0,8.0,16.0,32.0))));
        m.put(PickaxeTrait.TIMBER_SUPPORT,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(20.0,10.0,5.0,2.0))));
        m.put(PickaxeTrait.LIGHTBEARER,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(20.0,10.0,5.0,2.0))));
        m.put(PickaxeTrait.LAVA_CORK,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(20.0,10.0,5.0,2.0))));
        m.put(PickaxeTrait.WATER_CORK,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(20.0,10.0,5.0,2.0))));
        m.put(PickaxeTrait.BRIDGEWORK,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(20.0,10.0,5.0,2.0))));
        m.put(PickaxeTrait.ECOLOGIST,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(20.0,10.0,5.0,2.0))));
        m.put(PickaxeTrait.NIGHT_SIGHT,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(100.0,100.0,100.0,100.0)),Map.entry(Key.DURATION,List.of(400.0,600.0,900.0,1200.0)),Map.entry(Key.AMPLIFIER,List.of(0.0,0.0,0.0,0.0))));
        m.put(PickaxeTrait.MINERS_SPRINT,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(100.0,100.0,100.0,100.0)),Map.entry(Key.DURATION,List.of(160.0,240.0,300.0,400.0)),Map.entry(Key.AMPLIFIER,List.of(0.0,0.0,1.0,1.0))));
        m.put(PickaxeTrait.STONE_SKIN,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(100.0,100.0,100.0,100.0)),Map.entry(Key.DURATION,List.of(120.0,160.0,200.0,240.0)),Map.entry(Key.AMPLIFIER,List.of(0.0,0.0,1.0,1.0))));
        m.put(PickaxeTrait.FIRE_WARD,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(100.0,100.0,100.0,100.0)),Map.entry(Key.DURATION,List.of(200.0,400.0,600.0,900.0)),Map.entry(Key.AMPLIFIER,List.of(0.0,0.0,0.0,0.0))));
        m.put(PickaxeTrait.ABSORPTION,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(200.0,200.0,200.0,200.0)),Map.entry(Key.DURATION,List.of(200.0,300.0,400.0,600.0)),Map.entry(Key.AMPLIFIER,List.of(0.0,1.0,2.0,3.0))));
        m.put(PickaxeTrait.SOFT_LANDING,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(100.0,100.0,100.0,100.0)),Map.entry(Key.DURATION,List.of(160.0,240.0,320.0,480.0)),Map.entry(Key.AMPLIFIER,List.of(0.0,0.0,0.0,0.0))));
        m.put(PickaxeTrait.ECHO_CHAMBER,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(80.0,80.0,80.0,80.0)),Map.entry(Key.DURATION,List.of(80.0,120.0,160.0,200.0)),Map.entry(Key.AMPLIFIER,List.of(1.0,2.0,3.0,4.0)),Map.entry(Key.RADIUS,List.of(5.0,7.0,9.0,12.0))));
        m.put(PickaxeTrait.QUARRY_MARK,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(100.0,100.0,100.0,100.0)),Map.entry(Key.DURATION,List.of(200.0,300.0,450.0,600.0)),Map.entry(Key.AMPLIFIER,List.of(0.0,0.0,0.0,0.0)),Map.entry(Key.RADIUS,List.of(5.0,7.0,9.0,12.0))));
        m.put(PickaxeTrait.FALLING_STAR,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(200.0,200.0,200.0,200.0)),Map.entry(Key.AMOUNT,List.of(2.0,4.0,6.0,8.0)),Map.entry(Key.RADIUS,List.of(6.0,8.0,10.0,12.0)),Map.entry(Key.STRENGTH,List.of(1.0,1.25,1.5,2.0))));
        m.put(PickaxeTrait.HUNGER_SAVER,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(80.0,80.0,80.0,80.0)),Map.entry(Key.AMOUNT,List.of(2.0,3.0,4.0,6.0))));
        m.put(PickaxeTrait.RECOIL_STEP,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(40.0,40.0,40.0,40.0)),Map.entry(Key.RANGE,List.of(2.0,3.0,4.0,6.0)),Map.entry(Key.COST,List.of(0.0,0.0,0.0,0.0))));
        m.put(PickaxeTrait.OVERCHARGE,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(60.0,60.0,60.0,60.0)),Map.entry(Key.DURATION,List.of(100.0,160.0,240.0,300.0)),Map.entry(Key.AMPLIFIER,List.of(0.0,1.0,1.0,2.0)),Map.entry(Key.COST,List.of(3.0,3.0,3.0,3.0))));
        m.put(PickaxeTrait.KINETIC_BATTERY,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(20.0,20.0,20.0,20.0)),Map.entry(Key.AMOUNT,List.of(3.0,5.0,8.0,12.0))));
        m.put(PickaxeTrait.XP_BATTERY,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(4.0,6.0,10.0,16.0))));
        m.put(PickaxeTrait.SURGE_DISCHARGE,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(0.0,0.0,0.0,0.0)),Map.entry(Key.COST,List.of(3.0,3.0,3.0,3.0)),Map.entry(Key.AMOUNT,List.of(4.0,8.0,16.0,24.0))));
        m.put(PickaxeTrait.LOOT_DISCHARGE,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(20.0,20.0,20.0,20.0)),Map.entry(Key.COST,List.of(3.0,3.0,3.0,3.0)),Map.entry(Key.AMOUNT,List.of(4.0,8.0,12.0,16.0))));
        m.put(PickaxeTrait.RESTORATIVE_DISCHARGE,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(40.0,40.0,40.0,40.0)),Map.entry(Key.COST,List.of(4.0,4.0,4.0,4.0)),Map.entry(Key.AMOUNT,List.of(40.0,80.0,160.0,320.0))));
        m.put(PickaxeTrait.AURA_DISCHARGE,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(80.0,80.0,80.0,80.0)),Map.entry(Key.COST,List.of(4.0,4.0,4.0,4.0)),Map.entry(Key.RADIUS,List.of(6.0,10.0,14.0,20.0)),Map.entry(Key.DURATION,List.of(160.0,240.0,320.0,400.0)),Map.entry(Key.AMPLIFIER,List.of(0.0,1.0,1.0,2.0))));
        m.put(PickaxeTrait.RECIPROCAL_CHAIN,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(3.0,6.0,10.0,16.0))));
        m.put(PickaxeTrait.XP_SIPHON,Map.ofEntries(Map.entry(Key.MULTIPLIER,List.of(1.1,1.2,1.35,1.5)),Map.entry(Key.REPAIR,List.of(0.5,1.0,1.5,2.0))));
        m.put(PickaxeTrait.COMPACTOR,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(20.0,20.0,20.0,20.0)),Map.entry(Key.BATCH,List.of(1.0,2.0,4.0,8.0))));
        m.put(PickaxeTrait.SPOILS_SHIELD,Map.ofEntries(Map.entry(Key.DURATION,List.of(200.0,400.0,600.0,1200.0))));
        m.put(PickaxeTrait.DUST_CLOUD,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(80.0,80.0,80.0,80.0)),Map.entry(Key.DURATION,List.of(80.0,120.0,160.0,200.0)),Map.entry(Key.AMPLIFIER,List.of(0.0,1.0,2.0,3.0)),Map.entry(Key.RADIUS,List.of(5.0,7.0,9.0,12.0))));
        m.put(PickaxeTrait.SOUL_FURNACE,Map.ofEntries(Map.entry(Key.COST,List.of(1.0,1.0,1.0,1.0)),Map.entry(Key.MULTIPLIER,List.of(1.5,2.0,2.5,3.0)),Map.entry(Key.YIELD_BONUS,List.of(0.0,0.1,0.2,0.35))));
        m.put(PickaxeTrait.BASALT_HEART,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(80.0,80.0,80.0,80.0)),Map.entry(Key.AMOUNT,List.of(12.0,24.0,48.0,96.0)),Map.entry(Key.DURATION,List.of(160.0,240.0,320.0,400.0)),Map.entry(Key.AMPLIFIER,List.of(0.0,0.0,1.0,1.0))));
        m.put(PickaxeTrait.SOUL_CLEANSE,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(80.0,80.0,80.0,80.0)),Map.entry(Key.AMOUNT,List.of(1.0,2.0,3.0,4.0)),Map.entry(Key.DURATION,List.of(100.0,160.0,240.0,320.0)),Map.entry(Key.AMPLIFIER,List.of(0.0,0.0,0.0,0.0))));
        m.put(PickaxeTrait.PIGLIN_PURSE,Map.ofEntries(Map.entry(Key.AMOUNT,List.of(9.0,18.0,27.0,36.0))));
        m.put(PickaxeTrait.GHAST_BREATH,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(60.0,60.0,60.0,60.0)),Map.entry(Key.COST,List.of(3.0,3.0,3.0,3.0)),Map.entry(Key.RADIUS,List.of(6.0,8.0,10.0,12.0)),Map.entry(Key.DURATION,List.of(80.0,120.0,160.0,240.0)),Map.entry(Key.STRENGTH,List.of(1.0,1.25,1.5,2.0))));
        m.put(PickaxeTrait.EMBERLIGHT,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(20.0,10.0,5.0,2.0)),Map.entry(Key.COST,List.of(1.0,1.0,1.0,1.0))));
        m.put(PickaxeTrait.INFERNAL_ECHO,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(0.0,0.0,0.0,0.0)),Map.entry(Key.COST,List.of(3.0,3.0,3.0,3.0)),Map.entry(Key.AMOUNT,List.of(16.0,32.0,48.0,64.0))));
        m.put(PickaxeTrait.CHORUS_HOP,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(40.0,40.0,40.0,40.0)),Map.entry(Key.COST,List.of(2.0,2.0,2.0,2.0)),Map.entry(Key.RANGE,List.of(4.0,6.0,8.0,12.0))));
        m.put(PickaxeTrait.ENDER_DISPATCH,Map.ofEntries(Map.entry(Key.RADIUS,List.of(0.0,6.0,12.0,24.0)),Map.entry(Key.COOLDOWN,List.of(10.0,10.0,10.0,10.0))));
        m.put(PickaxeTrait.REPULSOR,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(60.0,60.0,60.0,60.0)),Map.entry(Key.RADIUS,List.of(6.0,8.0,10.0,14.0)),Map.entry(Key.STRENGTH,List.of(1.0,1.25,1.5,2.0))));
        m.put(PickaxeTrait.ENTANGLEMENT,Map.ofEntries(Map.entry(Key.MULTIPLIER,List.of(0.25,0.35,0.45,0.6)),Map.entry(Key.RADIUS,List.of(6.0,10.0,14.0,20.0))));
        m.put(PickaxeTrait.TEMPORAL_ECHO,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(20.0,20.0,20.0,20.0)),Map.entry(Key.COST,List.of(2.0,2.0,2.0,2.0)),Map.entry(Key.AMOUNT,List.of(3.0,6.0,9.0,16.0)),Map.entry(Key.DURATION,List.of(600.0,600.0,600.0,600.0))));
        m.put(PickaxeTrait.SHULKER_PULSE,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(60.0,60.0,60.0,60.0)),Map.entry(Key.COST,List.of(3.0,3.0,3.0,3.0)),Map.entry(Key.RADIUS,List.of(6.0,8.0,10.0,12.0)),Map.entry(Key.DURATION,List.of(40.0,60.0,80.0,120.0)),Map.entry(Key.AMPLIFIER,List.of(0.0,0.0,1.0,1.0))));
        m.put(PickaxeTrait.ENDSTONE_ALCHEMY,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(40.0,40.0,40.0,40.0)),Map.entry(Key.COST,List.of(2.0,2.0,2.0,2.0)),Map.entry(Key.CHANCE,List.of(0.35,0.55,0.75,1.0)),Map.entry(Key.AMOUNT,List.of(1.0,1.0,2.0,3.0))));
        m.put(PickaxeTrait.VOID_ANCHOR,Map.ofEntries(Map.entry(Key.COOLDOWN,List.of(1200.0,900.0,600.0,400.0)),Map.entry(Key.COST,List.of(10.0,8.0,6.0,4.0)),Map.entry(Key.DAMAGE,List.of(0.1,0.08,0.06,0.04))));
        m.put(PickaxeTrait.DIMENSIONAL_BATTERY,Map.ofEntries(Map.entry(Key.RADIUS,List.of(4.0,8.0,12.0,16.0)),Map.entry(Key.AMOUNT,List.of(3.0,6.0,10.0,16.0))));
        return Map.copyOf(m);
    }
    public static Map<Key,List<Double>> rules(PickaxeTrait trait){return RULES.getOrDefault(trait,Map.of());}
    public static double value(PickaxeTrait trait,Key key,int stage){var values=rules(trait).get(key);return values==null?0:values.get(Math.clamp(stage,0,3));}
    public static int integer(PickaxeTrait trait,Key key,int stage){return (int)value(trait,key,stage);}
    private ModifierTuning(){}
}
