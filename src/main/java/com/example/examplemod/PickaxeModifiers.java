package com.example.examplemod;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;

/** Each part scales its own rolls; the assembly's average score never scales a second time. */
public final class PickaxeModifiers {
    public record Bonuses(Map<PickaxeTrait,Double> values){
        public Bonuses {values=Map.copyOf(values);}
        public double get(PickaxeTrait trait){return values.getOrDefault(trait,0.0);}
    }
    public static Bonuses totals(ItemStack stack){
        var values=new EnumMap<PickaxeTrait,Double>(PickaxeTrait.class);
        if(!stack.is(ArchaeologyContent.PICKAXE.get()))return new Bonuses(values);
        var assembly=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY);
        if(!assembly.getStringOr("Pattern","").equals("pickaxe"))return new Bonuses(values);
        var parts=assembly.getCompoundOrEmpty("Parts");
        for(String id:ReconstructionPattern.PICKAXE.parts){
            var part=parts.getCompoundOrEmpty(id);double quality=Math.clamp(part.getIntOr("Score",0),0,100)/100.0;
            for(var roll:ArtifactModifiers.read(part.getCompoundOrEmpty("Modifiers"))){
                var trait=PickaxeTrait.byId(roll.id());if(trait==null)continue;
                values.merge(trait,Math.clamp(roll.value(),0,trait.cap)*quality,Double::sum);
            }
        }
        values.replaceAll((trait,value)->Math.clamp(value,0,trait.cap));return new Bonuses(values);
    }
    public static List<Component> pieceLore(ItemStack stack){
        var lore=new ArrayList<Component>();int score=ArtifactQuality.score(stack);if(score<0)return lore;
        lore.add(Component.literal("Excavation score: "+score+"/100").withStyle(ChatFormatting.GRAY));
        var recovered=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(ArtifactQuality.RECOVERED);
        if(!ArtifactModifiers.read(stack).isEmpty())lore.add(Component.literal(ArtifactModifierCatalog.tier(recovered.getIntOr("Rarity",0)).label()+" modifiers").withStyle(ChatFormatting.DARK_AQUA));
        for(var roll:ArtifactModifiers.read(stack)){
            var trait=PickaxeTrait.byId(roll.id());if(trait==null)continue;
            double effective=roll.value()*score/100.0;
            lore.add(Component.literal(trait.label+": +"+trait.format(effective)+" (rolled "+trait.format(roll.value())+")").withStyle(score==0?ChatFormatting.DARK_GRAY:ChatFormatting.AQUA));
        }
        return lore;
    }
    public static void applyToAssembly(ItemStack result){
        if(!result.is(ArchaeologyContent.PICKAXE.get()))return;
        var bonuses=totals(result);
        result.set(DataComponents.MAX_DAMAGE,(int)Math.round(1800*(1+bonuses.get(PickaxeTrait.DURABILITY))));
        double reach=bonuses.get(PickaxeTrait.REACH);
        if(reach>0){
            var attributes=result.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY);
            result.set(DataComponents.ATTRIBUTE_MODIFIERS,attributes.withModifierAdded(Attributes.BLOCK_INTERACTION_RANGE,
                new AttributeModifier(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"pickaxe_reach"),reach,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND));
        }
        var lore=new ArrayList<Component>();lore.add(Component.literal("Assembly score: "+ArtifactQuality.formatted(ArtifactQuality.assemblyScore(result))+"/100").withStyle(ChatFormatting.GRAY));
        for(var trait:PickaxeTrait.values())if(bonuses.values().containsKey(trait))lore.add(Component.literal(trait.label+": +"+trait.format(bonuses.get(trait))).withStyle(ChatFormatting.AQUA));
        result.set(DataComponents.LORE,new ItemLore(lore));
    }
    private PickaxeModifiers(){}
}
