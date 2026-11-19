# Tier 6 — Ancient Convergence

Tier 6 upgrades the existing Ancient Anvil workshop. All lower structural tiers, the four independent fluid basins and the 25-block-wide Tier 5 canopy remain required. The forge/fireplace is unchanged; hammer crafting still occurs on the Ancient Anvil.

## Required structure

| Location | Tier 6 requirement | Count |
|---|---|---:|
| Caps of the four 2×2 corner towers | Energized Ancient Crystal Blocks, replacing the 16 metal caps | 16 |
| Tops of the four outer single-block pillars | Obsidian, replacing the four metal caps | 4 |
| On those obsidian caps | Normal End Crystal entities, placed by the player with End Crystal items | 4 |
| Open central diamond inside the canopy | Full glass blocks, all one color, at the canopy's existing y=16 plane | 25 |
| Existing canopy rune nodes | Retain every Canopy Keystone | 16 |

Measurements are relative to the anvil's placed core. Tower caps remain at y=3. Corner cap cells are x/z ±6 and ±7. Outer pillar caps remain at radius 16, y=3. The lens is the diamond `abs(x)+abs(z)<4` at y=16. No new Runestone layers are added. White/red/blue/etc. stained glass, clear glass and tinted glass are supported; glass panes are not. Every lens cell must have the same glass color/type. The guide uses the majority existing lens color as its sample, so one mistaken block is highlighted as one repair.

The normal End Crystals retain vanilla placement and damage/explosion behavior. The anvil does not spawn, bind, shield, replace or consume them. Removal of a crystal, a missing cap, mixed glass, or damage to any required lower tier disables Tier 6 until repaired.

## Guides and construction

Shift + right-click toggles the guide. Hold while aiming at the anvil to place inventory blocks one at a time, with normal placement sounds and protections. Remove the old metal caps yourself first; auto-building never destroys solid blocks or containers. The Tier 6 guide stays selected while cap replacements are incomplete, provided the rest of the lower-tier shell and canopy are intact.

Place one glass sample first to choose a color, or hold the desired glass while starting an empty lens. Later placements match that color. Wrong-color glass must be removed manually. End Crystals remain manually placed; the builder never consumes their items or filled buckets. Energized caps and obsidian count as the appropriate upgraded lower-tier caps without widening the generic metal-cap tag.

## Energy network

- Exactly 16 energized-cap-to-keystone beams form a minimum-distance one-to-one assignment. A keystone is never reused. Layout changes supplied through the stage datapacks are synchronized to remote clients.
- Each of the four normal End Crystals has two visual beams to its two adjacent 2×2 tower centers, forming eight V connections.
- A successful hammer strike on a valid recipe at a completed Tier 6 anvil sends a large energy column down from above the canopy, through the glass, to the anvil. The column uses the chosen glass color. Invalid recipes, insufficient hammer tiers and hits rejected by the normal cooldown do not trigger it.
- The column is a visual effect only: animated render geometry, particles and sound. It has no damage, explosion, block edits, collision, lightning or spawned beam entities. A short pulse is synchronized through the anvil block entity; completed crafts hold the visual slightly longer.

Removing the anvil leaves all externally placed blocks and End Crystals intact. Existing lower-tier crafts continue to work at Tier 6; no new crafting payoffs or energizing recipes are added in this structure-focused change.

## Optional structural ideas, not implemented requirements

1. Four short focusing prongs or suspended rune brackets beneath the canopy, leaving the central glass and beam paths clear.
2. Small stepped obsidian/Runestone collars below the outer crystal caps, to visually support the crystals.
3. Four entrance stairways from the lower circular ring toward the raised workshop, with matching gateposts. This would help the finished structure feel like a place players enter and work in.

The geometry is in `anvil_stages/tier_6.json`; cap/glass materials use dedicated block tags. Existing Tier 3–5 geometry is unchanged. `AnvilTierSix` handles uniform glass, ordinary entity detection and beam topology; the client extracts immutable beam snapshots before rendering. Recipes may now request `anvil_tier: 6` in datapacks.
