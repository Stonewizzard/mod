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
                                forgePosition=new net.minecraft.core.BlockPos(12,player.blockPosition().getY(),12);
                                for(int x=-3;x<=3;x++)for(int z=-3;z<=5;z++)for(int y=0;y<=4;y++)level.setBlock(forgePosition.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                                level.setBlock(forgePosition,com.example.examplemod.AncientForgeContent.CORE.get().defaultBlockState(),3);
                                for(var part:com.example.examplemod.RuinedForgeStructure.PARTS)level.setBlock(part.at(forgePosition,net.minecraft.core.Direction.NORTH),com.example.examplemod.AncientForgeContent.BRICKS.get().defaultBlockState(),3);
                                var forge=(com.example.examplemod.ReconstructionTableBlockEntity)level.getBlockEntity(forgePosition);forge.pattern(1);
                                var finds=new com.example.examplemod.ArtifactProfile.Find[]{com.example.examplemod.ArtifactProfile.Find.PICKAXE_LEFT,com.example.examplemod.ArtifactProfile.Find.PICKAXE_BRIDGE,com.example.examplemod.ArtifactProfile.Find.PICKAXE_RIGHT,com.example.examplemod.ArtifactProfile.Find.TOOL_CONNECTOR,com.example.examplemod.ArtifactProfile.Find.TOOL_HANDLE};
                                for(int i=0;i<5;i++)forge.setItem(i,com.example.examplemod.ArchaeologyContent.reward(new com.example.examplemod.ExcavationStudy(new com.example.examplemod.ArtifactProfile(771+i,1,com.example.examplemod.ArtifactProfile.Matrix.SHALE,finds[i]))));
                                player.getInventory().setItem(0,net.minecraft.world.item.ItemStack.EMPTY);player.inventoryMenu.broadcastChanges();
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
                                if(action.equals("forge_view")){if(close)player.connection.teleport(12.5,forgePosition.getY(),9.5,0,com.example.examplemod.ForgeAssembly.formed(level.getBlockState(forgePosition))?15:23);else player.connection.teleport(9.5,forgePosition.getY(),8,-34,5);}
                            });
                        }
                        case "alloy_inventory"->{var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);player.getInventory().setItem(9,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_SCRAP,2));player.getInventory().setItem(10,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT,2));player.getInventory().setItem(11,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT,4));player.containerMenu.broadcastChanges();});}
                        case "anvil_fixture","anvil_view","anvil_foundation","anvil_expose","anvil_damage"->{
                            var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();var action=command.get("action").getAsString();boolean close=command.has("close")&&command.get("close").getAsBoolean();
                            server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var l=player.level();if(action.equals("anvil_fixture")){
                                anvilPosition=forgePosition.offset(7,0,0);
                                for(int x=-2;x<=2;x++)for(int z=-3;z<=3;z++)for(int y=0;y<=3;y++)l.setBlock(anvilPosition.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                                for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)l.setBlock(anvilPosition.offset(x,-1,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
                                player.getInventory().setItem(1,new net.minecraft.world.item.ItemStack(com.example.examplemod.AncientAnvilContent.ANVIL_ITEM.get()));player.inventoryMenu.broadcastChanges();
                                player.connection.teleport(anvilPosition.getX()+.5,anvilPosition.getY(),anvilPosition.getZ()-3,0,25);
                            }
                            if(action.equals("anvil_expose"))for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0)l.setBlock(anvilPosition.offset(x,-1,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                            if(action.equals("anvil_foundation"))for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)l.setBlock(anvilPosition.offset(x,-1,z),((x+z)%2==0?net.minecraft.world.level.block.Blocks.IRON_BLOCK:net.minecraft.world.level.block.Blocks.GOLD_BLOCK).defaultBlockState(),3);
                            if(action.equals("anvil_damage"))l.setBlock(anvilPosition.offset(1,-1,1),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                            if(action.equals("anvil_view")){if(close)player.connection.teleport(anvilPosition.getX()+.5,anvilPosition.getY(),anvilPosition.getZ()-2.5,0,17);else player.connection.teleport(anvilPosition.getX()-1.5,anvilPosition.getY(),anvilPosition.getZ()-2.5,-34,16);}
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
            if(screen instanceof ReconstructionTableScreen forgeScreen){
                var menu=forgeScreen.getMenu();state.addProperty("forgeMenu",menu.forge());state.addProperty("forgePattern",menu.pattern());state.addProperty("forgeOutput",!menu.getSlot(5).getItem().isEmpty());
                var controls=new JsonArray();int left=(window.getGuiScaledWidth()-320)/2,top=(window.getGuiScaledHeight()-242)/2;
                var b=new JsonObject();b.addProperty("id","forge_assemble");b.addProperty("enabled",menu.ready());b.addProperty("x",left+ReconstructionScreenView.BUILD_X);b.addProperty("y",top+ReconstructionScreenView.BUILD_Y);b.addProperty("width",ReconstructionScreenView.BUILD_W);b.addProperty("height",ReconstructionScreenView.BUILD_H);controls.add(b);
                if(menu.forge()){var a=new JsonObject();a.addProperty("id","forge_alloy_tab");a.addProperty("enabled",true);a.addProperty("x",left+218);a.addProperty("y",top+8);a.addProperty("width",49);a.addProperty("height",16);controls.add(a);}
                state.add("controls",controls);
            }
            if(screen instanceof ForgeAlloyScreen alloy){var menu=alloy.getMenu();state.addProperty("alloyReady",menu.ready());state.addProperty("alloyCount",menu.getSlot(5).getItem().is(com.example.examplemod.AncientAnvilContent.INGOT.get())?menu.getSlot(5).getItem().getCount():0);state.addProperty("alloyScrap",menu.getSlot(0).getItem().getCount());state.addProperty("alloyGold",menu.getSlot(1).getItem().getCount());state.addProperty("alloyIron",menu.getSlot(2).getItem().getCount());}
            if(anvilPosition!=null&&minecraft.level!=null){var a=minecraft.level.getBlockState(anvilPosition);boolean placed=a.is(com.example.examplemod.AncientAnvilContent.ANVIL.get());state.addProperty("anvilPlaced",placed);state.addProperty("anvilTier",placed?a.getValue(com.example.examplemod.AncientAnvilBlock.TIER):-1);state.addProperty("anvilMissing",com.example.examplemod.AncientAnvilBlockEntity.missing(minecraft.level,anvilPosition).size());state.addProperty("anvilGuide",AncientAnvilPreview.active(minecraft.level,anvilPosition));state.addProperty("aimedAtAnvil",minecraft.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit&&hit.getBlockPos().equals(anvilPosition));}
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
