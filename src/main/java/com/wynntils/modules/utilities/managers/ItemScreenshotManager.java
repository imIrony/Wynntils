package com.wynntils.modules.utilities.managers;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.BufferUtils;

import com.wynntils.ModCore;
import com.wynntils.Reference;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;

public class ItemScreenshotManager {
    
    private static Pattern ITEM_PATTERN = Pattern.compile("(Normal|Set|Unique|Rare|Legendary|Fabled|Mythic) Item.*");
    
    public static void takeScreenshot() {
        if (!Reference.onWorld) return;
        GuiScreen gui = ModCore.mc().currentScreen;
        if (!(gui instanceof GuiContainer)) return;
        
        Slot slot = ((GuiContainer) gui).getSlotUnderMouse();
        if (slot == null || !slot.getHasStack()) return;
        ItemStack stack = slot.getStack();
        if (!stack.hasDisplayName()) return;
        
        List<String> tooltip = stack.getTooltip(ModCore.mc().player, ITooltipFlag.TooltipFlags.NORMAL);
        removeItemLore(tooltip);
        
        FontRenderer fr = ModCore.mc().fontRenderer;
        int width = 0;
        int height = 16;
        
        // calculate width of tooltip
        for (String s : tooltip) {
            int w = fr.getStringWidth(s);
            if (w > width) width = w;
        }
        width += 8;
        
        // calculate height of tooltip
        if (tooltip.size() > 1) height += 2 + (tooltip.size() - 1) * 10;
        
        // account for text wrapping
        if (width > gui.width/2 + 8) {
            int wrappedWidth = 0;
            int wrappedLines = 0;
            for (String s : tooltip) {
                List<String> wrappedLine = fr.listFormattedStringToWidth(s, gui.width/2);
                for (String ws : wrappedLine) {
                    wrappedLines++;
                    int w = fr.getStringWidth(ws);
                    if (w > wrappedWidth) wrappedWidth = w;
                }
            }
            width = wrappedWidth + 8;
            height = 16 + (2 + (wrappedLines - 1) * 10);
        }
        
        // calculate scale of tooltip to fit it to the framebuffer
        float scaleh = (float) gui.height/height;
        float scalew = (float) gui.width/width;
        
        // draw tooltip to framebuffer, create image from it
        GlStateManager.pushMatrix();
        Framebuffer fb = new Framebuffer((int) (gui.width*(1/scalew)*2), (int) (gui.height*(1/scaleh)*2), true);
        fb.bindFramebuffer(false);
        GlStateManager.scale(scalew, scaleh, 1);
        GuiUtils.drawHoveringText(tooltip, -8, 8, gui.width, gui.height, gui.width/2, fr);
        BufferedImage bi = createScreenshot(width*2, height*2);
        fb.unbindFramebuffer();
        GlStateManager.popMatrix();
        
        // copy to clipboard
        ClipboardImage ci = new ClipboardImage(bi);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ci, null);
        
        ModCore.mc().player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Copied " + stack.getDisplayName() + TextFormatting.GREEN + " to the clipboard!"));
    }
    
    private static void removeItemLore(List<String> tooltip) {
        // iterate through each line of the tooltip and remove item lore
        List<String> temp = new ArrayList<>();
        boolean lore = false;
        for (String s : tooltip) {
            // only remove text after the item type indicator
            Matcher m = ITEM_PATTERN.matcher(TextFormatting.getTextWithoutFormattingCodes(s));
            if (!lore && m.matches()) lore = true;
            
            if (lore && s.contains("" + TextFormatting.DARK_GRAY)) temp.add(s);
        }
        tooltip.removeAll(temp);
    }
    
    private static BufferedImage createScreenshot(int width, int height) {
        // create pixel arrays
        int i = width * height;
        IntBuffer pixelBuffer = BufferUtils.createIntBuffer(i);
        int[] pixelValues = new int[i];

        GlStateManager.glPixelStorei(3333, 1);
        GlStateManager.glPixelStorei(3317, 1);
        pixelBuffer.clear();

        // create image from pixels
        GlStateManager.glReadPixels(0, 0, width, height, 32993, 33639, pixelBuffer);
        pixelBuffer.get(pixelValues);
        TextureUtil.processPixelValues(pixelValues, width, height);
        BufferedImage bufferedimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferedimage.setRGB(0, 0, width, height, pixelValues, 0, width);
        return bufferedimage;
    }
    
    private static class ClipboardImage implements Transferable {
        
        Image image;
        
        public ClipboardImage(Image image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.imageFlavor };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!DataFlavor.imageFlavor.equals(flavor)) throw new UnsupportedFlavorException(flavor);
            return this.image;
        }
        
    }

}
