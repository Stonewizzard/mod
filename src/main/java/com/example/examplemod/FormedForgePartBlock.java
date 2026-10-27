package com.example.examplemod;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** Each baked cell contributes a correctly textured, shaped section of the connected forge. */
public final class FormedForgePartBlock extends BaseEntityBlock {
    public static final IntegerProperty INDEX=IntegerProperty.create("part",0,17);
    public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
    public FormedForgePartBlock(BlockBehaviour.Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(INDEX,0).setValue(FACING,Direction.NORTH));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(INDEX,FACING);}
    public static BlockPos core(BlockPos pos,BlockState state){var part=RuinedForgeStructure.PARTS.get(state.getValue(INDEX));var front=state.getValue(FACING);return pos.relative(front.getClockWise(),-part.x()).relative(front.getOpposite(),-part.z()).above(-part.y());}
    public static boolean belongs(Level level,BlockPos at,BlockPos core,Direction facing,int index){var s=level.getBlockState(at);return s.is(AncientForgeContent.PART.get())&&s.getValue(INDEX)==index&&s.getValue(FACING)==facing&&core(at,s).equals(core)&&level.getBlockEntity(at) instanceof ForgePartBlockEntity;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new ForgePartBlockEntity(pos,state);}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return ForgeGeometry.shape(state.getValue(INDEX),state.getValue(FACING));}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){var core=core(pos,state);var hearth=level.getBlockState(core);return hearth.is(AncientForgeContent.CORE.get())?AncientForgeContent.CORE.get().useWithoutItem(hearth,level,core,player,hit):InteractionResult.PASS;}
    @Override protected void affectNeighborsAfterRemoval(BlockState state,ServerLevel level,BlockPos pos,boolean moving){super.affectNeighborsAfterRemoval(state,level,pos,moving);if(!level.getBlockState(pos).is(this))ForgeAssembly.dismantle(level,core(pos,state),state.getValue(FACING));}
    @Override protected List<ItemStack> getDrops(BlockState state,LootParams.Builder builder){
        if(builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ForgePartBlockEntity part)return part.original().getDrops(builder);
        return List.of(new ItemStack(AncientForgeContent.BRICKS_ITEM.get()));
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level instanceof ServerLevel?createTickerHelper(type,AncientForgeContent.PART_ENTITY.get(),ForgePartBlockEntity::tick):null;}
    @Override protected BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState state,Mirror mirror){return rotate(state,mirror.getRotation(state.getValue(FACING)));}
}
