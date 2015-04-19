package ak.MultiToolHolders.inventory;

import ak.MultiToolHolders.ItemMultiToolHolder;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotToolHolder extends Slot
{

	public SlotToolHolder(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}

    @Override
    public boolean isItemValid(ItemStack item) {
        return !(item.isStackable()) && !(item.getItem() instanceof ItemMultiToolHolder);
    }
}