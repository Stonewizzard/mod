# Forge runes — ongoing design workshop

Status: **ideas and proposed recipes, not rune-block implementation**. The implemented foundation is Runestone at 16 hits × 10 XP/hit → 4 blocks, plus tier-scaled Ancient Anvil XP storage and storage-first payment. “Forge” here means the Ancient Anvil ritual structure; the Ruined Ancient Forge remains the alloying station.

## User direction and selected list

The structure has many replaceable Runestone positions, particularly at later tiers. The user explicitly rejected arbitrary rune-count caps: substantial crafting investment should remain useful. Balance around the available positions and interactions, not “only the first N count.” The user also approved heavy Swift investment saving total XP and emphasized that Efficiency must remain worthwhile. Latest corrections: Tempering may reach zero hammer wear; surplus Swift runes must continue increasing XP cost after the one-swing floor; Efficiency should require substantially more investment to reach 50%, with diminishing returns after that threshold. Restoration is selected for any durability-bearing item, with more runes increasing repair speed.

The following concepts are selected for continued design. The user accepted the most recent zero-wear Tempering, surplus-Swift surcharge, slower Efficiency ramp and automatic XP Restoration direction in principle, then moved Efficiency to Tier 3. These remain design decisions, not implemented rune behavior. Recipe prices and remaining rates are still provisional.

| Tier where active | Selected rune | Direction |
|---|---|---|
| 1 | Rune of Storage | More stored XP; every installed rune contributes. |
| 1 | Rune of Extraction | Pipe export from storage; standing on the rune deposits player XP, faster while crouching. |
| 1 | Rune of Input | Pipe import to storage; standing on the rune withdraws stored XP, faster while crouching. |
| 2 | Rune of Swift Forging | One fewer swing per rune, minimum one swing; raises XP per swing. No arbitrary count limit. |
| 2 | Rune of Tempering | Reduces hammer wear and may reach zero wear; permanence is allowed with sufficient investment. |
| 2 | Rune of Conduit | User selected increased XP transfer speed **both into and out of** the forge. No arbitrary count limit. |
| 3 | Rune of Restoration | Put any item with durability in the center slot and spend XP to repair it. Additional runes increase repair speed. |
| 3 | Rune of Efficiency | XP reduction with a substantially longer investment ramp to 50%, then diminishing returns; no arbitrary rune-count cutoff. |
| 4 | Rune of Batching | User-selected redesign: configure with Shift-right-click using an item on any face; other faces accept recipe inputs; pulls matching ingredients from the chest above. Multiple runes need coordinated scheduling. |
| Enables 5; excluded from functional-rune target | Keystone Rune of the Sky | Rename/replace Canopy Keystone, retaining its sixteen structural positions and existing purpose; no added effect yet. Preserve saved worlds. |
| 6 | Rune of Master Craftsmanship | More completed output, initially described as a small per-rune chance to double. Scaling beyond guaranteed doubling needs discussion; do not silently impose a count cap. |
| 6 | Rune of Grand Pattern | User-selected redesign: the rune directly beneath the anvil center packs the whole structure into a portable rune; placing that rune relocates/restores it. This replaces the old projection-only concept. |

The previous Tier 3 suggestions (Workpiece Memory, Rhythm, Basin Attunement) and Tier 5 suggestions (Sky Conduit, Crystal Anchor, Precision) were rejected. Do not treat them as planned features. Resonant Sight, the old Pattern/Reserve suggestions, Spectrum and Concordance are not on the selected list either. Of the newer candidates, only Restoration was selected. Reforging, Reclamation, Awakening and Transference are also rejected; do not carry them forward as planned mechanics.

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
- Swift retains the user's natural one-swing floor. Every installed Swift rune continues to add its XP surcharge, including surplus runes after that floor. A layout optimized for a long recipe can therefore cost more than necessary on short recipes. Do not silently clamp the price calculation to the number of useful swing reductions.
- Crafting a rune one tier before it activates remains a proposal, not an approved requirement. It lets players prepare the next upgrade. Optional functional runes should not become mandatory just to advance a tier; Sky Keystone is the explicit structural exception.
- Keep each recipe's base swings and XP/hit independent. Show effective swings, XP/hit and total batch XP in the manual/recipe inspection. Freeze a job's rune-derived pricing/work parameters so moving runes cannot combine cheap early hits with a fast final hit.
- No passive XP creation. Transfer blocks move conserved points; efficiency reduces expenditure. Recipe-specific output bonuses must account for recycling and reversible conversions.

## Scaling without arbitrary count caps — proposed numbers

These replace the earlier capped formulas. The user accepted the latest Tempering/Efficiency starting values in principle; other coefficients, interactions and recipe prices still need tuning before implementation.

| Rune | Suggested scaling | Why further investment matters |
|---|---|---|
| Storage | Base tier capacity + `1,000 × storage runes` | Linear additional capacity, with no count cutoff. |
| Conduit | Each rune adds 2 XP/second to the core's inbound budget **and** 2 XP/second to its outbound budget | Both directions benefit. Multiple ports share each directional budget, rather than each port duplicating the full rate. |
| Swift | Swings = `max(1, base swings − swift runes)`; XP/hit multiplier = `1 + 0.10 × swift runes` | All Swift runes count toward cost, even after swings reach one. The +10% coefficient remains a proposal. |
| Efficiency | Proposed price multiplier: `1 − 0.01 × E` for `E ≤ 50`; `50 / (E + 50)` for `E > 50`, where E is installed Efficiency runes | 5 runes save 5%; 50 save 50%; 100 save about 66.7%; 200 save 80%; 552 save about 91.7%. Every rune keeps contributing. The user accepted this starting curve in principle; later balance tuning remains possible. |
| Tempering | Proposed hammer wear chance = `max(0, 1 − 0.01 × tempering runes)` | 50 runes halve expected wear; 100 eliminate wear. The user accepted zero wear and this numerical starting point in principle; later tuning remains possible. |
| Restoration | Repair throughput grows with installed Restoration runes; XP price is per actual durability restored | More runes repair faster, without implicitly reducing the XP price per durability point. General Efficiency can reduce that price separately. Exact rate and unit price remain open. |
| Master Craftsmanship | Candidate: `0.01 × rune count` extra base-output batches in expectation | 25 runes: 25% chance of double; 100: guaranteed double; 125: guaranteed double plus 25% chance of triple. This extends the original double-only effect and requires discussion. |

For Master Craftsmanship, a strictly “double at most” effect naturally saturates at 100% chance. The extra-batch proposal rewards further runes without a hidden cap; it is not approved yet. Roll once per completion, not once per hit; bonus output never recursively rolls. Split large outputs into ordinary legal stacks/buffers, without discarding over-stack results.

### Efficiency and fractional XP

Do not round each individual discounted swing upward to a whole point: that would make additional Efficiency runes stop helping cheap recipes. Track fractional XP accounting across paid work/crafts, with a persistent high-precision remainder and whole-point player transfers. Final precision/rounding and presentation need implementation design; this is not currently present in the integer reservoir. Payment must still be atomic and must not reset fractional accounting on save, cancellation, rune swaps or relocation. Avoid random per-hit discounts as a substitute for predictable pricing.

Expected cost examples below show the formula before integer wallet settlement. Preserve explicitly free recipes at zero. Heavy investment saving total XP is an approved direction.

| Runestone setup | Swings | Effective XP/hit | Expected total XP |
|---|---:|---:|---:|
| No runes | 16 | 10 | 160 |
| 5 Efficiency | 16 | 9.5 | 152 |
| 50 Efficiency | 16 | 5 | 80 |
| 100 Efficiency | 16 | about 3.333 | about 53.33 |
| 8 Swift | 8 | 18 | 144 |
| 8 Swift + 50 Efficiency | 8 | 9 | 72 |
| 15 Swift | 1 | 25 | 25 |
| 20 Swift | 1 | 30 | 30 |
| 20 Swift + 100 Efficiency | 1 | 10 | 10 |

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
| Restoration | To decide after repair rate and XP-per-durability are settled | To decide |
| Efficiency | Diamond + 2 ancient ingots + lapis | 16 × 15 |
| Batching | Chest + hopper + ancient ingot | 16 × 15 |
| Master Craftsmanship | Ancient crystal + diamond + 2 ancient ingots | 24 × 25 |
| Grand Pattern | Map + ender pearl + 2 ancient ingots; revisit cost for actual relocation power | 24 × 25; revisit |
| Sky Keystone | Existing 4 Runestone + 4 ancient ingots + glowstone dust → 4 keystones | Existing 8 × 1; review separately |

Master Craftsmanship must be recipe-eligible. Exclude currency conversion, refund/recycling, reusable-catalyst and reversible conversion recipes by default; review artifact melting, Cruncher recovery and scrap conversions for output-profit loops. Relocation and XP transfers are never multiplied crafts.

## Rune of Restoration — selected Tier 3 direction

Confirmed by the user:

- Place **any item with durability** in the anvil's center slot to repair it using XP. This includes vanilla and modded tools, weapons, armor and other durability-bearing items; do not restrict the rune to ancient equipment or require a repair-material recipe.
- Additional Restoration runes increase how quickly repair happens. No arbitrary count cutoff.
- The operation restores the same item. Preserve its enchantments, name, components and other data; change only actual durability damage.

Accepted operating direction from the previous discussion, with rates and implementation details still to settle:

- Repair runs automatically while the item remains in the center. The other workpiece positions must be empty, and an active normal crafting job takes priority. Batching waits while a repair item occupies the plate.
- Unattended repair uses **stored forge XP**, pausing when the next repair unit cannot be paid. It does not select a nearby player as an automatic XP source. Taking the item back stops repair.
- Charge a configurable XP price **per actual durability restored**, not per tick/rune. More Restoration runes increase throughput; Efficiency discounts XP per durability using its normal scaling. Do not charge for repair beyond full durability or while paused.
- Swift and Tempering affect hammer work, not automatic repair speed; Master Craftsmanship does not duplicate a repaired item. Further Restoration runes are the dedicated speed investment.
- Support normal item durability across mods without assuming every durability-bearing item is a pickaxe. Items using a separate custom charge/resource system need deliberate integration, rather than treating charge as durability.

Automatic repair from stored XP is the accepted direction. Base repair rate, XP per durability point, exact job precedence and the rune's recipe remain to settle; do not reopen the automatic-versus-hammered question without a concrete reason.

The other recent candidate operations (Reforging, Reclamation, Awakening and Transference) are removed from the active proposal. Tier 5 still has no user-selected optional functional rune. Sky Keystone is explicitly excluded from the requested three-per-tier count. The fresh candidates below are suggestions for the user to choose, not approved additions.

## Latest workshop direction: Tier 3 first

The user clarified that they are **not a fan of the previous additional forge ideas**. Inscription, Parallel Forging, Striking, Ascendance, Commission and Dispatch are therefore rejected, not selected or awaiting implementation. Do not carry them into the forge roster or rename them as accepted features.

The user likes **Vigilance as a general-purpose chunk-loading rune that can be placed anywhere**, rather than an Ancient Anvil upgrade. Move that concept to the general functional-rune list with no forge tier assigned yet. It does not count toward the requested three forge runes per tier. Its fuel, loading footprint and recipe remain open; the prior stored-forge-XP-only design is not an approved restriction for the standalone rune.

Efficiency remains moved to Tier 3. Forge-only selected counts, excluding Sky Keystone, are still **3 / 3 / 2 / 1 / 0 / 2** for tiers 1–6, eleven total. The user now wants to choose the next Tier 3 idea before moving on. Do not force a complete eighteen-rune lineup with weak filler.

### Current Tier 3

- **Restoration:** any durability-bearing item in the center, automatic repair using stored XP, more runes increase repair speed.
- **Efficiency:** the accepted slower XP-discount ramp, with diminishing returns after 50%.
- **Third slot:** open. The options below are new, unapproved alternatives, not three additions to the selected list.

### Candidate: Rune of Offering

Sacrifice an unwanted artifact at the anvil to turn its ancient power into **stored forge XP**. This gives archaeology another resource decision: keep the artifact, recover its Ancient Scrap through the existing routes, or sacrifice it for XP. It ties the forge's main resource directly to archaeological discoveries.

XP yields would be authored by artifact type/tier/rarity, not inferred from arbitrary item's crafting cost. Additional Offering runes improve recovery of that authored XP value with diminishing returns. The artifact is consumed, and the stored result obeys available capacity; do not destroy a valuable artifact if the complete yield cannot be accepted. This is an active material sacrifice, not passive XP generation.

Pricing/yield, eligible artifacts and whether this needs hammering remain open. Direct XP conversion must not receive Master Craftsmanship output bonuses or permit a forge-craft-to-XP profit loop. The concept is not a salvage-material refund operation; it creates a deliberate XP-versus-scrap choice.

### Candidate: Rune of Reinforcement

Use XP and Ancient Metal to **increase the maximum durability** of a compatible item placed in the center. Restoration repairs current wear; Reinforcement invests in the item's future durability. More installed runes allow stronger reinforcement, with material and XP costs scaling with the actual improvement.

Use the ordinary durability bar rather than adding a second charge/shield system. Preserve enchantments and item data. Repeated reinforcement should charge only for a real new improvement and should not also erase existing wear for free. Exact scaling, compatibility and recipe quantities remain open. This proposal adds material costs only to reinforcement; the already-selected Restoration repair remains XP-only.

This is an equipment-forging upgrade, not the rejected component-swapping Reforging operation. It offers a practical workshop payoff without enchantment-level systems or remote automation.

### Candidate: Rune of Containment

Stabilize the **four ritual fluid basins**: keep their fluids contained and stop unwanted flow/mixing. A stronger payoff would be enabling water-based forge basins in the Nether, so players can use the intended fluid recipes there.

More runes increase the number of basin cells that can be stabilized, up to the actual cells in the structure. These effects apply to the linked forge's designated pools, not arbitrary liquids across the world. Fluid type, recipe matching and actual fluid contents remain meaningful; it does not invent free liquids or bypass recipe requirements.

Which containment properties are included, activation during basin filling, supported mod fluids and behavior when a structure/rune is removed need design before implementation. Do not sell this as a generic world fluid-control rune: its purpose is making the ritual basins practical in an ancient workshop.

### Preference and unresolved choices

Offering has the strongest direct connection between archaeology and the new XP economy. Reinforcement is the clearest general equipment payoff. Containment is a more specialized physical-structure/dimension option. The user has not selected any of them. Prices and exact stacking values should follow the chosen effect, not precede it.

## Decisions still open

- Which third Tier 3 forge rune to select. Offering, Reinforcement and Containment are alternatives only.
- Tier 4/5/6 additions remain open after the rejected lineup; Sky Keystone remains excluded from the three-per-tier exercise.
- Vigilance's standalone general-purpose design and progression tier, separate from the forge.
- Numerical tuning, fractional XP accounting and future hammer interactions.
- Restoration's base repair rate, XP per durability point and recipe; automatic stored-XP operation is accepted in principle.
- Master Craftsmanship beyond 100%: guaranteed extra batches versus staying double-only with diminishing chance.
- Batching's template item interpretation, recipe-variant selection and output collection/re-arming policy.
- Grand Pattern's recipe, placement confirmation, optional decorative floor handling and relocation XP cost.
