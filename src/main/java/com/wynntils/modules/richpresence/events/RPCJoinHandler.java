/*
 *  * Copyright © Wynntils - 2019.
 */

package com.wynntils.modules.richpresence.events;

import com.wynntils.Reference;
import com.wynntils.core.events.custom.WynnWorldEvent;
import com.wynntils.core.framework.FrameworkManager;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.utils.Utils;
import com.wynntils.modules.richpresence.RichPresenceModule;
import com.wynntils.modules.richpresence.discordrpc.DiscordRichPresence;
import com.wynntils.modules.richpresence.profiles.SecretContainer;
import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.profiles.player.PlayerStatsProfile.PlayerTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RPCJoinHandler implements DiscordRichPresence.DiscordEventHandlers.OnJoinGame {

    private static final ServerData serverAddress = new ServerData("Wynncraft", "play.wynncraft.com", false);
    private static final Pattern dmRegex = Pattern.compile("§7(\\[(.*) ➤ (.*)\\])(.*)");

    boolean waitingLobby = false;
    boolean waitingInvite = false;

    boolean sentInvite = false;

    SecretContainer lastSecret = null;

    long delayTime = 0;

    public RPCJoinHandler() {
        serverAddress.setResourceMode(ServerData.ServerResourceMode.ENABLED);

        FrameworkManager.getEventBus().register(this);
    }

    public void accept(String joinSecret) {
        lastSecret = new SecretContainer(joinSecret);
        if (lastSecret.getOwner().isEmpty() || lastSecret.getRandomHash().isEmpty() || lastSecret.getWorldType().equals("HB") && WebManager.getPlayerProfile() != null && WebManager.getPlayerProfile().getTag() != PlayerTag.HERO) return;

        RichPresenceModule.getModule().getRichPresence().setJoinSecret(lastSecret);

        Minecraft mc = Minecraft.getMinecraft();

        if(!Reference.onServer) {
            GuiMultiplayer guiMultiplayer = new GuiMultiplayer(new GuiMainMenu());

            if(mc.world != null) {
                mc.world.sendQuittingDisconnectingPacket();
                mc.loadWorld(null);
            }

            FMLClientHandler.instance().connectToServer(guiMultiplayer, serverAddress);
            waitingLobby = true;
            return;
        }
        if(Reference.onWorld) {
            if (Reference.getUserWorld().replace("WC", "").replace("HB", "").replace("EU", "").equals(String.valueOf(lastSecret.getWorld())) && Reference.getUserWorld().replaceAll("\\d+", "").equals(lastSecret.getWorldType())) {
                sentInvite = true;
                mc.player.sendChatMessage("/msg " + lastSecret.getOwner() + " " + lastSecret.getRandomHash());
                return;
            }

            mc.player.sendChatMessage("/hub");
            waitingLobby = true;
            return;
        }

        Utils.joinWorld(lastSecret.getWorldType(), lastSecret.getWorld());
        waitingInvite = true;
    }

    @SubscribeEvent
    public void onLobby(RenderPlayerEvent.Post e) {
        if (Reference.onWorld || !waitingLobby || delayTime > Minecraft.getSystemTime()) return;

        waitingLobby = false;
        waitingInvite = true;
        Utils.joinWorld(lastSecret.getWorldType(), lastSecret.getWorld());
    }

    @SubscribeEvent
    public void onWorldJoin(WynnWorldEvent.Join e) {
        if(!waitingInvite) return;

        sentInvite = true;
        waitingInvite = false;
        Minecraft.getMinecraft().player.sendChatMessage("/msg " + lastSecret.getOwner() + " " + lastSecret.getRandomHash());
    }

    @SubscribeEvent
    public void onChatMessage(ClientChatReceivedEvent e) {
        if(e.getType() != ChatType.CHAT && e.getType() != ChatType.SYSTEM) return;

        //handles the invitation
        if (lastSecret != null && e.getMessage().getUnformattedText().startsWith("You have been invited to join " + lastSecret.getOwner())) {
            Minecraft.getMinecraft().player.sendChatMessage("/party join " + lastSecret.getOwner());

            lastSecret = null;
            return;
        }

        // handles the user join
        if (sentInvite && e.getMessage().getUnformattedText().startsWith("[" + Minecraft.getMinecraft().player.getName())) {
            sentInvite = false;
            e.setCanceled(true);
            return;
        }

        // handles the party owner
        if (PlayerInfo.getPlayerInfo().getPlayerParty().isPartying()) {
            String text = e.getMessage().getFormattedText();
            Matcher m = dmRegex.matcher(text);

            if (!m.matches()) return;

            String content = TextFormatting.getTextWithoutFormattingCodes(m.group(4).substring(1));
            String user = TextFormatting.getTextWithoutFormattingCodes(m.group(2));

            if (!RichPresenceModule.getModule().getRichPresence().validSecrent(content.substring(0, content.length() - 1))) return;

            e.setCanceled(true);
            Minecraft.getMinecraft().player.sendChatMessage("/party invite " + user);
        }
    }

    @SubscribeEvent
    public void onTitle(ClientChatReceivedEvent e) {
        String text = e.getMessage().getUnformattedText();
        if ((text.equals("You are already connected to this server!") || text.equals("You're rejoining too quickly! Give us a moment to save your data.")) && waitingInvite) {
            waitingLobby = true;
            waitingInvite = false;
            delayTime = Minecraft.getSystemTime() + 2000;
        }
    }

}
