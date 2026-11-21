package com.example.examplemod;

/** Stable saved IDs; tiers gate effects, never how many runes may contribute. */
public enum ForgeRune {
  STORAGE(1),
  EXTRACTION(1),
  INPUT(1),
  SWIFT_FORGING(2),
  TEMPERING(2),
  CONDUIT(2),
  RESTORATION(3),
  EFFICIENCY(3),
  EXPANSION(3),
  TRANSMUTATION(3),
  NETHER_TRANSMUTATION(3),
  END_TRANSMUTATION(3),
  BATCHING(4),
  INVOCATION(4),
  COLLECTION(4),
  DELIBERATION(4),
  SPECIALIZATION(5),
  FAMILIARITY(5),
  ALTERNATION(5),
  MASTER_CRAFTSMANSHIP(6),
  GRAND_PATTERN(6),
  MUTATION(6);
  public final int tier;

  ForgeRune(int tier) {
    this.tier = tier;
  }

  public String id() {
    return "rune_of_" + name().toLowerCase(java.util.Locale.ROOT);
  }

  public String title() {
    return "Rune of "
        + java.util.Arrays.stream(name().toLowerCase(java.util.Locale.ROOT).split("_"))
            .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
            .collect(java.util.stream.Collectors.joining(" "));
  }
}
