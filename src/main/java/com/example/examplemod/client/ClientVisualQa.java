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
                                player.connection.teleport(12.5,forgePosition.getY()+1.5,6,0,24);
                            });
                        }
                        case "forge_view","forge_damage","forge_repair"->{
                            var server=minecraft.getSingleplayerServer();var uuid=minecraft.player.getUUID();var action=command.get("action").getAsString();boolean close=command.has("close")&&command.get("close").getAsBoolean();
                            server.execute(()->{var player=server.getPlayerList().getPlayer(uuid);var level=player.level();
                                if(action.equals("forge_damage"))level.setBlock(com.example.examplemod.RuinedForgeStructure.PARTS.getLast().at(forgePosition,net.minecraft.core.Direction.NORTH),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
                                if(action.equals("forge_repair"))for(var part:com.example.examplemod.RuinedForgeStructure.PARTS)level.setBlock(part.at(forgePosition,net.minecraft.core.Direction.NORTH),com.example.examplemod.AncientForgeContent.BRICKS.get().defaultBlockState(),3);
                                if(action.equals("forge_view")){if(close)player.connection.teleport(12.5,forgePosition.getY(),9.5,0,23);else player.connection.teleport(8,forgePosition.getY()+1.5,6,-34,24);}
                            });
                        }
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
                state.addProperty("forgeReady",com.example.examplemod.RuinedForgeStructure.inspect(minecraft.level,forgePosition,net.minecraft.core.Direction.NORTH).complete());
                state.addProperty("aimedAtForge",minecraft.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit&&hit.getBlockPos().equals(forgePosition));
            }
            if(screen instanceof ReconstructionTableScreen forgeScreen){
                var menu=forgeScreen.getMenu();state.addProperty("forgeMenu",menu.forge());state.addProperty("forgePattern",menu.pattern());state.addProperty("forgeOutput",!menu.getSlot(5).getItem().isEmpty());
                var controls=new JsonArray();int left=(window.getGuiScaledWidth()-320)/2,top=(window.getGuiScaledHeight()-242)/2;
                var b=new JsonObject();b.addProperty("id","forge_assemble");b.addProperty("enabled",menu.ready());b.addProperty("x",left+ReconstructionScreenView.BUILD_X);b.addProperty("y",top+ReconstructionScreenView.BUILD_Y);b.addProperty("width",ReconstructionScreenView.BUILD_W);b.addProperty("height",ReconstructionScreenView.BUILD_H);controls.add(b);state.add("controls",controls);
            }
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
