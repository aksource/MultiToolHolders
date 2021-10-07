package ak.mcmod.multitoolholders;

import ak.mcmod.multitoolholders.item.ItemMultiToolHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * ツールホルダー用イベントフッククラス
 * Created by A.K. on 2017/03/08.
 */
public class ToolHolderEventHook {
  @SubscribeEvent
  public static void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock event) {
    if (MultiToolHolders.enableAutChange
            && !event.getWorld().isRemote
            && !(event.getEntityPlayer() instanceof FakePlayer)
            && !event.getEntityPlayer().getHeldItemMainhand().isEmpty()
            && event.getEntityPlayer().getHeldItemMainhand().getItem() instanceof ItemMultiToolHolder) {
      EntityPlayer player = event.getEntityPlayer();
      ItemStack stack = player.getHeldItemMainhand();
      World world = event.getWorld();
      BlockPos blockPos = event.getPos();
      IBlockState state = world.getBlockState(blockPos);
      int firstSlot = ItemMultiToolHolder.getSlotNumFromItemStack(stack);
      int slot = firstSlot;
      ItemStack slotItem = ItemMultiToolHolder.getInventoryFromItemStack(stack).getStackInSlot(slot);
      float miningSpeed = slotItem.getDestroySpeed(state);
      int inventorySize = ItemMultiToolHolder.getInventoryFromItemStack(stack).getSizeInventory();
      while (miningSpeed - 1.0F < 0.0001F) {
        slot = (slot + 1) % inventorySize;
        if (slot == firstSlot) break;
        slotItem = ItemMultiToolHolder.getInventoryFromItemStack(stack).getStackInSlot(slot);
        miningSpeed = slotItem.getDestroySpeed(state);
      }
      ItemMultiToolHolder.setSlotNumToItemStack(stack, slot);
    }
  }
}
