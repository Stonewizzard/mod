package com.example.examplemod;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ComponentStationContent {
    public record Entry(ComponentStationKind kind,DeferredBlock<ComponentStationBlock> block,DeferredItem<BlockItem> item){}
    public static final List<Entry> STATIONS=create();
    private static List<Entry> create(){var result=new ArrayList<Entry>();for(var kind:ComponentStationKind.values()){var block=ExampleMod.BLOCKS.registerBlock(kind.id,p->new ComponentStationBlock(p,kind),p->p.strength(3).sound(SoundType.STONE));result.add(new Entry(kind,block,ExampleMod.ITEMS.registerSimpleBlockItem(kind.id,block)));}return List.copyOf(result);}
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ComponentStationBlockEntity>> ENTITY=ExampleMod.BLOCK_ENTITIES.register("component_station",()->new BlockEntityType<>(ComponentStationBlockEntity::new,STATIONS.stream().map(e->e.block().get()).toArray(ComponentStationBlock[]::new)));
    public static final DeferredHolder<MenuType<?>,MenuType<ComponentStationMenu>> MENU=ExampleMod.MENUS.register("component_station",()->new MenuType<>(ComponentStationMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static ComponentStationBlock block(ComponentStationKind kind){return STATIONS.get(kind.ordinal()).block().get();}
    public static void register(){}
    private ComponentStationContent(){}
}
