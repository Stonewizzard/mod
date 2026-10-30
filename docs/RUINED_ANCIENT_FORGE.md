# Ruined Ancient Forge — tier I

A repairable first step toward the ancient workshop. The hearth is the recessed back-center fireplace with weathered copper bands; the surrounding masonry forms a foundation, two pillars, rear wall and broken-looking chimney. Tier I handles ancient-metal alloying and artifact melting. Equipment assembly belongs to the separate Reconstruction Table. No advanced editing, new mining ability, boss requirement or later-tier economy is implied by this first tier.

## Acquisition

Implemented: two routes to the same recoverable hearth.

1. **World discovery.** A small ruined workshop may appear in eligible newly generated Overworld terrain. It contains the hearth and fifteen of the seventeen required bricks. Repair the two missing bricks or relocate the hearth and salvage the masonry. The structure has no unique loot chest or guardian.
2. **Reconstruction recipe.** Craft the hearth with four Ruined Forge Bricks, two copper blocks, two iron ingots, and a blast furnace. The frame's bricks are crafted four at a time from four items in `minecraft:stone_crafting_materials`. This route needs no particular biome, new chunks, archaeological luck, revived mob, vault, or boss. Packs can replace the normal vanilla ingredients as needed.

The structure set uses random spread with spacing 48 chunks and separation 24; these are placement parameters, not a promised distance to a find. Biome and terrain eligibility can further reduce actual appearances. The crafting route is intentionally available without a long structure hunt.

Other options for later discussion: a bounded salvage project from archaeological finds; a cartographer/survey map to an existing ruin. Neither is implemented here. A map alone would not solve custom-worldgen compatibility, and an unbounded random fragment hunt would defeat the fallback's purpose.

## Build layers

Read each row from the front of the hearth toward the rear. `B` is a Ruined Forge Brick; `H` is the hearth. The hearth's opening faces the player. The whole pattern rotates with that facing.

```text
Foundation       Hearth level     Hearth + 1       Hearth + 2
BBB              ...              ...              ...
BBB              B.B              B.B              ...
BBB              BHB              .B.              .B.
```

Total: **one hearth + seventeen masonry blocks**. The occupied footprint is three wide by three deep, and four high including the foundation. Extra decoration is allowed; only the listed masonry is required. Do not block the interaction face.

Complete the frame to form the existing weathered stone-and-bronze forge. The fireplace core replaces the former back-center masonry cell, and the front table is removed. The other seventeen formed-cell models, textures, explicit UVs and collision geometry are unchanged. Ingredients and finished output appear inside the fireplace. The forge has no processing screen or repair panel. Equipment remains at the Reconstruction Table.

Breaking a frame section returns its original masonry and restores surviving cells. Breaking the core returns its inventory through native drops. Stored original block states remain intact, including pack-provided masonry. Formation does not overwrite containers.

## In-world processing

1. Right-click inside the fireplace once per ingredient item: **1 Ancient Scrap or Ancient Debris + 1 gold ingot + 2 iron ingots**. Inputs appear inside it.
2. Use **flint and steel** on the fireplace. One successful ignition costs one durability; repeated clicks do not consume more or reset the timer.
3. **Black smoke** rises from the chimney during the **20-second** process. Flames appear in the firebox without placing world fire.
4. **Pure white smoke** signals completion. Right-click with an empty hand to collect **4 Ancient Ingots**. White smoke stops when output is collected.

To melt an artifact, insert **one artifact alone**, ignite, and collect **one Ancient Scrap** after twenty seconds. All six rarities are accepted. The alloy recipe and recycling quantities are unchanged.

An empty-hand click retrieves the output first, then staged inputs/legacy storage one stack per click. Uncollected output, spare parts and incompatible recipes prevent ignition. Held items are never consumed on a rejected interaction. Materials stay reserved in their original slots during heating; they cannot be removed or automated out. Only the finishing tick consumes the exact recipe and creates output. Destroying the core before completion returns raw ingredients rather than granting an instant finished alloy. Missing or unloaded frame cells pause progress, and repair resumes it. Phase, remaining ticks, all item data and completed output survive save/reload.

## Old-world migration

Saved front-table cores are recognized by the missing pre-update layout version. They relocate two blocks back into their existing fireplace, preserve all six inventory slots and item metadata, and return the one displaced masonry block once. Stable cell indices keep all other formed sections in their original world positions. Migration is idempotent. If the target contains an unexpected block or container, relocation waits instead of overwriting it. Clear that target and click or reload to retry. New block placements and new ruins already use the back-center core.

## Ghost construction guide

Empty-hand right-click an **incomplete fireplace** to toggle the local ghost guide directly. Cyan shows the seventeen missing frame positions; amber shows incorrect occupied positions. Click again to hide. It clears on formation, core removal or world change. No inventory or repair screen opens, and the preview never modifies server blocks.

The overlay deliberately shows required full-block placement cells rather than the finished sculpted geometry. It follows all four hearth facings and does not obscure existing correct frame blocks.

## Testing commands

- `/give @s examplemod:ruined_ancient_forge`
- `/give @s examplemod:ruined_forge_bricks 17`
- `/locate structure examplemod:ruined_ancient_forge`
- `/place structure examplemod:ruined_ancient_forge` — uses the native jigsaw structure placement.
- `/place template examplemod:ruined_ancient_forge` — places the authored damaged workshop template for inspection.

Forge and masonry also appear in the mod creative tab. The hearth is in Functional Blocks; masonry is in Building Blocks. The Workshop Manual includes the layer layout, recipes, acquisition and repair behavior.

## Pack customization

All paths are under `data/examplemod/` unless otherwise stated.

| Purpose | Resource |
|---|---|
| Replace/remove fallback recipe | `recipe/ruined_ancient_forge.json` |
| Replace masonry recipe | `recipe/ruined_forge_bricks.json` |
| Accept alternate frame blocks | `tags/block/ruined_forge_masonry.json` |
| Change eligible biomes | `tags/worldgen/biome/has_structure/ruined_ancient_forge.json` |
| Change placement spacing/separation | `worldgen/structure_set/ruined_ancient_forge.json` |
| Change structure settings | `worldgen/structure/ruined_ancient_forge.json` |
| Change prefab selection | `worldgen/template_pool/ruined_ancient_forge.json` |
| Replace the ruin | `structure/ruined_ancient_forge.nbt` |

To disable natural discovery in a pack, override the biome tag with `{"replace":true,"values":[]}`. The recipe and player-built forge remain usable. A custom dimension/biome generator can opt its own biomes into the tag. Packs with unusual resources should replace the fallback recipe; ordinary ingredients do not guarantee availability in every possible pack.

`tools/generate_ruined_forge.py` reproducibly authors the recipes, worldgen JSON and compressed template, then invokes `tools/generate_formed_forge.py`. The latter authors the connected block models, explicit UV mapping, facing states, and matching collision shapes from the same geometry. Four custom 32×32 textures depict weathered masonry, soot, aged bronze and cold ash. Both scripts preserve existing pickaxe-mining tag entries and run only during authoring.

## Future compatibility

The hearth's stable block ID, orientation and stored inventory data can remain the starting point for later tiers. Current structure validation is localized in `RuinedForgeStructure`; eligibility uses a masonry tag. Advanced forge functions, corruption, and Forgemaster materials need their own considered design. The Blood Magic-inspired expanding monument is now planned as a distinct **Ancient Anvil**, rather than a committed growth pattern for this forge.
