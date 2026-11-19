package com.example.examplemod;

import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.*;

/** One inventory-backed placement per request, confined to the looked-at anvil’s next tier. */
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
        AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));
        var plan=AnvilGuide.next(level,core);var missing=plan.missing(level);
        if(missing.isEmpty())return false;
        boolean needsBlocks=false,hasMaterials=false;
        for(var goal:missing){
            if(goal.kind()==AnvilGuide.Kind.FLUID||goal.kind()==AnvilGuide.Kind.CRYSTAL)continue; // Fluids are always filled manually.
            needsBlocks=true;
            ItemStack material=ItemStack.EMPTY;
            for(int i=0;i<player.getInventory().getContainerSize();i++){
                var stack=player.getInventory().getItem(i);
                if(!stack.isEmpty()&&stack.getItem() instanceof BlockItem item&&goal.acceptsBlock(item.getBlock())){material=stack;break;}
            }
            if(goal.kind()==AnvilGuide.Kind.GLASS){var held=player.getMainHandItem();if(held.getItem() instanceof BlockItem item&&goal.acceptsBlock(item.getBlock()))material=held;}
            if(material.isEmpty())continue;
            hasMaterials=true;var at=goal.at();
            if(!level.hasChunkAt(at)||!level.getWorldBorder().isWithinBounds(at)||!level.mayInteract(player,at)||level.getBlockEntity(at)!=null||!level.getBlockState(at).canBeReplaced())continue;
            var target=new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false);
            // Native item use preserves placement cancellation/rollback, collisions, sound and exact item cost.
            material.useOn(new UseOnContext(level,player,InteractionHand.MAIN_HAND,material,target));
            player.getInventory().setChanged();player.containerMenu.broadcastChanges();
            boolean placed=goal.matches(level);
            if(placed){
                // Native BlockItem placement notifies everyone except its initiating player,
                // whose client normally predicts the sound. Inventory construction has no prediction.
                var sound=level.getBlockState(at).getSoundType(level,at,player);
                player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound.getPlaceSound()),SoundSource.BLOCKS,at.getX()+.5,at.getY()+.5,at.getZ()+.5,(sound.getVolume()+1)/2,sound.getPitch()*.8F,level.getRandom().nextLong()));
                AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));
                int left=plan.missing(level).size();
                AnvilWork.message(player,left==0?"Tier "+plan.tier()+" structure complete":"Tier "+plan.tier()+": "+(plan.targets().size()-left)+" / "+plan.targets().size());
            }
            return placed; // Never place more than one block, including when a mod cancels a placement.
        }
        AnvilWork.message(player,!needsBlocks?(plan.tier()==6?"Place End Crystals on the highlighted obsidian caps manually":"Fill the highlighted fluid pools manually"):!hasMaterials?"Add the required Runestone, caps or keystones to your inventory":"Remove the old highlighted caps or mismatched glass before building; containers are preserved");return false;
    }
    private AnvilFoundation(){}
}
