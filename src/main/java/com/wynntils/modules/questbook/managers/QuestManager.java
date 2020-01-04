/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.questbook.managers;

import com.wynntils.ModCore;
import com.wynntils.Reference;
import com.wynntils.core.framework.enums.FilterType;
import com.wynntils.core.utils.ItemUtils;
import com.wynntils.core.utils.StringUtils;
import com.wynntils.core.utils.objects.Pair;
import com.wynntils.modules.chat.overlays.ChatOverlay;
import com.wynntils.modules.core.instances.FakeInventory;
import com.wynntils.modules.questbook.configs.QuestBookConfig;
import com.wynntils.modules.questbook.enums.*;
import com.wynntils.modules.questbook.instances.DiscoveryInfo;
import com.wynntils.modules.questbook.instances.MiniQuestInfo;
import com.wynntils.modules.questbook.instances.QuestInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QuestManager {

    /** Minimum number of ms between successive analyzes (To prevent analyze spam) */
    private static final int ANALYZE_MIN_TIMEOUT = 5 * 1000;
    /** Time to reanalyze after interrupted */
    private static final int INTERRUPT_TIMEOUT = 30 * 1000;

    private static final int MESSAGE_ID = 423375494;  // QuestManager.class.getName().hashCode()

    private static final Pattern QUEST_BOOK_WINDOW_TITLE_PATTERN = Pattern.compile("\\[Pg\\. \\d+] \\w{3,16}'s? (?:Discoveries|(?:Mini-)?Quests)");

    private static long readRequestTime = Long.MIN_VALUE;

    private static HashMap<String, QuestInfo> currentQuestsData = new HashMap<>();
    private static HashSet<String> incompleteQuests = new HashSet<>();
    private static HashSet<String> incompleteMiniQuests = new HashSet<>();
    private static HashMap<String, DiscoveryInfo> currentDiscoveryData = new HashMap<>();
    private static QuestInfo trackedQuest = null;

    public static List<String> discoveryLore = new ArrayList<>();
    public static List<String> secretdiscoveryLore = new ArrayList<>();
    public static List<String> questsLore = new ArrayList<>();
    public static List<String> miniquestsLore = new ArrayList<>();

    private static boolean secretDiscoveries = false;
    private static FakeInventory currentInventory = null;

    private static boolean analyseRequested = false;
    private static boolean bookOpened = false;
    private static boolean interrupted = false;
    private static boolean isForcingDiscoveries = false;
    private static boolean isForcingMiniquests = false;
    private static List<Runnable> onFinished = new ArrayList<>();

    /**
     * Queue a QuestBook analyse
     */
    public static void requestAnalyse() {
        if (!bookOpened) {
            requestFullSearch();
            return;
        }

        analyseRequested = true;
        interrupted = false;
    }

    public static void executeQueue() {
        if (!analyseRequested || (Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) return;
        long currentTime = System.currentTimeMillis();
        if (currentTime > readRequestTime + (interrupted ? INTERRUPT_TIMEOUT : ANALYZE_MIN_TIMEOUT)) {
            readRequestTime = currentTime;
            analyseRequested = false;
            interrupted = false;
            sendMessage(TextFormatting.GRAY + "[Analysing quest book...]");
            readQuestBook(!bookOpened, isForcingDiscoveries, isForcingMiniquests);
        }
    }

    private static void readQuestBook(boolean fullSearch, boolean forceDiscoveries, boolean forceMiniquests) {
        if (currentInventory != null && currentInventory.isOpen()) {
            currentInventory.close();
        }

        long ms = System.currentTimeMillis();

        FakeInventory fakeInventory = new FakeInventory(QUEST_BOOK_WINDOW_TITLE_PATTERN, 7);
        secretDiscoveries = false;
        // Ensure that all previously incomplete quests have been seen, and when
        // not doing a fullSearch, don't double check completed quest pages after
        // all previously incomplete quests have been seen
        HashSet<String> seenIncompleteQuests = new HashSet<>(incompleteQuests.size());
        HashSet<String> previouslyIncompleteQuests = new HashSet<>(incompleteQuests);
        HashSet<String> seenIncompleteMiniQuests = new HashSet<>(incompleteMiniQuests.size());
        HashSet<String> previouslyIncompleteMiniQuests = new HashSet<>(incompleteMiniQuests);

        fakeInventory.onReceiveItems(i -> {
            if (i.getWindowTitle().contains("Quests")) {  // Quests
                boolean isMiniquests = i.getWindowTitle().contains("Mini-Quests");
                Pair<Integer, ItemStack> next = i.findItem(">>>>>", FilterType.CONTAINS);
                Pair<Integer, ItemStack> discoveries = i.findItem("Discoveries", FilterType.EQUALS);
                Pair<Integer, ItemStack> quests = i.findItem("Quests", FilterType.CONTAINS);
                Pair<Integer, ItemStack> miniquests = i.findItem("Mini-Quests", FilterType.CONTAINS);

                // lore
                if (discoveries != null) {
                    discoveryLore = ItemUtils.getLore(discoveries.b);
                    discoveryLore.removeAll(Arrays.asList("", null));
                }
                if (quests != null) {
                    questsLore = ItemUtils.getLore(quests.b);
                    questsLore.removeAll(Arrays.asList("", null));
                }
                if (miniquests != null) {
                    miniquestsLore = ItemUtils.getLore(miniquests.b);
                    miniquestsLore.removeAll(Arrays.asList("", null));
                }

                NonNullList<ItemStack> items = NonNullList.create();
                items.addAll(i.getItems());
                ModCore.mc().addScheduledTask(() -> {
                    // parsing
                    for (ItemStack item : items) {
                        if (!item.hasDisplayName()) continue;  // not a valid quest

                        List<String> lore = ItemUtils.getLore(item);
                        if (lore.isEmpty()) continue;  // not a valid quest

                        List<String> realLore = lore.stream().map(TextFormatting::getTextWithoutFormattingCodes).collect(Collectors.toList());
                        if (!realLore.contains("Right click to track")) continue;  // not a valid quest

                        String displayName = StringUtils.normalizeBadString(TextFormatting.getTextWithoutFormattingCodes(item.getDisplayName()));

                        QuestStatus status = null;
                        if (lore.get(0).contains("Completed!")) status = QuestStatus.COMPLETED;
                        else if (lore.get(0).contains("Started")) status = QuestStatus.STARTED;
                        else if (lore.get(0).contains("Can start")) status = QuestStatus.CAN_START;
                        else if (lore.get(0).contains("Cannot start")) status = QuestStatus.CANNOT_START;
                        if (status == null) continue;

                        if (!(isMiniquests ? previouslyIncompleteMiniQuests : previouslyIncompleteQuests).remove(displayName) && !fullSearch && status == QuestStatus.COMPLETED) {
                            continue;
                        }

                        if (status != QuestStatus.COMPLETED) {
                            (isMiniquests ? seenIncompleteMiniQuests : seenIncompleteQuests).add(displayName);
                        }

                        String[] parts = TextFormatting.getTextWithoutFormattingCodes(lore.get(2)).split("\\s+");
                        boolean hasLevel = !parts[0].equals("✖");
                        QuestLevelType levelType = QuestLevelType.valueOf(parts[1].toUpperCase(Locale.ROOT));
                        int minLevel = Integer.parseInt(parts[parts.length - 1]);
                        QuestSize size = QuestSize.valueOf(TextFormatting.getTextWithoutFormattingCodes(lore.get(3)).replace("- Length: ", "").toUpperCase(Locale.ROOT));

                        StringBuilder description = new StringBuilder();
                        for (int x = 5; x < lore.size(); x++) {
                            if (lore.get(x).equalsIgnoreCase(TextFormatting.GRAY + "Right click to track")) {
                                break;
                            }
                            description.append(TextFormatting.getTextWithoutFormattingCodes(lore.get(x)));
                        }

                        QuestInfo quest;
                        if (isMiniquests) {
                            quest = new MiniQuestInfo(displayName, status, minLevel, levelType, hasLevel, size, description.toString(), lore);
                        } else {
                            quest = new QuestInfo(displayName, status, minLevel, levelType, hasLevel, size, description.toString(), lore);
                        }
                        currentQuestsData.put(displayName, quest);

                        if (trackedQuest != null && trackedQuest.getName().equals(displayName)) {
                            if (quest.getStatus() == QuestStatus.COMPLETED) trackedQuest = null;
                            else
                                trackedQuest = quest;
                        }
                    }

                    QuestBookPages.QUESTS.getPage().updateSearch();
                });
                // pagination
                if (next != null && (fullSearch || (isMiniquests ? previouslyIncompleteMiniQuests : previouslyIncompleteQuests).size() != 0)) {
                    i.clickItem(next.a, 1, ClickType.PICKUP);
                } else {
                    if (isMiniquests) {
                        incompleteMiniQuests = seenIncompleteMiniQuests;
                    } else {
                        incompleteQuests = seenIncompleteQuests;
                    }
                    if (isForcingMiniquests && !isMiniquests && miniquests != null) {
                        i.clickItem(miniquests.a, 1, ClickType.PICKUP);
                    } else if ((QuestBookConfig.INSTANCE.scanDiscoveries || forceDiscoveries || fullSearch) && discoveries != null) {
                        i.clickItem(discoveries.a, 1, ClickType.PICKUP);
                    } else {
                        i.close();
                    }
                }
            } else if (i.getWindowTitle().contains("Discoveries")) {  // Discoveries
                Pair<Integer, ItemStack> next = i.findItem(">>>>>", FilterType.CONTAINS);
                Pair<Integer, ItemStack> sDiscoveries = i.findItem("Secret Discoveries", FilterType.EQUALS);

                // lore
                if (sDiscoveries != null) {
                    secretdiscoveryLore = ItemUtils.getLore(sDiscoveries.b);
                    secretdiscoveryLore.removeAll(Arrays.asList("", null));
                }

                NonNullList<ItemStack> items = NonNullList.create();
                items.addAll(i.getItems());
                ModCore.mc().addScheduledTask(() -> {
                    for (ItemStack item : items) {  // parsing discoveries
                        if (!item.hasDisplayName()) continue;  // not a valid discovery

                        List<String> lore = ItemUtils.getLore(item);
                        if (lore.isEmpty() || !TextFormatting.getTextWithoutFormattingCodes(lore.get(0)).contains("✔ Combat Lv")) continue;  // not a valid discovery

                        String displayName = item.getDisplayName();
                        displayName = StringUtils.normalizeBadString(displayName.substring(0, displayName.length() - 1));

                        DiscoveryType discoveryType = null;
                        if (displayName.charAt(1) == 'e') discoveryType = DiscoveryType.WORLD;
                        else if (displayName.charAt(1) == 'f') discoveryType = DiscoveryType.TERRITORY;
                        else if (displayName.charAt(1) == 'b') discoveryType = DiscoveryType.SECRET;

                        int minLevel = Integer.parseInt(TextFormatting.getTextWithoutFormattingCodes(lore.get(0)).replace("✔ Combat Lv. Min: ", ""));

                        StringBuilder description = new StringBuilder();
                        for (int x = 2; x < lore.size(); x++) {
                            description.append(TextFormatting.getTextWithoutFormattingCodes(lore.get(x)));
                        }

                        currentDiscoveryData.put(displayName, new DiscoveryInfo(displayName, minLevel, description.toString(), lore, discoveryType));
                    }

                    QuestBookPages.DISCOVERIES.getPage().updateSearch();
                });
                // pagination
                if (next != null) i.clickItem(next.a, 1, ClickType.PICKUP);
                else if (!secretDiscoveries && sDiscoveries != null) {
                    secretDiscoveries = true;
                    i.clickItem(sDiscoveries.a, 1, ClickType.PICKUP);
                }
                else i.close();
            } else i.close();
        });
        fakeInventory.onClose(c -> {
            currentInventory = null;
            if (fullSearch) {
                bookOpened = true;
            }
            if (forceDiscoveries) {
                isForcingDiscoveries = false;
            }
            if (forceMiniquests) {
                isForcingMiniquests = false;
            }
            Iterator<Runnable> runnables = onFinished.iterator();
            while (runnables.hasNext()) {
                runnables.next().run();
                runnables.remove();
            }

            readRequestTime = System.currentTimeMillis();

            if (Reference.developmentEnvironment) {
                sendMessage(TextFormatting.GRAY + "[Quest book analyzed in " + (System.currentTimeMillis() - ms) + "ms]");
            } else {
                sendMessage(TextFormatting.GRAY + "[Quest book analyzed]");
            }
        });
        fakeInventory.onInterrupt(c -> {
            currentInventory = null;

            readRequestTime = System.currentTimeMillis();

            if (forceDiscoveries) {
                forceDiscoveries();
            }
            if (forceMiniquests) {
                scanMiniquests();
            }
            if (fullSearch) {
                requestFullSearch();
            } else {
                requestAnalyse();
            }

            interrupted = true;
            if (Reference.developmentEnvironment) {
                sendMessage(TextFormatting.GRAY + "[Quest book analysis interrupted after " + (System.currentTimeMillis() - ms) + "ms]");
            } else {
                sendMessage(TextFormatting.RED + String.format("Quest book analysis has been interrupted by your actions. Retrying in %d seconds", INTERRUPT_TIMEOUT / 1000));
            }
        });
        currentInventory = fakeInventory;

        fakeInventory.open();
    }

    /**
     * Returns the current quests data
     *
     * @return the current quest data in a {@link HashMap}
     */
    public static HashMap<String, QuestInfo> getCurrentQuestsData() {
        return currentQuestsData;
    }

    /**
     * Returns the current tracked quest
     * if null, no quest is being tracked
     *
     * @return the current tracked quest
     */
    public static QuestInfo getTrackedQuest() {
        return trackedQuest;
    }

    /**
     * Returns the current discoveries data
     *
     * @return the current discovery data in a {@link HashMap}
     */
    public static HashMap<String, DiscoveryInfo> getCurrentDiscoveriesData() {
        return currentDiscoveryData;
    }

    /**
     * Defines the current tracked quest
     *
     * @param selected the quest that you want to track
     */
    public static void setTrackedQuest(QuestInfo selected) {
        trackedQuest = selected;
    }

    /**
     * Check if the book was already opened before, if false it will request a read
     */
    public static void wasBookOpened() {
        interrupted = false;

        if (bookOpened) return;

        requestFullSearch();
    }

    /**
     * Request a full search, including already completed quests and discoveries
     */
    public static void requestFullSearch() {
        bookOpened = false;
        forceDiscoveries();
        scanMiniquests();
        analyseRequested = true;
        interrupted = false;
    }

    /**
     * The next analyze request will scan discoveries too, regardless of if {@link QuestBookConfig#scanDiscoveries} is false
     */
    public static void forceDiscoveries() {
        isForcingDiscoveries = true;
    }

    /**
     * The next scan will also scan mini-quests
     */
    public static void scanMiniquests() {
        isForcingMiniquests = true;
    }

    /**
     * After the next analysis is finish the runnable will be executed
     *
     * @param runnable the runnable to run
     */
    public static void onFinished(Runnable runnable) {
        onFinished.add(runnable);
    }

    /**
     * Clears the entire collected book data
     */
    public static void clearData() {
        readRequestTime = Long.MIN_VALUE;

        currentQuestsData.clear();
        incompleteQuests.clear();
        currentDiscoveryData.clear();
        trackedQuest = null;

        discoveryLore.clear();
        secretdiscoveryLore.clear();
        questsLore.clear();
        miniquestsLore.clear();

        secretDiscoveries = false;
        if (currentInventory != null && currentInventory.isOpen()) currentInventory.close();
        currentInventory = null;

        analyseRequested = false;
        bookOpened = false;
        interrupted = false;
        isForcingDiscoveries = false;
        isForcingMiniquests = false;
        onFinished.clear();
    }

    private static void sendMessage(String msg) {
        // Can be called from nio thread by FakeInventory
        Minecraft.getMinecraft().addScheduledTask(() ->
            ChatOverlay.getChat().printChatMessageWithOptionalDeletion(new TextComponentString(msg), MESSAGE_ID)
        );
    }

}
