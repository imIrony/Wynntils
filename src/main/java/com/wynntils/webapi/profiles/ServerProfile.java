/*
 *  * Copyright © Wynntils - 2020.
 */

package com.wynntils.webapi.profiles;

import com.wynntils.core.utils.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class ServerProfile {

    long firstSeen;
    Set<String> players;

    public ServerProfile(long firstSeem, Set<String> players) {
        this.firstSeen = firstSeem; this.players = players;
    }

    public Set<String> getPlayers() {
        return players;
    }

    public long getFirstSeem() {
        return firstSeen;
    }

    public String getUptime() {
        return StringUtils.millisToLongString(System.currentTimeMillis() - firstSeen);
    }

    /**
     * This makes the firstSeem match the user computer time instead of the server time
     *
     * @param serverTime the input server time
     */
    public void matchTime(long serverTime) {
        firstSeen = firstSeen - (System.currentTimeMillis() - serverTime);
    }

}
