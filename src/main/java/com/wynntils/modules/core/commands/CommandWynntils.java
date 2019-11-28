/*
 *  * Copyright © Wynntils - 2019.
 */

package com.wynntils.modules.core.commands;

import com.wynntils.Reference;
import com.wynntils.core.utils.Delay;
import com.wynntils.core.utils.Location;
import com.wynntils.core.utils.Utils;
import com.wynntils.modules.chat.ChatModule;
import com.wynntils.modules.chat.configs.ChatConfig;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.core.enums.UpdateStream;
import com.wynntils.modules.core.managers.CompassManager;
import com.wynntils.modules.core.overlays.ui.ChangelogUI;
import com.wynntils.modules.map.overlays.ui.MainWorldMapUI;
import com.wynntils.modules.questbook.managers.QuestManager;
import com.wynntils.modules.utilities.managers.KeyManager;
import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.WebReader;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.IClientCommand;

import java.util.Collections;
import java.util.List;

public class CommandWynntils extends CommandBase implements IClientCommand {

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }

    @Override
    public String getName() {
        return "wynntils";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/wynntils <command>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length <= 0) {
            execute(server, sender, new String[]{"help"});
            return;
        }

        switch (String.join("", args).toLowerCase()) {
            case "donate":
                TextComponentString c = new TextComponentString("You can donate to Wynntils at: ");
                c.getStyle().setColor(TextFormatting.AQUA);
                TextComponentString url = new TextComponentString("https://www.patreon.com/Wynntils");
                url.getStyle()
                        .setColor(TextFormatting.LIGHT_PURPLE)
                        .setUnderlined(true)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.patreon.com/Wynntils"))
                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click here to open in your browser.")));

                sender.sendMessage(c.appendSibling(url));
                break;
            case "help":
                TextComponentString text = new TextComponentString("");
                text.getStyle().setColor(TextFormatting.GOLD);
                text.appendText("Wynntils' command list: ");
                text.appendText("\n");
                addCommandDescription(text, "-wynntils", " help", "This shows a list of all available commands for Wynntils.");
                text.appendText("\n");
                addCommandDescription(text, "-wynntils", " discord", "This provides you with an invite to our Discord server.");
                text.appendText("\n");
                addCommandDescription(text, "-wynntils", " version", "This shows the installed Wynntils version.");
                text.appendText("\n");
                addCommandDescription(text, "-wynntils", " changelog [major]", "This shows the latest changelog of your installed version.");
                text.appendText("\n");
                addCommandDescription(text, "-wynntils", " reloadapi", "This reloads all API data.");
                text.appendText("\n");
                addCommandDescription(text, "-wynntils", " donate", "This provides our Patreon link.");
                text.appendText("\n");
                addCommandDescription(text, "-", "token", "This provides a clickable token for you to create a Wynntils account to manage your cosmetics.");
                text.appendText("\n");
                addCommandDescription(text, "-", "forceupdate", "This downloads and installs the latest successful build.");
                text.appendText("\n");
                addCommandDescription(text, "-", "compass", "This makes your compass point towards an x and z or a direction (e.g. north, SE).");
                text.appendText("\n");
                addCommandDescription(text, "-", "territory", "This makes your compass point towards a specified territory.");
                sender.sendMessage(text);
                break;
                /*Since we combine all arguments, to get the second page of help the case could be "help2" for "/wynntils help 2".*/
            case "discord":
                TextComponentString msg = new TextComponentString("You're welcome to join our Discord server at:\n");
                msg.getStyle().setColor(TextFormatting.GOLD);
                WebReader apiUrls = WebManager.getApiUrls();
                TextComponentString link = new TextComponentString(apiUrls == null ? "<Wynntils servers are down>" : apiUrls.get("DiscordInvite"));
                link.getStyle()
                        .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, WebManager.getApiUrls().get("DiscordInvite")))
                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click here to join our Discord server.")))
                        .setColor(TextFormatting.DARK_AQUA);
                sender.sendMessage(msg.appendSibling(link));
                break;
            case "version":
                handleModVersion(sender);
                break;
            case "reloadapi":
                WebManager.reset();
                WebManager.setupWebApi(false);
                break;
            case "changelog":
                new Delay(() -> {
                    boolean major = CoreDBConfig.INSTANCE.updateStream == UpdateStream.STABLE;
                    Minecraft.getMinecraft().displayGuiScreen(new ChangelogUI(WebManager.getChangelog(major), major));
                }, 1);
                break;
            case "changelogmajor":
                new Delay(() -> {
                    Minecraft.getMinecraft().displayGuiScreen(new ChangelogUI(WebManager.getChangelog(true), true));
                }, 1);

                break;
            case "debug":
                if(!Reference.developmentEnvironment) {
                    ITextComponent message = new TextComponentString(TextFormatting.RED + "You can't use this command outside a development environment");

                    Minecraft.getMinecraft().player.sendMessage(message);
                    return;
                }

                QuestManager.requestFullSearch();
                break;
            case "hidehoveritemtext":
                ChatConfig.INSTANCE.heldItemChat = false;
                ChatConfig.INSTANCE.saveSettings(ChatModule.getModule());

                ITextComponent message = new TextComponentString("Enable §bMod options > Chat > Held Item Chat Messages§r to undo (or click this)");
                message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wynntils showhoveritemtext"));
                Minecraft.getMinecraft().player.sendMessage(message);
                break;
            case "showhoveritemtext":
                ChatConfig.INSTANCE.heldItemChat = true;
                ChatConfig.INSTANCE.saveSettings(ChatModule.getModule());
                break;
            case "openmapatcompass":
                Location compass = CompassManager.getCompassLocation();
                if (compass == null) {
                    Utils.displayGuiScreen(new MainWorldMapUI());
                    break;
                }

                Utils.displayGuiScreen(new MainWorldMapUI((float) compass.getX(), (float) compass.getZ()));
                break;
            default:
                execute(server, sender, new String[] {"help"});
        }
    }

    private void addCommandDescription(ITextComponent text, String prefix, String name, String description) {
        TextComponentString prefixText = new TextComponentString(prefix);
        prefixText.getStyle().setColor(TextFormatting.DARK_GRAY);
        text.appendSibling(prefixText);

        TextComponentString nameText = new TextComponentString(name);
        nameText.getStyle().setColor(TextFormatting.RED);
        text.appendSibling(nameText);

        text.appendText(" ");

        TextComponentString descriptionText = new TextComponentString(description);
        descriptionText.getStyle().setColor(TextFormatting.GRAY);
        text.appendSibling(descriptionText);
    }

    private void handleModVersion(ICommandSender sender) {
        if (Reference.developmentEnvironment) {
            TextComponentString text = new TextComponentString("Wynntils is running in a development environment.");
            text.getStyle().setColor(TextFormatting.GOLD);
            sender.sendMessage(text);
            return;
        }

        TextComponentString releaseStreamText = null;
        TextComponentString buildText = null;
        if (CoreDBConfig.INSTANCE.updateStream == UpdateStream.STABLE) {
            releaseStreamText = new TextComponentString("You are using Stable release stream: ");
            buildText = new TextComponentString("Version " + Reference.VERSION);
        } else {
            releaseStreamText = new TextComponentString("You are using Cutting Edge release stream: ");
            if (Reference.BUILD_NUMBER == -1) {
                buildText = new TextComponentString("Unknown Build");
            } else {
                buildText = new TextComponentString("Build " + Reference.BUILD_NUMBER);
            }
        }
        releaseStreamText.getStyle().setColor(TextFormatting.GOLD);
        buildText.getStyle().setColor(TextFormatting.YELLOW);
        TextComponentString versionText = new TextComponentString("");
        versionText.appendSibling(releaseStreamText);
        versionText.appendSibling(buildText);

        TextComponentString updateCheckText = null;
        TextFormatting color = null;
        if (WebManager.getUpdate().updateCheckFailed()) {
            updateCheckText = new TextComponentString("Wynntils failed to check for updates. Press " + KeyManager.getCheckForUpdatesKey().getKeyBinding().getDisplayName() + " to try again.");
            color = TextFormatting.DARK_RED;
        } else if (WebManager.getUpdate().hasUpdate()) {
            updateCheckText = new TextComponentString("Wynntils is currently outdated. Press " + KeyManager.getCheckForUpdatesKey().getKeyBinding().getDisplayName() + " to update now.");
            color = TextFormatting.DARK_RED;
        } else {
            updateCheckText = new TextComponentString("Wynntils was up-to-date when last checked. Press " + KeyManager.getCheckForUpdatesKey().getKeyBinding().getDisplayName() + " to check for updates.");
            color = TextFormatting.DARK_GREEN;
        }
        updateCheckText.getStyle().setColor(color);
        sender.sendMessage(updateCheckText);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "help", "discord", "version", "changelog", "reloadapi", "donate");
        } else if (args.length == 2) {
            if (args[0].equals("changelog")) {
                return getListOfStringsMatchingLastWord(args, "major");
            }
        }
        return Collections.emptyList();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
