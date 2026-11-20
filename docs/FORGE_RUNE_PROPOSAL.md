# Forge rune proposal — discussion, not implemented

The user requested discussion of tiers, recipes and effects before adding rune blocks. Only the Runestone price (10 XP/hit) and tier-scaled core storage are implemented in this iteration. “Forge” below refers to the Ancient Anvil and its ritual structure, not the Ruined Ancient Forge alloying station.

## Shared rules

- Functional runes replace eligible Runestone positions in the existing validated structure. Blank Runestone remains valid. Each coordinate counts once, for one core; shared/overlapping structures must not multiply the same rune's effect.
- An active rune needs both a valid position and its minimum **actual** structure tier. A higher-tier rune below that tier remains a structural block but contributes no effect. Decorative off-structure copies do nothing.
- Separate the tier where a rune is crafted from the tier where it activates. Craft Tier 1 runes at Tier 0, Tier 2 at Tier 1, and so forth, so players can prepare the next upgrade. Building tiers still depends on the existing structure, not these optional effects. Sky Keystone is the one explicit structural exception.
- Ordinary upgrades consume one blank Runestone and produce one functional rune. Materials below are proposals; use exact physical anvil positions in the eventual recipe JSON. Existing slots support up to thirteen workpieces; the largest proposal here needs nine.
- Each recipe owns its work and XP/hit. Effects modify effective swing count and XP/hit together. Show both figures and total XP in the manual/recipe preview. Lock the effective recipe parameters for the active craft; changing runes mid-craft pauses until that setup is restored or ingredients are reset, preventing cheap-hit/fast-finish swapping.
- Starting limits are deliberately conservative and tunable. Higher tiers unlock more choices; they should not require installing every rune. No rune passively creates XP or rewards standing still. Storage and transfer throughput are separate upgrades.
- Reserve mandatory Sky Keystone positions for their structural purpose. Ordinary runes replace Runestone, never lens glass, obsidian, energized caps, required fluids or keystones.

## Requested runes

Costs and caps below are proposals, not changes to current recipes. All XP figures are **points**, never levels. All ordinary recipes include the one blank Runestone listed in the shared rule; extra materials are below.

| Rune | Active tier / craft tier | Proposed effect | Proposed extra ingredients | Swings × XP/hit → output |
|---|---|---|---|---|
| Storage | 1 / 0 | +1,000 capacity each; first 16 count. No free XP or transfer speed. | 2 ancient ingots + 1 lapis lazuli + 1 gold ingot | 8 × 5 → 1 |
| Extraction | 1 / 0 | Pipe **export** from core storage. Standing on it deposits player XP into the core: 2 XP/second, crouch 10/second. | 1 hopper + 1 ancient ingot + 1 lapis | 8 × 8 → 1 |
| Input | 1 / 0 | Pipe **import** into core storage. Standing on it withdraws stored XP to the player at the same rates. | 1 hopper + 1 ancient ingot + 1 redstone dust | 8 × 8 → 1 |
| Swift Forging | 2 / 1 | Each rune removes one required swing (minimum 1), and adds **10% of base XP/hit**. First 8 count. | 1 sugar + 1 redstone dust + 1 ancient ingot | 12 × 10 → 1 |
| Efficiency | 4 / 3 | Each rune reduces the **final XP/hit, including Swift surcharge, by 10%**; first 5 count (50%). Discounts add within this family, then multiply the Swift-adjusted price. Positive-price recipes stay at least 1 XP/hit. | 1 diamond + 2 ancient ingots + 1 lapis | 16 × 15 → 1 |
| Keystone Rune of the Sky | enables 5 / 4 | Renames/replaces Canopy Keystone. The existing sixteen required canopy nodes enable Tier 5; no additional effect. Preserve old worlds and recipe IDs. | Keep existing batch: 4 Runestone + 4 ancient ingots + 1 glowstone dust | Keep 8 × 1 → 4 initially; review price separately |
| Master Craftsmanship | 6 / 5 | +1% chance per rune to double a completed recipe's base output, capped at 10%. Roll once per craft; a doubling never recursively rolls. | 1 ancient crystal + 1 diamond + 2 ancient ingots | 24 × 25 → 1 |

### Speed/efficiency calculation

For a future recipe requiring `S` base swings and `C` XP/hit:

- `S_effective = max(1, S - min(swiftCount, 8))`.
- `C_effective = ceil(C × (1 + 0.10 × min(swiftCount, 8)) × (1 - 0.10 × min(efficiencyCount, 5)))`.
- Preserve explicitly free recipes at zero; otherwise clamp to at least 1. Round only once at the end.

Example: Runestone is 16 swings × 10 XP = 160 XP. Four Swift runes give 12 × 14 = 168 XP. Four Swift + five Efficiency give 12 × 7 = 84 XP. Efficiency without Swift gives 16 × 5 = 80 XP: efficient crafting and faster crafting remain distinct choices, and using both is powerful. Eight Swift give 8 × 18 = 144 XP: speed investment eventually also saves total XP, even though each swing costs more. The user explicitly approved allowing heavy Swift investment to save total XP. The user also emphasized that Efficiency must remain useful; the stronger final-cost discount above is the revised proposal, not an implemented rune effect.

Future hammer work/swing must compose explicitly with these formulas. Do not silently treat work units as swings for hammers that provide more than one work. The current Iron Forge Hammer provides one.

### Transfer behavior and compatibility

The Extraction/Input naming follows the user's requested pipe directions, while player standing transfers move the other way relative to that player. Manual text and arrows on the blocks must say **deposit** or **withdraw**, plus **pipe export/import**, so players never have to infer direction from the name alone. Consider renaming later to Offering/Draw if the current names prove confusing.

Standing transfer needs the actual player above an active, linked rune. Cap rate per player per core, not per touching rune, and never auto-transfer a Creative player's free XP. Crouching increases rate rather than starting a second transfer. Use an exact integer debit/credit, no loose orbs, with balances/capacity checked before either side changes. Simultaneous opposite runes must not bounce the same points back and forth; choose the single rune directly below the player.

There is no universal XP-pipe unit across mods. Plan an internal point-based storage API plus explicitly supported NeoForge capability/adapters. If a mod represents XP as fluid, require a configured point-to-fluid conversion and preserve fractional remainders; never pretend arbitrary fluid or Forge Energy is XP. Export must respect simulation, bandwidth and stored points. Unknown pipes simply do not connect. Do focused adapter smoke checks when those integrations exist; do not promise all pipe mods work now.

### Duplication limits

Master Craftsmanship only doubles eligible completed **base outputs**, not transferred XP, stored XP, prior bonuses or structure refunds. Default-disable it on currency conversion, salvage/recycling, reversible conversion, and recipes containing reusable catalysts. Evaluate every recipe alongside artifact melting, Cruncher recovery and the 9-scrap/netherite conversions to prevent material-profit loops. Keep chance/eligibility in server recipe/rune data. Do not consume a random roll on a failed hammer hit.

Ancient crystals are currently Creative-only. Master Craftsmanship therefore remains survival-inaccessible until crystal acquisition is designed; do not add an accidental permanent survival gate around it.

## Additional candidates by tier

Each candidate below is an alternative choice, not a commitment to add every block. All proposed recipes yield **one rune** and consume one blank Runestone plus the listed extras. No new fluid or progression ingredient is required.

| Active tier / craft tier | Candidate | Role and initial limit | Extra ingredients | Swings × XP/hit |
|---|---|---|---|---|
| 2 / 1 | Tempering | 5% chance per rune to avoid hammer wear, cap 50%; accepted strike still spends full XP. | 2 iron ingots + 1 copper ingot | 10 × 8 |
| 2 / 1 | Conduit | +2 XP/second to transfer-rune throughput per rune, cap +20; never changes capacity or creates XP. | 2 copper ingots + 1 redstone | 10 × 8 |
| 2 / 1 | Resonant Sight | One rune projects recipe positions and missing ingredients onto the surface; more copies add no effect. | 1 glass block + 1 lapis + 1 gold ingot | 8 × 10 |
| 3 / 2 | Workpiece Memory | Preserve a paused recipe's earned work when its ingredients are lifted, then resume only with the exact same recipe/configuration and required items restored. One remembered job per core; no free input/output copying. | 1 book + 1 lapis + 2 ancient ingots | 12 × 12 |
| 3 / 2 | Rhythm | Rewards accurately timed manual strikes with a slightly shorter strike cooldown; cap 25%. Never adds automatic swings or bypasses payment. | 1 clock + 1 copper ingot + 1 redstone | 12 × 12 |
| 3 / 2 | Basin Attunement | Select a satisfied recipe fluid profile when several recipes share inputs, using a visible marker/cycle interaction; no fluid generation or recipe requirement bypass. One rune. | 1 glass block + 1 gold ingot + 1 lapis | 12 × 10 |
| 4 / 3 | Pattern | Stores one selected recipe as a ghost placement pattern. Shows required tier, ingredients, swings and XP; consumes nothing until a normal craft. One recipe per rune, cap 4. | 1 paper + 1 diamond + 1 ancient ingot | 16 × 12 |
| 4 / 3 | Batch | Repeat the selected recipe from an explicitly loaded ingredient buffer; each batch still needs normal paid hammer strikes. One pending batch per rune, cap 4. No automatic hammering or remote inventory scanning. | 1 chest + 1 hopper + 1 ancient ingot | 16 × 15 |
| 4 / 3 | Reserve | Select a protected XP floor that external export and standing withdrawals cannot cross; smithing can use it. One core-wide setting, no extra effect from duplicates. | 1 comparator + 1 gold ingot + 1 ancient ingot | 12 × 15 |
| 5 / 4 | Sky Conduit | Adds transfer range to a linked workshop XP connection, +4 blocks/rune, cap 16; only between owned/explicitly paired nodes, no extra XP. Requires later linking support. | 1 ender pearl + 1 copper ingot + 2 ancient ingots | 20 × 18 |
| 5 / 4 | Crystal Anchor | Protects the ritual's own blocks/nearby normal End Crystals from crystal-chain explosion damage while active; does not make players or the world explosion-immune. One rune; behavior needs a deliberate End Crystal safety decision. | 1 obsidian + 1 ender pearl + 2 ancient ingots | 20 × 20 |
| 5 / 4 | Precision | Shows the expected output and eligible bonuses before the final strike; warns if there is no room for a doubled stack. More copies add no effect; does not improve odds. | 1 spyglass + 1 diamond + 1 ancient ingot | 16 × 18 |
| 6 / 5 | Spectrum | Saves up to four approved lens colors/recipes as selectable visual presets; swapping still requires a valid single-color lens and never overrides recipe restrictions. Cosmetic plus setup assistance. | 1 glass block + 1 glowstone dust + 2 ancient ingots | 24 × 20 |
| 6 / 5 | Grand Pattern | Saves/restores one complete eligible rune layout as a projection. Building still uses actual inventory blocks, accepted placement and normal effects. One rune. | 1 map + 1 ender pearl + 2 ancient ingots | 24 × 25 |
| 6 / 5 | Concordance | Diversity bonus for Master Craftsmanship: one rune grants +2 percentage points to the completion doubling chance if at least four other distinct functional rune families are active. Does not stack with itself; total doubling chance capped 12%. No XP discount, so Efficiency keeps its own role. | 1 ancient crystal + 1 lapis + 1 diamond + 1 ancient ingot | 24 × 25 |

## Recommended first implementation after discussion

Start with Storage, Extraction, Input and Swift at their proposed tiers; settle the transfer naming first. Rename Sky Keystone without changing its art/role or invalidating saves. Add Efficiency and Master Craftsmanship only after confirming caps, speed-versus-total-XP behavior and duplication eligibility. Among optional candidates, Tempering, Conduit, Pattern and Reserve have the clearest distinct roles. Avoid adding weak cosmetic/stat runes merely to fill every tier; optional choices should earn their footprint.

Swift saving total batch XP at high investment is approved in principle. The strongest unresolved questions are the revised Efficiency strength/cap, whether Master Craftsmanship can affect functional rune recipes, and which XP-pipe mod to support first. The recipes above are starting proposals for that discussion, not approved progression gates.
