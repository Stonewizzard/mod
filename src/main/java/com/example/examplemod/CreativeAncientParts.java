package com.example.examplemod;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Fixed, named samples make all six prototype stations easy to review in Creative. */
public final class CreativeAncientParts {
    public static List<ItemStack> variants(){
        var result=new ArrayList<ItemStack>();
        for(var find:CreativePickaxeParts.FINDS){
            for(long seed=0;seed<10000;seed++){
                var profile=new ArtifactProfile(seed,3,ArtifactProfile.Matrix.OBSIDIAN,find);var record=ArtifactModifiers.record(profile,80,ArtifactModifierCatalog.pool(find,3));
                if(AncientModifiers.offers(record).size()!=3||ArtifactModifiers.read(record).size()<3)continue;
                var stack=new ItemStack(ReconstructionContent.reward(find));var root=new CompoundTag();var recovered=new CompoundTag();recovered.putInt("Quality",80);recovered.putInt("Score",80);recovered.putInt("Rarity",3);recovered.putLong("Seed",seed);recovered.putString("Find",find.name());root.put(ArtifactQuality.RECOVERED,recovered);root.put(ArtifactModifiers.KEY,record);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
                stack.set(DataComponents.ITEM_NAME,Component.literal("Ancient Test ").append(stack.getHoverName()));ArtifactQuality.showRecoveredScore(stack);PickaxeTooltips.markCurrent(stack);result.add(stack);break;
            }
        }
        return List.copyOf(result);
    }
    private CreativeAncientParts(){}
}
