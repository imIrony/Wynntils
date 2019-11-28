/*
 *  * Copyright © Wynntils - 2019.
 */

package com.wynntils.modules.utilities.overlays.hud;

import com.wynntils.Reference;
import com.wynntils.core.framework.overlays.Overlay;
import com.wynntils.core.framework.rendering.SmartFontRenderer;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.modules.core.managers.TabManager;
import com.wynntils.modules.utilities.configs.OverlayConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerInfoOverlay extends Overlay {

    public PlayerInfoOverlay() {
        super("Player Info Overlay", 410, 239, true, 0.5f, 0f, 0, 10, OverlayGrowFrom.TOP_CENTRE);
    }

    double animationProgress = 0;

    @Override
    public void render(RenderGameOverlayEvent.Post event) {
        if(!Reference.onWorld || !OverlayConfig.PlayerInfo.INSTANCE.replaceVanilla) return;

        //TODO make the animation be TIME, instead of FRAME, reliant. This is currently causing some slowdowns
        { // Animation Detection
            if (mc.gameSettings.keyBindPlayerList.isKeyDown()) {
                if (animationProgress < 1.0) animationProgress += 0.02;
            } else if (animationProgress > 0.0) {
                animationProgress -= 0.02;
            }

            if (animationProgress <= 0.0) return;
        }

        //scales if the screen don't fit the texture height
        float yScale = screen.getScaledHeight() < 280f ? (float)screen.getScaledHeight_double() / 280f : 1;

        { scale(yScale);

            { //mask
                createMask(Textures.Masks.full,
                            -(int) (178 * animationProgress),
                            0,
                            (int) (178 * animationProgress),
                            232, 0, 0, 1, 1);

                color(1f, 1f, 1f, OverlayConfig.PlayerInfo.INSTANCE.backgroundAlpha); //apply transparency
                drawRect(Textures.UIs.tab_overlay, -178, 0, 178, 226, 28, 6, 385, 232);
                color(1f, 1f, 1f, 1f);

                { //titles
                    drawString("Friends", -124, 7, CommonColors.BLACK, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);
                    drawString("Global " + Reference.getUserWorld(), -39, 7, CommonColors.BLACK, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);
                    drawString("Party", 47, 7, CommonColors.BLACK, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);
                    drawString("Guild", 133, 7, CommonColors.BLACK, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);
                }

                { //entries
                    List<String> players = getAvailablePlayers();

                    for(int x = 0; x < 4; x++) {
                        for(int y = 0; y < 20; y++) {
                            int position = (x * 20) + (y+1);

                            if(players.size() < position) break; //not enough players

                            String entry = players.get(position-1);
                            if(entry.contains("§l")) continue; //avoid the titles

                            int xPos = -166 + (87 * x);
                            int yPos = 11 + (10 * y);

                            drawString(entry, xPos, yPos,
                                    CommonColors.BLACK, SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                                    SmartFontRenderer.TextShadow.NONE);
                        }
                    }
                }

            } clearMask();

            color(1f, 1f, 1f, OverlayConfig.PlayerInfo.INSTANCE.backgroundAlpha); //apply transparency
            { //paper rolls
                drawRect(Textures.UIs.tab_overlay,
                        (int) (177 * animationProgress),
                        -5,
                        (int) (27 + (177 * animationProgress)),
                        239, 0, 0, 27, 239);

                drawRect(Textures.UIs.tab_overlay,
                        -(int) (27 + (177 * animationProgress)),
                        -5,
                        -(int) (177 * animationProgress),
                        239, 0, 0, 27, 239);
            }
            color(1f, 1f, 1f, 1f);

        } resetScale();

    }

    private static List<String> getAvailablePlayers() {
        List<NetworkPlayerInfo> players = TabManager.getEntryOrdering()
                .sortedCopy(Minecraft.getMinecraft().player.connection.getPlayerInfoMap());

        return players.stream()
                .map(c -> wrapText(c.getDisplayName().getUnformattedText().replace("§7", "§0"), 73))
                .collect(Collectors.toList());
    }

    private static String wrapText(String input, int maxLenght) {
        if(fontRenderer.getStringWidth(input) <= maxLenght) return input;

        StringBuilder builder = new StringBuilder();
        for(char c : input.toCharArray()) {
            if(fontRenderer.getStringWidth(builder.toString() + c) > maxLenght) break;

            builder.append(c);
        }

        return builder.toString();
    }

}
