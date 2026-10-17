# Archaeology: excavation and recovery

This is a reviewable first progression pass for a recurring feature, rather than final gear balance.

## Options considered

| Question | Options | Choice and reason |
| --- | --- | --- |
| Pressure | Countdown; tool stamina; condition | Untimed condition. Players can inspect and learn without latency or accessibility penalties. |
| Tool control | Selection buttons; inventory cursor | Keep the real inventory cursor and automatic return/swap. It already fits the table and works in multiplayer. |
| Progression | Hard material gates; faster copies; distinct control upgrades | Every rock works with starter tools. Upgrades improve precision, coverage and speed while each tool keeps its role. |
| Variation | Random rewards on completion; sealed specimens | Seal a seed, material and find when acquired. Save excavation on the item; moving it cannot reroll rewards. |
| Failure | Delete the find; unlimited resets; salvage | Low condition produces fragments. Recovering consumes one specimen; no free reset. |
| Enchantments | Many custom powers; vanilla support | Support vanilla durability enchantments first. Avoid inventing a second enchantment economy before playtesting tools. |

## Chosen loop

1. Sniffer debris digs and Cruncher geodes produce sealed artifacts, at their existing event frequency.
2. Place one artifact in the table. Its rarity and matrix are visible, but the component is concealed.
3. Pick up a tool. The lens outlines buried inclusions. Hammer bulk rock, drill close to the inclusion, brush the final cover.
4. Recovery requires the entire component exposed, or a ruined specimen. A dedicated output slot prevents accidental replacement or duplication.
5. Recover intact metal components at good condition; otherwise recover ancient fragments. Components and fragments purchase precision upgrades; resonant cores purchase resonant upgrades.

## Content and balance targets

- Common, uncommon, rare and epic specimens. Sniffer: 70/25/5/0%; Cruncher: 0/0/80/20%. Cruncher egg and ancient geode acquisition odds are unchanged.
- Sandstone, shale, deepslate, basalt and obsidian matrices. More prestigious finds generally have thicker, harder rock, with seeded irregular pockets.
- Pickaxe heads, sword blades, axe heads, resonant cores and alloy fragments. These are recovered components for future gear assembly, and already have a use as upgrade materials.
- Field tools remain inexpensive. Precision tools cost a recovered component/fragment and diamond. Resonant upgrades cost a resonant core and netherite scrap. No mandatory enchantment or rare tool gate.
- Hammer: broad and forceful, risky near a find. Drill: precise removal, upgraded depth stop. Brush: gentle finishing; higher tiers remove weathered cover over a wider area. Lens: progressively wider inspection, never damages anything.
- Three tiers change coverage, precision, cooldown and service life. Tool wear occurs only on a successful excavation stroke. Lens inspection is free.
- Condition is capped per stroke so one click cannot destroy a full component. Careful finishing can preserve 100%; quality is stored on recovered components.
- Ancient fragments provide a useful fallback, rather than a dead-end failure item.

## Safety and compatibility

The server owns excavation, cooldown, tool wear and recovery. Output-full recovery consumes nothing. Table range/menu ownership are checked. Old prototype excavation data retains its cover, scars and condition and receives a legacy fragment reward. Old unstarted artifacts can be studied normally. Table block identity, stored tools, existing item IDs and mob behavior remain compatible.

The interface stays within the existing 320 x 236 layout, keeps normal inventory item names, and adds no instructional or scan tooltips. Material, rarity, progress, condition and recovery are shown inline. The world table continues displaying stored tools and the specimen/recovered output.

## Deferred deliberately

Final equipment abilities, more bespoke component families, custom enchantments, timed challenges and competitive scoring should follow hands-on testing. The component profile and recorded quality leave room for future gear recipes without committing to their combat balance now.
