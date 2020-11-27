/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.map;

import com.wynntils.Reference;
import com.wynntils.core.framework.enums.Priority;
import com.wynntils.core.framework.instances.KeyHolder;
import com.wynntils.core.framework.instances.Module;
import com.wynntils.core.framework.interfaces.annotations.ModuleInfo;
import com.wynntils.core.utils.Utils;
import com.wynntils.modules.map.commands.CommandLocate;
import com.wynntils.modules.map.commands.CommandLootRun;
import com.wynntils.modules.map.configs.MapConfig;
import com.wynntils.modules.map.events.ClientEvents;
import com.wynntils.modules.map.instances.MapProfile;
import com.wynntils.modules.map.managers.LootRunManager;
import com.wynntils.modules.map.overlays.MiniMapOverlay;
import com.wynntils.modules.map.overlays.ui.MainWorldMapUI;
import com.wynntils.modules.map.overlays.ui.WaypointCreationMenu;
import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.WebReader;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "map", displayName = "Map")
public class MapModule extends Module {

    private static MapModule module;
    private KeyHolder mapKey;
    private MapProfile mainMap;

    @Override
    public void onEnable() {
        module = this;

        WebReader webApi = WebManager.getApiUrls();
        mainMap = new MapProfile(webApi == null ? null : webApi.get("MainMap"), "main-map");
        mainMap.updateMap();

        LootRunManager.setup();

        registerEvents(new ClientEvents());

        registerSettings(MapConfig.class);
        registerSettings(MapConfig.Textures.class);
        registerSettings(MapConfig.Waypoints.class);
        registerSettings(MapConfig.WorldMap.class);
        registerSettings(MapConfig.LootRun.class);
        registerSettings(MapConfig.Telemetry.class);

        registerOverlay(new MiniMapOverlay(), Priority.LOWEST);

        registerCommand(new CommandLootRun());
        registerCommand(new CommandLocate());

        registerKeyBinding("New Waypoint", Keyboard.KEY_B, "Wynntils", true, () -> {
            if (Reference.onWorld)
                Minecraft.getMinecraft().displayGuiScreen(new WaypointCreationMenu(null));
        });

        mapKey = registerKeyBinding("Open Map", Keyboard.KEY_M, "Wynntils", true, () -> {
            if (Reference.onWorld) {
                if (WebManager.getApiUrls() == null) {
                    WebManager.tryReloadApiUrls(true);
                } else {
                    Utils.displayGuiScreen(new MainWorldMapUI());
                }
            }
        });
    }

    public static MapModule getModule() {
        return module;
    }

    public MapProfile getMainMap() {
        return mainMap;
    }

    public KeyHolder getMapKey() { return mapKey; }
}
