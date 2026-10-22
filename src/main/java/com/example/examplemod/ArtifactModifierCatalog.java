package com.example.examplemod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static com.example.examplemod.PickaxeTrait.*;

/** Editable family weights, per-family value tiers, and artifact rarity rules. */
public final class ArtifactModifierCatalog {
    public static final int REVISION = 7;
    public record Rarity(String label, List<Double> chances) {
        public Rarity { chances = List.copyOf(chances); }
    }
    public record Spec(int minimumRarity, List<ModifierTier> tiers, ModifierInfluence influence, boolean influenceOnly) {
        public Spec(int minimumRarity, List<ModifierTier> tiers) { this(minimumRarity,tiers,ModifierInfluence.NONE,false); }
        public Spec { tiers = List.copyOf(tiers); if(influence==null || influenceOnly && influence==ModifierInfluence.NONE) throw new IllegalArgumentException("Influence-only modifiers need an influence tag"); }
    }
    private static final List<Rarity> RARITIES = List.of(
        new Rarity("Common", List.of(1.0)),
        new Rarity("Uncommon", List.of(1.0, .20)),
        new Rarity("Rare", List.of(1.0, .45, .10)),
        new Rarity("Epic", List.of(1.0, .65, .25)),
        new Rarity("Legendary", List.of(1.0, .80, .50, .15)),
        new Rarity("Unique", List.of(1.0, .90, .70, .35))
    );

    private static final Map<PickaxeTrait,Spec> SPECS=ModifierDefinitions.SPECS;
    /** Shared pieces are deliberately tool-agnostic; mining traits belong to the three head parts. */
    public static Map<PickaxeTrait,Integer> weights(ArtifactProfile.Find part){
        boolean utility=part==ArtifactProfile.Find.TOOL_HANDLE||part==ArtifactProfile.Find.TOOL_CONNECTOR;
        boolean head=part==ArtifactProfile.Find.PICKAXE_LEFT||part==ArtifactProfile.Find.PICKAXE_BRIDGE||part==ArtifactProfile.Find.PICKAXE_RIGHT;
        if(!utility&&!head)return Map.of();
        var result=new java.util.EnumMap<PickaxeTrait,Integer>(PickaxeTrait.class);
        for(var trait:PickaxeTrait.values())if(trait.utility==utility)result.put(trait,ModifierDefinitions.weight(trait));
        return Map.copyOf(result);
    }
    public static Rarity rarity(int rarity) { return RARITIES.get(Math.clamp(rarity, 0, 5)); }
    public static Spec spec(PickaxeTrait trait) { return SPECS.get(trait); }
    public static ModifierInfluence tag(String id) { var trait=PickaxeTrait.byId(id); var spec=trait==null?null:SPECS.get(trait); return spec==null?null:spec.influence(); }
    public static boolean pickaxePart(ArtifactProfile.Find part) { return !weights(part).isEmpty(); }
    public static ArtifactModifiers.Pool pool(ArtifactProfile.Find part) { return pool(part, 0); }
    public static ArtifactModifiers.Pool pool(ArtifactProfile.Find part, int rarity) {
        return pool(part,rarity,ModifierInfluence.NONE);
    }
    public static ArtifactModifiers.Pool pool(ArtifactProfile.Find part, int rarity, ModifierInfluence influence) {
        java.util.Objects.requireNonNull(influence);
        var weights = weights(part);
        var definitions = new ArrayList<ArtifactModifiers.Definition>();
        // Enum order keeps seeded results stable across JVM launches.
        for (var trait : PickaxeTrait.values()) {
            var spec = SPECS.get(trait);
            var weight = weights.get(trait);
            if (spec == null || weight == null || !influence.allows(spec.influence(),spec.influenceOnly())) continue;
            String group = trait == SILK || trait == FORTUNE || trait == SMELTING || trait == PRECISION_SILK ? "harvest" : "";
            var definition = new ArtifactModifiers.Definition(trait.id, influence.weight(weight,spec.influence()), spec.minimumRarity(), 0, spec.tiers(), group);
            if (definition.eligible(rarity, 100)) definitions.add(definition);
        }
        return new ArtifactModifiers.Pool(definitions.isEmpty() ? List.of() : rarity(rarity).chances(), definitions);
    }
    private ArtifactModifierCatalog() {}
}
