/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.webapi.account;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wynntils.ModCore;
import com.wynntils.Reference;
import com.wynntils.core.utils.helpers.MD5Verification;
import com.wynntils.webapi.WebManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.CryptManager;
import org.apache.commons.io.IOUtils;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class WynntilsAccount {

    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("wynntils-accounts-%d").build());

    String token;
    boolean ready = false;

    HashMap<String, String> encondedConfigs = new HashMap<>();
    HashMap<String, String> md5Verifications = new HashMap<>();
    CloudConfigurations configurationUploader;

    public WynntilsAccount() { }

    public String getToken() {
        return token;
    }

    public HashMap<String, String> getEncondedConfigs() {
        return encondedConfigs;
    }

    public void updateDiscord(String id, String username) {
        if (!ready || WebManager.getApiUrls() == null) return;

        service.submit(() -> {
            try {
                URLConnection st = new URL(WebManager.getApiUrls().get("UserAccount") + "updateDiscord/" + token).openConnection();

                // HeyZeer0: Request below
                JsonObject body = new JsonObject();
                body.addProperty("id", id);
                body.addProperty("username", username);
                // {"id":"<user-id>", "username":"<username>"}

                byte[] bodyBytes = body.toString().getBytes(StandardCharsets.UTF_8);

                st.setRequestProperty("User-Agent", "WynntilsClient/v" + Reference.VERSION + "/B" + Reference.BUILD_NUMBER);
                st.setRequestProperty("Content-Length", "" + bodyBytes.length);
                st.setRequestProperty("Content-Type", "application/json");
                st.setDoOutput(true);

                OutputStream outputStream = null;
                try {
                    outputStream = st.getOutputStream();
                    IOUtils.write(bodyBytes, outputStream);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                } finally {
                    IOUtils.closeQuietly(outputStream);
                }

                Reference.LOGGER.info("Updating user Discord ID");
            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    public void uploadConfig(String fileName, String base64) {
        if (!ready || configurationUploader == null) return;

        configurationUploader.queueConfig(fileName, base64);
    }

    int connectionAttempts = 0;

    public void login() {
        if (WebManager.getApiUrls() == null || connectionAttempts >= 4) return;

        connectionAttempts++;

        try {
            URLConnection st = new URL(WebManager.getApiUrls().get("UserAccount") + "/requestEncryption").openConnection();
            st.setRequestProperty("User-Agent", "WynntilsClient/v" + Reference.VERSION + "/B" + Reference.BUILD_NUMBER);

            JsonObject result = new JsonParser().parse(IOUtils.toString(st.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();

            byte[] publicKeyBy = DatatypeConverter.parseHexBinary(result.get("publicKeyIn").getAsString());

            SecretKey secretkey = CryptManager.createNewSharedKey();
            PublicKey publicKey = CryptManager.decodePublicKey(publicKeyBy);

            String s1 = (new BigInteger(1, CryptManager.getServerIdHash("", publicKey, secretkey))).toString(16);

            Minecraft mc = ModCore.mc();
            mc.getSessionService().joinServer(mc.getSession().getProfile(), mc.getSession().getToken(), s1);

            byte[] secretKeyEncrypted = CryptManager.encryptData(publicKey, secretkey.getEncoded());
            String lastKey = DatatypeConverter.printHexBinary(secretKeyEncrypted);

            URLConnection st2 = new URL(WebManager.getApiUrls().get("UserAccount") + "/responseEncryption/").openConnection();

            JsonObject object = new JsonObject();
            object.addProperty("username", mc.getSession().getUsername());
            object.addProperty("key", lastKey);
            object.addProperty("version", Reference.VERSION + (Reference.BUILD_NUMBER != -1 ? "_" + Reference.BUILD_NUMBER : ""));

            byte[] postAsBytes = object.toString().getBytes(StandardCharsets.UTF_8);

            st2.setRequestProperty("User-Agent", "WynntilsClient/v" + Reference.VERSION + "/B" + Reference.BUILD_NUMBER);
            st2.setRequestProperty("Content-Length", "" + postAsBytes.length);
            st2.setRequestProperty("Content-Type", "application/json");
            st2.setDoOutput(true);

            OutputStream outputStream = null;
            try {
                outputStream = st2.getOutputStream();
                IOUtils.write(postAsBytes, outputStream);
            } finally {
                IOUtils.closeQuietly(outputStream);
            }

            JsonObject finalResult = new JsonParser().parse(IOUtils.toString(st2.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();

            if (finalResult.has("error") || !finalResult.has("result")) {
                Reference.LOGGER.error("Failed to connect to Wynntils Accounts trying again!");

                login();
                return;
            }

            token = finalResult.get("authtoken").getAsString();
            ready = true;

            JsonObject hashes = finalResult.getAsJsonObject("hashes");
            hashes.entrySet().forEach((k) -> md5Verifications.put(k.getKey(), k.getValue().getAsString()));

            configurationUploader = new CloudConfigurations(service, token);

            Reference.LOGGER.info("Succesfully connected to accounts!");

            JsonObject obj = finalResult.get("configFiles").getAsJsonObject();
            if (obj.entrySet().size() <= 0) return;

            for (Map.Entry<String, JsonElement> objs : obj.entrySet()) {
                encondedConfigs.put(objs.getKey(), objs.getValue().getAsString());
            }

        } catch (Exception ex) {
            ex.printStackTrace();

            Reference.LOGGER.error("Failed to connect to Wynntils Accounts trying again!");
            login();
        }
    }

    public String getMD5Verification(String key) {
        String digest = md5Verifications.getOrDefault(key, null);
        return MD5Verification.isMd5Digest(digest) ? digest : null;
    }

}
