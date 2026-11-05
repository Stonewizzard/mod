# Runic Ancient Anvil artwork

The broad, grounded centerpiece follows `docs/images/runic-anvil-concept.jpg`.
All pixel sources are indexed native-grid artwork with an explicit warm iron,
aged bronze and amber palette. No photographic textures, filtered gradients or
random noise. `generate_runic_anvil_assets.py` compiles these exact sources;
`generate_workshop_materials.py --check` includes their export freshness.

The top is a 48x48 skin covering three blocks at 16 texels per block. Its rune
seal is designed once across the full surface. The plaque and horn use 16px
faces. B/C/D pixels on the top and panel are also exported as binary-alpha glow
masks. The model supplies light emission only to those inset rune quads.
