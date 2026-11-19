# Ancient Crystal

Individual Ancient Crystals and Energized Ancient Crystals are Creative-only for now. The normal item has the same approved silhouette with a palette darkened by 20%; the energized item preserves the original bright palette. There is no energized crystal item recipe yet. The energized item is not accepted by the normal crystal-block forge recipe.

**Completed Ruined Ancient Forge:** 12 Ancient Crystals + 1 diamond block -> 1 Block of Ancient Crystal. Insert one item per fireplace click, in either order, light with flint and steel, wait the existing 20-second process, and collect with an empty hand when smoke turns white. No crafting-grid or reverse conversion recipe. Existing alloying and artifact melting continue to work.

The opaque full-cube block emits light level 7, uses amethyst sounds, needs an iron pickaxe or better, and drops itself. No extra anvil effects or world generation are added. Both crystal items and both block variants appear in the mod Creative tab; both crystal items also appear in Ingredients and blocks in Building Blocks.

**Energized Ancient Crystal Block** keeps the approved lighter-blue amethyst palette as a separate block. It emits light level 12 and otherwise has the same mining/sound properties, dropping its own variant. It is Creative-only for now; no energizing recipe or additional effects have been invented. The original forge batch still produces the darker normal block.

Artwork uses explicit native 16px pixel sources under `tools/asset_sources/crystal`, a preserved vanilla-amethyst block pattern in blue-teal and gold, and the shared faceted crystal item silhouette. `generate_crystal_assets.py` creates block models, assets and loot. The standard item finisher packs the item at exact nearest-neighbor 2x for existing 32px UI renderers; the asset manifest enforces opaque cutout pixels, normal scale and no pinholes.
