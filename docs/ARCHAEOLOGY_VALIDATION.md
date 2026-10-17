# Archaeology expansion validation

The build and tests run against Minecraft 26.3 / NeoForge 26.3.0.77 on Java 25 through the repository's Build workflow.

## Automated checks

- Full Gradle build and all eight required Minecraft server game tests, including existing Sniffer/Cruncher drops, eggs, geodes and archaeology-table behavior.
- 160 seeded specimens across every rarity: deterministic contents, all rock/find families, retained progress, intact completion with starter tools, recorded component quality and weighted source rewards.
- Native inventory clicks for all 12 tool-switch directions at all three tiers, including custom names and client prediction; occupied return-slot and unrelated-item checks.
- Actual cursor-tool wear in Survival, native Unbreaking, broken-tool consumption, same-tick throttling, invalid packets, range checks and shared-table work.
- Recovery consumes one input, refuses a full output, cannot be claimed twice by two viewers, and shift-clicks into the player inventory. Salvage fragments stack between specimens.
- Version-one study migration retains cover, condition and unrelated custom data. Table saves, chunk updates and breaking either half retain all six stored slots, including multi-item recovered output.
- Eight loaded smithing recipes preserve custom names, damage and enchantments; all three restored-equipment crafting recipes match.
- Actual screen drawing plan exercised in 31 study, material, component and inventory states. Native SDL mouse events exercise tabs, tools, dragging, cooldowns and recovery. Artwork alpha masks are checked cell-for-cell against excavation hit masks.
- Existing sculk rendering checks remain active: 301 native model layers, 16 cow/chicken skins, host fit, color upload, adult/baby models, emissive and invisibility paths.

## Pacing check

A deterministic, conservative hammer/drill/brush route completes every matrix at 100% condition at every tier. Its accumulated tool cooldown is roughly 1.5–2 minutes with field tools, about 1–1.4 minutes with precision tools, and around 35–45 seconds with resonant tools. These are simulated stroke times, not player completion measurements or optimal routes; inspection and manual movement add time. There is no countdown.

The targets are provisional. In particular, playtest tool service life, the diamond/core upgrade costs, and whether hard-rock specimens should take longer than the current extra hammer passes.

## Visual review and practical limits

The committed overview is rendered from the same Java drawing commands, packaged textures and Minecraft bitmap font as the actual screen. It verifies layout and appearance without a graphics window. It is not a live gameplay screenshot, and the feature still needs hands-on client playtesting for feel, sound and final economic balance.

The playtest guide lists representative scenarios. The PR is intentionally left unmerged for review.
