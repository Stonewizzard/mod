package com.example.examplemod;

import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Mutable per-tool charge/cooldowns are separate from immutable recovered component data. */
public final class MiningRuntime {
    public static final String KEY="examplemod:mining_runtime";
    private static final ThreadLocal<HashSet<UUID>> SECONDARY=ThreadLocal.withInitial(HashSet::new);
    private static final ThreadLocal<java.util.Map<UUID,Boolean>> FURNACE=ThreadLocal.withInitial(java.util.HashMap::new);
    static boolean furnace(UUID player){return FURNACE.get().containsKey(player);}
    static boolean soulFurnace(UUID player){return Boolean.TRUE.equals(FURNACE.get().get(player));}
    static void begin(UUID player,boolean furnace,boolean soul){begin(player);if(furnace)FURNACE.get().put(player,soul);}
    public static boolean secondary(UUID player){return SECONDARY.get().contains(player);}
    static void begin(UUID player){SECONDARY.get().add(player);}
    static void end(UUID player){SECONDARY.get().remove(player);FURNACE.get().remove(player);}
    public static CompoundTag read(ItemStack tool){return tool.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(KEY).copy();}
    public static void edit(ItemStack tool,Consumer<CompoundTag> action){var root=tool.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var state=root.getCompoundOrEmpty(KEY).copy();action.accept(state);root.put(KEY,state);tool.set(DataComponents.CUSTOM_DATA,CustomData.of(root));}
    public static int charge(ItemStack tool){return Math.clamp(read(tool).getIntOr("Charge",0),0,(int)ModifierGameplay.CHARGE_CAPACITY);}
    public static void addCharge(ItemStack tool,int amount){if(amount>0)edit(tool,s->s.putInt("Charge",Math.clamp(s.getIntOr("Charge",0)+Math.min(amount,(int)ModifierGameplay.CHARGE_CAPACITY),0,(int)ModifierGameplay.CHARGE_CAPACITY)));}
    public static boolean spend(ItemStack tool,int cost){if(cost<0||charge(tool)<cost)return false;edit(tool,s->s.putInt("Charge",charge(tool)-cost));return true;}
    static boolean cooldown(ItemStack tool,String key,long now,int ticks){
        var state=read(tool);long last=state.getLongOr("CD_"+key,Long.MIN_VALUE/2);if(last>now){edit(tool,s->s.putLong("CD_"+key,now));return false;}
        if(now-last<ticks)return false;edit(tool,s->s.putLong("CD_"+key,now));return true;
    }
    static int chain(ItemStack tool,long now){var s=read(tool);return now-s.getLongOr("LastBreak",Long.MIN_VALUE/2)<=60?Math.clamp(s.getIntOr("Chain",0),0,5):0;}
    static boolean varied(ItemStack tool,long now,String block){var s=read(tool);return chain(tool,now)>0&&!s.getStringOr("LastBlock",block).equals(block);}
    static void noteBreak(ItemStack tool,long now,String block,boolean ore){int chain=chain(tool,now);edit(tool,s->{s.putLong("LastBreak",now);s.putInt("Chain",Math.min(5,chain+1));s.putString("LastBlock",block);s.putBoolean("LastOre",ore);});}
    public static void repair(ItemStack tool,int amount){if(amount>0&&tool.isDamaged())tool.setDamageValue(Math.max(0,tool.getDamageValue()-amount));}
    private MiningRuntime(){}
}
