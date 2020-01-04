/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.core.instances;

import com.wynntils.ModCore;
import com.wynntils.Reference;
import com.wynntils.core.events.custom.PacketEvent;
import com.wynntils.core.framework.FrameworkManager;
import com.wynntils.core.framework.enums.FilterType;
import com.wynntils.core.utils.objects.Pair;
import com.wynntils.modules.core.managers.PacketQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Used for fake opening inventories that are opened by items
 * Just create the instance (title, clickItemPosition)
 * you can receive the items by setting up onReceiveItems
 * call #.open when you are ready to open the inventory and
 * call #.close whenever you finish using the inventory, otherwise
 * everything will bug and catch fire.
 */
public class FakeInventory {

    private static final CPacketPlayerTryUseItem rightClick = new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND);
    private static final CPacketPlayerDigging releaseClick = new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);

    public static final Packet<?> ignoredPacket = rightClick;

    Pattern windowTitle;
    int itemSlot;

    private Consumer<FakeInventory> onReceiveItems = null;
    private Consumer<FakeInventory> onInterrupt = null;
    private Consumer<FakeInventory> onClose = null;
    private int windowId = -1;
    private short transactionId = 0;
    private String realWindowTitle = "";
    private boolean interrupted = false;

    private NonNullList<ItemStack> items = NonNullList.create();

    boolean open = false;
    long openTime = System.currentTimeMillis();

    public FakeInventory(Pattern windowTitle, int itemSlot) {
        this.windowTitle = windowTitle;
        this.itemSlot = itemSlot;
    }

    public FakeInventory onReceiveItems(Consumer<FakeInventory> onReceiveItems) {
        this.onReceiveItems = onReceiveItems;

        return this;
    }

    public FakeInventory onClose(Consumer<FakeInventory> onClose) {
        this.onClose = onClose;

        return this;
    }

    /* If set, the fake inventory will be closed when the player tries to
     * interact with anything. Will not call the onClose callback.
     */
    public FakeInventory onInterrupt(Consumer<FakeInventory> onInterrupt) {
        this.onInterrupt = onInterrupt;

        return this;
    }

    /**
     * Request the inventory to be opened
     *
     * @return this
     */
    public FakeInventory open() {
        openTime = System.currentTimeMillis();

        FrameworkManager.getEventBus().register(this);

        Minecraft mc = ModCore.mc();

        PacketQueue.queueComplexPacket(rightClick, SPacketOpenWindow.class).setSender((conn, pack) -> {
            if (mc.player.inventory.currentItem != itemSlot) {
                conn.sendPacket(new CPacketHeldItemChange(itemSlot));
            }
            conn.sendPacket(pack);
            if (mc.player.inventory.currentItem != itemSlot) {
                conn.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
            }
        }).onDrop(() -> {
            if (Reference.developmentEnvironment) {
                ModCore.mc().player.sendMessage(new TextComponentString(TextFormatting.RED + "[FakeInventory packets dropped after " + (System.currentTimeMillis() - openTime) + "ms]"));
            }
            FrameworkManager.getEventBus().unregister(this);
            open = false;
            if (this.onInterrupt != null) this.onInterrupt.accept(this);
        });
        return this;
    }

    /**
     * Closes the fake inventory, it NEEDS to be called, otherwise the inventory
     * will NEVER close and will glitch EVERY SINGLE THING.
     *
     * don't forget to call this at any cost, please.
     */
    public void close() {
        close(true);
    }

    private void close(boolean callOnClose) {
        if (!open) return;

        FrameworkManager.getEventBus().unregister(this);
        open = false;

        if (windowId != -1) Minecraft.getMinecraft().getConnection().sendPacket(new CPacketCloseWindow(windowId));
        if (callOnClose && onClose != null) onClose.accept(this);
    }

    /**
     * Simulates a inventory click on a certain slot
     *
     * @param slot the input slot
     * @param mouseButton what mouse button was
     * @param type the typeof the click
     */
    public void clickItem(int slot, int mouseButton, ClickType type) {
        if (!open) return;

        transactionId++;
        Minecraft.getMinecraft().getConnection().sendPacket(new CPacketClickWindow(windowId, slot, mouseButton, type, items.get(slot), transactionId));
    }

    /**
     * Returns the ItemStack that is present at the provied slot
     * Should only be called if onReceiveItems was triggered
     *
     * @param slot the input slot
     * @return the ItemStack at the slot
     */
    public ItemStack getItem(int slot) {
        if (!open || items.size() >= slot) return ItemStack.EMPTY;

        return items.get(slot);
    }

    /**
     * Returns all the inventory items
     *
     * @return a list containing all the items
     */
    public List<ItemStack> getItems() {
        if (!open) return null;

        return items;
    }

    /**
     * Find a specific item at the inventory
     *
     * @param name       the item name
     * @param filterType the type of the filter
     * @return A pair with the slot number and the ItemStack
     */
    public Pair<Integer, ItemStack> findItem(String name, FilterType filterType) {
        if (!open) return null;

        if (filterType == FilterType.CONTAINS) {
            for (int slot = 0; slot < items.size(); slot++) {
                ItemStack item = items.get(slot);
                if (!item.isEmpty() && item.hasDisplayName() && TextFormatting.getTextWithoutFormattingCodes(item.getDisplayName()).contains(name)) {
                    return new Pair<>(slot, item);
                }
            }
        } else if (filterType == FilterType.EQUALS) {
            for (int slot = 0; slot < items.size(); slot++) {
                ItemStack item = items.get(slot);
                if (!item.isEmpty() && item.hasDisplayName() && TextFormatting.getTextWithoutFormattingCodes(item.getDisplayName()).equals(name)) {
                    return new Pair<>(slot, item);
                }
            }
        } else if (filterType == FilterType.EQUALS_IGNORE_CASE) {
            for (int slot = 0; slot < items.size(); slot++) {
                ItemStack item = items.get(slot);
                if (!item.isEmpty() && item.hasDisplayName() && TextFormatting.getTextWithoutFormattingCodes(item.getDisplayName()).equalsIgnoreCase(name)) {
                    return new Pair<>(slot, item);
                }
            }
        } else {
            for (int slot = 0; slot < items.size(); slot++) {
                ItemStack item = items.get(slot);
                if (!item.isEmpty() && item.hasDisplayName() && TextFormatting.getTextWithoutFormattingCodes(item.getDisplayName()).startsWith(name)) {
                    return new Pair<>(slot, item);
                }
            }
        }
        return null;
    }

    /**
     * Returns if the current FakeInventory is opened
     *
     * @return if the current FakeInventory is opened
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Returns the Inventory Title
     *
     * @return the inventory title
     */
    public String getWindowTitle() {
        return realWindowTitle;
    }

    public boolean isCrashed() {
        if (System.currentTimeMillis() - openTime >= 3000) {
            close();
            Reference.LOGGER.warn("Wynntils Inventory Analyse was interrupted because it crashed.");
            return true;
        }

        return false;
    }

    // EVENTS BELOW

    // handles the inventory container receive, sets open to true
    @SubscribeEvent
    public void onInventoryReceive(PacketEvent<SPacketOpenWindow> e) {
        if (!"minecraft:container".equals(e.getPacket().getGuiId()) || !e.getPacket().hasSlots() || !windowTitle.matcher(TextFormatting.getTextWithoutFormattingCodes(e.getPacket().getWindowTitle().getUnformattedText())).matches()) {
            windowId = -1;
            close();
            return;
        }

        windowId = e.getPacket().getWindowId();
        open = true;
        realWindowTitle = e.getPacket().getWindowTitle().getUnformattedText();

        e.setCanceled(true);

        if (interrupted) {
            windowId = -1;
            close(false);
        }
    }

    // handles the items, calls onReceiveItems
    @SubscribeEvent
    public void onItemsReceive(PacketEvent<SPacketWindowItems> e) {
        if (e.getPacket().getWindowId() != windowId) {
            windowId = -1;
            close();
            return;
        }

        items.clear();

        items.addAll(e.getPacket().getItemStacks());

        if (!interrupted && onReceiveItems != null) onReceiveItems.accept(this);

        e.setCanceled(true);

        if (interrupted) {
            windowId = -1;
            close(false);
        }
    }

    // cancel all other interactions to avoid GUI openings while this one is already opened

    private boolean shouldCancel(Event e) {
        if (interrupted || isCrashed()) return false;

        if (!e.isCanceled() && onInterrupt != null) {
            interrupted = true;
            ModCore.mc().getConnection().sendPacket(new CPacketCloseWindow(1));
            onInterrupt.accept(this);
            return false;
        }

        return open;
    }

    @SubscribeEvent
    public void cancelInteractItem(PacketEvent<CPacketPlayerTryUseItem> e) {
        if (!shouldCancel(e)) return;

        if (!e.isCanceled())
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString(TextFormatting.RED + "Your action was canceled because Wynntils is processing a background inventory."));

        e.setCanceled(true);
    }

    // cancel all other interactions to avoid GUI openings while this one is already opened
    @SubscribeEvent
    public void cancelInteractItemOnBlock(PacketEvent<CPacketPlayerTryUseItemOnBlock> e) {
        if (!shouldCancel(e)) return;

        if (!e.isCanceled())
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString(TextFormatting.RED + "Your action was canceled because Wynntils is processing a background inventory."));

        e.setCanceled(true);
    }

    // avoid teleportation while reading the questbook
    @SubscribeEvent
    public void cancelCommands(ClientChatEvent e) {
        if (!e.getMessage().startsWith("/class") || !e.getMessage().startsWith("/classes")) return;

        close();
    }

    // cancel the reading if the user changes the world
    @SubscribeEvent
    public void closeOnWorldLoad(WorldEvent.Load e) {
        if (!open || isCrashed()) return;

        close();
    }

}
