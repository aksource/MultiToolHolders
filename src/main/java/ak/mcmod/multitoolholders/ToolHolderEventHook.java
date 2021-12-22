package ak.mcmod.multitoolholders;

import ak.mcmod.multitoolholders.item.MultiToolHolderItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * ツールホルダー用イベントフッククラス Created by A.K. on 2017/03/08.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ToolHolderEventHook {
  @SubscribeEvent
  public static void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock event) {
    if (ConfigUtils.COMMON.enableAutoChange
            && !event.getWorld().isClientSide()
            && !(event.getPlayer() instanceof FakePlayer)
            && !event.getPlayer().getMainHandItem().isEmpty()
            && event.getPlayer().getMainHandItem().getItem() instanceof MultiToolHolderItem) {
      var player = event.getPlayer();
      var stack = player.getMainHandItem();
      var world = event.getWorld();
      var blockPos = event.getPos();
      var state = world.getBlockState(blockPos);
      var firstSlot = MultiToolHolderItem.getSlotNumFromItemStack(stack);
      var slot = firstSlot;
      var slotItem = MultiToolHolderItem.getInventoryFromItemStack(stack).getItem(slot);
      var miningSpeed = slotItem.getDestroySpeed(state);
      var inventorySize = MultiToolHolderItem.getInventoryFromItemStack(stack).getContainerSize();

      while (miningSpeed - 1.0F < 0.0001F) {
        slot = (slot + 1) % inventorySize;
        if (slot == firstSlot) break;
        slotItem = MultiToolHolderItem.getInventoryFromItemStack(stack).getItem(slot);
        miningSpeed = slotItem.getDestroySpeed(state);
      }
      MultiToolHolderItem.setSlotNumToItemStack(stack, slot);
    }
  }
}
