# Replacement for PR #50

PR #50 was rejected by the user. The first commit on this branch restores the
pre-#50 tree, retaining the completed in-world fireplace forge from PR #49.
This replacement keeps the original artifact/tool/book designs; it does not reuse
#50's blanket procedural reskins.

## Actual defects and targeted repair

The original ingot occupied only 19 of its 32 canvas pixels and had 185 partially
transparent pixels. Scrap occupied 15 pixels and had 134 partial-alpha pixels.
Both could look tiny beside vanilla iron, and translucent cutout edges can produce
bad first-person extrusion. New ingot/scrap artwork retains the antique bronze/iron
identity and uses a 14x9 / 11x9 logical envelope, packed at exact 2x for existing UI
coordinates. Alpha is binary; silhouettes have no pinholes or floating pixels.

Original source art for the other generic items is preserved. Technical finishing
makes their cutout alpha solid, removes isolated one-pixel debris, keeps clear
borders, removes the unrelated footer specks from the shovel/spear heads, and gives artifacts a readable 24px extent. It preserves each item's RGB
artwork and shape rather than repainting tools/parts into one generic style.
Intentional fragment gaps and the resonance frame are documented exceptions.

Forge masonry, soot, copper and ash now reference Minecraft's actual deepslate
bricks, polished blackstone, cut copper and basalt textures. They share vanilla
pixel density and tiling. Forge geometry, direct interaction, processing and
chimney behavior are unchanged. The furniture keeps its original vanilla wood
bindings and physical item displays, with a vanilla green-wool work mat.

## Larger Ancient Anvil

The anvil spans three blocks along its facing axis, one block wide, and two owned
block layers. The work face is 1.5 blocks above its base. A broad heel, long stepped
horn, solid waist and heavy foot replace the compressed single-cell silhouette.
It uses recognizable vanilla metal/stone materials with one small gold maker's
seal. It has six actual owned cells with matching rotated selection/collision.

The recipe, item ID, stationary behavior, Tier 0, and centered 3x3 Tier 1 metal
foundation remain unchanged. New placements need clear space. Saved anvils remain
compact when blocked and expand safely when the surrounding space is cleared.
Formation never replaces containers or living entities. Mining any section removes
its other cells and yields exactly one anvil through native loot; foundation blocks
are preserved. Its item model is a uniformly scaled preview of the whole silhouette.

## Workflow

See [.agents/skills/minecraft-assets/SKILL.md](../.agents/skills/minecraft-assets/SKILL.md).
The replacement skill checks technical defects without granting permission to
replace an entire art style. Source PNGs are in `tools/asset_sources`; item role and
intentional-gap profiles are in `tools/item-asset-manifest.json`.

```sh
python -m pip install -r tools/requirements-assets.txt
python tools/finish_item_assets.py --check
python tools/audit_minecraft_assets.py
python tools/test_asset_guardrails.py
```

CI checks 44 item profiles and seven negative controls. Native Minecraft review adds
vanilla iron/gold comparison, main/offhand held cutouts, dropped ingot, original
artifact artwork, masonry tiling, and all four large-anvil orientations. Screenshots
remain in workflow artifacts instead of being embedded in large chat/log messages.
