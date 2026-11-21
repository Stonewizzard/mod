package com.example.examplemod.client;
import com.example.examplemod.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.*;
/** Disposable real-world fixtures; screenshots and interaction still use the native game window. */
public final class ForgeRuneVisualQa {
    private static BlockPos core;
    public static void command(ServerPlayer player,String action){
        var level=player.level();
        if(action.equals("rune_gallery")){
            core=new BlockPos(48,player.blockPosition().getY()+8,16);
            AnvilTierSixTests.prepare(level,core,Direction.NORTH,Blocks.LIGHT_BLUE_STAINED_GLASS,true);
            AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));
            var kinds=ForgeRune.values();for(int i=0;i<kinds.length;i++){
                int x=i%11,z=i/11;var at=core.offset(x-5,0,-9-z*2);level.setBlock(at,ForgeRunes.BLOCKS.get(kinds[i]).get().defaultBlockState(),3);
            }
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.getInventory().clearContent();player.connection.teleport(core.getX()+.5,core.getY()+5,core.getZ()-16,0,26);
        }else if(action.equals("rune_workshop")){
            var kinds=ForgeRune.values();var sockets=ForgeRunes.sockets(core,Direction.NORTH,6).stream().filter(p->p.getY()==core.getY()-1).toList();
            for(int i=0;i<Math.min(kinds.length,sockets.size());i++)level.setBlock(sockets.get(i),ForgeRunes.BLOCKS.get(kinds[i]).get().defaultBlockState(),3);
            AncientAnvilBlockEntity.refresh(level,core,level.getBlockState(core));
            player.connection.teleport(core.getX()-7,core.getY()+5,core.getZ()-8,-42,28);
        }else if(action.equals("rune_detail")){
            player.connection.teleport(core.getX()+.5,core.getY()+3,core.getZ()-12,0,35);
        }else if(action.equals("rune_overview")){
            player.connection.teleport(core.getX()-22,core.getY()+12,core.getZ()-24,-42,15);
        }
    }
}
