---
name: minecraft-assets
description: Author and validate Stonewizzard/mod generic item sprites, block materials and their models with vanilla Minecraft pixel scale.
---

Use this workflow when adding or changing generic items, block textures or item models.
Read tools/generate_pixel_assets.py and tools/check_pixel_assets.py first.

1. Author on a **16×16 logical grid**. Existing UI code expects 32×32 PNGs, so export each logical pixel as exactly 2×2 pixels with nearest-neighbor scaling. A 32px file is not permission to add 32px detail. Never blur or antialias.
2. Use a small, named palette (maximum 24 colors), broad shade clusters, a clear silhouette and consistent upper-left highlights. Compare stone against vanilla stone bricks; compare metal against vanilla ingots. Avoid photographic noise, gradients and dense rune wallpaper.
3. Generic items use binary alpha: 0 or 255. Keep a one-logical-pixel clear margin, an 11–14px longest dimension and intentional proportions. Ingots should occupy about 14×8px, not a small patch in a large transparent canvas. Narrow handles remain narrow; do not stretch them to fill the square.
4. Draw closed silhouettes. Check enclosed transparent pixels and detached flecks. A ring or separate fragments require an explicit, documented validator exception, not disabling the check globally. Inspect held items as well as flat icons: partial alpha and tiny holes can cause bad extrusion sides.
5. Add the asset to the deterministic generator; do not hand-patch its generated PNG. New shape families should have their own authored silhouettes. Components must not contain leftover handles from assembled tools. Reuse a palette and lighting style without erasing functional differences between items.
6. Use `minecraft:item/generated` for ordinary ingredients and `minecraft:item/handheld` when appropriate for tools. Connect the modern `items/<id>.json` to the correct model. Avoid per-item GUI/hand scale overrides as a substitute for fixing sprite occupancy. Block faces use explicit UVs at one logical texel per model unit. Generate collision and geometry together.
7. Run:
   ```sh
   python -m pip install -r tools/requirements-assets.txt
   python tools/generate_pixel_assets.py
   python tools/generate_pixel_assets.py --check
   python tools/check_pixel_assets.py
   python tools/test_pixel_asset_checks.py
   ```
8. Review a nearest-neighbor contact sheet on light and dark backgrounds. Run the repository build/client checks and Real Minecraft visual QA. Inspect a hotbar beside a vanilla ingot, held items in both hands, dropped items, block tiling beside vanilla blocks, and all four custom-model facings. State explicitly when native client review is unavailable.

Multiblock structures still require owned-cell placement/removal, rotation, saved-world compatibility, collision and in-game iteration. Do not enlarge a single block's render model into unowned neighbor cells to fake a multiblock. Preserve existing registry IDs and gameplay when doing an art pass. Never merge PRs automatically.
