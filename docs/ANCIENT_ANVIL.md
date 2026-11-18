# Ancient Anvil

The selected Foundry anvil is a broad runic centerpiece with four animated lava falls,
an extended working surface and a long tapered horn. A four-by-four footprint spans
two owned block layers. Body meshes, the cell-clipped horn, collision and selection
edges share `tools/expanded_anvil.py`; approved artwork remains in its native pixel grid.

The original six/eighteen cell indices and frozen meshes remain valid in saved worlds.
A saved anvil expands only when all extra cells are clear of blocks, containers and
living creatures. It never replaces neighboring inventories or the foundation. A new
placement requires the whole footprint clear. Breaking any section recovers one anvil
and all its workpieces once; its foundation remains intact. Design A is the normal item.

## Progression

| Step | Requirement / result |
|---|---|
| Repair the forge | Complete the Ruined Ancient Forge frame |
| Alloy ancient metal | 1 Ancient Scrap or Ancient Debris + 1 Gold Ingot + 2 Iron Ingots → 4 Ancient Ingots |
| Ancient metal block | 9 ingots ↔ 1 Block of Ancient Metal |
| Craft anvil | Vanilla anvil shape: `BBB / .I. / III`, B = ancient metal block, I = ancient ingot; 31 ingots total |
| Tier 0 | Place the Ancient Anvil; no foundation needed for basic smithing |
| Tier 1 | All 25 cells of a centered 5×5 Runestone foundation directly below the owned footprint |

All 25 foundation positions require Runestone. Metal blocks no longer qualify. The
foundation stays intact when the anvil is recovered. Removing one required block
downgrades the anvil within half a second.

Tap **Shift + right-click** on any anvil section, with any held item, to toggle the
local guide directly. No inventory or status screen opens. The ghost cells are one
block tall: cyan for empty positions and amber for solid obstructions. Hold the same
buttons for half a second to begin placing inventory Runestone, one block every six
ticks. Releasing use, stopping sneak or looking away stops construction. Solid blocks
and containers are preserved; clear them manually. Native placement permissions,
collision and mod cancellation apply. Survival consumes one Runestone per placement;
Creative needs Runestone in inventory and follows normal Creative item costs.

The guide remains on until toggled off or the world/target unloads. A blocked legacy
anvil also shows the cells needed for safe expansion. Only the local player sees it.

## Physical smithing

Thirteen independent positions hold one workpiece each: center, four diagonal corners,
four inner cardinal arms and four outer rim/heel positions. A gold outline identifies
the spot under the crosshair. Positions rotate with the model.

- Right-click a position with an item to place one workpiece.
- An empty hand retrieves it; a different held item swaps with it safely.
- Hit any anvil section with a forge hammer to work the matching recipe. Left-click
  strikes and right-click use are both supported. Hold left-click to strike repeatedly.
- Every accepted hit costs one hammer durability. Invalid recipes and clicks inside
  the eight-tick strike cooldown consume no durability or ingredients.
- Ingredients stay visible until the final hit. Moving an ingredient resets progress.
- The completed result appears at center; right-click there to collect it. That click
  never consumes a held item, even while holding another ingredient or the hammer.
- Placed items, output and partial progress save with the anvil. There is no inventory UI.
- Shift + right-click is reserved for the guide and foundation construction, including with a tool held.

### Iron Forge Hammer

Craft `III / .S. / .S.` with iron ingots and sticks. The hammer has 250 durability,
forge tier 1 and one work per strike. Its class supports future tiers and work rates.
The native 16px sprite is packed at exact 2× nearest-neighbor with binary alpha.

### Runestone (Tier 0+)

Put deepslate or cobbled deepslate in the **four diagonal corners** and one gold ingot
in the **center**. Leave the other eight positions empty. Four hammer strikes make one
Runestone, a blank prepared deepslate base for later functional Ancient Runes. It has no crafting-grid recipe. Its
future role in runic structures is deliberately not implemented yet.

## Recipe authoring

Server datapacks provide `data/<namespace>/anvil_recipes/<name>.json`. The filename is
the stable recipe ID. Resource-pack client code is not involved in recipe decisions.

```json
{
  "anvil_tier": 0,
  "hammer_tier": 1,
  "work": 4,
  "ingredients": {
    "north_west": {"tag": "examplemod:runestone_deepslate"},
    "north_east": {"tag": "examplemod:runestone_deepslate"},
    "south_west": {"tag": "examplemod:runestone_deepslate"},
    "south_east": {"tag": "examplemod:runestone_deepslate"},
    "center": {"item": "minecraft:gold_ingot"}
  },
  "result": {"id": "examplemod:runestone", "count": 1}
}
```

Other position IDs: `north_inner`, `east_inner`, `south_inner`, `west_inner`,
`north_outer`, `east_outer`, `south_outer`, `west_outer`. Unspecified positions must
be empty. Each ingredient specifies exactly one item ID or item tag. A datapack can
override a recipe, replace tags, or disable it with `"enabled": false`. `/reload` picks
up changes. Changed recipe IDs reset saved work on the next strike. Modpacks should
also reset work or use a new ID when changing work costs. Server-side hammer packets
validate the held tool, reach, line of sight and the authoritative cooldown.

The content registry is `AncientAnvilContent`; storage/sync is
`AncientAnvilBlockEntity`; controller logic is `AnvilWork`; reloadable recipes are
`AnvilRecipes`; all coordinates are in `AnvilSlot`. New hammer tiers reuse
`ForgeHammerItem`. Client rendering resolves real item models in the render extraction
phase. The anvil is not a hopper inventory.

The forge still performs only in-world alloying and artifact melting. Tool assembly
stays at the Reconstruction Table. See [Ancient Scrap](ANCIENT_SCRAP.md).

## Verification

Asset audits check the native item grid, binary alpha, holes, margins, per-cell body
and horn bounds, UV bounds, closed complete horn hull and frozen legacy indices.
Server GameTests cover the 5×5 Runestone foundation in four facings, all 128 anvil recovery
paths, container/entity protection, legacy expansion, thirteen-position interaction in
four rotations, exact ingredient matching, saved progress/output, hammer cooldown and
durability, center result retrieval, and dropped inventory recovery. Native-client QA
drives the real Minecraft window to place the item, use the foundation guide, place
and retrieve all thirteen positions, strike with both mouse buttons and collect a
Runestone. Actual window captures accompany CI artifacts.

## Tier 2: directional fluid basins

Keep the Tier 1 5×5 Runestone foundation. Everything below is in that same layer,
one block below the anvil's placement block. Directions are relative to the anvil:
front points toward its horn; right/left are viewed from behind the anvil.

For precise placement, each side extends out from the center: distance 3 has five
fluid cells (across -2..2), distance 4 has three (-1..1), distance 5 has one (0).
The front differs: replace its center cells at distances 3, 4 and 5 with Runestone.
This leaves two pools of three fluid blocks each. Both must be the same fluid.
At distance 6, place one Runestone tip on every side, including the front.
The stepped border lies at (distance, across) (3,±3), (4,±2), (5,±1).
All borders and the bridge use Runestone (the foundation block tag).

Each complete side independently detects its fluid, including registered mod fluids.
Source and flowing variants agree. Empty cells, waterlogged blocks and fluid-containing
block entities are not basin cells. The four sides need not match each other.
Fluid is not consumed. Removing part of a pool or its border downgrades to Tier 1;
breaking the inner foundation downgrades to Tier 0.

Tier 2 recipes are datapack files in `data/<namespace>/anvil_recipes/`.
The optional `fluids` object maps `front`, `right`, `back`, `left` to exactly one
`fluid` ID or `tag` per specified side. Unspecified sides accept any consistent fluid.
A recipe with fluid conditions implicitly requires at least Tier 2.

```json
{
  "anvil_tier": 2,
  "hammer_tier": 1,
  "work": 4,
  "ingredients": {"center": {"item": "minecraft:gold_ingot"}},
  "fluids": {
    "front": {"fluid": "minecraft:water"},
    "right": {"tag": "minecraft:lava"}
  },
  "result": {"id": "minecraft:gold_nugget", "count": 9}
}
```

This example is documentation, not an enabled recipe. No new progression recipes
are imposed yet. Tier 0/1 recipes continue to work. The placement guide and automatic
construction currently cover the original Tier 1 foundation only.

Invisible legacy part blocks with no mesh are released when an anvil refreshes;
their serialized indices remain stable. Visible parts and the horn retain ownership.
