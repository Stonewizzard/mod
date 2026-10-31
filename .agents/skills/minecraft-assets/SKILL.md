---
name: minecraft-assets
description: Preserve this mod's artwork while preventing Minecraft alpha, scale, UV and owned-cell model defects.
---

Use this for generic items, block materials and their models. The user rejected PR #50's
blanket sprite replacement. Do not reapply that art style or repaint every item with one
procedural silhouette. Reuse approved artwork and make a targeted change first.

## Start with evidence

Read tools/finish_item_assets.py, tools/audit_minecraft_assets.py and
 tools/item-asset-manifest.json. Inspect the original source and the actual native held
item beside vanilla iron before deciding a shape is bad. Ask for screenshots only if
necessary; large chat images have caused a 4 MiB request limit. Keep full captures in
workflow artifacts, not base64 logs.

## Generic items

- Prefer a native 16px art grid for new ordinary materials, with clear clustered shading
  and a silhouette appropriate to its role. Keep the existing detailed artifact/tool
  artwork unless the user explicitly requests a redesign. Existing 32px artwork is an
  intentional compatibility choice, not a reason to flatten it into anonymous shapes.
- Existing UI renderers use 32px image coordinates. Pack native 16px sprites at exact 2x
  nearest-neighbor; keep intentional legacy detail at 32px. Never blur or antialias.
- Ordinary generated item sprites need alpha 0 or 255. Tiny translucent pixels can look
  fine in a flat preview and produce missing extrusion faces in hand.
- An ingot should be close to vanilla's 14x9 logical silhouette. Scrap can be smaller
  (about 11x9). Handles stay thin. Check the item's visible bounds, not just file size.
  Do not use arbitrary first-person or GUI scale overrides to hide a tiny sprite.
- Keep a transparent border. No accidental interior pinholes or isolated one-pixel
  flecks. Genuine separated fragments/open frames need an explicit reason in the
  manifest; do not generate exceptions automatically to make a failure disappear.
- Preserve source artwork in tools/asset_sources. The finisher is technical cleanup,
  not a new art director. Do not repaint its output by hand. Add the modern item/model
  wiring and an appropriate profile for every new generic sprite.

## Block materials and custom structures

Prefer Minecraft's actual 16px material references for familiar stone, iron, wood and
copper. A custom material should add recognizable identity, not photographic noise.
UVs use model units consistently, with no stretched rune wallpaper on tiny faces.
The enlarged Ancient Anvil uses six owned cells. Never put a large render mesh through
unowned blocks. Generate its meshes and per-cell collision together. Test all four
orientations, occupied space, old-world fallback, harvesting any part, and exactly one
item drop. Do not overwrite neighboring containers or foundation blocks.

## Required checks and review

```sh
python -m pip install -r tools/requirements-assets.txt
python tools/finish_item_assets.py --check
python tools/audit_minecraft_assets.py
python tools/test_asset_guardrails.py
```

After editing source art, run the finisher without --check, then inspect a nearest-
neighbor before/after sheet. Run the repository's build/client tests and actual Minecraft
window QA. Review hotbar beside vanilla ingots, main/offhand extrusion, dropped items,
block tiling beside vanilla masonry, and all anvil orientations. A passing validator
is not proof that the art looks good. State any missing visual verification honestly.
