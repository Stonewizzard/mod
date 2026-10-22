package com.example.examplemod;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

/** Balance/edit materials here. Every rolled occurrence costs resources, including repeated traits. */
public final class AscensionResources {
    public record Binding(Item item,int baseCount){}
    public record Requirement(Item item,int count){}
    public static final Map<PickaxeTrait,Binding> BINDINGS=bindings();
    private static Map<PickaxeTrait,Binding> bindings(){
        var map=new EnumMap<PickaxeTrait,Binding>(PickaxeTrait.class);
        map.put(PickaxeTrait.SPEED,new Binding(Items.REDSTONE,2));
        map.put(PickaxeTrait.FORTUNE,new Binding(Items.LAPIS_LAZULI,2));
        map.put(PickaxeTrait.MAGNET,new Binding(Items.IRON_INGOT,2));
        map.put(PickaxeTrait.SILK,new Binding(Items.STRING,2));
        map.put(PickaxeTrait.DURABILITY,new Binding(Items.COPPER_INGOT,2));
        map.put(PickaxeTrait.EXPERIENCE,new Binding(Items.AMETHYST_SHARD,2));
        map.put(PickaxeTrait.AQUATIC,new Binding(Items.PRISMARINE_SHARD,2));
        map.put(PickaxeTrait.AIRBORNE,new Binding(Items.FEATHER,2));
        map.put(PickaxeTrait.REACH,new Binding(Items.ENDER_PEARL,1));
        map.put(PickaxeTrait.SMELTING,new Binding(Items.BLAZE_POWDER,2));
        map.put(PickaxeTrait.PHASEBOUND,new Binding(Items.ENDER_PEARL,2));
        if(map.size()!=PickaxeTrait.values().length)throw new IllegalStateException("Every pickaxe trait needs an ascension resource");
        return Map.copyOf(map);
    }
    public static List<Requirement> requirements(ItemStack tool){
        if(!PickaxeProgression.supports(tool)||PickaxeProgression.read(tool).mastered())return List.of();
        var counts=new EnumMap<PickaxeTrait,Integer>(PickaxeTrait.class);
        var assembly=tool.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY);
        var parts=assembly.getCompoundOrEmpty("Parts");
        for(String id:ReconstructionPattern.PICKAXE.parts)for(var roll:ArtifactModifiers.read(parts.getCompoundOrEmpty(id).getCompoundOrEmpty("Modifiers"))){
            var trait=PickaxeTrait.byId(roll.id());if(trait!=null)counts.merge(trait,1,Integer::sum);
        }
        // Stable enum order keeps the cost list still as inventory contents change.
        var materials=new LinkedHashMap<Item,Integer>();int stage=PickaxeProgression.read(tool).ascensions()+1;
        for(var trait:PickaxeTrait.values())if(counts.containsKey(trait)){var binding=BINDINGS.get(trait);materials.merge(binding.item(),binding.baseCount()*counts.get(trait)*stage,Integer::sum);}
        var result=new ArrayList<Requirement>();materials.forEach((item,count)->result.add(new Requirement(item,count)));return List.copyOf(result);
    }
    public static boolean isMaterial(ItemStack stack){return !stack.isEmpty()&&BINDINGS.values().stream().anyMatch(b->stack.is(b.item()));}
    public static int available(Container inventory,Item item){int count=0;for(int slot=1;slot<inventory.getContainerSize();slot++)if(inventory.getItem(slot).is(item))count+=inventory.getItem(slot).getCount();return count;}
    /** Server-thread transaction: validate every cost before removing a single item. */
    public static boolean tryAscend(Container inventory){
        var tool=inventory.getItem(0);if(!PickaxeProgression.supports(tool)||!PickaxeProgression.read(tool).needsAscension())return false;
        var costs=requirements(tool);for(var cost:costs)if(available(inventory,cost.item())<cost.count())return false;
        if(!PickaxeProgression.ascend(tool))return false;
        for(var cost:costs){int remaining=cost.count();for(int slot=1;slot<inventory.getContainerSize()&&remaining>0;slot++){
            var stack=inventory.getItem(slot);if(!stack.is(cost.item()))continue;int used=Math.min(remaining,stack.getCount());stack.shrink(used);remaining-=used;if(stack.isEmpty())inventory.setItem(slot,ItemStack.EMPTY);
        }}
        inventory.setChanged();return true;
    }
    private AscensionResources(){}
}
