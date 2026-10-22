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
        public double base(){return Math.clamp(roll.value()*trait.power,0,trait.cap)*quality;}
        public double value(int ascensions){return trait.growth==PickaxeTrait.Growth.MILESTONE?base():base()*(1+ascensions*roll.scaling().ascensionGrowth+levelHits*roll.scaling().levelGrowth);}
    }
    private record Cached(CustomData source,List<Contribution> contributions,Bonuses totals,int ascensions){}
    private static final Map<ItemStack,Cached> CACHE=java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
    private static Cached cached(ItemStack stack){
        var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY);var cached=CACHE.get(stack);
        if(cached!=null&&cached.source()==data)return cached;
        var contributions=readContributions(stack);var values=new EnumMap<PickaxeTrait,Double>(PickaxeTrait.class);int ascensions=PickaxeProgression.read(stack).ascensions();
        for(var c:contributions)values.merge(c.trait(),c.value(ascensions),Double::sum);
        values.replaceAll((trait,value)->Math.clamp(value,0,trait.cap));cached=new Cached(data,contributions,new Bonuses(values),ascensions);CACHE.put(stack,cached);return cached;
    }
    public static List<Contribution> contributions(ItemStack stack){return stack.isEmpty()?List.of():cached(stack).contributions();}
    private static List<Contribution> readContributions(ItemStack stack){
        var result=new ArrayList<Contribution>();var pattern=pattern(stack);if(pattern==null)return result;
        var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var assembly=root.getCompoundOrEmpty(ArtifactQuality.ASSEMBLY);
        if(!assembly.getStringOr("Pattern","").equals(pattern.key()))return result;
        var growth=root.getCompoundOrEmpty(PickaxeProgression.KEY).getCompoundOrEmpty(PickaxeGrowth.COUNTERS);var parts=assembly.getCompoundOrEmpty("Parts");
        for(String id:pattern.parts){
            var part=parts.getCompoundOrEmpty(id);double quality=Math.clamp(part.getIntOr("Score",0),0,ComponentWork.MAX_QUALITY)/100.0;
            for(var roll:ArtifactModifiers.read(part.getCompoundOrEmpty("Modifiers"))){
                var trait=PickaxeTrait.byId(roll.id());if(trait==null||pattern!=ReconstructionPattern.PICKAXE&&!trait.utility)continue;String key=id+"|"+roll.id();
                result.add(new Contribution(key,trait,roll,quality,Math.clamp(growth.getIntOr(key,0),0,99)));
            }
        }
        return List.copyOf(result);
    }
    public static ReconstructionPattern pattern(ItemStack stack){
        String key=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY).getStringOr("Pattern","");
        if(key.isEmpty())return null;
        for(var p:ReconstructionPattern.values())if(p.key().equals(key)&&stack.is(p.output()))return p;return null;
    }
    /** Stage is controlled by the strongest nonzero-quality copy, never by a dormant zero-score piece. */
    public static int stage(ItemStack stack,PickaxeTrait trait){
        int result=0,asc=PickaxeProgression.supports(stack)?cached(stack).ascensions():0;
        for(var c:contributions(stack))if(c.trait()==trait&&c.base()>0){int offset=c.roll().scaling().ordinal()<=1?1:c.roll().scaling().ordinal()>=4?-1:0;
            int stage=(asc>=3+offset?1:0)+(asc>=6+offset?1:0)+(asc>=9+offset?1:0);result=Math.max(result,stage);}
        return result;
    }
    /** Only the three reconstructed mining heads contribute to the innate core. */
    public static double headQuality(ItemStack stack){
        if(pattern(stack)!=ReconstructionPattern.PICKAXE)return 0;
        var parts=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY).getCompoundOrEmpty("Parts");
        double sum=0;for(String id:ReconstructionPattern.PICKAXE.parts.subList(0,3))sum+=Math.clamp(parts.getCompoundOrEmpty(id).getIntOr("Score",0),0,ComponentWork.MAX_QUALITY)/100.0;
        return sum/3;
    }
    public static double coreSpeed(ItemStack stack){return 1+ModifierGameplay.CORE_SPEED_BONUS*headQuality(stack);}
    public static Bonuses totals(ItemStack stack){
        return stack.isEmpty()?new Bonuses(Map.of()):cached(stack).totals();
    }
    public static List<Component> pieceLore(ItemStack stack){return PickaxeTooltips.piece(stack);}
    public static void applyToAssembly(ItemStack result){
        if(pattern(result)==null){if(PickaxeProgression.supports(result)){PickaxeProgression.initialize(result);updateLore(result);}return;}
        PickaxeProgression.initialize(result);
        var bonuses=totals(result);
        result.set(DataComponents.MAX_DAMAGE,(int)Math.round(1800*(1+bonuses.get(PickaxeTrait.DURABILITY))+bonuses.get(PickaxeTrait.RESERVE)));
        addAttribute(result,Attributes.BLOCK_INTERACTION_RANGE,"pickaxe_reach",bonuses.get(PickaxeTrait.REACH),AttributeModifier.Operation.ADD_VALUE);
        addAttribute(result,Attributes.MOVEMENT_SPEED,"tool_speed",bonuses.get(PickaxeTrait.SWIFT_GRIP),AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        addAttribute(result,Attributes.KNOCKBACK_RESISTANCE,"tool_brace",bonuses.get(PickaxeTrait.STEADY_GRIP),AttributeModifier.Operation.ADD_VALUE);
        addAttribute(result,Attributes.ARMOR,"tool_guard",bonuses.get(PickaxeTrait.GUARD),AttributeModifier.Operation.ADD_VALUE);
        addAttribute(result,Attributes.MAX_HEALTH,"tool_vigor",bonuses.get(PickaxeTrait.VIGOR),AttributeModifier.Operation.ADD_VALUE);
        updateLore(result);
    }
    private static void addAttribute(ItemStack stack,net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,String id,double value,AttributeModifier.Operation operation){
        if(value<=0)return;var attributes=stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS,attributes.withModifierAdded(attribute,new AttributeModifier(Identifier.fromNamespaceAndPath(ExampleMod.MODID,id),value,operation),EquipmentSlotGroup.MAINHAND));
    }
    public static void updateLore(ItemStack result){
        result.set(DataComponents.LORE,new ItemLore(PickaxeTooltips.tool(result)));PickaxeTooltips.markCurrent(result);
    }
    private PickaxeModifiers(){}
}
