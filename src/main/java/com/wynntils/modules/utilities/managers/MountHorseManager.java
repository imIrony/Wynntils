/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.utilities.managers;

import com.wynntils.ModCore;
import com.wynntils.Reference;
import com.wynntils.modules.utilities.overlays.hud.GameUpdateOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class MountHorseManager {

    public enum MountHorseStatus {
        SUCCESS, ALREADY_RIDING, NO_HORSE, HORSE_TOO_FAR
    }

    private static final int searchRadius = 18;  // Search a bit further for message "too far" instead of "not found"

    public static boolean isPlayersHorse(Entity horse, String playerName) {
        return (horse instanceof AbstractHorse) && isPlayersHorse(horse.getCustomNameTag(), playerName);
    }

    public static boolean isPlayersHorse(String horseName, String playerName) {
        String defaultName = TextFormatting.WHITE + playerName + TextFormatting.GRAY + "'s horse";
        String customSuffix = TextFormatting.GRAY + " [" + playerName + "]";

        return defaultName.equals(horseName) || horseName.endsWith(customSuffix);
    }

    public static MountHorseStatus mountHorse() {
        Minecraft mc = ModCore.mc();
        EntityPlayerSP player = mc.player;
        if (player.isRiding()) {
            return MountHorseStatus.ALREADY_RIDING;
        }

        List<Entity> horses = mc.world.getEntitiesWithinAABB(AbstractHorse.class, new AxisAlignedBB(
                player.posX - searchRadius, player.posY - searchRadius, player.posZ - searchRadius,
                player.posX + searchRadius, player.posY + searchRadius, player.posZ + searchRadius
        ));

        Entity playersHorse = null;
        String playerName = player.getName();

        for (Entity horse : horses) {
            if (isPlayersHorse(horse, playerName)) {
                playersHorse = horse;
                break;
            }
        }

        if (playersHorse == null) {
            return MountHorseStatus.NO_HORSE;
        }

        double maxDistance = player.canEntityBeSeen(playersHorse) ? 36.0D : 9.0D;
        if (player.getDistanceSq(playersHorse) > maxDistance) {
            return MountHorseStatus.HORSE_TOO_FAR;
        }

        mc.playerController.interactWithEntity(player, playersHorse, EnumHand.MAIN_HAND);
        return MountHorseStatus.SUCCESS;
    }

    public static String getMountHorseErrorMessage(MountHorseStatus status) {
        switch (status) {
            case ALREADY_RIDING:
                Entity ridingEntity = ModCore.mc().player.getRidingEntity();
                String ridingEntityType;
                if (ridingEntity == null) {
                    ridingEntityType = "nothing?";
                } else if (ridingEntity instanceof AbstractHorse) {
                    ridingEntityType = "a horse";
                } else if (ridingEntity instanceof EntityBoat) {
                    ridingEntityType = "a boat";
                } else {
                    String name = ridingEntity.getName();
                    if (name == null) {
                        ridingEntityType = "something";
                    } else {
                        ridingEntityType = name;
                    }
                }
                return "You are already riding " + ridingEntityType;
            case NO_HORSE:
                return "Your horse was unable to be found";
            case HORSE_TOO_FAR:
                return "Your horse is too far away";
            default:
                return null;
        }

    }

    // Called on key press
    public static void mountHorseAndShowMessage() {
        String message = getMountHorseErrorMessage(mountHorse());
        if (message == null) return;

        GameUpdateOverlay.queueMessage(TextFormatting.DARK_RED + message);
    }

    // Called by event when a horse's metadata (name) is sent
    public static void mountHorseAndLogMessage() {
        String message = MountHorseManager.getMountHorseErrorMessage(MountHorseManager.mountHorse());
        if (message == null) return;

        Reference.LOGGER.warn("mountHorse failed onHorseSpawn. Reason: " + message);
    }

}
