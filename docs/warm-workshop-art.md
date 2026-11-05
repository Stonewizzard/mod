# Runic smithing centerpiece

The Ancient Anvil is a broad, grounded centerpiece for a future surrounding rune
structure. Its design reference is [the concept sheet](images/runic-anvil-concept.jpg).
The actual block model uses native pixel materials, not the concept painting.

The 3x3 plinth has four hollow bronze connection sockets and inset amber circuits.
A substantial armored head, pointed bronze-bound horn and raised square heel sit
on broad shoulders. The forging surface has an inset octagonal seal, sixteen
short glyphs and a central forge sigil. Four large bronze-framed rune plaques
face outward. Selected rune pixels emit light; the working metal stays shaded.

Geometry, clipped physical collision, item preview and the true-edge assembled
outline share `tools/runic_anvil_geometry.py`. Material palettes and indexed
sources live in `tools/asset_sources/runic-anvil`; their compiler participates in
`generate_workshop_materials.py --check`. The 48x48 top maps one texel to one
Minecraft model pixel over three blocks. There is no filtered or photographic
texture, random noise or independently stretched glyph wallpaper.

The broader model reserves eighteen real cells (3x3x2). The original indices
0..5 remain stable. Old saved cores and parts default to `monumental=false` and
use frozen six-cell models/geometry until the full footprint is clear of blocks,
containers and living entities. Expansion claims the outer cells before switching
central models. Blocked legacy anvils keep their old footprint and inventories;
no neighboring block is overwritten. Clearing the room permits automatic upgrade.

The Tier 0/Tier 1 rules, centered 3x3 metal foundation, recipes and one-item recovery
remain. Breaking any of the eighteen cells recovers one anvil. Selection uses the
assembled silhouette and actual horn edges, not per-cell collision stair steps.
The foundation guide includes raised wire markers so its target locations remain
visible around the wider plinth. The approved brown forge artwork is unchanged.

Native review captures include day/night views, the rune seal, all four facings,
held item, foundation guide, and real OS-driven placement and interactions.
