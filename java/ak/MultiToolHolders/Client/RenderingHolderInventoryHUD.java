package ak.MultiToolHolders.Client;

import ak.MultiToolHolders.ItemMultiToolHolder;
import ak.MultiToolHolders.MultiToolHolders;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

/**
 * Created by A.K. on 14/10/25.
 */
public class RenderingHolderInventoryHUD {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final RenderItem itemRenderer = new RenderItem();

    @SubscribeEvent
    public void renderingOverlay(RenderGameOverlayEvent.Text event) {
        EntityPlayer player = mc.thePlayer;
        ItemStack holdItem = player.getCurrentEquippedItem();
        if (holdItem != null && holdItem.getItem() instanceof ItemMultiToolHolder) {
            renderHolderInventory(holdItem, event.partialTicks);
        }
    }

    private void renderHolderInventory(ItemStack holder, float partialTicks) {
        IInventory inventory = ((ItemMultiToolHolder)holder.getItem()).getInventoryFromItemStack(holder);
        int nowSlot = ItemMultiToolHolder.getSlotNumFromItemStack(holder);
        ItemStack itemStack;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            itemStack = inventory.getStackInSlot(i);
            int xShift = (i == nowSlot) ? 16 : 0;
            if (itemStack != null) {
                renderInventorySlot(itemStack, MultiToolHolders.toolHolderInvX + xShift, MultiToolHolders.toolHolderInvY + i * 16);
            }
        }
    }

    private void renderInventorySlot(ItemStack itemstack, int par2, int par3) {
        if (itemstack != null){
            RenderHelper.enableGUIStandardItemLighting();
            itemRenderer.renderItemAndEffectIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), itemstack, par2, par3);
            itemRenderer.renderItemOverlayIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), itemstack, par2, par3);
            RenderHelper.disableStandardItemLighting();
//            String s = itemstack.getDisplayName();
//            this.mc.fontRenderer.drawStringWithShadow(s, par2 + 16, par3 + 4, 0xFFFFFF);
        }
    }
}
