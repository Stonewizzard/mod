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
| 3 | Rune of Expansion | Adds physical ingredient sockets to the forge, supporting larger and more elaborate recipes. More installed runes provide more sockets; placement details remain open. |
| 3 | Rune of Transmutation | The base/Overworld variant: authored item-to-item conversions into Overworld materials, using XP. |
| 3 | Rune of Nether Transmutation | Authored item-to-item conversions into Nether materials, using XP. |
| 3 | Rune of End Transmutation | Authored item-to-item conversions into End materials, using XP. |
| 4 | Rune of Batching | User-selected redesign: configure with Shift-right-click using an item on any face; other faces accept recipe inputs; pulls matching ingredients from the chest above. Multiple runes need coordinated scheduling. |
| 4 | Rune of Invocation | Selected in principle: forge ritual seals for the Forgemaster encounter. A Forgemaster drop becomes an ingredient for the Sky Keystones needed to reach Tier 5. |
| 4 | Rune of Collection | Selected: route completed forge results into a chest above the rune or connected item pipes, clearing the working center. |
| 4 | Rune of Deliberation | Selected: add required hammer swings in exchange for a reduction in the craft's total XP cost. |
| 5 | Rune of Specialization | Selected: reduce XP cost for a chosen recipe category while increasing XP costs outside that category. |
| 5 | Rune of Familiarity | Selected: consecutive crafts of the same recipe build an XP discount; switching recipes resets it. |
| 5 | Rune of Alternation | Selected: consecutive crafts that change recipes build an XP discount; repeating the same recipe resets this streak. |
| Enables 5; excluded from functional-rune target | Keystone Rune of the Sky | Rename/replace Canopy Keystone, retaining its sixteen structural positions and existing purpose; no added effect yet. Preserve saved worlds. |
| 6 | Rune of Master Craftsmanship | More completed output, initially described as a small per-rune chance to double. Scaling beyond guaranteed doubling needs discussion; do not silently impose a count cap. |
| 6 | Rune of Grand Pattern | User-selected redesign: the rune directly beneath the anvil center packs the whole structure into a portable rune; placing that rune relocates/restores it. This replaces the old projection-only concept. |
| 6 | Rune of Mutation | Selected for now: supported Transmutation recipes can produce a rarer alternative item instead of the normal result. Exact outcomes, chance scaling and recipe costs remain deferred. |

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
- Crafting a rune one tier before it activates remains a proposal, not an approved requirement. It lets players prepare the next upgrade. Ordinary stat/utility runes need not all become mandatory to advance tiers. Invocation is now an explicit progression exception selected by the user: it enables the Forgemaster encounter whose material is needed to craft the structural Sky Keystones for Tier 5.
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

## Recipe review 1 — Rune of Storage

The user is reviewing anvil recipes **one at a time**, starting with Storage. The following **ingredients and positions are explicitly selected**:

| Position | Input |
|---|---|
| Four diagonal corners (`north_west`, `north_east`, `south_west`, `south_east`) | 1 blank Runestone each — 4 total |
| Center (`center`) | 1 **Block of Lapis Lazuli** |
| Four inner cardinal positions (`north_inner`, `east_inner`, `south_inner`, `west_inner`) | 1 Gold Ingot each — 4 total |
| Four outer cardinal positions | Empty |

Logical layout on the thirteen-position working surface:

```text
R G R
G L G
R G R
```

R = blank Runestone, G = Gold Ingot, L = Block of Lapis Lazuli. This replaces the earlier tentative two-Ancient-Ingot/lapis-item/gold recipe. Do not use individual lapis lazuli instead of its block.

Remaining **proposed values from the first recipe presentation**, not separately confirmed by the ingredient correction: craft at Tier 1, Iron Forge Hammer or better, 8 swings at 5 XP per swing (40 XP), output 1 Rune of Storage. Activation at Tier 1 was already selected. Do not silently change the yield to four merely because four Runestone are inputs; output count can be reviewed explicitly. The general earlier craft-one-tier-before-activation suggestion remains unapproved.

This is recipe-design documentation only. Do not add a live recipe referencing an unregistered rune block before rune implementation is authorized.

## Recipe review 2 — Rune of Extraction: agreed

The user accepted the proposed Tier 1 recipe and one-rune output:

| Position | Input |
|---|---|
| Four diagonal corners | 1 blank Runestone each — 4 total |
| Center | 1 Hopper |
| Four inner cardinal positions | 1 Redstone Dust each — 4 total |
| Four outer cardinal positions | Empty |

```text
R D R
D H D
R D R
```

R = blank Runestone, D = Redstone Dust, H = Hopper. Output: **1 Rune of Extraction**. Its selected function remains pipe XP export and player-standing XP deposit, faster while crouching. The user explicitly deferred XP-value tuning; do not reopen XP balance on each recipe review.

## Recipe review 3 — Rune of Input: proposed next

Propose the same Tier 1 frame with **1 Dropper at the center**, distinguishing the opposite XP-flow recipe from Extraction:

| Position | Input |
|---|---|
| Four diagonal corners | 1 blank Runestone each — 4 total |
| Center | 1 Dropper |
| Four inner cardinal positions | 1 Redstone Dust each — 4 total |
| Four outer cardinal positions | Empty |

```text
R D R
D P D
R D R
```

R = blank Runestone, D = Redstone Dust, P = Dropper. Proposed output: **1 Rune of Input**. Its already-selected effect remains pipe XP import and player-standing XP withdrawal, faster while crouching. Ingredients/layout/output above are still **proposed**, not yet accepted; continue review one recipe at a time. XP values remain deferred.

## Draft recipes — retained for discussion, not approved

The rows below remain preliminary except for Storage's explicitly reviewed ingredients/layout and Extraction's accepted recipe above. Input is the next proposal under review. XP-value tuning is deferred by the user. Unless otherwise stated, earlier ordinary-rune proposals used **one blank Runestone plus** the listed extras and one-rune output. Storage is now an exception with **four blank Runestone inputs**; its output/work/XP values remain proposed. Sky Keystone's existing four-output batch is a starting reference, but its recipe must now include a Forgemaster drop as selected by the user. The earlier “craft one tier before activation” proposal still needs agreement. Crystal acquisition is currently Creative-only, so crystal-based survival gates need archaeology acquisition before release.

| Rune | Inputs (earlier rows use one blank Runestone unless stated) | Proposed swings × XP/hit |
|---|---|---:|
| Storage | **Confirmed layout:** 4 blank Runestone at corners + 1 Block of Lapis Lazuli at center + 4 Gold Ingots on inner cardinal positions | Proposed 8 × 5; output 1 remains proposed |
| Extraction | **Agreed:** 4 blank Runestone at corners + 1 Hopper at center + 4 Redstone Dust on inner cardinal positions; outer positions empty; output 1 | Work/XP tuning deferred |
| Input | **Proposed next:** 4 blank Runestone at corners + 1 Dropper at center + 4 Redstone Dust on inner cardinal positions; outer positions empty; output 1 | Work/XP tuning deferred |
| Swift Forging | Sugar + redstone + ancient ingot | 12 × 10 |
| Tempering | 2 iron ingots + copper ingot | 10 × 8 |
| Conduit | 2 copper ingots + redstone | 10 × 8 |
| Expansion | To decide after socket placement/recipe design | To decide |
| Transmutation: base, Nether, End | Separate recipes/catalysts to decide; all three are Tier 3 | To decide |
| Restoration | To decide after repair rate and XP-per-durability are settled | To decide |
| Efficiency | Diamond + 2 ancient ingots + lapis | 16 × 15 |
| Batching | Chest + hopper + ancient ingot | 16 × 15 |
| Invocation | To decide; the rune and first summoning seal must be obtainable before defeating the Forgemaster | To decide |
| Collection, Deliberation (Tier 4); Specialization, Familiarity, Alternation (Tier 5) | Choose recipe ingredients after operating details and numerical scaling | To decide |
| Master Craftsmanship | Ancient crystal + diamond + 2 ancient ingots | 24 × 25 |
| Mutation | Deferred; select supported alternative-output recipes and chance behavior before fixing rune ingredients | To decide |
| Grand Pattern | Map + ender pearl + 2 ancient ingots; revisit cost for actual relocation power | 24 × 25; revisit |
| Sky Keystone | Add a Forgemaster material to the recipe. Existing 4 Runestone + 4 ancient ingots + glowstone is a reference; whether the drop replaces glowstone or is additional is open. | Existing 8 × 1 → 4 is a reference; new costs/output/drop quantities remain open |

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

## Latest selections: Expansion and three Tier 3 Transmutation runes

The user rejected Offering, Reinforcement and Containment, then selected **Expansion** from the next group of Tier 3 suggestions. The other suggestions from that group remain unselected. Do not carry the rejected artifact-sacrifice, durability-reinforcement or basin-containment operations into the active list.

The user then requested **Rune of Transmutation plus Nether and End variants** and explicitly answered that **all three belong at Tier 3**. Interpret the base Rune of Transmutation as the Overworld-targeting variant, not an additional fourth universal rune. Exact item conversion recipes and XP costs remain to be designed.

Current Tier 3 selected roster:
- Restoration
- Efficiency
- Expansion
- Rune of Transmutation (Overworld)
- Rune of Nether Transmutation
- Rune of End Transmutation

All earlier added forge candidates (Inscription, Parallel Forging, Striking, Ascendance, Commission and Dispatch) remain rejected. **Vigilance is selected as a general-purpose chunk-loading rune usable anywhere**, outside the forge roster, with its progression/fuel still open.

Excluding Sky Keystone, selected forge counts are now **3 / 3 / 6 / 4 / 3 / 3** across tiers 1–6: twenty-two functional runes. The user selected Alternation and authorized moving one or two Tier 4 runes into Tier 5; Specialization and Familiarity moved together, keeping Batching, Invocation, Collection and Deliberation at Tier 4. The user then selected Mutation for Tier 6 **for now**, explicitly allowing a later revisit. The initial roster now meets at least three functional runes per tier; this does not finalize numerical tuning, recipes or implementation.

### Expansion — selected operation, socket design still open

Expansion increases the forge's actual physical ingredient positions, rather than an arbitrary passive stat. The additional sockets allow recipes with more inputs than the original thirteen-position working surface. Exactly where the sockets appear or whether the Expansion rune itself holds an ingredient is open. Preserve clear item graphics/selection feedback and future Batching support. Do not register sockets from arbitrary unlinked blocks or impose a hidden first-N rune-count cutoff.

### Transmutation — selected operation, recipes still open

Each dimensional variant enables authored recipes converting **item inputs into item outputs associated with its target dimension**, spending XP through the ordinary forge system. Examples and prices must be selected deliberately; do not enable every item or infer automatic value ratios from rarity or crafting cost.

- Rune of Transmutation: Overworld-target recipes.
- Rune of Nether Transmutation: Nether-target recipes.
- Rune of End Transmutation: End-target recipes.
- All three variants are active at Tier 3, as explicitly selected by the user. They do not require placing the forge in the target dimension unless a future individual recipe deliberately says so.
- What additional copies improve (conversion cost, ratio, throughput or recipe access) remains open; preserve the user's substantial-investment/no-arbitrary-count-cap direction.
- Multiple variants may coexist, each enabling its own recipes. Source inputs, catalysts, target lists, discovery requirements and XP are recipe decisions still to discuss.
- Account for reverse conversions and Master Craftsmanship eligibility when choosing ratios. No direct XP-currency multiplication or unintended item-conversion profit loop.
- These are rune-enabled item conversions, distinct from the Ruined Ancient Forge's existing alloying job. The new candidates below do not change its current recipes.

## Invocation and the Tier 5 progression link — selected direction

The user selected Invocation for Tier 4 in principle and explicitly proposed using **a Forgemaster drop to craft the Sky Keystones needed for Tier 5**. The progression is now:

**Validated Tier 4 forge → Invocation ritual/seal → Forgemaster encounter → boss material → Sky Keystone crafting → Tier 5 canopy.**

The drop's identity, name, amount, whether it replaces glowstone or supplements existing ingredients, and the Sky Keystone batch/XP costs remain open. Do not present a named boss item or exact recipe as approved. Invocation's rune and the first seal must be obtainable before this boss material exists, avoiding a circular unlock.

Recommended balance target, not yet approved: one normal victory should provide enough material for the required sixteen Sky Keystones; repeat encounters can supply other future forge recipes. This respects the user's earlier concern about substantial effort having a worthwhile payoff. Boss difficulty, repeat summoning costs and loot still need separate design.

The other preceding Tier 4 candidates (Conjuration, Refinement, Adaptation, Liquefaction and Solidification) remain unselected. The user did not like most of that group; do not carry them into this new shortlist as approved features.

## Tier 4 and Tier 5 — current selected roster

The user selected Collection, Specialization, Familiarity and Deliberation, then selected **Alternation** and authorized moving **one or two** Tier 4 runes into Tier 5 before discussing Tier 6. The chosen placement is:

| Tier | Selected runes |
|---|---|
| 4 | Batching, Invocation, Collection, Deliberation |
| 5 | Specialization, Familiarity, Alternation |

Moving Specialization and Familiarity keeps the recipe-family and recipe-streak XP behavior together at Tier 5. This is an authorized design placement, not an implementation change or permission request. Exact craft costs, activation code and rune recipes remain future work.

Compression, Amplification, Convergence, Challenge, Reconstruction and Safekeeping remain rejected, as do Reanimation, Engraving and Seals. The first Tier 5 set (Retention, Versatility, Counterbalance, Economy, Equivalence, Exchange, Carryover and Signalling) was rejected. In the next set, only Alternation was selected; Solar/Lunar/Storm Forging, Renewal, Rejuvenation, Cooperation, Intricacy and Attunement were not selected.

### Collection — selected effect

Move finished forge output into a chest directly above the rune or connected item pipes, freeing the center for subsequent crafting. Keep output in the forge if the destination cannot accept it; never overwrite inputs or lose items. Additional runes offer output access/storage-routing points, rather than a first-N count cutoff. Exact filters, fair distribution, capabilities and chest interactions remain to design.

Collection concerns actual finished items, distinct from Tier 1 Extraction's XP export. It supports Batching's input handling without introducing the rejected remote delivery or automatic order-planning systems.

### Specialization — selected Tier 5 effect

Select a recipe category. Crafting in that category costs less XP, while crafting outside it costs more. Additional runes strengthen the specialization, including its tradeoff; precise curves remain open. Define categories from recipe data so new content can be added without hard-coded item lists.

Category selection and reconfiguration need deliberate design. Freeze the selected category and effective price for an active job; do not allow free per-hit switching to capture every bonus while avoiding every penalty.

### Familiarity — selected Tier 5 effect

Repeated successful crafts of the same recipe build an XP discount. Switching recipes resets the accumulated benefit. Extra runes improve how familiarity develops or its strength; the exact curve is not yet approved. This rewards sustained production and complements Batching.

Recipe identity, successful-craft counting, persistence and the exact reset trigger remain to design. Do not grant progress from failed hammer attempts or let a client decide the discount. The prior Retention proposal was rejected; do not silently preserve Familiarity across recipe changes.

### Deliberation — selected effect

Require additional hammer swings in exchange for a reduction in the craft's **total XP cost**. Do not accidentally make a supposedly cheaper craft cost more overall merely because there are more paid hits. More runes deepen the slower/cheaper tradeoff; exact curves and interaction order with Swift, Familiarity and Efficiency are open.

Keep the existing Swift rule: every installed Swift rune adds its surcharge even after the one-swing floor. Calculate the full effective job price/work consistently rather than silently dropping surplus Swift cost. Fractional XP accounting remains relevant when distributing the discounted total across more swings.

### Alternation — selected Tier 5 effect

Consecutive crafts that change recipes build an XP discount; repeating the same recipe resets this buildup. More runes strengthen this production style, with exact scaling open. Familiarity rewards repeated production; Alternation rewards variety. Neither choice is an arbitrary count cap.

Price each new job from the streak applicable to that job: do not accidentally apply the preceding recipe's Familiarity discount to a switched recipe and then also award Alternation. Recipe identity, whether a longer sequence must contain distinct recipes rather than just alternate two recipes, growth/decay and save data remain design details. The user selected the general behavior, not these unsettled details.

## Tier 6 selection — Mutation, provisional details

The user selected **Mutation** as the third Tier 6 rune, saying it is good for now and can be revisited. Tier 6 has **Master Craftsmanship, Grand Pattern and Mutation**. The initial forge roster is complete for the current three-per-tier exercise.

Selected Mutation concept: **supported Transmutation recipes may produce a rarer alternative item instead of their normal result**. This changes the output type; it is not automatically an extra copy, an improved numerical modifier roll or a guaranteed global rarity upgrade.

Keep its details open:
- Which Overworld, Nether and End transmutation recipes support Mutation.
- Exact alternative items, rarity ordering and weights.
- Rune-count scaling, chance limits and benefits of further investment, respecting the no-arbitrary-first-N-cap direction.
- Any XP premium, catalyst or additional material requirement.
- Interaction/order with Master Craftsmanship and other job-pricing/output effects.
- Mutation rune ingredients, crafting work and XP/hit.

Use authored alternative-output data if implemented; do not automatically infer value from item names or allow arbitrary modded outputs. Recipes without a declared mutation outcome keep their ordinary result. These are implementation principles for later design, not approved weights or conversion ratios. The user has explicitly deferred a detailed revisit.

The preceding Tier 6 sets were rejected: Perfection, Legacy, Empowerment, Crescendo, Overstrike and Multidiscipline; then Imprinting, Tethering, Recall, Salvation, Duality and Projection. The last set's other options (Frugality, Breakthrough, Remnants, Forgemaster's Favor and Greater Transmutation) were not selected. Do not carry these forward as approved features.

## Initial selected forge roster

Sky Keystone remains a structural Tier 5 requirement and is excluded from these counts. Vigilance remains a separate general-purpose chunk-loading rune.

| Tier | Count | Selected functional runes |
|---|---:|---|
| 1 | 3 | Storage, Extraction, Input |
| 2 | 3 | Swift Forging, Tempering, Conduit |
| 3 | 6 | Restoration, Efficiency, Expansion, Transmutation (Overworld), Nether Transmutation, End Transmutation |
| 4 | 4 | Batching, Invocation, Collection, Deliberation |
| 5 | 3 | Specialization, Familiarity, Alternation |
| 6 | 3 | Master Craftsmanship, Grand Pattern, Mutation |
| Total | 22 | At least three per tier; effects and recipes still require detailed design before implementation |

## Decisions still open

- Continue recipe review one rune at a time. Storage ingredients/layout and Extraction's recipe are agreed. Input's Dropper-centered recipe is the next proposal. The user deferred XP-value tuning; remaining work/cost details can be reviewed later.

- Mutation's alternatives, probabilities, scaling, costs and recipe; deliberately deferred for a later revisit.
- Numerical curves/configuration for Tier 5 Specialization, Familiarity and Alternation.
- Collection and Deliberation scaling/recipes at Tier 4.
- Forgemaster drop identity/quantity and the revised Sky Keystone recipe; whether one normal kill should cover all sixteen required keystones.
- Invocation seal ingredients/XP, boss difficulty, repeat encounters and rewards.
- Expansion socket placement, ingredient addressing and interaction with expanded recipes/Batching.
- Transmutation source/target recipes, catalysts, XP costs and extra-copy benefits. Tier 3 for all three variants is settled.
- Vigilance's standalone general-purpose design and progression tier, separate from the forge.
- Numerical tuning, fractional XP accounting and future hammer interactions.
- Restoration's base repair rate, XP per durability point and recipe; automatic stored-XP operation is accepted in principle.
- Master Craftsmanship beyond 100%: guaranteed extra batches versus staying double-only with diminishing chance.
- Batching's template item interpretation, recipe-variant selection and output collection/re-arming policy.
- Grand Pattern's recipe, placement confirmation, optional decorative floor handling and relocation XP cost.

This document records **selected design directions**, provisional numbers/recipes and deferred questions. It does not authorize or imply that all twenty-two rune blocks have been implemented.
