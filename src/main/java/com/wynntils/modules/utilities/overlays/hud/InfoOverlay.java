/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.utilities.overlays.hud;

import com.wynntils.Reference;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.overlays.Overlay;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.core.framework.rendering.colors.CustomColor;
import com.wynntils.core.utils.Utils;
import com.wynntils.core.utils.reference.EmeraldSymbols;
import com.wynntils.modules.core.managers.PingManager;
import com.wynntils.modules.richpresence.RichPresenceModule;
import com.wynntils.modules.utilities.configs.OverlayConfig;
import com.wynntils.modules.utilities.managers.SpeedometerManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class InfoOverlay extends Overlay {
    private static final CustomColor backgroundColour = new CustomColor(CommonColors.BLACK);
    private static final Pattern colourRegex = Pattern.compile("§[^kKlLmMnNoOrR]");

    private InfoOverlay(int index) {
        super("Info " + index, 100, 9, true, 0, 0, 10, 105 + index * 11, OverlayGrowFrom.TOP_LEFT);
    }

    public abstract int getIndex();
    public abstract String getFormat();

    @Override
    public void render(RenderGameOverlayEvent.Pre e) {
        if (!Reference.onWorld || e.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        String format = getFormat();
        if (format != null && !format.isEmpty()) {
            String formatted = doFormat(format, formatter);
            if (!formatted.isEmpty()) {
                float center = staticSize.x / 2f;
                String[] lines = formatted.split("\n");
                int nLines = lines.length;
                int[] lineWidths = new int[nLines];
                for (int i = 0; i < nLines; ++i) {
                    lineWidths[i] = mc.fontRenderer.getStringWidth(lines[i]);
                }

                if (OverlayConfig.InfoOverlays.INSTANCE.opacity != 0) {
                    int height = 11 * nLines;

                    int width = 0;
                    for (int currentWidth : lineWidths) {
                        if (currentWidth > width) {
                            width = currentWidth;
                        }
                    }

                    drawRect(backgroundColour, (int) (center - width / 2f - 1.5f), 0, (int) (center + width / 2f + 1.5f), height - 2);
                }

                center += drawingOrigin().x;
                int y = -10 + drawingOrigin().y;
                switch (OverlayConfig.InfoOverlays.INSTANCE.textShadow) {
                    // This switch could have been inside the for loop,
                    // but since this is run up to 4 times every frame, it has been hoisted
                    case OUTLINE:
                        for (int i = 0; i < nLines; ++i) {
                            int x = (int) (center - lineWidths[i] / 2f);
                            y += 11;

                            // Render outline
                            String withoutColours = colourRegex.matcher(lines[i]).replaceAll("§r");
                            mc.fontRenderer.drawString(withoutColours, x - 1, y, 0xFF000000, false);
                            mc.fontRenderer.drawString(withoutColours, x + 1, y, 0xFF000000, false);
                            mc.fontRenderer.drawString(withoutColours, x, y - 1, 0xFF000000, false);
                            mc.fontRenderer.drawString(withoutColours, x, y + 1, 0xFF000000, false);

                            mc.fontRenderer.drawString(lines[i], x, y, 0xFFFFFFFF, false);
                        }
                        break;

                    case NORMAL:
                        for (int i = 0; i < nLines; ++i) {
                            int x = (int) (center - lineWidths[i] / 2f);
                            y += 11;

                            mc.fontRenderer.drawString(lines[i], x, y, 0xFFFFFFFF, true);
                        }
                        break;

                    default:
                        for (int i = 0; i < nLines; ++i) {
                            int x = (int) (center - lineWidths[i] / 2f);
                            y += 11;

                            mc.fontRenderer.drawString(lines[i], x, y, 0xFFFFFFFF, false);
                        }
                        break;
                }
            }
        }

    }

    public static class _1 extends InfoOverlay {
        public _1() { super(1); }
        @Override public final int getIndex() { return 1; }
        @Override public final String getFormat() { return OverlayConfig.InfoOverlays.INSTANCE.info1Format; }
        @Override public void render(RenderGameOverlayEvent.Pre e) {
            formatter.clear();
            backgroundColour.setA(OverlayConfig.InfoOverlays.INSTANCE.opacity / 100f);
            super.render(e);
        }
    }

    public static class _2 extends InfoOverlay {
        public _2() { super(2); }
        @Override public final int getIndex() { return 2; }
        @Override public final String getFormat() { return OverlayConfig.InfoOverlays.INSTANCE.info2Format; }
    }

    public static class _3 extends InfoOverlay {
        public _3() { super(3); }
        @Override public final int getIndex() { return 3; }
        @Override public final String getFormat() { return OverlayConfig.InfoOverlays.INSTANCE.info3Format; }
    }

    public static class _4 extends InfoOverlay {
        public _4() { super(4); }
        @Override public final int getIndex() { return 4; }
        @Override public final String getFormat() { return OverlayConfig.InfoOverlays.INSTANCE.info4Format; }
    }

    private static final Pattern formatRegex = Pattern.compile(
            "%([a-zA-Z_]+|%)%|\\\\([\\\\n%§EBL]|x[0-9A-Fa-f]{2}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})"
    );

    private static InfoFormatter formatter = new InfoFormatter();

    private static class InfoFormatter {
        int money = -1;
        long maxMemory;
        long usedMemory;
        int memoryPct = -1;
        PlayerInfo.UnprocessedAmount unprocessed;

        void clear() {
            money = -1;
            unprocessed = null;
            memoryPct = -1;
        }

        int getMoney() {
            if (money == -1) money = PlayerInfo.getPlayerInfo().getMoney();
            return money;
        }

        PlayerInfo.UnprocessedAmount getUnprocessed() {
            if (unprocessed == null) unprocessed = PlayerInfo.getPlayerInfo().getUnprocessedAmount();
            return unprocessed;
        }

        void setMemory() {
            if (memoryPct == -1) {
                long maxMemory = Runtime.getRuntime().maxMemory();
                long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                memoryPct = (int) (((float) usedMemory / maxMemory) * 100f);
                this.maxMemory = maxMemory / (1024 * 1024);
                this.usedMemory = usedMemory / (1024 * 1024);
            }
        }

        String doPctFormat(String name) {
            String lowerName = name.toLowerCase(Locale.ROOT);
            switch (lowerName) {
                case "x": return Integer.toString((int) mc.player.posX);
                case "y": return Integer.toString((int) mc.player.posY);
                case "z": return Integer.toString((int) mc.player.posZ);
                case "dir": return Utils.getPlayerDirection(mc.player.rotationYaw);
                case "fps": return Integer.toString(Minecraft.getDebugFPS());
                case "world": return Reference.getUserWorld();
                case "loc": case "location": return RichPresenceModule.getModule().getData().getLocation();
                case "ping":
                    PingManager.calculatePing();
                    return Long.toString(PingManager.getLastPing());
                case "class":
                    String className;
                    switch (PlayerInfo.getPlayerInfo().getCurrentClass()) {
                        case MAGE: className = "mage"; break;
                        case ARCHER: className = "archer"; break;
                        case WARRIOR: className = "warrior"; break;
                        case ASSASSIN: className = "assassin"; break;
                        case SHAMAN: className = "shaman"; break;
                        default: className = null; break;
                    }

                    if (className != null) {
                        if (name.equals("Class")) {  // %Class% is title case
                            className = StringUtils.capitalize(className);
                        } else if (name.equals("CLASS")) {  // %CLASS% is all caps
                            className = className.toUpperCase();
                        }
                        return className;
                    }
                    return null;
                case "lvl":
                    int lvl = PlayerInfo.getPlayerInfo().getLevel();
                    if (lvl != -1) {
                        return Integer.toString(lvl);
                    }
                    return null;
                case "money":
                    return Integer.toString(getMoney());
                case "emeralds":
                    return Integer.toString(getMoney() % 64);
                case "blocks": case "emeraldblocks": case "emerald_blocks":
                    return Integer.toString((getMoney() / 64) % 64);
                case "le": case "lightemeralds": case "light_emeralds":
                    return Integer.toString(getMoney() / (64 * 64));
                case "unprocessed":
                    return Integer.toString(getUnprocessed().current);
                case "unprocessed_max": case "unprocessedmax":
                    int max = getUnprocessed().maximum;
                    if (max == -1) {
                        return "??";
                    }
                    return Integer.toString(max);
                case "mem_pct": case "mempct":
                    setMemory();
                    return Integer.toString(memoryPct);
                case "mem_used": case "memused":
                    setMemory();
                    return Long.toString(usedMemory);
                case "mem_max": case "memmax":
                    setMemory();
                    return Long.toString(maxMemory);
                case "bps":
                    return PlayerInfo.perFormat.format(SpeedometerManager.getCurrentSpeed());
                case "bpm":
                    return PlayerInfo.perFormat.format(SpeedometerManager.getCurrentSpeed() * 60);
                case "%":
                    return "%";
                default:
                    return null;
            }
        }

        String doEscapeFormat(String escaped) {
            switch (escaped) {
                case "\\": return "\\";
                case "n": return "\n";
                case "%": return "%";
                case "§": return "&";
                case "E": return EmeraldSymbols.E_STRING;
                case "B": return EmeraldSymbols.B_STRING;
                case "L": return EmeraldSymbols.L_STRING;
                default:
                    // xXX, uXXXX, UXXXXXXXX
                    int codePoint = Integer.parseInt(escaped.substring(1), 16);
                    if (Utils.StringUtils.isValidCodePoint(codePoint)) {
                        return new String(new int[]{ codePoint }, 0, 1);
                    }
                    return null;
            }
        }

        String doFormat(String format) {
            StringBuffer sb = new StringBuffer(format.length() + 10);
            Matcher m = formatRegex.matcher(format);
            while (m.find()) {
                String replacement = null;
                String group;
                if ((group = m.group(1)) != null) {
                    // %name%
                    replacement = doPctFormat(group);
                } else if ((group = m.group(2)) != null) {
                    // \escape
                    replacement = doEscapeFormat(group);
                }
                if (replacement == null) {
                    replacement = m.group(0);
                }
                m.appendReplacement(sb, replacement);
            }
            m.appendTail(sb);

            return sb.toString();
        }
    }

    public static String doFormat(String format) {
        return doFormat(format, new InfoFormatter());
    }

    private static String doFormat(String format, InfoFormatter formatter) {
        return formatter.doFormat(format);
    }
}
