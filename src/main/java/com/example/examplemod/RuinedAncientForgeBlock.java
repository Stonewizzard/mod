package com.example.examplemod;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class RuinedAncientForgeBlock extends BaseEntityBlock {
    public static final BooleanProperty FORMED=BooleanProperty.create("formed");
    public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
    public RuinedAncientForgeBlock(BlockBehaviour.Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(FORMED,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,FORMED);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new ReconstructionTableBlockEntity(pos,state);}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
        return ForgeGeometry.shape(state.getValue(FORMED)?18:19,state.getValue(FACING));
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(player instanceof ServerPlayer server){
            var status=RuinedForgeStructure.inspect(level,pos,state.getValue(FACING));
            if(!status.complete())server.connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(status.message()));
            else if(level.getBlockEntity(pos) instanceof ReconstructionTableBlockEntity forge&&ForgeAssembly.form(server.level(),pos))server.openMenu(forge);
        }
        return InteractionResult.SUCCESS;
    }
    @Override public <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level,BlockState state,net.minecraft.world.level.block.entity.BlockEntityType<T> type){return level instanceof net.minecraft.server.level.ServerLevel?createTickerHelper(type,ReconstructionContent.ENTITY.get(),ForgeAssembly::tick):null;}
    @Override protected void affectNeighborsAfterRemoval(BlockState state,net.minecraft.server.level.ServerLevel level,BlockPos pos,boolean moving){super.affectNeighborsAfterRemoval(state,level,pos,moving);if(!level.getBlockState(pos).is(this))ForgeAssembly.dismantle(level,pos,state.getValue(FACING));}
    @Override protected BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState state,Mirror mirror){return rotate(state,mirror.getRotation(state.getValue(FACING)));}
}
