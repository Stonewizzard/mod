package com.example.examplemod.client;

import com.example.examplemod.FieldGuideNavigation;
import com.example.examplemod.GuideDesign;
import com.example.examplemod.ExampleMod;
import com.google.gson.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Development-only observation/control bridge. Input and screenshots come from the OS. */
public final class ClientVisualQa {
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private static final Path DIRECTORY=Path.of(System.getProperty("examplemod.clientQaDirectory","build/client-qa"));
    private static FieldGuideNavigation navigation;
    private static boolean opened;
    private static volatile net.minecraft.core.BlockPos forgePosition;
    private static net.minecraft.core.BlockPos generatedForgePosition;private static boolean probeForgeWorldgen;
    private static volatile net.minecraft.core.BlockPos anvilPosition;
    private static volatile int feedCruncherId=-1;
    private static volatile net.minecraft.core.BlockPos assetsAnvil;
    private static long sequence,frames,lastWrite;
    private static long commandId=-1;

    public static void tick(ClientTickEvent.Post event){
        if(!Boolean.getBoolean("examplemod.clientVisualQa"))return;
        var minecraft=Minecraft.getInstance();
        try{
            // 26.3 item components are world-bound: do not render item icons at the title screen.
            if(!opened&&minecraft.level!=null&&minecraft.player!=null&&minecraft.gui.screen()==null){open(minecraft);opened=true;}
            if(minecraft.gui.screen()==null)writeState(minecraft,null);
            Path commands=DIRECTORY.resolve("command.json");
            if(Files.isRegularFile(commands)){
                var command=JsonParser.parseString(Files.readString(commands)).getAsJsonObject();long id=command.get("id").getAsLong();
                if(id!=commandId){
                    commandId=id;
                    switch(command.get("action").getAsString()){
                        case "open_book"->{navigation=new FieldGuideNavigation();minecraft.gui.setScreen(new FieldGuideScreen(navigation,command.has("design")?GuideDesign.valueOf(command.get("design").getAsString()):GuideDesign.MANUAL));}
                        case "seed_books"->{
                            var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();
                            server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);int slot=0;for(var design:GuideDesign.values())player.getInventory().setItem(slot++,new net.minecraft.world.item.ItemStack(com.example.examplemod.FieldGuideContent.book(design).get()));player.inventoryMenu.broadcastChanges();});
                        }
                        case "forge_fixture"->{
                            var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();
                            server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var level=player.level();
                                forgePosition=new net.minecraft.core.BlockPos(12,player.blockPosition().getY(),14);
                                for(int x=-3;x<=3;x++)for(int z=-5;z<=3;z++)for(int y=0;y<=4;y++)level.setBlock(forgePosition.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                                level.setBlock(forgePosition,com.example.examplemod.AncientForgeContent.CORE.get().defaultBlockState(),3);
                                for(var part:com.example.examplemod.RuinedForgeStructure.PARTS)level.setBlock(part.at(forgePosition,net.minecraft.core.Direction.NORTH),com.example.examplemod.AncientForgeContent.BRICKS.get().defaultBlockState(),3);
                                var forge=(com.example.examplemod.ReconstructionTableBlockEntity)level.getBlockEntity(forgePosition);forge.pattern(1);
                                var finds=new com.example.examplemod.ArtifactProfile.Find[]{com.example.examplemod.ArtifactProfile.Find.PICKAXE_LEFT,com.example.examplemod.ArtifactProfile.Find.PICKAXE_BRIDGE,com.example.examplemod.ArtifactProfile.Find.PICKAXE_RIGHT,com.example.examplemod.ArtifactProfile.Find.TOOL_CONNECTOR,com.example.examplemod.ArtifactProfile.Find.TOOL_HANDLE};
                                var legacy=new java.util.ArrayList<net.minecraft.world.item.ItemStack>();for(int i=0;i<5;i++){var item=com.example.examplemod.ArchaeologyContent.reward(new com.example.examplemod.ExcavationStudy(new com.example.examplemod.ArtifactProfile(771+i,1,com.example.examplemod.ArtifactProfile.Matrix.SHALE,finds[i])));forge.setItem(i,item);legacy.add(item.copy());}
                                // Saved inventory from before forge assembly was removed; no assembly action is invoked.
                                forge.setItem(5,com.example.examplemod.ArtifactQuality.assemble(com.example.examplemod.ReconstructionPattern.PICKAXE,legacy));
                                player.getInventory().clearContent();player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.getAbilities().invulnerable=true;player.inventoryMenu.broadcastChanges();
                                player.connection.teleport(12.5,forgePosition.getY(),6,0,5);
                            });
                        }
                        case "forge_view","forge_damage","forge_repair","forge_obstruct","forge_strip_frame","forge_part_view"->{
                            var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();var action=command.get("action").getAsString();boolean close=command.has("close")&&command.get("close").getAsBoolean();
                            server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var level=player.level();
                                if(action.equals("forge_damage"))level.setBlock(com.example.examplemod.RuinedForgeStructure.PARTS.getLast().at(forgePosition,net.minecraft.core.Direction.NORTH),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                                if(action.equals("forge_strip_frame"))for(var part:com.example.examplemod.RuinedForgeStructure.PARTS)level.setBlock(part.at(forgePosition,net.minecraft.core.Direction.NORTH),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                                if(action.equals("forge_obstruct"))level.setBlock(com.example.examplemod.RuinedForgeStructure.PARTS.getLast().at(forgePosition,net.minecraft.core.Direction.NORTH),net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState(),3);
                                if(action.equals("forge_part_view"))player.connection.teleport(11.5,forgePosition.getY(),10,0,14.5F);
                                if(action.equals("forge_repair"))for(var part:com.example.examplemod.RuinedForgeStructure.PARTS)level.setBlock(part.at(forgePosition,net.minecraft.core.Direction.NORTH),com.example.examplemod.AncientForgeContent.BRICKS.get().defaultBlockState(),3);
                                if(action.equals("forge_view")){if(close)player.connection.teleport(12.5,forgePosition.getY(),11.5,0,25);else player.connection.teleport(9.5,forgePosition.getY(),8,-29,0);}
                            });
                        }
                        case "melt_inventory"->{var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();server.execute(()->{var p=server.getPlayerList().getPlayer(uuid);p.getInventory().setItem(9,p.getInventory().getItem(0));p.getInventory().setItem(0,com.example.examplemod.ArchaeologyContent.specimen(445566,2));p.containerMenu.broadcastChanges();});}
                        case "feed_fixture"->{var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var l=player.level();var p=forgePosition.offset(12,0,0);
                            for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++){l.setBlock(p.offset(x,-1,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);for(int y=0;y<=3;y++)l.setBlock(p.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);}
                            var mob=com.example.examplemod.ExampleMod.CRUNCHER.get().create(l,net.minecraft.world.entity.EntitySpawnReason.COMMAND);mob.snapTo(p.getX()+.5,p.getY(),p.getZ()+.5,180,0);mob.setNoAi(true);l.addFreshEntity(mob);feedCruncherId=mob.getId();
                            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.getAbilities().invulnerable=true;player.getInventory().setItem(12,player.getInventory().getItem(0));player.getInventory().setItem(0,com.example.examplemod.ArchaeologyContent.specimen(778899,0));player.inventoryMenu.broadcastChanges();player.connection.teleport(p.getX()+.5,p.getY(),p.getZ()-3,0,8);
                        });}
                        case "crystal_fixture","crystal_art","crystal_offhand","crystal_energized_offhand","crystal_drop_view"->{
                            var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();var action=command.get("action").getAsString();
                            server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var l=player.level();
                                if(action.equals("crystal_fixture")){
                                    var forge=(com.example.examplemod.ReconstructionTableBlockEntity)l.getBlockEntity(forgePosition);forge.clearContent();com.example.examplemod.ForgeAssembly.form(l,forgePosition);
                                    player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.getAbilities().invulnerable=true;player.getInventory().clearContent();
                                    player.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.CRYSTAL.get(),13));
                                    player.getInventory().setItem(1,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_BLOCK));
                                    player.getInventory().setItem(2,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT));
                                    player.getInventory().setItem(3,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FLINT_AND_STEEL));
                                }else{
                                    var p=forgePosition.offset(10,0,0);
                                    if(action.equals("crystal_art")){
                                        for(int x=-3;x<=7;x++)for(int z=-7;z<=2;z++)for(int y=-1;y<=3;y++)l.setBlock(p.offset(x,y,z),(y==-1?net.minecraft.world.level.block.Blocks.STONE:net.minecraft.world.level.block.Blocks.AIR).defaultBlockState(),3);
                                        for(int x=0;x<2;x++)for(int y=0;y<2;y++){l.setBlock(p.offset(x,y,0),com.example.examplemod.AncientAnvilContent.CRYSTAL_BLOCK.get().defaultBlockState(),3);l.setBlock(p.offset(x+3,y,0),com.example.examplemod.AncientAnvilContent.ENERGIZED_CRYSTAL_BLOCK.get().defaultBlockState(),3);}
                                        for(int y=0;y<2;y++){l.setBlock(p.offset(-2,y,0),net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState(),3);l.setBlock(p.offset(6,y,0),net.minecraft.world.level.block.Blocks.AMETHYST_BLOCK.defaultBlockState(),3);}
                                        player.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.CRYSTAL.get(),2));
                                        player.getInventory().setItem(1,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT));
                                        player.getInventory().setItem(2,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.CRYSTAL_BLOCK_ITEM.get()));
                                        player.getInventory().setItem(4,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.ENERGIZED_CRYSTAL_BLOCK_ITEM.get()));
                                        player.getInventory().setItem(5,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.ENERGIZED_CRYSTAL.get(),2));
                                        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT));
                                    }else if(action.equals("crystal_energized_offhand")){
                                        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.ENERGIZED_CRYSTAL.get()));
                                    }else if(action.equals("crystal_offhand")){
                                        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.CRYSTAL.get()));
                                    }
                                    player.connection.teleport(p.getX()+2.5,p.getY(),p.getZ()-6.5,0,action.equals("crystal_drop_view")?32:5);
                                }
                                player.inventoryMenu.broadcastChanges();
                            });
                        }
                        case "alloy_inventory"->{var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);player.getInventory().clearContent();player.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.SCRAP_ITEM.get()));player.getInventory().setItem(1,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT));player.getInventory().setItem(2,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT,2));player.getInventory().setItem(3,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FLINT_AND_STEEL));player.inventoryMenu.broadcastChanges();});}
                        case "tier6_fixture","tier6_clear_caps","tier6_view","tier6_crystal_view","tier6_recipe","tier6_blast_view","tier6_lens_red","tier6_lens_mixed","tier6_break_crystal"->{
                            var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();var action=command.get("action").getAsString();int index=command.has("index")?command.get("index").getAsInt():0;
                            server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var l=player.level();var front=net.minecraft.core.Direction.NORTH;
                                if(action.equals("tier6_fixture")){
                                    anvilPosition=forgePosition.offset(40,8,0);
                                    for(int x=-19;x<=19;x++)for(int z=-19;z<=19;z++)for(int y=-5;y<=19;y++)l.setBlock(anvilPosition.offset(x,y,z),(y==-5?net.minecraft.world.level.block.Blocks.STONE:net.minecraft.world.level.block.Blocks.AIR).defaultBlockState(),3);
                                    for(int x=-7;x<=7;x++)for(int z=-7;z<=7;z++)l.setBlock(anvilPosition.offset(x,-2,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
                                    com.example.examplemod.AnvilCanopyTests.prepare(l,anvilPosition,front);com.example.examplemod.AnvilStageTests.fill(l,anvilPosition,front,com.example.examplemod.AnvilStages.all().get(2));
                                    player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.getAbilities().invulnerable=true;player.getInventory().clearContent();
                                    player.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.IRON_FORGE_HAMMER.get()));
                                    player.getInventory().setItem(1,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.ENERGIZED_CRYSTAL_BLOCK_ITEM.get(),16));
                                    player.getInventory().setItem(2,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OBSIDIAN,4));player.getInventory().setItem(3,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.END_CRYSTAL,4));
                                    player.getInventory().setItem(4,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LIGHT_BLUE_STAINED_GLASS,25));player.getInventory().setItem(5,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.RED_STAINED_GLASS,5));player.inventoryMenu.broadcastChanges();
                                }else if(action.equals("tier6_clear_caps")){
                                    for(var req:com.example.examplemod.AnvilStages.all().getLast().blocks())if(!req.tag().equals(com.example.examplemod.AnvilTierSix.GLASS))l.setBlock(req.at(anvilPosition,front),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                                }else if(action.equals("tier6_crystal_view")){
                                    var req=com.example.examplemod.AnvilTierSix.requirements(com.example.examplemod.AnvilStages.all(),com.example.examplemod.AnvilTierSix.OBSIDIAN).get(index);var cap=req.at(anvilPosition,front);
                                    player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.getAbilities().mayfly=true;player.getAbilities().flying=true;player.getAbilities().invulnerable=true;player.onUpdateAbilities();player.connection.teleport(cap.getX()+.5,cap.getY()+1,cap.getZ()-2.5,0,28.4F);
                                }else if(action.equals("tier6_recipe")){
                                    var be=(com.example.examplemod.AncientAnvilBlockEntity)l.getBlockEntity(anvilPosition);be.takeOutput();com.example.examplemod.AnvilTierSixTests.recipe(be);
                                }else if(action.equals("tier6_lens_red")||action.equals("tier6_lens_mixed")){
                                    var reqs=com.example.examplemod.AnvilTierSix.requirements(com.example.examplemod.AnvilStages.all(),com.example.examplemod.AnvilTierSix.GLASS);
                                    if(action.equals("tier6_lens_mixed"))l.setBlock(reqs.getFirst().at(anvilPosition,front),net.minecraft.world.level.block.Blocks.BLUE_STAINED_GLASS.defaultBlockState(),3);
                                    else for(var req:reqs)l.setBlock(req.at(anvilPosition,front),net.minecraft.world.level.block.Blocks.RED_STAINED_GLASS.defaultBlockState(),3);
                                }else if(action.equals("tier6_break_crystal")){
                                    var req=com.example.examplemod.AnvilTierSix.requirements(com.example.examplemod.AnvilStages.all(),com.example.examplemod.AnvilTierSix.OBSIDIAN).getFirst();var crystal=com.example.examplemod.AnvilTierSix.crystal(l,req.at(anvilPosition,front));if(crystal!=null)crystal.discard();
                                }else{
                                    player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();
                                    if(action.equals("tier6_blast_view"))player.connection.teleport(anvilPosition.getX()-26,anvilPosition.getY()+10,anvilPosition.getZ()-30,-41,-10);
                                    else player.connection.teleport(anvilPosition.getX()-28,anvilPosition.getY()+18,anvilPosition.getZ()-34,-40,5);
                                }
                                com.example.examplemod.AncientAnvilBlockEntity.refresh(l,anvilPosition,l.getBlockState(anvilPosition));
                            });
                        }
                        case "canopy_fixture","canopy_overview","canopy_oblique","canopy_night","canopy_break_node"->{
                            var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();var action=command.get("action").getAsString();
                            server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var l=player.level();var front=net.minecraft.core.Direction.NORTH;
                                if(action.equals("canopy_fixture")){
                                    anvilPosition=forgePosition.offset(40,8,0);
                                    for(int x=-18;x<=18;x++)for(int z=-18;z<=18;z++)for(int y=-5;y<=20;y++)l.setBlock(anvilPosition.offset(x,y,z),(y==-5?net.minecraft.world.level.block.Blocks.STONE:net.minecraft.world.level.block.Blocks.AIR).defaultBlockState(),3);
                                    for(int x=-7;x<=7;x++)for(int z=-7;z<=7;z++)l.setBlock(anvilPosition.offset(x,-2,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
                                    com.example.examplemod.AnvilCanopyTests.prepare(l,anvilPosition,front);
                                    player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.getAbilities().invulnerable=true;player.getInventory().clearContent();
                                    player.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.IRON_FORGE_HAMMER.get()));
                                    for(int i=1;i<=3;i++)player.getInventory().setItem(i,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.RUNESTONE_ITEM.get(),i==3?28:64));
                                    player.getInventory().setItem(7,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.CANOPY_KEYSTONE_ITEM.get(),16));
                                    player.getInventory().setItem(8,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));player.getInventory().setItem(9,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LAVA_BUCKET));player.inventoryMenu.broadcastChanges();
                                }else if(action.equals("canopy_break_node")){
                                    var req=com.example.examplemod.AnvilStages.all().get(2).blocks().stream().filter(r->r.tag().equals(com.example.examplemod.AnvilStages.KEYSTONES)).findFirst().orElseThrow();l.setBlock(req.at(anvilPosition,front),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);com.example.examplemod.AncientAnvilBlockEntity.refresh(l,anvilPosition,l.getBlockState(anvilPosition));
                                }else if(action.equals("canopy_night"))server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"time set midnight");
                                else{
                                    player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();
                                    if(action.equals("canopy_overview"))player.connection.teleport(anvilPosition.getX()+.5,anvilPosition.getY()+42,anvilPosition.getZ()+.5,0,90);
                                    else player.connection.teleport(anvilPosition.getX()-28,anvilPosition.getY()+12,anvilPosition.getZ()-28,-45,0);
                                }
                            });
                        }
                        case "anvil_auto_fixture","anvil_manual_fluids","anvil_auto_overview","anvil_auto_close","anvil_lowered_height_view"->{
                            var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();var action=command.get("action").getAsString();
                            server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var l=player.level();var front=net.minecraft.core.Direction.NORTH;
                                if(action.equals("anvil_auto_fixture")){
                                    for(int x=-18;x<=18;x++)for(int z=-18;z<=18;z++)if(Math.max(Math.abs(x),Math.abs(z))>7)for(int y=-5;y<=6;y++)l.setBlock(anvilPosition.offset(x,y,z),(y==-5?net.minecraft.world.level.block.Blocks.STONE:net.minecraft.world.level.block.Blocks.AIR).defaultBlockState(),3);
                                    for(var at:com.example.examplemod.AnvilBasins.frame(anvilPosition,front))l.setBlock(at,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                                    for(var side:com.example.examplemod.AnvilBasins.Side.values())for(var at:com.example.examplemod.AnvilBasins.pool(anvilPosition,front,side))l.setBlock(at,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                                    l.setBlock(anvilPosition.east(2),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);l.setBlock(anvilPosition.east(2).above(),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                                    player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.getAbilities().invulnerable=true;player.getInventory().clearContent();
                                    player.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.IRON_FORGE_HAMMER.get()));
                                    for(int i=1;i<=6;i++)player.getInventory().setItem(i,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.RUNESTONE_ITEM.get(),i==6?19:64));
                                    player.getInventory().setItem(7,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_BLOCK,20));
                                    player.getInventory().setItem(8,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));player.getInventory().setItem(9,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LAVA_BUCKET));player.inventoryMenu.broadcastChanges();
                                }else if(action.equals("anvil_manual_fluids")){
                                    for(var side:com.example.examplemod.AnvilBasins.Side.values())for(var at:com.example.examplemod.AnvilBasins.pool(anvilPosition,front,side))l.setBlock(at,net.minecraft.world.level.block.Blocks.WATER.defaultBlockState(),3);
                                }else if(action.equals("anvil_lowered_height_view")){player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.connection.teleport(anvilPosition.getX()-18,anvilPosition.getY()+7,anvilPosition.getZ()-19,-43,18);}
                                else if(action.equals("anvil_auto_close")){player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.getAbilities().invulnerable=true;player.connection.teleport(anvilPosition.getX()+.5,anvilPosition.getY()+2.5,anvilPosition.getZ()+.5,0,90);}
                                else{player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);int height=l.getBlockState(anvilPosition).getValue(com.example.examplemod.AncientAnvilBlock.TIER)>=3?32:22;player.connection.teleport(anvilPosition.getX()+.5,anvilPosition.getY()+height,anvilPosition.getZ()-10,0,70);}
                                com.example.examplemod.AncientAnvilBlockEntity.refresh(l,anvilPosition,l.getBlockState(anvilPosition));
                            });
                        }
                        case "anvil_tier3","anvil_tier4","anvil_outer_pillar_break"->{
                            var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();var action=command.get("action").getAsString();
                            server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var l=player.level();
                                if(action.equals("anvil_outer_pillar_break"))l.setBlock(anvilPosition.offset(16,3,0),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                                else {
                                    int target=action.equals("anvil_tier4")?4:3;
                                    if(target==3)for(int x=-18;x<=18;x++)for(int z=-18;z<=18;z++)if(Math.max(Math.abs(x),Math.abs(z))>7)for(int y=-5;y<=6;y++)l.setBlock(anvilPosition.offset(x,y,z),(y==-5?net.minecraft.world.level.block.Blocks.STONE:net.minecraft.world.level.block.Blocks.AIR).defaultBlockState(),3);
                                    for(var stage:com.example.examplemod.AnvilStages.all())if(stage.tier()<=target)for(var req:stage.blocks())l.setBlock(req.at(anvilPosition,net.minecraft.core.Direction.NORTH),(req.tag().equals(com.example.examplemod.AnvilStages.KEYSTONES)?com.example.examplemod.AncientAnvilContent.CANOPY_KEYSTONE.get():req.tag().equals(com.example.examplemod.AnvilStages.CAPS)?net.minecraft.world.level.block.Blocks.IRON_BLOCK:com.example.examplemod.AncientAnvilContent.RUNESTONE.get()).defaultBlockState(),3);
                                    player.connection.teleport(anvilPosition.getX()+.5,anvilPosition.getY()+(target==4?32:20),anvilPosition.getZ()-(target==4?12:8),0,70);
                                }
                                com.example.examplemod.AncientAnvilBlockEntity.refresh(l,anvilPosition,l.getBlockState(anvilPosition));
                            });
                        }
                        case "anvil_tier2_fixture","anvil_tier2_drain"->{
                            var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();var action=command.get("action").getAsString();
                            server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var l=player.level();
                                if(action.equals("anvil_tier2_fixture")){
                                    anvilPosition=forgePosition.offset(18,0,0);
                                    for(int x=-7;x<=7;x++)for(int z=-7;z<=7;z++)for(int y=-2;y<=4;y++)l.setBlock(anvilPosition.offset(x,y,z),(y==-2?net.minecraft.world.level.block.Blocks.STONE:net.minecraft.world.level.block.Blocks.AIR).defaultBlockState(),3);
                                    var front=net.minecraft.core.Direction.NORTH;
                                    for(var at:com.example.examplemod.AncientAnvilStructure.foundation(anvilPosition,front))l.setBlock(at,com.example.examplemod.AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
                                    for(var at:com.example.examplemod.AnvilBasins.frame(anvilPosition,front))l.setBlock(at,com.example.examplemod.AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
                                    for(var side:com.example.examplemod.AnvilBasins.Side.values())for(var at:com.example.examplemod.AnvilBasins.pool(anvilPosition,front,side))l.setBlock(at,(side==com.example.examplemod.AnvilBasins.Side.FRONT||side==com.example.examplemod.AnvilBasins.Side.LEFT?net.minecraft.world.level.block.Blocks.WATER:net.minecraft.world.level.block.Blocks.LAVA).defaultBlockState(),3);
                                    l.setBlock(anvilPosition,com.example.examplemod.AncientAnvilContent.ANVIL.get().defaultBlockState(),3);
                                    com.example.examplemod.AncientAnvilBlockEntity.refresh(l,anvilPosition,l.getBlockState(anvilPosition));
                                    // Place blocks in the formerly invisible, empty edge cells.
                                    l.setBlock(anvilPosition.east(2),com.example.examplemod.AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
                                    l.setBlock(anvilPosition.east(2).above(),com.example.examplemod.AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
                                    player.connection.teleport(anvilPosition.getX()+.5,anvilPosition.getY()+12,anvilPosition.getZ()-6,0,65);
                                }else{
                                    var at=com.example.examplemod.AnvilBasins.pool(anvilPosition,net.minecraft.core.Direction.NORTH,com.example.examplemod.AnvilBasins.Side.FRONT).getFirst();
                                    l.setBlock(at,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
                                    com.example.examplemod.AncientAnvilBlockEntity.refresh(l,anvilPosition,l.getBlockState(anvilPosition));
                                }
                            });
                        }
                        case "anvil_guide_build_fixture","anvil_clear_obstruction","anvil_metal_foundation","anvil_fixture","anvil_view","anvil_foundation","anvil_expose","anvil_damage"->{
                            var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();var action=command.get("action").getAsString();boolean close=command.has("close")&&command.get("close").getAsBoolean();
                            server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var l=player.level();if(action.equals("anvil_fixture")){
                                anvilPosition=forgePosition.offset(7,0,0);
                                for(int x=-2;x<=2;x++)for(int z=-3;z<=3;z++)for(int y=0;y<=3;y++)l.setBlock(anvilPosition.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                                for(var at:com.example.examplemod.AncientAnvilStructure.foundation(anvilPosition,net.minecraft.core.Direction.NORTH))l.setBlock(at,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
                                player.getInventory().setItem(12,player.getInventory().getItem(1));player.getInventory().setItem(1,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.ANVIL_ITEM.get()));player.inventoryMenu.broadcastChanges();
                                player.connection.teleport(anvilPosition.getX()+.5,anvilPosition.getY(),anvilPosition.getZ()-3,0,25);
                            }
                            if(action.equals("anvil_expose"))for(var at:com.example.examplemod.AncientAnvilStructure.foundation(anvilPosition,net.minecraft.core.Direction.NORTH))if(!at.equals(anvilPosition.below()))l.setBlock(at,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                            if(action.equals("anvil_foundation")||action.equals("anvil_metal_foundation"))for(var at:com.example.examplemod.AncientAnvilStructure.foundation(anvilPosition,net.minecraft.core.Direction.NORTH))l.setBlock(at,(action.equals("anvil_foundation")?com.example.examplemod.AncientAnvilContent.RUNESTONE.get():net.minecraft.world.level.block.Blocks.IRON_BLOCK).defaultBlockState(),3);
                            if(action.equals("anvil_guide_build_fixture")){
                                for(var at:com.example.examplemod.AncientAnvilStructure.foundation(anvilPosition,net.minecraft.core.Direction.NORTH))l.setBlock(at,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                                l.setBlock(anvilPosition.offset(2,-1,2),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
                                player.getInventory().clearContent();player.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.RUNESTONE_ITEM.get(),25));player.getInventory().setItem(2,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.IRON_FORGE_HAMMER.get()));player.inventoryMenu.broadcastChanges();
                                var be=(com.example.examplemod.AncientAnvilBlockEntity)l.getBlockEntity(anvilPosition);be.placeInput(com.example.examplemod.AnvilSlot.CENTER,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT));
                            }
                            if(action.equals("anvil_clear_obstruction"))l.setBlock(anvilPosition.offset(2,-1,2),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                            if(action.equals("anvil_damage"))l.setBlock(anvilPosition.offset(1,-1,1),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                            if(action.equals("anvil_view")){if(close)player.connection.teleport(anvilPosition.getX()+.5,anvilPosition.getY(),anvilPosition.getZ()-2.5,0,17);else player.connection.teleport(anvilPosition.getX()-1.5,anvilPosition.getY(),anvilPosition.getZ()-2.5,-34,16);}
                            });
                        }
                        case "smithing_fixture","smithing_aim","smithing_overview"->{
                            var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();var action=command.get("action").getAsString();var slot=command.has("slot")?com.example.examplemod.AnvilSlot.byId(command.get("slot").getAsString()):com.example.examplemod.AnvilSlot.CENTER;
                            server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var l=player.level();
                                if(action.equals("smithing_fixture")){
                                    player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.getInventory().clearContent();player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,net.minecraft.world.item.ItemStack.EMPTY);
                                    player.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DEEPSLATE,8));player.getInventory().setItem(1,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT,2));player.getInventory().setItem(2,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.IRON_FORGE_HAMMER.get()));player.getInventory().setItem(3,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLED_DEEPSLATE,8));
                                    for(var at:com.example.examplemod.AncientAnvilStructure.foundation(anvilPosition,net.minecraft.core.Direction.NORTH))l.setBlock(at,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);com.example.examplemod.AncientAnvilBlockEntity.refresh(l,anvilPosition,l.getBlockState(anvilPosition));
                                    l.setBlock(anvilPosition.offset(-2,0,0),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);player.inventoryMenu.broadcastChanges();
                                }
                                double x=anvilPosition.getX()-1.5,y=anvilPosition.getY()+1,z=anvilPosition.getZ()+.8;
                                var target=slot.world(anvilPosition,net.minecraft.core.Direction.NORTH);double dx=target.x-x,dy=target.y-(y+player.getEyeHeight()),dz=target.z-z;
                                float yaw=(float)Math.toDegrees(Math.atan2(-dx,dz)),pitch=(float)-Math.toDegrees(Math.atan2(dy,Math.sqrt(dx*dx+dz*dz)));
                                if(action.equals("smithing_overview")){
                                    player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();
                                    x=anvilPosition.getX()+.5;y=anvilPosition.getY()+3.5;z=anvilPosition.getZ()+.5;yaw=180;pitch=90;
                                }
                                player.connection.teleport(x,y,z,yaw,pitch);
                            });
                        }
                        case "anvil_sample_item"->{
                            var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();var design=com.example.examplemod.AncientAnvilDesign.valueOf(command.get("design").getAsString().toUpperCase(java.util.Locale.ROOT));
                            server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var l=player.level();
                                l.destroyBlock(assetsAnvil,false);
                                for(int x=-1;x<=2;x++)for(int z=-2;z<=1;z++){
                                    l.setBlock(assetsAnvil.offset(x,-1,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
                                    for(int y=0;y<=3;y++)l.setBlock(assetsAnvil.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                                }
                                for(var mob:l.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,new net.minecraft.world.phys.AABB(assetsAnvil).inflate(8)))mob.discard();
                                player.getInventory().clearContent();player.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.item(design)));
                                player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,net.minecraft.world.item.ItemStack.EMPTY);player.inventoryMenu.broadcastChanges();
                                player.connection.teleport(assetsAnvil.getX()+.5,assetsAnvil.getY(),assetsAnvil.getZ()-3,0,25);
                            });
                        }
                        case "assets_anvil_centered","assets_anvil_heel","assets_anvil_under","assets_scene","assets_anvil_view","assets_anvil_detail","assets_anvil_profile","assets_anvil_rear","assets_offhand","assets_right_hand","assets_drop_view"->{
                            var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();var action=command.get("action").getAsString();var facing=command.has("facing")?net.minecraft.core.Direction.valueOf(command.get("facing").getAsString()):net.minecraft.core.Direction.NORTH;
                            server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var l=player.level();
                                if(action.equals("assets_scene")){
                                    assetsAnvil=new net.minecraft.core.BlockPos(40,forgePosition.getY(),12);player.connection.teleport(40.5,assetsAnvil.getY(),5,0,0);
                                    for(int x=-7;x<=7;x++)for(int z=-6;z<=5;z++){l.setBlock(assetsAnvil.offset(x,-1,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);for(int y=0;y<=4;y++)l.setBlock(assetsAnvil.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);}
                                    for(var mob:l.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,new net.minecraft.world.phys.AABB(assetsAnvil).inflate(12)))mob.discard();
                                    l.setBlock(assetsAnvil.west(4),net.minecraft.world.level.block.Blocks.ANVIL.defaultBlockState(),3);
                                    var wall=new net.minecraft.world.level.block.Block[]{net.minecraft.world.level.block.Blocks.STONE_BRICKS,net.minecraft.world.level.block.Blocks.DEEPSLATE_BRICKS,com.example.examplemod.AncientForgeContent.BRICKS.get(),com.example.examplemod.AncientAnvilContent.METAL.get()};
                                    for(int x=0;x<4;x++)for(int y=0;y<2;y++)l.setBlock(assetsAnvil.offset(x+3,y,1),wall[x].defaultBlockState(),3);
                                    player.getInventory().clearContent();player.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT));player.getInventory().setItem(1,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT));player.getInventory().setItem(2,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.INGOT.get()));player.getInventory().setItem(3,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.SCRAP_ITEM.get()));player.getInventory().setItem(4,com.example.examplemod.ArchaeologyContent.specimen(7788,0));player.getInventory().setItem(5,new net.minecraft.world.item.ItemStack(com.example.examplemod.FieldGuideContent.book(com.example.examplemod.GuideDesign.MANUAL).get()));player.getInventory().setItem(6,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.ANVIL_ITEM.get()));player.getInventory().setItem(7,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ANVIL));
                                    player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT));
                                }
                                if(action.equals("assets_scene")||action.equals("assets_anvil_view")){
                                    l.setBlock(assetsAnvil,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);l.setBlock(assetsAnvil,com.example.examplemod.AncientAnvilContent.ANVIL.get().defaultBlockState().setValue(com.example.examplemod.AncientAnvilBlock.FACING,facing),3);com.example.examplemod.AncientAnvilBlockEntity.refresh(l,assetsAnvil,l.getBlockState(assetsAnvil));
                                    var view=assetsAnvil.relative(facing,5).relative(facing.getCounterClockWise(),3);float yaw=(float)Math.toDegrees(Math.atan2(view.getX()-assetsAnvil.getX(),assetsAnvil.getZ()-view.getZ()));player.connection.teleport(view.getX()+.5,assetsAnvil.getY(),view.getZ()+.5,yaw,0);
                                }
                                if(action.equals("assets_anvil_centered")){
                                    for(var at:com.example.examplemod.AncientAnvilStructure.foundation(assetsAnvil,net.minecraft.core.Direction.NORTH))l.setBlock(at,com.example.examplemod.AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
                                    com.example.examplemod.AncientAnvilBlockEntity.refresh(l,assetsAnvil,l.getBlockState(assetsAnvil));player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();player.connection.teleport(assetsAnvil.getX()+.5,assetsAnvil.getY()+6,assetsAnvil.getZ()+.5,180,90);
                                }
                                if(action.equals("assets_anvil_heel")){
                                    l.setBlock(assetsAnvil.offset(1,0,4),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);player.connection.teleport(assetsAnvil.getX()+.6,assetsAnvil.getY()+1,assetsAnvil.getZ()+4.5,180,14);
                                }
                                if(action.equals("assets_anvil_under")){
                                    var view=assetsAnvil.north(3);for(var at:java.util.List.of(view,view.east())){l.setBlock(at.below(),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);l.setBlock(at.below(2),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);}
                                    player.connection.teleport(assetsAnvil.getX()+.5,assetsAnvil.getY()-1,assetsAnvil.getZ()-2.5,0,-38);
                                }
                                if(action.equals("assets_anvil_detail")){
                                    var front=l.getBlockState(assetsAnvil).getValue(com.example.examplemod.AncientAnvilBlock.FACING);
                                    var view=assetsAnvil.relative(front,3).relative(front.getCounterClockWise(),2);
                                    l.setBlock(view,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
                                    float yaw=(float)Math.toDegrees(Math.atan2(view.getX()-assetsAnvil.getX(),assetsAnvil.getZ()-view.getZ()));
                                    player.connection.teleport(view.getX()+.5,assetsAnvil.getY()+1,view.getZ()+.5,yaw,19);
                                }
                                if(action.equals("assets_anvil_profile")||action.equals("assets_anvil_rear")){
                                    var front=l.getBlockState(assetsAnvil).getValue(com.example.examplemod.AncientAnvilBlock.FACING);
                                    boolean rear=action.equals("assets_anvil_rear");
                                    var view=rear?assetsAnvil.relative(front.getOpposite(),4).relative(front.getClockWise(),3):assetsAnvil.relative(front,1).relative(front.getClockWise(),5);
                                    if(rear)l.setBlock(view,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
                                    float yaw=(float)Math.toDegrees(Math.atan2(view.getX()-assetsAnvil.getX(),assetsAnvil.getZ()-view.getZ()));
                                    player.connection.teleport(view.getX()+.5,assetsAnvil.getY()+(rear?1:0),view.getZ()+.5,yaw,rear?16:5);
                                }
                                if(action.equals("assets_offhand")){player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.INGOT.get()));}
                                if(action.equals("assets_right_hand")){player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT));player.getInventory().setItem(2,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.INGOT.get()));}
                                if(action.equals("assets_drop_view")){var drops=l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(player.blockPosition()).inflate(8));for(var item:drops)if(item.getItem().is(com.example.examplemod.AncientAnvilContent.INGOT.get())){player.connection.teleport(item.getX(),assetsAnvil.getY(),item.getZ()-2.2,0,40);break;}}
                                player.inventoryMenu.broadcastChanges();
                            });
                        }
                        case "forge_probe_worldgen"->probeForgeWorldgen=true;
                        case "forge_generated_view"->{var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();var found=generatedForgePosition;server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var front=player.level().getBlockState(found).getValue(com.example.examplemod.RuinedAncientForgeBlock.FACING);var viewer=found.relative(front,6).relative(front.getCounterClockWise(),4);float yaw=(float)Math.toDegrees(Math.atan2(viewer.getX()-found.getX(),found.getZ()-viewer.getZ()));player.connection.teleport(viewer.getX()+.5,forgePosition.getY(),viewer.getZ()+.5,yaw,-6);});}
                        case "gui_scale"->{minecraft.options.guiScale().set(command.get("value").getAsInt());minecraft.resizeGui();}
                        case "quit"->minecraft.stop();
                        default->throw new IllegalArgumentException("Unknown visual-QA command");
                    }
                }
            }
        }catch(Exception failure){throw new IllegalStateException("Client visual-QA bridge failed",failure);}
    }
    private static void open(Minecraft minecraft){navigation=new FieldGuideNavigation();minecraft.gui.setScreen(new FieldGuideScreen(navigation));}
    public static void rendered(ScreenEvent.Render.Post event){
        if(!Boolean.getBoolean("examplemod.clientVisualQa"))return;
        var minecraft=Minecraft.getInstance();
        if(minecraft.gui.screen()==event.getScreen()){frames++;writeState(minecraft,event.getScreen());}
    }
    private static void writeState(Minecraft minecraft,Screen screen){
        if(System.currentTimeMillis()-lastWrite<150)return;lastWrite=System.currentTimeMillis();
        try{
            Files.createDirectories(DIRECTORY);var state=new JsonObject();var window=minecraft.getWindow();
            state.addProperty("sequence",++sequence);state.addProperty("frames",frames);state.addProperty("commandId",commandId);
            state.addProperty("screen",screen==null?"InGame":screen.getClass().getSimpleName());state.addProperty("windowWidth",window.getScreenWidth());state.addProperty("windowHeight",window.getScreenHeight());
            state.addProperty("guiWidth",window.getGuiScaledWidth());state.addProperty("guiHeight",window.getGuiScaledHeight());state.addProperty("guiScale",window.getGuiScale());state.addProperty("inWorld",minecraft.level!=null);
            var widgets=new JsonArray();if(screen!=null)for(var child:screen.children())widgets(child,widgets);state.add("widgets",widgets);
            if(screen instanceof FieldGuideScreen book){
                state.addProperty("focus",book.getFocused() instanceof GuideBookButton button?button.control.id():book.getFocused() instanceof net.minecraft.client.gui.components.EditBox?"search":"");
                navigation=book.navigation();state.addProperty("design",book.design().name());state.addProperty("section",navigation.section());state.addProperty("related",book.related());
                state.addProperty("page",navigation.page().name());state.addProperty("query",navigation.query());state.addProperty("articlePage",navigation.articlePage());state.addProperty("listPage",navigation.listPage());
                state.addProperty("selected",navigation.selected()==null?"":navigation.selected().id());state.addProperty("results",navigation.results().size());
                state.addProperty("category",navigation.category()==null?"":navigation.category().name());state.addProperty("group",navigation.group().name());
                var controls=new JsonArray();
                for(var c:book.liveControls()){
                    var b=new JsonObject();b.addProperty("id",c.id());b.addProperty("label",c.label());b.addProperty("enabled",c.enabled());
                    b.addProperty("x",c.x());b.addProperty("y",c.y());b.addProperty("width",c.w());b.addProperty("height",c.h());controls.add(b);
                }
                state.add("controls",controls);state.addProperty("searchX",book.searchX());state.addProperty("searchY",book.searchY());
            }
            if(forgePosition!=null&&minecraft.level!=null){
                state.addProperty("forgeY",forgePosition.getY());
                state.addProperty("forgeDisplayOutput",minecraft.level.getBlockEntity(forgePosition) instanceof com.example.examplemod.ReconstructionTableBlockEntity forge&&!forge.getItem(5).isEmpty());
                if(probeForgeWorldgen&&generatedForgePosition==null){
                    outer:for(int x=8;x<=40;x++)for(int z=24;z<=56;z++)for(int y=forgePosition.getY()-2;y<=forgePosition.getY()+7;y++){
                        var at=new net.minecraft.core.BlockPos(x,y,z);if(minecraft.level.hasChunkAt(at)&&minecraft.level.getBlockState(at).is(com.example.examplemod.AncientForgeContent.CORE.get())){generatedForgePosition=at;break outer;}
                    }
                }
                state.addProperty("generatedForge",generatedForgePosition!=null);
                state.addProperty("forgeFormed",com.example.examplemod.ForgeAssembly.formed(minecraft.level.getBlockState(forgePosition)));
                state.addProperty("forgeGhost",ForgePreview.active(minecraft.level,forgePosition));
                if(minecraft.level.getBlockState(forgePosition).is(com.example.examplemod.AncientForgeContent.CORE.get())){
                    var status=com.example.examplemod.RuinedForgeStructure.inspect(minecraft.level,forgePosition,net.minecraft.core.Direction.NORTH);state.addProperty("forgeMissing",status.missing().size());
                    state.addProperty("forgeOccupied",status.missing().stream().filter(p->!minecraft.level.getBlockState(p.at(forgePosition,net.minecraft.core.Direction.NORTH)).isAir()).count());
                }
                state.addProperty("forgeReady",com.example.examplemod.RuinedForgeStructure.inspect(minecraft.level,forgePosition,net.minecraft.core.Direction.NORTH).complete());
                state.addProperty("aimedAtForge",minecraft.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit&&(hit.getBlockPos().equals(forgePosition)||minecraft.level.getBlockState(hit.getBlockPos()).is(com.example.examplemod.AncientForgeContent.PART.get())&&com.example.examplemod.FormedForgePartBlock.core(hit.getBlockPos(),minecraft.level.getBlockState(hit.getBlockPos())).equals(forgePosition)));
            }
            if(forgePosition!=null&&minecraft.level!=null&&minecraft.level.getBlockEntity(forgePosition) instanceof com.example.examplemod.ReconstructionTableBlockEntity forge){
                state.addProperty("legacyTool",forge.getItem(5).is(com.example.examplemod.ArchaeologyContent.PICKAXE.get()));state.addProperty("storedCount",java.util.stream.IntStream.range(0,6).map(i->forge.getItem(i).isEmpty()?0:1).sum());
                state.addProperty("energizedCrystalInventory",minecraft.player.getInventory().countItem(com.example.examplemod.AncientAnvilContent.ENERGIZED_CRYSTAL.get()));state.addProperty("holdingEnergizedCrystal",minecraft.player.getMainHandItem().is(com.example.examplemod.AncientAnvilContent.ENERGIZED_CRYSTAL.get()));
                state.addProperty("energizedInventory",minecraft.player.getInventory().countItem(com.example.examplemod.AncientAnvilContent.ENERGIZED_CRYSTAL_BLOCK_ITEM.get()));state.addProperty("holdingEnergized",minecraft.player.getMainHandItem().is(com.example.examplemod.AncientAnvilContent.ENERGIZED_CRYSTAL_BLOCK_ITEM.get()));
                state.addProperty("crystalResult",forge.getItem(5).is(com.example.examplemod.AncientAnvilContent.CRYSTAL_BLOCK_ITEM.get())?forge.getItem(5).getCount():0);
                state.addProperty("crystalInventory",minecraft.player.getInventory().countItem(com.example.examplemod.AncientAnvilContent.CRYSTAL.get()));state.addProperty("crystalBlockInventory",minecraft.player.getInventory().countItem(com.example.examplemod.AncientAnvilContent.CRYSTAL_BLOCK_ITEM.get()));
                state.addProperty("alloyReady",forge.process.ready());state.addProperty("alloyCount",forge.getItem(5).is(com.example.examplemod.AncientAnvilContent.INGOT.get())?forge.getItem(5).getCount():0);
                state.addProperty("alloyScrap",forge.getItem(0).getCount());state.addProperty("alloyGold",forge.getItem(1).getCount());state.addProperty("alloyIron",forge.getItem(2).getCount());state.addProperty("melting",com.example.examplemod.AncientAlloy.melting(forge));state.addProperty("meltCount",forge.getItem(5).is(com.example.examplemod.AncientAnvilContent.SCRAP_ITEM.get())?forge.getItem(5).getCount():0);
                state.addProperty("forgeHeating",forge.process.heating());state.addProperty("forgeFinished",forge.process.finished());state.addProperty("forgeRemaining",forge.process.remaining());
                state.addProperty("frontTableAbsent",minecraft.level.getBlockState(forgePosition.north(2)).isAir());
                if(minecraft.player!=null){int ingots=0,recovered=0;for(int i=0;i<36;i++){var item=minecraft.player.getInventory().getItem(i);if(item.is(com.example.examplemod.AncientAnvilContent.INGOT.get()))ingots+=item.getCount();if(item.is(com.example.examplemod.ArchaeologyContent.PICKAXE.get())||com.example.examplemod.ReconstructionContent.pieces().contains(item.getItem()))recovered+=item.getCount();}state.addProperty("ingotInventory",ingots);state.addProperty("recoveredLegacyItems",recovered);state.addProperty("flintDamage",minecraft.player.getInventory().getItem(3).getDamageValue());}
            }
            if(anvilPosition!=null&&minecraft.level!=null){var a=minecraft.level.getBlockState(anvilPosition);boolean placed=a.is(com.example.examplemod.AncientAnvilContent.ANVIL.get());state.addProperty("anvilPlaced",placed);state.addProperty("anvilLarge",placed&&a.getValue(com.example.examplemod.AncientAnvilBlock.LARGE));state.addProperty("anvilTier",placed?a.getValue(com.example.examplemod.AncientAnvilBlock.TIER):-1);state.addProperty("anvilMissing",com.example.examplemod.AncientAnvilBlockEntity.missing(minecraft.level,anvilPosition).size());state.addProperty("anvilFreeEdge",minecraft.level.getBlockState(anvilPosition.east(2)).is(com.example.examplemod.AncientAnvilContent.RUNESTONE.get()));state.addProperty("anvilGuide",AncientAnvilPreview.active(minecraft.level,anvilPosition));var plan=com.example.examplemod.AnvilGuide.next(minecraft.level,anvilPosition);state.addProperty("guideTier",plan.tier());state.addProperty("guideMissing",plan.missing(minecraft.level).size());state.addProperty("aimedAtAnvil",minecraft.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit&&(hit.getBlockPos().equals(anvilPosition)||minecraft.level.getBlockState(hit.getBlockPos()).is(com.example.examplemod.AncientAnvilContent.PART.get())&&com.example.examplemod.AncientAnvilPartBlock.core(hit.getBlockPos(),minecraft.level.getBlockState(hit.getBlockPos())).equals(anvilPosition)));}
            if(minecraft.player!=null){int count=0;for(int i=0;i<36;i++)if(minecraft.player.getInventory().getItem(i).is(com.example.examplemod.AncientAnvilContent.SCRAP_ITEM.get()))count+=minecraft.player.getInventory().getItem(i).getCount();state.addProperty("scrapInventory",count);state.addProperty("holdingArtifact",minecraft.player.getMainHandItem().is(com.example.examplemod.ArchaeologyTableMenu.ARTIFACTS));}
            if(feedCruncherId>=0&&minecraft.level!=null){state.addProperty("aimedAtCruncher",minecraft.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit&&hit.getEntity().getId()==feedCruncherId);state.addProperty("cruncherChewing",minecraft.level.getEntity(feedCruncherId) instanceof com.example.examplemod.Cruncher c&&c.isCrunching());}
            if(anvilPosition!=null&&minecraft.level!=null&&minecraft.level.getBlockEntity(anvilPosition) instanceof com.example.examplemod.AncientAnvilBlockEntity energy){
                var front=energy.getBlockState().getValue(com.example.examplemod.AncientAnvilBlock.FACING);var stages=com.example.examplemod.AnvilGuide.stages(minecraft.level);var status=com.example.examplemod.AnvilTierSix.inspect(minecraft.level,anvilPosition,front,stages);
                state.addProperty("tier6Crystals",status.crystals());state.addProperty("tier6Color",status.color());state.addProperty("energyColor",energy.energyColor);state.addProperty("energyActive",energy.energyUntil>minecraft.level.getGameTime());state.addProperty("energyBeams",AnvilEnergyRenderer.extract(energy,0).size());
                state.addProperty("tier6CrystalItems",minecraft.player.getInventory().countItem(net.minecraft.world.item.Items.END_CRYSTAL));state.addProperty("tier6GlassItems",minecraft.player.getInventory().countItem(net.minecraft.world.item.Items.LIGHT_BLUE_STAINED_GLASS));state.addProperty("tier6ObsidianItems",minecraft.player.getInventory().countItem(net.minecraft.world.item.Items.OBSIDIAN));state.addProperty("tier6CapItems",minecraft.player.getInventory().countItem(com.example.examplemod.AncientAnvilContent.ENERGIZED_CRYSTAL_BLOCK_ITEM.get()));
                state.addProperty("aimedAtCrystalCap",minecraft.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit&&minecraft.level.getBlockState(hit.getBlockPos()).is(net.minecraft.world.level.block.Blocks.OBSIDIAN));
            }
            if(anvilPosition!=null&&minecraft.level!=null&&minecraft.level.getBlockEntity(anvilPosition) instanceof com.example.examplemod.AncientAnvilBlockEntity be){
                state.addProperty("foundationRunestone",com.example.examplemod.AncientAnvilStructure.foundation(anvilPosition,be.getBlockState().getValue(com.example.examplemod.AncientAnvilBlock.FACING)).stream().filter(p->minecraft.level.getBlockState(p).is(com.example.examplemod.AncientAnvilContent.RUNESTONE.get())).count());
                state.addProperty("foundationObstruction",minecraft.level.getBlockState(anvilPosition.offset(2,-1,2)).is(net.minecraft.world.level.block.Blocks.STONE));
                state.addProperty("guideSneaking",minecraft.player.isShiftKeyDown());state.addProperty("holdingForgeHammer",minecraft.player.getMainHandItem().is(com.example.examplemod.AncientAnvilContent.IRON_FORGE_HAMMER.get()));
                state.addProperty("smithInputs",be.inputCount());state.addProperty("smithWork",be.work());state.addProperty("smithOutput",be.output().is(com.example.examplemod.AncientAnvilContent.RUNESTONE_ITEM.get()));
                var hit=minecraft.hitResult;var slot=hit instanceof net.minecraft.world.phys.BlockHitResult block?com.example.examplemod.AnvilSlot.nearest(anvilPosition,be.getBlockState().getValue(com.example.examplemod.AncientAnvilBlock.FACING),block.getLocation()):null;state.addProperty("smithAim",slot==null?"":slot.id);
                int runes=0,hammerDamage=0;for(int i=0;i<minecraft.player.getInventory().getContainerSize();i++){var stack=minecraft.player.getInventory().getItem(i);if(stack.is(com.example.examplemod.AncientAnvilContent.RUNESTONE_ITEM.get()))runes+=stack.getCount();if(stack.is(com.example.examplemod.AncientAnvilContent.IRON_FORGE_HAMMER.get()))hammerDamage=stack.getDamageValue();}state.addProperty("smithRunes",runes);state.addProperty("guideKeystones",minecraft.player.getInventory().countItem(com.example.examplemod.AncientAnvilContent.CANOPY_KEYSTONE_ITEM.get()));state.addProperty("guideCaps",minecraft.player.getInventory().countItem(net.minecraft.world.item.Items.DIAMOND_BLOCK));state.addProperty("guideBuckets",minecraft.player.getInventory().countItem(net.minecraft.world.item.Items.WATER_BUCKET)+minecraft.player.getInventory().countItem(net.minecraft.world.item.Items.LAVA_BUCKET));state.addProperty("smithHammerDamage",hammerDamage);
            }
            if(assetsAnvil!=null&&minecraft.level!=null){var s=minecraft.level.getBlockState(assetsAnvil);boolean anvil=s.is(com.example.examplemod.AncientAnvilContent.ANVIL.get());state.addProperty("assetsLarge",anvil&&s.getValue(com.example.examplemod.AncientAnvilBlock.LARGE));if(anvil){var facing=s.getValue(com.example.examplemod.AncientAnvilBlock.FACING);state.addProperty("assetsFacing",facing.name());state.addProperty("assetsDesign",s.getValue(com.example.examplemod.AncientAnvilBlock.DESIGN).name());state.addProperty("sampleInventory",minecraft.player.getInventory().getItem(0).getCount());int count=1;for(var c:com.example.examplemod.AncientAnvilStructure.CELLS)if(c.index()!=0&&com.example.examplemod.AncientAnvilStructure.belongs(minecraft.level,c.at(assetsAnvil,facing),assetsAnvil,facing,c.index()))count++;state.addProperty("assetsCells",count);}state.addProperty("assetsIngotDropped",!minecraft.level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(minecraft.player.blockPosition()).inflate(8),e->e.getItem().is(com.example.examplemod.AncientAnvilContent.INGOT.get())).isEmpty());}
            Path temporary=DIRECTORY.resolve("state.tmp");Files.writeString(temporary,JSON.toJson(state),StandardCharsets.UTF_8);
            Files.move(temporary,DIRECTORY.resolve("state.json"),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
            if(sequence==1)ExampleMod.LOGGER.info("CLIENT_VISUAL_QA_READY: actual Minecraft client renderer running");
        }catch(Exception failure){throw new IllegalStateException("Cannot write live visual-QA observations",failure);}
    }
    private static void widgets(GuiEventListener child,JsonArray output){
        if(child instanceof AbstractWidget widget){
            var b=new JsonObject();b.addProperty("label",widget.getMessage().getString());b.addProperty("enabled",widget.active&&widget.visible);
            b.addProperty("x",widget.getX());b.addProperty("y",widget.getY());b.addProperty("width",widget.getWidth());b.addProperty("height",widget.getHeight());output.add(b);
        }
        if(child instanceof ContainerEventHandler container)for(var nested:container.children())widgets(nested,output);
    }
    private record Measure(Minecraft minecraft) implements ExcavationScreenView.Canvas {
        @Override public int textWidth(String text){return minecraft.font.width(text);}
        @Override public void fill(int x,int y,int w,int h,int color){throw new UnsupportedOperationException();}
        @Override public void texture(net.minecraft.resources.Identifier id,int x,int y,int w,int h,float u,float v,int sw,int sh,int tw,int th,int color){throw new UnsupportedOperationException();}
        @Override public void text(String text,int x,int y,int color){throw new UnsupportedOperationException();}
    }
    private ClientVisualQa(){}
}
