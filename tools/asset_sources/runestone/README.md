# Runestone stone-surface revisions — preview only

The original `ancient_slate_16.png` was rejected after in-game review because its patches disappeared into a concrete-like surface. It is retained for reference.

Two new source tiles are uninscribed 16×16 opaque stone, using eight colors:
- `hewn_slate_16.png`: cooler cut stone with stronger bevels and large faceted patches.
- `weathered_slate_16.png`: rougher chipped stone with more irregular edges and grain clusters.

These are proposals, not approved final artwork. Both keep the middle free of symbols for later rune overlays. Imported from separate generated stone concepts using native-grid area sampling and palette quantization without dithering.

`generate_runestone_assets.py --variant hewn` exports the first proposal. The native visual QA job runs twice, swapping only the texture in its disposable checkout before resource processing; both screenshots use the actual Runestone model, not extra registered blocks or image reconstructions. The final chosen proposal has not been decided. Polished deepslate slabs remain vanilla.
