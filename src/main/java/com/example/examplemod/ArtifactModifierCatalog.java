package com.example.examplemod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static com.example.examplemod.PickaxeTrait.*;

/** Editable family weights, per-family value tiers, and artifact rarity rules. */
public final class ArtifactModifierCatalog {
    public static final int REVISION = 6;
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

    // Arguments after the range are Common, Uncommon, Rare, Epic, Legendary, Unique weights.
    // T1 is strongest. Each family owns its list; there is no fixed number of value tiers.
    private static ModifierTier t(int rank, double min, double max, Integer... weights) {
        return new ModifierTier("t" + rank, rank, min, max, List.of(weights));
    }
    private static Spec spec(int minimumRarity, ModifierTier... tiers) {
        return new Spec(minimumRarity, List.of(tiers));
    }
    private static Spec tagged(ModifierInfluence influence, boolean exclusive, int minimumRarity, ModifierTier... tiers) {
        return new Spec(minimumRarity,List.of(tiers),influence,exclusive);
    }
    private static final Map<PickaxeTrait, Spec> SPECS = Map.ofEntries(
        Map.entry(SPEED, spec(0,
            t(6, .04, .12, 940, 650, 350, 150, 100, 50),
            t(5, .12, .22, 50, 280, 400, 200, 150, 100),
            t(4, .22, .40, 10, 65, 220, 300, 200, 150),
            t(3, .40, .65, 0, 5, 28, 250, 250, 200),
            t(2, .65, .90, 0, 0, 2, 90, 200, 250),
            t(1, .90, 1.20, 0, 0, 0, 10, 100, 250))),
        Map.entry(FORTUNE, spec(0,
            t(5, .10, .35, 950, 700, 400, 200, 120, 60),
            t(4, .35, .60, 45, 250, 350, 250, 180, 120),
            t(3, .60, .90, 5, 49, 220, 300, 250, 220),
            t(2, .90, 1.10, 0, 1, 30, 220, 300, 300),
            t(1, 1.10, 1.25, 0, 0, 0, 30, 150, 300))),
        Map.entry(DURABILITY, spec(0,
            t(5, .05, .12, 950, 700, 400, 200, 120, 60),
            t(4, .12, .25, 45, 250, 350, 250, 180, 120),
            t(3, .25, .40, 5, 49, 220, 300, 250, 220),
            t(2, .40, .50, 0, 1, 30, 220, 300, 300),
            t(1, .50, .65, 0, 0, 0, 30, 150, 300))),
        Map.entry(EXPERIENCE, spec(0,
            t(6, .05, .15, 940, 650, 350, 150, 100, 50),
            t(5, .15, .25, 50, 280, 400, 200, 150, 100),
            t(4, .25, .40, 10, 65, 220, 300, 200, 150),
            t(3, .40, .60, 0, 5, 28, 250, 250, 200),
            t(2, .60, .80, 0, 0, 2, 90, 200, 250),
            t(1, .80, 1.00, 0, 0, 0, 10, 100, 250))),
        Map.entry(MAGNET, spec(1,
            t(4, .75, 1.75, 0, 900, 650, 400, 250, 150),
            t(3, 1.75, 3.00, 0, 95, 300, 400, 350, 300),
            t(2, 3.00, 4.50, 0, 5, 49, 180, 300, 350),
            t(1, 4.50, 6.00, 0, 0, 1, 20, 100, 200))),
        Map.entry(AQUATIC, spec(1,
            t(4, .10, .25, 0, 900, 650, 400, 250, 150),
            t(3, .25, .40, 0, 95, 300, 400, 350, 300),
            t(2, .40, .60, 0, 5, 49, 180, 300, 350),
            t(1, .60, .85, 0, 0, 1, 20, 100, 200))),
        Map.entry(AIRBORNE, spec(1,
            t(4, .10, .25, 0, 900, 650, 400, 250, 150),
            t(3, .25, .40, 0, 95, 300, 400, 350, 300),
            t(2, .40, .60, 0, 5, 49, 180, 300, 350),
            t(1, .60, .85, 0, 0, 1, 20, 100, 200))),
        Map.entry(SILK, spec(2,
            t(3, .10, .25, 0, 0, 950, 700, 450, 300),
            t(2, .25, .50, 0, 0, 49, 270, 450, 450),
            t(1, .50, .85, 0, 0, 1, 30, 100, 250))),
        Map.entry(REACH, tagged(ModifierInfluence.END, false, 2,
            t(4, .15, .35, 0, 0, 650, 400, 250, 150),
            t(3, .35, .55, 0, 0, 300, 400, 350, 300),
            t(2, .55, .80, 0, 0, 49, 180, 300, 350),
            t(1, .80, 1.00, 0, 0, 1, 20, 100, 200))),
        Map.entry(SMELTING, tagged(ModifierInfluence.NETHER, false, 3,
            t(3, .15, .35, 0, 0, 0, 850, 600, 400),
            t(2, .35, .55, 0, 0, 0, 145, 350, 450),
            t(1, .55, .75, 0, 0, 0, 5, 50, 150))),
        Map.entry(PHASEBOUND, tagged(ModifierInfluence.END, true, 4,
            t(3, .10, .25, 0, 0, 0, 0, 700, 450),
            t(2, .25, .50, 0, 0, 0, 0, 250, 400),
            t(1, .50, .80, 0, 0, 0, 0, 50, 150)))
    );

    /** Family frequency is independent of how many tiers a modifier has. */
    public static Map<PickaxeTrait, Integer> weights(ArtifactProfile.Find part) {
        return switch (part) {
            case PICKAXE_LEFT -> Map.of(SPEED,32,FORTUNE,30,DURABILITY,12,EXPERIENCE,15,SILK,8,SMELTING,8,PHASEBOUND,8);
            case PICKAXE_BRIDGE -> Map.of(SPEED,24,DURABILITY,30,EXPERIENCE,18,FORTUNE,14,SILK,8,SMELTING,10,PHASEBOUND,8);
            case PICKAXE_RIGHT -> Map.of(FORTUNE,30,SPEED,28,DURABILITY,14,EXPERIENCE,18,SILK,12,SMELTING,12,PHASEBOUND,12);
            case TOOL_CONNECTOR -> Map.of(DURABILITY,26,EXPERIENCE,22,MAGNET,28,FORTUNE,10,REACH,12,AQUATIC,10,SMELTING,8,PHASEBOUND,8);
            case TOOL_HANDLE -> Map.of(SPEED,24,DURABILITY,24,EXPERIENCE,14,MAGNET,22,AQUATIC,18,AIRBORNE,18,REACH,14,PHASEBOUND,8);
            default -> Map.of();
        };
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
            String group = trait == SILK || trait == FORTUNE || trait == SMELTING ? "harvest" : "";
            var definition = new ArtifactModifiers.Definition(trait.id, influence.weight(weight,spec.influence()), spec.minimumRarity(), 0, spec.tiers(), group);
            if (definition.eligible(rarity, 100)) definitions.add(definition);
        }
        return new ArtifactModifiers.Pool(definitions.isEmpty() ? List.of() : rarity(rarity).chances(), definitions);
    }
    private ArtifactModifierCatalog() {}
}
