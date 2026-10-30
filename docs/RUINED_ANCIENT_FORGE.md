# Ruined Ancient Forge — tier I

A repairable first step toward the ancient workshop. The hearth is a stone work basin with weathered copper bands; the surrounding masonry forms a foundation, two pillars, rear wall and broken-looking chimney. Tier I handles ancient-metal alloying and artifact melting. Equipment assembly belongs to the separate Reconstruction Table. No advanced editing, new mining ability, boss requirement or later-tier economy is implied by this first tier.

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

Complete the frame to automatically form one connected, weathered forge: a raised stone-and-bronze work pad, tapered supports, recessed soot-blackened firebox and broken-looking chimney. Each frame cell uses its own portion of a unified model, collision shape and purpose-made textures. Every face has explicit UV coordinates. Right-click any formed section to open the metallurgy screen directly. Stored input and output are kept in the core. Breaking the frame closes the menu and rejects processing without consuming stored inputs; repair restores access. Mining any formed section drops that section’s original masonry and restores the surviving frame. Mining the hearth returns the hearth and its stored inventory through native block drops, and restores the surviving frame. Original masonry states are saved on each formed section, including pack-provided states; formation never erases containers.

The current output, or first input when no output exists, appears on the work pad. The Reconstruction Table handles pickaxes and other equipment. The forge no longer offers equipment patterns. Previously stored components and completed tools remain withdrawable from all six slots.

## Ancient alloying

Open the completed forge. One Ancient Scrap or Ancient Debris, one gold ingot and two iron ingots produce four Ancient Ingots. The alloy screen uses separate typed inputs and a shared output; stacks and shift-click are supported. Remove any old parts and output first. All six stored slots, including legacy equipment from older worlds, are preserved. A missing frame or blocked output rejects a batch without consuming inputs. See [the Ancient Anvil reference](ANCIENT_ANVIL.md) for metal-block and anvil recipes.

## Melt artifacts

Place an artifact in the metallurgy screen’s left source slot and leave Gold/Iron empty. The action becomes **Melt**: one artifact produces one Ancient Scrap. All six artifact rarities are accepted, with one scrap each. The source artifact and any excavation progress are consumed only after validating the frame, empty spare slots and available scrap-output space. An existing ingot output must be collected first. An empty source never produces another scrap from the leftover output. See [Ancient Scrap](ANCIENT_SCRAP.md).

## Ghost construction guide

Right-click an **incomplete hearth with an empty hand** to open a small repair panel. **Show ghost layout** superimposes the eighteen-cell pattern in the world: cyan for missing masonry and amber for a space occupied by the wrong block. Place actual bricks to repair it. Correctly placed masonry disappears from the preview. Click the hearth again and use **Hide ghost layout** to turn it off; the preview also clears on formation, core removal or world change. It is client-local and never modifies the world or another player's display. Shift-right-click the hearth with an empty hand to inspect repair status.

The overlay deliberately shows required full-block placement cells rather than the finished sculpted geometry. It follows all four hearth facings and does not obscure existing correct frame blocks.

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

`tools/generate_ruined_forge.py` reproducibly authors the recipes, worldgen JSON and compressed template, then invokes `tools/generate_formed_forge.py`. The latter authors the connected block models, explicit UV mapping, facing states, and matching collision shapes from the same geometry. Four custom 32×32 textures depict weathered masonry, soot, aged bronze and cold ash. Both scripts preserve existing pickaxe-mining tag entries and run only during authoring.

## Future compatibility

The hearth's stable block ID, orientation and stored inventory data can remain the starting point for later tiers. Current structure validation is localized in `RuinedForgeStructure`; eligibility uses a masonry tag. Advanced forge functions, corruption, and Forgemaster materials need their own considered design. The Blood Magic-inspired expanding monument is now planned as a distinct **Ancient Anvil**, rather than a committed growth pattern for this forge.
