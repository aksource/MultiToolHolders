package ak.mcmod.multitoolholders.inventory;

import ak.mcmod.multitoolholders.MultiToolHolders;
import ak.mcmod.multitoolholders.item.ItemMultiToolHolder;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class SlotToolHolder extends Slot {

  public SlotToolHolder(IInventory inventoryIn, int index, int xPosition, int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
  }

  @Override
  public boolean isItemValid(ItemStack item) {
    return !(item.isStackable()) && (item.isItemStackDamageable() || item.getItem() instanceof ItemTool || isRegisteredTool(item)) && !(item.getItem() instanceof ItemMultiToolHolder);
  }

  private boolean isRegisteredTool(ItemStack itemStack) {
    return MultiToolHolders.toolNameSet.contains(Objects.requireNonNull(itemStack.getItem().getRegistryName()).toString());
  }
}