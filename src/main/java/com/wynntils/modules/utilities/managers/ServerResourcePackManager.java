/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.utilities.managers;

import com.wynntils.Reference;
import com.wynntils.modules.utilities.UtilitiesModule;
import com.wynntils.modules.utilities.configs.UtilitiesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.network.play.server.SPacketResourcePackSend;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class ServerResourcePackManager {

    public static void applyOnServerJoin() {
        if (!Reference.onServer) return;

        if (Minecraft.getMinecraft().getResourcePackRepository().getServerResourcePack() == null && UtilitiesConfig.INSTANCE.autoResource && !UtilitiesConfig.INSTANCE.lastServerResourcePack.isEmpty()) {
            if (Minecraft.getMinecraft().getCurrentServerData().getResourceMode() != ServerData.ServerResourceMode.ENABLED) {
                Reference.LOGGER.warn("Did not auto apply Wynncraft server resource pack because resource packs have not been enabled");
                return;
            }

            downloadServerResourcePack();
        }
    }

    public static boolean shouldCancelResourcePackLoad(SPacketResourcePackSend packet) {
        if (!Reference.onServer) return false;

        String resourcePack = packet.getURL();
        String hash = packet.getHash();
        String fileName = DigestUtils.sha1Hex(resourcePack);

        if (!resourcePack.equals(UtilitiesConfig.INSTANCE.lastServerResourcePack) || !hash.equalsIgnoreCase(UtilitiesConfig.INSTANCE.lastServerResourcePackHash)) {
            if (UtilitiesConfig.INSTANCE.lastServerResourcePack.isEmpty()) {
                Reference.LOGGER.info("Found server resource pack: \"server-resource-packs/" + fileName + "\"#" + hash + " from \"" + resourcePack + "\"");
            } else {
                String lastPack = UtilitiesConfig.INSTANCE.lastServerResourcePack;
                String lastHash = UtilitiesConfig.INSTANCE.lastServerResourcePackHash;
                Reference.LOGGER.info(
                    "New server resource pack: \"server-resource-packs/" + fileName + "\"#" + hash + " from \"" + resourcePack +
                    "\" (Was \"server-resource-packs/" + DigestUtils.sha1Hex(lastPack) + "\"#" + lastHash + " from \"" + lastPack + "\")"
                );
            }
            UtilitiesConfig.INSTANCE.lastServerResourcePack = resourcePack;
            UtilitiesConfig.INSTANCE.lastServerResourcePackHash = hash;
            UtilitiesConfig.INSTANCE.saveSettings(UtilitiesModule.getModule());
        }

        IResourcePack current = Minecraft.getMinecraft().getResourcePackRepository().getServerResourcePack();
        if (current != null && current.getPackName().equals(fileName)) {
            boolean hashMatches = false;
            try (InputStream is = new FileInputStream(new File(new File(Minecraft.getMinecraft().gameDir, "server-resource-packs"), fileName))) {
                hashMatches = DigestUtils.sha1Hex(is).equalsIgnoreCase(hash);
            } catch (IOException err) { }

            // Already loaded this pack if matches so cancel
            return hashMatches;
        }
        return false;
    }

    public static boolean isLoaded() {
        if (UtilitiesConfig.INSTANCE.lastServerResourcePack.isEmpty()) return false;

        IResourcePack current = Minecraft.getMinecraft().getResourcePackRepository().getServerResourcePack();
        if (current == null) return false;

        String resourcePack = UtilitiesConfig.INSTANCE.lastServerResourcePack;
        String hash = UtilitiesConfig.INSTANCE.lastServerResourcePackHash;
        String fileName = DigestUtils.sha1Hex(resourcePack);
        if (!current.getPackName().equals(fileName)) return false;

        try (InputStream is = new FileInputStream(new File(new File(Minecraft.getMinecraft().gameDir, "server-resource-packs"), fileName))) {
            return DigestUtils.sha1Hex(is).equalsIgnoreCase(hash);
        } catch (IOException err) { }

        return false;
    }

    public static void loadServerResourcePack() {
        // Does not download if not found / invalid
        if (UtilitiesConfig.INSTANCE.lastServerResourcePack.isEmpty()) return;
        String resourcePack = UtilitiesConfig.INSTANCE.lastServerResourcePack;
        String hash = UtilitiesConfig.INSTANCE.lastServerResourcePackHash;
        String fileName = DigestUtils.sha1Hex(resourcePack);

        File f = new File(new File(Minecraft.getMinecraft().gameDir, "server-resource-packs"), fileName);

        boolean valid = false;
        try (InputStream is = new FileInputStream(f)) {
            valid = DigestUtils.sha1Hex(is).equalsIgnoreCase(hash);
        } catch (IOException err) { }

        if (!valid) return;

        Minecraft.getMinecraft().getResourcePackRepository().setServerResourcePack(f);
    }

    public static void downloadServerResourcePack() {
        if (UtilitiesConfig.INSTANCE.lastServerResourcePack.isEmpty()) return;
        try {
            Minecraft.getMinecraft().getResourcePackRepository().downloadResourcePack(UtilitiesConfig.INSTANCE.lastServerResourcePack, UtilitiesConfig.INSTANCE.lastServerResourcePackHash).get();
        } catch (InterruptedException ignored) {
        } catch (ExecutionException e) {
            Reference.LOGGER.error("Could not load server resource pack");
            e.printStackTrace();

            UtilitiesConfig.INSTANCE.lastServerResourcePack = "";
            UtilitiesConfig.INSTANCE.lastServerResourcePackHash = "";
            UtilitiesConfig.INSTANCE.saveSettings(UtilitiesModule.getModule());
        }
    }

}
