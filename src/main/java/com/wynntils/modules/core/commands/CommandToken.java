/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.core.commands;

import com.wynntils.webapi.WebManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.IClientCommand;

public class CommandToken extends CommandBase implements IClientCommand {


    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }

    @Override
    public String getName() {
        return "token";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Returns your Wynntils auth token";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (WebManager.getAccount().getToken() != null) {
            TextComponentString text = new TextComponentString("");
            text.appendText("Wynntils Token ");
            text.getStyle().setColor(TextFormatting.AQUA);

            TextComponentString token = new TextComponentString(WebManager.getAccount().getToken());

            token.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                    "https://account.wynntils.com/register.php?token=" + WebManager.getAccount().getToken()));
            token.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new TextComponentString("Click me to register an account.")));

            token.getStyle().setColor(TextFormatting.DARK_AQUA);
            token.getStyle().setUnderlined(true);
            text.appendSibling(token);

            sender.sendMessage(text);
            return;
        }

        TextComponentString text = new TextComponentString("Error when getting token, try restarting your client");
        text.getStyle().setColor(TextFormatting.RED);

        sender.sendMessage(text);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

}
