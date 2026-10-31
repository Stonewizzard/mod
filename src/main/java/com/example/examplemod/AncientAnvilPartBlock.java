package com.example.examplemod;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** Non-item parts belong to one core; breaking any section recovers exactly one anvil. */
public final class AncientAnvilPartBlock extends Block {
    public static final IntegerProperty CELL=IntegerProperty.create("cell",1,5);
    public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
    public AncientAnvilPartBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(CELL,1).setValue(FACING,Direction.NORTH));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(CELL,FACING);}
    public static BlockPos core(BlockPos at,BlockState s){var c=AncientAnvilStructure.CELLS.get(s.getValue(CELL));return at.relative(s.getValue(FACING),c.z()).below(c.y());}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return AncientAnvilGeometry.cell(s.getValue(CELL),s.getValue(FACING));}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){var core=core(p,s);var state=l.getBlockState(core);return state.is(AncientAnvilContent.ANVIL.get())?AncientAnvilContent.ANVIL.get().useWithoutItem(state,l,core,player,hit):InteractionResult.PASS;}
    @Override protected void affectNeighborsAfterRemoval(BlockState s,ServerLevel l,BlockPos p,boolean moving){super.affectNeighborsAfterRemoval(s,l,p,moving);if(!l.getBlockState(p).is(this))AncientAnvilStructure.brokenPart(l,core(p,s),s.getValue(FACING));}
    // Player harvesting requests loot AFTER removal; the core has already been cleared then.
    @Override protected List<ItemStack> getDrops(BlockState s,LootParams.Builder b){return List.of(new ItemStack(AncientAnvilContent.ANVIL_ITEM.get()));}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
}
