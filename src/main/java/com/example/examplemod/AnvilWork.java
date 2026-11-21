package com.example.examplemod;

import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/** Physical placement and forging are committed once on the authoritative server. */
public final class AnvilWork {
    public static BlockPos core(Level level,BlockPos clicked){
        if(!level.hasChunkAt(clicked))return null;
        var state=level.getBlockState(clicked);
        if(state.is(AncientAnvilContent.ANVIL.get()))return clicked;
        if(state.is(AncientAnvilContent.PART.get())){var owner=AncientAnvilPartBlock.core(clicked,state);return level.hasChunkAt(owner)&&level.getBlockState(owner).is(AncientAnvilContent.ANVIL.get())?owner:null;}
        return null;
    }
    public static InteractionResult interact(Level level,BlockPos clicked,Player player,InteractionHand hand,BlockHitResult hit){
        var core=core(level,clicked);if(core==null)return InteractionResult.PASS;
        if(level.isClientSide())return InteractionResult.SUCCESS;
        if(!(level.getBlockEntity(core) instanceof AncientAnvilBlockEntity be)||player.isSpectator())return InteractionResult.SUCCESS;
        var state=level.getBlockState(core);var slot=AnvilSlot.nearest(core,state.getValue(AncientAnvilBlock.FACING),hit.getLocation());
        var held=player.getItemInHand(hand);
        if(player.isShiftKeyDown())return InteractionResult.SUCCESS; // Reserved for the client foundation guide.
        if(!state.getValue(AncientAnvilBlock.EXPANDED)){message(player,"Clear the 4 x 4 working space to assemble the anvil");return InteractionResult.SUCCESS;}
        if(held.isEmpty()&&hand==InteractionHand.MAIN_HAND&&hit.getLocation().y<core.getY()+1.5){
            if(level.getGameTime()-be.lastExperienceTransfer>=8){
                be.lastExperienceTransfer=level.getGameTime();int deposited=be.depositExperience(player,10);
                message(player,"Stored XP: "+be.storedExperience()+" / "+be.experienceCapacity()+(deposited>0?" | +"+deposited+" XP":" | Right-click the lower body to deposit up to 10 XP"));
            }
            return InteractionResult.SUCCESS;
        }
        if(slot==AnvilSlot.CENTER&&!be.output().isEmpty()){give(player,be.takeOutput());return InteractionResult.SUCCESS;}
        if(held.getItem() instanceof ForgeHammerItem){strike(be,player,hand);return InteractionResult.SUCCESS;}
        if(slot==null)return InteractionResult.SUCCESS;
        var old=be.input(slot);
        if(held.isEmpty()){if(!old.isEmpty())give(player,be.takeInput(slot));return InteractionResult.SUCCESS;}
        if(!old.isEmpty()&&ItemStack.isSameItemSameComponents(old,held))return InteractionResult.SUCCESS;
        var placed=held.copyWithCount(1);if(!player.getAbilities().instabuild)held.shrink(1);
        var previous=be.takeInput(slot);be.placeInput(slot,placed);if(!previous.isEmpty())give(player,previous);
        player.containerMenu.broadcastChanges();return InteractionResult.SUCCESS;
    }
    private static void give(Player player,ItemStack stack){if(!stack.isEmpty()&&!player.getInventory().add(stack))net.minecraft.world.Containers.dropItemStack(player.level(),player.getX(),player.getY()+.5,player.getZ(),stack);player.containerMenu.broadcastChanges();}
    public static boolean strike(AncientAnvilBlockEntity be,Player player,InteractionHand hand){
        if(!(be.getLevel() instanceof ServerLevel level)||player.isSpectator()||!(player.getItemInHand(hand).getItem() instanceof ForgeHammerItem hammer)||!be.getBlockState().getValue(AncientAnvilBlock.EXPANDED))return false;
        if(!be.output().isEmpty()){message(player,"Take the finished item from the center");return false;}
        AncientAnvilBlockEntity.refresh(level,be.getBlockPos(),be.getBlockState());
        var recipe=AnvilRecipes.match(be);
        if(recipe==null){message(player,"These items do not match an anvil recipe");return false;}
        if(be.getBlockState().getValue(AncientAnvilBlock.TIER)<recipe.anvilTier()){message(player,"This recipe needs a higher anvil tier");return false;}
        if(hammer.forgeTier()<recipe.hammerTier()){message(player,"This recipe needs a higher forge hammer tier");return false;}
        if(level.getGameTime()-be.lastStrike<8)return false;
        var counts=ForgeRunes.counts(be);
        int swift=counts.getOrDefault(ForgeRune.SWIFT_FORGING,0),deliberation=counts.getOrDefault(ForgeRune.DELIBERATION,0);
        int required=ForgeRuneMath.swings(recipe.work(),swift,deliberation);
        double price=ForgeRuneMath.hitPrice(recipe.xpPerHit(),recipe.work(),required,swift,counts.getOrDefault(ForgeRune.EFFICIENCY,0),deliberation);
        for(var rune:ForgeRunes.members(be))if(rune.kind()==ForgeRune.SPECIALIZATION&&!rune.category.isEmpty())price*=rune.category.equals(recipe.category())?1/1.01:1.01;
        price*=ForgeRuneMath.sequenceDiscount(counts.getOrDefault(ForgeRune.FAMILIARITY,0),be.lastCompleted.equals(recipe.id().toString())?be.familiarStreak:0);
        price*=ForgeRuneMath.sequenceDiscount(counts.getOrDefault(ForgeRune.ALTERNATION,0),!be.lastCompleted.equals(recipe.id().toString())?be.alternateStreak:0);
        String signature=recipe.toString()+counts+ForgeRunes.members(be).stream().map(r->r.category+":"+r.getItem(0)).toList();
        if(!be.planSignature.equals(signature)){be.work=0;be.workRecipe="";be.planSignature=signature;}
        int xp=(int)Math.min(Integer.MAX_VALUE,Math.max(0,Math.ceil(price-be.xpCredit-1e-9)));
        if(!be.payExperience(player,xp)){message(player,"Need "+xp+" XP point"+(xp==1?"":"s")+" to strike ("+be.storedExperience()+" stored, "+AnvilExperience.available(player)+" carried)");return false;}
        be.lastStrike=level.getGameTime();
        if(!player.getAbilities().instabuild)be.xpCredit=Math.max(0,be.xpCredit+xp-price);
        if(!be.workRecipe.equals(recipe.id().toString())){be.workRecipe=recipe.id().toString();be.work=0;}
        be.work=Math.min(required,be.work+hammer.workPerStrike());
        if(be.getBlockState().getValue(AncientAnvilBlock.TIER)==6){
            var lens=AnvilTierSix.inspect(level,be.getBlockPos(),be.getBlockState().getValue(AncientAnvilBlock.FACING),AnvilStages.all());
            if(lens.complete()){
                be.energyColor=lens.color();be.energyStarted=level.getGameTime();be.energyUntil=level.getGameTime()+(be.work>=required?36:24);
                var lensCenter=be.getBlockPos().above(16);level.sendParticles(new DustParticleOptions(lens.color(),1.35F),lensCenter.getX()+.5,lensCenter.getY()+.25,lensCenter.getZ()+.5,12,.7,.12,.7,.03);
                level.playSound(null,be.getBlockPos(),SoundEvents.BEACON_POWER_SELECT,SoundSource.BLOCKS,.65F,1.25F);
            }
        }
        var center=AnvilSlot.CENTER.world(be.getBlockPos(),be.getBlockState().getValue(AncientAnvilBlock.FACING));
        level.playSound(null,be.getBlockPos(),SoundEvents.ANVIL_USE,SoundSource.BLOCKS,.55F,.95F+level.getRandom().nextFloat()*.15F);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.DEEPSLATE.defaultBlockState()),center.x,center.y+.1,center.z,8,.15,.05,.15,.02);
        if(!player.getAbilities().instabuild){be.wearCredit+=ForgeRuneMath.wear(counts.getOrDefault(ForgeRune.TEMPERING,0));int wear=(int)Math.floor(be.wearCredit+1e-9);be.wearCredit-=wear;if(wear>0)player.getItemInHand(hand).hurtAndBreak(wear,level,player instanceof ServerPlayer server?server:null,unused->{});}
        if(be.work>=required){
            be.finish(recipe);level.playSound(null,be.getBlockPos(),SoundEvents.ANVIL_LAND,SoundSource.BLOCKS,.65F,1.2F);
            level.sendParticles(ParticleTypes.ENCHANT,center.x,center.y+.2,center.z,14,.2,.1,.2,.05);message(player,"Craft complete - take the result from the center");
        }else{be.sync();message(player,"Forging: "+be.work+" / "+required+(!player.getAbilities().instabuild&&xp>0?" | -"+xp+" XP":""));}
        player.containerMenu.broadcastChanges();return true;
    }
    public static void message(Player player,String text){if(player instanceof ServerPlayer server)server.connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(Component.literal(text)));}
    private AnvilWork(){}
}
