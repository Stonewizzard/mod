package com.example.examplemod;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** The only live balance configuration. Definitions are data; this catalog applies no effects. */
public final class ArtifactModifierCatalog {
    public static final int REVISION=1;
    private static final Map<ArtifactProfile.Find,ArtifactModifiers.Pool> POOLS=create();
    private static Map<ArtifactProfile.Find,ArtifactModifiers.Pool> create(){
        var pools=new EnumMap<ArtifactProfile.Find,ArtifactModifiers.Pool>(ArtifactProfile.Find.class);
        for(var find:ArtifactProfile.Find.values())pools.put(find,new ArtifactModifiers.Pool(0,0,List.of()));
        // All pools deliberately ship empty. Future balance edits belong here, per part.
        // Example syntax (not active):
        // pools.put(ArtifactProfile.Find.PICKAXE_LEFT,new ArtifactModifiers.Pool(2,.75,List.of(
        //     new ArtifactModifiers.Definition("examplemod:example",10,1,60,.1,.3,"mining"))));
        return Map.copyOf(pools);
    }
    public static ArtifactModifiers.Pool pool(ArtifactProfile.Find part){return POOLS.get(part);}
    private ArtifactModifierCatalog(){}
}
