package com.example.examplemod.client;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/** Verify uploaded native pixels against Minecraft's PNG decoder, not a Java-only preview. */
final class SculkTexturePixelsRegression {
    static void run(ResourceManager resources) throws Exception {
        var samples=new BufferedImage(3,2,BufferedImage.TYPE_INT_ARGB);
        int[] colors={0xff12a8dc,0xffff7b1c,0x407e22c1,0x00ab3479,0xffffffff,0xff091d31};
        samples.setRGB(0,0,3,2,colors,0,3);
        verify(samples,"cyan, magma, translucent, transparent, white and dark samples",true);
        for(String file:new String[]{"sculk_growth_material.png","ancient_infection/materials.png",
                "ancient_infection/sniffer_growth.png","ancient_infection/cruncher_growth.png"}) {
            var id=Identifier.fromNamespaceAndPath("examplemod","textures/entity/"+file);
            try(var input=resources.getResourceOrThrow(id).open()) {
                verify(ImageIO.read(input),file,false);
            }
        }
        System.out.println("SCULK_PIXEL_CHECK: PASS: native upload matches Minecraft PNG decoding for colors, alpha and all four growth atlases; deliberately swapped pixels rejected");
    }

    private static void verify(BufferedImage source,String label,boolean negativeControl) throws Exception {
        var png=new ByteArrayOutputStream();
        if(!ImageIO.write(source,"png",png))throw new AssertionError("No PNG writer");
        try(var decoded=NativeImage.read(new ByteArrayInputStream(png.toByteArray()));
                var uploaded=SculkTexturePixels.copy(source)) {
            if(uploaded.getWidth()!=decoded.getWidth() || uploaded.getHeight()!=decoded.getHeight())
                throw new AssertionError("Native image dimensions changed: "+label);
            for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++)
                if(uploaded.getPixel(x,y)!=decoded.getPixel(x,y))
                    throw new AssertionError("Native color/alpha mismatch in "+label+" at "+x+","+y);
            if(negativeControl)try(var swapped=new NativeImage(source.getWidth(),source.getHeight(),false)) {
                // A deliberately wrong red/blue conversion must fail the decoder comparison.
                int argb=source.getRGB(0,0);
                swapped.setPixel(0,0,(argb&0xff00ff00)|((argb>>>16)&0xff)|((argb&0xff)<<16));
                if(swapped.getPixel(0,0)==decoded.getPixel(0,0))
                    throw new AssertionError("Negative control failed to reproduce the red/blue swap");
            }
        }
    }
}
