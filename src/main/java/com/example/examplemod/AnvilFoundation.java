package com.example.examplemod;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.*;

/** One inventory-backed placement per request, confined to the looked-at anvil's foundation. */
public final class AnvilFoundation {
    public static final int INTERVAL=6;
    public static boolean placeNext(ServerPlayer player,BlockPos core){
        var level=player.level();
        if(!player.isAlive()||player.isSpectator()||!player.isShiftKeyDown()||!player.getAbilities().mayBuild||!level.hasChunkAt(core)||!level.mayInteract(player,core))return false;
        var sight=player.pick(player.blockInteractionRange(),1,false);
        if(!(sight instanceof BlockHitResult hit)||!core.equals(AnvilWork.core(level,hit.getBlockPos())))return false;
        if(!(level.getBlockEntity(core) instanceof AncientAnvilBlockEntity be))return false;
        if(level.getGameTime()-be.lastFoundationPlacement<INTERVAL)return false;
        be.lastFoundationPlacement=level.getGameTime();
        var missing=AncientAnvilBlockEntity.missing(level,core);if(missing.isEmpty())return false;
        ItemStack material=ItemStack.EMPTY;
        for(int i=0;i<player.getInventory().getContainerSize();i++){
            var stack=player.getInventory().getItem(i);
            if(!stack.isEmpty()&&stack.getItem() instanceof BlockItem item&&item.getBlock().defaultBlockState().is(AncientAnvilContent.FOUNDATION)){material=stack;break;}
        }
        if(material.isEmpty()){AnvilWork.message(player,"No Runestone in your inventory");return false;}
        for(var at:missing){
            if(!level.hasChunkAt(at)||!level.getWorldBorder().isWithinBounds(at)||!level.mayInteract(player,at)||level.getBlockEntity(at)!=null||!level.getBlockState(at).canBeReplaced())continue;
            var target=new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false);
            // Native item use preserves placement cancellation/rollback, collisions, sound and exact item cost.
            material.useOn(new UseOnContext(level,player,InteractionHand.MAIN_HAND,material,target));
            player.getInventory().setChanged();player.containerMenu.broadcastChanges();
            boolean placed=level.getBlockState(at).is(AncientAnvilContent.FOUNDATION);
            if(placed){AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));int left=AncientAnvilBlockEntity.missing(level,core).size();AnvilWork.message(player,left==0?"Runestone foundation complete":"Runestone foundation: "+(25-left)+" / 25");}
            return placed; // Never place more than one block, including when a mod cancels a placement.
        }
        AnvilWork.message(player,"Clear the highlighted solid blocks to finish the foundation");return false;
    }
    private AnvilFoundation(){}
}
