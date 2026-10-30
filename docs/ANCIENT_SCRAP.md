# Ancient Scrap

`examplemod:ancient_scrap` is the small ancient-metal unit. It replaces vanilla Netherite Scrap in the Ruined Forge alloy source tag and the guaranteed Sniffer debris-dig reward. Ancient Debris remains a direct alloy alternative. Resonant-tool recipes and ordinary vanilla Netherite Scrap uses keep using the vanilla item.

| Source | Cost | Result |
|---|---|---|
| Feed an adult idle Cruncher | Right-click with 1 artifact | 1 Ancient Scrap; two-second chew animation |
| Melt in a completed Ruined Forge | 1 artifact in the fireplace, flint-and-steel ignition and twenty seconds; no gold/iron | 1 Ancient Scrap |
| Sniffer excavates exposed Ancient Debris | One actual debris block, respecting griefing protection | 1 Ancient Scrap + the existing artifact and optional Cruncher egg |
| Split vanilla scrap | 1 Netherite Scrap in any crafting grid | 9 Ancient Scrap |
| Combine small scrap | Full 3×3 of Ancient Scrap | 1 Netherite Scrap |

All six artifact rarities yield one scrap when recycled. These are deliberate destructive alternatives to excavation; component pieces and Ancient Fragments are not accepted. Legendary/Unique acquisition chances are unchanged.

Cruncher feeding awards the scrap immediately into inventory, dropping at the player if full. Only the animation/cooldown is deferred, so saving or killing the mob cannot delete a pending reward or duplicate it. Babies and currently busy Crunchers reject the interaction without consuming an artifact. Artifact feeding does not heal/breed the Cruncher or change its existing wither-rose food interaction. Explicit player feeding does not destroy world blocks.

The forge is dedicated to in-world alloying and artifact melting, with no processing interface. Its core is in the back-center fireplace and the old front table is removed. Right-click ingredients into the firebox, ignite with flint and steel, and collect empty-handed after black chimney smoke changes to pure white. Both operations take twenty seconds. All six legacy inventory slots remain recoverable. Ingredients stay reserved until completion; frame damage pauses progress and core destruction returns the unfinished ingredients. Existing front cores relocate into the fireplace without losing inventory. The alloy recipe remains 1 Ancient Scrap or Ancient Debris + 1 gold + 2 iron → 4 Ancient Ingots.

The default alloy eligibility is in `data/examplemod/tags/item/ancient_alloy_scrap.json`. Both conversion recipes are normal datapack crafting recipes. `tools/generate_ancient_anvil.py` authors the scrap item models, default tag and conversion recipes alongside ancient-metal assets. `AncientAlloy` centralizes forge validation and quantities; `Cruncher.mobInteract` handles explicit entity feeding; `SnifferDebrisLootModifier` replaces only the successful debris excavation’s scrap reward.

Tests cover exact native conversion quantities, all artifact rarities for both recycling paths, busy/baby/full-inventory feeding, no breeding, wrong/overflow outputs, leftover metals, frame interruption/repair, and duplicate/empty-source guards. The existing real Sniffer dig and 600-seeded-loot checks verify the new guaranteed scrap without changing artifact/egg behavior.
