/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.core.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class Utils {

    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("wynntils-utilities-%d").build());
    private static Random random = new Random();

    /**
     * Runs a runnable after the determined time
     *
     * @param r the runnable
     * @param timeUnit the time unit
     * @param amount the amount of the specified time unit
     */
    public static ScheduledFuture<?> runAfter(Runnable r, TimeUnit timeUnit, long amount) {
        return executorService.scheduleAtFixedRate(r, 0, amount, timeUnit);
    }

    /**
     * @return the main random instance
     */
    public static Random getRandom() {
        return random;
    }

    public static ScheduledFuture runTaskTimer(Runnable r, TimeUnit timeUnit, long amount) {
        return executorService.scheduleAtFixedRate(r, 0, amount, timeUnit);
    }

    public static Future<?> runAsync(Runnable r) {
        return executorService.submit(r);
    }

    public static <T> Future<T> runAsync(Callable<T> r) {
        return executorService.submit(r);
    }

    private static final String[] directions = new String[]{ "N", "NE", "E", "SE", "S", "SW", "W", "NW" };

    /**
     * Get short direction string for a given yaw
     *
     * @param yaw player's yaw
     * @return Two or one character string
     */
    public static String getPlayerDirection(float yaw) {
        int index = (int) (MathHelper.positiveModulo(yaw + 202.5f, 360.0f) / 45.0f);

        return 0 <= index && index < 8 ? directions[index] : directions[0];
    }


    /**
     * Copy a file from a location to another
     *
     * @param sourceFile The source file
     * @param destFile Where it will be
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (destFile == null || !destFile.exists()) {
            destFile = new File(new File(sourceFile.getParentFile(), "mods"), "Wynntils.jar");
            sourceFile.renameTo(destFile);
            return;
        }

        try (FileChannel source = new FileInputStream(sourceFile).getChannel(); FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static float easeOut(float current, float goal, float jump, float speed) {
        if (Math.floor(Math.abs(goal - current) / jump) > 0) {
            return current + (goal - current) / speed;
        } else {
            return goal;
        }
    }

    public static String getPlayerHPBar(EntityPlayer entityPlayer) {
        int health = (int) (0.3f + (entityPlayer.getHealth() / entityPlayer.getMaxHealth()) * 15);  // 0.3f for better experience rounding off near full hp
        String healthBar = TextFormatting.DARK_RED + "[" + TextFormatting.RED + "|||||||||||||||" + TextFormatting.DARK_RED + "]";
        healthBar = healthBar.substring(0, 5 + Math.min(health, 15)) + TextFormatting.DARK_GRAY + healthBar.substring(5 + Math.min(health, 15));
        if (health < 8) { healthBar = healthBar.replace(TextFormatting.RED.toString(), TextFormatting.GOLD.toString()); }
        return healthBar;
    }

    /**
     * Creates a Fake scoreboard
     *
     * @param name Scoreboard Name
     * @param rule Collision Rule
     * @return the Scoreboard Team
     */
    public static ScorePlayerTeam createFakeScoreboard(String name, Team.CollisionRule rule) {
        Scoreboard mc = Minecraft.getMinecraft().world.getScoreboard();
        if (mc.getTeam(name) != null) return mc.getTeam(name);

        ScorePlayerTeam team = mc.createTeam(name);
        team.setCollisionRule(rule);

        mc.addPlayerToTeam(Minecraft.getMinecraft().player.getName(), name);
        return team;
    }

    /**
     * Deletes a fake scoreboard from existence
     *
     * @param name the scoreboard name
     */
    public static void removeFakeScoreboard(String name) {
        Scoreboard mc = Minecraft.getMinecraft().world.getScoreboard();
        if (mc.getTeam(name) == null) return;

        mc.removeTeam(mc.getTeam(name));
    }

    /**
     * Opens a guiScreen without cleaning the users keys/mouse movents
     *
     * @param screen the provided screen
     */
    public static void displayGuiScreen(GuiScreen screen) {
        Minecraft mc = Minecraft.getMinecraft();

        GuiScreen oldScreen = mc.currentScreen;

        GuiOpenEvent event = new GuiOpenEvent(screen);
        if (MinecraftForge.EVENT_BUS.post(event)) return;
        screen = event.getGui();

        if (oldScreen == screen) return;
        if (oldScreen != null) {
            oldScreen.onGuiClosed();
        }

        mc.currentScreen = screen;

        if (screen != null) {
            Minecraft.getMinecraft().setIngameNotInFocus();

            ScaledResolution scaledresolution = new ScaledResolution(mc);
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            screen.setWorldAndResolution(mc, i, j);
            mc.skipRenderWorld = false;
        } else {
            mc.getSoundHandler().resumeSounds();
            mc.setIngameFocus();
        }
    }

    private static int doubleClickTime = -1;

    /**
     * @return Maximum milliseconds between clicks to count as a double click
     */
    public static int getDoubleClickTime() {
        if (doubleClickTime < 0) {
            Object prop = Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
            if (prop instanceof Integer) {
                doubleClickTime = (Integer) prop;
            }
            if (doubleClickTime < 0) {
                doubleClickTime = 500;
            }
        }
        return doubleClickTime;
    }

    /**
     * Write a String, `s`, to the clipboard. Clears if `s` is null.
     */
    public static void copyToClipboard(String s) {
        if (s == null) {
            clearClipboard();
        } else {
            copyToClipboard(new StringSelection(s));
        }
    }

    public static void copyToClipboard(StringSelection s) {
        if (s == null) {
            clearClipboard();
        } else {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, null);
        }
    }

    public static void clearClipboard() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
            public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[0]; }
            public boolean isDataFlavorSupported(DataFlavor flavor) { return false; }
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException { throw new UnsupportedFlavorException(flavor); }
        }, null);
    }

    /**
     * @return A String read from the clipboard, or null if the clipboard does not contain a string
     */
    public static String pasteFromClipboard() {
        try {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            return null;
        }
    }

    public static void tab(GuiTextField... tabList) {
        tab(Arrays.asList(tabList));
    }

    /**
     * Given a list of text fields, blur the currently focused field and focus the
     * next one. Focuses the first one if there is no focused field or the last field is focused.
     */
    public static void tab(List<GuiTextField> tabList) {
        int focusIndex = -1;
        for (int i = 0; i < tabList.size(); ++i) {
            GuiTextField field = tabList.get(i);
            if (field.isFocused()) {
                focusIndex = i;
                field.setCursorPosition(0);
                field.setSelectionPos(0);
                field.setFocused(false);
                break;
            }
        }
        focusIndex = (focusIndex + 1) % tabList.size();
        GuiTextField selected = tabList.get(focusIndex);
        selected.setFocused(true);
        selected.setCursorPosition(0);
        selected.setSelectionPos(selected.getText().length());
    }

    // Alias if using already imported org.apache.commons.lang3.StringUtils
    public static class StringUtils extends com.wynntils.core.utils.StringUtils { }

}
