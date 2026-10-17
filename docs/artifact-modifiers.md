# Artifact modifier foundation

No ability effects ship with this change. Every live pool is empty. Equipment keeps fixed base stats; excavation quality is saved for future effectiveness calculations.

Edit `ArtifactModifierCatalog.java` to balance the pools. Every `ArtifactProfile.Find` has its own entry, including each pickaxe tip, its bridge, the connector and handle, and each other tool head. The shared connector/handle pools apply to those parts across all reconstruction patterns.

An illustrative entry (not enabled):

```java
pools.put(ArtifactProfile.Find.PICKAXE_LEFT,
    new ArtifactModifiers.Pool(2, .75, List.of(
        new ArtifactModifiers.Definition(
            "examplemod:future_mining_trait", // stable saved identifier
            10,                             // relative selection weight
            1,                              // minimum artifact rarity, 0–5
            60,                             // minimum excavation score, 0–100
            .10, .30,                       // raw rolled value range
            "mining"                        // mutually exclusive group, "" = none
        )
    )));
```

`Pool.rolls` is the number of attempts (0–8); `chance` is the success probability for each attempt. Eligible entries are selected by weight without replacement. Entries in the same nonempty group cannot both appear. Score and rarity gates apply before selection. Constructor validation catches invalid ranges, weights and duplicate IDs.

On the server, the final excavation stroke seals a versioned `examplemod:part_modifiers` record on the specimen. It contains the pool revision, seed, part, excavation score and rolls. Each roll stores its stable ID, raw value and separate quality scale (`score / 100`). Nothing applies that scale to stats yet. Extraction supports a fallback for artifacts completed before this system existed.

The seed makes rolls deterministic, and an existing record is never overwritten, including an empty record. Reopening, moving the specimen, changing pools or extracting it cannot reroll a completed find. Newly excavated finds use the edited catalog. Old recovered parts are not retroactively rerolled.

Recovery copies the sealed record to the part. Reconstruction stores it alongside that part's original excavation provenance inside `examplemod:reconstruction.Parts`. Removed or unknown modifier IDs remain in saved data. Future ability code can ignore unavailable definitions without destroying their records. Increment `REVISION` when changing the catalog for future diagnostic use; never reuse an existing modifier ID for an unrelated ability.

The GameTests use isolated test pools to check deterministic rolls, score/rarity gates, exclusivity and completion/recovery/assembly/item-serialization preservation. Test definitions never enter the live catalog.

# Reconstruction catalog

`ReconstructionPattern.java` defines stable pattern IDs, result item IDs and input slot maps. Pickaxe retains its five-piece `/−\\`, connector and handle layout. Sword, axe, shovel, hoe, mace and spear use their own recovered head/blade/tip plus a connector and handle. Pattern switching retains all contents; leftover pieces remain visible and must be removed before assembly. All outputs retain individual part scores and the precise average.

New find enum values must be appended to preserve existing saved specimens. New excavation artwork needs a matching `ArtifactFootprints` alpha mask and coverage in the client visual regression.
