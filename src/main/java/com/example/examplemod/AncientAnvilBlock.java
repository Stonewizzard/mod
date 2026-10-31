package com.example.examplemod;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

/** A stationary ritual anvil; its foundation is inspected in place and is never consumed. */
public final class AncientAnvilBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LARGE=BooleanProperty.create("large");
    public static final IntegerProperty TIER=IntegerProperty.create("tier",0,1);
    public AncientAnvilBlock(BlockBehaviour.Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(TIER,0).setValue(LARGE,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,TIER,LARGE);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){var facing=c.getHorizontalDirection().getOpposite();return AncientAnvilStructure.room(c.getLevel(),c.getClickedPos(),facing,true)?defaultBlockState().setValue(FACING,facing):null;}
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,net.minecraft.world.entity.LivingEntity placer,net.minecraft.world.item.ItemStack stack){super.setPlacedBy(level,pos,state,placer,stack);if(level instanceof ServerLevel server)AncientAnvilBlockEntity.refresh(server,pos,state);}
    @Override protected void affectNeighborsAfterRemoval(BlockState state,ServerLevel level,BlockPos pos,boolean moving){super.affectNeighborsAfterRemoval(state,level,pos,moving);if(!level.getBlockState(pos).is(this))AncientAnvilStructure.clear(level,pos,state.getValue(FACING));}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new AncientAnvilBlockEntity(p,s);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter level,BlockPos p,CollisionContext c){return s.getValue(LARGE)?AncientAnvilGeometry.cell(0,s.getValue(FACING)):AncientAnvilGeometry.shape(s.getValue(FACING));}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos p,Player player,BlockHitResult hit){if(level instanceof ServerLevel server)AncientAnvilBlockEntity.refresh(server,p,s);return InteractionResult.SUCCESS;}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return l instanceof ServerLevel?createTickerHelper(type,AncientAnvilContent.ENTITY.get(),AncientAnvilBlockEntity::tick):null;}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
}
