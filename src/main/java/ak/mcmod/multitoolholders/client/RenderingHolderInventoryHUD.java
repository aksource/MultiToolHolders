package ak.mcmod.multitoolholders.client;

import ak.mcmod.multitoolholders.ConfigUtils;
import ak.mcmod.multitoolholders.item.MultiToolHolderItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
  public void renderingOverlay(RenderGameOverlayEvent.Text event) {
    Player player = mc.player;
    itemRenderer = mc.getItemRenderer();
    if (Objects.nonNull(player)) {
      ItemStack holdItem = player.getMainHandItem();
      if (!holdItem.isEmpty() && holdItem.getItem() instanceof MultiToolHolderItem) {
        renderHolderInventory(holdItem, event.getPartialTicks());
      }
    }
  }

  private void renderHolderInventory(ItemStack holder, float partialTicks) {
    var inventory = MultiToolHolderItem
            .getInventoryFromItemStack(holder);
    var nowSlot = MultiToolHolderItem.getSlotNumFromItemStack(holder);
    ItemStack itemStack;
    for (int i = 0; i < inventory.getContainerSize(); i++) {
      itemStack = inventory.getItem(i);
      var xShift = (i == nowSlot) ? 16 : 0;
      if (!itemStack.isEmpty()) {
        renderInventorySlot(itemStack, ConfigUtils.COMMON.toolHolderInvX + xShift,
                ConfigUtils.COMMON.toolHolderInvY + i * 16);
      }
    }
  }

  private void renderInventorySlot(@Nonnull ItemStack itemstack, int x, int y) {
    if (!itemstack.isEmpty()) {
//      RenderHelper.turnBackOn();
      itemRenderer.renderGuiItem(itemstack, x, y);//Itemの描画
      itemRenderer.renderGuiItemDecorations(this.mc.font, itemstack, x, y);//耐久値の描画
//      RenderHelper.turnOff();
    }
  }
}
