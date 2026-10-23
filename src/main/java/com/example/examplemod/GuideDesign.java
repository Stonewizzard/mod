package com.example.examplemod;
/** The selected Workshop Manual presentation. The item ID remains stable for existing saves. */
public enum GuideDesign {
    MANUAL("ancient_field_guide","Workshop Manual",0xff304e70);
    public final String itemId,title;public final int accent;
    GuideDesign(String itemId,String title,int accent){this.itemId=itemId;this.title=title;this.accent=accent;}
}
