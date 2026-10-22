# Appraising recovered equipment

Craft an **Appraisal Table** using three planks, two sticks, one paper and one glass:

```text
  P G     P = paper, G = glass
W W W     W = any planks
S   S     S = sticks
```

Place a recovered component, an artifact or a reconstructed tool in its single slot. The left arrows browse entries; select a modifier, then use the right arrows to browse its detailed pages. Shift-click works normally. The item stays in the table when closed and drops when the table is broken.

- **Overview:** quality, original excavation score, rarity, tool progression, durability and innate mining/resonance information.
- **Modifier details:** activation conditions, current contribution, stage parameters, cooldowns, resonance costs, value tiers, combined caps, scaling grade and ascension resources.
- **Tool sources:** each contributing component's individual quality, saved tier, grade, effective contribution and awarded growth counts. Repeated modifiers are combined in the list while retaining their individual source explanations.
- **Unopened artifacts:** rarity and known rock matrix only. Appraisal never rolls a reward or exposes the hidden find.
- **Ancient runes:** remain unread until revealed at the Unveiler. Their saved choices stay hidden during appraisal.

Tooltips now show short quality/progression summaries. Nether-influenced components use animated ember-colored names; End-influenced components use a violet name shimmer. Inventory icons stay unchanged. Ordinary components keep their usual appearance. Influence assignment remains permanent and cannot be rerolled, changed or removed.

## Reward benchmarks

Native Minecraft regression tests compare against real Netherite items, native enchantment registry entries and the native ore loot system:

| Build | Stone mining speed before situational effects |
|---|---:|
| Netherite, Efficiency V | 35 |
| Restored, 100% heads, no rolled speed, Efficiency V | 102 |
| Restored, 100% heads, one +200% effective Mining Speed contribution, no enchantments | 72 |

These values do not bypass hardness, the correct-tool requirement, native penalties or the one-tick minimum. Area mining still consumes durability and uses cancellable native breaks. Quality and modifier power remain separate; existing saved rolls are not rerolled.

The Fortune benchmark adds five effective modifier levels above a native Fortune III enchantment. In the recorded 160-block test, it produced **771 diamonds**, versus **359** from vanilla Fortune III. This is a sampled loot result, not a guaranteed exact yield.

See [the complete modifier catalog](PICKAXE_MODIFIERS.md) and [the individual review of all 120 modifiers](MODIFIER_REWARD_REVIEW.md) for the full balance changes. Artifact acquisition odds are unchanged.
