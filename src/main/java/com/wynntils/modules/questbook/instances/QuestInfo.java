/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.questbook.instances;

import com.wynntils.core.utils.ItemUtils;
import com.wynntils.core.utils.StringUtils;
import com.wynntils.core.utils.objects.Location;
import com.wynntils.modules.core.managers.CompassManager;
import com.wynntils.modules.questbook.configs.QuestBookConfig;
import com.wynntils.modules.questbook.enums.QuestLevelType;
import com.wynntils.modules.questbook.enums.QuestSize;
import com.wynntils.modules.questbook.enums.QuestStatus;
import com.wynntils.modules.utilities.configs.TranslationConfig;
import com.wynntils.webapi.services.TranslationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.util.text.TextFormatting.*;

public class QuestInfo {

    private static final Pattern coordinatePattern = Pattern.compile("\\[(-?\\d+), ?(-?\\d+), ?(-?\\d+)\\]");

    private ItemStack originalStack;

    private String name;
    private QuestStatus status;
    private QuestLevelType levelType;
    private QuestSize size;
    private int minLevel;
    private List<String> lore;
    private String description;

    private String friendlyName;
    private List<String> splittedDescription;
    private Location targetLocation = null;

    private boolean valid = false;
    private boolean isMiniQuest;

    public QuestInfo(ItemStack originalStack, boolean isMiniQuest) {
        this.originalStack = originalStack;
        this.isMiniQuest = isMiniQuest;

        lore = ItemUtils.getLore(originalStack);
        name = StringUtils.normalizeBadString(getTextWithoutFormattingCodes(originalStack.getDisplayName()));

        //quest status
        if (lore.get(0).contains("Completed!")) status = QuestStatus.COMPLETED;
        else if (lore.get(0).contains("Started")) status = QuestStatus.STARTED;
        else if (lore.get(0).contains("Can start")) status = QuestStatus.CAN_START;
        else if (lore.get(0).contains("Cannot start")) status = QuestStatus.CANNOT_START;
        else return;

        String[] parts = getTextWithoutFormattingCodes(lore.get(2)).split("\\s+");
        levelType = QuestLevelType.valueOf(parts[1].toUpperCase(Locale.ROOT));
        minLevel = Integer.parseInt(parts[parts.length - 1]);
        size = QuestSize.valueOf(getTextWithoutFormattingCodes(lore.get(3)).replace("- Length: ", "").toUpperCase(Locale.ROOT));

        // flat description
        StringBuilder descriptionBuilder = new StringBuilder();
        for (int x = 5; x < lore.size(); x++) {
            if (lore.get(x).equalsIgnoreCase(GRAY + "Right click to track")) {
                break;
            }
            if (descriptionBuilder.length() > 0 && !descriptionBuilder.substring(descriptionBuilder.length() - 1).equals(" ")) {
                descriptionBuilder.append(" ");
            }
            descriptionBuilder.append(getTextWithoutFormattingCodes(lore.get(x)));
        }
        description = descriptionBuilder.toString();

        // splitted description
        splittedDescription = Stream.of(StringUtils.wrapTextBySize(description, 200)).collect(Collectors.toList());

        // friendly name
        friendlyName = this.name.replace("Mini-Quest - ", "");
        if (Minecraft.getMinecraft().fontRenderer.getStringWidth(friendlyName) > 120) friendlyName += "...";
        while (Minecraft.getMinecraft().fontRenderer.getStringWidth(friendlyName) > 120) {
            friendlyName = friendlyName.substring(0, friendlyName.length() - 4).trim() + "...";
        }

        // location
        Matcher m = coordinatePattern.matcher(description);
        if(m.find()) {
            targetLocation = new Location(0, 0, 0);

            if(m.group(1) != null) targetLocation.setX(Integer.parseInt(m.group(1)));
            if(m.group(2) != null) targetLocation.setY(Integer.parseInt(m.group(2)));
            if(m.group(3) != null) targetLocation.setZ(Integer.parseInt(m.group(3)));
        }

        lore.add(0, BOLD + name);
        valid = true;

        // translation (might replace splittedDescription)
        if (TranslationConfig.INSTANCE.enableTextTranslation && TranslationConfig.INSTANCE.translateTrackedQuest) {
            TranslationManager.getTranslator().translate(description, TranslationConfig.INSTANCE.languageName, translatedMsg -> {
                List<String> translatedSplitted = Stream.of(StringUtils.wrapTextBySize(TranslationManager.TRANSLATED_PREFIX + translatedMsg, 200)).collect(Collectors.toList());
                if (TranslationConfig.INSTANCE.keepOriginal) {
                    splittedDescription.addAll(translatedSplitted);
                } else {
                    splittedDescription = translatedSplitted;
                }
            });
        }
    }

    public String getName() {
        return name;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public List<String> getLore() {
        return lore;
    }

    public QuestLevelType getLevelType() {
        return levelType;
    }

    public QuestSize getSize() {
        return size;
    }

    public List<String> getSplittedDescription() {
        return splittedDescription;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public Location getTargetLocation() {
        return targetLocation;
    }

    public ItemStack getOriginalStack() {
        return originalStack;
    }

    public boolean hasTargetLocation() {
        return targetLocation != null;
    }

    public boolean isMiniQuest() {
        return isMiniQuest;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean equals(ItemStack stack) {
        return ItemUtils.getStringLore(originalStack).equals(ItemUtils.getStringLore(stack));
    }

    public void setAsCompleted() {
        status = QuestStatus.COMPLETED;

        lore.clear();
        lore.add(WHITE.toString() + BOLD + name);
        lore.add(GREEN + "Completed!");
        lore.add(WHITE + " ");
        lore.add(GREEN + "✔ " + GRAY + "Combat Lv. Min: " + WHITE + minLevel);
        lore.add(GREEN + "- " + GRAY + "Length: " + WHITE + StringUtils.capitalizeFirst(size.name().toLowerCase()));
    }

    public void updateAsTracked() {
        if (!hasTargetLocation() || !QuestBookConfig.INSTANCE.compassFollowQuests) return;

        CompassManager.setCompassLocation(getTargetLocation());
    }

    @Override
    public String toString() {
        return name + ":" + minLevel + ":" + levelType + ":" + size.toString() + ":" + status.toString() + ":" + description;
    }
}
