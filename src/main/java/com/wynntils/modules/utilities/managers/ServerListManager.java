/*
 *  * Copyright © Wynntils - 2020.
 */

package com.wynntils.modules.utilities.managers;

import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.profiles.ServerProfile;

import java.util.HashMap;
import java.util.Map;

public class ServerListManager {

    private static Map<String, ServerProfile> availableServers = new HashMap<>();

    public static synchronized void updateServers() {
        WebManager.getServerList((list) -> availableServers = list);
    }

    public static ServerProfile getServer(String id) {
        return availableServers.get(id);
    }

    public static String getUptime(String id) {
        if (!availableServers.containsKey(id)) return "Latest";
        else return availableServers.get(id).getUptime();
    }

}
