package ak.MultiToolHolders;

import ak.MultiToolHolders.Client.GuiToolHolder;
import ak.MultiToolHolders.inventory.ContainerToolHolder;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CommonProxy implements IGuiHandler
{
	public CommonProxy(){}
	public void registerClientInformation(){}

    public EntityPlayer getPlayer() {return null;}

	//returns an instance of the Container you made earlier
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (player.getCurrentEquippedItem() != null &&player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder) {
            ItemStack stack = player.getCurrentEquippedItem();
            ItemMultiToolHolder itemMultiToolHolder = (ItemMultiToolHolder)stack.getItem();
//            InventoryToolHolder inventoryToolHolder = itemMultiToolHolder.getInventoryFromItemStack(stack);
            return new ContainerToolHolder(player.inventory, stack, itemMultiToolHolder.inventorySize);
        }
        return null;
	}

	//returns an instance of the Gui you made earlier
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (player.getCurrentEquippedItem() != null &&player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder) {
            ItemStack stack = player.getCurrentEquippedItem();
            ItemMultiToolHolder itemMultiToolHolder = (ItemMultiToolHolder)stack.getItem();
//            InventoryToolHolder inventoryToolHolder = itemMultiToolHolder.getInventoryFromItemStack(stack);
            return new GuiToolHolder(player.inventory, stack, itemMultiToolHolder.inventorySize);
        }
        return null;
	}
}