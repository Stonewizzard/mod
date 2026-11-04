# Warm ruined workshop art pass

The forge keeps PR #51's complete silhouette, fireplace interaction, chimney and
native 16px pixel clusters. Only the palette changes: umber masonry, warm charcoal,
aged brown bronze and pale ash. No return to the old noisy 32px masonry. The preview
guide uses the same warm stone. Item sprites, recipes and process timing stay as-is.

The Ancient Anvil gets a heavier, broader head, stepped shoulders, a raised burnished
workface, gradual horn taper, a recessed hardie hole at the heel and two small bronze
maker's seals on the waist. Warm iron replaces the uniform gray; aged bronze replaces
the bright copper strip and tiny gold T. It still owns the same six block cells and
uses the same tier-zero/tier-one foundation. Geometry and collision are generated
together. The old-world compact fallback remains available when neighbors block it.

Material sources and palettes live in `tools/asset_sources/workshop-materials` and
`tools/generate_workshop_materials.py`. `--check` verifies exact exports in CI.
All source clusters remain intact: no resampling, smoothing or added noise.

Review in the actual Minecraft client: warm forge beside ordinary masonry, finished
fireplace, anvil from all four orientations, and the complete anvil as a held item.
The native fixture replaces its own anvil without break particles for clear captures;
real player harvesting and one-item recovery remain covered by gameplay tests.

The first native review caught a pale, stone-like workface and a blunt horn. The
revised pass uses darker neutral steel on top and a narrower five-step taper.
