package ak.multitoolholders.inventory;

import ak.multitoolholders.ConfigUtils;
import ak.multitoolholders.ItemMultiToolHolder;
import javax.annotation.Nonnull;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;

public class SlotToolHolder extends Slot {

  public SlotToolHolder(IInventory inventoryIn, int index, int xPosition, int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
  }

  @Override
  public boolean isItemValid(@Nonnull ItemStack item) {
    return !(item.isStackable()) && (item.isDamageable() || item.getItem() instanceof ItemTool
        || isRegisteredTool(item)) && !(item.getItem() instanceof ItemMultiToolHolder);
  }

  private boolean isRegisteredTool(@Nonnull ItemStack itemStack) {
    return ConfigUtils.COMMON.toolNameSet
        .contains(itemStack.getItem().getRegistryName().toString());
  }
}