package com.wynntils.modules.core.commands;

import com.google.common.collect.Lists;
import com.wynntils.Reference;
import com.wynntils.core.utils.Utils;
import com.wynntils.modules.chat.overlays.ChatOverlay;
import com.wynntils.webapi.WebManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.IClientCommand;

import java.util.*;


public class CommandServer extends CommandBase implements IClientCommand {
    private List<String> serverTypes = Lists.newArrayList("WC", "lobby", "GM", "WAR", "HB");

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }

    @Override
    public String getName() {
        return "s";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/s <command> [options]\n\ncommands:\nl,ls,list | list avaiable servers\ni,info | get info about a server\n\nmore detailed help:\n/s <command> help";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (Reference.onServer) {
            if (args.length >= 1) {
                //String option = args[0];
                switch (args[0].toLowerCase()) {
                    case "list":
                    case "ls":
                    case "l":
                        serverList(server, sender, Arrays.copyOfRange(args, 1, args.length));
                        break;
                    case "info":
                    case "i":
                        serverInfo(server, sender, Arrays.copyOfRange(args, 1, args.length));
                        break;
                    default:
                        throw new CommandException(getUsage(sender));
                }
            } else {
                throw new CommandException(getUsage(sender));
            }
        }
    }

    private void serverList(MinecraftServer server, ICommandSender sender, String[] args) {
        List<String> options = new ArrayList<>();
        String selectedType = null;

        for (String arg : args) {
            argparser:
            for (String type : serverTypes) {
                if (arg.equalsIgnoreCase(type)) {
                    selectedType = type;
                    break argparser;
                }
            }
            switch(arg.toLowerCase()) {
                case "group":
                case "g":
                    options.add("group");
                    break;
                case "sort":
                case "s":
                    options.add("sort");
                    options.add("group");
                    break;
                case "count":
                case "c":
                    options.add("count");
                    break;
                case "help":
                case "h":
                    options.add("help");
                    break;
            }
        }

        TextComponentString text;
        if (options.contains("help")) {
            text = new TextComponentString(
                    "Usage: /s list [type] [options]\n order of types and options does not matter\nDefault: print all servers oldest to new\n\ntypes:\n");
            for (String type : serverTypes) {
                text.appendText(String.format("  %s\n", type));
            }
            text.appendText("options:\n");
            text.appendText("  g, group : group servers by type\n");
            text.appendText("  s, sort : sort servers alphabetically, sets group flag\n");
            text.appendText("  c, count : print amount of online servers\n");
            text.appendText("  h, help : this help\n");
            sender.sendMessage(text);
            return;
        }

        int messageId = Utils.getRandom().nextInt(Integer.MAX_VALUE);
        ChatOverlay.getChat().printUnloggedChatMessage(new TextComponentString(TextFormatting.GRAY + "Calculating Servers..."), messageId);

        String finalSelectedType = selectedType;
        Utils.runAsync(() -> {
            try{
                HashMap<String, ArrayList<String>> onlinePlayers = WebManager.getOnlinePlayers();
                if(finalSelectedType == null) {
                    ChatOverlay.getChat().printUnloggedChatMessage(
                            getFilteredServerList(onlinePlayers, "", options), messageId
                    ); //updates the message
                    return;
                }

                if(options.contains("group")) {
                    TextComponentString toEdit = new TextComponentString("Available servers" +
                            (options.contains("count") ? String.format(" (%d)", onlinePlayers.size()): "") + ":\n");

                    for (String type : serverTypes.subList(0, serverTypes.size() - 1)) {
                        toEdit.appendSibling(getFilteredServerList(onlinePlayers, type, options));
                        toEdit.appendText("\n");
                    }
                    toEdit.appendSibling(getFilteredServerList(onlinePlayers, serverTypes.get(serverTypes.size() - 1), options));

                    ChatOverlay.getChat().printUnloggedChatMessage(toEdit, messageId); //updates the message
                    return;
                }

                ChatOverlay.getChat().printUnloggedChatMessage(
                        getFilteredServerList(onlinePlayers, finalSelectedType, options), messageId
                ); //updates the message
            }catch (Exception ex) {
                ChatOverlay.getChat().printUnloggedChatMessage(
                        new TextComponentString(
                                TextFormatting.RED +
                                "An error occurred while trying to get the servers!"
                        ).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new TextComponentString(TextFormatting.RED + ex.getMessage())
                                ))),
                        messageId
                );

                ex.printStackTrace();
            }
        });
    }

    private void serverInfo(MinecraftServer server, ICommandSender sender, String[] args) {
        int messageId = Utils.getRandom().nextInt(Integer.MAX_VALUE);
        ChatOverlay.getChat().printUnloggedChatMessage(
                new TextComponentString(TextFormatting.GRAY + "Calculating Server Information..."
                ), messageId);

        Utils.runAsync(() -> {
            try {
                HashMap<String, ArrayList<String>> onlinePlayers = WebManager.getOnlinePlayers();
                if (args.length >= 1) {
                    for (String serverName : onlinePlayers.keySet()) {
                        if (args[0].equalsIgnoreCase(serverName)) {
                            TextComponentString text = new TextComponentString(String.format("%s: ", serverName));
                            TextComponentString playerText = new TextComponentString("");

                            ArrayList<String> players = onlinePlayers.get(serverName);

                            for (String player : players.subList(0, players.size() - 1)) {
                                playerText.appendText(String.format("%s, ", player));
                            }
                            playerText.appendText(players.get(players.size() - 1));
                            playerText.getStyle().setColor(TextFormatting.GRAY);
                            text.appendSibling(playerText);

                            text.appendText("\nTotal online players: ");
                            TextComponentString playerCountText = new TextComponentString(String.valueOf(players.size()));
                            playerCountText.getStyle().setColor(TextFormatting.GRAY);
                            text.appendSibling(playerCountText);

                            ChatOverlay.getChat().printUnloggedChatMessage(text, messageId);
                            return;
                        }
                    }
                    ChatOverlay.getChat().printUnloggedChatMessage(
                            new TextComponentString(String.format("Unknown server ID: %s", args[0])), messageId);
                } else { //args.length == 0
                    ChatOverlay.getChat().printUnloggedChatMessage(
                            new TextComponentString("Usage: /s info <serverID>"), messageId);
                }
            } catch (Exception e) {
                ChatOverlay.getChat().printUnloggedChatMessage(
                        new TextComponentString(
                                TextFormatting.RED +
                                        "An error occurred while trying to get the servers!"
                        ).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new TextComponentString(TextFormatting.RED + e.getMessage())
                        ))),
                        messageId
                );

                e.printStackTrace();
            }
        });
    }

    private TextComponentString getFilteredServerList(HashMap<String, ArrayList<String>> onlinePlayers,
                                                       String filter,
                                                       List<String> options) {
        TextComponentString text = new TextComponentString("");
        TextComponentString serverListText = new TextComponentString("");

        int serverCount = 0;
        for (String serverName : options.contains("sort") ? new TreeSet<>(onlinePlayers.keySet()) : onlinePlayers.keySet()) {
            if (serverName.toLowerCase().contains(filter.toLowerCase())) {
                TextComponentString serverText = new TextComponentString(String.format("%s ", serverName));
                if (onlinePlayers.get(serverName).size() >= 48) {serverText.getStyle().setColor(TextFormatting.RED);}
                else {serverText.getStyle().setColor(TextFormatting.GREEN);}
                serverListText.appendSibling(serverText);
                serverCount++;
            }
        }

        if (filter.equals("")) {
            text.appendText("Available servers" +
                    (options.contains("count") ? String.format(" (%d)", onlinePlayers.size()): "") + ":\n");
        } else if (options.contains("count")) {
            text.appendText(String.format("%s (%d):\n", filter, serverCount));
        } else {
            text.appendText(String.format("%s:\n", filter));
        }

        if(serverCount == 0) {
            serverListText.appendText("none");
            serverListText.getStyle().setColor(TextFormatting.DARK_GRAY);
            text.getStyle().setColor(TextFormatting.GRAY);
        }

        text.appendSibling(serverListText);

        return text;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
