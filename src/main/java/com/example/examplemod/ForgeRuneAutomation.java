package com.example.examplemod;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.*;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.fluid.*;
import net.neoforged.neoforge.transfer.transaction.*;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.fluids.FluidType;

/** NeoForge's current transactional transfer API. One mB of tagged liquid XP is one XP point. */
public final class ForgeRuneAutomation {
    public static final DeferredRegister<Fluid> FLUIDS=DeferredRegister.create(Registries.FLUID,ExampleMod.MODID);
    public static final DeferredRegister<FluidType> TYPES=DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_TYPES,ExampleMod.MODID);
    public static final DeferredHolder<FluidType,FluidType> TYPE=TYPES.register("experience",()->new FluidType(FluidType.Properties.create()){});
    public static final DeferredHolder<Fluid,Fluid> XP=FLUIDS.register("experience",ExperienceFluid::new);
    public static final TagKey<Fluid> XP_TAG=TagKey.create(Registries.FLUID,Identifier.parse("c:experience"));
    public static final class ExperienceFluid extends WaterFluid.Source {
        @Override public FluidType getFluidType(){return TYPE.get();}
        @Override public Fluid getFlowing(){return this;}
        @Override public Fluid getSource(){return this;}
        @Override public boolean isSame(Fluid fluid){return fluid==this;}
    }
    public static void register(RegisterCapabilitiesEvent e){
        e.registerBlockEntity(Capabilities.Item.BLOCK,ForgeRunes.ENTITY.get(),(be,side)->VanillaContainerWrapper.of(be));
        e.registerBlockEntity(Capabilities.Fluid.BLOCK,ForgeRunes.ENTITY.get(),(be,side)->be.kind()==ForgeRune.EXTRACTION||be.kind()==ForgeRune.INPUT?new ExperiencePort(be):null);
    }
    static final class ExperiencePort implements ResourceHandler<FluidResource> {
        final ForgeRuneBlockEntity rune;
        ExperiencePort(ForgeRuneBlockEntity rune){this.rune=rune;}
        private final SnapshotJournal<int[]> journal=new SnapshotJournal<>(){
            @Override protected int[] createSnapshot(){return rune.core().transferSnapshot();}
            @Override protected void revertToSnapshot(int[] s){var be=rune.core();if(be!=null)be.restoreTransfer(s);}
            @Override protected void onRootCommit(int[] s){var be=rune.core();if(be!=null)be.sync();}
        };
        public int size(){return 1;}
        public FluidResource getResource(int i){return getAmountAsInt(i)>0?FluidResource.of(XP.get()):FluidResource.EMPTY;}
        public int getAmountAsInt(int i){var be=rune.core();return be==null?0:be.storedExperience();}
        public int getCapacityAsInt(int i,FluidResource r){var be=rune.core();return be==null?0:be.experienceCapacity();}
        public boolean isValid(int i,FluidResource r){return !r.isEmpty()&&r.getFluid().defaultFluidState().is(XP_TAG)&&r.isComponentsPatchEmpty();}
        public int insert(int i,FluidResource r,int n,TransactionContext tx){
            var be=rune.core();if(i!=0||be==null||rune.kind()!=ForgeRune.INPUT||n<=0||!isValid(i,r))return 0;
            int amount=be.insertExperience(be.transferBudget(true,n,true),true);if(amount==0)return 0;
            journal.updateSnapshots(tx);be.transferBudget(true,amount,false);return be.insertExperience(amount,false);
        }
        public int extract(int i,FluidResource r,int n,TransactionContext tx){
            var be=rune.core();if(i!=0||be==null||rune.kind()!=ForgeRune.EXTRACTION||n<=0||!r.equals(FluidResource.of(XP.get())))return 0;
            int amount=be.extractExperience(be.transferBudget(false,n,true),true);if(amount==0)return 0;
            journal.updateSnapshots(tx);be.transferBudget(false,amount,false);return be.extractExperience(amount,false);
        }
    }
    private ForgeRuneAutomation(){}
}
