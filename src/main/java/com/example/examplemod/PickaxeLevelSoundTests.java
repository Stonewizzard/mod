package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public final class PickaxeLevelSoundTests {
    public static void register(RegisterGameTestsEvent event){
        var env=event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"pickaxe_level_sound_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"pickaxe_level_sound"),new Test(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));
    }
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> data){super(data);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Native mining emits level-up audio only when a tool gains a level");}
        @Override public void run(GameTestHelper h){
            var player=h.makeMockServerPlayerInLevel();player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);var pos=h.absolutePos(new BlockPos(3,1,3));player.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
            var tool=PickaxeGrowthTests.tool(ModifierScaling.S,100,92);player.setItemInHand(InteractionHand.MAIN_HAND,tool);int[] sounds={0};boolean[] listen={true};
            NeoForge.EVENT_BUS.addListener((PlayLevelSoundEvent event)->{if(listen[0]&&event.getLevel()==h.getLevel()&&event.getSound()!=null&&event.getSound().value()==SoundEvents.PLAYER_LEVELUP){if(event.getSource()!=SoundSource.PLAYERS||event.getNewVolume()<=0)throw h.assertionException("Level-up sound must use an audible player sound channel");sounds[0]++;}});
            try{
                mine(h,player,pos);check(h,sounds[0]==0,"Partial XP must not make a level-up sound");
                PickaxeProgression.write(tool,new PickaxeProgression.State(1,19,0));mine(h,player,pos);check(h,sounds[0]==1&&PickaxeProgression.read(tool).level()==2,"A real mining level-up must emit exactly one native level-up sound");
                mine(h,player,pos);check(h,sounds[0]==1,"Further XP at the same level must stay quiet");
                PickaxeProgression.write(tool,new PickaxeProgression.State(9,PickaxeProgression.xpRequired(9)-1,0));mine(h,player,pos);check(h,sounds[0]==2&&PickaxeProgression.read(tool).needsAscension(),"Reaching an ascension gate must sound once");
                mine(h,player,pos);check(h,sounds[0]==2,"Mining at an unpaid gate must stay quiet");
                player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);mine(h,player,pos);check(h,sounds[0]==2,"Creative mining must not emit tool level-up audio");
            }finally{listen[0]=false;}
            System.out.println("PICKAXE_LEVEL_SOUND_CHECK: PASS: real native sound events at ordinary levels and ascension gates, exactly once, with no sound on partial XP, blocked progress or Creative mining");h.succeed();
        }
    }
    private static void mine(GameTestHelper h,net.minecraft.server.level.ServerPlayer player,BlockPos pos){h.getLevel().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());check(h,player.gameMode.destroyBlock(pos),"Native mining action must succeed");}
    private static void check(GameTestHelper h,boolean value,String message){if(!value)throw h.assertionException(message);}
    private PickaxeLevelSoundTests(){}
}
