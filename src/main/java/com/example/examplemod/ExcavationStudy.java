package com.example.examplemod;

import java.util.Arrays;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Non-consuming study state lives on the artifact, so moving it cannot lose the excavation. */
public final class ExcavationStudy {
    public static final int COLS=28,ROWS=12,CELLS=COLS*ROWS;
    public static final String KEY="examplemod:excavation_study";
    // Alpha footprint of the neutral specimen artwork, sampled onto the study grid.
    private static final int[] RELIC_ROWS={0x00000000,0x00000000,0x00000000,0x0000003c,0x0033fbfc,0x00fffff8,0x01fffff0,0x03ffffc0,0x039ffe80,0x00000000,0x00000000,0x00000000};
    private final byte[] depths=new byte[CELLS],scars=new byte[CELLS];
    private int damage;
    public ExcavationStudy(){for(int i=0;i<CELLS;i++)depths[i]=(byte)(coveredCell(i)?3:0);}
    public ExcavationStudy copy(){
        var result=new ExcavationStudy();
        System.arraycopy(depths,0,result.depths,0,CELLS);System.arraycopy(scars,0,result.scars,0,CELLS);
        result.damage=damage;return result;
    }
    public static boolean coveredCell(int cell){
        if(cell<0||cell>=CELLS)return false;
        int x=cell%COLS,y=cell/COLS;
        return !((x==0||x==COLS-1)&&(y<2||y>=ROWS-2) || (x<2||x>=COLS-2)&&(y==0||y==ROWS-1));
    }
    public static boolean relicCell(int cell){return cell>=0 && cell<CELLS && (RELIC_ROWS[cell/COLS]&(1<<(cell%COLS)))!=0;}
    public int depth(int cell){return cell>=0&&cell<CELLS?depths[cell]:0;}
    public int scar(int cell){return cell>=0&&cell<CELLS?scars[cell]:0;}
    public int condition(){return 100-damage;}
    public int progress(){
        int all=0,exposed=0;
        for(int i=0;i<CELLS;i++)if(relicCell(i)){all++;if(depths[i]==0)exposed++;}
        return all==0?0:exposed*100/all;
    }
    public boolean complete(){return progress()==100;}
    public static int radius(int tool){return tool==3||tool==4?2:tool==2?1:0;}
    public static boolean affects(int tool,int center,int cell){
        if(center<0||center>=CELLS||cell<0||cell>=CELLS)return false;
        int dx=cell%COLS-center%COLS,dy=cell/COLS-center/COLS,r=radius(tool);
        return tool==2?Math.abs(dx)<=r&&Math.abs(dy)<=r:dx*dx+dy*dy<=r*r;
    }
    public boolean work(int tool,int cell){
        if(tool<1||tool>3||!coveredCell(cell)||(complete()&&tool!=2))return false;
        boolean changed=false;int power=tool==3?2:1;
        for(int i=0;i<CELLS;i++)if(coveredCell(i)&&affects(tool,cell,i)){
            int old=depths[i];
            if(tool==2 && old>1)continue;
            int next=Math.max(0,old-power);
            if(next!=old){depths[i]=(byte)next;changed=true;}
            if(tool!=2 && old<=power && relicCell(i)){
                int nextDamage=Math.min(100,damage+(tool==3?3:1));
                int nextScar=Math.min(3,scars[i]+1);
                if(nextDamage!=damage||nextScar!=scars[i])changed=true;
                damage=nextDamage;scars[i]=(byte)nextScar;
            }
        }
        return changed;
    }
    public static ExcavationStudy read(ItemStack stack){
        var result=new ExcavationStudy();
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(KEY);
        if(tag.getIntOr("Version",0)!=1)return result;
        var depths=tag.getByteArray("Depths").orElseGet(()->new byte[0]);
        var scars=tag.getByteArray("Scars").orElseGet(()->new byte[0]);
        if(depths.length!=CELLS || scars.length!=CELLS)return result;
        for(int i=0;i<CELLS;i++){
            result.depths[i]=(byte)(coveredCell(i)?Math.clamp(depths[i],0,3):0);
            result.scars[i]=(byte)(relicCell(i)?Math.clamp(scars[i],0,3):0);
        }
        result.damage=Math.clamp(tag.getIntOr("Damage",0),0,100);return result;
    }
    public void write(ItemStack stack){
        var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        var tag=new CompoundTag();tag.putInt("Version",1);tag.putByteArray("Depths",depths.clone());tag.putByteArray("Scars",scars.clone());tag.putInt("Damage",damage);
        root.put(KEY,tag);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
    }
    public static void reset(ItemStack stack){
        var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();root.remove(KEY);
        if(root.isEmpty())stack.remove(DataComponents.CUSTOM_DATA);else stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
    }
    @Override public boolean equals(Object other){return other instanceof ExcavationStudy s && damage==s.damage && Arrays.equals(depths,s.depths)&&Arrays.equals(scars,s.scars);}
    @Override public int hashCode(){return 31*(31*damage+Arrays.hashCode(depths))+Arrays.hashCode(scars);}
}
