package com.example.examplemod;

import java.util.List;
import net.minecraft.world.item.Item;

/** Explicit pattern IDs leave room for more tools without changing saved menu state. */
public enum ReconstructionPattern {
    PICKAXE(1,"Pickaxe",List.of("pickaxe_left_tip","pickaxe_head_bridge","pickaxe_right_tip","ancient_tool_connector","ancient_tool_handle"));
    public final int id;public final String label;public final List<String> parts;
    ReconstructionPattern(int id,String label,List<String> parts){this.id=id;this.label=label;this.parts=parts;}
    public Item part(int slot){return ReconstructionContent.pieces().get(slot);}
    public static ReconstructionPattern byId(int id){for(var pattern:values())if(pattern.id==id)return pattern;return null;}
}
