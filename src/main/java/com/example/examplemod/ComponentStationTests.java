package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.Unpooled;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public final class ComponentStationTests {
    public static void register(RegisterGameTestsEvent event){
        var env=event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"component_stations_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"component_stations"),new Test(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),200,0,true,Rotation.NONE,false,1,1,false,0)));
    }
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> data){super(data);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Native component inventories, snapshot-bound packets and exact saved drops");}
        @Override public void run(GameTestHelper h){
            var level=h.getLevel();var pos=h.absolutePos(new BlockPos(3,1,3));var player=h.makeMockServerPlayerInLevel();var second=h.makeMockServerPlayerInLevel();player.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);second.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+1.5);
            for(var kind:ComponentStationKind.values()){
                level.setBlockAndUpdate(pos,ComponentStationContent.block(kind).defaultBlockState());var station=(ComponentStationBlockEntity)level.getBlockEntity(pos);player.getInventory().clearContent();
                var menu=new ComponentStationMenu(55,player.getInventory(),station);var other=new ComponentStationMenu(56,second.getInventory(),station);player.containerMenu=menu;second.containerMenu=other;
                check(h,menu.kind()==kind&&menu.slots.size()==37&&!menu.getSlot(0).mayPlace(new ItemStack(ArchaeologyContent.PICKAXE.get()))&&!menu.getSlot(0).mayPlace(new ItemStack(ExampleMod.ANCIENT_ARTIFACT.get()))&&!menu.getSlot(0).mayPlace(new ItemStack(Items.DIRT)),"Each station must expose one component slot and reject finished tools, unexcavated artifacts and unrelated items");
                var part=kind==ComponentStationKind.UNVEILER?ComponentWorkTests.ancient(ArtifactProfile.Find.PICKAXE_LEFT,3):ComponentWorkTests.component(ArtifactProfile.Find.TOOL_HANDLE,80,5,44);
                player.getInventory().setItem(9,part.copy());check(h,!menu.quickMoveStack(player,1).isEmpty()&&ItemStack.matches(part,station.getItem(0)),"Components must shift-click into every station without losing data");
                var saved=station.saveWithFullMetadata(level.registryAccess());var restored=BlockEntity.loadStatic(pos,station.getBlockState(),saved,level.registryAccess());check(h,restored instanceof ComponentStationBlockEntity&&((ComponentStationBlockEntity)restored).kind()==kind&&ItemStack.matches(part,((ComponentStationBlockEntity)restored).getItem(0)),"Every station kind and complete input must survive block-entity serialization");
                var client=new ComponentStationMenu(57,player.getInventory());client.setData(0,kind.ordinal());check(h,client.kind()==kind,"The shared client menu must synchronize its real station kind");
                int action=kind==ComponentStationKind.UNVEILER?10:kind==ComponentStationKind.LOCKER?20:kind==ComponentStationKind.INFLUENCER?30:0;
                var payload=packet(h,new ComponentStationPayload(55,action,part.copy()));
                check(h,payload.menuId()==55&&payload.action()==action&&ItemStack.matches(payload.expected(),part),"The native registry-aware payload codec must retain the expected part and action");
                ComponentStationPayload.dispatch(new ComponentStationPayload(99,action,part.copy()),player);ComponentStationPayload.dispatch(new ComponentStationPayload(55,999,part.copy()),player);check(h,ItemStack.matches(part,station.getItem(0)),"Wrong menu IDs and invalid actions must consume or change nothing");
                player.snapTo(pos.getX()+30,pos.getY(),pos.getZ());ComponentStationPayload.dispatch(payload,player);check(h,ItemStack.matches(part,station.getItem(0)),"Out-of-range players must not mutate a station");player.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
                ComponentStationPayload.dispatch(payload,player);var result=station.getItem(0).copy();check(h,!ItemStack.matches(part,result),"A real decoded request must execute the correct station action");
                if(kind==ComponentStationKind.INFLUENCER){ComponentStationPayload.dispatch(new ComponentStationPayload(55,31,result.copy()),player);ComponentStationPayload.dispatch(new ComponentStationPayload(55,30,result.copy()),player);check(h,ItemStack.matches(result,station.getItem(0)),"Fresh malicious requests must neither switch nor reroll an assigned influence");}
                ComponentStationPayload.dispatch(payload,player);ComponentStationPayload.dispatch(new ComponentStationPayload(56,action,part.copy()),second);check(h,ItemStack.matches(result,station.getItem(0)),"Duplicate packets and stale second-viewer clicks must not execute another mutation");
                check(h,!menu.quickMoveStack(player,0).isEmpty()&&station.getItem(0).isEmpty(),"Customized components must shift-click back out");
                station.setItem(0,ComponentWorkTests.component(ArtifactProfile.Find.TOOL_HANDLE,70,5,45));var replacement=station.getItem(0).copy();ComponentStationPayload.dispatch(payload,player);check(h,ItemStack.matches(replacement,station.getItem(0)),"An old click must not modify a replacement component");
                station.setItem(0,result.copy());var bounds=new net.minecraft.world.phys.AABB(pos).inflate(2);level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,bounds).forEach(net.minecraft.world.entity.item.ItemEntity::discard);level.destroyBlock(pos,true);
                var drops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,bounds);check(h,drops.stream().mapToInt(e->e.getItem().getCount()).sum()==2&&drops.stream().anyMatch(e->ItemStack.matches(result,e.getItem())),"Breaking any station must return its block and customized part exactly once");
            }
            level.setBlockAndUpdate(pos,ComponentStationContent.block(ComponentStationKind.CORRUPTOR).defaultBlockState());var station=(ComponentStationBlockEntity)level.getBlockEntity(pos);var pending=CreativePickaxeParts.template(ArtifactProfile.Find.TOOL_HANDLE,100);station.setItem(0,pending);
            check(h,!station.getItem(0).get(DataComponents.CUSTOM_DATA).copyTag().getBooleanOr(CreativePickaxeParts.PENDING,false)&&!ArtifactModifiers.read(station.getItem(0)).isEmpty(),"Pending Creative parts must resolve before a station can corrupt them");
            var menu=new ComponentStationMenu(60,player.getInventory(),station);player.containerMenu=menu;var expected=station.getItem(0).copy();ComponentStationPayload.dispatch(new ComponentStationPayload(60,0,expected),player);var corrupted=station.getItem(0).copy();
            check(h,ComponentWork.corrupted(corrupted)&&!CreativePickaxeParts.materialize(corrupted,player.getRandom()),"A part corrupted through the station must never acquire new modifiers afterward");
            System.out.println("COMPONENT_STATIONS_CHECK: PASS: all seven block kinds, strict component input, kind synchronization, native payload codecs and server route, range/stale/replay guards, two viewers, shift-click, saves, exact block drops and pending-Creative materialization before corruption");h.succeed();
        }
    }
    private static ComponentStationPayload packet(GameTestHelper h,ComponentStationPayload packet){var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),h.getLevel().registryAccess());try{ComponentStationPayload.STREAM_CODEC.encode(buffer,packet);return ComponentStationPayload.STREAM_CODEC.decode(buffer);}finally{buffer.release();}}
    private static void check(GameTestHelper h,boolean value,String message){if(!value)throw h.assertionException(message);}
    private ComponentStationTests(){}
}
