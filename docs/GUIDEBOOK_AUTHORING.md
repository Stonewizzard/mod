# Extending the guidebook

## One library, one Workshop Manual

`tools/field-guide.json` is the source for authored chapters. `FieldGuide` adds modifier entries from the same catalog, descriptions, stage tuning and resource bindings used by gameplay. `FieldGuideNavigation` owns search, history, selected section and pagination. `GuideBookView` presents this state in the Workshop Manual; `FieldGuideScreen` creates one native Minecraft button per visible control. It fits the layout horizontally at narrow GUI widths, with normal-size text and matching measured wrapping and native input bounds. Reading never reads or mutates a player's rolled equipment.

To add a chapter, append an object:

```json
{
  "id": "new_feature",
  "category": "EXCAVATION",
  "title": "A clear, searchable title",
  "icon": "minecraft:brush",
  "sections": [
    {"title": "Overview", "paragraphs": ["Say what this does and why a player would use it."]},
    {"title": "Use it", "paragraphs": ["Explain the first action, requirements and result."]},
    {"title": "Limits", "paragraphs": ["State irreversible choices and important restrictions here."]}
  ],
  "links": ["archaeology_table"],
  "keywords": "ordinary words synonyms players might search"
}
```

Use stable unique IDs. Keep section labels short and purposeful; write one decision or related group of facts per section. Chapters with more than seven sections automatically page their section choices with More. Body paragraphs can be long: font-measured pagination continues them safely. Related links resolve entry IDs, including runtime modifier IDs such as `examplemod:magnetized`. Up to four related links form a short reading path. Optional `recipes` lists real recipe IDs; the generator derives ingredients and rows into a Recipe section. Do not duplicate recipes by hand.

Run `python tools/generate_pickaxe_catalog.py`, then `python tools/generate_pickaxe_catalog.py --check`. Generated Java and reference docs are committed. The generator rejects empty or duplicate sections, duplicate IDs, and broken links. Category lists, category counts, search results and pagination update automatically; layouts never enumerate chapter IDs. Only the Manual home shortcuts curate a small introductory route; adding an article does not require changing that route.

For a new modifier, author its normal gameplay catalog/description/tuning; its Effect, Value tiers, Growth and Resources sections appear in the book automatically. Do not write separate prose or balance tables for each book. Item stacks are created only when the client renders in a world: Minecraft 26.3 item components are not available at the title screen or common static initialization.

`GuideBookRegression` measures every section at full and narrow widths with the actual Minecraft font and checks bounds/overlap. Real-client QA additionally creates a world, opens the actual book item, supplies OS input, resizes the window and captures the native renderer. Run it as described in [REAL_CLIENT_QA.md](REAL_CLIENT_QA.md). Inspect those screenshots after any presentation change; structural tests cannot judge a design.

## Item graphics

The chapter `icon` is the actual item registry ID, not an image path. It appears beside every category entry and search result, on the article heading, and on related links and task shortcuts. Category and modifier-topic illustrations are centralized in `FieldGuideView.categoryIcon` and `GuideBookView.topicIcon`. New chapters automatically inherit the illustrated layout. The live screen and native buttons render item stacks; common data and static initialization contain only registry strings. Results use the shared seven-entry page size so 16px native icons cannot overlap neighboring rows.

Only `examplemod:ancient_field_guide` is registered, named Workshop Manual. The four comparison editions and their resources have been removed. The original item ID and recipe are retained for existing Workshop Manuals.
