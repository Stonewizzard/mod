# Your first Minecraft block

This project uses the versions in your uploaded template: Minecraft 26.3,
NeoForge 26.3.0.77, and Java 25. Use these together; this project is not for 1.21.

## Run it

1. Install a **JDK 25** (Java Development Kit), such as Eclipse Temurin 25,
   and IntelliJ IDEA with Java support.
2. Extract this entire ZIP into a folder. Open that folder in IntelliJ
   (the folder containing `build.gradle`). Import it as a Gradle project.
3. Set the project's SDK and Gradle JVM to your JDK 25. Let Gradle finish
   downloading Minecraft and NeoForge; the first import can take several minutes.
4. Open IntelliJ's Terminal in the project folder and run:
   - Windows PowerShell: `.\gradlew.bat runClient`
   - macOS/Linux: `chmod +x gradlew`, then `./gradlew runClient`
5. In the development Minecraft window, create a Creative world. Open the
   inventory and search for **Example Block**. It also appears under
   **Building Blocks** and **Example Mod Tab**. Place it like any normal block.

It looks like a diamond block because we reuse that existing texture, but it is
its own block: `examplemod:example_block`. It has no diamond-block functionality.
With commands enabled, you can also use:

```text
/give @s examplemod:example_block
```

To make an installable mod, run `.\gradlew.bat build` on Windows or
`./gradlew build` on macOS/Linux. The resulting mod JAR is in `build/libs/`.
Install matching NeoForge for Minecraft 26.3 and put the mod JAR in that
installation's `mods` folder. The source ZIP itself does not go in `mods`.

## How the pieces fit together

**Java describes behavior. JSON files describe appearance, names, and drops.**
You do not need to create a new Java class for every ordinary block.

In `src/main/java/com/example/examplemod/ExampleMod.java`:

```java
public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock(
    "example_block", p -> p.mapColor(MapColor.DIAMOND).strength(1.5f, 6.0f));
```

- `EXAMPLE_BLOCK` is the Java variable's name. `example_block` is the saved game
  identifier. `examplemod` is the mod's namespace, giving `examplemod:example_block`.
- `DeferredBlock<Block>` holds a reference to a block that NeoForge registers
  at the appropriate time during startup.
- `p -> ...` is a small function (a lambda). Here it takes the starting
  block properties and sets their map color, hardness, and explosion resistance.
- `strength(1.5f, 6.0f)` means hardness 1.5 and explosion resistance 6.
  The `f` means these numbers are Java `float` values.
- Map color affects maps; it does not choose the block texture.

The next declaration registers a **BlockItem**. The placed block and the item
held in your inventory are separate things. The BlockItem connects them so
right-clicking places our block.

The constructor connects `BLOCKS` and `ITEMS` to NeoForge's event bus. Think of
that bus as notifications during startup. The `addCreative` method responds to
the creative-tab contents event and adds the block item to Building Blocks.
The template's custom tab now includes our block too.

In `src/main/resources/`:

| File | Purpose |
| --- | --- |
| `assets/examplemod/blockstates/example_block.json` | Chooses the placed block's model. Our block has no special states. |
| `assets/examplemod/models/block/example_block.json` | Makes a cube and applies Minecraft's diamond-block texture to every face. |
| `assets/examplemod/models/item/example_block.json` | Uses the block model for the inventory item. |
| `assets/examplemod/items/example_block.json` | Connects the modern item renderer to that item model. |
| `assets/examplemod/lang/en_us.json` | Names it "Example Block" in English. |
| `data/examplemod/loot_table/blocks/example_block.json` | Makes it drop itself when broken in Survival, subject to explosion survival. Creative breaking normally does not drop items. |

The template already registered the block and its Building Blocks entry. This
exercise completes its missing visual resources and drop table, sets its basic
properties, and adds it to the template's own tab. The original example food,
configuration, and logging remain template scaffolding; they are not needed to
understand this block. There is no crafting recipe or world generation yet.

## An easy first experiment

Change `"minecraft:block/diamond_block"` to `"minecraft:block/gold_block"` in
`models/block/example_block.json`. Restart the development game: the same block
now looks gold. Changing appearance does not require changing its identifier.
Next, try changing the Java hardness from `1.5f` to `3.0f` and compare breaking
it in Survival (Creative breaks blocks immediately).

## Verification in this environment

Resource JSON and their local model references were checked. Gradle downloaded
and started, but stopped before Java compilation: this environment has Java 21
and its network policy blocked the automatic Java 25 download. No compiled JAR
is included. Install JDK 25 locally to build this project. A graphical Minecraft session
was not run here; use the Creative-world steps above to verify in game.
