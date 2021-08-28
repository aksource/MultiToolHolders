package ak.multitoolholders.client;

import ak.multitoolholders.ConfigUtils;
import ak.multitoolholders.item.MultiToolHolderItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * ツールホルダー内のアイテムをHUDに描画するクラス Created by A.K. on 14/10/25.
 */
public class RenderingHolderInventoryHUD {

  private final Minecraft mc = Minecraft.getInstance();
  private ItemRenderer itemRenderer;

  @SubscribeEvent
  @SuppressWarnings("unused")
  public void renderingOverlay(RenderGameOverlayEvent.Text event) {
    PlayerEntity player = mc.player;
    itemRenderer = mc.getItemRenderer();
    if (Objects.nonNull(player)) {
      ItemStack holdItem = player.getHeldItemMainhand();
      if (!holdItem.isEmpty() && holdItem.getItem() instanceof MultiToolHolderItem) {
        renderHolderInventory(holdItem, event.getPartialTicks());
      }
    }
  }

  private void renderHolderInventory(ItemStack holder, float partialTicks) {
    IInventory inventory = ((MultiToolHolderItem) holder.getItem())
        .getInventoryFromItemStack(holder);
    int nowSlot = MultiToolHolderItem.getSlotNumFromItemStack(holder);
    ItemStack itemStack;
    for (int i = 0; i < inventory.getSizeInventory(); i++) {
      itemStack = inventory.getStackInSlot(i);
      int xShift = (i == nowSlot) ? 16 : 0;
      if (!itemStack.isEmpty()) {
        renderInventorySlot(itemStack, ConfigUtils.COMMON.toolHolderInvX + xShift,
            ConfigUtils.COMMON.toolHolderInvY + i * 16);
      }
    }
  }

  private void renderInventorySlot(@Nonnull ItemStack itemstack, int x, int y) {
    if (!itemstack.isEmpty()) {
      RenderHelper.enableStandardItemLighting();
      itemRenderer.renderItemIntoGUI(itemstack, x, y);//Itemの描画
      itemRenderer.renderItemOverlays(this.mc.fontRenderer, itemstack, x, y);//耐久値の描画
      RenderHelper.disableStandardItemLighting();
    }
  }
}
