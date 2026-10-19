package com.example.examplemod;

import net.minecraft.core.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import static com.example.examplemod.PickaxeTrait.*;

/** Inventory-backed utility placements use snapshots and the same cancellable native placement event. */
final class MiningPlacementEffects {
    static void apply(MiningContext c){
        if(!c.primary)return;var pos=c.event.getPos();boolean dark=c.level.getMaxLocalRawBrightness(pos)<8;
        if(dark&&c.level.getBlockState(pos.below()).isSolidRender()&&c.perk(EMBERLIGHT,cooldown(c,EMBERLIGHT),2))place(c,pos,Blocks.SOUL_TORCH,null,false);
        if(dark&&SharedToolEffects.owns(c.player,Items.TORCH)&&c.level.getBlockState(pos.below()).isSolidRender()&&c.perk(LIGHTBEARER,cooldown(c,LIGHTBEARER),0))place(c,pos,Blocks.TORCH,Items.TORCH,false);
        if(SharedToolEffects.owns(c.player,Items.COBBLESTONE)){
            if(c.level.getBlockState(pos.below()).isAir()&&c.perk(TIMBER_SUPPORT,cooldown(c,TIMBER_SUPPORT),0))place(c,pos.below(),Blocks.COBBLESTONE,Items.COBBLESTONE,false);
            if(c.player.isShiftKeyDown()&&c.perk(BRIDGEWORK,cooldown(c,BRIDGEWORK),0))place(c,c.player.blockPosition().below(),Blocks.COBBLESTONE,Items.COBBLESTONE,false);
            cork(c,LAVA_CORK,FluidTags.LAVA);cork(c,WATER_CORK,FluidTags.WATER);
        }
        if(SharedToolEffects.owns(c.player,Items.BONE_MEAL)&&c.get(ECOLOGIST)>0){
            for(var d:Direction.values()){var p=pos.relative(d);if(c.level.getBlockState(p).is(Blocks.DIRT)&&c.level.getBlockState(p.above()).isAir()&&c.perk(ECOLOGIST,cooldown(c,ECOLOGIST),0)){place(c,p,Blocks.GRASS_BLOCK,Items.BONE_MEAL,true);break;}}
        }
    }
    static int cooldown(MiningContext c,PickaxeTrait trait){return (8-2*c.stage(trait))*20;}
    private static void cork(MiningContext c,PickaxeTrait trait,net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> tag){
        if(c.get(trait)<=0)return;for(var d:Direction.values()){var p=c.event.getPos().relative(d);if(c.level.hasChunkAt(p)&&c.level.getFluidState(p).is(tag)&&c.perk(trait,cooldown(c,trait),0)){place(c,p,Blocks.COBBLESTONE,Items.COBBLESTONE,false);break;}}
    }
    static boolean place(MiningContext c,BlockPos pos,Block block,Item cost,boolean dirt){
        if(!c.level.hasChunkAt(pos)||!c.level.getWorldBorder().isWithinBounds(pos)||!c.level.mayInteract(c.player,pos)||c.level.getBlockState(pos).hasBlockEntity()||cost!=null&&!SharedToolEffects.owns(c.player,cost))return false;
        var old=c.level.getBlockState(pos);if(dirt?!old.is(Blocks.DIRT):!old.isAir()&&!old.liquid())return false;
        var state=block.defaultBlockState();if(!state.canSurvive(c.level,pos))return false;
        // Never place a solid block inside an entity, including the miner.
        if(!state.getCollisionShape(c.level,pos).isEmpty()&&!c.level.getEntities(null,new net.minecraft.world.phys.AABB(pos)).isEmpty())return false;
        var snapshot=BlockSnapshot.create(c.level.dimension(),c.level,pos);
        if(!c.level.setBlock(pos,state,3))return false;
        if(EventHooks.onBlockPlace(c.player,snapshot,Direction.UP)){snapshot.restore(3);return false;}
        if(cost!=null&&!c.chance(RECLAMATION))SharedToolEffects.consume(c.player,cost,1);
        return true;
    }
    private MiningPlacementEffects(){}
}
