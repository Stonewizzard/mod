# Warm workshop materials

Native 16px source tiles from Minecraft (Mojang), mirrored at
[InventivetalentDev/minecraft-assets, 1.21.4](https://github.com/InventivetalentDev/minecraft-assets/tree/1.21.4/assets/minecraft/textures/block).
These are source references for palette swaps, not original mod artwork.

`tools/generate_workshop_materials.py` maps each source colour to one explicit
warm colour for the forge and anvil body/edge. It preserves their pixel clusters,
seams and texture coordinates. The forge uses brown masonry, warm charcoal and
aged bronze; the anvil body uses warm iron. No resampling or procedural noise.

`anvil-workface.pixels` is a separately authored indexed source for the top. It
uses the established steel colours and two existing bronze shades for its small
forge mark. Fine clusters reference Minecraft's `anvil_top`, with a darker rim
around the thick striking face. The separate faceted nose uses native-resolution
steel from the same palette. The 16x48 atlas covers three
blocks at **16 texels per block**, with continuous UVs across the owned cells.
The narrow workface sides retain the prior material.

The user approved the coarse material pattern in PR #51 and requested the earlier
brown palette. Do not restore the old 32px noise textures or repaint item sprites.
