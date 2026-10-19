package com.example.examplemod;

public enum ComponentStationKind {
    UNVEILER("unveiler","Unveiler","Choose one ancient revelation","chiseled_stone_bricks",0xB69BE8),
    ANULLER("anuller","Anuller","Remove one unlocked modifier","polished_blackstone",0xD68A86),
    CORRUPTOR("corruptor","Corruptor","A permanent gamble on this part","sculk",0x62B9BE),
    LOCKER("modifier_locker","Locker","Lock one revealed modifier forever","iron_block",0xB9C3D1),
    SCALER("scaling_randomizer","Scaling Randomizer","Reroll every unlocked scaling grade","amethyst_block",0xC8A1F0),
    RESTORER("component_restorer","Restorer","Restore quality, up to 120%","quartz_block_side",0xE1CF94),
    INFLUENCER("influencer","Influencer","Choose a permanent influence","chiseled_deepslate",0xA8A3EB);
    public final String id,title,description,texture;public final int color;
    ComponentStationKind(String id,String title,String description,String texture,int color){this.id=id;this.title=title;this.description=description;this.texture=texture;this.color=color;}
    public static ComponentStationKind byId(int id){return values()[Math.clamp(id,0,values().length-1)];}
}
