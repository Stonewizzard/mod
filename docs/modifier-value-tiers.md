# Modifier value tiers

Each modifier family owns an independent list of value tiers. A family may have one, three, nine, or any other positive number of tiers. T1 denotes the strongest initial band in the current catalog. Artifact rarity (Common–Unique), a modifier's value tier (T1, T2, …), and its F–S growth grade are separate properties.

## Roll order and probabilities

1. Artifact rarity determines modifier-count attempts, with the existing extra-roll chances.
2. Choose an eligible modifier family using its component-role weight. Previously selected families and conflicting harvest families are removed.
3. Within that family, choose a value tier using its weight for the artifact's rarity. A zero weight makes that tier unavailable.
4. Roll the raw value uniformly inside the chosen band's minimum and maximum.
5. Independently roll the modifier's F–S scaling grade using the existing rarity-based grade weights.
6. Apply the component's quality to its raw value when calculating effectiveness; normal level and ascension growth continue to use its F–S grade.

For one successful modifier attempt:

`P(family + tier) = family weight / total eligible family weights × tier weight / total eligible tier weights within that family`

Adding tiers does not increase a family's overall frequency. Family weights and tier weights are deliberately separate balance controls. Probabilities change after exclusions; the chance that an entire multi-modifier component contains a trait is not simply its first-attempt probability. Ancient replacement is a later operation and can replace a normal roll with a hidden slot.

## Initial mining-speed balance

These probabilities are **conditional on selecting Mining speed**, before quality or growth:

| Value tier | Raw mining speed | Common | Uncommon | Rare | Epic | Legendary | Unique |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: |
| T6 | +4–12% | 94% | 65% | 35% | 15% | 10% | 5% |
| T5 | +12–22% | 5% | 28% | 40% | 20% | 15% | 10% |
| T4 | +22–40% | 1% | 6.5% | 22% | 30% | 20% | 15% |
| T3 | +40–65% | — | 0.5% | 2.8% | 25% | 25% | 20% |
| T2 | +65–90% | — | — | 0.2% | 9% | 20% | 25% |
| T1 | +90–120% | — | — | — | 1% | 10% | 25% |

Common can reach +40%, while heavily favoring +4–12%. Legendary can roll every band, retaining weak outcomes while favoring stronger ones. Higher rarity does not guarantee a stronger individual component.

A Common left tip has Mining speed family weight 32 out of 89. Its chance to roll T4 speed is therefore approximately `32/89 × 1% = 0.36%`, before ancient replacement (which Common cannot get). Its chance for any speed tier remains approximately 35.96%.

## Other current families

| Modifier | Value tiers | Minimum artifact rarity |
| --- | ---: | --- |
| Mining speed | 6 | Common |
| Mining XP | 6 | Common |
| Fortune | 5 | Common |
| Durability | 5 | Common |
| Magnetized | 4 | Uncommon |
| Underwater recovery | 4 | Uncommon |
| Airborne recovery | 4 | Uncommon |
| Block reach | 4 | Rare |
| Silk Touch chance | 3 | Rare |
| Smelting chance | 3 | Epic |

Each family's bands and weights are explicit in the catalog. The per-component role pools still apply. Fortune, Silk Touch, and Smelting remain mutually exclusive on one piece. All bands of an eligible family remain possible on Legendary and Unique artifacts.

## Editing and extending

`ArtifactModifierCatalog.java` centralizes:

- `RARITIES`: display names and independent modifier-count attempts.
- `SPECS`: each trait's minimum artifact rarity and its complete list of value tiers.
- `weights(part)`: modifier family weights for each component role.

A catalog tier entry such as:

```java
t(6, .04, .12, 940, 650, 350, 150, 100, 50)
```

means T6, +4–12%, with weights for Common, Uncommon, Rare, Epic, Legendary, Unique in that order. Weights are relative and do not have to total 100 or 1000. Set a rarity's weight to zero to exclude that band there. There is no tier-count limit and no assumption that every modifier shares a ladder.

`ModifierTier` keeps a stable tier ID separate from its display rank. The convenience helper above generates ID `t6`; use the full constructor when a different stable ID is useful. Do not reuse an existing tier ID for a different concept. Definition validation rejects duplicate tier IDs/ranks, invalid ranges, negative weights, and wrong rarity-weight lengths. Zero-weight bands may remain defined for later balancing. A family with no eligible bands is excluded entirely. Weight totals use 64-bit arithmetic.

Natural excavation, Creative random parts, ancient unveiling offers, and corruption-added modifiers all use the same tier sampler. Existing grade, quality, runtime-cap, and extra-modifier rules remain separate. This change adds no new modifier abilities.

## Saves, customization, and presentation

Catalog revision 5 adds optional `ValueTier` and `TierRank` to each version-1 modifier roll. The original raw value, quality scale, scaling grade, tier ID, and displayed tier rank are saved. Loading does not look up the current catalog to reinterpret an old value.

- Existing pieces and assembled tools are never rerolled.
- Old rolls without tier metadata stay unclassified; they are not silently assigned tiers.
- Unknown or retired tier IDs and ranks above six remain readable and preserved.
- Existing saved ancient offers remain selectable with their exact old values/grades.
- New ancient offers save their individual tiers. Reopening the Unveiler cannot reroll them.
- Scaling rerolls change only F–S grades. Restoration changes only quality.
- Locks protect the entire roll, including its tier metadata.
- Corruption retains an existing roll's **origin tier** even if its strength is increased or reduced beyond the original band. An added modifier gets a freshly sampled legal tier.
- Reconstruction copies the exact finalized component records into the tool.

Piece lore displays the rolled tier beside the base value. Station cards and unveiling offers display it beside the raw value, separately from the F–S badge. Finished-tool aggregate effects still combine component contributions rather than pretending the combined statistic has one tier.

## Validation

The native GameTests cover all six rarity distributions, uniform values inside bands, independent growth grades, equal family frequency with one versus nine tiers, zero-weight exclusions, large weight totals, saved legacy/unknown tiers, old and new unveiling offers, workshop mutations, and assembled-item serialization. Existing native mining, progression, Creative acquisition, lock, corruption, and container tests remain enabled. Client checks exercise tier labels, legacy lore, ranks above six, actual Minecraft font bounds, and station hit regions.
