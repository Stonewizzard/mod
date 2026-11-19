# Runestone stone-surface revisions — preview only

The original `ancient_slate_16.png` was rejected after in-game review because its patches disappeared into a concrete-like surface. It is retained for reference.

Two new source tiles are uninscribed 16×16 opaque stone, using eight colors:
- `hewn_slate_16.png`: cooler cut stone with stronger bevels and large faceted patches.
- `weathered_slate_16.png`: rougher chipped stone with more irregular edges and grain clusters.

These are proposals, not approved final artwork. Both keep the middle free of symbols for later rune overlays. Imported from separate generated stone concepts using native-grid area sampling and palette quantization without dithering.

`generate_runestone_assets.py --variant hewn` exports the first proposal. The native visual QA job runs twice, swapping only the texture in its disposable checkout before resource processing; both screenshots use the actual Runestone model, not extra registered blocks or image reconstructions. The final chosen proposal has not been decided. Polished deepslate slabs remain vanilla.

## Dark palette comparison

The four `palette_*_16.png` files correspond to A Warm charcoal, B Sootstone, C Dark umber and D Volcanic stone. They are native 16×16 opaque exports of the four-color mockup atlas. These remain unapproved palette proposals.

`tools/client_qa/palette_resources.py` creates disposable model overrides for four vanilla full-cube material carriers only in the native visual-QA checkout before launching Minecraft. This lets all four proposed textures appear together in one real screenshot, with equal geometry and lighting. Generated carrier overrides and preview textures are not versioned or registered as production blocks. The source PNGs stay outside game resources until this QA-only preparation step. Main Runestone has not been assigned a final palette.
