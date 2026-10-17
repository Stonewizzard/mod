# Pickaxe modifiers

Newly completed pickaxe parts roll modifiers once, on the server. Each part contributes `raw roll × its excavation score / 100`. A +10% mining-speed roll contributes +5% at score 50 and +0% at score 0. Assembly preserves every part's raw values, score, seed and catalog revision. The assembly's average score is informational and never multiplies the contributions again.

The five compatible pieces are the left tip, head bridge, right tip, connector and handle. Other equipment has no active modifier effects yet. Completed pickaxe-compatible finds remain usable below 60 condition; a completely ruined find still yields fragments. Existing completed/recovered parts retain their saved rolls, including previously empty records.

| Artifact | Modifier count | Extra-roll chances | Mining speed per speed roll | Fortune per Fortune roll | New options |
| --- | --- | --- | --- | --- | --- |
| Common | 1 | none | +4–12% | +0.10–0.35 levels | speed, durability, Fortune, XP |
| Uncommon | 1–2 | 20% | +8–22% | +0.20–0.55 levels | magnetism, underwater and airborne mining |
| Rare | 1–3 | 45%, 10% | +12–40% | +0.30–0.75 levels | Silk Touch chance, reach |
| Epic | 1–3 | 65%, 25% | +20–65% | +0.40–0.90 levels | smelting chance |
| Legendary | 1–4 | 80%, 50%, 15% | +30–90% | +0.55–1.10 levels | stronger ranges |
| Unique | 1–4 | 90%, 70%, 35% | +45–120% | +0.70–1.25 levels | strongest ranges |

Every compatible piece gets one guaranteed roll. Extra attempts are independent and can all fail. Duplicate traits and multiple harvest traits (Fortune/Silk/Smelting) on the same piece are excluded. Different pieces have different weights and options; for example, magnetism is available on connectors and handles. Common pieces cannot roll Silk Touch. Even four perfect Common speed rolls total only +48%, below the speed increase of vanilla Efficiency II on a diamond pickaxe.

## Effects

- **Mining speed:** multiplies native mining speed, including normal Efficiency enchantments.
- **Reinforced:** increases the assembled tool's native maximum durability. Unbreaking, repairs and Mending keep their native behavior.
- **Fortune:** adds fractional levels. A total of 1.5 uses Fortune I or II with equal probability per block. The combined native Fortune level is capped at III; existing higher command enchantments are never lowered.
- **Magnetized:** attracts the holder's recently mined block drops within the effective radius. It uses normal item motion and pickup, and does not pull arbitrary thrown items or other players' marked drops. Marking lasts 60 seconds.
- **Silk Touch:** uses the effective value as an activation probability. A 50% raw chance at score 50 activates 25% of the time. Successful Silk Touch takes precedence over Fortune and smelting and grants no mining XP. A normal Silk Touch enchantment remains unconditional.
- **Scholar:** increases the block's native mining XP. Fractional XP is stochastic-rounded so small bonuses retain their expected value.
- **Aquatic / Steady hands:** remove the indicated fraction of the native underwater/airborne mining penalty. Existing underwater mining attributes are respected.
- **Long reach:** adds native main-hand block interaction range.
- **Smelter:** chance to convert drops using native smelting recipes, preserving output counts and granting the recipe's XP. It does not run on a Silk Touch harvest.

Contributions add after per-part quality scaling. Aggregate caps are: +600% mining speed, Fortune III, 12-block magnet radius, 100% Silk/smelting chance, +200% durability, +300% mining XP, full removal of each movement penalty and +2.5 blocks of reach. These keep combined rolls bounded; caps can make a combined total grow less than linearly near its limit. Normal tool enchantments remain separate from part rolls. Virtual loot enchantments never alter the actual tool.

Fortune/Silk use native block loot generation. Ender Chest Silk Touch is supported; dynamic block-entity/container drops otherwise stay intact. Ability handling stays server-authoritative for drops, XP, Creative rolls and magnetism; speed and native attributes also work on the client for responsive mining.

## Creative testing

The mod tab includes 0/50/100-score variants of all five pieces, named **Random Test … [score/100]**. Each catalog entry is an unrolled template. Once a fresh copy enters the actual player inventory, the server assigns a new seed and rolls the **Unique** pool so every ability is available for testing. Each fresh grab is independent, including the zero-score variants. Tooltips show the raw roll and effective score-scaled value. Zero-score pieces have real rolls with zero bonus.

Moving a resolved piece, storing it, opening a UI or assembling it never rerolls it. Direct transfer of a still-pending Creative template to the table is resolved before assembly as a fallback. Use fresh artifacts of each rarity to test their actual tier restrictions.

## Balance configuration

`ArtifactModifierCatalog.java` contains the six `Tier` entries, every value range and the per-part weight maps. Edit these to add/remove options or rebalance ranges/counts. `PickaxeTrait.java` defines stable IDs, display units and aggregate caps. `PickaxeEvents.java` implements runtime mining/drop/pickup effects; `PickaxeModifiers.java` aggregates scores, applies native components and formats item information.

`Tier.chances` specifies one probability per roll attempt; `[1.0, .45, .10]` means one guaranteed roll, a 45% second attempt and a 10% third attempt. Tier ranges and per-part weights are separate. Pool iteration uses enum order so seeds remain stable across JVMs. Definition validation rejects invalid IDs, weights, ranges and duplicate entries.

`examplemod:part_modifiers` remains version 1 with catalog revision 2. Existing records are never overwritten. `examplemod:reconstruction.Parts` retains each part's complete record. Unknown or removed IDs remain stored but have no active effect. Keep IDs stable and increment the catalog revision when changing future rolls; the data format stays compatible with existing saves.
