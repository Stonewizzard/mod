package com.example.examplemod;

import java.util.Map;

/** Uncapped curves. All values are starting balance, isolated for later tuning. */
public final class ForgeRuneMath {
  public static int count(Map<ForgeRune, Integer> runes, ForgeRune kind) {
    return runes.getOrDefault(kind, 0);
  }

  public static double efficiency(int n) {
    return n <= 50 ? 1 - .01 * n : 50.0 / (n + 50.0);
  }

  public static int swings(int base, int swift, int deliberation) {
    return Math.max(1, base - swift) + deliberation;
  }

  public static double hitPrice(
      double base, int original, int swings, int swift, int efficiency, int deliberation) {
    // Deliberation divides the entire job price across its added swings, then discounts it.
    return base
        * (1 + .05 * swift)
        * efficiency(efficiency)
        * (double) Math.max(1, original - swift)
        / swings
        / (1 + .02 * deliberation);
  }

  public static double wear(int tempering) {
    return Math.max(0, 1 - .01 * tempering);
  }

  public static double mutationChance(int n) {
    return n / (100.0 + n);
  }

  public static double sequenceDiscount(int runes, int streak) {
    return 1 / (1 + .002 * runes * Math.min(streak, 100));
  }

  private ForgeRuneMath() {}
}
