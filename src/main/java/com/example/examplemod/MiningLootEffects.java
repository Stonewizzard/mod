package com.example.examplemod;

import java.util.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import static com.example.examplemod.PickaxeTrait.*;
import static com.example.examplemod.ModifierTuning.Key.*;

/** Loot changes operate on the actual event drops; they never reroll containers or secondary bonuses. */
final class MiningLootEffects {
    static double fortune(MiningContext c){
        double value=c.get(FORTUNE);if(c.ore)value+=c.get(ORE_FORTUNE);if(c.gem)value+=c.get(GEM_FORTUNE);
        if(c.block.contains("redstone_ore"))value+=c.get(REDSTONE_FORTUNE);if(c.block.contains("coal_ore"))value+=c.get(COAL_FORTUNE);
        if(c.ore&&(c.block.contains("iron")||c.block.contains("gold")||c.block.contains("copper")))value+=c.get(METAL_FORTUNE);return Math.min(ModifierGameplay.MAX_FORTUNE,value);
    }
    static double silk(MiningContext c){return Math.min(1,c.get(SILK)+(MiningConditions.still(c.player)?c.get(PRECISION_SILK):0));}
    static double smeltChance(MiningContext c){
        double chance=Math.min(1,c.get(SMELTING)+c.get(PYROCLASTIC)+.04*c.get(CINDER_CHAIN));
        if(!c.primary&&MiningRuntime.furnace(c.player.getUUID())){c.soulFurnace=c.get(SOUL_FURNACE)>0;return 1;}
        if(c.primary&&c.get(SOUL_FURNACE)>0&&c.chance(SOUL_FURNACE)){
            boolean recipe=false;
            for(var e:c.event.getDrops()){
                var stack=e.getItem().copy();stack.setCount(1);
                if(c.level.getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMELTING,new net.minecraft.world.item.crafting.SingleRecipeInput(stack),c.level).isPresent()){recipe=true;break;}
            }
            if(recipe&&MiningChargeEffects.pay(c.player,c.tool,c.count(SOUL_FURNACE,COST))){c.soulFurnace=true;chance=1;}
        }return chance;
    }
    static void bonus(MiningContext c){
        if(c.silk||c.event.getState().hasBlockEntity())return;
        // Pure material conversions compose with area mining; primary-only bonus resources do not.
        if(c.block.equals("gravel")&&c.roll(GRAVEL_SIFTER))convert(c,Items.GRAVEL,Items.FLINT);
        if((c.block.equals("sand")||c.block.equals("red_sand"))&&c.roll(SAND_GLASS)){convert(c,Items.SAND,Items.GLASS);convert(c,Items.RED_SAND,Items.GLASS);}
        if(c.roll(CLAY_FIRING))convert(c,Items.CLAY_BALL,Items.BRICK);
        if(c.roll(STONE_SMOOTHER))convert(c,Items.COBBLESTONE,Items.STONE);
        if(c.roll(DEEPSLATE_POLISHER))convert(c,Items.COBBLED_DEEPSLATE,Items.POLISHED_DEEPSLATE);
        if(!c.primary)return;
        if(c.ore&&c.chance(METAL_NUGGETS)){if(c.block.contains("iron"))add(c,new ItemStack(Items.IRON_NUGGET,c.count(METAL_NUGGETS,AMOUNT)));else if(c.block.contains("gold"))add(c,new ItemStack(Items.GOLD_NUGGET,c.count(METAL_NUGGETS,AMOUNT)));}
        if((c.block.equals("calcite")||c.block.equals("tuff"))&&c.chance(FOSSIL_DUST))add(c,new ItemStack(Items.BONE_MEAL,c.count(FOSSIL_DUST,AMOUNT)));
        if(c.block.equals("cobweb")&&c.chance(SALVAGE_STRING))add(c,new ItemStack(Items.STRING,c.count(SALVAGE_STRING,AMOUNT)));
        if(c.block.equals("amethyst_cluster")&&c.chance(PRISMATIC_SHARDS))add(c,new ItemStack(Items.AMETHYST_SHARD,c.count(PRISMATIC_SHARDS,AMOUNT)));
        if(c.block.contains("moss")&&c.chance(MOSS_GARDENER))add(c,new ItemStack(Items.BONE_MEAL,c.count(MOSS_GARDENER,AMOUNT)));
        if((c.block.contains("gold_ore")||c.block.equals("gilded_blackstone"))&&c.chance(PIGLIN_PURSE))add(c,new ItemStack(Items.GOLD_NUGGET,c.count(PIGLIN_PURSE,AMOUNT)));
        if(c.ore&&c.chance(LUCKY_REFUND))MiningRuntime.repair(c.tool,c.count(LUCKY_REFUND,AMOUNT));
        int copies=c.amount(DROP_DUPLICATION);for(int i=0;i<copies;i++)duplicate(c,16);
        if(c.gem&&c.perk(GEM_ECHO)){Item item=c.block.contains("diamond")?Items.DIAMOND:c.block.contains("emerald")?Items.EMERALD:Items.LAPIS_LAZULI;add(c,new ItemStack(item,c.count(GEM_ECHO,AMOUNT)));}
        if(!c.event.getDrops().isEmpty()&&c.perk(LOOT_DISCHARGE))duplicate(c,c.count(LOOT_DISCHARGE,AMOUNT));
        var state=MiningRuntime.read(c.tool);long until=state.getLongOr("TemporalUntil",0);
        if(c.get(TEMPORAL_ECHO)>0&&until>=c.now&&until>0&&!c.event.getDrops().isEmpty()){
            duplicate(c,c.count(TEMPORAL_ECHO,AMOUNT));MiningRuntime.edit(c.tool,t->t.putLong("TemporalUntil",0));
        }else if(c.perk(TEMPORAL_ECHO))MiningRuntime.edit(c.tool,t->t.putLong("TemporalUntil",c.now+c.count(TEMPORAL_ECHO,DURATION)));
        if(c.block.equals("end_stone")&&c.perk(ENDSTONE_ALCHEMY)&&c.player.getRandom().nextDouble()<c.rule(ENDSTONE_ALCHEMY,CHANCE))add(c,new ItemStack(Items.ENDER_PEARL,c.count(ENDSTONE_ALCHEMY,AMOUNT)));
    }
    static void experience(MiningContext c,double furnaceXp){
        double boost=c.get(EXPERIENCE);if(c.event.getPos().getY()<0)boost+=c.get(DEEP_XP);if(c.ore)boost+=c.get(ORE_XP);
        if(MiningRuntime.chain(c.tool,c.now)>0)boost+=c.get(CHAIN_XP);
        double furnaceBoost=1+c.get(PYROCLASTIC)+(c.soulFurnace?c.rule(SOUL_FURNACE,MULTIPLIER)-1:0);
        int xp=c.silk?0:PickaxeEvents.stochastic(c.player.getRandom(),(c.event.getDroppedExperience()+furnaceXp*furnaceBoost)*(1+Math.min(ModifierGameplay.MAX_XP_BONUS,boost)));
        if(c.primary&&!c.silk){
            if(xp>0&&c.chance(XP_BATTERY)){xp--;c.charge(c.count(XP_BATTERY,AMOUNT));}
            if(c.get(XP_CONDENSER)>0&&c.chance(XP_CONDENSER)){
                var state=MiningRuntime.read(c.tool);int bank=Math.clamp(state.getIntOr("XPBank",0),0,1000000),step=state.getIntOr("XPBankStep",0)+1;
                int kept=xp/2;xp-=kept;bank=Math.min(1000000,bank+kept);
                if(step>=c.count(XP_CONDENSER,INTERVAL)){xp+=PickaxeEvents.stochastic(c.player.getRandom(),bank*c.rule(XP_CONDENSER,MULTIPLIER));c.charge(c.count(XP_CONDENSER,AMOUNT));bank=0;step=0;}
                final int b=bank,s=step;MiningRuntime.edit(c.tool,t->{t.putInt("XPBank",b);t.putInt("XPBankStep",s);});
            }
            if(xp>0&&c.chance(XP_SIPHON)){
                xp=PickaxeEvents.stochastic(c.player.getRandom(),xp*c.rule(XP_SIPHON,MULTIPLIER));
                var mending=c.level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING);
                double repair=c.rule(XP_SIPHON,REPAIR)+(net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(mending,c.tool)>0?2:0);
                MiningRuntime.repair(c.tool,PickaxeEvents.stochastic(c.player.getRandom(),xp*repair*(1+c.get(MENDING_BOOST))));c.player.giveExperiencePoints(xp);xp=0;
            }
        }
        c.event.setDroppedExperience(xp);
    }
    private static void convert(MiningContext c,Item from,Item to){for(var e:c.event.getDrops())if(SharedToolEffects.plain(e.getItem(),from))e.setItem(new ItemStack(to,e.getItem().getCount()));}
    static void add(MiningContext c,ItemStack stack){var p=c.event.getPos();c.event.getDrops().add(new ItemEntity(c.level,p.getX()+.5,p.getY()+.5,p.getZ()+.5,stack));}
    private static void duplicate(MiningContext c,int max){if(c.event.getDrops().isEmpty())return;var copy=c.event.getDrops().getFirst().getItem().copy();copy.setCount(Math.min(max,copy.getCount()));if(!copy.isEmpty())add(c,copy);}
    private MiningLootEffects(){}
}
