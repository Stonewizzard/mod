package com.example.examplemod;
/** Presentation only: every edition reads the same library and changes no gameplay. */
public enum GuideDesign {
    JOURNAL("ancient_field_guide","Field Journal",0xff355f43),
    MANUAL("workshop_manual","Workshop Manual",0xff304e70),
    TRAIL("trail_guide","Trail Guide",0xff8b4435),
    REFERENCE("pocket_reference","Pocket Reference",0xff594778),
    ATLAS("relic_atlas","Relic Atlas",0xff74613a);
    public final String itemId,title;public final int accent;
    GuideDesign(String itemId,String title,int accent){this.itemId=itemId;this.title=title;this.accent=accent;}
}
