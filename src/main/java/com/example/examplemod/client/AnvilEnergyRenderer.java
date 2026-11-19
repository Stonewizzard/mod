package com.example.examplemod.client;

import com.example.examplemod.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Snapshot-only visual beams: no world edits, entity effects, collision or damage. */
public final class AnvilEnergyRenderer {
    private static final ForgeGhostModel CUBE=new ForgeGhostModel(0);
    private static final Identifier WHITE=Identifier.fromNamespaceAndPath(ExampleMod.MODID,"textures/effect/anvil_energy.png");
    public record Beam(Vec3 from,Vec3 to,int color,float width,boolean blast){}
    public static List<Beam> extract(AncientAnvilBlockEntity be,float partial){
        var l=be.getLevel();if(l==null||be.getBlockState().getValue(AncientAnvilBlock.TIER)!=6)return List.of();
        var core=be.getBlockPos();var front=be.getBlockState().getValue(AncientAnvilBlock.FACING);var stages=AnvilGuide.stages(l);var origin=Vec3.atLowerCornerOf(core);var out=new ArrayList<Beam>();
        for(var link:AnvilTierSix.links(stages)){
            var from=Vec3.atCenterOf(AnvilTierSix.at(core,front,link.from())).add(0,.51,0).subtract(origin);
            var to=Vec3.atCenterOf(AnvilTierSix.at(core,front,link.to())).subtract(origin);out.add(new Beam(from,to,0x62d8d1,.10F,false));
        }
        for(var req:AnvilTierSix.requirements(stages,AnvilTierSix.OBSIDIAN)){
            var cap=req.at(core,front);var crystal=AnvilTierSix.crystal(l,cap);if(crystal==null)continue;
            var from=crystal.position().add(0,1,0);var nearest=AnvilTierSix.adjacentTowers(stages,req.offset());
            for(int i=0;i<Math.min(2,nearest.size());i++)out.add(new Beam(from.subtract(origin),AnvilTierSix.world(core,front,nearest.get(i)).subtract(origin),0xd58afa,.12F,false));
        }
        float remaining=be.energyUntil-l.getGameTime()-partial;
        if(remaining>0){float age=(float)(l.getGameTime()+partial-be.energyStarted);double bottom=Math.max(1.84,48-age*16);if(bottom<48)out.add(new Beam(new Vec3(.5,48,.5),new Vec3(.5,bottom,.5),be.energyColor,2.1F*(float)Math.min(1,remaining/8),true));}
        return List.copyOf(out);
    }
    public static void submit(List<Beam> beams,float time,PoseStack p,SubmitNodeCollector collector){
        for(var beam:beams){
            var delta=beam.to().subtract(beam.from());double length=delta.length();if(length<.001)continue;
            p.pushPose();p.translate(beam.from().x,beam.from().y,beam.from().z);
            p.mulPose(new Matrix4f().rotation(new Quaternionf().rotationTo(new Vector3f(0,1,0),new Vector3f((float)delta.x,(float)delta.y,(float)delta.z).normalize())));
            p.rotateDegrees(Axis.YP,time*(beam.blast()?5:1.5F));
            int rgb=beam.color()&0xffffff;float pulse=beam.blast()?1+.07F*(float)Math.sin(time*1.7):1+.08F*(float)Math.sin(time*.12);
            shell(p,collector,(float)length,beam.width()*pulse,0x50000000|rgb);
            if(beam.blast())shell(p,collector,(float)length,beam.width()*.45F,0x85000000|rgb);
            int bright=lighten(rgb,beam.blast()?.45F:.65F);shell(p,collector,(float)length,beam.width()*(beam.blast()?.12F:.25F),0xe0000000|bright);p.popPose();
        }
    }
    private static int lighten(int color,float factor){int r=color>>16&255,g=color>>8&255,b=color&255;return (int)(r+(255-r)*factor)<<16|(int)(g+(255-g)*factor)<<8|(int)(b+(255-b)*factor);}
    private static void shell(PoseStack p,SubmitNodeCollector c,float length,float width,int color){
        p.pushPose();p.scale(width,length,width);p.translate(-.5,0,-.5);c.order(2).submitModel(CUBE,new LivingEntityRenderState(),p,RenderTypes.entityTranslucent(WHITE),15728880,OverlayTexture.NO_OVERLAY,color,null,0);p.popPose();
    }
    private AnvilEnergyRenderer(){}
}
