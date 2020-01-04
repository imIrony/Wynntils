/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.map.configs;

import com.wynntils.Reference;
import com.wynntils.core.framework.instances.Module;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.core.framework.rendering.colors.CustomColor;
import com.wynntils.core.framework.rendering.colors.MinecraftChatColors;
import com.wynntils.core.framework.settings.annotations.Setting;
import com.wynntils.core.framework.settings.annotations.SettingsInfo;
import com.wynntils.core.framework.settings.instances.SettingsClass;
import com.wynntils.modules.map.instances.PathWaypointProfile;
import com.wynntils.modules.map.instances.WaypointProfile;
import com.wynntils.modules.map.overlays.MiniMapOverlay;
import com.wynntils.modules.map.overlays.objects.MapApiIcon;
import com.wynntils.modules.map.overlays.objects.MapPathWaypointIcon;
import com.wynntils.modules.map.overlays.objects.MapWaypointIcon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@SettingsInfo(name = "map", displayPath = "Map")
public class MapConfig extends SettingsClass {
    public static MapConfig INSTANCE;

    @Setting(displayName = "Show Compass Beam", description = "Should a beacon beam be displayed at your compass position?", order = 0)
    public boolean showCompassBeam = true;

    @Setting(displayName = "Show Compass Directions", description = "Should the cardinal directions (N, E, S, W) been displayed on the minimap", order = 1)
    public boolean showCompass = true;

    @Setting(displayName = "Enable Minimap", description = "Should a minimap be displayed?", order = 2)
    public boolean enabled = true;

    @Setting(displayName = "Minimap Shape", description = "Should the minimap be a square or a circle?", order = 3)
    public MapFormat mapFormat = MapFormat.CIRCLE;

    @Setting(displayName = "Minimap Rotation", description = "Should the minimap be locked facing north or rotate based on the direction you're facing?", order = 4)
    public boolean followPlayerRotation = true;

    @Setting(displayName = "Minimap Size", description = "How large should the minimap be?", order = 5)
    @Setting.Limitations.IntLimit(min = 75, max = 200)
    public int mapSize = 100;

    @Setting(displayName = "Minimap Coordinates", description = "Should your coordinates be displayed below the minimap?", order = 6)
    public boolean showCoords = false;

    @Setting(displayName = "Display Only North", description = "Should only north be displayed on the minimap?", order = 7)
    public boolean northOnly = false;

    @Setting(displayName = "Display Minimap Icons", description = "Should map icons be displayed on the minimap?", order = 8)
    public boolean minimapIcons = true;

    @Setting(displayName = "Hide Completed Quest Icons", description = "Should map icons for completed quests be hidden?", order = 9)
    public boolean hideCompletedQuests = true;

    @Setting(displayName = "Compass Beacon Color", description = "What color should the compass beacon be?", order = 10)
    @Setting.Features.CustomColorFeatures(allowAlpha = true)
    public CustomColor compassBeaconColor = CommonColors.RED;

    @Setting(displayName = "Map Blur", description = "Should the map be rendered using linear textures to avoid aliasing issues?", order = 11)
    public boolean renderUsingLinear = true;

    @Setting(displayName = "Minimap Icons Size", description = "How big should minimap icons be?", order = 12)
    @Setting.Limitations.FloatLimit(min = 0.5f, max = 2f)
    public float minimapIconSizeMultiplier = 1f;

    @Setting(displayName = "Minimap Zoom", description = "How far zoomed out should the minimap be?", order = 13)
    @Setting.Limitations.IntLimit(min = MiniMapOverlay.MIN_ZOOM, max = MiniMapOverlay.MAX_ZOOM, precision = 1)
    public int mapZoom = 30;

    @Setting
    public HashMap<String, Boolean> enabledMapIcons = resetMapIcons(false);

    @Setting
    public HashMap<String, Boolean> enabledMinimapIcons = resetMapIcons(true);

    @SettingsInfo(name = "map_worldmap", displayPath = "Map/World Map")
    public static class WorldMap extends SettingsClass {
        public static WorldMap INSTANCE;

        @Setting(displayName = "Keep Territory Visible", description = "Should territory names always be displayed rather than only when you hold CTRL?")
        public boolean keepTerritoryVisible = false;

        @Setting(displayName = "Territory Names", description = "Should territory names be displayed?")
        public boolean showTerritoryName = false;

        @Setting(displayName = "Territory Guild Tags", description = "Should be guild names be replaced by their guild tags?")
        public boolean useGuildShortNames = true;

        @Setting(displayName = "Territory Colour Transparency", description = "How transparent should the colour of territories be?")
        @Setting.Limitations.FloatLimit(min = 0.1f, max = 1f)
        public float colorAlpha = 0.4f;

        @Setting(displayName = "Show Territory Areas", description = "Should territory rectangles be visible?")
        public boolean territoryArea = true;

        @Setting(displayName = "Show Friends on Map", description = "Should online friends in your world be displayed on your map?")
        public boolean showFriends = true;
    }

    @SettingsInfo(name = "map_textures", displayPath = "Map/Textures")
    public static class Textures extends SettingsClass {
        public static Textures INSTANCE;

        @Setting(displayName = "Minimap Texture Style", description = "What should the texture of the minimap be?", order = 0)
        public TextureType textureType = TextureType.Paper;

        @Setting(displayName = "Pointer Style", description = "What should the texture of the pointer be?", order = 1)
        public PointerType pointerStyle = PointerType.ARROW;

        @Setting(displayName = "Pointer Colour", description = "What should the colour of the pointer be?\n\n§aClick the coloured box to open the colour wheel.", order = 2)
        @Setting.Features.CustomColorFeatures(allowAlpha = true)
        public CustomColor pointerColor = new CustomColor(1, 1, 1, 1);

    }

    @SettingsInfo(name = "waypoints", displayPath = "Map/Waypoints")
    public static class Waypoints extends SettingsClass {
        public static Waypoints INSTANCE;

        // HeyZeer0: this stores all waypoints
        @Setting(upload = true)
        public ArrayList<WaypointProfile> waypoints = new ArrayList<>();

        @Setting(upload = true)
        public ArrayList<PathWaypointProfile> pathWaypoints = new ArrayList<>();


        @Setting(displayName = "Recording Chest Waypoints", description = "Which chest tiers should be recorded as waypoints?\n\n§8Tiers higher than the specified value will also be recorded.", order = 6)
        public ChestTiers chestTiers = ChestTiers.TIER_3;

        public enum ChestTiers {
            TIER_1(4),
            TIER_2(3),
            TIER_3(2),
            TIER_4(1),
            NONE(0);

            private int tierArrayIndex;  // Array starts at 1 :P
            private String[] tiers = new String[]{"IV", "III", "II", "I"};

            ChestTiers(int tierArrayIndex) {
                this.tierArrayIndex = tierArrayIndex;
            }

            public boolean isTierAboveThis(String testTier) {
                ArrayList<String> allowedTiers = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(tiers, 0, tierArrayIndex)));
                return allowedTiers.contains(testTier);
            }
        }

        @Setting(displayName = "Compass Marker", description = "Should a marker appear on the map where the compass is currently pointing towards?")
        public boolean compassMarker = true;

        @Override
        public void saveSettings(Module m) {
            super.saveSettings(m);
            MapWaypointIcon.resetWaypoints();
            MapPathWaypointIcon.resetPathWaypoints();
        }

        @Override
        public void onSettingChanged(String name) {
            super.onSettingChanged(name);
            MapWaypointIcon.resetWaypoints();
            MapPathWaypointIcon.resetPathWaypoints();
        }
    }

    @SettingsInfo(name = "lootrun", displayPath = "Map/Loot Run")
    public static class LootRun extends SettingsClass {
        public static LootRun INSTANCE;

        @Setting(displayName = "Loot Run Path Type", description = "How should paths be drawn?\n\n§8Available options are textures and lines.", order = 1)
        public PathType pathType = PathType.TEXTURED;

        @Setting(displayName = "Loot Run Path Colour", description = "What should the colour of the displayed path be?", order = 2)
        @Setting.Features.CustomColorFeatures(allowAlpha = true)
        public CustomColor activePathColour = MinecraftChatColors.AQUA;

        @Setting(displayName = "Recording Loot Run Path Colour", description = "What should the colour of the currently recording path be?", order = 3)
        @Setting.Features.CustomColorFeatures(allowAlpha = true)
        public CustomColor recordingPathColour = CommonColors.RED;

        public enum PathType {

            TEXTURED,
            LINE

        }

    }

    public enum MapFormat {
        SQUARE, CIRCLE
    }

    public enum TextureType {
        Paper, Wynn, Gilded
    }

    public enum PointerType {

        ARROW(10, 8, 5, 4, 0), CURSOR(8, 7, 4, 3.5f, 8), NARROW(8, 8, 4, 4, 15), ROUND(8, 8, 4, 4, 23), STRAIGHT(6, 8, 3, 4, 31), TRIANGLE(8, 6, 4, 3, 39);

        public float width, height, dWidth, dHeight, yStart;

        PointerType(float width, float height, float dWidth, float dHeight, float yStart) {
            this.width = width; this.height = height; this.dWidth = dWidth; this.dHeight = dHeight; this.yStart = yStart;
        }
    }

    @Override
    public void onSettingChanged(String name) { }

    public static HashMap<String, Boolean> resetMapIcons(boolean forMiniMap) {
        HashMap<String, Boolean> enabledIcons = new HashMap<>();
        for (String icon : new String[]{
            "Dungeons", "Accessory Merchant", "Armour Merchant", "Dungeon Merchant", "Horse Merchant",
            "Key Forge Merchant", "LE Merchant", "Emerald Merchant", "TNT Merchant", "Ore Refinery",
            "Potion Merchant", "Powder Merchant", "Scroll Merchant", "Seasail Merchant", "Weapon Merchant",
            "Blacksmith", "Guild Master", "Item Identifier", "Powder Master", "Fast Travel",
            "Fish Refinery", "Wood Refinery", "Crop Refinery", "Marketplace", "Nether Portal",
            "Light's Secret", "Quests", "Boss Altar"
        }) {
            enabledIcons.put(icon, true);
        }
        for (String icon : new String[]{
            "Mini-Quests", "Runes", "Ultimate Discovery", "Caves", "Grind Spots", "Other Merchants",
            "Art Merchant", "Tool Merchant"
        }) {
            enabledIcons.put(icon, forMiniMap);
        }
        for (String icon : new String[]{
            "Weaponsmithing Station", "Armouring Station", "Alchemism Station",
            "Jeweling Station", "Tailoring Station", "Scribing Station",
            "Cooking Station", "Woodworking Station"
        }) {
            enabledIcons.put(icon, false);
        }

        // Warn if we are either missing some icons in the options
        // or have options that do not have an icon
        if (Reference.developmentEnvironment) {
            for (String icon : MapApiIcon.MAPMARKERNAME_TRANSLATION.values()) {
                if (MapApiIcon.IGNORED_MARKERS.contains(icon)) continue;
                if (!enabledIcons.containsKey(icon)) Reference.LOGGER.warn("Missing option for icon \"" + icon + "\"");
            }
            for (String icon : enabledIcons.keySet()) {
                if (MapApiIcon.IGNORED_MARKERS.contains(icon)) continue;
                if (!MapApiIcon.MAPMARKERNAME_REVERSE_TRANSLATION.containsKey(icon)) Reference.LOGGER.warn("Missing translation for \"" + icon + "\"");
            }
        }

        return enabledIcons;
    }

    @Setting
    public IconTexture iconTexture = IconTexture.Classic;
    public enum IconTexture {
        Classic, Medieval
    }

}
