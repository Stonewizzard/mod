package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** A formed cell retains the exact masonry it replaces, including pack-provided block states. */
public final class ForgePartBlockEntity extends BlockEntity {
    private BlockState original;
    public ForgePartBlockEntity(BlockPos pos,BlockState state){super(AncientForgeContent.PART_ENTITY.get(),pos,state);original=AncientForgeContent.BRICKS.get().defaultBlockState();}
    public BlockState original(){return original;}
    public void original(BlockState value){original=value.getBlock() instanceof net.minecraft.world.level.block.EntityBlock?AncientForgeContent.BRICKS.get().defaultBlockState():value;setChanged();}
    @Override protected void saveAdditional(ValueOutput output){super.saveAdditional(output);output.store("OriginalMasonry",BlockState.CODEC,original);}
    @Override protected void loadAdditional(ValueInput input){super.loadAdditional(input);var restored=input.read("OriginalMasonry",BlockState.CODEC).orElse(AncientForgeContent.BRICKS.get().defaultBlockState());original=restored.getBlock() instanceof net.minecraft.world.level.block.EntityBlock?AncientForgeContent.BRICKS.get().defaultBlockState():restored;}
    public static void tick(net.minecraft.world.level.Level level,BlockPos pos,BlockState state,ForgePartBlockEntity part){
        if(level.getGameTime()%20!=0||!(level instanceof net.minecraft.server.level.ServerLevel server))return;
        var core=FormedForgePartBlock.core(pos,state);
        if(level.hasChunkAt(core)&&!level.getBlockState(core).is(AncientForgeContent.CORE.get()))ForgeAssembly.dismantle(server,core,state.getValue(FormedForgePartBlock.FACING));
    }
}
