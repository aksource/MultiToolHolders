package ak.MultiToolHolders.inventory;

import ak.MultiToolHolders.ItemMultiToolHolder;
import ak.MultiToolHolders.MultiToolHolders;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class SlotToolHolder extends Slot
{

	public SlotToolHolder(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

    @Override
    public boolean isItemValid(ItemStack item) {
        return !(item.isStackable()) && (item.isItemStackDamageable() || item.getItem() instanceof ItemTool || isRegisteredTool(item)) && !(item.getItem() instanceof ItemMultiToolHolder);
    }

    private boolean isRegisteredTool(ItemStack itemStack) {
        return MultiToolHolders.toolNameSet.contains(GameRegistry.findUniqueIdentifierFor(itemStack.getItem()).toString());
    }
}