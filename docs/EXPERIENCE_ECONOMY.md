# Experience economy reference

Direction: player experience is intended to become the mod's main working resource. This first step powers Ancient Anvil hammer work; it does not change unrelated progression or add automatic XP production. Numbers are intentionally initial values to tune as content grows.

| Craft | Output | Iron hammer hits | XP per hit | Default total XP |
|---|---:|---:|---:|---:|
| Runestone: four corner deepslate/cobbled deepslate + center gold ingot | 4 Runestone | 16 | 10 points | 160 points |
| Canopy Keystone: existing ingredients | 4 keystones | 8 | 1 point | 8 points |

## Server recipe data

Anvil recipes may set `xp_per_hit` to an integer from 0 to 100000. Omission inherits `AnvilRecipes.DEFAULT_XP_PER_HIT`, initially 1. A zero override permits a free recipe. Costs reload with the normal server datapack reload; client settings cannot reduce them. The Runestone recipe explicitly sets `work: 16`, `xp_per_hit: 10`, and result count 4. Keep the generator in sync when changing built-in recipes.

## Payment rules

Only accepted, valid hammer strikes pay, spending core storage before the player pays the shortfall. The entire combined price must be affordable before either balance changes. Wrong ingredients, output waiting to be collected, insufficient anvil/hammer tiers, cooldown rejection and no-XP attempts never consume XP or durability. Insufficient XP preserves partial work and inputs and does not advance the strike cooldown. A completed craft consumes the final hit's XP before producing output, once. Creative smithing bypasses both XP balances and durability costs. Placement guides and inventory construction are not smithing and have no XP cost.

Spendable points come from the player's current level and progress bar using vanilla's piecewise level curve. Do not use `Player.totalExperience`: enchantment/level spending can leave that historical counter higher than the current balance. Vanilla point subtraction handles level-boundary changes and HUD synchronization.

Future hammer tiers may produce more work per accepted strike. Costs are per strike, so future efficiency and recipe XP pricing should be balanced together. This reference establishes the resource direction without preselecting later progression rewards.

## Stored XP (implemented)

Base capacities for actual tiers 0–6: 250 / 1,000 / 2,500 / 5,000 / 10,000 / 25,000 / 50,000 points, centralized in `AncientAnvilBlockEntity.experienceCapacity(int)`. These are initial balance values. Empty-main-hand right-click on the lower body deposits up to 10 current player XP (eight-tick cooldown); there is no withdrawal control yet. The top and Shift-use guide retain their existing purposes.

The reservoir persists through reload and structure damage. Downgrades preserve over-capacity points but stop deposits; breaking the anvil releases XP as native orbs once. Storage insert/extract APIs support simulation for future integrations, but no external capability or liquid-XP conversion is exposed yet. Rune effects, transfer automation and rune recipes remain proposals in [FORGE_RUNE_PROPOSAL.md](FORGE_RUNE_PROPOSAL.md).
