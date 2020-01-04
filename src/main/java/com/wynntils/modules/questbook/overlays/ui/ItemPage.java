/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.questbook.overlays.ui;

import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.SmartFontRenderer;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.core.utils.ItemUtils;
import com.wynntils.modules.questbook.enums.QuestBookPages;
import com.wynntils.modules.questbook.instances.IconContainer;
import com.wynntils.modules.questbook.instances.QuestBookPage;
import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.profiles.item.ItemProfile;
import com.wynntils.webapi.profiles.item.enums.ItemType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ItemPage extends QuestBookPage {

    private ArrayList<ItemProfile> itemSearch;

    private boolean byAlphabetical = true;
    private boolean byLevel = false;
    private boolean byRarity = false;

    private boolean allowHelmet = true;
    private boolean allowChestplate = true;
    private boolean allowLeggings = true;
    private boolean allowBoots = true;
    private boolean allowWands = true;
    private boolean allowDaggers = true;
    private boolean allowSpears = true;
    private boolean allowBows = true;
    private boolean allowReliks = true;
    private boolean allowNecklaces = true;
    private boolean allowBracelets = true;
    private boolean allowRings = true;

    private static final ItemStack helmetIcon = new ItemStack(ItemType.HELMET.getDefaultItem());
    private static final ItemStack chestplateIcon = new ItemStack(ItemType.CHESTPLATE.getDefaultItem());
    private static final ItemStack leggingsIcon = new ItemStack(ItemType.LEGGINGS.getDefaultItem());
    private static final ItemStack bootsIcon = new ItemStack(ItemType.BOOTS.getDefaultItem());
    private static final ItemStack wandsIcon = new ItemStack(ItemType.WAND.getDefaultItem());
    private static final ItemStack daggersIcon = new ItemStack(ItemType.DAGGER.getDefaultItem());
    private static final ItemStack spearsIcon = new ItemStack(ItemType.SPEAR.getDefaultItem());
    private static final ItemStack bowsIcon = new ItemStack(ItemType.BOW.getDefaultItem());
    private static final ItemStack relikIcon = new ItemStack(ItemType.RELIK.getDefaultItem(), 1, ItemType.RELIK.getMeta());
    private static final ItemStack ringsIcon = new ItemStack(ItemType.RING.getDefaultItem());
    private static final ItemStack necklaceIcon = new ItemStack(ItemType.NECKLACE.getDefaultItem());
    private static final ItemStack braceletsIcon = new ItemStack(ItemType.BRACELET.getDefaultItem());

    static {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("Unbreakable", true);

        relikIcon.setTagCompound(compound);
    }

    public ItemPage() {
        super("Item Guide", true, IconContainer.itemGuideIcon);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int x = width / 2; int y = height / 2;
        int posX = (x - mouseX); int posY = (y - mouseY);
        List<String> hoveredText = new ArrayList<>();

        ScreenRenderer.beginGL(0, 0);
        {
            // order buttons
            render.drawString("Order the list by", x - 84, y - 30, CommonColors.BLACK, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);
            render.drawString("Alphabetical Order (A-Z)", x - 140, y - 15, CommonColors.BLACK, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);

            if (posX >= 144 && posX <= 150 && posY >= 8 && posY <= 15) {
                selected = 1;
                render.drawRect(Textures.UIs.quest_book, x - 150, y -15, 246, 259, 7, 7);
            } else {
                if (selected == 1) selected = 0;
                if (byAlphabetical) {
                    render.drawRect(Textures.UIs.quest_book, x - 150, y -15, 246, 259, 7, 7);
                } else {
                    render.drawRect(Textures.UIs.quest_book, x - 150, y -15, 254, 259, 7, 7);
                }
            }

            render.drawString("Level Order (100-0)", x - 140, y - 5, CommonColors.BLACK, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);

            if (posX >= 144 && posX <= 150 && posY >= -2 && posY <= 5) {
                selected = 2;
                render.drawRect(Textures.UIs.quest_book, x - 150, y -5, 246, 259, 7, 7);
            } else {
                if (selected == 2) selected = 0;
                if (byLevel) {
                    render.drawRect(Textures.UIs.quest_book, x - 150, y -5, 246, 259, 7, 7);
                } else {
                    render.drawRect(Textures.UIs.quest_book, x - 150, y -5, 254, 259, 7, 7);
                }
            }

            render.drawString("Rarity Order (MYTH-NORM)", x - 140, y + 5, CommonColors.BLACK, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);

            if (posX >= 144 && posX <= 150 && posY >= -12 && posY <= -5) {
                selected = 3;
                render.drawRect(Textures.UIs.quest_book, x - 150, y + 5, 246, 259, 7, 7);
            } else {
                if (selected == 3) selected = 0;
                if (byRarity) {
                    render.drawRect(Textures.UIs.quest_book, x - 150, y +5, 246, 259, 7, 7);
                } else {
                    render.drawRect(Textures.UIs.quest_book, x - 150, y +5, 254, 259, 7, 7);
                }
            }

            // filter ++
            render.drawString("Item Filter", x - 84, y + 20, CommonColors.BLACK, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);

            int placed = 0;
            int plusY = 0;
            for (int i = 0; i < 12; i++) {
                if (placed + 1 >= 7) {
                    placed = 0;
                    plusY ++;
                }

                int maxX = x - 139 + (placed * 20);
                int maxY = y + 50 + (plusY * 20);
                int minX = x - 123 + (placed * 20);
                int minY = y + 34 + (plusY * 20);

                if (mouseX >= maxX && mouseX <= minX && mouseY >= minY && mouseY <= maxY) {
                    render.drawRect(selected_cube, maxX, maxY, minX, minY);

                    selected = (i + 1) * 10;
                } else {
                    if (selected == (i + 1) * 10) selected = 0;

                    if (i == 0 && allowHelmet) render.drawRect(selected_cube_2, maxX, maxY, minX, minY);
                    else if (i == 1 && allowChestplate) render.drawRect(selected_cube_2, maxX, maxY, minX, minY);
                    else if (i == 2 && allowLeggings) render.drawRect(selected_cube_2, maxX, maxY, minX, minY);
                    else if (i == 3 && allowBoots) render.drawRect(selected_cube_2, maxX, maxY, minX, minY);
                    else if (i == 4 && allowWands) render.drawRect(selected_cube_2, maxX, maxY, minX, minY);
                    else if (i == 5 && allowDaggers) render.drawRect(selected_cube_2, maxX, maxY, minX, minY);
                    else if (i == 6 && allowSpears) render.drawRect(selected_cube_2, maxX, maxY, minX, minY);
                    else if (i == 7 && allowBows) render.drawRect(selected_cube_2, maxX, maxY, minX, minY);
                    else if (i == 8 && allowReliks) render.drawRect(selected_cube_2, maxX, maxY, minX, minY);
                    else if (i == 9 && allowNecklaces) render.drawRect(selected_cube_2, maxX, maxY, minX, minY);
                    else if (i == 10 && allowRings) render.drawRect(selected_cube_2, maxX, maxY, minX, minY);
                    else if (i == 11 && allowBracelets) render.drawRect(selected_cube_2, maxX, maxY, minX, minY);
                    else render.drawRect(unselected_cube, maxX, maxY, minX, minY);
                }

                if (i == 0) render.drawItemStack(helmetIcon, maxX, minY, false);
                else if (i == 1) render.drawItemStack(chestplateIcon, maxX, minY, false);
                else if (i == 2) render.drawItemStack(leggingsIcon, maxX, minY, false);
                else if (i == 3) render.drawItemStack(bootsIcon, maxX, minY, false);
                else if (i == 4) render.drawItemStack(wandsIcon, maxX, minY, false);
                else if (i == 5) render.drawItemStack(daggersIcon, maxX, minY, false);
                else if (i == 6) render.drawItemStack(spearsIcon, maxX, minY, false);
                else if (i == 7) render.drawItemStack(bowsIcon, maxX, minY, false);
                else if (i == 8) render.drawItemStack(relikIcon, maxX, minY, false);
                else if (i == 9) render.drawItemStack(necklaceIcon, maxX, minY, false);
                else if (i == 10) render.drawItemStack(ringsIcon, maxX, minY, false);
                else if (i == 11) render.drawItemStack(braceletsIcon, maxX, minY, false);

                placed++;
            }

            // back to menu button
            if (posX >= 74 && posX <= 90 && posY >= 37 & posY <= 46) {
                hoveredText = Arrays.asList(TextFormatting.GOLD + "[>] " + TextFormatting.BOLD + "Back to Menu", TextFormatting.GRAY + "Click here to go", TextFormatting.GRAY + "back to the main page", "", TextFormatting.GREEN + "Left click to select");
                render.drawRect(Textures.UIs.quest_book, x - 90, y - 46, 238, 234, 16, 9);
            } else {
                render.drawRect(Textures.UIs.quest_book, x - 90, y - 46, 222, 234, 16, 9);
            }

            render.drawString("Available Items", x + 80, y - 78, CommonColors.BLACK, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);

            // page counter including search
            int pages = itemSearch.size() <= 42 ? 1 : (int) Math.ceil(itemSearch.size() / 42d);
            if (pages < currentPage) {
                currentPage = pages;
            }

            if (byAlphabetical) itemSearch.sort((o1, o2) -> o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName()));
            if (byLevel) itemSearch.sort(Comparator.comparingInt(c -> -c.getRequirements().getLevel()));
            if (byRarity) itemSearch.sort(Comparator.comparingInt(c -> -c.getTier().getPriority()));

            render.drawString(currentPage + " / " + pages, x + 80, y + 88, CommonColors.BLACK, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);

            // but next and back button
            if (currentPage == pages) {
                render.drawRect(Textures.UIs.quest_book, x + 128, y + 88, 223, 222, 18, 10);
                acceptNext = false;
            } else {
                acceptNext = true;
                if (posX >= -145 && posX <= -127 && posY >= -97 && posY <= -88) {
                    render.drawRect(Textures.UIs.quest_book, x + 128, y + 88, 223, 222, 18, 10);
                } else {
                    render.drawRect(Textures.UIs.quest_book, x + 128, y + 88, 205, 222, 18, 10);
                }
            }

            if (currentPage == 1) {
                acceptBack = false;
                render.drawRect(Textures.UIs.quest_book, x + 13, y + 88, 241, 222, 18, 10);
            } else {
                acceptBack = true;
                if (posX >= -30 && posX <= -13 && posY >= -97 && posY <= -88) {
                    render.drawRect(Textures.UIs.quest_book, x + 13, y + 88, 241, 222, 18, 10);
                } else {
                    render.drawRect(Textures.UIs.quest_book, x + 13, y + 88, 259, 222, 18, 10);
                }
            }

            // available items
            int placedCubes = 0;
            int currentY = 0;
            for (int i = ((currentPage - 1) * 42); i < 42 * currentPage; i++) {
                if (itemSearch.size() <= i) break;

                if (placedCubes + 1 >= 7) {
                    placedCubes = 0;
                    currentY += 1;
                }

                int maxX = x + 22 + (placedCubes * 20);
                int maxY = y - 66 + (currentY * 20);
                int minX = x + 38 + (placedCubes * 20);
                int minY = y - 50 + (currentY * 20);


                ItemProfile pf = itemSearch.get(i);

                float r, g, b;

                switch (pf.getTier()) {
                    case MYTHIC:
                        r = 0.3f;
                        g = 0;
                        b = 0.3f;
                        break;
                    case FABLED:
                        r = 1f;
                        g = 0.58f;
                        b = 0.49f;
                        break;
                    case LEGENDARY:
                        r = 0;
                        g = 1;
                        b = 1;
                        break;
                    case RARE:
                        r = 1;
                        g = 0;
                        b = 1;
                        break;
                    case UNIQUE:
                        r = .8f;
                        g = .8f;
                        b = 0;
                        break;
                    case SET:
                        r = 0;
                        g = 1;
                        b = 0;
                        break;
                    case NORMAL:
                        r = 0.1f;
                        g = 0.1f;
                        b = 0.1f;
                        break;
                    default:
                        r = 0;
                        g = 0;
                        b = 0;
                        break;
                }


                if (mouseX >= maxX && mouseX <= minX && mouseY >= maxY && mouseY <= minY) {
                    GlStateManager.color(r, g, b, 0.5f);
                    GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
                    render.drawRect(Textures.UIs.rarity, maxX - 1, maxY - 1, 0, 0, 18, 18);
                    GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

                    if (pf.getGuideStack().isEmpty()) continue;

                    render.drawItemStack(pf.getGuideStack(), maxX, maxY, false);

                    ArrayList<String> lore = new ArrayList<>();
                    lore.add(pf.getGuideStack().getDisplayName());
                    lore.addAll(ItemUtils.getLore(pf.getGuideStack()));

                    hoveredText = lore;
                } else {

                    GlStateManager.color(r, g, b, 1.0f);
                    GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
                    render.drawRect(Textures.UIs.rarity, maxX - 1, maxY - 1, 0, 0, 18, 18);
                    GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

                    if (pf.getGuideStack().isEmpty()) continue;

                    render.drawItemStack(pf.getGuideStack(), maxX, maxY, false);
                }

                placedCubes++;
            }
        }
        ScreenRenderer.endGL();
        renderHoveredText(hoveredText, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ScaledResolution res = new ScaledResolution(mc);
        int posX = ((res.getScaledWidth()/2) - mouseX); int posY = ((res.getScaledHeight()/2) - mouseY);

        if (acceptNext && posX >= -145 && posX <= -127 && posY >= -97 && posY <= -88) {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1f));
            currentPage++;
            return;
        } else if (acceptBack && posX >= -30 && posX <= -13 && posY >= -97 && posY <= -88) {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1f));
            currentPage--;
            return;
        } else if (posX >= 74 && posX <= 90 && posY >= 37 & posY <= 46) {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1f));
            QuestBookPages.MAIN.getPage().open(false);
            return;
        } else if (selected == 1) {
            if (!byAlphabetical) {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1f));
                byAlphabetical = true;
                byLevel = false;
                byRarity = false;
            }
        } else if (selected == 2) {
            if (!byLevel) {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1f));
                byAlphabetical = false;
                byLevel = true;
                byRarity = false;
            }
        } else if (selected == 3) {
            if (!byRarity) {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1f));
                byRarity = true;
                byAlphabetical = false;
                byLevel = false;
            }
        } else if (selected >= 10) {
            switch (selected / 10) {
                case 1:
                    allowHelmet = !allowHelmet;
                    break;
                case 2:
                    allowChestplate = !allowChestplate;
                    break;
                case 3:
                    allowLeggings = !allowLeggings;
                    break;
                case 4:
                    allowBoots = !allowBoots;
                    break;
                case 5:
                    allowWands = !allowWands;
                    break;
                case 6:
                    allowDaggers = !allowDaggers;
                    break;
                case 7:
                    allowSpears = !allowSpears;
                    break;
                case 8:
                    allowBows = !allowBows;
                    break;
                case 9:
                    allowReliks = !allowReliks;
                    break;
                case 10:
                    allowNecklaces = !allowNecklaces;
                    break;
                case 11:
                    allowRings = !allowRings;
                    break;
                case 12:
                    allowBracelets = !allowBracelets;
                    break;
                default:
                    break;
            }
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1f));
            updateSearch();
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void searchUpdate(String currentText) {
        itemSearch = new ArrayList<>(WebManager.getDirectItems());

        itemSearch.removeIf(c -> {
            if (c.getItemInfo().getType() == null) return true;
              switch (c.getItemInfo().getType()) {
                  case HELMET: return !allowHelmet;
                  case CHESTPLATE: return !allowChestplate;
                  case BOOTS: return !allowBoots;
                  case LEGGINGS: return !allowLeggings;
                  case WAND: return !allowWands;
                  case SPEAR: return !allowSpears;
                  case DAGGER: return !allowDaggers;
                  case BOW: return !allowBows;
                  case RELIK: return !allowReliks;
                  case RING: return !allowRings;
                  case NECKLACE: return !allowNecklaces;
                  case BRACELET: return !allowBracelets;

                  default: return true;
              }
        });

        if (currentText != null && !currentText.isEmpty()) {
            String lowerText = currentText.toLowerCase();
            itemSearch.removeIf(c -> !doesSearchMatch(c.getDisplayName().toLowerCase(), lowerText));
        }
    }

    @Override
    public List<String> getHoveredDescription() {
        return Arrays.asList(TextFormatting.GOLD + "[>] " + TextFormatting.BOLD + "Item Guide", TextFormatting.GRAY + "See all items", TextFormatting.GRAY + "currently available", TextFormatting.GRAY + "in the game.", "", TextFormatting.GREEN + "Left click to select");
    }

}
