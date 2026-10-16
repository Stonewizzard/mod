package com.example.examplemod.client;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;

/** Use the current NativeImage ARGB API without pre-swapping the color channels. */
final class SculkTexturePixels {
    private SculkTexturePixels() {}

    static NativeImage copy(BufferedImage image) {
        var pixels=new NativeImage(image.getWidth(),image.getHeight(),false);
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++) {
            pixels.setPixel(x,y,image.getRGB(x,y));
        }
        return pixels;
    }
}
