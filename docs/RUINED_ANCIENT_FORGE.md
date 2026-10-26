# Ruined Ancient Forge — tier I

A repairable first step toward the larger ancient forge. The hearth is a stone work basin with weathered copper bands; the surrounding masonry forms a foundation, two pillars, rear wall and broken-looking chimney. Tier I uses existing reconstruction patterns and retains each component's quality, modifiers and provenance. No advanced editing, new mining ability, boss requirement or later-tier economy is implied by this first tier.

## Acquisition

Implemented: two routes to the same recoverable hearth.

1. **World discovery.** A small ruined workshop may appear in eligible newly generated Overworld terrain. It contains the hearth and sixteen of the eighteen required bricks. Repair the two missing bricks or relocate the hearth and salvage the masonry. The structure has no unique loot chest or guardian.
2. **Reconstruction recipe.** Craft the hearth with four Ruined Forge Bricks, two copper blocks, two iron ingots, and a blast furnace. The frame's bricks are crafted four at a time from four items in `minecraft:stone_crafting_materials`. This route needs no particular biome, new chunks, archaeological luck, revived mob, vault, or boss. Packs can replace the normal vanilla ingredients as needed.

The structure set uses random spread with spacing 48 chunks and separation 24; these are placement parameters, not a promised distance to a find. Biome and terrain eligibility can further reduce actual appearances. The crafting route is intentionally available without a long structure hunt.

Other options for later discussion: a bounded salvage project from archaeological finds; a cartographer/survey map to an existing ruin. Neither is implemented here. A map alone would not solve custom-worldgen compatibility, and an unbounded random fragment hunt would defeat the fallback's purpose.

## Build layers

Read each row from the front of the hearth toward the rear. `B` is a Ruined Forge Brick; `H` is the hearth. The hearth's opening faces the player. The whole pattern rotates with that facing.

```text
Foundation       Hearth level     Hearth + 1       Hearth + 2
BBB              .H.              ...              ...
BBB              B.B              B.B              ...
BBB              BBB              .B.              .B.
```

Total: **one hearth + eighteen masonry blocks**. The occupied footprint is three wide by three deep, and four high including the foundation. Extra decoration is allowed; only the listed masonry is required. Do not block the interaction face.

Click an incomplete hearth to see the current masonry count and first section requiring repair. Complete it to open the equipment-pattern screen. Stored input and output are kept in the core. Breaking the frame closes the menu and rejects assembly without consuming parts; repair restores access. Mining the hearth returns the hearth and its stored inventory through native block drops.

The current output, or first input when no output exists, appears on the work pad. The wooden Reconstruction Table remains available; the forge uses the same patterns and assembly semantics while establishing the physical multiblock foundation for future tiers.

## Testing commands

- `/give @s examplemod:ruined_ancient_forge`
- `/give @s examplemod:ruined_forge_bricks 18`
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

`tools/generate_ruined_forge.py` reproducibly authors the default models, recipes, worldgen JSON and compressed template. It uses vanilla texture references, retains the existing pickaxe-mining block tag entries, and creates no new image assets. It is an authoring tool, not something executed on player machines.

## Future compatibility

The hearth's stable block ID, orientation and stored reconstruction data can remain the starting point for later tiers. Current structure validation is localized in `RuinedForgeStructure`; eligibility uses a masonry tag. Larger tiers, specialized upgrade slots, corruption, and Forgemaster materials need their own considered design rather than being represented by unused settings now.
