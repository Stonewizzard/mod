package com.example.examplemod;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
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
    public record Contribution(String key,PickaxeTrait trait,ArtifactModifiers.Roll roll,double quality,int levelHits){
        public double base(){return Math.clamp(roll.value(),0,trait.cap)*quality;}
        public double value(int ascensions){return base()*(1+ascensions*roll.scaling().ascensionGrowth+levelHits*roll.scaling().levelGrowth);}
    }
    public static List<Contribution> contributions(ItemStack stack){
        var result=new ArrayList<Contribution>();if(!stack.is(ArchaeologyContent.PICKAXE.get()))return result;
        var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var assembly=root.getCompoundOrEmpty(ArtifactQuality.ASSEMBLY);
        if(!assembly.getStringOr("Pattern","").equals("pickaxe"))return result;
        var growth=root.getCompoundOrEmpty(PickaxeProgression.KEY).getCompoundOrEmpty(PickaxeGrowth.COUNTERS);var parts=assembly.getCompoundOrEmpty("Parts");
        for(String id:ReconstructionPattern.PICKAXE.parts){
            var part=parts.getCompoundOrEmpty(id);double quality=Math.clamp(part.getIntOr("Score",0),0,100)/100.0;
            for(var roll:ArtifactModifiers.read(part.getCompoundOrEmpty("Modifiers"))){
                var trait=PickaxeTrait.byId(roll.id());if(trait==null)continue;String key=id+"|"+roll.id();
                result.add(new Contribution(key,trait,roll,quality,Math.clamp(growth.getIntOr(key,0),0,99)));
            }
        }
        return List.copyOf(result);
    }
    public static Bonuses totals(ItemStack stack){
        var values=new EnumMap<PickaxeTrait,Double>(PickaxeTrait.class);int ascensions=PickaxeProgression.read(stack).ascensions();
        for(var contribution:contributions(stack))values.merge(contribution.trait(),contribution.value(ascensions),Double::sum);
        values.replaceAll((trait,value)->Math.clamp(value,0,trait.cap));return new Bonuses(values);
    }
    public static List<Component> pieceLore(ItemStack stack){return PickaxeTooltips.piece(stack);}
    public static void applyToAssembly(ItemStack result){
        if(!result.is(ArchaeologyContent.PICKAXE.get()))return;
        PickaxeProgression.initialize(result);
        var bonuses=totals(result);
        result.set(DataComponents.MAX_DAMAGE,(int)Math.round(1800*(1+bonuses.get(PickaxeTrait.DURABILITY))));
        double reach=bonuses.get(PickaxeTrait.REACH);
        if(reach>0){
            var attributes=result.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY);
            result.set(DataComponents.ATTRIBUTE_MODIFIERS,attributes.withModifierAdded(Attributes.BLOCK_INTERACTION_RANGE,
                new AttributeModifier(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"pickaxe_reach"),reach,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND));
        }
        updateLore(result);
    }
    public static void updateLore(ItemStack result){
        result.set(DataComponents.LORE,new ItemLore(PickaxeTooltips.tool(result)));PickaxeTooltips.markCurrent(result);
    }
    private PickaxeModifiers(){}
}


