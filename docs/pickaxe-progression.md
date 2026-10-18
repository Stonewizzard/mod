# Pickaxe progression and the temporary Ascender

Restored Ancient Pickaxes begin at **level 1**, reach **level 100**, and retain their level when moved, dropped, saved, repaired, or renamed. Other tools do not level yet. Older restored pickaxes start at level 1 on first use.

## Mining XP

- Successfully mining a suitable, non-instant block in Survival with the pickaxe awards **1 tool XP** through the native tool callback. Creative mining, attacks, and unsuitable blocks do not award tool XP.
- Going from level `L` to `L + 1` costs `20 + 2 × (L − 1)` XP. This is separate from the player's experience orbs and the Scholar modifier.
- Levels **10, 20, …, 100** require ascension. XP stops accumulating at these gates, with no overflow saved for later.
- The tenth ascension at level 100 completes the tool. No levels beyond 100 are possible.

## Ascension effects

Each ascension adds **10% of the original quality-adjusted modifier strength**, up to +100% after ten ascensions. Bonuses add linearly rather than compound. Existing trait caps still apply: Fortune III, 100% Silk/Smelter chance, and the existing range/durability limits.

For example, a raw +10% speed roll on a 50-score part contributes +5% initially, +5.5% after one ascension, and +10% after ten. A zero-score part stays at zero. Original rolls, part scores, names, enchantments, and accumulated damage remain unchanged. Increased maximum durability does not reset damage.

## Using the Ascender

The **Ascender** is available in the mod's Creative tab and Functional Blocks. It intentionally has **no crafting recipe**. Right-click it, place a restored pickaxe in the tool slot, and put its required materials in the 18 storage slots. The right side shows available/required counts for the next ascension. Once the tool reaches its gate and all materials are present, it ascends automatically in the same slot. Shift-click works in both directions. Excess materials remain stored. Breaking the block returns its inventory.

Each occurrence of a modifier on any of the five parts has a cost, including repeated modifiers and rolls on zero-score parts. Costs are summed by resource, and multiplied by the next ascension number (1–10).

| Modifier | Resource | Base cost per occurrence |
| --- | --- | ---: |
| Mining speed | Redstone dust | 2 |
| Fortune | Lapis lazuli | 2 |
| Magnetized | Iron ingot | 2 |
| Silk Touch | String | 2 |
| Reinforced | Copper ingot | 2 |
| Scholar | Amethyst shard | 2 |
| Aquatic | Prismarine shard | 2 |
| Steady hands | Feather | 2 |
| Long reach | Ender pearl | 1 |
| Smelter | Blaze powder | 2 |

For example, two speed rolls cost 4 redstone for ascension 1, 8 for ascension 2, and 40 for ascension 10. All resource requirements must be satisfied together; partial payments consume nothing. Tools with no rolled modifiers can ascend without materials but gain no modifier benefits.

## Balance and persistence

- `PickaxeProgression`: level/ascension caps, XP curve, bonus per ascension, and the versioned `examplemod:pickaxe_progression` item data.
- `AscensionResources.BINDINGS`: one resource and base count per modifier; validates that every live trait has a mapping. Requirements are derived from sealed part rolls, with no new random rolls.
- `PickaxeModifiers`: applies ascension once after each part's score scaling, then clamps to the existing trait caps. Native reach and maximum durability refresh on ascension; level/XP and effective bonuses are visible in item information.
- `PickaxeAscensionTests`: native mining, all ten gates, quality scaling, resource transactions, native inventory operations, saved data, and block-break drops. `AscenderVisualRegression` checks the real UI drawing plan and native assets.

These are initial balance values for testing; the Ascender is a temporary interface for the later ascension system.
