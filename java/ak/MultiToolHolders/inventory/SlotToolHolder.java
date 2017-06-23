package ak.MultiToolHolders.inventory;

import ak.MultiToolHolders.ItemMultiToolHolder;
import ak.MultiToolHolders.MultiToolHolders;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;

import javax.annotation.Nonnull;

public class SlotToolHolder extends Slot
{

	public SlotToolHolder(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

    @Override
    public boolean isItemValid(@Nonnull ItemStack item) {
        return !(item.isStackable()) && (item.isItemStackDamageable() || item.getItem() instanceof ItemTool || isRegisteredTool(item)) && !(item.getItem() instanceof ItemMultiToolHolder);
    }

    private boolean isRegisteredTool(@Nonnull ItemStack itemStack) {
        return MultiToolHolders.toolNameSet.contains(itemStack.getItem().getRegistryName().toString());
    }
}