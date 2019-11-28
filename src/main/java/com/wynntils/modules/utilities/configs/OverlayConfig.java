/*
 *  * Copyright © Wynntils - 2019.
 */

package com.wynntils.modules.utilities.configs;

import com.wynntils.core.framework.rendering.SmartFontRenderer;
import com.wynntils.core.framework.settings.annotations.Setting;
import com.wynntils.core.framework.settings.annotations.SettingsInfo;
import com.wynntils.core.framework.settings.instances.SettingsClass;
import com.wynntils.core.framework.settings.ui.SettingsUI;
import com.wynntils.core.utils.Utils;
import com.wynntils.modules.core.enums.OverlayRotation;
import com.wynntils.modules.utilities.overlays.hud.TerritoryFeedOverlay;
import com.wynntils.webapi.WebManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

@SettingsInfo(name = "overlays", displayPath = "Overlays")
public class OverlayConfig extends SettingsClass {
    public static OverlayConfig INSTANCE;


    @Setting(displayName = "Text Shadow", description = "What should the text shadow look like?")
    public SmartFontRenderer.TextShadow textShadow = SmartFontRenderer.TextShadow.OUTLINE;

    @Setting(displayName = "Action Bar Coordinates", description = "Should the action bar display your coordinates when there is nothing else to show?")
    public boolean actionBarCoordinates = true;

    @Setting(displayName = "Split Coordinates", description = "Should the coordinates be shown separately to the action bar?")
    public boolean splitCoordinates = false;

    @SettingsInfo(name = "health_settings", displayPath = "Overlays/Health")
    public static class Health extends SettingsClass {
        public static Health INSTANCE;

        @Setting(displayName = "Health Bar Width", description = "How wide should the health bar be in pixels?\n\n§8This will be adjusted using Minecraft's scaling.")
        @Setting.Limitations.IntLimit(min = 0, max = 81)
        public int width = 81;

        @Setting(displayName = "Health Bar Orientation", description = "How orientated in degrees should the health bar be?\n\n§8Accompanied text will be removed.")
        public OverlayRotation overlayRotation = OverlayRotation.NORMAL;

        @Setting(displayName = "Low Health Vignette", description = "Should a red vignette be displayed when you're low on health?")
        public boolean healthVignette = true;

        @Setting(displayName = "Low Health Threshold", description = "At what percentage of health should a red vignette be displayed?")
        @Setting.Limitations.IntLimit(min = 0, max = 100)
        public int lowHealthThreshold = 25;

        @Setting(displayName = "Low Health Animation", description = "Which animation should be used for the low health indicator?")
        public HealthVignetteEffect healthVignetteEffect = HealthVignetteEffect.Pulse;

        @Setting(displayName = "Health Texture", description = "What texture should be used for the health bar?")
        public HealthTextures healthTexture = HealthTextures.a;

        @Setting.Limitations.FloatLimit(min = 0f, max = 10f)
        @Setting(displayName = "Animation Speed", description = "How fast should the animation be played?\n\n§8Set this to 0 for it to display instantly.")
        public float animated = 2f;

        @Setting(displayName = "Text Shadow", description = "What should the text shadow look like?")
        public SmartFontRenderer.TextShadow textShadow = SmartFontRenderer.TextShadow.OUTLINE;

        public enum HealthVignetteEffect {
            Pulse,
            Growing,
            Static
        }

        public enum HealthTextures {
            Wynn,
            Grune,
            Aether,
            Skull,
            Skyrim,
            a,
            b,
            c,
            d
            //following the format, to add more textures, register them here with a name and create a special case in the render method
        }

    }

    @SettingsInfo(name = "mana_settings", displayPath = "Overlays/Mana")
    public static class Mana extends SettingsClass {
        public static Mana INSTANCE;

        @Setting(displayName = "Mana Bar Width", description = "How wide should the mana bar be in pixels?\n\n§8This will be adjusted using Minecraft's scaling.")
        @Setting.Limitations.IntLimit(min = 0, max = 81)
        public int width = 81;

        @Setting(displayName = "Mana Bar Orientation", description = "How orientated in degrees should the mana bar be?\n\n§8Accompanied text will be removed.")
        public OverlayRotation overlayRotation = OverlayRotation.NORMAL;

        @Setting(displayName = "Mana Texture", description = "What texture should be used for the mana bar?")
        public ManaTextures manaTexture = ManaTextures.a;

        @Setting.Limitations.FloatLimit(min = 0f, max = 10f)
        @Setting(displayName = "Animation Speed", description = "How fast should the animation be played?\n\n§8Set this to 0 for it to display instantly.")
        public float animated = 2f;

        @Setting(displayName = "Text Shadow", description = "What should the text shadow look like?")
        public SmartFontRenderer.TextShadow textShadow = SmartFontRenderer.TextShadow.OUTLINE;


        public enum ManaTextures {
            Wynn,
            Brune,
            Aether,
            Skull,
            Inverse,
            Skyrim,
            a,
            b,
            c,
            d
            //following the format, to add more textures, register them here with a name and create a special case in the render method
        }

    }

    @SettingsInfo(name = "hotbar_settings", displayPath = "Overlays/Hotbar")
    public static class Hotbar extends SettingsClass {
        public static Hotbar INSTANCE;

        @Setting(displayName = "Hotbar Texture", description = "What texture should be used for the hotbar?")
        public HotbarTextures hotbarTexture = HotbarTextures.Resource_Pack;

        public enum HotbarTextures {
            Resource_Pack,
            Wynn
        }
    }

    @SettingsInfo(name = "toast_settings", displayPath = "Overlays/Toasts")
    public static class ToastsSettings extends SettingsClass {
        public static ToastsSettings INSTANCE;

        @Setting(displayName = "Enable Toast Messages", description = "Should certain messages be displayed in the form of rolling parchment?", order = 0)
        public boolean enableToast = true;

        @Setting(displayName = "Enable Territory Enter Messages", description = "Should a toast be displayed to inform that you are entering a territory?")
        public boolean enableTerritoryEnter = true;

        @Setting(displayName = "Enable Area Discovered Messages", description = "Should a toast be displayed to inform that you have discovered an area?")
        public boolean enableAreaDiscovered = true;

        @Setting(displayName = "Enable Quest Completed Messages", description = "Should a toast be displayed to inform that you have completed a quest?")
        public boolean enableQuestCompleted = true;

        @Setting(displayName = "Enable Discovery Found Messages", description = "Should a toast be displayed to inform that you have found a secret discovery?")
        public boolean enableDiscovery = true;

        @Setting(displayName = "Flip Toast Messages", description = "Should a toast display from the left to right?\n\n§8Some visual glitches may occur if Toast overlay isn't moved to either side of your screen.")
        public boolean flipToast = false;
    }

    @SettingsInfo(name = "exp_settings", displayPath = "Overlays/Experience")
    public static class Exp extends SettingsClass {
        public static Exp INSTANCE;

        @Setting(displayName = "EXP Texture", description = "What texture should be used for the EXP bar?")
        public expTextures expTexture = expTextures.a;

        @Setting.Limitations.FloatLimit(min = 0f, max = 10f)
        @Setting(displayName = "Animation Speed", description = "How fast should the animation be played?\n\n§8Set this to 0 for it to display instantly.")
        public float animated = 2f;

        @Setting(displayName = "Text Shadow", description = "What should the text shadow look like?")
        public SmartFontRenderer.TextShadow textShadow = SmartFontRenderer.TextShadow.OUTLINE;


        public enum expTextures {
            Wynn,
            Liquid,
            Emerald,
            a,
            b,
            c
            //following the format, to add more textures, register them here with a name and create a special case in the render method
        }

    }

    @SettingsInfo(name = "bubbles_settings", displayPath = "Overlays/Bubbles")
    public static class Bubbles extends SettingsClass {
        public static Bubbles INSTANCE;

        @Setting(displayName = "Bubbles Texture", description = "What texture should be used for the EXP bar when it acts as the air meter?")
        public BubbleTexture bubblesTexture = BubbleTexture.a;

        @Setting.Limitations.FloatLimit(min = 0f, max = 10f)
        @Setting(displayName = "Animation Speed", description = "How fast should the animation be played?\n\n§8Set this to 0 for it to display instantly.")
        public float animated = 2f;

        @Setting(displayName = "Text Shadow", description = "What should the text shadow look like?")
        public SmartFontRenderer.TextShadow textShadow = SmartFontRenderer.TextShadow.OUTLINE;

        @Setting(displayName = "Bubble Vignette", description = "Should a blue vignette be displayed when you're underwater?")
        public boolean drowningVignette = true;

        public enum BubbleTexture {
            Wynn,
            Liquid,
            Saphire,
            a,
            b,
            c
        }
    }

    @SettingsInfo(name = "leveling_settings", displayPath = "Overlays/Leveling")
    public static class Leveling extends SettingsClass {
        public static Leveling INSTANCE;

        @Setting.Features.StringParameters(parameters = {"actual", "max", "percent", "needed", "actualg", "maxg", "neededg", "curlvl", "nextlvl"})
        @Setting(displayName = "Current Text", description = "How should the leveling text be displayed?")
        @Setting.Limitations.StringLimit(maxLength = 200)
        public String levelingText = TextFormatting.GREEN + "(%actual%/%max%) " + TextFormatting.GOLD + "%percent%%";

        @Setting(displayName = "Text Shadow", description = "What should the text shadow look like?")
        public SmartFontRenderer.TextShadow textShadow = SmartFontRenderer.TextShadow.OUTLINE;


    }

    @SettingsInfo(name = "game_update_settings", displayPath = "Overlays/Update Ticker")
    public static class GameUpdate extends SettingsClass {
        public static GameUpdate INSTANCE;

        // Default settings designed for large ui scale @ 1080p
        // I personally use ui scale normal - but this works fine with that too

        @Setting(displayName = "Message Limit", description = "What should the maximum amount of ticker messages displayed in the game-update-list be?")
        @Setting.Limitations.IntLimit(min = 1, max = 20)
        public int messageLimit = 5;

        @Setting(displayName = "Align Text - Right", description = "Should the text align along the right side?")
        public boolean rightToLeft = true;

        @Setting(displayName = "Message Expiry Time", description = "How long (in seconds) should a ticker message remain on the screen?")
        @Setting.Limitations.FloatLimit(min = 0.2f, max = 20f, precision = 0.2f)
        public float messageTimeLimit = 10f;

        @Setting(displayName = "Message Fadeout Animation", description = "How long should the fadeout animation be played?")
        @Setting.Limitations.FloatLimit(min = 10f, max = 60f, precision = 1f)
        public float messageFadeOut = 30f;

        @Setting(displayName = "Invert Growth", description = "Should the way ticker messages appear be inverted?")
        public boolean invertGrowth = true;

        @Setting(displayName = "Max Message Length", description = "What should the maximum length of messages in the game-update-ticker be?\n\n§8Messages longer than this set value will be truncated. Set this to 0 for no maximum length.")
        @Setting.Limitations.IntLimit(min = 0, max = 100)
        public int messageMaxLength = 0;

        @Setting(displayName = "Text Shadow", description = "What should the text shadow look like?")
        public SmartFontRenderer.TextShadow textShadow = SmartFontRenderer.TextShadow.OUTLINE;

        @Setting(displayName = "New Message Override", description = "Should new messages force out the oldest previous messages?\n\n§8If disabled, ticker messages will be queued and appear when a previous message disappears.")
        public boolean overrideNewMessages = true;

        @SettingsInfo(name = "game_update_exp_settings", displayPath = "Overlays/Update Ticker/Experience")
        public static class GameUpdateEXPMessages extends SettingsClass {
            public static GameUpdateEXPMessages INSTANCE;

            @Setting(displayName = "Enable EXP Messages", description = "Should EXP messages be displayed in the game-update-ticker?", order = 0)
            public boolean enabled = true;

            @Setting(displayName = "EXP Message Update Rate", description = "How often should the EXP change messages (in seconds) be added to the game update ticker?")
            @Setting.Limitations.FloatLimit(min = 0.2f, max = 10f, precision = 0.2f)
            public float expUpdateRate = 1f;

            @Setting(displayName = "EXP Message Format", description = "How should the format of EXP messages be displayed?")
            @Setting.Features.StringParameters(parameters = {"xo", "xn", "xc", "po", "pn", "pc"})
            @Setting.Limitations.StringLimit(maxLength = 100)
            public String expMessageFormat = TextFormatting.DARK_GREEN + "+%xc%XP (" + TextFormatting.GOLD + "+%pc%%" + TextFormatting.DARK_GREEN + ")";
        }

        @SettingsInfo(name = "game_update_inventory_settings", displayPath = "Overlays/Update Ticker/Inventory")
        public static class GameUpdateInventoryMessages extends SettingsClass {
            public static GameUpdateInventoryMessages INSTANCE;

            @Setting(displayName = "Enable Full Inventory Messages", description = "Should messages be displayed in the game-update-ticker when your inventory is full?")
            public boolean enabled = false;

            @Setting(displayName = "Full Inventory Update Rate", description = "How often should the inventory full message (in seconds) be displayed in the game update ticker?")
            @Setting.Limitations.FloatLimit(min = 5f, max = 60f, precision = 5f)
            public float inventoryUpdateRate = 10f;

            @Setting(displayName = "Inventory Full Message Format", description = "What message should be displayed when your inventory is full?")
            @Setting.Limitations.StringLimit(maxLength = 100)
            public String inventoryMessageFormat = TextFormatting.DARK_RED + "Your inventory is full";
        }

        @SettingsInfo(name = "game_update_redirect_settings", displayPath = "Overlays/Update Ticker/Redirect Messages")
        public static class RedirectSystemMessages extends SettingsClass {
            public static RedirectSystemMessages INSTANCE;

            @Setting(displayName = "Redirect Combat Messages", description = "Should combat chat messages be redirected to the game update ticker?")
            public boolean redirectCombat = true;

            @Setting(displayName = "Redirect Horse Messages", description = "Should messages related to your horse be redirected to the game update ticker?")
            public boolean redirectHorse = true;

            @Setting(displayName = "Redirect Local Login Messages", description = "Should local login messages (for people with ranks) be redirected to the game update ticker?")
            public boolean redirectLoginLocal = true;

            @Setting(displayName = "Redirect Friend Login Messages", description = "Should login messages for friends be redirected to the game-update-ticker?")
            public boolean redirectLoginFriend = true;

            @Setting(displayName = "Redirect Guild Login Messages", description = "Should login messages for guild members be redirected to the game-update-ticker?")
            public boolean redirectLoginGuild = true;

            @Setting(displayName = "Redirect Merchant Messages", description = "Should item buyer and identifier messages be redirected to the game-update-ticker?")
            public boolean redirectMerchants = true;

            @Setting(displayName = "Redirect Other Messages", description = "Should skill points, price of identifying items, and other users' level up messages be redirected to the game-update-ticker?")
            public boolean redirectOther = true;

            @Setting(displayName = "Redirect Server Status", description = "Should server shutdown messages be redirected to the game-update-ticker?")
            public boolean redirectServer = true;

            @Setting(displayName = "Redirect Quest Messages", description = "Should messages relating to the progress of a quest be redirected to the game-update-ticker?")
            public boolean redirectQuest = true;

            @Setting(displayName = "Redirect Soul Point Messages", description = "Should messages about regaining soul points be redirected to the game-update-ticker?")
            public boolean redirectSoulPoint = true;
        }

        @SettingsInfo(name = "game_update_territory_settings", displayPath = "Overlays/Update Ticker/Territory Change")
        public static class TerritoryChangeMessages extends SettingsClass {
            public static TerritoryChangeMessages INSTANCE;

            @Setting(displayName = "Enable Territory Change", description = "Should territory change messages be displayed in the game-update-ticker?")
            public boolean enabled = false;

            @Setting(displayName = "Enable Territory Enter", description = "Should territory enter messages be displayed in the game-update-ticker?")
            public boolean enter = true;

            @Setting(displayName = "Enable Territory Leave", description = "Should territory leave messages be displayed in the game-update-ticker?")
            public boolean leave = false;

            @Setting(displayName = "Enable Music Change", description = "Should music change messages be displayed in the game-update-ticker?\n\n§8This has no effect if the Music module is disabled.")
            public boolean musicChange = true;

            @Setting(displayName = "Territory Enter Format", description = "How should the format of the territory enter ticker messages be displayed?")
            @Setting.Features.StringParameters(parameters = {"t"})
            @Setting.Limitations.StringLimit(maxLength = 100)
            public String territoryEnterFormat = TextFormatting.GRAY + "Now Entering [%t%]";

            @Setting(displayName = "Territory Leave Format", description = "How should the format of the territory leave ticker messages be displayed?")
            @Setting.Features.StringParameters(parameters = {"t"})
            @Setting.Limitations.StringLimit(maxLength = 100)
            public String territoryLeaveFormat = TextFormatting.GRAY + "Now Leaving [%t%]";

            @Setting(displayName = "Music Change Format", description = "How should the format of the music change ticker messages be displayed?")
            @Setting.Features.StringParameters(parameters = {"np"})
            @Setting.Limitations.StringLimit(maxLength = 100)
            public String musicChangeFormat = TextFormatting.GRAY + "♫ %np%";
        }
    }

    @SettingsInfo(name = "war_timer_settings", displayPath = "Overlays/War Timer")
    public static class WarTimer extends SettingsClass {
        public static WarTimer INSTANCE;

        @Setting(displayName = "Text Shadow", description = "What should the text shadow look like?")
        public SmartFontRenderer.TextShadow textShadow = SmartFontRenderer.TextShadow.OUTLINE;
    }

    @SettingsInfo(name = "territory_feed_settings", displayPath = "Overlays/Territory Feed")
    public static class TerritoryFeed extends SettingsClass {
        public static TerritoryFeed INSTANCE;

        @Setting(displayName = "Territory Feed" ,description = "Should the territory feed be displayed?", order = 0)
        public boolean enabled = true;

        @Setting(displayName = "Animation Length", description = "How long (in seconds) should messages on the territory feed be displayed?")
        @Setting.Limitations.IntLimit(min = 1, max = 60)
        public int animationLength = 20;

        @Setting(displayName = "Territory Messages Mode", description = "What messages should be displayed in the territory feed?\n\n" +
                "Normal: Display all territory messages.\n\n" +
                "Distinguish Own Guild: Display all territory messages, but messages relating to your guild will be displayed in different colors. (§2Gained territory §r& §4lost territory§r)\n\n" +
                "Only Own Guild: Display only territory messages that relate to your guild.")
        public TerritoryFeedDisplayMode displayMode = TerritoryFeedDisplayMode.DISTINGUISH_OWN_GUILD;

        @Setting(displayName = "Shorten Messages", description = "Should territory feed messages be shortened?", order = 1)
        public boolean shortMessages = false;

        @Setting(displayName = "Use Guild Tags", description = "Should guild tags be displayed rather than names?", order = 2)
        public boolean useTag = false;

        @Override
        public void onSettingChanged(String name) {
            if (name.equals("enabled")) {
                WebManager.updateTerritoryThreadStatus(enabled);
                TerritoryFeedOverlay.clearQueue();
            }
        }

        public enum TerritoryFeedDisplayMode {
            NORMAL,
            DISTINGUISH_OWN_GUILD,
            ONLY_OWN_GUILD
        }
    }

    @SettingsInfo(name = "info_overlays_settings", displayPath = "Overlays/Info")
    public static class InfoOverlays extends SettingsClass {
        public static InfoOverlays INSTANCE;

        @Setting.Features.StringParameters(parameters = {"x", "y", "z", "dir", "fps", "class", "lvl"})
        @Setting(displayName = "Info 1 text", description = "What should the first box display?", order = 1)
        @Setting.Limitations.StringLimit(maxLength = 200)
        public String info1Format = "";

        @Setting.Features.StringParameters(parameters = {"x", "y", "z", "dir", "fps", "class", "lvl"})
        @Setting(displayName = "Info 2 text", description = "What should the second box display?", order = 2)
        @Setting.Limitations.StringLimit(maxLength = 200)
        public String info2Format = "";

        @Setting.Features.StringParameters(parameters = {"x", "y", "z", "dir", "fps", "class", "lvl"})
        @Setting(displayName = "Info 3 text", description = "What should the third box display?", order = 3)
        @Setting.Limitations.StringLimit(maxLength = 200)
        public String info3Format = "";

        @Setting.Features.StringParameters(parameters = {"x", "y", "z", "dir", "fps", "class", "lvl"})
        @Setting(displayName = "Info 4 text", description = "What should the fourth box display?", order = 4)
        @Setting.Limitations.StringLimit(maxLength = 200)
        public String info4Format = "";

        @Setting(displayName = "Presets", description = "Copies various formats to the clipboard (Paste to one of the fields above)", upload = false, order = 5)
        public Presets preset = Presets.CLICK_ME;

        @Setting(displayName = "Background Opacity", description = "How dark should the background box be (% opacity)?", order = 6)
        @Setting.Limitations.IntLimit(min = 0, max = 100)
        public int opacity = 0;

        @Setting(displayName = "Text Shadow", description = "What should the text shadow look like?")
        public SmartFontRenderer.TextShadow textShadow = SmartFontRenderer.TextShadow.OUTLINE;

        @Override
        public void onSettingChanged(String name) {
            if ("preset".equals(name)) {
                if (!(Minecraft.getMinecraft().currentScreen instanceof SettingsUI)) {
                    preset = Presets.CLICK_ME;
                } else if (preset.value != null) {
                    Utils.copyToClipboard(preset.value);
                }
            }
        }

        public enum Presets {
            CLICK_ME("Click me to copy to clipboard", null),
            COORDS("Coordinates", "%x% %z% (%y%)"),
            ACTIONBAR_COORDS("Actionbar Coordinates", "&7%x% &a%dir% &7%z%"),
            FPS("FPS Counter", "FPS: %fps%"),
            CLASS("Class", "%Class%\\nLevel %lvl%"),
            LOCATION("Location", "[%world%] %location%"),
            BALANCE("Balance", "%le%\\L\\E %blocks%\\E\\B %emeralds%\\E (%money%\\E)"),
            UNPROCESSED_MATERIALS("Unprocessed Materials", "Unprocessed materials: %unprocessed% / %unprocessed_max%"),
            MEMORY_USAGE("Memory usage", "%mem_pct%\\% %mem_used%/%mem_max%MB"),
            PING("Ping", "%ping%ms/15s");

            public final String displayName;
            public final String value;

            Presets(String displayName, String value) {
                this.displayName = displayName;
                this.value = value;
            }
        }
    }

    @SettingsInfo(name = "player_info_settings", displayPath = "Overlays/Player Info")
    public static class PlayerInfo extends SettingsClass {
        public static PlayerInfo INSTANCE;

        @Setting(displayName = "Replace Vanilla Player List", description = "Should the vanilla player list be replaced with Wynntils' custom list?", order = 1)
        public boolean replaceVanilla = true;

        @Setting(displayName = "Player List Transparency", description = "How transparent should the custom player list be?", order = 2)
        @Setting.Limitations.FloatLimit(min = .0f, max = 1f)
        public float backgroundAlpha = 0.3f;

    }
}
