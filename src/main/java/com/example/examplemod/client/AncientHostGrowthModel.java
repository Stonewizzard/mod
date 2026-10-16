package com.example.examplemod.client;

import java.util.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/** A fitted colony, surface roots and asymmetric armor grafted onto native bones. */
final class AncientHostGrowthModel extends EntityModel<AncientHostGrowthState> {
    private final Map<String,ModelPart> sources;
    private final Map<String,ModelPart> followers=new LinkedHashMap<>();
    private final ModelPart parasite;
    private final List<ModelPart> fronds=new ArrayList<>();
    private final List<ModelPart> glands=new ArrayList<>();
    private final List<ModelPart> filaments=new ArrayList<>();

    private AncientHostGrowthModel(ModelPart root,Map<String,ModelPart> sources,String bodyPath) {
        super(root);
        this.sources=sources;
        sources.forEach((path,part)->followers.put(path,resolve(root,path)));
        var growth=resolve(root,bodyPath).getChild("infection_growth");
        parasite=growth.getChild("colony");
        for(int i=0;i<7;i++)fronds.add(growth.getChild("ridge_"+i));
        for(int i=0;i<4;i++) {
            glands.add(growth.getChild("gland_"+i));
            filaments.add(growth.getChild("filament_"+i));
        }
    }

    static AncientHostGrowthModel create(ModelPart nativeRoot,boolean cruncher) {
        var geometry=new AdaptiveSculkSkin(nativeRoot);
        var mesh=new MeshDefinition();
        var definitions=new LinkedHashMap<String,PartDefinition>();
        var sources=new LinkedHashMap<String,ModelPart>();
        definitions.put("",mesh.getRoot()); sources.put("",nativeRoot);
        for(var surface:geometry.surfaces) {
            String parent="";
            for(String token:surface.path().split("/")) {
                if(token.isEmpty())continue;
                String path=parent+"/"+token;
                if(!definitions.containsKey(path)) {
                    var source=resolve(nativeRoot,path);
                    definitions.put(path,definitions.get(parent).addOrReplaceChild(token,CubeListBuilder.create(),source.getInitialPose()));
                    sources.put(path,source);
                }
                parent=path;
            }
        }
        String bodyPath=pathEnding(definitions,"/body");
        String headPath=pathEnding(definitions,"/head");
        body(definitions.get(bodyPath),cruncher);
        head(definitions.get(headPath),cruncher);
        int leg=0;
        var legs=new HashSet<String>();
        for(var surface:geometry.surfaces) {
            String path=surface.path();String name=path.substring(path.lastIndexOf('/')+1);
            if(!name.contains("leg") || !legs.add(path))continue;
            leg(definitions.get(path),surface,leg++);
        }
        if(cruncher) {
            jaw(definitions.get(pathEnding(definitions,"/upper_jaw")),true);
            jaw(definitions.get(pathEnding(definitions,"/lower_jaw")),false);
        }
        var root=LayerDefinition.create(mesh,128,128).bakeRoot();
        root.setInitialPose(nativeRoot.getInitialPose());
        return new AncientHostGrowthModel(root,sources,bodyPath);
    }

    AncientHostGrowthState snapshot(net.minecraft.client.renderer.entity.state.LivingEntityRenderState host,boolean cruncher) {
        var state=new AncientHostGrowthState();
        var poses=new LinkedHashMap<String,PartPose>(); sources.forEach((path,part)->poses.put(path,part.storePose()));
        var visibility=new LinkedHashMap<String,Boolean>();sources.forEach((path,part)->visibility.put(path,part.visible));
        state.visibility=Map.copyOf(visibility);
        state.poses=Map.copyOf(poses);state.ageInTicks=host.ageInTicks;state.isBaby=host.isBaby;
        state.walkAnimationPos=host.walkAnimationPos;state.walkAnimationSpeed=host.walkAnimationSpeed;
        state.cruncher=cruncher;state.outlineColor=host.outlineColor;
        var head=sources.entrySet().stream().filter(e->e.getKey().endsWith("/head")).findFirst().orElseThrow().getValue();
        state.activity=cruncher && host instanceof CruncherRenderState crunch && crunch.crunching?1:
                Mth.clamp(Math.abs(head.xRot)*.65F+host.walkAnimationSpeed*.18F,0,.8F);
        return state;
    }

    @Override public void setupAnim(AncientHostGrowthState state) {
        super.setupAnim(state);
        followers.forEach((path,part)->{
            part.loadPose(state.poses.getOrDefault(path,part.getInitialPose()));
            part.visible=state.visibility.getOrDefault(path,true);
        });
        float phase=state.ageInTicks*.09F;
        parasite.xScale=parasite.yScale=parasite.zScale=1.05F;
        parasite.y+=Mth.sin(phase)*.07F;
        SkulkParasiteGeometry.animate(parasite,phase+state.walkAnimationPos*.25F,
                .065F+state.walkAnimationSpeed*.06F+state.activity*.11F);
        for(int i=0;i<fronds.size();i++) {
            var frond=fronds.get(i);
            frond.zRot+=Mth.sin(phase*.7F-i*.65F)*(.025F+state.activity*.035F);
            frond.xRot+=Mth.sin(phase*.8F-i*.7F)*.02F;
        }
        for(int i=0;i<glands.size();i++) {
            float pulse=1+Mth.sin(phase*1.2F-i*.9F)*(.04F+state.activity*.025F);
            glands.get(i).xScale=glands.get(i).yScale=glands.get(i).zScale=pulse;
            var filament=filaments.get(i);
            filament.yRot+=Mth.sin(phase*.9F+i)*(.07F+state.activity*.06F);
            filament.getChild("curl").yRot+=Mth.sin(phase*.75F+i*1.2F)*.14F;
        }
    }

    private static void body(PartDefinition parent,boolean cruncher) {
        var growth=parent.addOrReplaceChild("infection_growth",CubeListBuilder.create(),PartPose.ZERO);
        SkulkParasiteGeometry.addParasite(growth,"colony",PartPose.offsetAndRotation(-5.2F,-16.3F,4,.03F,.15F,-.04F));
        for(int i=0;i<7;i++) {
            float z=-13+i*4.8F;
            growth.addOrReplaceChild("ridge_"+i,CubeListBuilder.create().texOffs(64+i%3*12,0)
                    .addBox(-2.1F,-(cruncher?3:2.2F),-2,4.2F,cruncher?3:2.2F,4)
                    .texOffs(0+i%3*10,64).addBox(-.5F,-(cruncher?4.2F:3.2F),-1,.9F,1.8F,2),
                    PartPose.offsetAndRotation(1.5F+Mth.sin(i*.8F)*1.3F,-14.5F,z,0,(i%2==0?1:-1)*.12F,(i%2==0?1:-1)*.13F));
        }
        growth.addOrReplaceChild("left_armor",CubeListBuilder.create().texOffs(70,12)
                .addBox(-13.35F,-12,-8,1.2F,6,7)
                .texOffs(86,12).addBox(-13.3F,-7,5,1.1F,6,8)
                .texOffs(68,28).addBox(-12.9F,0,12,1,4,5),PartPose.ZERO);
        growth.addOrReplaceChild("right_armor",CubeListBuilder.create().texOffs(96,12)
                .addBox(12.35F,-12,8,1.1F,4,6),PartPose.ZERO);
        vessel(growth,"shoulder",new float[][]{{-5,-14.8F,2},{-9,-14.8F,-2},{-13,-12,-5},{-13.25F,-7,-11},{-13,-3,-15}},.8F);
        vessel(growth,"left_flank",new float[][]{{-6,-14.8F,6},{-11,-14.7F,7},{-13.2F,-10,9},{-13.3F,-5,13},{-13.1F,1,16}},.7F);
        vessel(growth,"spine",new float[][]{{-3,-14.9F,6},{1,-14.9F,9},{6,-14.8F,13},{11,-14.8F,16},{13,-11,16},{13,-7,12}},.6F);
        vessel(growth,"neck",new float[][]{{-4,-14.8F,0},{-3,-14.7F,-7},{-6,-14.8F,-14},{-10,-11,-19.8F},{-8,-5,-20.3F}},.65F);
        for(int i=0;i<4;i++) {
            growth.addOrReplaceChild("gland_"+i,CubeListBuilder.create().texOffs(80+i*8,72)
                    .addBox(-.8F,-1.1F,-1.1F,1.6F,2.2F,2.2F),PartPose.offset(-13.4F,-10+i*3.5F,5+i*2.7F));
            var filament=growth.addOrReplaceChild("filament_"+i,CubeListBuilder.create().texOffs(72+i*8,88)
                    .addBox(-.35F,-4,-.35F,.7F,4,.7F),
                    PartPose.offsetAndRotation(-9+i*2.7F,-14.7F,9+i*2,0,i*.4F,(i%2==0?1:-1)*.35F));
            filament.addOrReplaceChild("curl",CubeListBuilder.create().texOffs(0,76)
                    .addBox(-.23F,-3.2F,-.23F,.46F,3.2F,.46F),PartPose.offsetAndRotation(0,-3.7F,0,.4F,i*.3F,.25F));
        }
    }

    private static void head(PartDefinition parent,boolean cruncher) {
        var growth=parent.addOrReplaceChild("infection_face",CubeListBuilder.create().texOffs(72,32)
                .addBox(-7,-6,-8,1,4,4)
                .texOffs(0,84).addBox(-7.1F,-6.5F,-6,.7F,1.8F,1.4F),PartPose.ZERO);
        vessel(growth,"temple",new float[][]{{-6.8F,-5,-3},{-6.9F,-3,-6},{-6.9F,0,-9},{-6.8F,3,-10}},.5F);
        vessel(growth,"brow",new float[][]{{-6,-7.8F,-5},{-3,-7.8F,-7},{0,-7.8F,-9}},.45F);
        if(cruncher) {
            growth.addOrReplaceChild("infected_hinge",CubeListBuilder.create().texOffs(96,32)
                    .addBox(-9.15F,-1.5F,-8.5F,1.4F,4,4)
                    .texOffs(80,80).addBox(-7.25F,-4,-7.6F,.5F,.8F,2.7F),PartPose.ZERO);
        }
    }

    private static void jaw(PartDefinition parent,boolean upper) {
        var growth=parent.addOrReplaceChild("infection_jaw",CubeListBuilder.create().texOffs(68,44)
                .addBox(-8.35F,.5F,-11,1,2.6F,5),PartPose.ZERO);
        vessel(growth,"jaw_roots",new float[][]{{-8.35F,1,-3},{-8.4F,2,-6},{-8.35F,1.8F,-10},{-6,1.7F,-13.3F}},.4F);
        if(upper)growth.addOrReplaceChild("fang_root",CubeListBuilder.create().texOffs(112,80)
                .addBox(-6.5F,3.65F,-13.2F,.9F,1.7F,.8F),PartPose.ZERO);
    }

    private static void leg(PartDefinition parent,AdaptiveSculkSkin.Surface surface,int index) {
        var c=surface.cube();boolean left=surface.transform().m30()>0;
        float x=left?c.maxX+.1F:c.minX-.7F;
        var growth=parent.addOrReplaceChild("infection_leg",CubeListBuilder.create().texOffs(70+index%3*12,48)
                .addBox(x,c.minY+1,c.minZ+1,.6F,3.4F,3.2F),PartPose.ZERO);
        vessel(growth,"leg_root",new float[][]{{x,c.minY+.2F,c.minZ+2},{x,c.minY+2,c.minZ+1},{x,c.minY+5,c.minZ+.3F}},.32F);
    }

    private static void vessel(PartDefinition parent,String name,float[][] points,float thickness) {
        for(int i=0;i<points.length-1;i++) {
            var a=points[i];var b=points[i+1];float dx=b[0]-a[0],dy=b[1]-a[1],dz=b[2]-a[2];
            float length=Mth.sqrt(dx*dx+dy*dy+dz*dz),w=thickness*(1-i*.09F);
            parent.addOrReplaceChild(name+i,CubeListBuilder.create().texOffs(64+i%4*10,96)
                    .addBox(-w/2,-w/2,0,w,w,length+.1F),
                    PartPose.offsetAndRotation(a[0],a[1],a[2],(float)Math.atan2(-dy,Math.sqrt(dx*dx+dz*dz)),(float)Math.atan2(dx,dz),0));
        }
    }

    static ModelPart resolve(ModelPart root,String path) {
        var part=root;for(String token:path.split("/"))if(!token.isEmpty())part=part.getChild(token);return part;
    }
    private static String pathEnding(Map<String,?> paths,String ending) {
        return paths.keySet().stream().filter(p->p.endsWith(ending)).findFirst().orElseThrow(()->new IllegalStateException("Missing native bone "+ending));
    }
}
