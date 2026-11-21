package com.example.examplemod.client;

import com.example.examplemod.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

/** Disposable real-world fixtures; screenshots and interaction still use the native game window. */
public final class ForgeRuneVisualQa {
  private static BlockPos core;
  public static BlockPos batchPos;

  public static void command(ServerPlayer player, String action) {
    var level = player.level();
    player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
    player.getAbilities().flying = true;
    player.onUpdateAbilities();
    if (action.equals("rune_gallery")) {
      core = new BlockPos(48, player.blockPosition().getY() + 8, 16);
      for (int x = -19; x <= 19; x++)
        for (int z = -19; z <= 19; z++)
          for (int y = -5; y <= 19; y++)
            level.setBlock(
                core.offset(x, y, z), (y == -5 ? Blocks.STONE : Blocks.AIR).defaultBlockState(), 3);
      for (int x = -7; x <= 7; x++)
        for (int z = -7; z <= 7; z++)
          level.setBlock(core.offset(x, -2, z), Blocks.STONE.defaultBlockState(), 3);
      AnvilTierSixTests.prepare(
          level, core, Direction.NORTH, AnvilTierSix.stainedGlass("light_blue"), true);
      AncientAnvilBlockEntity.refresh(level, core, level.getBlockState(core));
      var kinds = ForgeRune.values();
      for (int i = 0; i < kinds.length; i++) {
        int x = i % 11, z = i / 11;
        var at = core.offset(x - 5, 0, -9 - z * 2);
        level.setBlock(at, ForgeRunes.BLOCKS.get(kinds[i]).get().defaultBlockState(), 3);
      }
      player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
      player.getInventory().clearContent();
      player.connection.teleport(core.getX() + .5, core.getY() + 5, core.getZ() - 16, 0, 26);
    } else if (action.equals("rune_workshop")) {
      var kinds = ForgeRune.values();
      var sockets =
          ForgeRunes.sockets(core, Direction.NORTH, 6).stream()
              .filter(p -> p.getY() == core.getY() - 1)
              .toList();
      for (int i = 0; i < Math.min(kinds.length, sockets.size()); i++)
        level.setBlock(
            sockets.get(i), ForgeRunes.BLOCKS.get(kinds[i]).get().defaultBlockState(), 3);
      AncientAnvilBlockEntity.refresh(level, core, level.getBlockState(core));
      player.connection.teleport(core.getX() - 7, core.getY() + 5, core.getZ() - 8, -42, 28);
    } else if (action.equals("rune_batch_fixture")) {
      batchPos = core.north(5).below();
      level.setBlock(
          batchPos, ForgeRunes.BLOCKS.get(ForgeRune.BATCHING).get().defaultBlockState(), 3);
      AncientAnvilBlockEntity.refresh(level, core, level.getBlockState(core));
      ForgeRunes.members((AncientAnvilBlockEntity) level.getBlockEntity(core));
      player.getInventory().clearContent();
      player.getInventory().setItem(0, new ItemStack(AncientAnvilContent.RUNESTONE_ITEM.get(), 2));
      player.getInventory().setItem(1, new ItemStack(Items.DEEPSLATE, 4));
      player.getInventory().setItem(2, new ItemStack(Items.GOLD_INGOT));
      player.connection.teleport(
          batchPos.getX() + .5, batchPos.getY() + 1.2, batchPos.getZ() + .5, 0, 90);
      player.containerMenu.broadcastChanges();
    } else if (action.equals("rune_detail")) {
      player.connection.teleport(core.getX() + .5, core.getY() + 3, core.getZ() - 12, 0, 35);
    } else if (action.equals("rune_overview")) {
      player.connection.teleport(core.getX() - 22, core.getY() + 12, core.getZ() - 24, -42, 15);
    }
  }
}
