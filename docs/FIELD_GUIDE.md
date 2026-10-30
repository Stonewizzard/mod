# Workshop Manual

The Workshop Manual is the selected guidebook. Craft a book and one paper, or find it in the creative tab. Right-click to read. Global search, named article sections, related links, Back, mouse-wheel pagination and native keyboard focus are supported. The book remembers its place during the session.

Native item graphics identify categories, task shortcuts, every entry and search result, modifier topics, related links, and article headings. Seven full-height illustrated results fit each page, including at narrow GUI widths. Each chapter supplies one `icon` registry ID reused wherever that subject appears.

Modifier entries open with their Effect; Value tiers, Growth and Resources are separate sections. Actual rolls stay on equipment tooltips. Reading changes no items. See [the authoring guide](GUIDEBOOK_AUTHORING.md) and [native-client QA](REAL_CLIENT_QA.md).

The visual direction draws on familiar mod guidebook category browsing, with original presentation code and an ancient archaeology tome inventory icon. No book dependency or copied mod textures is required.

## Contents

- [Start your first excavation](#welcome) — Start
- [Using this field guide](#book-controls) — Start
- [Sniffers and ancient debris](#sniffers) — Creatures
- [The Sniffer Head](#sniffer-head) — Creatures
- [Crunchers](#crunchers) — Creatures
- [Hatching a Cruncher egg](#cruncher-egg) — Creatures
- [Ancient geodes](#ancient-geodes) — Artifacts
- [Sculk parasites](#sculk-parasites) — Creatures
- [Artifacts and rarity](#artifacts) — Artifacts
- [Rock matrices and hidden finds](#rock-matrices) — Artifacts
- [The Archaeology Table](#archaeology-table) — Excavation
- [Choosing excavation tools](#excavation-tools) — Excavation
- [Field-tool upgrades](#tool-tiers) — Excavation
- [Quality and scaling grades](#scaling-and-quality) — Equipment
- [Reconstructing equipment](#reconstruction) — Equipment
- [The Ruined Ancient Forge](#ruined-ancient-forge) — Stations
- [Ancient metal and alloying](#ancient-alloy) — Stations
- [The Ancient Anvil](#ancient-anvil) — Stations
- [The reconstructed pickaxe](#pickaxe-core) — Equipment
- [Tool levels and mastery](#levels) — Equipment
- [The Ascender](#ascension) — Stations
- [Reading modifiers and value tiers](#modifier-basics) — Equipment
- [Resonance and combinations](#resonance) — Equipment
- [The Influencer](#influences) — Stations
- [The Unveiler and ancient runes](#unveiler) — Stations
- [The Anuller](#anuller) — Stations
- [The Locker](#locker) — Stations
- [The Scaling Randomizer](#scaling-randomizer) — Stations
- [The Restorer](#restoration) — Stations
- [The Corruptor](#corruption) — Stations

- All 120 modifiers: generated in the in-game Modifiers category from the same definitions and tuning values used by gameplay. See also [the complete modifier table](PICKAXE_MODIFIERS.md).

<a id="welcome"></a>

## Start your first excavation

Hatch a Sniffer and expose ancient debris. Bring its artifact to an Archaeology Table. Uncover a component, then use a Reconstruction Table to assemble equipment.

Start with a Sniffer. Expose ancient debris on a walkable surface for it to investigate. Its finds lead to artifacts and, occasionally, a Cruncher egg.

Use the Archaeology Table to excavate finds, then the Reconstruction Table to assemble components. Your excavation quality affects every modifier that the component contributes.

Choose a category or search from any page. Articles have named sections: read the overview first, then open details when you need them. Back returns to your previous search or article.

This book contains general knowledge. Your item tooltips show the values actually rolled on your own components and tools. Unread ancient runes stay hidden until you unveil them.

<a id="book-controls"></a>

## Using this field guide

Craft a normal book with one paper for the Workshop Manual, or find it in the creative tab. Item pictures identify the subjects in Contents, entry lists, search results and article headings.

Home opens the Manual's contents. Categories cover creatures, artifacts, excavation, equipment and customization stations. Modifiers opens a topic index.

Search looks through the whole guide, including descriptions and modifier IDs. All words must match. Clear empties search. Arrows and the mouse wheel turn pages. Select a named section to read details. Related opens connected articles; Back restores your previous view.

Escape or the close button closes the book. Reading never consumes the book, changes equipment or rolls an artifact. The last page and search are remembered during the current play session.

Hover a reconstructed tool and hold Shift while scrolling to browse its modifier pages. Effective values and grades remain on the tool; definitions and rules live here.

Craft ancient field guide: book + paper, in any arrangement.

<a id="sniffers"></a>

## Sniffers and ancient debris

Brush suspicious sand in warm ocean ruins to find a vanilla Sniffer egg. Hatch it and raise the Sniffer. Adults can investigate exposed ancient debris on a walkable surface; buried debris is not a target. Adults breed with torchflower seeds.

When the Sniffer successfully digs at exposed ancient debris, that debris breaks and yields netherite scrap and an artifact. Mob griefing must allow the interaction.

There is also a one-in-fifteen chance of a Cruncher egg from this excavation. Ordinary Sniffer digging still has its normal uses.

Adult sniffers drop a Sniffer Head when killed. The head is a decorative trophy, with wall attachment and sixteen floor rotations.

<a id="sniffer-head"></a>

## The Sniffer Head

The Sniffer Head is a decorative drop from a killed Sniffer. It is also in the creative menu.

Place it on a wall to face directly out from the supporting surface. Floor placement supports sixteen rotations, including diagonals. The back sits flush against its support.

<a id="crunchers"></a>

## Crunchers

The Cruncher is a Nether relative of the Sniffer, with heavy jaws suited to breaking ancient geodes. Hatch its egg to raise one, or use the creative spawn egg for testing.

Crunchers approach geodes from beside the block and crunch them with their jaws. The geode is consumed and produces an artifact; it is not a block the player must stand the Cruncher on.

Crunchers eat wither roses. Their visual design uses obsidian, Nether colors and animated magma veins.

An infected Cruncher has its own sculk transformation and attached parasite appearance. Further specialized infected behavior is planned.

<a id="cruncher-egg"></a>

## Hatching a Cruncher egg

A Cruncher egg can be found when an adult Sniffer excavates exposed ancient debris. Its chance is one in fifteen.

The placed egg progresses through three visible stages. Normal hatching takes about twenty minutes, plus small random stage delays, matching the normal Sniffer egg timing.

Place the egg on warped nylium to reduce the base hatching time to about ten minutes. It does not use the Sniffer egg moss boost.

The final hatch needs clear space for the baby. If the baby would collide, the egg waits and retries instead of losing the hatchling.

<a id="ancient-geodes"></a>

## Ancient geodes

Ancient geodes are obsidian-like relics found as an additional reward from Ominous Vaults. The current extra-geode chance is twenty percent.

Place one where a Cruncher can walk up to it. The Cruncher crunches the geode and releases an artifact suitable for the Archaeology Table.

Geodes and Sniffer debris finds are different artifact sources. Neither source currently supplies Legendary or Unique artifacts; acquisition for those rarities is reserved for later progression.

<a id="sculk-parasites"></a>

## Sculk parasites

Sculk parasites naturally appear in the Deep Dark and Ancient Cities. Natural spawn checks keep their local population sparse. Their movement does not continually trigger sculk vibrations.

A parasite jumps onto a supported mob and merges with it. Players cannot be infected. Infected hostile mobs are more dangerous and reward more experience; normally passive hosts become hostile.

The transformation preserves the host's normal identity and applicable texture variants while adding sculk growth, an attached parasite and animated infection details.

Bosses such as the Ender Dragon are excluded. So are tadpoles, ghasts and happy ghasts, endermites and silverfish, ordinary fish, squid and glow squid, bats, allays and vexes.

Slimes, magma cubes, blazes, breezes and shulkers remain supported. Sniffers and Crunchers use specialized infected visuals.

<a id="artifacts"></a>

## Artifacts and rarity

Artifacts are sealed specimens containing an ancient find in a rock matrix. A Sniffer or Cruncher can recover them. The Archaeology Table reveals the find through a cleaning minigame.

The six artifact rarities are Common, Uncommon, Rare, Epic, Legendary and Unique. Rarity affects available modifiers, the odds of stronger value tiers, modifier-count attempts and scaling grades.

A higher rarity offers better opportunities, not a guaranteed perfect component. Every modifier family has its own tier bands and rarity weights. Each modifier entry lists those weights.

Current Sniffer finds: seventy percent Common, twenty-five percent Uncommon and five percent Rare. Cruncher geode finds: eighty percent Rare and twenty percent Epic.

Legendary and Unique acquisition is reserved for later progression. They are available in creative mode. This guide does not add drop chances for them.

<a id="rock-matrices"></a>

## Rock matrices and hidden finds

Artifacts may be encased in sandstone, shale, deepslate, basalt or obsidian. Harder matrices need more clearing and put more emphasis on the right tool for each layer.

Rarer specimens favor tougher matrices. The rock is a shell around the find; its material is not the same thing as a Nether or End influence.

Potential recovered finds include pickaxe head pieces, a connector, a handle, other equipment heads and blades, resonant cores and salvage materials.

Once a specimen is sealed, its study is stable. Completing an excavation and claiming the reward does not let you repeatedly reroll the same find.

<a id="archaeology-table"></a>

## The Archaeology Table

This two-block workbench holds an artifact and four field tools: drill, brush, hammer and magnifying lens. Items stored in it appear physically on the tabletop.

Put an artifact in the artifact slot. Switch to the excavation workspace for a large view of the encased find. The inventory view is still available for moving tools and rewards.

Pick up a tool from the field kit and click or drag over the specimen. Clicking another tool returns the held tool to its proper position and switches to the new one.

Clear the covering rock while preserving the inclusion. Thick crust calls for the hammer or drill; loose material near the find is safer to brush. The lens helps identify hidden material.

When the table says Ready to recover, use Recover with the output slot empty. Take the recovered component before starting another reward. Table contents persist when the screen is closed.

Craft archaeology table: grid rows PPP / SIS / S.S. A dot means an empty cell. P = any planks; S = stick; I = iron ingot.

<a id="excavation-tools"></a>

## Choosing excavation tools

Drill: precise removal for working through cover. Use care near delicate material.

Brush: gentle cleaning of loose sediment, suited to exposed edges and the final pass.

Hammer: broad removal of thick crust. Efficient for hard cover and risky near the inclusion.

Magnifying lens: inspection that helps reveal where material lies beneath the surface without removing that cover.

Tool choice, covering thickness and how close you are to the find matter more than simply repeating the same action everywhere. Damage reduces the recovered quality.

An ordinary Minecraft brush is the basic brush. The custom drill, hammer and lens recipes are listed on the following pages.

Craft archaeology drill: grid rows .IR / .SI / .S.. A dot means an empty cell. I = iron ingot; R = redstone; S = stick.

Craft archaeology hammer: grid rows II. / .S. / .S.. A dot means an empty cell. I = iron ingot; S = stick.

Craft magnifying glass: grid rows GI / .S. A dot means an empty cell. G = glass pane; I = iron ingot; S = stick.

<a id="tool-tiers"></a>

## Field-tool upgrades

Field tools have basic, precision and resonant versions. Upgrades improve excavation control, speed, tool durability and inspection behavior.

Precision upgrades use recovered components or fragments and diamonds. Resonant upgrades use resonant cores and netherite scrap. Use the smithing upgrades appropriate to the tool.

Upgrading preserves the tool's custom name, damage and enchantments. The drill, brush, hammer and lens retain their distinct jobs at every tier.

<a id="scaling-and-quality"></a>

## Quality and scaling grades

A recovered component remembers its excavation score. That score multiplies each numeric modifier on that component: a +10% mining-speed roll at 50% quality contributes +5%.

Original excavation provenance stays recorded. Component restoration can improve current quality up to 120%. At zero quality, a component grants no modifier effect.

F, D, C, B, A and S are scaling grades. They are independent of base value tier and influence. A high base roll and strong grade work well together.

Numeric grade growth per ascension is F +6%, D +10%, C +15%, B +22%, A +32%, S +45%, relative to that modifier's quality-adjusted base. Growth adds; it does not compound.

An ordinary level selects one to three numeric contributions for a small grade-based increase. Per selected level: F 0.05%, D 0.10%, C 0.15%, B 0.25%, A 0.35%, S 0.50% of base.

Milestone perks gain stages instead of meaningless numeric bonuses. Stages unlock at ascensions 4/7/10 for F/D, 3/6/9 for C/B and 2/5/8 for A/S.

<a id="reconstruction"></a>

## Reconstructing equipment

Use the Reconstruction Table to select an equipment pattern, place its matching recovered pieces and assemble the result. Each selected pattern shows the parts it requires.

A pickaxe uses three top pieces: left tip, head bridge and right tip. Beneath them sit a connector and handle. All five pieces keep their individual scores and modifier records.

Sword, axe, shovel, hoe, mace and spear patterns are also available. Handles and connectors use universal modifiers that work on all reconstructed tool types.

The finished tool keeps each part's quality, saved rolls and customization provenance. Repeated contributions combine up to their relevant caps.

The pickaxe has the developed mining modifier and progression system. Further specialized modifiers for other equipment can be added later.

Craft reconstruction table: grid rows PPP / .C. / S.S. A dot means an empty cell. P = any planks; C = crafting table; S = stick.

<a id="ruined-ancient-forge"></a>

## The Ruined Ancient Forge

The Ruined Ancient Forge is the first, small form of the ancient workshop. Complete its masonry to reconstruct equipment with the same recovered parts and patterns as the Reconstruction Table. It does not grant extra modifier rolls or change your pieces.

The hearth holds five input pieces and an output. Completing the frame automatically forms one weathered stone-and-bronze forge. Right-click the hearth or any formed section to open it. Breaking a section restores the surviving original masonry and keeps the stored pieces safe. Breaking the hearth drops its inventory and the hearth itself.

Open the completed forge and select Alloy in the upper bar. Place one Netherite Scrap or Ancient Debris in Scrap, one gold ingot in Gold, and two iron ingots in Iron. Press Alloy to make four Ancient Ingots. Ingredients and output can stack. Shift-click routes ingredients to their matching input.

Both forge modes share stored slots. Switching to Alloy preserves all your parts; remove them, including the two stored-parts slots, before alloying. Collect the result before assembling equipment. Breaking the frame blocks both modes and preserves everything inside.

The footprint is three blocks wide and three deep, four blocks tall including the foundation. The hearth sits at the center of the front edge, one block above the foundation. All layers below are read front row first. B is Ruined Forge Bricks, H is the hearth, and a dot is empty space.

Foundation: BBB / BBB / BBB. Hearth level: .H. / B.B / BBB. One above the hearth: ... / B.B / .B. Two above the hearth: ... / ... / .B.

Use one hearth and eighteen Ruined Forge Bricks. The two side pillars are behind the hearth; the chimney is centered at the back. The frame rotates with the hearth. The completed structure forms a connected work pad, firebox, supports and chimney.

Right-click an incomplete hearth with an empty hand, then choose Show ghost layout. Translucent cyan blocks mark missing masonry. Amber marks spaces occupied by another block: clear those spaces before placing bricks. The preview never places or consumes blocks.

Click the incomplete hearth again to hide the layout. Only you see your preview, and it disappears when the forge forms or you leave the world. Shift-right-click the hearth with an empty hand to inspect repair status.

Small ruined workshops occasionally generate in plains, forests, taigas and savannas in newly generated terrain. A found workshop contains a removable hearth and most of its frame, with two bricks missing. Repair it there or take the materials home.

Exploration is optional. Craft the same hearth from four Ruined Forge Bricks, two copper blocks, two iron ingots and one blast furnace. Forge bricks use ordinary stone crafting materials. No egg, boss drop, vault, new structure or rare random find is needed for the crafting route.

Packs can replace these recipes, change the masonry tag, or disable/add structure biomes with datapacks. The locator command is /locate structure examplemod:ruined_ancient_forge.

This is a weathered starter forge. Advanced functions, Forgemaster rewards and future forge designs remain separate work. The planned expanding, Blood Magic-inspired structure will be the Ancient Anvil. Its exact layout and upgrades are not part of this first forge.

Craft ruined forge bricks: grid rows DD / DD. A dot means an empty cell. D = any stone crafting materials.

Craft ruined ancient forge: grid rows BCB / IFI / BCB. A dot means an empty cell. B = ruined forge bricks; C = copper block; I = iron ingot; F = blast furnace.

<a id="ancient-alloy"></a>

## Ancient metal and alloying

The repaired Ruined Ancient Forge can alloy ancient material with ordinary metals. Select Alloy in the forge, then use one Netherite Scrap OR Ancient Debris, one gold ingot, and two iron ingots to make four Ancient Ingots. No fuel or additional structure is needed for this first alloy.

Netherite Scrap is the vanilla scrap obtained by smelting Ancient Debris. Both forms are accepted directly. Artifacts, recovered component pieces, and Ancient Fragments are not alloy ingredients.

Craft nine Ancient Ingots into a Block of Ancient Metal. Craft a block back into nine ingots. Three blocks and four ingots make one Ancient Anvil, following the ordinary anvil crafting shape.

The alloy screen has separate Scrap, Gold, Iron and output slots, plus two preserved-part slots. It refuses to consume ingredients when the recipe is incomplete, the output will not fit, or old parts remain stored.

Craft ancient metal block: grid rows III / III / III. A dot means an empty cell. I = ancient ingot.

Craft ancient ingots from block: ancient metal block, in any arrangement.

<a id="ancient-anvil"></a>

## The Ancient Anvil

The Ancient Anvil is a rune-carved metal anvil with warm inlays. Craft it like an ordinary anvil: three Blocks of Ancient Metal across the top, one Ancient Ingot in the middle, and three ingots across the bottom. This uses 31 ingots in total.

Place the anvil on its own to establish Tier 0. It is a stationary workshop structure and does not fall like a vanilla anvil. Its foundation is a separate project from repairing the Ruined Ancient Forge.

Build a complete 3 by 3 square of metal blocks directly one layer below the anvil, centered on it. All nine blocks are required, including the center. Iron, gold, copper, netherite and ancient metal qualify; you may mix them. Diamond, emerald and lapis blocks are not metal.

The anvil automatically becomes Tier 1 when the foundation is complete. Removing or replacing a required metal downgrades it to Tier 0. The metals remain normal blocks and are never consumed. Breaking or relocating the anvil leaves its foundation in place.

Right-click the anvil with an empty hand to see its tier and metal count. Show foundation guide displays the missing layer: cyan for air and amber for a wrong block that needs replacing. Only you see the guide. It clears when Tier 1 is complete or you leave the world.

Tier 0 and Tier 1 establish the first stages of the planned growing ancient workshop. Later tiers, rituals and artifact-modification powers will be added separately.

Craft ancient anvil: grid rows BBB / .I. / III. A dot means an empty cell. B = ancient metal block; I = ancient ingot.

<a id="pickaxe-core"></a>

## The reconstructed pickaxe

A reconstructed pickaxe combines the three mining head pieces with a universal connector and handle. A carefully assembled build can substantially outperform an enchanted vanilla pickaxe.

The innate mining multiplier is 1 plus twice the average quality of its three head pieces. Heads at 50% give 2x native speed; 100% gives 3x; 120% gives 3.4x. This multiplies native Efficiency and Haste too.

The pickaxe has a 200-point resonance store. Primary mining and holding the tool generate quality-scaled core charge. Dedicated generation modifiers support more simultaneous spenders.

Crouch to enable area-mining perks. Native extra breaks consume durability, respect cancellation and share a limit of 64 extra blocks within a loaded 16-block range.

Extra breaks cannot recurse, farm tool XP or generate primary bonuses or resonance. Native Fortune, Silk, furnace handling and pure material conversions still compose with secondary loot.

Recoil uses right-click air for a backward blink. Chorus uses sneak-right-click air for a forward blink. Both require a safe destination.

<a id="levels"></a>

## Tool levels and mastery

Valid Survival or Adventure pickaxe mining grants tool XP. Creative mining, canceled breaks, incorrect-tool targets and secondary area breaks do not farm levels.

The pickaxe levels from 1 to 100. Every tenth level is an ascension gate: collect its materials and ascend before gaining more tool XP. XP at an unpaid gate is not banked.

Ordinary levels award small grade-scaled bonuses to one to three numeric contributions. Level-up sound marks a successful gain. Milestone perks advance with ascensions.

At full mastery, the Rally Mining modifier converts its extra tool-XP opportunity into player XP and resonance, so that investment remains useful.

<a id="ascension"></a>

## The Ascender

The prototype Ascender has a tool slot and storage for the materials required by that tool's modifiers. Place the pickaxe at a ten-level gate and supply every listed resource.

Each rolled modifier occurrence has a resource binding. Cost increases with the upcoming ascension number. Repeated modifiers and materials combine into the total requirement.

Once every cost is present, the tool ascends and the exact materials are consumed. Ascension raises numeric contributions according to their grades and may unlock perk stages.

This prototype has no crafting recipe. Its final late-game construction is reserved for future progression. It is available in creative mode.

<a id="modifier-basics"></a>

## Reading modifiers and value tiers

A modifier family is selected by its relative weight. Then rarity weights select one of that modifier's value tiers, followed by a value within that band. Different families may have different numbers of tiers.

T1 is the strongest value tier. Common artifacts favor low bands, with occasional stronger results where allowed. High rarities favor stronger bands and have more chances to add extra modifiers.

Value tier, F-S scaling grade, excavation quality and influence are separate properties. Your tooltips show your actual values; each modifier's book entry explains its operation, limits and tier bands.

Handles and connectors roll the universal pool. Mining head pieces roll mining effects. Some harvest effects exclude each other on one piece, and Silk Touch takes priority over Fortune and furnace loot.

Fortune contributions add above a native Fortune enchantment up to X. Applicable mining-XP bonuses add up to +2000%. Conditional mining-speed bonuses add up to +800%, then multiply the core and general speed bonus.

The Modifiers category contains all 120 definitions. Use its topic filter for speed, loot, area mining, resonance, survival, universal parts or either influence.

<a id="resonance"></a>

## Resonance and combinations

Resonance is energy stored on an assembled pickaxe, with a capacity of 200. The innate core supplies quality-scaled charge on primary breaks and periodically while held.

Battery modifiers add charge from activities such as gem mining, movement, furnace processing or trading some current mining XP. They can support multiple spenders.

Spenders provide effects such as area excavation, Haste, repair, bonus loot, teleportation and protection. Each effect has its own cost, conditions and cooldown.

Consumers share the same store and run in a defined mining-event order. If one spends the remaining energy, another may have to wait. Entanglement can refund part of spending and share that refund with nearby allies.

Secondary breaks and remote collection cannot recursively farm resonance. Read individual modifier entries for stage values and exact conditions.

<a id="influences"></a>

## The Influencer

Components start without an influence. The Influencer assigns Nether or End influence once and rerolls eligible modifiers with a stronger matching theme.

Matching themed modifier families have sixfold relative weight. Other-influence families are excluded. Neutral families remain available, and influence-exclusive families require their matching influence.

An influence is permanent. You cannot remove it, switch it or keep applying the same influence to fish for better rolls. Annulment does not reset that rule.

Nether names have an ember-colored pulse; End names have a violet shimmer. Inventory icons keep their original appearance. A piece's individual influence is retained when assembled.

Influence changes roll eligibility and weighting, not the meaning of its original excavation score. This prototype has no crafting recipe and is available in creative mode.

<a id="unveiler"></a>

## The Unveiler and ancient runes

Epic and higher components may carry a hidden ancient modifier written as unreadable runes. It replaces a modifier slot rather than adding unlimited extra rolls.

The Unveiler shows three fixed legal choices. Each choice includes a modifier, scaling grade and base value. Select one to reveal it on the component.

The choices are saved, not freshly rerolled every time the menu opens. Revealing consumes the hidden slot. Role pools, rarity, exclusivity, influence and existing modifiers still constrain the choices.

The Unveiler acts on recovered pieces, not assembled tools. Corrupted components cannot be modified. The prototype has no crafting recipe.

<a id="anuller"></a>

## The Anuller

The Anuller randomly removes one unlocked modifier from a recovered component. It cannot target a permanently locked modifier.

It does not remove influence or make influence assignment repeatable. A corrupted component is terminal and cannot be annulled.

This prototype works on pieces, not assembled tools, and has no crafting recipe.

<a id="locker"></a>

## The Locker

The Locker permanently protects one revealed modifier on a component. It cannot be removed by the Anuller or targeted by modification operations that respect the lock.

You may lock only one modifier. You cannot move that lock to a different modifier later. Hidden ancient runes must be unveiled before locking a revealed choice.

Influence assignment preserves a compatible lock and rejects a conflicting assignment. Corruption never selects the locked modifier as a target.

The prototype has no crafting recipe and works on recovered pieces.

<a id="scaling-randomizer"></a>

## The Scaling Randomizer

The Scaling Randomizer rerolls scaling grades on the component's unlocked modifiers. It does not reroll the base values, value-tier provenance or excavation score.

A locked modifier retains its grade. Corrupted pieces cannot be randomized. Influence remains permanent and unchanged.

The prototype has no crafting recipe and works on recovered pieces.

<a id="restoration"></a>

## The Restorer

The Restorer improves a recovered component's current quality in ten-point steps, up to 120%. Quality above 100% strengthens contributions that have room below their caps.

The original excavation score remains recorded. Restoration does not reroll its modifier families, values, tiers, grades or influence.

Corruption prevents further restoration. Restore a piece before taking that final gamble. This prototype has no crafting recipe.

<a id="corruption"></a>

## The Corruptor

Corruption is a final gamble on a recovered component. Once corrupted, no more component modifications are allowed.

Possible outcomes include strengthening or weakening an unlocked modifier, raising or lowering a scaling grade, adding or removing an eligible unlocked modifier, restoring or damaging quality, or leaving the piece otherwise unchanged.

Only eligible outcomes are rolled. Locked modifiers cannot be selected. Quality limits, modifier limits, role pools and permanent influence rules still apply.

Plan your restoration, revelation, influence and lock before corrupting. The prototype has no crafting recipe. Further sculk-related progression is planned.
