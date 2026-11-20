# Forge runes — ongoing design workshop

Status: **ideas and proposed recipes, not rune-block implementation**. The implemented foundation is Runestone at 16 hits × 10 XP/hit → 4 blocks, plus tier-scaled Ancient Anvil XP storage and storage-first payment. “Forge” here means the Ancient Anvil ritual structure; the Ruined Ancient Forge remains the alloying station.

## User direction and selected list

The structure has many replaceable Runestone positions, particularly at later tiers. The user explicitly rejected arbitrary rune-count caps: substantial crafting investment should remain useful. Balance around the available positions and interactions, not “only the first N count.” The user also approved heavy Swift investment saving total XP and emphasized that Efficiency must remain worthwhile.

The following concepts are selected for continued design. Their numerical tuning and recipes remain proposals.

| Tier where active | Selected rune | Direction |
|---|---|---|
| 1 | Rune of Storage | More stored XP; every installed rune contributes. |
| 1 | Rune of Extraction | Pipe export from storage; standing on the rune deposits player XP, faster while crouching. |
| 1 | Rune of Input | Pipe import to storage; standing on the rune withdraws stored XP, faster while crouching. |
| 2 | Rune of Swift Forging | One fewer swing per rune, minimum one swing; raises XP per swing. No arbitrary count limit. |
| 2 | Rune of Tempering | User selected the earlier hammer-durability idea (“tampering” interpreted as Tempering). More runes reduce hammer wear. |
| 2 | Rune of Conduit | User selected increased XP transfer speed **both into and out of** the forge. No arbitrary count limit. |
| 4 | Rune of Efficiency | Strong XP reduction that remains valuable alongside Swift and scales past the previous five-rune proposal. |
| 4 | Rune of Batching | User-selected redesign: configure with Shift-right-click using an item on any face; other faces accept recipe inputs; pulls matching ingredients from the chest above. Multiple runes need coordinated scheduling. |
| Enables 5 | Keystone Rune of the Sky | Rename/replace Canopy Keystone, retaining its sixteen structural positions and existing purpose; no added effect yet. Preserve saved worlds. |
| 6 | Rune of Master Craftsmanship | More completed output, initially described as a small per-rune chance to double. Scaling beyond guaranteed doubling needs discussion; do not silently impose a count cap. |
| 6 | Rune of Grand Pattern | User-selected redesign: the rune directly beneath the anvil center packs the whole structure into a portable rune; placing that rune relocates/restores it. This replaces the old projection-only concept. |

The previous Tier 3 suggestions (Workpiece Memory, Rhythm, Basin Attunement) and Tier 5 suggestions (Sky Conduit, Crystal Anchor, Precision) were rejected. Do not treat them as planned features. Resonant Sight, the old Pattern/Reserve suggestions, Spectrum and Concordance are not on the selected list either.

## Actual current layout budget

Counting the foundation, unique Tier 2 frame/bridge cells, then cumulative stage requirements gives the following **full Runestone positions**. These exclude mandatory keystones, metal/energized/obsidian caps, glass, fluids and slabs. Duplicate corner coordinates count once.

| Tier | Cumulative replaceable full Runestone positions |
|---|---:|
| 0 | 0 |
| 1 | 25 |
| 2 | 52 |
| 3 | 124 |
| 4 | 364 |
| 5 | 520 |
| 6 | 552 |

These are the current geometry's budget, not a new cap or a promise that the layout can never change. Balance the proposed formulas against these counts. For scale, 552 Storage runes would add 552,000 capacity; 552 Master runes under the extra-batch proposal would produce 6 guaranteed base batches plus a 52% chance of a seventh. Those extreme builds must pay the actual rune material/XP cost and give up the other rune families' positions.

## Shared structure and progression rules — proposals

- Functional runes replace eligible Runestone in the validated structure. Count each coordinate once for one core; overlapping builds must not multiply the same installed rune. Decorative off-structure copies have no forge effect.
- Current Tier 1/2 validators reject all block entities in Runestone positions. When runes are implemented, explicitly permit the mod's valid rune block entities there while continuing to reject arbitrary inventories/containers; adding a tag alone will not make buffered Batching runes work.
- Actual validated tier gates rune effects. A higher-tier rune may still satisfy a Runestone position at lower tiers, but its functional effect is inactive. Mandatory Sky Keystones, lens glass, energized caps, fluids and other special structural materials retain their roles.
- Existing structure size is the build budget. No arbitrary first-N limits on Storage, Conduit, Efficiency, Tempering, Swift or Master Craftsmanship. Utility blocks gain usefulness through their individual position, ports, templates or buffers rather than inventing percentage bonuses for every block.
- Swift retains the user's natural one-swing floor. Once a particular recipe needs one swing, extra Swift runes cannot shorten it further and **must not keep increasing its price**. Longer recipes can still use those additional runes. This is a recipe floor, not a count cap.
- Crafting a rune one tier before it activates remains a proposal, not an approved requirement. It lets players prepare the next upgrade. Optional functional runes should not become mandatory just to advance a tier; Sky Keystone is the explicit structural exception.
- Keep each recipe's base swings and XP/hit independent. Show effective swings, XP/hit and total batch XP in the manual/recipe inspection. Freeze a job's rune-derived pricing/work parameters so moving runes cannot combine cheap early hits with a fast final hit.
- No passive XP creation. Transfer blocks move conserved points; efficiency reduces expenditure. Recipe-specific output bonuses must account for recycling and reversible conversions.

## Scaling without arbitrary count caps — proposed numbers

These replace the earlier capped formulas; the user has not approved exact coefficients.

| Rune | Suggested scaling | Why further investment matters |
|---|---|---|
| Storage | Base tier capacity + `1,000 × storage runes` | Linear additional capacity, with no count cutoff. |
| Conduit | Each rune adds 2 XP/second to the core's inbound budget **and** 2 XP/second to its outbound budget | Both directions benefit. Multiple ports share each directional budget, rather than each port duplicating the full rate. |
| Swift | `usefulSwift = min(swift runes, base swings − 1)`; swings = base swings − usefulSwift; XP/hit multiplier = `1 + 0.10 × usefulSwift` | Every rune needed by the recipe removes a swing. Surplus runes beyond the one-swing floor do not add a penalty. |
| Efficiency | Divide the Swift-adjusted XP price by `1 + 0.20 × efficiency runes` | 5 runes halve cost; 20 reduce it by 80%; 100 reduce it by about 95.2%. Every further rune improves it. |
| Tempering | Hammer wear chance per accepted strike = `1 / (1 + 0.05 × tempering runes)` | 20 runes halve expected wear; 100 reduce it by about 83.3%. Approaches zero wear without a finite count making hammers permanent. |
| Master Craftsmanship | Candidate: `0.01 × rune count` extra base-output batches in expectation | 25 runes: 25% chance of double; 100: guaranteed double; 125: guaranteed double plus 25% chance of triple. This extends the original double-only effect and requires discussion. |

For Master Craftsmanship, a strictly “double at most” effect naturally saturates at 100% chance. The extra-batch proposal rewards further runes without a hidden cap; it is not approved yet. Roll once per completion, not once per hit; bonus output never recursively rolls. Split large outputs into ordinary legal stacks/buffers, without discarding over-stack results.

### Efficiency and fractional XP

Do not round each individual discounted swing upward to a whole point: that would make additional Efficiency runes stop helping cheap recipes. Track fractional XP accounting across paid work/crafts, with a persistent high-precision remainder and whole-point player transfers. Final precision/rounding and presentation need implementation design; this is not currently present in the integer reservoir. Payment must still be atomic and must not reset fractional accounting on save, cancellation, rune swaps or relocation. Avoid random per-hit discounts as a substitute for predictable pricing.

Expected cost examples below show the formula before integer wallet settlement. Preserve explicitly free recipes at zero. Heavy investment saving total XP is an approved direction.

| Runestone setup | Swings | Effective XP/hit | Expected total XP |
|---|---:|---:|---:|
| No runes | 16 | 10 | 160 |
| 5 Efficiency | 16 | 5 | 80 |
| 20 Efficiency | 16 | 2 | 32 |
| 100 Efficiency | 16 | about 0.476 | about 7.62 |
| 8 Swift | 8 | 18 | 144 |
| 8 Swift + 5 Efficiency | 8 | 9 | 72 |
| 15 Swift + 20 Efficiency | 1 | 5 | 5 |

These are intentionally powerful late-investment examples. Tune coefficients against the **actual replaceable-block counts per tier**, recipe cost and material investment before implementation. The old +10% Swift surcharge is still only a proposal; uncapped stacking changes its balance substantially. Future hammers with more than one work per strike need an explicit composition rule rather than confusing work units with swings.

## Rune of Batching — proposed interaction and multiple-rune solution

User requirements: Shift-click an item onto **any** face; other faces accept inputs; pull ingredients from the chest above, filtered to the configured recipe.

Proposed interpretation of the template item: the **finished output** chooses the recipe. Shift-right-click copies a ghost template onto that face without consuming the item. The user has not yet confirmed output-versus-ingredient interpretation. Display its icon on the configured face; other faces accept valid ingredients. Clear/reconfigure with an explicit deliberate interaction, never accidentally during ordinary insertion.

- Each Batching rune has its own saved template, selected **recipe ID**, and ingredient buffer. More runes provide more independently filtered ports, buffer capacity and prepared recipes. No first-four-runes limit.
- If an output has several recipes, let the player choose the exact variant in the Workshop Manual before the rune starts pulling. Never silently pick a different fluid/material recipe. A configured face is for the template, not a consumed catalyst.
- Accept player/pipe insertion through the other faces only for ingredients the selected recipe needs. The chest directly above supplies the same filtered ingredients. Tags and item components must match the selected recipe. Leave unrelated items in the chest. Stop pulling when the rune's recipe-aware buffer is full.
- A **single coordinator per anvil** schedules complete ready jobs round-robin among the linked runes. Skip a rune missing ingredients, fluids or tier; it must not block a ready rune elsewhere. Finish or pause the current job before changing the plate.
- Different-template runes prepare different jobs. Same-template runes feed a coordinated recipe group; they increase staging capacity and entry points rather than trying to start overlapping crafts.
- Reserve one full batch atomically before loading the physical plate. Runes sharing a double chest must not each reserve the same stack. Re-check shared inventory extraction; no item removal until the full reservation can commit.
- Batching handles ingredient staging and loading. **It does not automatically swing the hammer.** Normal recipe swings, stored-first XP payment, durability and output rules still apply. Auto-rearming/output collection policy remains open: default should wait for the center to be collected rather than overwrite output.
- Do not seize items already placed manually on the plate. A manually occupied plate pauses automatic loading; player control takes priority. Breaking/reconfiguring a rune returns its actual buffered ingredients, never ghost templates.

## Rune of Grand Pattern — proposed relocation behavior

The active Tier 6 rune sits in the Runestone position directly beneath the anvil's placement center. Its positional role is the user's design, not an arbitrary stat-rune count limit.

1. Preview and confirm the complete relocation footprint from that central rune. Freeze job scheduling/transfers while packing. Pack **registered multiblock contents**, including functional runes, keystones, glass, crystal/metal caps, required fluid cells and the normal End Crystal entities associated with the ritual.
2. Preserve stored XP, fractional accounting if later added, workpieces, output, paid progress, Batching templates/buffers and orientations. Suspend the structure's effects while packed.
3. Turn the central control rune into one unstackable packed rune carrying the structure. Remove the original only after successful capture. Never include nearby terrain, player builds/chests or decorative floor blocks merely because they lie inside a bounding box. Whether to include optional decorative floors is a separate choice.
4. At the new site, placing the packed rune shows a whole-structure preview above it. Permit rotation. Place only if every destination block/entity location is loaded, in bounds, permitted and clear; validate support/containment for fluid pools. Do not overwrite chests, protected blocks or other players' builds.
5. Restore the full structure and data as one transaction; consume the packed state once. If placement cannot complete, retain the packed rune and leave the world unchanged. A server relocation identity/journal must prevent crash rollback from duplicating both the package and the live structure.

Move End Crystals without damaging/exploding them during capture; restored crystals remain normal player-placed, breakable/explosive End Crystals. Move only the required pool cells, not an attached river or unlimited surrounding fluid. Chest-above-Batching inventories remain external; the Batching rune's own buffers move with it.

The desired payoff is **move an entire invested ancient workshop**, not just save a blueprint or rebuild it from another inventory. Rune recipe and any relocation XP charge remain open; do not charge until an operation successfully commits.

## Transfer behavior and compatibility

Extraction/Input names follow the user's requested pipe directions; player standing transfer goes the opposite way relative to that player. Show explicit deposit/withdraw and pipe import/export arrows so players do not have to infer direction from the name alone.

Conduit upgrades both directions, including standing transfer and supported pipe adapters. Use shared per-core directional budgets so extra faces or overlapping player collision checks cannot multiply a transfer. More rune positions remain useful; these budgets are the rune-upgraded throughput, not a count cap. No Creative XP creation. Simultaneous opposite runes must not bounce points back and forth; choose the player's actual support rune.

No universal XP-pipe unit exists across mods. Plan a point-based storage interface plus explicitly supported NeoForge capabilities/adapters. XP-fluid integrations need a configured conversion and conserved fractional remainders. Arbitrary fluid and Forge Energy are not XP. Unknown pipes simply do not connect. Do focused adapter checks once a concrete integration exists.

## Draft recipes — retained for discussion, not approved

Ordinary recipes below consume **one blank Runestone plus** the listed extras and produce one rune. Sky Keystone retains its existing four-output batch. The earlier “craft one tier before activation” proposal still needs agreement. Crystal acquisition is currently Creative-only, so crystal-based survival gates need archaeology acquisition before release.

| Rune | Extra ingredients | Proposed swings × XP/hit |
|---|---|---:|
| Storage | 2 ancient ingots + lapis + gold ingot | 8 × 5 |
| Extraction | Hopper + ancient ingot + lapis | 8 × 8 |
| Input | Hopper + ancient ingot + redstone | 8 × 8 |
| Swift Forging | Sugar + redstone + ancient ingot | 12 × 10 |
| Tempering | 2 iron ingots + copper ingot | 10 × 8 |
| Conduit | 2 copper ingots + redstone | 10 × 8 |
| Efficiency | Diamond + 2 ancient ingots + lapis | 16 × 15 |
| Batching | Chest + hopper + ancient ingot | 16 × 15 |
| Master Craftsmanship | Ancient crystal + diamond + 2 ancient ingots | 24 × 25 |
| Grand Pattern | Map + ender pearl + 2 ancient ingots; revisit cost for actual relocation power | 24 × 25; revisit |
| Sky Keystone | Existing 4 Runestone + 4 ancient ingots + glowstone dust → 4 keystones | Existing 8 × 1; review separately |

Master Craftsmanship must be recipe-eligible. Exclude currency conversion, refund/recycling, reusable-catalyst and reversible conversion recipes by default; review artifact melting, Cruncher recovery and scrap conversions for output-profit loops. Relocation and XP transfers are never multiplied crafts.

## Fresh candidates — not on the selected list

Do not fill tiers with weak effects just to make a complete table. The following proposals introduce meaningful **forging operations**, requiring their own recipe/gear design before numerical pricing.

| Tier | New candidate | Concrete forge payoff | Stacking direction to investigate |
|---|---|---|---|
| 3 | Rune of Reforging | Replace a component in an invested ancient tool while retaining its earned level/progression. Supply the replacement part and XP. | Further runes reduce the reforge-specific XP overhead continuously; general Efficiency still applies. |
| 3 | Rune of Restoration | Repair worn ancient equipment at the anvil with matching materials and XP, giving the workshop a practical maintenance role. | More repair work per paid strike; input materials remain conserved and damage cannot go below zero. |
| 3 | Rune of Reclamation | Recover useful materials from obsolete functional runes when changing the workshop's build. | Increasing recovery toward full original material value; no XP refund, no recovery beyond paid inputs, and no Master bonus on reclamation. |
| 5 | Rune of Awakening | Forge ordinary Ancient Crystals into Energized Ancient Crystals, and later awaken selected artifacts using deliberately designed recipes. | Additional runes improve the awakening process's XP efficiency; no free material duplication. Creates a survival forging route toward Tier 6 caps once crystal acquisition exists. |
| 5 | Rune of Transference | Move a chosen modifier from a sacrificed artifact onto a compatible workpiece. Archaeology still supplies the donor, and the donor is consumed. | More runes reduce the transfer-specific XP overhead; eligibility and modifier compatibility remain explicit, not a random reroll. |

These candidates are **unapproved** and may need different tiers once equipment editing and energized-crystal acquisition are designed. They should fit the wider archaeology/workshop progression rather than replace discoveries with generic crafting.

## Decisions still open

- Coefficients for uncapped scaling against the counted 25–552-position layouts; fractional XP accounting and future hammer interactions.
- Master Craftsmanship beyond 100%: guaranteed extra batches versus staying double-only with diminishing chance.
- Batching's template item interpretation, recipe-variant selection and output collection/re-arming policy.
- Grand Pattern's recipe, placement confirmation, optional decorative floor handling and relocation XP cost.
- Whether any fresh Tier 3/Tier 5 operation belongs in the mod, and only then its recipe price.
