/*
 *  * Copyright © Wynntils - 2020.
 */

package com.wynntils.core.events.custom;

import net.minecraftforge.fml.common.eventhandler.Event;

public class ClientEvent extends Event {

    /**
     * Called when the client is successfully loaded
     */
    public static class Ready extends ClientEvent {

    }

}
