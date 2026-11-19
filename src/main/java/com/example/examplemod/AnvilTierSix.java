package com.example.examplemod;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;

/** Tier 6 only reads the workshop. It never spawns, binds, protects or damages crystals. */
public final class AnvilTierSix {
    private static TagKey<Block> tag(String s){return TagKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath(ExampleMod.MODID,s));}
    public static final TagKey<Block> ENERGIZED=tag("anvil_energized_caps"),OBSIDIAN=tag("anvil_obsidian_caps"),GLASS=tag("anvil_lens_glass"),COLLAR_SLABS=tag("anvil_collar_slabs");
    public static Block collarSlab(){return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace("polished_deepslate_slab"));}
    public static boolean matchesShape(net.minecraft.world.level.block.state.BlockState state,TagKey<Block> tag){return !tag.equals(COLLAR_SLABS)||state.hasProperty(SlabBlock.TYPE)&&state.getValue(SlabBlock.TYPE)==net.minecraft.world.level.block.state.properties.SlabType.BOTTOM;}
    public record Link(BlockPos from,BlockPos to){}
    public record Status(boolean complete,int color,Block glass,int crystals){}
    public static List<AnvilStages.Requirement> requirements(List<AnvilStages.Stage> stages,TagKey<Block> tag){return stages.stream().filter(s->s.tier()==6).flatMap(s->s.blocks().stream()).filter(r->r.tag().equals(tag)).toList();}
    public static BlockPos at(BlockPos core,Direction front,BlockPos offset){return core.relative(front.getClockWise(),offset.getX()).above(offset.getY()).relative(front.getOpposite(),offset.getZ());}
    public static boolean upgradedCap(Level l,BlockPos core,Direction front,int tier,AnvilStages.Requirement old){
        if(!old.tag().equals(AnvilStages.CAPS)||tier!=3&&tier!=4)return false;
        return requirements(AnvilStages.all(),tier==3?ENERGIZED:OBSIDIAN).stream().anyMatch(r->r.offset().equals(old.offset())&&r.matches(l,core,front));
    }
    /** A complete lower-tier shell keeps the upgrade guide stable while old caps are removed. */
    public static boolean canUpgrade(Level l,BlockPos core,Direction front,List<AnvilStages.Stage> stages){
        if(AncientAnvilBlockEntity.missing(l,core).size()>0||!AnvilBasins.inspect(l,core,front).complete())return false;
        if(stages.stream().filter(s->s.tier()>=3&&s.tier()<=5).count()!=3)return false;
        return stages.stream().filter(s->s.tier()>=3&&s.tier()<=5).flatMap(s->s.blocks().stream()).filter(r->!r.tag().equals(AnvilStages.CAPS)).allMatch(r->r.matches(l,core,front));
    }
    public static Block stainedGlass(String color){return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(color+"_stained_glass"));}
    public static String glassKey(Block b){if(b==Blocks.GLASS)return "clear";if(b==Blocks.TINTED_GLASS)return "tinted";return b instanceof StainedGlassBlock g?g.getColor().name():null;}
    public static boolean sameGlass(Block a,Block b){var key=glassKey(a);return key!=null&&key.equals(glassKey(b));}
    public static int glassColor(Block b){
        if(b==Blocks.TINTED_GLASS)return 0x746080;
        if(!(b instanceof StainedGlassBlock g))return 0xffffff;
        return switch(g.getColor()){
            case WHITE->0xffffff;case ORANGE->0xf9801d;case MAGENTA->0xc74ebd;case LIGHT_BLUE->0x3ab3da;case YELLOW->0xfed83d;case LIME->0x80c71f;case PINK->0xf38baa;case GRAY->0x474f52;case LIGHT_GRAY->0x9d9d97;case CYAN->0x169c9c;case PURPLE->0x8932b8;case BLUE->0x3c44aa;case BROWN->0x835432;case GREEN->0x5e7c16;case RED->0xb02e26;case BLACK->0x1d1d21;
        };
    }
    public static Block referenceGlass(Level l,BlockPos core,Direction front,List<AnvilStages.Stage> stages){
        var counts=new LinkedHashMap<String,Integer>();var samples=new HashMap<String,Block>();
        for(var r:requirements(stages,GLASS)){var p=r.at(core,front);if(l.hasChunkAt(p)){var b=l.getBlockState(p).getBlock();var key=glassKey(b);if(key!=null){counts.merge(key,1,Integer::sum);samples.putIfAbsent(key,b);}}}
        String chosen=null;int most=0;for(var entry:counts.entrySet())if(entry.getValue()>most){chosen=entry.getKey();most=entry.getValue();}return chosen==null?null:samples.get(chosen);
    }
    public static EndCrystal crystal(Level l,BlockPos cap){
        if(!l.hasChunkAt(cap))return null;
        for(var e:l.getEntitiesOfClass(EndCrystal.class,new AABB(cap.above()).inflate(.6,1,.6)))if(e.isAlive()&&Math.abs(e.getX()-cap.getX()-.5)<.35&&Math.abs(e.getZ()-cap.getZ()-.5)<.35&&Math.abs(e.getY()-cap.getY()-1)<.35)return e;
        return null;
    }
    public static Status inspect(Level l,BlockPos core,Direction front,List<AnvilStages.Stage> stages){
        var stage=stages.stream().filter(s->s.tier()==6).findFirst().orElse(null);var lens=referenceGlass(l,core,front,stages);int count=0;boolean complete=stage!=null&&lens!=null;
        if(stage!=null)for(var r:stage.blocks()){
            var p=r.at(core,front);boolean loaded=l.hasChunkAt(p)&&l.getWorldBorder().isWithinBounds(p);
            if(!loaded||!(r.tag().equals(GLASS)?lens!=null&&sameGlass(lens,l.getBlockState(p).getBlock()):r.matches(l,core,front)))complete=false;
            if(r.tag().equals(OBSIDIAN)&&loaded&&crystal(l,p)!=null)count++;
        }
        return new Status(complete&&count==4,lens==null?0xffffff:glassColor(lens),lens,count);
    }
    public static List<Vec3> towerCenters(List<AnvilStages.Stage> stages){
        var result=new ArrayList<Vec3>();var blocks=requirements(stages,ENERGIZED);
        for(int sx:new int[]{-1,1})for(int sz:new int[]{-1,1}){var group=blocks.stream().filter(r->Integer.signum(r.offset().getX())==sx&&Integer.signum(r.offset().getZ())==sz).toList();if(group.isEmpty())continue;double x=0,y=0,z=0;for(var r:group){x+=r.offset().getX()+.5;y+=r.offset().getY()+1.02;z+=r.offset().getZ()+.5;}result.add(new Vec3(x/group.size(),y/group.size(),z/group.size()));}return List.copyOf(result);
    }
    public static List<Vec3> adjacentTowers(List<AnvilStages.Stage> stages,BlockPos cap){
        var towers=new ArrayList<>(towerCenters(stages));var center=Vec3.atCenterOf(cap);towers.sort(Comparator.comparingDouble(v->v.distanceToSqr(center)));return List.copyOf(towers.subList(0,Math.min(2,towers.size())));
    }
    private record Cache(List<AnvilStages.Stage> source,List<Link> links){}
    private static volatile Cache cached=new Cache(List.of(),List.of());
    /** Minimum-distance bijection; a keystone is never reused by two energized caps. */
    public static List<Link> links(List<AnvilStages.Stage> stages){
        var c=cached;if(c.source()==stages)return c.links();
        var order=Comparator.<BlockPos>comparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ).thenComparingInt(BlockPos::getY);
        var from=requirements(stages,ENERGIZED).stream().map(AnvilStages.Requirement::offset).sorted(order).toList();var to=stages.stream().filter(s->s.tier()==5).flatMap(s->s.blocks().stream()).filter(r->r.tag().equals(AnvilStages.KEYSTONES)).map(AnvilStages.Requirement::offset).sorted(order).toList();
        if(from.size()!=16||to.size()!=16)return List.of();
        // Hungarian assignment in O(n^3), with sorted inputs for stable tie-breaking.
        int n=16;double[] u=new double[n+1],v=new double[n+1];int[] p=new int[n+1],way=new int[n+1];
        for(int i=1;i<=n;i++){p[0]=i;int j0=0;var min=new double[n+1];Arrays.fill(min,Double.POSITIVE_INFINITY);var used=new boolean[n+1];do{used[j0]=true;int i0=p[j0],j1=0;double delta=Double.POSITIVE_INFINITY;for(int j=1;j<=n;j++)if(!used[j]){double cur=from.get(i0-1).distSqr(to.get(j-1))-u[i0]-v[j];if(cur<min[j]){min[j]=cur;way[j]=j0;}if(min[j]<delta){delta=min[j];j1=j;}}for(int j=0;j<=n;j++)if(used[j]){u[p[j]]+=delta;v[j]-=delta;}else min[j]-=delta;j0=j1;}while(p[j0]!=0);do{int j1=way[j0];p[j0]=p[j1];j0=j1;}while(j0!=0);}
        var result=new ArrayList<Link>();for(int j=1;j<=n;j++)result.add(new Link(from.get(p[j]-1),to.get(j-1)));var out=List.copyOf(result);cached=new Cache(stages,out);return out;
    }
    public static Vec3 world(BlockPos core,Direction front,Vec3 local){var right=front.getClockWise();var back=front.getOpposite();double x=local.x-.5,z=local.z-.5;return new Vec3(core.getX()+.5+right.getStepX()*x+back.getStepX()*z,core.getY()+local.y,core.getZ()+.5+right.getStepZ()*x+back.getStepZ()*z);}
    private AnvilTierSix(){}
}
