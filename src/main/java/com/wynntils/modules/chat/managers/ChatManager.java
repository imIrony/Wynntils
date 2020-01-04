/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.chat.managers;

import com.wynntils.ModCore;
import com.wynntils.core.utils.StringUtils;
import com.wynntils.core.utils.objects.Pair;
import com.wynntils.modules.chat.configs.ChatConfig;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatManager {

    public static DateFormat dateFormat;
    public static boolean validDateFormat;

    private static final SoundEvent popOffSound = new SoundEvent(new ResourceLocation("minecraft", "entity.blaze.hurt"));

    private static final String nonTranslatable = "[^a-zA-Z1-9.!?]";
    private static final String optionalTranslatable = "[.!?]";

    private static final Pattern inviteReg = Pattern.compile("((" + TextFormatting.GOLD + "|" + TextFormatting.AQUA + ")/(party|guild) join [a-zA-Z0-9._-]+)");
    private static final Pattern tradeReg = Pattern.compile("\\w+ would like to trade! Type /trade \\w+ to accept\\.");
    private static final Pattern duelReg = Pattern.compile("\\w+ \\[Lv\\. \\d+] would like to duel! Type /duel \\w+ to accept\\.");
    private static final Pattern coordinateReg = Pattern.compile("(-?\\d{1,5}[ ,]{1,2})(\\d{1,3}[ ,]{1,2})?(-?\\d{1,5})");

    public static ITextComponent processRealMessage(ITextComponent in) {
        ITextComponent original = in.createCopy();

        // Reorginizing
        if (!in.getUnformattedComponentText().isEmpty()) {
            ITextComponent newMessage = new TextComponentString("");
            newMessage.setStyle(in.getStyle().createDeepCopy());
            newMessage.appendSibling(in);
            newMessage.getSiblings().addAll(in.getSiblings());
            in.getSiblings().clear();
            in = newMessage;
        }

        // timestamps
        if (ChatConfig.INSTANCE.addTimestampsToChat) {
            if (dateFormat == null || !validDateFormat) {
                try {
                    dateFormat = new SimpleDateFormat(ChatConfig.INSTANCE.timestampFormat);
                    validDateFormat = true;
                } catch (IllegalArgumentException ex) {
                    validDateFormat = false;
                }
            }

            List<ITextComponent> timeStamp = new ArrayList<>();
            ITextComponent startBracket = new TextComponentString("[");
            startBracket.getStyle().setColor(TextFormatting.DARK_GRAY);
            timeStamp.add(startBracket);
            ITextComponent time;
            if (validDateFormat) {
                time = new TextComponentString(dateFormat.format(new Date()));
                time.getStyle().setColor(TextFormatting.GRAY);
            } else {
                time = new TextComponentString("Invalid Format");
                time.getStyle().setColor(TextFormatting.RED);
            }
            timeStamp.add(time);
            ITextComponent endBracket = new TextComponentString("] ");
            endBracket.getStyle().setColor(TextFormatting.DARK_GRAY);
            timeStamp.add(endBracket);
            in.getSiblings().addAll(0, timeStamp);
        }

        // popup sound
        if (in.getUnformattedText().contains(" requires your ") && in.getUnformattedText().contains(" skill to be at least "))
            ModCore.mc().player.playSound(popOffSound, 1f, 1f);

        // wynnic translator
        if (StringUtils.hasWynnic(in.getUnformattedText())) {
            List<ITextComponent> newTextComponents = new ArrayList<>();
            boolean capital = false;
            boolean isGuildOrParty = Pattern.compile(TabManager.DEFAULT_GUILD_REGEX.replace("&", "§")).matcher(original.getFormattedText()).find() || Pattern.compile(TabManager.DEFAULT_PARTY_REGEX.replace("&", "§")).matcher(original.getFormattedText()).find();
            boolean foundStart = false;
            boolean foundEndTimestamp = !ChatConfig.INSTANCE.addTimestampsToChat;
            boolean previousWynnic = false;
            ITextComponent currentTranslatedComponents = new TextComponentString("");
            List<ITextComponent> currentOldComponents = new ArrayList<>();
            if (foundEndTimestamp && !in.getSiblings().get(ChatConfig.INSTANCE.addTimestampsToChat ? 3 : 0).getUnformattedText().contains("/") && !isGuildOrParty) {
                foundStart = true;
            }
            for (ITextComponent component : in.getSiblings()) {
                String toAdd = "";
                String currentNonTranslated = "";
                StringBuilder oldText = new StringBuilder();
                for (char character : component.getUnformattedText().toCharArray()) {
                    if (StringUtils.isWynnic(character)) {
                        if (previousWynnic) {
                            toAdd += currentNonTranslated;
                            oldText.append(currentNonTranslated);
                            currentNonTranslated = "";
                        } else {
                            ITextComponent newComponent = new TextComponentString(oldText.toString());
                            newComponent.setStyle(component.getStyle().createDeepCopy());
                            newTextComponents.add(newComponent);
                            oldText = new StringBuilder();
                            toAdd = "";
                            previousWynnic = true;
                        }
                        String englishVersion = StringUtils.translateCharacterFromWynnic(character);
                        if (capital && englishVersion.matches("[a-z]")) {
                            englishVersion = Character.toString(Character.toUpperCase(englishVersion.charAt(0)));
                        }

                        if (".?!".contains(englishVersion)) {
                            capital = true;
                        } else {
                            capital = false;
                        }
                        toAdd += englishVersion;
                        oldText.append(character);
                    } else if (Character.toString(character).matches(nonTranslatable) || Character.toString(character).matches(optionalTranslatable)) {
                        if (previousWynnic) {
                            currentNonTranslated += character;
                        } else {
                            oldText.append(character);
                        }

                        if (".?!".contains(Character.toString(character))) {
                            capital = true;
                        } else if (character != ' ') {
                            capital = false;
                        }
                    } else {
                        if (previousWynnic) {
                            previousWynnic = false;
                            ITextComponent oldComponent = new TextComponentString(oldText.toString());
                            oldComponent.setStyle(component.getStyle().createDeepCopy());
                            ITextComponent newComponent = new TextComponentString(toAdd);
                            newComponent.setStyle(component.getStyle().createDeepCopy());

                            newTextComponents.add(oldComponent);
                            for (ITextComponent currentOldComponent : currentOldComponents) {
                                currentOldComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, currentTranslatedComponents));
                            }

                            currentOldComponents.clear();
                            currentTranslatedComponents = new TextComponentString("");

                            oldText = new StringBuilder(currentNonTranslated);
                            currentNonTranslated = "";
                            oldText.append(character);
                        } else {
                            oldText.append(character);
                        }

                        if (character != ' ') {
                            capital = false;
                        }
                    }
                }
                if (!currentNonTranslated.isEmpty()) {
                    oldText.append(currentNonTranslated);
                    if (previousWynnic) {
                        toAdd += currentNonTranslated;
                    }
                }
                if (previousWynnic) {
                    ITextComponent oldComponent = new TextComponentString(oldText.toString());
                    oldComponent.setStyle(component.getStyle().createDeepCopy());
                    ITextComponent newComponent = new TextComponentString(toAdd);
                    newComponent.setStyle(component.getStyle().createDeepCopy());

                    newTextComponents.add(oldComponent);
                    currentTranslatedComponents.appendSibling(newComponent);
                    currentOldComponents.add(oldComponent);
                } else {
                    ITextComponent oldComponent = new TextComponentString(oldText.toString());
                    oldComponent.setStyle(component.getStyle().createDeepCopy());
                    newTextComponents.add(oldComponent);
                }
                if (!foundStart) {
                    if (foundEndTimestamp) {
                        if (in.getSiblings().get(ChatConfig.INSTANCE.addTimestampsToChat ? 3 : 0).getUnformattedText().contains("/")) {
                            foundStart = component.getUnformattedText().contains(":");
                        } else if (isGuildOrParty) {
                            foundStart = component.getUnformattedText().contains("]");
                        }
                    } else if (component.getUnformattedComponentText().contains("] ")) {
                        foundEndTimestamp = true;
                        if (!in.getSiblings().get(ChatConfig.INSTANCE.addTimestampsToChat ? 3 : 0).getUnformattedText().contains("/") && !isGuildOrParty) {
                            foundStart = true;
                        }
                    }

                    if (foundStart) {
                        capital = true;
                    }
                }
            }

            for (ITextComponent component : currentOldComponents) {
                component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, currentTranslatedComponents));
            }

            in.getSiblings().clear();
            in.getSiblings().addAll(newTextComponents);
        }

        // clickable party invites
        if (ChatConfig.INSTANCE.clickablePartyInvites && inviteReg.matcher(in.getFormattedText()).find()) {
            for (ITextComponent textComponent : in.getSiblings()) {
                if (textComponent.getUnformattedComponentText().startsWith("/")) {
                    String command = textComponent.getUnformattedComponentText();
                    textComponent.getStyle()
                            .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                            .setUnderlined(true)
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Join!")));
                }
            }
        }

        // clickable trade messages
        if (ChatConfig.INSTANCE.clickableTradeMessage && tradeReg.matcher(in.getUnformattedText()).find()) {
            for (ITextComponent textComponent : in.getSiblings()) {
                if (textComponent.getUnformattedComponentText().startsWith("/")) {
                    String command = textComponent.getUnformattedComponentText();
                    textComponent.getStyle()
                            .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                            .setUnderlined(true)
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Trade!")));
                }
            }
        }

        // clickable duel messages
        if (ChatConfig.INSTANCE.clickableDuelMessage && duelReg.matcher(in.getUnformattedText()).find()) {
            for (ITextComponent textComponent : in.getSiblings()) {
                if (textComponent.getUnformattedComponentText().startsWith("/")) {
                    String command = textComponent.getUnformattedComponentText();
                    textComponent.getStyle()
                            .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                            .setUnderlined(true)
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Duel!")));
                }
            }
        }

        // clickable coordinates
        if (ChatConfig.INSTANCE.clickableCoordinates && coordinateReg.matcher(in.getUnformattedText()).find()) {
            String crdText;
            Style style;
            String command = "/compass ";
            List<ITextComponent> crdMsg = new ArrayList<>();

            for (ITextComponent texts: in.getSiblings()) {
                Matcher m = coordinateReg.matcher(texts.getUnformattedText());
                if (!m.find())  continue;

                // Most likely only needed during the Wynnter Fair for the message with how many more players are required to join.
                // As far as i could find all other messages from the Wynnter Fair use text components properly.
                if (m.start() > 0 && texts.getUnformattedText().charAt(m.start() - 1) == '§') continue;

                int index = in.getSiblings().indexOf(texts);

                crdText = texts.getUnformattedText();
                style = texts.getStyle();
                in.getSiblings().remove(texts);

                // Pre-text
                ITextComponent preText = new TextComponentString(crdText.substring(0, m.start()));
                preText.setStyle(style.createShallowCopy());
                crdMsg.add(preText);

                // Coordinates:
                command += crdText.substring(m.start(1), m.end(1)).replaceAll("[ ,]", "") + " ";
                command += crdText.substring(m.start(3), m.end(3)).replaceAll("[ ,]", "");
                ITextComponent clickableText = new TextComponentString(crdText.substring(m.start(), m.end()));
                clickableText.setStyle(style.createShallowCopy());
                clickableText.getStyle()
                        .setColor(TextFormatting.DARK_AQUA)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(command)));
                crdMsg.add(clickableText);

                // Post-text
                ITextComponent postText = new TextComponentString(crdText.substring(m.end()));
                postText.setStyle(style.createShallowCopy());
                crdMsg.add(postText);

                in.getSiblings().addAll(index, crdMsg);
                break;
            }
        }

        return in;
    }

    public static ITextComponent renderMessage(ITextComponent in) {
        return in;
    }

    public static boolean processUserMention(ITextComponent in, ITextComponent original) {
        boolean hasMention = false;
        if (ChatConfig.INSTANCE.allowChatMentions) {
            if (in.getFormattedText().contains(ModCore.mc().player.getName())) {
                // Patterns used to detect guild/party chat
                boolean isGuildOrParty = Pattern.compile(TabManager.DEFAULT_GUILD_REGEX.replace("&", "§")).matcher(original.getFormattedText()).find() || Pattern.compile(TabManager.DEFAULT_PARTY_REGEX.replace("&", "§")).matcher(original.getFormattedText()).find();
                boolean foundStart = false;
                boolean foundEndTimestamp = !ChatConfig.INSTANCE.addTimestampsToChat;
                ArrayList<ITextComponent> components = new ArrayList<>();
                for (ITextComponent component : in.getSiblings()) {
                    if (component.getUnformattedComponentText().contains(ModCore.mc().player.getName()) && foundStart) {
                        hasMention = true;
                        String text = component.getUnformattedComponentText();
                        String playerName = ModCore.mc().player.getName();
                        while (text.contains(playerName)) {
                            String section = text.substring(0, text.indexOf(playerName));
                            ITextComponent sectionComponent = new TextComponentString(section);
                            sectionComponent.setStyle(component.getStyle().createShallowCopy());
                            components.add(sectionComponent);

                            ITextComponent playerComponent = new TextComponentString(ModCore.mc().player.getName());
                            playerComponent.setStyle(component.getStyle().createShallowCopy());
                            playerComponent.getStyle().setColor(TextFormatting.YELLOW);
                            components.add(playerComponent);

                            text = text.replaceFirst(".*" + ModCore.mc().player.getName(), "");
                        }
                        ITextComponent sectionComponent = new TextComponentString(text);
                        sectionComponent.setStyle(component.getStyle().createShallowCopy());
                        components.add(sectionComponent);
                    } else if (!foundStart) {
                        if (foundEndTimestamp) {
                            if (in.getSiblings().get(ChatConfig.INSTANCE.addTimestampsToChat ? 3 : 0).getUnformattedText().contains("/")) {
                                foundStart = component.getUnformattedText().contains(":");
                            } else if (isGuildOrParty) {
                                foundStart = component.getUnformattedText().contains("]");
                            }
                        } else if (component.getUnformattedComponentText().contains("] ")) {
                            foundEndTimestamp = true;
                        }
                        components.add(component);
                    } else {
                        components.add(component);
                    }
                }
                in.getSiblings().clear();
                in.getSiblings().addAll(components);
                if (hasMention) {
                    ModCore.mc().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_NOTE_PLING, 1.0F));
                }
            }
        }
        return hasMention;
    }

    public static Pair<String, Boolean> applyUpdatesToServer(String message) {
        String after = message;

        boolean cancel = false;

        if (message.contains("{")) {
            StringBuilder newString = new StringBuilder();
            boolean isWynnic = false;
            for (char character : message.toCharArray()) {
                if (character == '{') {
                    isWynnic = true;
                } else if (isWynnic && character == '}') {
                    isWynnic = false;
                } else if (isWynnic) {
                    if (!Character.toString(character).matches(nonTranslatable)) {
                        if (Character.toString(character).matches("[a-z]")) {
                            newString.append((char) ((character) + 9275));
                        } else if (Character.toString(character).matches("[A-Z]")) {
                            newString.append((char) ((character) + 9307));
                        } else if (Character.toString(character).matches("[1-9]")) {
                            newString.append((char) ((character) + 9283));
                        } else if (character == '.') {
                            newString.append("\uFF10");
                        } else if (character == '!') {
                            newString.append("\uFF11");
                        } else if (character == '?') {
                            newString.append("\uFF12");
                        }
                    } else {
                        newString.append(character);
                    }
                } else {
                    newString.append(character);
                }
            }
            after = newString.toString();

        }

        return new Pair<>(after, cancel);
    }

}
