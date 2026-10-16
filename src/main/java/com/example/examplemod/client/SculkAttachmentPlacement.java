package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Fit the attached body clear of adjacent host bones without repainting the host. */
final class SculkAttachmentPlacement {
    final Vector3f point;
    final Vector3f normal;
    final float scale;

    private SculkAttachmentPlacement(Vector3f point, Vector3f normal, float scale) {
        this.point = new Vector3f(point);
        this.normal = new Vector3f(normal);
        this.scale = scale;
    }

    static SculkAttachmentPlacement original(AdaptiveSculkSkin geometry) {
        return new SculkAttachmentPlacement(geometry.localAnchor, geometry.normal, geometry.scale);
    }

    static SculkAttachmentPlacement fit(AdaptiveSculkSkin geometry, EntityType<?> type) {
        var original = original(geometry);
        if (geometry.anchor == null || type == EntityTypes.SNIFFER || type == ExampleMod.CRUNCHER.get()
                || type == EntityTypes.PANDA) return original;
        var preferred = original;
        if (type == EntityTypes.IRON_GOLEM) {
            // The wide chest must use its back, rather than the top under the head.
            preferred = face(geometry, new Vector3f(0, 0, 1), geometry.scale);
        } else if (type == EntityTypes.LLAMA || type == EntityTypes.TRADER_LLAMA) {
            // Give the long neck clearance, retaining the existing position on the back.
            var localBack = new Matrix4f(geometry.anchor.transform()).invert()
                    .transformDirection(new Vector3f(0, 0, 1)).normalize().mul(3);
            localBack.fma(-localBack.dot(original.normal), original.normal);
            preferred = new SculkAttachmentPlacement(new Vector3f(original.point).add(localBack), original.normal, original.scale);
        }
        if (collisions(geometry, preferred) == 0) return preferred;
        var candidates = new ArrayList<SculkAttachmentPlacement>();
        candidates.add(preferred);
        addNearby(geometry, preferred, candidates);
        for (var direction : List.of(new Vector3f(0, 0, 1), new Vector3f(0, -1, 0),
                new Vector3f(1, 0, 0), new Vector3f(-1, 0, 0))) {
            var alternative = face(geometry, direction, original.scale);
            candidates.add(alternative);
            addNearby(geometry, alternative, candidates);
        }
        SculkAttachmentPlacement best = preferred;
        double bestScore = Double.POSITIVE_INFINITY;
        for (var candidate : candidates) {
            double score = collisions(geometry, candidate) * 1000
                    + candidate.point.distanceSquared(preferred.point)
                    + (1 - candidate.normal.dot(preferred.normal)) * 64
                    + (original.scale - candidate.scale) * 100;
            if (score < bestScore) { bestScore = score; best = candidate; }
        }
        return best;
    }

    private static void addNearby(AdaptiveSculkSkin geometry, SculkAttachmentPlacement base,
            List<SculkAttachmentPlacement> candidates) {
        var c = geometry.anchor.cube();
        var size = new Vector3f(c.maxX-c.minX, c.maxY-c.minY, c.maxZ-c.minZ);
        int axis = Math.abs(base.normal.x) > .5 ? 0 : Math.abs(base.normal.y) > .5 ? 1 : 2;
        int first = (axis+1)%3, second = (axis+2)%3;
        for (float shrink : new float[]{1, .85F}) for (float u : new float[]{0, -.2F, .2F, -.35F, .35F})
            for (float v : new float[]{0, -.2F, .2F, -.35F, .35F}) {
                var p = new Vector3f(base.point);
                p.setComponent(first, Math.clamp(p.get(first)+size.get(first)*u,
                        minimum(c,first)+size.get(first)*.12F, maximum(c,first)-size.get(first)*.12F));
                p.setComponent(second, Math.clamp(p.get(second)+size.get(second)*v,
                        minimum(c,second)+size.get(second)*.12F, maximum(c,second)-size.get(second)*.12F));
                candidates.add(new SculkAttachmentPlacement(p, base.normal, base.scale*shrink));
            }
    }

    private static SculkAttachmentPlacement face(AdaptiveSculkSkin geometry, Vector3f worldNormal, float scale) {
        Vector3f normal = null; float best = -2;
        for (int axis=0;axis<3;axis++) for(int sign:new int[]{-1,1}) {
            var n=new Vector3f().setComponent(axis,sign);
            float alignment=geometry.anchor.transform().transformDirection(new Vector3f(n)).normalize().dot(worldNormal);
            if(alignment>best){best=alignment;normal=n;}
        }
        var c=geometry.anchor.cube();
        var p=new Vector3f((c.minX+c.maxX)/2,(c.minY+c.maxY)/2,(c.minZ+c.maxZ)/2);
        for(int axis=0;axis<3;axis++) if(Math.abs(normal.get(axis))>.5)
            p.setComponent(axis,normal.get(axis)<0?minimum(c,axis):maximum(c,axis));
        return new SculkAttachmentPlacement(p,normal,scale);
    }

    Matrix4f transform(AdaptiveSculkSkin geometry) {
        var offset=new Vector3f(point).fma(2.1F*scale,normal).div(16);
        return new Matrix4f(geometry.anchor.transform()).translate(offset)
                .rotate(new Quaternionf().rotationTo(new Vector3f(0,-1,0),normal)).scale(scale);
    }

    /** Test the solid shell and mouth; thin tendrils may deliberately meet the host skin. */
    static int collisions(AdaptiveSculkSkin geometry, SculkAttachmentPlacement placement) {
        if(geometry.anchor==null)return 0;
        var transform=placement.transform(geometry);
        int collisions=0;
        for(var surface:geometry.surfaces) {
            if(surface==geometry.anchor)continue;
            var inverse=new Matrix4f(surface.transform()).invert();
            for(float[] box:new float[][]{{-3.8F,-1.8F,-4.8F,3.8F,1.8F,4.8F},{-2.8F,-.8F,-8.8F,2.8F,1.8F,-5.2F}})
                for(int x=0;x<3;x++)for(int y=0;y<3;y++)for(int z=0;z<3;z++) {
                    var p=new Vector3f(box[0]+(box[3]-box[0])*x/2,box[1]+(box[4]-box[1])*y/2,box[2]+(box[5]-box[2])*z/2).div(16);
                    transform.transformPosition(p);inverse.transformPosition(p).mul(16);
                    var c=surface.cube();
                    if(p.x>c.minX+.15F && p.x<c.maxX-.15F && p.y>c.minY+.15F && p.y<c.maxY-.15F
                            && p.z>c.minZ+.15F && p.z<c.maxZ-.15F)collisions++;
                }
        }
        return collisions;
    }

    private static float minimum(net.minecraft.client.model.geom.ModelPart.Cube cube,int axis) {
        return axis==0?cube.minX:axis==1?cube.minY:cube.minZ;
    }
    private static float maximum(net.minecraft.client.model.geom.ModelPart.Cube cube,int axis) {
        return axis==0?cube.maxX:axis==1?cube.maxY:cube.maxZ;
    }
}
