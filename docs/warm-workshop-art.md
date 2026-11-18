# Runic smithing centerpiece

The Ancient Anvil sits on an eighteen-cell (3x3x2) plinth for the future surrounding
runic structure. Four hollow sockets, large carved side glyphs and an inset forge
seal establish its identity. Its metal uses the actual vanilla anvil pixel pattern
in a warm charcoal palette; structural bands are cooled iron. Molten channels
have red-orange edges, pale hot cores and darker crust instead of flat gold lines.
The source artwork is native indexed pixel data, not filtered image generation.

The horn is a closed static OBJ mesh, baked by NeoForge's built-in OBJ/composite
model loaders. Its width and underside taper towards a thin, beveled tip; it is
not a vertically extruded diamond or a stack of small cubes. All of it fits within
the upper front owned cell. `tools/anvil_horn.py` generates world/item meshes,
collision envelopes and the actual selection edges. No per-frame entity renderer
or additional runtime dependency is required. UVs retain native pixel density,
including on the uniformly scaled item. Collision strips conservatively follow
the sloped surface within one native pixel; the outline follows mesh edges.

`tools/runic_anvil_geometry.py` builds the remaining clipped block meshes, physics,
item and blockstates. Materials are in `tools/asset_sources/runic-anvil`; the
compiler participates in `generate_workshop_materials.py --check`. The 48x48 top
skin maps one texel per Minecraft model pixel continuously across cell boundaries.
Rune glow masks use binary alpha. Native texture animation supplies a slow heat
pulse and moving lava cores without block ticks or particles. The previous concept
sheet remains a reference for the broad silhouette, not the current material spec.

Original indices 0..5 remain stable. Saved six-cell assemblies retain frozen old
models until the full footprint is clear, then expand safely. Adjacent containers,
entities and the centered metal foundation remain protected. Tier 0/Tier 1,
crafting and one-item recovery are unchanged. Raised foundation-guide markers stay
visible around the plinth. The approved brown forge textures are unchanged.

Asset checks cover closed/outward mesh faces, ownership, native UVs and the thin
horn tip, including negative controls for an open mesh, displaced vertex and blunt
tip. Gameplay checks exercise 72 harvesting paths, migration and obstruction.
Native Minecraft captures include the original offending angle, side profile,
rear, day/night, four block orientations, the item and foundation guide.

The [Foundry design (A)](anvil-design-options.md) is now the normal Ancient Anvil:
a longer classic horn, slightly slimmer waist and four thin falls into the corner
cups. The comparison items are hidden from Creative; their IDs and saved models
remain for compatibility. The selected geometry and pixel artwork are unchanged.
