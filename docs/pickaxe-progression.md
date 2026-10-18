# Pickaxe progression and the temporary Ascender

Restored Ancient Pickaxes begin at **level 1**, reach **level 100**, and retain their level when moved, dropped, saved, repaired, or renamed. Other tools do not level yet. Older restored pickaxes start at level 1 on first use.

## Mining XP

- Successfully mining a suitable, non-instant block in Survival with the pickaxe awards **1 tool XP** through the native tool callback. Creative mining, attacks, and unsuitable blocks do not award tool XP.
- Going from level `L` to `L + 1` costs `20 + 2 × (L − 1)` XP. This is separate from the player's experience orbs and the Scholar modifier.
- Levels **10, 20, …, 100** require ascension. XP stops accumulating at these gates, with no overflow saved for later.
- The tenth ascension at level 100 completes the tool. No levels beyond 100 are possible.

## Modifier grades and growth

Every newly excavated modifier rolls its own **F, D, C, B, A, or S** scaling grade independently of its base strength. Higher-tier artifacts favor stronger grades; even Common parts can rarely roll an S. Grades remain fixed through assembly, leveling, saving, and ascension.

| Scaling | Growth per ascension | Growth per selected level-up |
| --- | ---: | ---: |
| F | +3% | +0.025% |
| D | +5% | +0.05% |
| C | +7.5% | +0.075% |
| B | +10% | +0.1% |
| A | +15% | +0.15% |
| S | +20% | +0.2% |

These percentages multiply the **original score-adjusted modifier value**, not the tool's current value. Each normal level-up selects **1–3 distinct modifier occurrences** with a positive score-adjusted base value, limited by how many the tool has. Every selected occurrence receives one tiny permanent increase. Repeated copies of a trait on different parts grow independently. A modifier may be selected again on later levels. Zero-quality rolls never become effective through growth.

```text
effective occurrence = raw roll × excavation score / 100
                     × (1 + ascensions × grade.ascensionGrowth
                          + selectedLevelUps × grade.levelGrowth)
```

For example, a +10% mining-speed roll at score 50 with S scaling contributes +5% initially, +6% after one ascension, and gains +0.01 percentage points whenever a normal level-up selects it. A B-grade copy instead gains +0.5 percentage points per ascension and +0.005 percentage points per selection. High base values and high grades work together. Contributions add, then the existing trait caps apply: Fortune III, 100% Silk/Smelter chance, and the existing speed/range/durability limits.

Level gains play Minecraft's level-up chime once per mining action. Ordinary XP, blocked gates, and Creative mining stay quiet. A level-up refreshes native durability and reach immediately when those modifiers grow. Maximum durability increases preserve accumulated damage.

## Item information and compatibility

Tooltips use non-italic text, a compact XP bar, colored assembly/part quality, and separate label/value colors. Value colors progress from muted/white through green, teal, violet, and gold as the effective value approaches that trait's cap. Scaling badges have their own F–S colors. An assembled trait with several grades shows their range, such as `[C-S]`; each part shows its exact per-modifier grade, raw roll, and level/ascension growth rates. Zero-effect rows are hidden on assembled tools; parts still show their raw rolls at quality zero.

Old modifiers without a saved grade use **B scaling**, preserving the previous +10% per ascension. Old tools retain their earned levels and ascensions, with no retroactive level bonuses or rerolled parts. Owned old items, including those in an open container, get a one-time tooltip refresh. Sealed modifier data stays version 1 with an optional `Scaling` field; new catalog records use revision 3. Growth counters and their saved random seed live separately in version 2 of `examplemod:pickaxe_progression`, leaving the original part provenance intact.

## Creative comparisons

The mod tab contains six **Scaling Test Pickaxe [F] … [S]** entries. They have identical base modifiers, perfect excavation scores, and the same saved growth seed, so their growth is directly comparable. Each starts at level 9, **one suitable Survival-mined block from level 10**: break stone to hear the sound and see the level bonus, then use the Ascender to compare the six ascension rates. These are fixed comparison tools; the existing 0/50/100-score Random Test parts still receive fresh random modifiers and grades when acquired.

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

- `PickaxeProgression`: level/ascension caps, XP curve, and the versioned `examplemod:pickaxe_progression` item data.
- `AscensionResources.BINDINGS`: one resource and base count per modifier; validates that every live trait has a mapping. Requirements are derived from sealed part rolls, with no new random rolls.
- `ModifierScaling`: grade rates, colors, and per-artifact-rarity grade weights.
- `PickaxeGrowth`: deterministic 1–3 selections per normal level, with per-occurrence counters and a saved seed.
- `PickaxeModifiers`: aggregates each roll's own grade, quality, and growth; native attributes refresh on levels and ascensions.
- `PickaxeTooltips`: shared styled presentation, magnitude colors, compact labels, and one-time legacy refresh.
- `PickaxeAscensionTests`: native mining, all ten gates, quality scaling, resource transactions, native inventory operations, saved data, and block-break drops. `AscenderVisualRegression` checks the real UI drawing plan and native assets.

`PickaxeGrowthTests` covers every normal level, independent grades and quality, deterministic future growth after item-codec serialization, legacy migration, and Creative comparison tools. `PickaxeLevelSoundTests` observes real native sound events during mining; `PickaxeTooltipRegression` checks the same styled components used by real lore against Minecraft's font and screen bounds.

These are initial balance values for testing; the Ascender is a temporary interface for the later ascension system.

