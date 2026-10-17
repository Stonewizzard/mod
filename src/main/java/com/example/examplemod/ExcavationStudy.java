package com.example.examplemod;

import java.util.Arrays;
import java.util.Random;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Server-owned excavation state saved on the specimen, including interrupted studies. */
public final class ExcavationStudy {
    public static final int COLS=28,ROWS=12,CELLS=COLS*ROWS;
    public static final String KEY="examplemod:excavation_study";
    private static final int[] RELIC_ROWS={0,0,0,0x3c,0x33fbfc,0xfffff8,0x1fffff0,0x3ffffc0,0x39ffe80,0,0,0};
    private final byte[] depths=new byte[CELLS],scars=new byte[CELLS];
    private final ArtifactProfile profile;
    private int damage,strokes;
    public ExcavationStudy(){this(ArtifactProfile.legacy());}
    public ExcavationStudy(ArtifactProfile profile){
        this.profile=profile;var random=new Random(profile.seed()^0x519ac7f2L);
        for(int i=0;i<CELLS;i++)depths[i]=(byte)(coveredCell(i)?(profile.find()==ArtifactProfile.Find.LEGACY?3:Math.max(2,profile.matrix().depth+random.nextInt(3)-1)):0);
    }
    public ArtifactProfile profile(){return profile;}
    public ExcavationStudy copy(){
        var result=new ExcavationStudy(profile);System.arraycopy(depths,0,result.depths,0,CELLS);System.arraycopy(scars,0,result.scars,0,CELLS);result.damage=damage;result.strokes=strokes;return result;
    }
    public static boolean coveredCell(int cell){
        if(cell<0||cell>=CELLS)return false;int x=cell%COLS,y=cell/COLS;
        return !((x==0||x==COLS-1)&&(y<2||y>=ROWS-2)||(x<2||x>=COLS-2)&&(y==0||y==ROWS-1));
    }
    public static boolean relicCell(int cell){return cell>=0&&cell<CELLS&&(RELIC_ROWS[cell/COLS]&(1<<(cell%COLS)))!=0;}
    public boolean inclusion(int cell){
        if(profile.find()==ArtifactProfile.Find.LEGACY||profile.find()==ArtifactProfile.Find.ALLOY)return relicCell(cell);
        return ArtifactFootprints.contains(profile.find(),cell);
    }
    public int depth(int cell){return cell>=0&&cell<CELLS?depths[cell]:0;}
    public int scar(int cell){return cell>=0&&cell<CELLS?scars[cell]:0;}
    public int condition(){return 100-damage;}
    public int strokes(){return strokes;}
    public int progress(){int all=0,exposed=0;for(int i=0;i<CELLS;i++)if(inclusion(i)){all++;if(depths[i]==0)exposed++;}return all==0?0:exposed*100/all;}
    public boolean complete(){return progress()==100;}
    public boolean recoverable(){return complete()||condition()==0;}
    public static int power(int tool,int tier){return tool==3?(tier==0?2:3):1;}
    public static int damageCap(int tier){return tier==0?8:tier==1?5:3;}
    public static int radius(int tool){return radius(tool,0);}
    public static int radius(int tool,int tier){return tool==4?2+tier:tool==3?2+(tier==2?1:0):tool==2?1+(tier==2?1:0):tool==1&&tier==2?1:0;}
    public static boolean affects(int tool,int center,int cell){return affects(tool,0,center,cell);}
    public static boolean affects(int tool,int tier,int center,int cell){
        if(center<0||center>=CELLS||cell<0||cell>=CELLS)return false;
        int dx=cell%COLS-center%COLS,dy=cell/COLS-center/COLS,r=radius(tool,tier);
        return tool==2&&tier<2?Math.abs(dx)<=r&&Math.abs(dy)<=r:dx*dx+dy*dy<=r*r;
    }
    public boolean work(int tool,int cell){return work(tool,0,cell);}
    public boolean work(int tool,int tier,int cell){
        if(tool<1||tool>3||!coveredCell(cell)||recoverable())return false;
        tier=Math.clamp(tier,0,2);boolean changed=false;int strokeDamage=0,power=power(tool,tier);
        for(int i=0;i<CELLS;i++)if(coveredCell(i)&&affects(tool,tier,cell,i)){
            int old=depths[i];if(old==0)continue;
            if(tool==2&&old>(tier==0?1:2))continue;
            int next=Math.max(tool==1&&tier>0?1:0,old-power);
            if(next!=old){depths[i]=(byte)next;changed=true;}
            if(tool!=2&&!(tool==1&&tier>0)&&old<=power&&inclusion(i)){
                scars[i]=(byte)Math.min(3,scars[i]+1);strokeDamage+=tool==3?3:1;changed=true;
            }
        }
        if(changed){damage=Math.min(100,damage+Math.min(damageCap(tier),strokeDamage));strokes++;}
        return changed;
    }
    public static ExcavationStudy read(ItemStack stack){
        var result=new ExcavationStudy(ArtifactProfile.read(stack));var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(KEY);
        int version=tag.getIntOr("Version",0);if(version!=1&&version!=2)return result;
        var depths=tag.getByteArray("Depths").orElseGet(()->new byte[0]);var scars=tag.getByteArray("Scars").orElseGet(()->new byte[0]);
        if(depths.length!=CELLS||scars.length!=CELLS)return result;
        for(int i=0;i<CELLS;i++){result.depths[i]=(byte)(coveredCell(i)?Math.clamp(depths[i],0,version==1?3:9):0);result.scars[i]=(byte)(result.inclusion(i)?Math.clamp(scars[i],0,3):0);}
        result.damage=Math.clamp(tag.getIntOr("Damage",0),0,100);result.strokes=Math.max(0,tag.getIntOr("Strokes",0));return result;
    }
    public void write(ItemStack stack){
        profile.write(stack);var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var tag=new CompoundTag();
        tag.putInt("Version",2);tag.putByteArray("Depths",depths.clone());tag.putByteArray("Scars",scars.clone());tag.putInt("Damage",damage);tag.putInt("Strokes",strokes);root.put(KEY,tag);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
    }
    /** Compatibility helper for tests; not exposed as a survival action. */
    public static void reset(ItemStack stack){var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();root.remove(KEY);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));}
    @Override public boolean equals(Object other){return other instanceof ExcavationStudy s&&damage==s.damage&&strokes==s.strokes&&profile.equals(s.profile)&&Arrays.equals(depths,s.depths)&&Arrays.equals(scars,s.scars);}
    @Override public int hashCode(){return 31*(31*profile.hashCode()+Arrays.hashCode(depths))+Arrays.hashCode(scars)+damage+strokes;}
}
