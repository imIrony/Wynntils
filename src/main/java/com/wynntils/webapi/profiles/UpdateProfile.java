/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.webapi.profiles;

import com.wynntils.ModCore;
import com.wynntils.Reference;
import com.wynntils.core.utils.helpers.MD5Verification;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.core.enums.UpdateStream;
import com.wynntils.modules.core.overlays.UpdateOverlay;
import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.WebReader;

public class UpdateProfile {

    boolean hasUpdate = false;
    boolean updateCheckFailed = false;
    String latestUpdate = Reference.VERSION;

    private WebReader versions;

    public UpdateProfile() {
        new Thread(() -> {
            try {
                MD5Verification md5Installed = new MD5Verification(ModCore.jarFile);
                if (CoreDBConfig.INSTANCE.updateStream == UpdateStream.CUTTING_EDGE) {
                    String cuttingEdgeMd5 = WebManager.getCuttingEdgeJarFileMD5();
                    if (!md5Installed.equals(cuttingEdgeMd5)) {
                        hasUpdate = true;
                        latestUpdate = "B" + WebManager.getCuttingEdgeBuildNumber();
                        UpdateOverlay.reset();
                    }
                } else {
                    String stableMd5 = WebManager.getStableJarFileMD5();
                    if (!md5Installed.equals(stableMd5)) {
                        hasUpdate = true;
                        latestUpdate = WebManager.getStableJarVersion();
                        UpdateOverlay.reset();
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                updateCheckFailed = true;
            }
        }, "wynntils-updateprofile").start();
    }

    public boolean hasUpdate() {
        return hasUpdate;
    }

    public void forceUpdate() {
        UpdateOverlay.reset();
        hasUpdate = true;
    }

    public String getLatestUpdate() {
        return latestUpdate;
    }

    public boolean updateCheckFailed() {
        return updateCheckFailed;
    }
}
