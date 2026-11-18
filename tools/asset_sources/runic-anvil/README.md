# Ancient Anvil source art

Indexed native pixel sources are compiled by `generate_runic_anvil_assets.py` and
checked by `generate_workshop_materials.py --check`. The metal/edge/frame clusters
come from the preserved vanilla 16px anvil source in `../workshop-materials`.
The palette gives the body warm charcoal iron, worn steel bevels and cooler bands.
Do not replace these with photographic noise, gradients or decorative bronze boxes.

`top.pixels` is a continuous 48x48 skin: each texel is one model pixel across the
3x3 reservation. The forge seal and eight short glyphs are carved into the metal.
`panel.pixels` uses the same dark channel edges and hot rune cores. B/C/D are
emissive molten pixels. Glow masks have fixed binary alpha and a gentle heat pulse.
`ember.pixels` contains lava cores and cooling crust, compiled as a native texture
animation for the narrow channels. `horn.pixels` is deliberately unbordered iron
with restrained strike wear. The actual tapered horn comes from `anvil_horn.py`;
its broad texture is never used to pretend a blunt cuboid is a pointed nose.

Legacy forge/anvil materials remain independently versioned and preserved.
