package ak.mcmod.multitoolholders.client;

import ak.mcmod.multitoolholders.MultiToolHolders;
import ak.mcmod.multitoolholders.item.ItemMultiToolHolder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * ツールホルダー内のアイテムをHUDに描画するクラス
 * Created by A.K. on 14/10/25.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RenderingHolderInventoryHUD {
  private final Minecraft mc = Minecraft.getMinecraft();
  private RenderItem itemRenderer;

  @SubscribeEvent
  public void renderingOverlay(RenderGameOverlayEvent.Text event) {
    EntityPlayer player = mc.player;
    itemRenderer = mc.getRenderItem();
    ItemStack holdItem = player.getHeldItemMainhand();
    if (!holdItem.isEmpty() && holdItem.getItem() instanceof ItemMultiToolHolder) {
      renderHolderInventory(holdItem, event.getPartialTicks());
    }
  }

  private void renderHolderInventory(ItemStack holder, float partialTicks) {
    IInventory inventory = ItemMultiToolHolder.getInventoryFromItemStack(holder);
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

  private void renderInventorySlot(ItemStack itemstack, int x, int y) {
    if (!itemstack.isEmpty()) {
      RenderHelper.enableGUIStandardItemLighting();
      itemRenderer.renderItemIntoGUI(itemstack, x, y);//Itemの描画
      itemRenderer.renderItemOverlays(this.mc.fontRenderer, itemstack, x, y);//耐久値の描画
      RenderHelper.disableStandardItemLighting();
    }
  }
}
