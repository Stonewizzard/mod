# Component workshops

These are seven **prototype blocks**, available in the mod's Creative tab and Functional Blocks. They deliberately have **no recipes or processing costs** and use plain vanilla-textured cubes. Their operations are separated from their blocks so later rituals and multiblocks can reuse the same rules.

Every station has one input slot and the normal player inventory. Place a **recovered component** in the slot, click the desired action, and take the same component back out. Shift-click works both ways. Unexcavated artifacts, fragments, finished pickaxes, other completed equipment, and unrelated items are rejected. Block inventories save normally; breaking a station returns its block and input once.

| Block | Action |
| --- | --- |
| Unveiler | Replace one hidden ancient modifier with one of three saved alternatives, showing modifier, raw base value, and F–S grade. |
| Anuller | Randomly remove one unlocked modifier. A hidden ancient slot can also be removed. |
| Corruptor | Roll a good, bad, or unchanged outcome, then permanently finalize the component. |
| Locker | Choose one revealed modifier to protect permanently. The lock cannot be moved or replaced. |
| Scaling Randomizer | Reroll the grade of every revealed, unlocked modifier, preserving IDs and raw values. Rerolls may retain an existing grade. |
| Restorer | Add 10 quality points per action, capped at 120%. |
| Influencer | Permanently choose Nether or End and reroll unlocked modifiers toward that influence. Usable only once per piece, including for the same influence. |

The five **Ancient Test …** Creative components have guaranteed hidden rolls, two ordinary modifiers, and 80% quality. They provide a quick way to test choices, locks, annulment, rerolls, restoration, and corruption. Their three offers are intentionally fixed. The existing Random Test components still get fresh rolls when acquired.

The Influencer is the only way to assign influence; tagged modifiers alone do not assign one. Its assignment cannot be repeated, removed, or switched; annulment does not reset it. See [influences](influences.md) for matching weights, exclusive modifiers, locks, and the End-only Phasebound ability.

## Ancient modifiers

When a new eligible component is recovered from an **Epic, Legendary, or Unique** artifact, one normal roll may become an inactive ancient slot. Initial probabilities are **25%, 35%, and 50%**, respectively. Lower rarities never roll one. This replaces a modifier rather than adding to the rarity's modifier count. Each part can have at most one hidden slot. Previously sealed records retain their original modifiers.

Ancient rolls use Minecraft's real obfuscated text in item information. The Unveiler shows **three distinct modifier choices**, each with its own raw value and grade. Choices respect that part's existing modifier pool, weighted value tiers, duplicate restrictions, and harvest exclusivity. Each new offer saves its own value tier, shown separately from its F–S grade; existing offers keep their original values. See [modifier value tiers](modifier-value-tiers.md). The currently active pools are the five pickaxe-compatible component roles; other recovered components can be restored/corrupted, and their modifier actions use whatever pools are configured for them later.

Offers are generated and saved when the ancient slot is discovered. Moving the component, reopening a screen, changing stations, or saving/reloading never rerolls them. One choice permanently replaces the hidden slot and consumes its offers. Annulment of the hidden slot removes its offers too. Unveil before assembly: these stations cannot edit completed equipment, and an unresolved ancient roll has no active effect.

## Locks and final corruption

A lock protects the selected modifier's **identity, raw rolled value, and scaling grade** from annulment, scaling rerolls, and every corruption outcome. The lock is stored by stable modifier ID, so removing other rows cannot move it. Hidden slots must be unveiled before they can be locked. A component can only receive one lock, ever.

Quality belongs to the **whole component**, so restoring quality or a corruption quality change affects the component's effectiveness, including its locked roll. This does not alter the protected roll's stored values.

Corruption always marks the component **Corrupted**, even when the secondary result is unchanged. All seven stations then reject further actions, including unveiling, locking, restoration, and another corruption. Corrupted components can still be assembled into equipment. The result is shown in the station and on the component.

| Corruption outcome | Relative weight | Result |
| --- | ---: | --- |
| Stronger modifier | 18 | One unlocked known modifier gains 25% raw strength, bounded by its trait cap. |
| Weaker modifier | 18 | One unlocked known modifier loses 25% raw strength. |
| Higher scaling | 12 | One unlocked modifier gains one grade, up to S. |
| Lower scaling | 12 | One unlocked modifier loses one grade, down to F. |
| Additional modifier | 10 | Add an eligible modifier from the component's pool, preserving duplicates/exclusive-group rules. |
| Lost modifier | 10 | Remove one unlocked modifier, possibly its ancient slot. |
| Improved quality | 8 | Gain 10 quality points, up to 120%. |
| Damaged quality | 8 | Lose 10 quality points, down to 0%. |
| Unchanged | 4 | No secondary change; the component is still permanently corrupted. |

Only possible outcomes participate in the weighted roll. For example, a component with only its locked modifier cannot lose that modifier. A component without a live pool cannot gain an undefined modifier. No outcome deletes the component or changes its item identity.

## Quality above 100%

The Restorer can repair a damaged recovered component and improve it beyond a perfect excavation, up to **120%**. Its original excavation quality, seed, rarity, and find remain unchanged for provenance; the current quality is a separate customization field. Current part quality and assembled averages retain the full 120% limit.

A raw +10% roll on a 120% component contributes +12% before normal level/ascension growth. An assembled tool copies each component's current quality, rolls, and customization provenance. Its original source components cannot subsequently change the tool. Existing tools are not upgraded by the new blocks; work only happens on components before reconstruction. Existing aggregate trait caps still apply.

## Implementation and validation

- `AncientModifiers`: discovery chances, legal alternatives, and saved offer data inside `examplemod:part_modifiers` (version 1, catalog revision 6).
- `ComponentWork`: component validation, permanent lock/corruption guards, mutation actions, current quality, and version 1 `examplemod:component_work` metadata.
- `ComponentCorruption`: editable outcome weights and eligibility; direct roll mutations always skip the lock.
- `ComponentStationKind` / `ComponentStationContent`: seven prototype blocks sharing a container/menu implementation.
- `ComponentStationPayload`: server-authored operations bound to the active menu and the exact input snapshot. Wrong/ranged/stale/replayed requests do nothing, including two-viewer races.
- `ComponentWorkTests`, `ComponentStationTests`, and `ComponentStationVisualRegression`: natural discovery/recovery, fixed offers, lock invariants, all corruption outcomes, terminal guards, 120% assembly, Creative acquisition, native packet codecs/routing, save/load, block drops, and UI bounds.

The level/ascension system continues to work on assembled pickaxes as before. Component workshop actions do not target those tools.
