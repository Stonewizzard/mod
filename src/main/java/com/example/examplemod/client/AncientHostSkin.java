package com.example.examplemod.client;

import java.awt.image.BufferedImage;
import java.util.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Anatomical, asymmetric takeover fields projected into the host's exact native UVs. */
final class AncientHostSkin {
    record Result(BufferedImage growth,BufferedImage veins) {}

    static Result create(AdaptiveSculkSkin geometry,BufferedImage nativeSkin,BufferedImage material,boolean cruncher) {
        var growth=new BufferedImage(nativeSkin.getWidth(),nativeSkin.getHeight(),BufferedImage.TYPE_INT_ARGB);
        var veins=new BufferedImage(nativeSkin.getWidth(),nativeSkin.getHeight(),BufferedImage.TYPE_INT_ARGB);
        var body=geometry.surfaces.stream().filter(s->s.path().endsWith("/body")).findFirst().orElseThrow();
        var head=geometry.surfaces.stream().filter(s->s.path().endsWith("/head")).findFirst().orElseThrow();
        var inverseBody=new Matrix4f(body.transform()).invert();
        var inverseHead=new Matrix4f(head.transform()).invert();
        for(var surface:geometry.surfaces)for(var polygon:surface.cube().polygons) {
            var vertices=polygon.vertices();if(vertices.length!=4)continue;
            var a=vertices[0];var b=vertices[1];var d=vertices[3];
            float minU=1,maxU=0,minV=1,maxV=0;
            for(var v:vertices){minU=Math.min(minU,v.u());maxU=Math.max(maxU,v.u());minV=Math.min(minV,v.v());maxV=Math.max(maxV,v.v());}
            int minX=Math.max(0,(int)Math.floor(minU*nativeSkin.getWidth())),maxX=Math.min(nativeSkin.getWidth(),(int)Math.ceil(maxU*nativeSkin.getWidth()));
            int minY=Math.max(0,(int)Math.floor(minV*nativeSkin.getHeight())),maxY=Math.min(nativeSkin.getHeight(),(int)Math.ceil(maxV*nativeSkin.getHeight()));
            float du1=b.u()-a.u(),dv1=b.v()-a.v(),du2=d.u()-a.u(),dv2=d.v()-a.v(),det=du1*dv2-du2*dv1;
            if(Math.abs(det)<1e-9)continue;
            boolean face=surface.path().contains("/head");
            boolean leg=surface.path().substring(surface.path().lastIndexOf('/')+1).contains("leg");
            boolean jaw=surface.path().contains("jaw") || surface.path().contains("teeth");
            var relative=new Matrix4f(face?inverseHead:inverseBody).mul(surface.transform());
            for(int y=minY;y<maxY;y++)for(int x=minX;x<maxX;x++) {
                if((nativeSkin.getRGB(x,y)>>>24)<128)continue;
                float u=(x+.5F)/nativeSkin.getWidth()-a.u(),v=(y+.5F)/nativeSkin.getHeight()-a.v();
                float s=(u*dv2-du2*v)/det,t=(du1*v-u*dv1)/det;
                if(s<0||s>1||t<0||t>1)continue;
                var p=new Vector3f(a.x()+(b.x()-a.x())*s+(d.x()-a.x())*t,
                        a.y()+(b.y()-a.y())*s+(d.y()-a.y())*t,a.z()+(b.z()-a.z())*s+(d.z()-a.z())*t).div(16);
                relative.transformPosition(p).mul(16);
                double noise=Math.sin(p.x*.52+Math.sin(p.z*.23)*1.6)+.65*Math.cos(p.y*.61-p.z*.36);
                double coverage;
                if(face) {
                    // One infected temple flows toward the snout; the yellow nose and teeth stay readable.
                    coverage=(-p.x-1.3)/7+noise*.24;
                    if(!cruncher && p.z < -11)coverage-=.9;
                    if(jaw)coverage-=.7;
                    if(surface.path().contains("teeth"))coverage-=1.6;
                } else if(leg) {
                    coverage= - .6 + (p.x<0?.5:0)+noise*.28;
                } else {
                    // Dense shoulder colony, broken diagonal shell plates, and exposed moss/lava islands.
                    double shoulder=1-new Vector3f(p.x,p.y+14,p.z-3).length()/25;
                    coverage=.35+shoulder*.55-p.y*.034-p.x*.025+noise*.25;
                    if(p.y>7)coverage-=.65;
                    if(cruncher)coverage-=.18;
                }
                double distance=channelDistance(p,face,leg,cruncher);
                boolean channel=distance<.65;
                if(coverage<.05 && !channel)continue;
                int tx=Math.floorMod((int)Math.floor(p.x+p.z*.33),64),ty=Math.floorMod((int)Math.floor(p.y+p.z*.67),64);
                int color=material.getRGB(tx+(cruncher?64:0),ty)|0xff000000;
                if(leg || (face && coverage<.3))color=material.getRGB(tx,ty+64)|0xff000000;
                if(channel) {
                    int brightness=(int)(130+70*(1-distance/.65));
                    color=0xff000000 | 12<<16 | brightness<<8 | Math.min(255,brightness+22);
                }
                growth.setRGB(x,y,color);
                int r=color>>16&255,g=color>>8&255,blue=color&255;
                if(channel || (g>90 && blue>85 && g>r*1.4 && blue>r*1.4))veins.setRGB(x,y,color);
            }
        }
        return new Result(growth,veins);
    }

    private static final float[][][] BODY_ROUTES={
            {{-5,-14.5F,3},{-8,-14.5F,-3},{-11,-14.5F,-10},{-13,-10,-16}},
            {{-4,-14.5F,5},{1,-14.5F,7},{7,-14.5F,12},{12.5F,-10,15},{12.5F,-4,12}},
            {{-6,-14.5F,5},{-12.5F,-10,7},{-12.5F,-4,11},{-12.5F,3,16}}};
    private static final float[][][] FACE_ROUTES={
            {{-6.7F,-5,-3},{-6.7F,-2,-7},{-6.7F,2,-10},{-6.7F,4,-14}},
            {{-6,-7.4F,-4},{-3,-7.4F,-7},{0,-7.4F,-9}}};

    private static double channelDistance(Vector3f p,boolean face,boolean leg,boolean cruncher) {
        if(leg)return 99;
        double best=99;
        for(var route:face?FACE_ROUTES:BODY_ROUTES)for(int i=0;i<route.length-1;i++) {
            var a=route[i];var b=route[i+1];float dx=b[0]-a[0],dy=b[1]-a[1],dz=b[2]-a[2];
            float t=Math.clamp(((p.x-a[0])*dx+(p.y-a[1])*dy+(p.z-a[2])*dz)/(dx*dx+dy*dy+dz*dz),0,1);
            float x=p.x-a[0]-t*dx,y=p.y-a[1]-t*dy,z=p.z-a[2]-t*dz;
            best=Math.min(best,Math.sqrt(x*x+y*y+z*z));
        }
        return best;
    }

    static BufferedImage colorFrame(BufferedImage source,int frame,boolean cruncher) {
        if(!cruncher)return source;
        var result=new BufferedImage(source.getWidth(),source.getHeight(),BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++) {
            int color=source.getRGB(x,y),r=color>>16&255,g=color>>8&255,b=color&255;
            if((color>>>24)>0 && r>150 && g>45 && b<g*.7) {
                double heat=.72+.28*(.5+.5*Math.sin(Math.PI*2*(frame/16.0-x/39.0-y/51.0)));
                color=0xff000000|(int)(r*heat)<<16|(int)(g*heat)<<8|b;
            }
            result.setRGB(x,y,color);
        }
        return result;
    }

    static BufferedImage glowFrame(BufferedImage source,int frame) {
        var result=new BufferedImage(source.getWidth(),source.getHeight(),BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++) {
            int color=source.getRGB(x,y);if((color>>>24)==0)continue;
            int r=color>>16&255,g=color>>8&255,b=color&255;
            if(g<75||b<70||g<r*1.35||b<r*1.35)continue;
            double pulse=.24+.46*(.5+.5*Math.sin(Math.PI*2*(frame/16.0-x/43.0-y/59.0)));
            result.setRGB(x,y,0xff000000|(int)(r*pulse)<<16|(int)(g*pulse)<<8|(int)(b*pulse));
        }
        return result;
    }
}
