package com.example.examplemod;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;

/** A reserved in-world batch: ingredients are consumed only when the timed process finishes. */
public final class ForgeProcess {
    public static final int DURATION=20*20,BLACK=0x080808,WHITE=0xffffff;
    private final ReconstructionTableBlockEntity forge;
    private int phase,remaining;
    public ForgeProcess(ReconstructionTableBlockEntity forge){this.forge=forge;}
    public boolean heating(){return phase==1;}
    public boolean finished(){return phase==2&&!forge.getItem(5).isEmpty();}
    public int remaining(){return remaining;}
    private boolean complete(Level level){var state=forge.getBlockState();return !forge.legacyForge()&&ForgeAssembly.formed(state)&&RuinedForgeStructure.inspect(level,forge.getBlockPos(),state.getValue(RuinedAncientForgeBlock.FACING)).complete();}
    public boolean ready(){return !heating()&&!finished()&&forge.getItem(5).isEmpty()&&AncientAlloy.ready(forge);}
    /** Called only on the server, also used by native gameplay regressions. */
    public boolean start(ServerLevel level){
        if(forge.getLevel()!=level||!complete(level)||!ready())return false;
        phase=1;remaining=DURATION;forge.setChanged();return true;
    }
    public static InteractionResult frameInteraction(Level level,BlockPos core,Player player){
        if(player instanceof ServerPlayer)message(player,"Click inside the fireplace");
        return InteractionResult.SUCCESS;
    }
    public static InteractionResult interact(Level level,BlockPos core,Player player,InteractionHand hand){
        var held=player.getItemInHand(hand);
        boolean relevant=held.isEmpty()||held.is(Items.FLINT_AND_STEEL)||AncientAlloy.accepts(0,held)||AncientAlloy.accepts(1,held)||AncientAlloy.accepts(2,held);
        if(!relevant)return InteractionResult.PASS;
        if(!(level instanceof ServerLevel server))return InteractionResult.SUCCESS;
        if(!server.mayInteract(player,core)||player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(core))>64||!(level.getBlockEntity(core) instanceof ReconstructionTableBlockEntity forge))return InteractionResult.FAIL;
        if(forge.legacyForge()){
            var front=forge.getBlockState().getValue(RuinedAncientForgeBlock.FACING);
            if(!ForgeAssembly.migrate(server,core,forge)){message(player,"Clear the fireplace two blocks behind this old forge");return InteractionResult.SUCCESS;}
            core=core.relative(front.getOpposite(),2);forge=(ReconstructionTableBlockEntity)level.getBlockEntity(core);
        }
        var process=forge.process;
        if(!ForgeAssembly.form(server,core)){message(player,"Repair the forge; empty-hand click toggles its ghost layout");return InteractionResult.SUCCESS;}
        if(process.heating()){message(player,"Alloying · "+((process.remaining+19)/20)+"s remaining");return InteractionResult.SUCCESS;}
        if(held.isEmpty()){
            // The output is always first; then legacy storage and staged ingredients, one stack per click.
            for(int slot:new int[]{5,4,3,2,1,0})if(!forge.getItem(slot).isEmpty()){
                var taken=forge.removeItemNoUpdate(slot);if(!player.addItem(taken)&&!taken.isEmpty())server.addFreshEntity(new ItemEntity(server,player.getX(),player.getY()+.5,player.getZ(),taken));
                if(slot==5)process.phase=0;forge.setChanged();return InteractionResult.SUCCESS;
            }
            return InteractionResult.SUCCESS;
        }
        if(held.is(Items.FLINT_AND_STEEL)){
            if(process.start(server)){
                if(!player.getAbilities().instabuild)held.hurtAndBreak(1,server,player,unused->{});
                server.playSound(null,core,SoundEvents.FLINTANDSTEEL_USE,SoundSource.BLOCKS,1,.85F);
            }else message(player,"Add 1 Ancient Scrap or debris, 1 gold and 2 iron; or one artifact alone");
            return InteractionResult.SUCCESS;
        }
        if(!forge.getItem(5).isEmpty()){message(player,"Collect the finished batch first");return InteractionResult.SUCCESS;}
        int target=-1;for(int i=0;i<3;i++)if(AncientAlloy.accepts(i,held)){target=i;break;}
        if(target<0)return InteractionResult.PASS;
        boolean artifact=AncientAlloy.artifact(held);
        if(artifact&&(!forge.getItem(1).isEmpty()||!forge.getItem(2).isEmpty())||target>0&&AncientAlloy.melting(forge)){message(player,"Artifacts melt on their own; take the other ingredients out first");return InteractionResult.SUCCESS;}
        var existing=forge.getItem(target);int cap=target==0?1:AncientAlloy.count(target);
        if(!existing.isEmpty()&&(!ItemStack.isSameItemSameComponents(existing,held)||existing.getCount()>=cap)){message(player,"That ingredient is already loaded");return InteractionResult.SUCCESS;}
        if(existing.isEmpty())forge.setItem(target,held.copyWithCount(1));else existing.grow(1);
        if(!player.getAbilities().instabuild)held.shrink(1);forge.setChanged();
        if(process.ready())message(player,"Ready to light");
        return InteractionResult.SUCCESS;
    }
    public void tick(ServerLevel level){
        if(phase==2&&forge.getItem(5).isEmpty()){phase=0;forge.setChanged();}
        if(!complete(level))return; // Missing masonry pauses the saved batch without losing materials.
        if(heating()){
            if(--remaining<=0){
                if(forge.getItem(5).isEmpty()&&AncientAlloy.ready(forge)){
                    boolean melt=AncientAlloy.melting(forge);var result=new ItemStack(AncientAlloy.result(forge),AncientAlloy.outputCount(forge));
                    if(melt)forge.removeItem(0,1);else for(int i=0;i<3;i++)forge.removeItem(i,AncientAlloy.count(i));
                    forge.setItem(5,result);phase=2;
                    level.playSound(null,forge.getBlockPos(),SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,.45F,1.3F);
                }else phase=0;
                remaining=0;forge.setChanged();
            }else if(remaining%20==0)forge.setChanged();
        }
        if((heating()||finished())&&level.getGameTime()%5==0){
            var p=forge.getBlockPos();
            level.sendParticles(new DustParticleOptions(heating()?BLACK:WHITE,2.2F),p.getX()+.5,p.getY()+3.04,p.getZ()+.56,5,.065,.06,.065,.06);
            if(heating()){
                level.sendParticles(ParticleTypes.LARGE_SMOKE,p.getX()+.5,p.getY()+3.04,p.getZ()+.56,1,.04,.03,.04,.025);
                var front=forge.getBlockState().getValue(RuinedAncientForgeBlock.FACING);
                level.sendParticles(ParticleTypes.FLAME,p.getX()+.5+front.getStepX()*.18,p.getY()+.34,p.getZ()+.5+front.getStepZ()*.18,2,.12,.02,.12,.005);
            }
        }
    }
    public void save(ValueOutput out){out.putInt("ForgePhase",phase);out.putInt("ForgeRemaining",remaining);}
    public void load(ValueInput in){phase=in.read("ForgePhase",com.mojang.serialization.Codec.INT).filter(v->v>=0&&v<=2).orElse(0);remaining=Math.clamp(in.read("ForgeRemaining",com.mojang.serialization.Codec.INT).orElse(0),0,DURATION);if(phase==1&&remaining==0)phase=0;}
    private static void message(Player player,String text){player.displayClientMessage(Component.literal(text),true);}
}
