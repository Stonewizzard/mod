package com.example.examplemod;

import java.util.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import static com.example.examplemod.PickaxeTrait.*;

/** Loot changes operate on the actual event drops; they never reroll containers or secondary bonuses. */
final class MiningLootEffects {
    static double fortune(MiningContext c){
        double value=c.get(FORTUNE);if(c.ore)value+=c.get(ORE_FORTUNE);if(c.gem)value+=c.get(GEM_FORTUNE);
        if(c.block.contains("redstone_ore"))value+=c.get(REDSTONE_FORTUNE);if(c.block.contains("coal_ore"))value+=c.get(COAL_FORTUNE);
        if(c.ore&&(c.block.contains("iron")||c.block.contains("gold")||c.block.contains("copper")))value+=c.get(METAL_FORTUNE);return Math.min(3,value);
    }
    static double silk(MiningContext c){return Math.min(1,c.get(SILK)+(MiningConditions.still(c.player)?c.get(PRECISION_SILK):0));}
    static double smeltChance(MiningContext c){
        double chance=Math.min(1,c.get(SMELTING)+c.get(PYROCLASTIC));
        if(c.primary&&c.player.totalExperience>0&&c.get(SOUL_FURNACE)>0&&c.chance(SOUL_FURNACE)){
            boolean recipe=false;
            for(var e:c.event.getDrops()){
                var s=e.getItem().copy();s.setCount(1);
                if(c.level.getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMELTING,new net.minecraft.world.item.crafting.SingleRecipeInput(s),c.level).isPresent()){recipe=true;break;}
            }
            if(recipe){c.player.giveExperiencePoints(-1);chance=1;}
        }return chance;
    }
    static void bonus(MiningContext c){
        if(!c.primary||c.silk||c.event.getState().hasBlockEntity())return;
        if(c.chance(GRAVEL_SIFTER)&&c.block.equals("gravel"))convert(c,Items.GRAVEL,Items.FLINT);
        if(c.chance(SAND_GLASS)&&(c.block.equals("sand")||c.block.equals("red_sand"))){convert(c,Items.SAND,Items.GLASS);convert(c,Items.RED_SAND,Items.GLASS);}
        if(c.chance(CLAY_FIRING))convert(c,Items.CLAY_BALL,Items.BRICK);
        if(c.chance(STONE_SMOOTHER))convert(c,Items.COBBLESTONE,Items.STONE);
        if(c.chance(DEEPSLATE_POLISHER))convert(c,Items.COBBLED_DEEPSLATE,Items.POLISHED_DEEPSLATE);
        if(c.ore&&c.chance(METAL_NUGGETS)){if(c.block.contains("iron"))add(c,new ItemStack(Items.IRON_NUGGET));else if(c.block.contains("gold"))add(c,new ItemStack(Items.GOLD_NUGGET));}
        if((c.block.equals("calcite")||c.block.equals("tuff"))&&c.chance(FOSSIL_DUST))add(c,new ItemStack(Items.BONE_MEAL));
        if(c.block.equals("cobweb")&&c.chance(SALVAGE_STRING))add(c,new ItemStack(Items.STRING));
        if(c.block.equals("amethyst_cluster")&&c.chance(PRISMATIC_SHARDS))add(c,new ItemStack(Items.AMETHYST_SHARD));
        if(c.block.contains("moss")&&c.chance(MOSS_GARDENER))add(c,new ItemStack(Items.BONE_MEAL));
        if(c.ore&&c.block.contains("gold")&&c.chance(PIGLIN_PURSE))add(c,new ItemStack(Items.GOLD_NUGGET));
        if(c.ore&&c.chance(LUCKY_REFUND))MiningRuntime.repair(c.tool,2);
        if(c.chance(DROP_DUPLICATION))duplicate(c,8);
        if(c.gem&&c.perk(GEM_ECHO,100,0)){Item item=c.block.contains("diamond")?Items.DIAMOND:c.block.contains("emerald")?Items.EMERALD:Items.LAPIS_LAZULI;add(c,new ItemStack(item));}
        if(!c.event.getDrops().isEmpty()&&c.perk(LOOT_DISCHARGE,100,3))duplicate(c,1+c.stage(LOOT_DISCHARGE));
        var state=MiningRuntime.read(c.tool);long until=state.getLongOr("TemporalUntil",0);
        if(c.get(TEMPORAL_ECHO)>0&&until>=c.now&&until>0&&!c.event.getDrops().isEmpty()){
            duplicate(c,1+c.stage(TEMPORAL_ECHO));MiningRuntime.edit(c.tool,s->s.putLong("TemporalUntil",0));
        }else if(c.perk(TEMPORAL_ECHO,100,3))MiningRuntime.edit(c.tool,s->s.putLong("TemporalUntil",c.now+200));
        if(c.block.equals("end_stone")&&c.perk(ENDSTONE_ALCHEMY,200,5)&&c.player.getRandom().nextDouble()<.15+.05*c.stage(ENDSTONE_ALCHEMY))add(c,new ItemStack(Items.ENDER_PEARL));
    }
    static void experience(MiningContext c,double furnaceXp){
        double boost=c.get(EXPERIENCE);if(c.event.getPos().getY()<0)boost+=c.get(DEEP_XP);if(c.ore)boost+=c.get(ORE_XP);
        if(MiningRuntime.chain(c.tool,c.now)>0)boost+=c.get(CHAIN_XP);
        double furnaceBoost=1+c.get(PYROCLASTIC)+(c.get(SOUL_FURNACE)>0?.2*c.stage(SOUL_FURNACE):0);
        int xp=c.silk?0:PickaxeEvents.stochastic(c.player.getRandom(),(c.event.getDroppedExperience()+furnaceXp*furnaceBoost)*(1+Math.min(5,boost)));
        if(c.primary&&!c.silk){
            if(xp>0&&c.chance(XP_BATTERY)){xp--;c.charge(1+c.stage(XP_BATTERY));}
            if(c.get(XP_CONDENSER)>0&&c.chance(XP_CONDENSER)){
                var state=MiningRuntime.read(c.tool);int bank=Math.clamp(state.getIntOr("XPBank",0),0,1000000),step=state.getIntOr("XPBankStep",0)+1;
                int kept=xp/2;xp-=kept;bank=Math.min(1000000,bank+kept);
                if(step>=5-c.stage(XP_CONDENSER)){xp+=bank;bank=0;step=0;}
                final int b=bank,s=step;MiningRuntime.edit(c.tool,t->{t.putInt("XPBank",b);t.putInt("XPBankStep",s);});
            }
            if(xp>0&&c.chance(XP_SIPHON)){c.player.giveExperiencePoints(xp);xp=0;}
        }
        c.event.setDroppedExperience(xp);
    }
    private static void convert(MiningContext c,Item from,Item to){for(var e:c.event.getDrops())if(SharedToolEffects.plain(e.getItem(),from))e.setItem(new ItemStack(to,e.getItem().getCount()));}
    static void add(MiningContext c,ItemStack stack){var p=c.event.getPos();c.event.getDrops().add(new ItemEntity(c.level,p.getX()+.5,p.getY()+.5,p.getZ()+.5,stack));}
    private static void duplicate(MiningContext c,int max){if(c.event.getDrops().isEmpty())return;var copy=c.event.getDrops().getFirst().getItem().copy();copy.setCount(Math.min(max,copy.getCount()));if(!copy.isEmpty())add(c,copy);}
    private MiningLootEffects(){}
}
