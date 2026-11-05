# Warm ruined workshop art

The forge keeps its coarse native Minecraft texture layout and full silhouette.
Its palette is umber masonry, warm charcoal, aged brown bronze and pale ash.
The fireplace interaction, process, chimney, preview guide and item sprites remain
as before. No return to the old noisy 32px masonry.

The Ancient Anvil keeps the warm pedestal, bronze collar and small maker seals.
Its redesigned upper assembly has a shorter, thicker striking head and one
faceted nose set below the face. The old thin cap and repeated little plates
along the horn are gone. A square hardie hole sits near the heel. A restrained
bronze forge mark is worked into the steel face.

The indexed 16x48 top skin uses fine native anvil-top clusters, a dark chamfered rim
and the existing steel/bronze palette. It covers three blocks at 16 texels per
block; global UVs keep its pixel scale and pattern continuous between cells.
It is not stretched across the head. Item previews retain the world face UVs.

The same six owned cells and tier-zero/tier-one foundation remain. The head mesh
and collision are generated together by `tools/generate_ancient_anvil.py`.
The safe compact fallback remains available in cramped saved worlds. The nose is
one native 45-degree element spanning two owned upper cells. Its collision is
derived from that rotated square in clipped, conservative native-pixel strips;
none escapes its own cell. Asset checks validate the actual rotated corners
against the full six-cell owned volume.

Selection rendering uses the assembled metal silhouette and the nose's actual
diagonal edges through Minecraft's native outline submission. It does not outline
collision strips or the borders between owned cells. Physical collision/picking
remain bounded to their individual cells. High-contrast outlines are supported.

Material sources/palettes live in `tools/asset_sources/workshop-materials` and
`tools/generate_workshop_materials.py`; `--check` verifies their exports in CI.
Native review captures include all four facings, the workface close-up, held item
and an extra F1 view through real OS input for unobstructed artwork inspection.
Both normal and F1 captures are saved; screenshots do not replace gameplay tests.
