package ak.mcmod.multitoolholders;

import ak.mcmod.multitoolholders.item.MultiToolHolderItem;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
      PlayerEntity player = event.getPlayer();
      ItemStack stack = player.getMainHandItem();
      World world = event.getWorld();
      BlockPos blockPos = event.getPos();
      BlockState state = world.getBlockState(blockPos);
      int firstSlot = MultiToolHolderItem.getSlotNumFromItemStack(stack);
      int slot = firstSlot;
      ItemStack slotItem = MultiToolHolderItem.getInventoryFromItemStack(stack).getItem(slot);
      float miningSpeed = slotItem.getDestroySpeed(state);
      int inventorySize = MultiToolHolderItem.getInventoryFromItemStack(stack).getContainerSize();

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
