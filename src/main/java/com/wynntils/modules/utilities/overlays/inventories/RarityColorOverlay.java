/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.utilities.overlays.inventories;

import com.wynntils.ModCore;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.colors.CustomColor;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.core.utils.ItemUtils;
import com.wynntils.modules.utilities.configs.UtilitiesConfig;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;

import static net.minecraft.client.renderer.GlStateManager.color;
import static net.minecraft.client.renderer.GlStateManager.glTexEnvi;

public class RarityColorOverlay implements Listener {

    private static String professionFilter = "-";

    @SubscribeEvent
    public void onChestInventory(GuiOverlapEvent.ChestOverlap.DrawGuiContainerForegroundLayer e) {
        drawChest(e.getGui(), e.getGui().getLowerInv(), e.getGui().getUpperInv(), true, true);
    }

    @SubscribeEvent
    public void onHorseInventory(GuiOverlapEvent.HorseOverlap.DrawGuiContainerForegroundLayer e) {
        drawChest(e.getGui(), e.getGui().getUpperInv(), e.getGui().getLowerInv(), true, false);
    }

    @SubscribeEvent
    public void onPlayerInventory(GuiOverlapEvent.InventoryOverlap.DrawGuiContainerForegroundLayer e) {
        for (Slot s : e.getGui().inventorySlots.inventorySlots) {
            if (!UtilitiesConfig.Items.INSTANCE.accesoryHighlight && s.slotNumber >= 9 && s.slotNumber <= 12)
                continue;
            if (!UtilitiesConfig.Items.INSTANCE.hotbarHighlight && s.slotNumber >= 36 && s.slotNumber <= 41)
                continue;
            if (!UtilitiesConfig.Items.INSTANCE.armorHighlight && s.slotNumber >= 5 && s.slotNumber <= 8)
                continue;
            if (!UtilitiesConfig.Items.INSTANCE.mainHighlightInventory && s.slotNumber >= 13 && s.slotNumber <= 35)
                continue;

            ItemStack is = s.getStack();
            String lore = ItemUtils.getStringLore(is);
            String name = is.getDisplayName();
            CustomColor colour;

            if (is.isEmpty()) {
                continue;
            } else if (lore.contains("Reward") || StringUtils.containsIgnoreCase(lore, "rewards")) {
                continue;
            } else if (lore.contains(TextFormatting.RED + "Fabled") && UtilitiesConfig.Items.INSTANCE.fabledHighlight) {
                colour = UtilitiesConfig.Items.INSTANCE.fabledHighlightColor;
            } else if (lore.contains(TextFormatting.AQUA + "Legendary") && UtilitiesConfig.Items.INSTANCE.legendaryHighlight) {
                colour = UtilitiesConfig.Items.INSTANCE.lengendaryHighlightColor;
            } else if (lore.contains(TextFormatting.DARK_PURPLE + "Mythic") && UtilitiesConfig.Items.INSTANCE.mythicHighlight) {
                colour = UtilitiesConfig.Items.INSTANCE.mythicHighlightColor;
            } else if (lore.contains(TextFormatting.LIGHT_PURPLE + "Rare") && UtilitiesConfig.Items.INSTANCE.rareHighlight) {
                colour = UtilitiesConfig.Items.INSTANCE.rareHighlightColor;
            } else if (lore.contains(TextFormatting.YELLOW + "Unique") && UtilitiesConfig.Items.INSTANCE.uniqueHighlight) {
                colour = UtilitiesConfig.Items.INSTANCE.uniqueHighlightColor;
            } else if (lore.contains(TextFormatting.GREEN + "Set") && UtilitiesConfig.Items.INSTANCE.setHighlight) {
                colour = UtilitiesConfig.Items.INSTANCE.setHighlightColor;
            } else if (lore.contains(TextFormatting.WHITE + "Normal") && UtilitiesConfig.Items.INSTANCE.normalHighlight) {
                colour = UtilitiesConfig.Items.INSTANCE.normalHighlightColor;
            } else if (name.matches("^(" + TextFormatting.DARK_AQUA + ".*%.*)$")) {
                colour = UtilitiesConfig.Items.INSTANCE.craftedHighlightColor;
            } else if (name.endsWith(TextFormatting.GOLD + " [" + TextFormatting.YELLOW + "✫" + TextFormatting.DARK_GRAY + "✫✫" + TextFormatting.GOLD + "]") && UtilitiesConfig.Items.INSTANCE.ingredientHighlight && !(is.getCount() == 0)) {
                colour = UtilitiesConfig.Items.INSTANCE.ingredientOneHighlightColor;
            } else if ((name.endsWith(TextFormatting.GOLD + " [" + TextFormatting.YELLOW + "✫✫" + TextFormatting.DARK_GRAY + "✫" + TextFormatting.GOLD + "]") && UtilitiesConfig.Items.INSTANCE.ingredientHighlight || name.endsWith(TextFormatting.DARK_PURPLE + " [" + TextFormatting.LIGHT_PURPLE + "✫✫" + TextFormatting.DARK_GRAY + "✫" + TextFormatting.DARK_PURPLE + "]") && UtilitiesConfig.Items.INSTANCE.ingredientHighlight) && !(is.getCount() == 0)) {
                colour = UtilitiesConfig.Items.INSTANCE.ingredientTwoHighlightColor;
            } else if ((name.endsWith(TextFormatting.GOLD + " [" + TextFormatting.YELLOW + "✫✫✫" + TextFormatting.GOLD + "]") || name.endsWith(TextFormatting.DARK_AQUA + " [" + TextFormatting.AQUA + "✫✫✫" + TextFormatting.DARK_AQUA + "]")) && UtilitiesConfig.Items.INSTANCE.ingredientHighlight && !(is.getCount() == 0)) {
                colour = UtilitiesConfig.Items.INSTANCE.ingredientThreeHighlightColor;
            } else if (isPowder(is)) {
                if (UtilitiesConfig.Items.INSTANCE.minPowderTier == 0 || getPowderTier(is) < UtilitiesConfig.Items.INSTANCE.minPowderTier)
                    continue;
                colour = getPowderColor(is);
            } else {
                continue;
            }

            if (colour == null) continue;

            // start rendering
            ScreenRenderer renderer = new ScreenRenderer();
            ScreenRenderer.beginGL(0, 0); {
                color(colour.r, colour.g, colour.b, UtilitiesConfig.Items.INSTANCE.inventoryAlpha / 100);
                glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
                RenderHelper.disableStandardItemLighting();

                renderer.drawRect(Textures.UIs.rarity, s.xPos - 1, s.yPos - 1, 0, 0, 18, 18);

                glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
                color(1.0f, 1.0f, 1.0f, 1.0f);
            } ScreenRenderer.endGL();
        }
    }

    public static void drawChest(GuiContainer guiContainer, IInventory lowerInv, IInventory upperInv, boolean emeraldsUpperInv, boolean emeraldsLowerInv) {
        int playerInvSlotNumber = 0;

        for (Slot s : guiContainer.inventorySlots.inventorySlots) {
            if (s.inventory.getDisplayName().equals(ModCore.mc().player.inventory.getDisplayName())) {
                playerInvSlotNumber++;
                if (playerInvSlotNumber <= 4 && playerInvSlotNumber >= 1 && !UtilitiesConfig.Items.INSTANCE.accesoryHighlight)
                    continue;
                if (playerInvSlotNumber <= 27 && playerInvSlotNumber >= 5 && !UtilitiesConfig.Items.INSTANCE.mainHighlightInventory)
                    continue;
                if (playerInvSlotNumber <= 36 && playerInvSlotNumber >= 28 && !UtilitiesConfig.Items.INSTANCE.hotbarHighlight)
                    continue;
            } else {
                if (!UtilitiesConfig.Items.INSTANCE.mainHighlightChest)
                    continue;
            }

            ItemStack is = s.getStack();
            String lore = ItemUtils.getStringLore(is);
            String name = is.getDisplayName();
            float r, g, b;

            if (is.isEmpty()) {
                continue;
            } else if (UtilitiesConfig.Items.INSTANCE.filterEnabled && !professionFilter.equals("-") && lore.contains(professionFilter)) {
                r = 0.078f; g = 0.35f; b = 0.8f;
            } else if (lore.contains(TextFormatting.RED + "Fabled") && UtilitiesConfig.Items.INSTANCE.fabledHighlight) {
                r = UtilitiesConfig.Items.INSTANCE.fabledHighlightColor.r; g = UtilitiesConfig.Items.INSTANCE.fabledHighlightColor.g; b = UtilitiesConfig.Items.INSTANCE.fabledHighlightColor.b;
            } else if (lore.contains(TextFormatting.AQUA + "Legendary") && UtilitiesConfig.Items.INSTANCE.legendaryHighlight) {
                r = UtilitiesConfig.Items.INSTANCE.lengendaryHighlightColor.r; g = UtilitiesConfig.Items.INSTANCE.lengendaryHighlightColor.g; b = UtilitiesConfig.Items.INSTANCE.lengendaryHighlightColor.b;
            } else if (lore.contains(TextFormatting.DARK_PURPLE + "Mythic") && UtilitiesConfig.Items.INSTANCE.mythicHighlight) {
                r = UtilitiesConfig.Items.INSTANCE.mythicHighlightColor.r; g = UtilitiesConfig.Items.INSTANCE.mythicHighlightColor.g; b = UtilitiesConfig.Items.INSTANCE.mythicHighlightColor.b;
            } else if (lore.contains(TextFormatting.LIGHT_PURPLE + "Rare") && UtilitiesConfig.Items.INSTANCE.rareHighlight) {
                r = UtilitiesConfig.Items.INSTANCE.rareHighlightColor.r; g = UtilitiesConfig.Items.INSTANCE.rareHighlightColor.g; b = UtilitiesConfig.Items.INSTANCE.rareHighlightColor.b;
            } else if (lore.contains(TextFormatting.YELLOW + "Unique") && UtilitiesConfig.Items.INSTANCE.uniqueHighlight) {
                r = UtilitiesConfig.Items.INSTANCE.uniqueHighlightColor.r; g = UtilitiesConfig.Items.INSTANCE.uniqueHighlightColor.g; b = UtilitiesConfig.Items.INSTANCE.uniqueHighlightColor.b;
            } else if (lore.contains(TextFormatting.GREEN + "Set") && UtilitiesConfig.Items.INSTANCE.setHighlight) {
                r = UtilitiesConfig.Items.INSTANCE.setHighlightColor.r; g = UtilitiesConfig.Items.INSTANCE.setHighlightColor.g; b = UtilitiesConfig.Items.INSTANCE.setHighlightColor.b;
            } else if (lore.contains(TextFormatting.WHITE + "Normal") && UtilitiesConfig.Items.INSTANCE.normalHighlight) {
                r = UtilitiesConfig.Items.INSTANCE.normalHighlightColor.r; g = UtilitiesConfig.Items.INSTANCE.normalHighlightColor.g; b = UtilitiesConfig.Items.INSTANCE.normalHighlightColor.b;
            } else if (UtilitiesConfig.Items.INSTANCE.highlightCosmeticDuplicates && guiContainer.getSlotUnderMouse() != null && lore.contains("Reward") && guiContainer.getSlotUnderMouse().slotNumber != s.slotNumber && guiContainer.getSlotUnderMouse().getStack().getDisplayName().equals(is.getDisplayName())) {
                r = 0f; g = 1f; b = 0f;
            } else if (lore.contains(TextFormatting.GOLD + "Epic") && lore.contains("Reward") && UtilitiesConfig.Items.INSTANCE.epicEffectsHighlight) {
                r = 1; g = 0.666f; b = 0;
            } else if (lore.contains(TextFormatting.RED + "Godly") && lore.contains("Reward") && UtilitiesConfig.Items.INSTANCE.godlyEffectsHighlight) {
                r = 1; g = 0; b = 0;
            } else if (lore.contains(TextFormatting.LIGHT_PURPLE + "Rare") && lore.contains("Reward") && UtilitiesConfig.Items.INSTANCE.rareEffectsHighlight) {
                r = 1; g = 0; b = 1;
            } else if (lore.contains(TextFormatting.WHITE + "Common") && lore.contains("Reward") && UtilitiesConfig.Items.INSTANCE.commonEffectsHighlight) {
                r = 1; g = 1; b = 1;
            } else if (lore.contains(TextFormatting.DARK_RED + " Black Market") && lore.contains("Reward") && UtilitiesConfig.Items.INSTANCE.blackMarketEffectsHighlight) {
                r = 0; g = 0; b = 0;
            } else if (name.matches("^(" + TextFormatting.DARK_AQUA + ".*%.*)$")) {
                r = UtilitiesConfig.Items.INSTANCE.craftedHighlightColor.r; g = UtilitiesConfig.Items.INSTANCE.craftedHighlightColor.g; b = UtilitiesConfig.Items.INSTANCE.craftedHighlightColor.b;
            } else if (name.contains(TextFormatting.GOLD + " [" + TextFormatting.YELLOW + "✫" + TextFormatting.DARK_GRAY + "✫✫" + TextFormatting.GOLD + "]") && UtilitiesConfig.Items.INSTANCE.ingredientHighlight && !(is.getCount() == 0)) {
                r = UtilitiesConfig.Items.INSTANCE.ingredientOneHighlightColor.r; g = UtilitiesConfig.Items.INSTANCE.ingredientOneHighlightColor.g; b = UtilitiesConfig.Items.INSTANCE.ingredientOneHighlightColor.b;
            } else if ((name.contains(TextFormatting.GOLD + " [" + TextFormatting.YELLOW + "✫✫" + TextFormatting.DARK_GRAY + "✫" + TextFormatting.GOLD + "]") || name.contains(TextFormatting.DARK_PURPLE + " [" + TextFormatting.LIGHT_PURPLE + "✫✫" + TextFormatting.DARK_GRAY + "✫" + TextFormatting.DARK_PURPLE + "]")) && UtilitiesConfig.Items.INSTANCE.ingredientHighlight && !(is.getCount() == 0)) {
                r = UtilitiesConfig.Items.INSTANCE.ingredientTwoHighlightColor.r; g = UtilitiesConfig.Items.INSTANCE.ingredientTwoHighlightColor.g; b = UtilitiesConfig.Items.INSTANCE.ingredientTwoHighlightColor.b;
            } else if ((name.contains(TextFormatting.GOLD + " [" + TextFormatting.YELLOW + "✫✫✫" + TextFormatting.GOLD + "]") || name.contains(TextFormatting.DARK_AQUA + " [" + TextFormatting.AQUA + "✫✫✫" + TextFormatting.DARK_AQUA + "]")) && UtilitiesConfig.Items.INSTANCE.ingredientHighlight && !(is.getCount() == 0)) {
                r = UtilitiesConfig.Items.INSTANCE.ingredientThreeHighlightColor.r; g = UtilitiesConfig.Items.INSTANCE.ingredientThreeHighlightColor.g; b = UtilitiesConfig.Items.INSTANCE.ingredientThreeHighlightColor.b;
            } else if (isPowder(is)) {
                if (UtilitiesConfig.Items.INSTANCE.minPowderTier == 0 || getPowderTier(is) < UtilitiesConfig.Items.INSTANCE.minPowderTier)
                    continue;
                CustomColor powderColour = getPowderColor(is);
                if (powderColour == null) continue;
                r = powderColour.r;
                g = powderColour.g;
                b = powderColour.b;
            } else {
                continue;
            }

            // start rendering
            ScreenRenderer renderer = new ScreenRenderer();
            ScreenRenderer.beginGL(0, 0); {
                color(r, g, b, UtilitiesConfig.Items.INSTANCE.inventoryAlpha / 100);
                glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
                RenderHelper.disableStandardItemLighting();

                renderer.drawRect(Textures.UIs.rarity, s.xPos - 1, s.yPos - 1, 0, 0, 18, 18);

                glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
                color(1F, 1F, 1F, 1F);
            } ScreenRenderer.endGL();
        }
    }

    private static boolean isPowder(ItemStack is) {
        return (is.getItem() == Items.DYE && is.hasDisplayName() && is.getDisplayName().contains("Powder") && TextFormatting.getTextWithoutFormattingCodes(ItemUtils.getStringLore(is)).contains("Effect on Weapons"));
    }

    private static int getPowderTier(ItemStack is) {
        if (is.getDisplayName().endsWith("III")) {
            return 3;
        } else if (is.getDisplayName().endsWith("IV")) {
            return 4;
        } else if (is.getDisplayName().endsWith("VI")) {
            return 6;
        } else if (is.getDisplayName().endsWith("V")) {
            return 5;
        } else if (is.getDisplayName().endsWith("II")) {
            return 2;
        } else {
            return 1;
        }
    }

    private static final HashMap<Character, CustomColor> POWDER_COLOUR_MAP = new HashMap<>(10);
    static {
        // Lightning
        POWDER_COLOUR_MAP.put(TextFormatting.YELLOW.toString().charAt(1), new CustomColor(1, 1, 1 / 3f));
        // Water
        POWDER_COLOUR_MAP.put(TextFormatting.AQUA.toString().charAt(1), new CustomColor(1 / 3f, 1, 1));
        // Air
        POWDER_COLOUR_MAP.put(TextFormatting.WHITE.toString().charAt(1), new CustomColor(1, 1, 1));
        // Earth
        POWDER_COLOUR_MAP.put(TextFormatting.DARK_GREEN.toString().charAt(1), new CustomColor(0, 2 / 3f, 0));
        // Fire
        POWDER_COLOUR_MAP.put(TextFormatting.RED.toString().charAt(1), new CustomColor(1, 1 / 3f, 1 / 3f));
    }

    private static CustomColor getPowderColor(ItemStack is) {
        String name = is.getDisplayName();
        if (name.length() < 2 || name.charAt(0) != '§') return null;
        return POWDER_COLOUR_MAP.get(name.charAt(1));
    }

    public static void setProfessionFilter(String s) {
        professionFilter = s;
    }

    public static String getProfessionFilter() {
        return professionFilter;
    }

}
