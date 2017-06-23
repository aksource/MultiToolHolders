package ak.MultiToolHolders.client;

import ak.MultiToolHolders.ItemMultiToolHolder;
import ak.MultiToolHolders.MultiToolHolders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 * ツールホルダー内のアイテムをHUDに描画するクラス
 * Created by A.K. on 14/10/25.
 */
public class RenderingHolderInventoryHUD {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final RenderItem itemRenderer = mc.getRenderItem();

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void renderingOverlay(RenderGameOverlayEvent.Text event) {
        EntityPlayer player = mc.player;
        ItemStack holdItem = player.getHeldItemMainhand();
        if (!holdItem.isEmpty() && holdItem.getItem() instanceof ItemMultiToolHolder) {
            renderHolderInventory(holdItem, event.getPartialTicks());
        }
    }

    private void renderHolderInventory(ItemStack holder, float partialTicks) {
        IInventory inventory = ((ItemMultiToolHolder)holder.getItem()).getInventoryFromItemStack(holder);
        int nowSlot = ItemMultiToolHolder.getSlotNumFromItemStack(holder);
        ItemStack itemStack;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            itemStack = inventory.getStackInSlot(i);
            int xShift = (i == nowSlot) ? 16 : 0;
            if (!itemStack.isEmpty()) {
                renderInventorySlot(itemStack, MultiToolHolders.toolHolderInvX + xShift, MultiToolHolders.toolHolderInvY + i * 16);
            }
        }
    }

    private void renderInventorySlot(@Nonnull ItemStack itemstack, int par2, int par3) {
        if (!itemstack.isEmpty()){
            RenderHelper.enableGUIStandardItemLighting();
            itemRenderer.renderItemIntoGUI(itemstack, par2, par3);//Itemの描画
            itemRenderer.renderItemOverlays(this.mc.fontRenderer, itemstack, par2, par3);//耐久値の描画
            RenderHelper.disableStandardItemLighting();
        }
    }
}
