# Component influences

Influence is a permanent property assigned **only by the Influencer**. Every excavated piece, existing saved piece, and Creative test piece starts uninfluenced. A modifier's thematic tag is separate: an uninfluenced piece may roll an ordinary tagged modifier without acquiring its influence.

Most modifiers remain neutral. The initial influences are **Nether** and **End**.

## Prototype workflow

The Influencer is a recipe-free Creative/Functional Blocks workstation using a plain vanilla block texture. It accepts the same recovered components as the other stations; only components with active modifier pools can be influenced. Unexcavated artifacts and finished equipment are rejected.

1. Put a recovered component in the input.
2. Choose Nether or End. This permanently assigns that influence and rerolls unlocked modifiers.
3. The same button subsequently rerolls within that influence. The other influence is disabled forever.

There is no removal, clearing, or switching operation. Corrupted components reject the Influencer entirely. Assignment and rerolls currently have no processing cost, consistent with the other prototype stations.

The station uses the existing server-authoritative input-snapshot protocol. A stale click, replacement input, duplicate request, invalid choice, or out-of-range player changes nothing. Permanent assignment is checked on the server even if a client sends a fresh request for the other influence.

## Modifier eligibility and weighting

Each modifier specification has an optional influence tag and an `influenceOnly` flag.

| Modifier kind | Uninfluenced piece | Matching influenced piece | Different influence |
| --- | --- | --- | --- |
| Neutral | Normal weight | Normal weight | Normal weight |
| Ordinary tagged | Normal weight | **6× family weight** | Excluded |
| Influence-only tagged | Excluded | **6× family weight** | Excluded |

The multiplier affects relative family weight, not an absolute chance. A matching modifier is favored but never guaranteed. Existing part-role restrictions, minimum artifact rarity, value-tier weights, modifier-count attempts, duplicate restrictions, and harvest exclusivity still apply. Choosing an influence does not improve the artifact's rarity, quality, value-tier odds, or F–S growth-grade odds directly.

Current tagged modifiers:

- **Smelting:** Nether; ordinary tagged modifier, available on uninfluenced Epic+ pieces as before.
- **Block reach:** End; ordinary tagged modifier, available on eligible uninfluenced Rare+ connectors/handles as before.
- **Phasebound / Phase collection:** End-only, Legendary/Unique, available to all five pickaxe-compatible roles.

Other current modifiers remain neutral. An End-influenced right tip cannot roll Smelting. Its first successful Unique modifier attempt has Phasebound weight 72 out of 174 (approximately 41.38%). An uninfluenced right tip cannot roll Phasebound at all. Nether Smelting on that role has the same initial 72/174 probability, compared with the uninfluenced 12/114 probability. Later attempts recalculate after exclusions.

## Locks and related stations

A locked modifier is retained **exactly**, including its value, quality scale, F–S grade, and origin tier. It consumes the guaranteed modifier slot; it does not add a free extra modifier. Remaining attempts use the artifact's normal extra-modifier chances and exclude the protected modifier and its conflicting group.

A foreign or unknown locked modifier blocks an incompatible first assignment. For example, a locked Smelting roll allows Nether assignment and prevents End assignment. A neutral lock allows either initial influence. Ancient replacement never replaces the locked roll.

Influencer rerolls replace old unlocked modifiers and their old hidden offers. Any newly discovered ancient slot saves three choices from the matching influence-aware pool. Opening or saving the Unveiler cannot reroll those choices. Unveiling an ordinary tagged modifier on an uninfluenced piece still does not assign influence.

Corruption-added modifiers also use the current influence-aware pool. Corruption retains the assigned influence, then makes the part final. Annulment, scaling rerolls, restoration, and locks preserve the influence; none can remove or change it.

## Phasebound behavior

Phase collection is a percentage chance per final mined-drop stack to insert it directly into the mining player's inventory. Its initial tiers are:

| Tier | Base chance | Legendary weight | Unique weight |
| --- | --- | ---: | ---: |
| T3 | 10–25% | 700 | 450 |
| T2 | 25–50% | 250 | 400 |
| T1 | 50–80% | 50 | 150 |

Quality and F–S growth scale the chance normally; the aggregate cap is 100%. It runs after Fortune/Silk/smelting processing, retains mining/furnace XP rules, and only processes the current mining event's drops. It does not collect unrelated thrown items or nearby world items. A full inventory leaves all drops in the world; a partial insertion leaves the exact remainder. Its ascension resource is two Ender Pearls per modifier occurrence per ascension stage.

Different components in a reconstructed tool can have different influences. Assembly copies their exact finalized records and customization provenance; this can combine effects from different components. Existing assembled tools are not rerolled or influenced by this update.

## Code and saves

- `ModifierInfluence`: stable IDs, labels, colors, the available choices, and the editable matching-weight multiplier.
- `ArtifactModifierCatalog.Spec`: optional tag and influence-only eligibility beside each modifier's rarity and value tiers. The neutral `spec(...)` helper remains the default; `tagged(...)` opts in explicitly.
- `ArtifactModifierCatalog.pool(part, rarity, influence)`: the shared filter/weight policy. Existing two-argument callers use the uninfluenced pool.
- `ComponentInfluences`: permanent assignment validation and reroll construction, including exact lock preservation.
- `AncientModifiers` and `ComponentCorruption`: use that same pool for offers and added modifiers.
- `PickaxeEvents`: final-drop collection using the native inventory insertion path.

The authoritative influence is saved as a stable string in `examplemod:component_work.Influence`. Influencer-generated modifier records also store their influence context, reroll seed, and catalog revision 6. Original recovered seed, rarity, and excavation quality remain unchanged. Saved values and tiers are never reclassified on load. Unknown future influence IDs remain preserved and mutation is disabled until supported.

The native tests cover uninfluenced defaults, ordinary tags versus exclusives, all rarity/role pools, boosted distributions, zero foreign rolls and offers, immutable assignment and locks, influenced corruption/unveiling, native saves and block drops, assembly provenance, and Phasebound with empty/full/partially full inventories and real mining. Client checks cover permanent choice controls, disabled/corrupted states, native font bounds, and all seven station resources.
