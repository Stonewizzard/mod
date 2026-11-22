# Forge runes: implementation and first balance pass

All 22 forge rune ingredient layouts in FORGE_RUNE_PROPOSAL.md are implemented. Sky Keystone retains its `canopy_keystone` saved ID, with its agreed Tier 4 anvil recipe and output of four. Vigilance is a separate general-purpose chunk loader, outside this forge implementation.

Replace full Runestone positions, including the higher rings and canopy. The required tier gates the effect. There is no first-N rune limit. Ordinary full Runestone and inactive higher-tier runes still support the structure. Slabs, metal caps, crystals, lens glass, fluids and keystones keep their own requirements. Every effect is tied to exact structural positions, not a radius search for arbitrary blocks. A rune binds to one anvil at a time.

## Provisional numbers

Rune crafting: `16 + 4 * tier` swings, `10 * tier` XP per hit; Sky Keystone 32 swings, 40 XP/hit. All are datapack values. These are starting values, not previously approved balance. The original blank Runestone remains four output, 16 swings, 10 XP/hit.

- Storage: +1,000 XP each, on top of existing tier capacity. Downgrades preserve excess XP.
- Input/Extraction: 40 XP/tick shared per direction across all ports. Standing transfer happens every 10 ticks, 10 points or 40 when crouching. Each Conduit adds the base rate again. Standing on Extraction deposits; standing on Input withdraws, as requested.
- Swift: subtract one swing to a minimum of one; all Swift runes add 5% to per-swing price, including surplus. Less work can reduce total XP.
- Tempering: one percentage point less hammer wear each; zero at 100. Fractional wear accumulates.
- Efficiency: 1% less XP each to 50%; thereafter price multiplier `50 / (runes + 50)`. Fractional XP uses persistent prepayment credit, so rounding cannot erase small benefits or mint XP.
- Deliberation: one extra swing each, with total craft XP divided by `1 + .02 * count`. Its discount is applied to the entire job, not merely each strike.
- Restoration: one durability per rune per 10 ticks, one stored XP per durability. Only a lone damaged center item is repaired. Its other components survive.
- Specialization: selected recipe category costs `1 / 1.01` per rune, other categories cost `1.01` per rune. Shift-click a result to select; empty shift-click clears.
- Familiarity/Alternation: price multiplier `1 / (1 + .002 * runeCount * streak)`, streak contribution up to 100 crafts. All rune counts still contribute. Same recipe increments Familiarity; a repeat resets Alternation, a change increments it. Switching recipes resets Familiarity.
- Master Craftsmanship: 1% extra-batch chance each; every full 100 grants an additional whole batch. Overflow is retained in a persisted output queue. Only datapack recipes explicitly opting into `allow_bonus` receive it. Recycling, conversions and keystones are excluded by default.
- Mutation: replacement probability `n / (100 + n)` on recipes specifying `mutation_result`; no extra free copy.

## Data and automation

Anvil recipes support `category`, `requires_rune`, `extra_ingredients`, `mutation_result`, `allow_bonus`, `work` and `xp_per_hit`, alongside existing fluids and ingredients. Expansion rune sockets are ordered by position; each provides one extra physical input. Default recipes use the original thirteen positions.

Batching: shift-click a recipe's result on any face to choose its exact template; repeating cycles same-result variants. Only matching ingredients enter its buffer. A chest above supplies one real item per pull. An anvil coordinator fairly stages a ready recipe from same-template buffers; different templates cannot mix or spend the same source item twice. It never hammers. Collection sends finished items into a chest above, otherwise its own pipe-accessible buffer. Full destinations pause safely.

NeoForge 26.3 uses the transactional `ResourceHandler` API, not the older `IItemHandler` API. Rune inventories expose the standard item capability. XP ports expose the standard fluid capability with `examplemod:experience`, tagged `c:experience`, at **1 mB = 1 XP point**. Import accepts tagged, component-free XP fluids; export uses this mod's fluid. A mod using a different unit or tag needs a datapack/integration adapter; there is no universal cross-mod XP unit. Import and export budgets are shared per core and transactional rollback restores both XP and budget. Compatibility with a particular third-party pipe mod is not claimed without testing that mod.

Grand Pattern moves actual required blocks, their block entities, stored XP, work, rune buffers/templates, fluid blocks and four normal End Crystals. It excludes external chests and decorative terrain. It preflights loaded/buildable space and protection, refuses foreign containers at structural positions, and consumes its packed item even in Creative to avoid duplication. Original orientation is retained. The payload is bounded to avoid giant inventory packets. Optional rotated placement and a ghost preview for the packed structure are future UI refinements.

## Provisional progression content

Invocation unlocks a starter Forgemaster Seal recipe: Blaze Powder center; Iron Blocks north/south inner, Ancient Ingots east/west inner, Tier 4, 24 hits at 30 XP. Use the seal on an active Invocation anvil with clear space seven blocks in front. The initial Forgemaster is an iron-golem-bodied fireproof boss with 350 health, armor, melee and a flame-telegraphed ground slam. It drops four cores to unlock all sixteen Sky Keystones from one victory. The encounter, art and loot count are provisional; four cores was not an earlier approved loot quantity.

Starter transmutations (Tier 3, eight hits, eight XP/hit) demonstrate the three domains: Netherrack → Cobblestone; Cobblestone → Netherrack; Cobblestone → End Stone. Their Mutation replacements are Amethyst Shard, Quartz and Chorus Fruit. These example conversions/outcomes are new provisional choices, not previously reviewed recipes. Ancient and Energized Crystal survival acquisition remains creative-only as previously requested; the rune implementation does not silently invent that progression.
