# Your first Minecraft block: Sniffer Head

This project uses Minecraft 26.3, NeoForge 26.3.0.77, and Java 25.

## Run it

1. Install a **JDK 25** (Java Development Kit), such as Eclipse Temurin 25,
   and IntelliJ IDEA with Java support.
2. Download and extract the repository, then open the folder containing
   `build.gradle` in IntelliJ. Import it as a Gradle project.
3. Set the project's SDK and Gradle JVM to JDK 25. Let Gradle finish downloading
   Minecraft and NeoForge; the first import can take several minutes.
4. Open IntelliJ's Terminal in the folder containing `gradlew.bat` and run:
   - Windows PowerShell: `.\gradlew.bat runClient`
   - macOS/Linux: `chmod +x gradlew`, then `./gradlew runClient`
5. Create a Creative world. Open the inventory and search for **Sniffer Head**.
   It also appears in **Building Blocks** and **Example Mod Tab**.
6. Click a block's side to place the head facing outward from that side, even
   when you approach at an angle. Its back sits flush against that surface.
   On top of a block, it faces toward you in 16 steps of 22.5 degrees, including diagonals.
   It has a head-shaped outline and collision rather than filling a whole cube.

With commands enabled, you can also use:

```text
/give @s examplemod:example_block
```

**Why is the ID still example_block?** The displayed name is now Sniffer Head,
but we keep the original saved ID so blocks in your existing world and items
in your inventory survive this update. Old blocks use the default north-facing
state. The label and model can change independently of the saved ID.

To make an installable mod, run `.\gradlew.bat build` on Windows or
`./gradlew build` on macOS/Linux. The mod JAR appears in `build/libs/`.
Install matching NeoForge for Minecraft 26.3 and put the mod JAR in that
installation's `mods` folder. Gradle handles NeoForge for the development launch.

## Java: what the block does

`src/main/java/com/example/examplemod/ExampleMod.java` registers the block:

```java
public static final DeferredBlock<SnifferSkullBlock> SNIFFER_SKULL = BLOCKS.registerBlock(
        "example_block", SnifferSkullBlock::new,
        p -> p.mapColor(MapColor.COLOR_RED).strength(1.5f, 6.0f).noOcclusion());
```

- `SNIFFER_SKULL` is a Java variable name; `example_block` is the saved game ID.
- `DeferredBlock<SnifferSkullBlock>` holds the registered custom block.
- `SnifferSkullBlock::new` tells NeoForge which constructor creates it.
- `p -> ...` is a small function (a lambda) that changes the starting properties.
- `strength(1.5f, 6.0f)` sets hardness and explosion resistance. `f` means `float`.
- `noOcclusion()` prevents the head from hiding neighboring block faces as
  though it were a full cube. Map color affects maps, not the texture.

The BlockItem declaration below this creates the inventory item that places it.
The constructor connects the registrations and creative-tab event to NeoForge.

`SnifferSkullBlock.java` provides the behavior that an ordinary cube did not need:

- It extends `HorizontalDirectionalBlock`, which already supports rotating and
  mirroring its north/east/south/west direction.
- `FACING` stores the cardinal quadrant. `ROTATION_OFFSET` adds one of four
  22.5-degree steps within that quadrant, giving 16 standing directions.
  Its default of zero preserves the direction of older saved placements.
- `WALL_MOUNTED` records whether the clicked surface was a side. It selects a
  wall model and collision shape whose back touches the block boundary.
- `getStateForPlacement` uses the clicked face for side placements. Clicking an
  east face gives an east-facing head regardless of your viewing angle. For
  top/bottom faces, it rounds the player's yaw to the nearest 22.5-degree step.
- `getShape` returns a union of smaller boxes matching the head's model. Those
  boxes are rotated to match each direction and also supply its collision shape.
  Wall placements shift the model and shape back by half a model pixel to remove
  the small gap between the head and the supporting block.

## JSON: what the block looks like

All paths below are inside `src/main/resources/`.

| File | Purpose |
| --- | --- |
| `assets/examplemod/blockstates/example_block.json` | Chooses the model and rotation for each facing direction. |
| `assets/examplemod/models/block/sniffer_skull.json` | Six model pieces: head, mouth plane, ears, nose, and lower beak. Uses explicit UV coordinates to select the right parts of the sniffer's texture. |
| `assets/examplemod/models/block/sniffer_skull_wall.json` | Wall version of the head, shifted half a pixel toward its supporting block. |
| `assets/examplemod/models/block/example_block.json` | A model alias for the original resource name. |
| `assets/examplemod/models/item/example_block.json` | Uses the sniffer head model for the inventory item. |
| `assets/examplemod/items/example_block.json` | Connects the modern item renderer to the item model. |
| `assets/minecraft/atlases/blocks.json` | Adds the vanilla sniffer entity texture to the block texture atlas. Its sources merge with the existing atlas sources. |
| `assets/examplemod/lang/en_us.json` | Names the block "Sniffer Head" in English. |
| `data/examplemod/loot_table/blocks/example_block.json` | Drops the head itself in Survival, subject to explosion survival. Creative breaking normally does not drop items. |

The head keeps the vanilla sniffer's proportions and texture layout, scaled to
75% to fit within one block space. It has the appearance of the living sniffer's
head, like a mob head trophy. This is a decorative block with no animation,
crafting recipe or wearable helmet behavior yet. Sniffers now drop one head
on death when mob loot is enabled.

## Check the update in game

1. Search for Sniffer Head and confirm the inventory icon is a head.
2. Place it against each of a block's four sides, approaching both straight on
   and diagonally. The snout should always face outward from the clicked side.
   Check from the side that its back touches the wall without a gap.
   Place it on top while turning through a full circle: there should be 16
   rotation steps, including diagonals. Save/reload and verify those angles persist.
3. Summon a vanilla sniffer nearby to compare: `/summon minecraft:sniffer`.
4. Walk around it and check that the selection outline follows its shape.
5. Switch to Survival and break it; it should drop a Sniffer Head item.
6. Save and reload the world, and check that its direction is retained.

The JSON, UV coordinates, model links, and bounds were checked, and a reference
preview was rendered with the vanilla texture. Local Java compilation is blocked
by this cloud environment's Java 21 installation and restricted automatic JDK 25
download. GitHub Actions runs the Java 25 build; graphical Minecraft testing is
still done using the steps above.

Standing rotation models use NeoForge's root `transform` around the block center,
so the head, ears, and snout rotate together. Wall models remain flush and cardinal.
Diagonal collision/selection boxes enclose the rotated model parts; voxel boxes
are axis-aligned. Structure rotation and mirroring also preserve the finer angles.

## Sniffer death drops

Kill a sniffer and it drops **one Sniffer Head**. There is no random chance,
player-kill requirement, or Looting bonus. Normal mob-loot rules still apply.

`SnifferHeadLootModifier.java` adds one item to the existing generated loot.
`ExampleMod.java` registers its codec so NeoForge can read the modifier JSON.
`data/examplemod/loot_modifiers/sniffer_head.json` supplies its settings, with
a single condition matching `minecraft:entities/sniffer`. NeoForge 26.3 loads
the modifier files directly; this version uses singular `condition`, not an
older `conditions` array or a separate global modifier list.

The existing Java/model names containing "skull" are internal names retained
for compatibility; the inventory and placed block display **Sniffer Head**.

To verify it in the development game:

1. Search Creative for Sniffer Head and check an existing head's new name.
2. Summon a sniffer with `/summon minecraft:sniffer`, then kill it. Pick up
   the head and place it on a block to confirm it is our placeable item.
3. In Creative, use `/loot give @s loot minecraft:entities/sniffer` to check
   the loot table directly; this should give one head.
4. Kill a different mob and confirm it does not acquire a Sniffer Head drop.
5. Test with mob loot disabled: sniffer deaths should produce no head.

## Automated drop regression

Run `.\\gradlew.bat runGameTestServer` on Windows or
`./gradlew runGameTestServer` on macOS/Linux. This launches a separate headless
Minecraft test world and checks adult/baby sniffer death drops, other mobs, and
mob loot disabled. GitHub Actions also runs this after compiling the mod.

The modifier has an outer `condition` setting, but the predicate inside it
uses `"type": "neoforge:loot_table_id"` in Minecraft 26.3. Using the older
inner `"condition"` discriminator makes the modifier fail to load at runtime,
which a Java compilation check alone cannot catch.

Adult sniffers use the loot modifier. Minecraft skips normal loot generation
for baby mobs, so a death-drop event adds the head for baby sniffers only. It
checks the mob-loot rule and avoids adding a duplicate head if one is present.
