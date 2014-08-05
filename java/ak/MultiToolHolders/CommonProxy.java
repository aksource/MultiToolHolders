package ak.MultiToolHolders;

import ak.MultiToolHolders.Client.GuiToolHolder;
import ak.MultiToolHolders.inventory.ContainerToolHolder;
import ak.MultiToolHolders.inventory.InventoryToolHolder;
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
            InventoryToolHolder inventoryToolHolder = new InventoryToolHolder(stack, world);
            return new ContainerToolHolder(player.inventory, inventoryToolHolder, itemMultiToolHolder.Slotsize);
        }
//		if(id == MultiToolHolders.guiIdHolder3)
//		{
//			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
//			{
//				ItemStack stack = player.getCurrentEquippedItem();
//                InventoryToolHolder inventoryToolHolder = new InventoryToolHolder(stack, world);
//				return new ContainerToolHolder(player.inventory, inventoryToolHolder, 3);
//			}
//			else
//				return null;
//		}
//		else if(id == MultiToolHolders.guiIdHolder5)
//		{
//			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
//			{
//				ItemStack stack = player.getCurrentEquippedItem();
//                InventoryToolHolder inventoryToolHolder = new InventoryToolHolder(stack, world);
//				return new ContainerToolHolder(player.inventory, inventoryToolHolder, 5);
//			}
//			else
//				return null;
//		}
//		else if(id == MultiToolHolders.guiIdHolder7)
//		{
//			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
//			{
//				ItemStack stack = player.getCurrentEquippedItem();
//                InventoryToolHolder inventoryToolHolder = new InventoryToolHolder(stack, world);
//				return new ContainerToolHolder(player.inventory, inventoryToolHolder, 7);
//			}
//			else
//				return null;
//		}
//		else if(id == MultiToolHolders.guiIdHolder9)
//		{
//			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
//			{
//				ItemStack stack = player.getCurrentEquippedItem();
//                InventoryToolHolder inventoryToolHolder = new InventoryToolHolder(stack, world);
//				return new ContainerToolHolder(player.inventory, inventoryToolHolder, 9);
//			}
//			else
//				return null;
//		}
		else
			return null;
	}

	//returns an instance of the Gui you made earlier
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (player.getCurrentEquippedItem() != null &&player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder) {
            ItemStack stack = player.getCurrentEquippedItem();
            ItemMultiToolHolder itemMultiToolHolder = (ItemMultiToolHolder)stack.getItem();
            InventoryToolHolder inventoryToolHolder = new InventoryToolHolder(stack, world);
            return new GuiToolHolder(player.inventory, inventoryToolHolder, itemMultiToolHolder.Slotsize);
        }
//		if(id == MultiToolHolders.guiIdHolder3)
//		{
//			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
//			{
//				ItemStack stack = player.getCurrentEquippedItem();
//                InventoryToolHolder inventoryToolHolder = new InventoryToolHolder(stack, world);
//				return new GuiToolHolder(player.inventory, inventoryToolHolder, 3);
//			}
//			else
//				return null;
//		}
//		else if(id == MultiToolHolders.guiIdHolder5)
//		{
//			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
//			{
//				ItemStack stack = player.getCurrentEquippedItem();
//                InventoryToolHolder inventoryToolHolder = new InventoryToolHolder(stack, world);
//				return new GuiToolHolder(player.inventory, inventoryToolHolder, 5);
//			}
//			else
//				return null;
//		}
//		else if(id == MultiToolHolders.guiIdHolder7)
//		{
//			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
//			{
//				ItemStack stack = player.getCurrentEquippedItem();
//                InventoryToolHolder inventoryToolHolder = new InventoryToolHolder(stack, world);
//				return new GuiToolHolder(player.inventory, inventoryToolHolder, 7);
//			}
//			else
//				return null;
//		}
//		else if(id == MultiToolHolders.guiIdHolder9)
//		{
//			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
//			{
//				ItemStack stack = player.getCurrentEquippedItem();
//                InventoryToolHolder inventoryToolHolder = new InventoryToolHolder(stack, world);
//				return new GuiToolHolder(player.inventory,inventoryToolHolder, 9);
//			}
//			else
//				return null;
//		}
		else
			return null;
	}
}