package net.creeperhost.creeperhost;

import net.creeperhost.creeperhost.common.Config;
import net.creeperhost.creeperhost.gui.element.ButtonCreeper;
import net.creeperhost.creeperhost.gui.GuiGetServer;
import net.creeperhost.creeperhost.gui.mpreplacement.CreeperHostEntry;
import net.creeperhost.creeperhost.api.Order;
import net.creeperhost.creeperhost.gui.mpreplacement.CreeperHostServerSelectionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

public class EventHandler{

    private static final int BUTTON_ID = 30051988;

    private static Field parentScreenField;

    private GuiMultiplayer lastInitialized = null;

    @SubscribeEvent
    public void onInitGui(InitGuiEvent.Post event){
        if (!Config.getInstance().isMainMenuEnabled())
            return;
        GuiScreen gui = Util.getGuiFromEvent(event);
        if(gui instanceof GuiMainMenu){
            CreeperHost.instance.setRandomImplementation();
            if (CreeperHost.instance.getImplementation() == null)
                return;
            List<GuiButton> buttonList = Util.getButtonList(event);
            if (buttonList != null) {
                buttonList.add(new ButtonCreeper(BUTTON_ID, gui.width/2+104, gui.height/4+48+72+12));
            }
        } else if(gui instanceof  GuiMultiplayer) {
            if (!Config.getInstance().isMpMenuEnabled() || CreeperHost.instance.getImplementation() == null)
                return;
            // Done using reflection so we can work on 1.8.9 before setters/getters
            GuiMultiplayer mpGUI = (GuiMultiplayer) gui;
            try
            {
                if (serverListSelectorField == null) {
                    serverListSelectorField = ReflectionHelper.findField(GuiMultiplayer.class, "field_146803_h", "serverListSelector");
                    serverListSelectorField.setAccessible(true);
                }

                if (serverListInternetField == null) {
                    serverListInternetField = ReflectionHelper.findField(ServerSelectionList.class, "field_148198_l", "serverListInternet");
                    serverListInternetField.setAccessible(true);
                }

                ServerSelectionList serverListSelector = (ServerSelectionList) serverListSelectorField.get(mpGUI); // Get the old selector
                List serverListInternet = (List) serverListInternetField.get(serverListSelector); // Get the list from inside it
                CreeperHostServerSelectionList ourList = new CreeperHostServerSelectionList(mpGUI, Minecraft.getMinecraft(), mpGUI.width, mpGUI.height, 32, mpGUI.height - 64, 36);
                ourList.replaceList(serverListInternet);
                serverListInternetField.set(ourList, serverListInternet);
                serverListSelectorField.set(mpGUI, ourList);
                lastInitialized = mpGUI;
            } catch (Throwable e)
            {
                CreeperHost.logger.warn("Reflection to alter server list failed.", e);
            }
        }
    }

    @SubscribeEvent
    public void onActionPerformed(ActionPerformedEvent.Pre event){
        if (!Config.getInstance().isMainMenuEnabled() || CreeperHost.instance.getImplementation() == null)
            return;
        GuiScreen gui = Util.getGuiFromEvent(event);
        if(gui instanceof GuiMainMenu){
            GuiButton button = Util.getButton(event);
            if(button != null && button.id == BUTTON_ID){
                Minecraft.getMinecraft().displayGuiScreen(GuiGetServer.getByStep(0, new Order()));
            }
        }
    }

    private static Field serverListSelectorField;
    private static Field serverListInternetField;
}
