# Ancient Anvil — first foundation

The Ancient Anvil is a distinct workshop structure from the Ruined Ancient Forge. Its broad work face, tapered horn, plain waist and small foot use whole-pixel geometry in one owned cell. One small gold maker's mark replaces the dense ornament. Four custom materials are authored on a 16×16 grid and exported losslessly at 2× for existing UI compatibility. Model and collision share `tools/generate_ancient_anvil.py`; materials and sprites share `tools/generate_pixel_assets.py`. Warm work-face inlays gently pulse and brighten at Tier 1. Larger future tiers can use multiple owned cells; this art pass preserves placement and saved-world behavior.

## Progression

| Step | Requirement / result |
|---|---|
| Repair the forge | Complete the existing Ruined Ancient Forge frame |
| Alloy ancient metal | 1 Ancient Scrap **or** Ancient Debris + 1 Gold Ingot + 2 Iron Ingots → **4 Ancient Ingots** |
| Ancient metal block | 9 ingots ↔ 1 Block of Ancient Metal |
| Craft anvil | Vanilla anvil shape: `BBB / .I. / III`, B = ancient metal block, I = ancient ingot; 31 ingots total |
| Tier 0 | Place the Ancient Anvil on its own |
| Tier 1 | All nine cells of a 3×3 metal-block square directly below it; anvil centered above the center block |

Eight alloy batches produce 32 ingots, enough for one anvil with one ingot left. Ancient Scrap is obtained by feeding an artifact to an adult Cruncher, melting an artifact in the repaired forge, or from a Sniffer debris excavation. Each action yields one scrap. Nine Ancient Scrap craft into one Netherite Scrap, and one Netherite Scrap crafts back into nine. Netherite Scrap is no longer a direct alloy ingredient. Ancient Debris remains an alternative direct alloy source.

Iron, gold, copper, netherite and ancient metal count. Mixed metals work. Common copper storage-block tags also allow the variants supplied by the current game/modpack. Gem blocks do not qualify. The foundation remains normal blocks; upgrading/downgrading consumes nothing. Missing a cell downgrades to Tier 0 within half a second. The stationary anvil does not fall, take vanilla falling-anvil damage, or overwrite its foundation.

Right-click empty-handed to view the tier, foundation count and native **Show foundation guide** button. The local ghost marks air in cyan and wrong occupied blocks in amber. Complete the foundation to clear it. This first implementation establishes Tier 0/1; artifact editing, larger tiers and ritual abilities remain future work.

## Forge alloying

The completed forge is operated in-world. Right-click inside its fireplace once per ingredient item, ignite with flint and steel, wait twenty seconds, then collect the result empty-handed. Black chimney smoke marks processing; pure white smoke marks ready output. One artifact alone melts into one Ancient Scrap through the same process. The front table and processing interface have been removed; equipment assembly stays at the separate Reconstruction Table.

All six persisted slots are preserved and recoverable by empty-hand clicks. A batch reserves its ingredients until the final tick. Wrong/occupied output, leftover metals in artifact melting, missing ingredients, a broken frame, or spare components prevent ignition. Frame damage pauses a burning batch; repair resumes it. Core drops return ingredients if unfinished and output if finished. Save/reload preserves phase and progress.

Recipe definitions live in `AncientAlloy`; `ForgeProcess` handles interaction, reservation, timer, smoke and collection. Foundation/source eligibility is tag-driven:

- `data/examplemod/tags/block/ancient_anvil_foundation.json`
- `data/examplemod/tags/item/ancient_alloy_scrap.json`

The three normal recipes are `ancient_anvil`, `ancient_metal_block`, and `ancient_ingots_from_block`. Registry IDs are `examplemod:ancient_anvil`, `examplemod:ancient_ingot`, and `examplemod:ancient_metal_block`. All appear together after the forge in the mod creative tab. The Workshop Manual documents both stages and actual recipes.

## Validation

Native server tests cover all four facings, standalone tier, complete mixed-metal foundation, missing-cell downgrade, unchanged foundation blocks, both alloy inputs, exact output/costs, incomplete/overflow/foreign-output/duplicate/frame guards, spare-part preservation, stacked persistence, shift-click and actual crafting recipes. Client checks cover custom textures, explicit bounded UVs, collision authoring and ingot transparency. The native-client workflow uses actual item placement, mouse/keyboard alloying, tier/status/guide buttons and screenshots.

See [Ancient Scrap](ANCIENT_SCRAP.md) for feeding, melting and conversion details.
