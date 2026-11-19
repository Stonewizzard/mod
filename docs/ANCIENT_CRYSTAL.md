# Ancient Crystal

Individual Ancient Crystals are Creative-only until archaeology acquisition is designed.

**Completed Ruined Ancient Forge:** 12 Ancient Crystals + 1 diamond block -> 1 Block of Ancient Crystal. Insert one item per fireplace click, in either order, light with flint and steel, wait the existing 20-second process, and collect with an empty hand when smoke turns white. No crafting-grid or reverse conversion recipe. Existing alloying and artifact melting continue to work.

The opaque full-cube block emits light level 7, uses amethyst sounds, needs an iron pickaxe or better, and drops itself. No extra anvil effects or world generation are added. Both items appear in the mod Creative tab; crystals also appear in Ingredients and blocks in Building Blocks.

Artwork uses explicit native 16px pixel sources under `tools/asset_sources/crystal`, broad teal facets, dark mineral outlines, and amber inclusions. `generate_crystal_assets.py` creates block models, assets and loot. The standard item finisher packs the item at exact nearest-neighbor 2x for existing 32px UI renderers; the asset manifest enforces opaque cutout pixels, normal scale and no pinholes.
