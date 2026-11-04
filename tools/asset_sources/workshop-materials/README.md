# Warm workshop materials

Native 16px source tiles from Minecraft (Mojang), mirrored at
[InventivetalentDev/minecraft-assets, 1.21.4](https://github.com/InventivetalentDev/minecraft-assets/tree/1.21.4/assets/minecraft/textures/block).
These are source references for palette swaps, not original mod artwork.

`tools/generate_workshop_materials.py` maps each source colour to one explicit
warm colour. It preserves every pixel cluster, seam and texture coordinate. The
forge uses brown masonry, warm charcoal and aged bronze; the anvil adds warm iron
and a lighter, burnished workface. No resampling, procedural noise or extra detail.

The user approved the coarse material pattern in PR #51 and requested the earlier
brown palette. Do not restore the old 32px noise textures or repaint item sprites.
