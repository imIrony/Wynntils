/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.webapi.account;

import com.google.gson.Gson;
import com.wynntils.Reference;
import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.request.PostRequest;
import com.wynntils.webapi.request.multipart.IMultipartFormPart;
import com.wynntils.webapi.request.multipart.MultipartFormDataPart;
import com.wynntils.webapi.request.multipart.MultipartFormFilePart;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CloudConfigurations {

    private final ScheduledExecutorService service;
    private ScheduledFuture runningTask;
    private final String token;

    private final Gson gson = new Gson();

    public CloudConfigurations(ScheduledExecutorService service, String token) {
        this.service = service; this.token = token;
    }

    private final Map<String, File> toUpload = new ConcurrentHashMap<>();

    public void queueConfig(File f) {
        synchronized (toUpload) {
            toUpload.put(f.getName(), f);

            startUploadQueue();
        }
    }

    private void startUploadQueue() {
        if (runningTask != null && !runningTask.isDone() && !runningTask.isCancelled() || WebManager.getApiUrls() == null) return;

        runningTask = service.scheduleAtFixedRate(() -> {
            if (toUpload.isEmpty()) {
                runningTask.cancel(false); // cancel to avoid useless verifications
                return;
            }

            PostRequest request = new PostRequest(WebManager.getApiUrls().get("Athena") + "/user/uploadConfigs", "uploadConfigs");
            request.handleJsonObject(json -> true);

            List<IMultipartFormPart> formParts = new ArrayList<>();

            formParts.add(new MultipartFormDataPart("authToken", token.getBytes(StandardCharsets.UTF_8)));

            Iterator it = toUpload.values().iterator();
            while (it.hasNext()) {
                File f = (File) it.next();

                formParts.add(new MultipartFormFilePart("config", f));

                it.remove();
            }

            request.postMultipart(formParts);
            WebManager.getHandler().addAndDispatch(request);

            Reference.LOGGER.info("Configuration upload complete!");
        }, 0, 10, TimeUnit.SECONDS);
    }

}
