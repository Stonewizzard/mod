# Ancient Anvil visual comparison

The normal anvil keeps the previous iron/lava design. Three additional Creative
items let the player compare alternatives before choosing a direction:

| Sample | Horn and proportions | Corner lava |
| --- | --- | --- |
| A: Foundry | Longer classic horn, waist about 8% narrower | Four slender open streams into the corner cups |
| B: Spillways | Broad, sturdy horn, existing waist proportions | Four wider waterfall sheets from open iron troughs |
| C: Runic Columns | Long broad horn, waist about 15% narrower | Four falls framed by vertical iron posts |

Search the mod tab for **Ancient Anvil A**, **B**, or **C**. They use the same
foundation/tier rules. These are comparison items without crafting recipes; no
choice has replaced the normal anvil. The lava is an animated visual element.

All models fit the existing 3x3x2 reserved footprint. The larger horns reach into
the already-owned upper center cell, which is claimed before the horn cell is
shown. Meshes, clipped per-design physics and true outline edges share the source
generators. Long native UV skins repeat the existing pixel art without stretching.

The design is serialized on the core and every part. Recovery gives back the
chosen sample item, and custom Item placement leaves the normal block-item mapping
intact. Saved anvils without a design load as BASE, preserving migration behavior.
Four-way ownership/recovery tests cover all 288 part/orientation/design paths.
Native QA uses actual placement of all four items, matching day/night/profile
cameras and frame captures of the flowing lava. A compact screenshot artifact is
exported separately from the full session video.
