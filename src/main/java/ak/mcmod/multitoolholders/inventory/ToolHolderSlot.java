package ak.mcmod.multitoolholders.inventory;

import ak.mcmod.multitoolholders.ConfigUtils;
import ak.mcmod.multitoolholders.item.MultiToolHolderItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ToolHolderSlot extends Slot {

  public ToolHolderSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
  }

  @Override
  public boolean mayPlace(@Nonnull ItemStack item) {
    return !(item.isStackable()) && (item.isDamageableItem() || item.getItem() instanceof TieredItem
            || isRegisteredTool(item)) && !(item.getItem() instanceof MultiToolHolderItem);
  }

  private boolean isRegisteredTool(@Nonnull ItemStack itemStack) {
    return ConfigUtils.COMMON.toolNameSet
            .contains(Objects.requireNonNull(itemStack.getItem().getRegistryName()).toString());
  }
}