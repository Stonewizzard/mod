# Archaeology playtest guide

## Getting started

Craft the existing archaeology table, drill, hammer and magnifying glass; a vanilla brush completes the field kit. Place an artifact in the left input slot. The next slot is the recovery output. Tools live in the four slots across the top. Pick a tool up onto your cursor; clicking another stored tool returns the held one to its matching slot and picks up the new one. Your hotbar and the Inventory tab work normally.

The board is untimed. Inspect with the lens, break thick rock with the hammer, work closer with the drill, and brush the final layer. Hammer strokes happen on deliberate clicks or when dragged to a new cell. Holding the drill or brush repeats strokes. A wider tool can affect several cells, so pay attention to the entire outline.

Recover unlocks when the component is fully exposed. Take the previous output first. Recovery consumes the artifact. Progress follows the specimen if you remove it, close the screen, or reload the world. There is no survival reset/reroll button.

## Finds

| Source | Common | Uncommon | Rare | Epic |
| --- | ---: | ---: | ---: | ---: |
| Sniffer excavating exposed ancient debris | 70% | 25% | 5% | — |
| Cruncher successfully crunching an ancient geode | — | — | 80% | 20% |

This changes which specimen drops, not how often those activities happen. Existing netherite scrap, egg chances, geode acquisition, and mob behavior remain intact.

Common finds contain sandstone or shale; uncommon contain shale or deepslate; rare contain deepslate or basalt; epic contain basalt or obsidian. Rock has seeded irregular thickness. A specimen's rock and inclusion are fixed; putting it back cannot change either.

Pickaxe heads, sword blades and axe heads can be recovered at any rarity. Rare finds have a 30% chance of containing a resonant core, rising to 60% for epic finds. Alloy inclusion chances decrease from 45% common to 25% uncommon, 15% rare and 5% epic. Alloy inclusions yield ancient fragments, which stack across specimens. Recovering below 60% condition also yields fragments instead of a usable part. A completely ruined specimen may be salvaged immediately. Better rarity and remaining condition increase fragment yield (one to six).

Components retain quality, original rarity, matrix and excavation provenance for future equipment systems. At 90% condition they are Pristine; 75–89% Fine; 60–74% Worn. These labels do not currently change combat stats. Restored equipment uses fixed, diamond-level attributes and 1,800 durability, and repairs with iron.

## Tool progression

| Tool | Field | Precision | Resonant |
| --- | --- | --- | --- |
| Drill | One cell, one layer, 4 ticks; risky at the final layer | One cell, 2 ticks, stops at the dust layer | Five-cell cross, 2 ticks, dust stop |
| Brush | 3×3 area, final layer only | 3×3 area, can also brush weathered depth 2 | Radius 2 disk, same gentle action |
| Hammer | Radius 2, removes 2 layers, 8 ticks | Radius 2, removes 3 layers, 6 ticks | Radius 3, removes 3 layers, 4 ticks |
| Lens | Radius 2 inspection | Radius 3 inspection | Radius 4 inspection |

Damage per stroke is capped at 8/5/3 for field/precision/resonant tiers. Brushes and lenses never damage the inclusion. A precision drill stops safely, but a strong hammer still requires care.

Successful work costs one durability. Field drill/hammer last 256/192 strokes; precision drill/brush/hammer last 768/256/640; resonant 1,536/512/1,280. The vanilla brush keeps vanilla durability. Inspection is free. Native Unbreaking and Mending apply to working tools. Mending repairs tools when held/equipped in the usual way; it does not draw experience into a table slot or GUI cursor. Repair working tools with iron at an anvil. Lenses do not wear out.

Use a smithing table:

- Precision: any ancient metal component or fragment in the template slot, matching field tool (or vanilla brush) in the base slot, diamond in the addition slot.
- Resonant: resonant core in the template slot, matching precision tool in the base slot, netherite scrap in the addition slot.

Native smithing keeps custom names and enchantments. Damage is retained, rather than offering free repairs.

Restored equipment recipes unlock when you obtain the respective component:

```
Sword       Pickaxe      Axe
 P           P           PI
 I          ISI           S
 S           S            S
```

P = matching ancient component, I = iron ingot, S = stick. Final gear abilities and custom enchantments are deliberately left for later review.

## Review checklist

Try a common sandstone specimen and an epic obsidian specimen, each with field and resonant tools. Compare hammer bulk removal, the drill's depth stop, brush finishing and lens reach. Test a careless salvage, a pristine recovery, taking output with shift-click, closing mid-study, and two players using one table. Existing prototype studies keep their progress and become legacy fragment finds.
