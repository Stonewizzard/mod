package com.example.examplemod;

import java.util.List;

/** One value band. Its six weights refer to artifact rarities, not to the number of tiers. */
public record ModifierTier(String id, int rank, double minimum, double maximum, List<Integer> rarityWeights) {
    public ModifierTier {
        rarityWeights = List.copyOf(rarityWeights);
        if (id == null || !id.matches("[a-z0-9_.:/-]+") || rank < 1
                || !Double.isFinite(minimum) || !Double.isFinite(maximum) || !Double.isFinite(maximum-minimum) || minimum > maximum
                || rarityWeights.size() != 6 || rarityWeights.stream().anyMatch(w -> w < 0)) {
            throw new IllegalArgumentException("Invalid modifier value tier: " + id);
        }
    }
    public int weight(int rarity) {
        return rarityWeights.get(Math.clamp(rarity, 0, 5));
    }
}
