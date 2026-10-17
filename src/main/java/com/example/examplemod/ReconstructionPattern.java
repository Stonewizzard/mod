package com.example.examplemod;

import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/** Stable IDs and explicit slot maps; append patterns without rewriting saved inventories. */
public enum ReconstructionPattern {
    PICKAXE(1,"Pickaxe","restored_ancient_pickaxe",List.of(0,1,2,3,4),List.of("pickaxe_left_tip","pickaxe_head_bridge","pickaxe_right_tip","ancient_tool_connector","ancient_tool_handle")),
    SWORD(2,"Sword","restored_ancient_sword","ancient_sword_blade"),
    AXE(3,"Axe","restored_ancient_axe","ancient_axe_head"),
    SHOVEL(4,"Shovel","restored_ancient_shovel","ancient_shovel_head"),
    HOE(5,"Hoe","restored_ancient_hoe","ancient_hoe_head"),
    MACE(6,"Mace","restored_ancient_mace","ancient_mace_head"),
    SPEAR(7,"Spear","restored_ancient_spear","ancient_spear_tip");
    public final int id;
    public final String label,outputId;
    public final List<Integer> slots;
    public final List<String> parts;
    ReconstructionPattern(int id,String label,String outputId,String head){this(id,label,outputId,List.of(1,3,4),List.of(head,"ancient_tool_connector","ancient_tool_handle"));}
    ReconstructionPattern(int id,String label,String outputId,List<Integer> slots,List<String> parts){this.id=id;this.label=label;this.outputId=outputId;this.slots=List.copyOf(slots);this.parts=List.copyOf(parts);}
    public String key(){return name().toLowerCase(Locale.ROOT);}
    public String partId(int slot){int index=slots.indexOf(slot);return index<0?null:parts.get(index);}
    public Item part(int slot){var id=partId(slot);return id==null?null:item(id);}
    public Item output(){return item(outputId);}
    private static Item item(String id){return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(ExampleMod.MODID,id));}
    public static ReconstructionPattern byId(int id){for(var pattern:values())if(pattern.id==id)return pattern;return null;}
}
