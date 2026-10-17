package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** One item places two horizontal halves; only the left half owns storage. */
public final class ArchaeologyTableBlock extends BaseEntityBlock {
    public enum Part implements StringRepresentable {
        LEFT, RIGHT;
        @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    }
    public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Part> PART=EnumProperty.create("part",Part.class);
    private static final VoxelShape SHAPE=Shapes.or(Block.box(0,14,0,16,16,16),
            Block.box(0,10,1,16,14,3),Block.box(0,10,13,16,14,15),
            Block.box(1,0,1,3,14,3),Block.box(13,0,1,15,14,3),
            Block.box(1,0,13,3,14,15),Block.box(13,0,13,15,14,15),
            Block.box(1,3,3,15,4,13));

    public ArchaeologyTableBlock(BlockBehaviour.Properties properties){
        super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PART,Part.LEFT));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,PART);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){
        var facing=context.getHorizontalDirection().getOpposite();
        var other=context.getClickedPos().relative(facing.getClockWise());
        return context.getLevel().getWorldBorder().isWithinBounds(other)
                && context.getLevel().getBlockState(other).canBeReplaced(context)
                ?defaultBlockState().setValue(FACING,facing):null;
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity placer,ItemStack stack){
        super.setPlacedBy(level,pos,state,placer,stack);
        if(!level.isClientSide())level.setBlock(partnerPos(pos,state),state.setValue(PART,Part.RIGHT),Block.UPDATE_ALL);
    }
    public static BlockPos masterPos(BlockPos pos,BlockState state){
        return state.getValue(PART)==Part.LEFT?pos:pos.relative(state.getValue(FACING).getCounterClockWise());
    }
    public static BlockPos partnerPos(BlockPos pos,BlockState state){
        return pos.relative(state.getValue(PART)==Part.LEFT?state.getValue(FACING).getClockWise():state.getValue(FACING).getCounterClockWise());
    }
    public static ArchaeologyTableBlockEntity inventory(Level level,BlockPos pos){
        var state=level.getBlockState(pos);
        if(!state.is(ExampleMod.ARCHAEOLOGY_TABLE.get()))return null;
        return level.getBlockEntity(masterPos(pos,state)) instanceof ArchaeologyTableBlockEntity table?table:null;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        var table=inventory(level,pos);
        if(table!=null && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)serverPlayer.openMenu(table);
        return InteractionResult.SUCCESS;
    }
    @Override protected void affectNeighborsAfterRemoval(BlockState state,ServerLevel level,BlockPos pos,boolean moving){
        super.affectNeighborsAfterRemoval(state,level,pos,moving);
        if(level.getBlockState(pos).is(this))return;
        var other=partnerPos(pos,state);var partner=level.getBlockState(other);
        if(partner.is(this) && partner.getValue(FACING)==state.getValue(FACING) && partner.getValue(PART)!=state.getValue(PART))
            level.destroyBlock(other,false);
        // The removed half's loot drops one table; the partner never drops a second one.
        // Native BlockEntity.preRemoveSideEffects returns the inventory when the master is removed.
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){
        return state.getValue(PART)==Part.LEFT?new ArchaeologyTableBlockEntity(pos,state):null;
    }
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return SHAPE;}
    @Override protected BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState state,Mirror mirror){return rotate(state,mirror.getRotation(state.getValue(FACING)));}
}
