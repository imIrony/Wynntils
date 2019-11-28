/*
 *  * Copyright © Wynntils - 2019.
 */

package com.wynntils.modules.core.events;

import com.wynntils.ModCore;
import com.wynntils.Reference;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.enums.ClassType;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.utils.Utils;
import com.wynntils.core.utils.reflections.ReflectionFields;
import com.wynntils.modules.core.instances.MainMenuButtons;
import com.wynntils.modules.core.managers.PacketQueue;
import com.wynntils.modules.core.managers.PingManager;
import com.wynntils.modules.core.overlays.inventories.ChestReplacer;
import com.wynntils.modules.core.overlays.inventories.HorseReplacer;
import com.wynntils.modules.core.overlays.inventories.IngameMenuReplacer;
import com.wynntils.modules.core.overlays.inventories.InventoryReplacer;
import net.minecraft.block.BlockBarrier;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Locale;

public class ClientEvents implements Listener {

    /**
     * This replace these GUIS into a "provided" format to make it more modular
     *
     * GuiInventory -> InventoryReplacer
     * GuiChest -> ChestReplacer
     * GuiScreenHorseInventory -> HorseReplacer
     * GuiIngameMenu -> IngameMenuReplacer
     *
     * Since forge doesn't provides any way to intercept these guis, like events, we need to replace them
     * this may cause conflicts with other mods that does the same thing
     *
     * @see InventoryReplacer
     * @see ChestReplacer
     * @see HorseReplacer
     * @see IngameMenuReplacer
     *
     * All of these "class replacers" emits a bunch of events that you can use to edit the selected GUI
     *
     * @param e GuiOpenEvent
     */
    @SubscribeEvent
    public void onGuiOpened(GuiOpenEvent e) {
        if(e.getGui() instanceof GuiInventory) {
            if(e.getGui() instanceof InventoryReplacer) return;

            e.setGui(new InventoryReplacer(ModCore.mc().player));
            return;
        }
        if(e.getGui() instanceof GuiChest) {
            if(e.getGui() instanceof ChestReplacer) return;

            e.setGui(new ChestReplacer(ModCore.mc().player.inventory, (IInventory) ReflectionFields.GuiChest_lowerChestInventory.getValue(e.getGui())));
            return;
        }
        if(e.getGui() instanceof GuiScreenHorseInventory) {
            if(e.getGui() instanceof HorseReplacer) return;

            e.setGui(new HorseReplacer(ModCore.mc().player.inventory, (IInventory) ReflectionFields.GuiScreenHorseInventory_horseInventory.getValue(e.getGui()), (AbstractHorse) ReflectionFields.GuiScreenHorseInventory_horseEntity.getValue(e.getGui())));
        }
        if(e.getGui() instanceof GuiIngameMenu) {
            if(e.getGui() instanceof IngameMenuReplacer) return;

            e.setGui(new IngameMenuReplacer());
        }
    }

    /**
     * Detects the user class based on the class selection GUI
     * This detection happens when the user click on an item that contains the class name pattern, inside the class selection GUI
     *
     * @param e Represents the click event
     */
    @SubscribeEvent
    public void changeClass(GuiOverlapEvent.ChestOverlap.HandleMouseClick e) {
        if(e.getGuiInventory().getLowerInv().getName().contains("Select a Class")) {
            if(e.getMouseButton() == 0 && e.getSlotIn() != null &&  e.getSlotIn().getHasStack() && e.getSlotIn().getStack().hasDisplayName() && e.getSlotIn().getStack().getDisplayName().contains("[>] Select")) {
                PlayerInfo.getPlayerInfo().setClassId(e.getSlotId());

                String classLore = Utils.getLore(e.getSlotIn().getStack()).get(1);
                String classS = classLore.substring(classLore.indexOf(TextFormatting.WHITE.toString()) + 2);

                ClassType selectedClass = ClassType.NONE;

                try{
                    selectedClass = ClassType.valueOf(classS.toUpperCase(Locale.ROOT));
                }catch (Exception ex) {
                    switch(classS) {
                        case "Hunter":
                            selectedClass = ClassType.ARCHER;
                            break;
                        case "Knight":
                            selectedClass = ClassType.WARRIOR;
                            break;
                        case "Dark Wizard":
                            selectedClass = ClassType.MAGE;
                            break;
                        case "Ninja":
                            selectedClass = ClassType.ASSASSIN;
                            break;
                        case "Skyseer":
                            selectedClass = ClassType.SHAMAN;
                            break;
                    }
                }

                PlayerInfo.getPlayerInfo().updatePlayerClass(selectedClass);
            }
        }
    }

    /**
     * Prevents player entities from rendering if they're supposed to be invisible (as in a Spectator or have Invisibility)
     */
    @SubscribeEvent
    public void removeInvisiblePlayers(RenderPlayerEvent.Pre e) {
        if(!Reference.onWorld || e.getEntityPlayer() == null) return;

        //HeyZeer0: this verifies based if there's a barrier block below the player, it will also helps
        //if the player is inside a dungeon | Main Use = cutscene
        EntityPlayer player = e.getEntityPlayer();
        if(!(player.world.getBlockState(new BlockPos(player.posX, player.posY-1, player.posZ)).getBlock() instanceof BlockBarrier)) return;

        e.setCanceled(true);
    }

    /**
     * Process the packet queue if the queue is not empty
     */
    @SubscribeEvent
    public void proccessPacketQueue(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !PacketQueue.hasQueuedPacket()) return;

        PingManager.calculatePing();
        PacketQueue.proccessQueue();
    }

    /**
     *  Register the new Main Menu buttons
     */
    @SubscribeEvent
    public void addMainMenuButtons(GuiScreenEvent.InitGuiEvent.Post e) {
        GuiScreen gui = e.getGui();
        if(gui != gui.mc.currentScreen || !(gui instanceof GuiMainMenu)) return;

        MainMenuButtons.addButtons((GuiMainMenu) gui, e.getButtonList());
    }

    /**
     *  Handles the main menu new buttons actions
     */
    @SubscribeEvent
    public void mainMenuActionPerformed(GuiScreenEvent.ActionPerformedEvent.Post e) {
        GuiScreen gui = e.getGui();
        if(gui != gui.mc.currentScreen || !(gui instanceof GuiMainMenu)) return;

        MainMenuButtons.actionPerformed((GuiMainMenu) gui, e.getButton(), e.getButtonList());
    }

}
