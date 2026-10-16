package com.example.examplemod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.image.BufferedImage;
import java.util.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Project connected sculk tissue through native geometry into its own UV layout. */
final class AdaptiveSculkSkin {
    record Surface(String path, int index, ModelPart.Cube cube, Matrix4f transform) {}
    final List<Surface> surfaces = new ArrayList<>();
    final Surface anchor;
    final Vector3f localAnchor;
    final Vector3f normal;
    final float scale;

    AdaptiveSculkSkin(ModelPart root) {
        var parts = root.getAllParts();
        var saved = parts.stream().map(ModelPart::storePose).toList();
        var visible = new ArrayList<Boolean>();
        for (var part : parts) { visible.add(part.visible); part.resetPose(); part.visible = true; }
        try {
            root.visit(new PoseStack(), (pose, path, index, cube) ->
                    surfaces.add(new Surface(path, index, cube, new Matrix4f(pose.pose()))));
        } finally {
            for (int i = 0; i < parts.size(); i++) { parts.get(i).loadPose(saved.get(i)); parts.get(i).visible = visible.get(i); }
        }
        anchor = surfaces.stream().max(Comparator.comparingDouble(AdaptiveSculkSkin::score)).orElse(null);
        if (anchor == null) { localAnchor = new Vector3f(); normal = new Vector3f(0, -1, 0); scale = 0; return; }
        var cube = anchor.cube;
        float width = cube.maxX - cube.minX, height = cube.maxY - cube.minY, depth = cube.maxZ - cube.minZ;
        localAnchor = new Vector3f((cube.minX + cube.maxX) / 2, (cube.minY + cube.maxY) / 2,
                (cube.minZ + cube.maxZ) / 2);
        // Pick a local face whose posed normal points up, or backward on an upright torso.
        var extents = new Vector3f();
        float[] low = {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, high = {-Float.MAX_VALUE,-Float.MAX_VALUE,-Float.MAX_VALUE};
        for (int x = 0; x < 2; x++) for (int y = 0; y < 2; y++) for (int z = 0; z < 2; z++) {
            extents.set(x == 0 ? cube.minX : cube.maxX, y == 0 ? cube.minY : cube.maxY, z == 0 ? cube.minZ : cube.maxZ).div(16);
            anchor.transform.transformPosition(extents);
            for (int i = 0; i < 3; i++) { low[i] = Math.min(low[i], extents.get(i)); high[i] = Math.max(high[i], extents.get(i)); }
        }
        boolean upright = high[1]-low[1] > Math.max(high[0]-low[0],high[2]-low[2]) * 1.15;
        var desired = upright ? new Vector3f(0,0,1) : new Vector3f(0,-1,0);
        Vector3f best = null; float dot = -2;
        for (int axis = 0; axis < 3; axis++) for (int sign : new int[]{-1,1}) {
            var n = new Vector3f().setComponent(axis, sign);
            float d = anchor.transform.transformDirection(new Vector3f(n)).normalize().dot(desired);
            if (d > dot) { dot = d; best = n; }
        }
        normal = best;
        float faceWidth, faceHeight;
        if (Math.abs(normal.y) > .5) { localAnchor.y = normal.y < 0 ? cube.minY : cube.maxY; faceWidth=width; faceHeight=depth; }
        else if (Math.abs(normal.z) > .5) { localAnchor.z = normal.z < 0 ? cube.minZ : cube.maxZ; localAnchor.y -= height*.12F; faceWidth=width; faceHeight=height; }
        else { localAnchor.x = normal.x < 0 ? cube.minX : cube.maxX; faceWidth=depth; faceHeight=height; }
        scale = Math.clamp(Math.min(faceWidth,faceHeight) / 13, .12F, .9F);
    }

    private static double score(Surface surface) {
        var c = surface.cube;
        double volume = Math.max(.1, c.maxX-c.minX)*Math.max(.1,c.maxY-c.minY)*Math.max(.1,c.maxZ-c.minZ);
        String name = surface.path.toLowerCase(Locale.ROOT);
        if (name.endsWith("/body") || name.endsWith("/upper_body") || name.endsWith("/torso")) volume *= 32;
        if (name.contains("head") || name.contains("wing") || name.contains("leg") || name.contains("tail")) volume *= .15;
        return volume;
    }

    BufferedImage paint(BufferedImage skin, BufferedImage tissue) {
        var output = new BufferedImage(skin.getWidth(), skin.getHeight(), BufferedImage.TYPE_INT_ARGB);
        if (anchor == null) return output;
        var center = anchor.transform.transformPosition(new Vector3f(localAnchor).div(16)).mul(16);
        float radius = Math.max(5, (anchor.cube.maxX-anchor.cube.minX + anchor.cube.maxY-anchor.cube.minY + anchor.cube.maxZ-anchor.cube.minZ)*.38F);
        for (var surface : surfaces) for (var polygon : surface.cube.polygons) {
            var vertices = polygon.vertices();
            if (vertices.length != 4) continue;
            var a = vertices[0]; var b = vertices[1]; var d = vertices[3];
            float minU=1,maxU=0,minV=1,maxV=0;
            for (var v : vertices) { minU=Math.min(minU,v.u()); maxU=Math.max(maxU,v.u()); minV=Math.min(minV,v.v()); maxV=Math.max(maxV,v.v()); }
            int minX=Math.max(0,(int)Math.floor(minU*skin.getWidth())),maxX=Math.min(skin.getWidth(),(int)Math.ceil(maxU*skin.getWidth()));
            int minY=Math.max(0,(int)Math.floor(minV*skin.getHeight())),maxY=Math.min(skin.getHeight(),(int)Math.ceil(maxV*skin.getHeight()));
            float du1=b.u()-a.u(),dv1=b.v()-a.v(),du2=d.u()-a.u(),dv2=d.v()-a.v(),det=du1*dv2-du2*dv1;
            if (Math.abs(det)<1e-9) continue;
            for (int y=minY;y<maxY;y++) for(int x=minX;x<maxX;x++) {
                if ((skin.getRGB(x,y)>>>24)<128) continue;
                float u=(x+.5F)/skin.getWidth()-a.u(),v=(y+.5F)/skin.getHeight()-a.v();
                float s=(u*dv2-du2*v)/det,t=(du1*v-u*dv1)/det;
                if (s<0||s>1||t<0||t>1) continue;
                var p=new Vector3f(a.x()+(b.x()-a.x())*s+(d.x()-a.x())*t,
                        a.y()+(b.y()-a.y())*s+(d.y()-a.y())*t,a.z()+(b.z()-a.z())*s+(d.z()-a.z())*t).div(16);
                surface.transform.transformPosition(p).mul(16);
                double grain=Math.sin(p.x*.43+Math.sin(p.z*.28)*2)+Math.cos(p.y*.47-p.z*.32)+.4*Math.sin(p.x*1.7+p.y*.9);
                double coverage=1.1-p.distance(center)/radius+grain*.4;
                if (coverage<-.15) continue;
                int tx=Math.floorMod((int)Math.floor(p.x+p.z*.37),tissue.getWidth());
                int ty=Math.floorMod((int)Math.floor(p.y+p.z*.67),tissue.getHeight());
                int color=tissue.getRGB(tx,ty)|0xff000000;
                // Sparse cyan roots at the edge join the larger, textured sculk patches.
                if (coverage<.05 && Math.floorMod((int)(p.x+p.y*2+p.z),5)==0) color=0xff258b91;
                output.setRGB(x,y,color);
            }
        }
        return output;
    }
}
