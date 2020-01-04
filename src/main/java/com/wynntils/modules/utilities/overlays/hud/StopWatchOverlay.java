/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.utilities.overlays.hud;

import com.wynntils.core.framework.overlays.Overlay;
import com.wynntils.core.framework.rendering.SmartFontRenderer;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.core.utils.StringUtils;
import com.wynntils.core.utils.helpers.LongPress;
import com.wynntils.modules.utilities.managers.KeyManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class StopWatchOverlay extends Overlay {

    // detects if the key is pressed for 2s and clean the stopwatch
    private static final LongPress longPressDetection = new LongPress(2000, () -> {
        startTime = -1; lastTime = -1;
    });

    private static long startTime = -1;
    private static long lastTime = -1;

    public static void start() {
        if (startTime == -1) {
            startTime = System.currentTimeMillis();
            return;
        }

        lastTime = System.currentTimeMillis() - startTime;
        startTime = -1;
    }

    public static void clear() {
        startTime = -1;
        lastTime = -1;
    }

    public StopWatchOverlay() {
        super("Stop Watch", 60, 10, true, 0, 1f, 10, -15, OverlayGrowFrom.TOP_LEFT, RenderGameOverlayEvent.ElementType.EXPERIENCE, RenderGameOverlayEvent.ElementType.JUMPBAR);
    }

    @Override
        public void render(RenderGameOverlayEvent.Pre event) {
        longPressDetection.tick(KeyManager.getStopwatchKey().getKeyBinding().isKeyDown());

        if ((startTime == -1 && lastTime == -1) || longPressDetection.isFinished()) return;

        if (startTime != -1) {
            drawString(StringUtils.millisToString(System.currentTimeMillis() - startTime), 0, 0, CommonColors.ORANGE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.OUTLINE);
            return;
        }

        drawString(StringUtils.millisToString(lastTime), 0, 0, CommonColors.RED, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.OUTLINE);
    }

}
