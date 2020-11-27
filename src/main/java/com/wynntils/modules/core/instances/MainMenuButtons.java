/*
 *  * Copyright © Wynntils - 2020.
 */

package com.wynntils.modules.core.instances;

import com.wynntils.Reference;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.core.utils.ServerUtils;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.core.overlays.UpdateOverlay;
import com.wynntils.modules.core.overlays.ui.UpdateAvailableScreen;
import com.wynntils.modules.utilities.instances.ServerIcon;
import com.wynntils.webapi.WebManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.List;

public class MainMenuButtons {

    private static ServerList serverList = null;
    private static final int WYNNCRAFT_BUTTON_ID = 3790627;

    private static WynncraftButton lastButton = null;
    
    private static boolean alreadyLoaded = false;

    public static void addButtons(GuiMainMenu to, List<GuiButton> buttonList, boolean resize) {
        if (!CoreDBConfig.INSTANCE.addMainMenuButton) return;

        if (lastButton == null || !resize) {
            ServerData s = getWynncraftServerData(to.mc);
            FMLClientHandler.instance().setupServerList();

            lastButton = new WynncraftButton(s, WYNNCRAFT_BUTTON_ID, to.width / 2 + 104, to.height / 4 + 48 + 24);
            WebManager.checkForUpdates();
            UpdateOverlay.reset();

            buttonList.add(lastButton);

            // little pling when finished loading
            if (!alreadyLoaded) {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_NOTE_PLING, 1f));
                alreadyLoaded = true;
            }
            return;
        }

        lastButton.x = to.width / 2 + 104; lastButton.y = to.height / 4 + 48 + 24;
        buttonList.add(lastButton);
    }

    public static void actionPerformed(GuiMainMenu on, GuiButton button, List<GuiButton> buttonList) {
        if (button.id == WYNNCRAFT_BUTTON_ID) {
            clickedWynncraftButton(on.mc, ((WynncraftButton) button).serverIcon.getServer(), on);
        }
    }

    private static void clickedWynncraftButton(Minecraft mc, ServerData server, GuiScreen backGui) {
        if (hasUpdate()) {
            mc.displayGuiScreen(new UpdateAvailableScreen(server));
        } else {
            WebManager.skipJoinUpdate();
            ServerUtils.connect(backGui, server);
        }
    }

    private static boolean hasUpdate() {
        return !Reference.developmentEnvironment && WebManager.getUpdate() != null && WebManager.getUpdate().hasUpdate();
    }

    private static ServerData getWynncraftServerData(Minecraft mc) {
        return ServerUtils.getWynncraftServerData(serverList = new ServerList(mc), true);
    }

    private static class WynncraftButton extends GuiButton {

        private ServerIcon serverIcon;

        WynncraftButton(ServerData server, int buttonId, int x, int y) {
            super(buttonId, x, y, 20, 20, "");

            serverIcon = new ServerIcon(server, true);
            serverIcon.onDone(r -> serverList.saveServerList());
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!visible) return;

            super.drawButton(mc, mouseX, mouseY, partialTicks);

            ServerIcon.ping();
            ResourceLocation icon = serverIcon.getServerIcon();
            if (icon == null) icon = ServerIcon.UNKNOWN_SERVER;
            mc.getTextureManager().bindTexture(icon);

            boolean hasUpdate = hasUpdate();

            GlStateManager.pushMatrix();

            GlStateManager.translate(x + 2, y + 2, 0);
            GlStateManager.scale(0.5f, 0.5f, 0);
            GlStateManager.enableBlend();
            drawModalRectWithCustomSizedTexture(0, 0, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
            if (!hasUpdate) {
                GlStateManager.disableBlend();
            }

            GlStateManager.popMatrix();

            if (hasUpdate) {
                Textures.UIs.main_menu.bind();
                // When not provided with the texture size vanilla automatically assumes both the height and width are 256
                drawTexturedModalRect(x, y, 0, 0, 20, 20);
            }

            GlStateManager.disableBlend();
        }

    }

    public static class FakeGui extends GuiScreen {
        FakeGui() {
            doAction();
        }

        @Override
        public void initGui() {
            doAction();
        }

        private static void doAction() {
            clickedWynncraftButton(Minecraft.getMinecraft(), getWynncraftServerData(Minecraft.getMinecraft()), null);
        }
    }

}
