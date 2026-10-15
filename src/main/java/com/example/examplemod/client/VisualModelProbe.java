package com.example.examplemod.client;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelPart;

public final class VisualModelProbe {
    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var definitions = LayerDefinitions.createRoots();
        for (var entry : definitions.entrySet()) {
            String name = entry.getKey().toString();
            if (!name.contains("cow") && !name.contains("zombie")) continue;
            if (name.contains("armor") || name.contains("villager")) continue;
            var root = entry.getValue().bakeRoot();
            for (String partName : new String[]{"body", "head"}) {
                if (!root.hasChild(partName)) continue;
                var part = root.getChild(partName);
                System.out.println("MODEL_PROBE " + name + " " + partName + " pose=" + part.storePose());
                var cubesField = ModelPart.class.getDeclaredField("cubes");
                cubesField.setAccessible(true);
                for (Object cube : (java.util.List<?>) cubesField.get(part)) {
                    StringBuilder values = new StringBuilder();
                    for (var field : cube.getClass().getDeclaredFields()) {
                        if (field.getType() == float.class) {
                            field.setAccessible(true);
                            values.append(field.getName()).append('=').append(field.getFloat(cube)).append(' ');
                        }
                    }
                    System.out.println("MODEL_PROBE bounds " + values);
                }
            }
        }
    }
}
