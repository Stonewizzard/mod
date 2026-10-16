package com.example.examplemod.client;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;

/** Convert Java's packed ARGB into the packed ABGR used by native RGBA textures. */
final class SculkTexturePixels {
    private SculkTexturePixels() {}

    static NativeImage copy(BufferedImage image) {
        var pixels=new NativeImage(image.getWidth(),image.getHeight(),false);
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++) {
            int argb=image.getRGB(x,y);
            int abgr=(argb&0xff00ff00)|((argb>>>16)&0xff)|((argb&0xff)<<16);
            pixels.setPixel(x,y,abgr);
        }
        return pixels;
    }
}
