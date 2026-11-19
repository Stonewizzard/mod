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
in the **center**. Leave the other eight positions empty. Sixteen Iron Forge Hammer strikes make four
Runestone, a blank prepared deepslate base for later functional Ancient Runes. It has no crafting-grid recipe. Its
future role in runic structures is deliberately not implemented yet.

## Recipe authoring

Server datapacks provide `data/<namespace>/anvil_recipes/<name>.json`. The filename is
the stable recipe ID. Resource-pack client code is not involved in recipe decisions.

```json
{
  "anvil_tier": 0,
  "hammer_tier": 1,
  "work": 16,
  "xp_per_hit": 1,
  "ingredients": {
    "north_west": {"tag": "examplemod:runestone_deepslate"},
    "north_east": {"tag": "examplemod:runestone_deepslate"},
    "south_west": {"tag": "examplemod:runestone_deepslate"},
    "south_east": {"tag": "examplemod:runestone_deepslate"},
    "center": {"item": "minecraft:gold_ingot"}
  },
  "result": {"id": "examplemod:runestone", "count": 4}
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
are imposed yet. Tier 0/1 recipes continue to work. The guide and automatic construction advance through all tiers. Fluid spaces are
highlighted, but filled manually; no filled buckets are used.

Invisible legacy part blocks with no mesh are released when an anvil refreshes;
their serialized indices remain stable. Visible parts and the horn retain ownership.

## Tiers 3 and 4: the growing runic workshop

All measurements are relative to the core block (0,0,0), with x right and z back.
The existing Tier 2 foundation is y=-1. Concrete is a layout marker, never required.
The anvil requires every previous tier; breaking additions falls back to the highest
complete tier. Existing fluids and recipe-side constraints remain applicable.

**Tier 3:** in each diagonal, single Runestone blocks at (±4,-1,±4) and
(±5,-1,±5). Four 2×2 corner pillars occupy x=±6/±7, z=±6/±7:
four Runestone layers y=-1..2, capped by eligible blocks at y=3.
Additional cost: 72 Runestone + 16 cap blocks.

**Tier 4:** a hollow 17×17 square perimeter, max(|x|,|z|)=8 at y=-3.
A 3×3 Runestone base is centered 16 blocks from the core on each cardinal axis
at y=-4. Each base has a one-block-wide center column at y=-3..2, capped at y=3.
Connect the four side-pillar bases with a hollow circular Runestone outline of
radius 16 at y=-4. Its face-connected stepped perimeter contains 128 cells; twelve
are shared with the 3×3 bases, so it adds 116 unique Runestone requirements.
Additional Tier 4 cost: 240 Runestone + 4 cap blocks. All four pillars and the
complete circular outline are required. The inner square ring remains in place.

The main ring is two blocks below the Tier 2 foundation (one block lower than the
previous guide). The 3×3 side-pillar bases are three blocks below that foundation
(two blocks lower than the previous guide). Pillar cap heights stay unchanged;
two extra shaft blocks per pillar connect each lowered base.
The reference photo's three spaced markers between ring and side pillar are guides,
not required blocks. Geometry is deliberately editable if the reference measurements
need adjustment after in-game review.

### Editing requirements and future runes

- Measurements: `tools/generate_anvil_stages.py` generates
  `data/examplemod/anvil_stages/tier_3.json` and `tier_4.json`.
- Each stage contains groups of a block `tag` and integer `positions` [x,y,z].
  Datapacks can override those files; `/reload` refreshes requirements.
- `examplemod:ancient_anvil_runes` currently contains Runestone. Add future
  functional rune blocks to this tag to allow substitution without editing coordinates.
  No rune effects are implemented yet.
- `examplemod:ancient_anvil_caps` accepts iron, gold, copper, diamond, emerald,
  netherite and optional common metal storage tags. Different caps and blocks within
  a cap may be mixed. Packs can replace this tag.
- These structures are detected in place. Matching blocks, containers and functional
  rune block entities are never claimed or consumed.
- The guide and inventory-backed construction now advance through all five tiers.
- Recipes can now require `anvil_tier` 3 or 4. No new recipes or rune effects are
  imposed by this structural implementation.


## All-tier guide and inventory construction

Shift + right-click toggles the next-tier guide; holding places one matching inventory
block every six ticks after the initial short delay. Cyan = Runestone/rune positions,
gold = caps, blue = manually filled fluid spaces, yellow = wrong occupied positions.
The next tier appears automatically after the current one forms. Tier 5 completion
leaves no further guide cells. Repairs show only missing requirements.

The server chooses targets, block tags and inventory costs, retaining native placement
cancellation, permissions, collisions and sounds. Fluids and filled buckets are never
used automatically. Tier 2 construction waits for manual fluid completion before
unlocking Tier 3. Solid obstructions and containers must be cleared manually.

Pack-defined stage coordinates and tags sync to remote clients on login and datapack
reload, so dedicated-server guides use the same layout as authoritative construction.


## Tier 5: Grand Runic Canopy (concept D)

Tier 5 adds the floating canopy; it still requires every prior tier. Tier 6 is described in [ANCIENT_ANVIL_TIER6.md](ANCIENT_ANVIL_TIER6.md). The existing ground layout and block heights are unchanged.

- Main octagonal rim: 25-block span, with one-block-wide paths at y=16 relative to the anvil core. Diagonals follow a connected one-cell staircase.
- Open inner diamond and four narrow diagonal spokes: the same y=16 layer.
- Inset Canopy Keystones: eight on the rim, four on the diamond and four on the spokes. These replace frame cells; there are no raised nodes, pads or lower lip.
- The entire 25-cell central diamond opening stays clear above the anvil for future Tier 6 work.
- Exact structural cost: **156 Runestone and 16 Canopy Keystones** (55% fewer structural Runestone than the original 348).
- Existing wider canopy blocks are not deleted automatically. Use the updated guide to adjust an existing build; extra old blocks do not prevent recognition.

The keystone uses an explicit native 16px pixel source in
`tools/asset_sources/canopy/keystone.pixels`, the approved runic-anvil palette, a
normal cube block/item model and light level 12. Assets, tags, drops and recipe are
generated by `tools/generate_canopy_assets.py`. There are no extra rune abilities yet.

The Tier 4 anvil recipe consumes four Runestone in the corners, four ancient ingots
in the inner cardinal positions, and one glowstone dust in the center. Eight Iron
Forge Hammer strikes produce four Canopy Keystones. Leave the outer four positions
empty. This makes the material obtainable before constructing Tier 5.

`tier_5.json` is generated with the other stage definitions. Keystone positions use
`examplemod:ancient_anvil_keystones`; frame positions use the existing rune tag.
Guides sync the entire stage on login/reload, include its full elevated bounds, and
mark keystones amber. Inventory construction uses ordinary native block placement at
the elevated targets; it never consumes fluids or overwrites containers/solids.
Missing aerial blocks downgrade to the highest complete lower tier and become
repair targets. Blocks remain in place if the anvil is removed.


## Tier 6: Ancient Convergence

See [the Tier 6 reference](ANCIENT_ANVIL_TIER6.md) for energized tower caps, normal End Crystals, the uniform glass lens, guide behavior and harmless colored forging beams.

## Experience-powered smithing

Accepted hammer strikes spend player XP points, not levels. The default cost is one point per strike for every anvil recipe. `xp_per_hit` is a per-recipe integer override from 0 through 100000; omission uses 1 and 0 allows a free recipe. An Iron Forge Hammer needs 16 accepted strikes and 16 XP points for four Runestone. The four deepslate inputs and center gold ingot are unchanged.

Payment is checked on the server after recipe, tier, hammer and cooldown checks, before progress, durability or crafting effects. Insufficient XP leaves ingredients, progress and cooldown untouched. Gain XP to resume. Creative mode bypasses XP and durability costs. Guide construction and placing/retrieving ingredients are not hammer work. Current level/progress determines spendable XP; the lifetime `totalExperience` counter is not a wallet.

See [EXPERIENCE_ECONOMY.md](EXPERIENCE_ECONOMY.md) for the initial resource direction and tuning reference.
