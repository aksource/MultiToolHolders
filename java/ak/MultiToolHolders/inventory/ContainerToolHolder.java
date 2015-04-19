package ak.MultiToolHolders.inventory;

import ak.MultiToolHolders.ItemMultiToolHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerToolHolder extends Container
{
	private IInventory holderInventory;
	private int HolderNum;
    private ItemStack holderStack;

	public ContainerToolHolder(InventoryPlayer inventoryPlayer, ItemStack holderStack, int num)
	{
		this.holderInventory = ((ItemMultiToolHolder)holderStack.getItem()).getInventoryFromItemStack(holderStack);
		this.HolderNum = num;
        this.holderStack = holderStack;
        holderInventory.openInventory();
		for (int k = 0; k < HolderNum; ++k) {
			this.addSlotToContainer(new SlotToolHolder(holderInventory, k, 8 + k * 18, 18));
		}
        bindPlayerInventory(inventoryPlayer);
	}
	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
						8 + j * 18, 50 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 108));
		}
	}
	public boolean canInteractWith(EntityPlayer par1EntityPlayer) {
		return par1EntityPlayer.inventory.getCurrentItem() != null && par1EntityPlayer.inventory.getCurrentItem().getItem() instanceof ItemMultiToolHolder;
	}

	/**
	 * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
	 */
	 public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
	 {
		 ItemStack itemstack = null;
		 Slot slot = this.getSlot(par2);

		 if (slot != null && slot.getHasStack()) {
			 ItemStack itemstack1 = slot.getStack();
			 itemstack = itemstack1.copy();

			 if (par2 < this.HolderNum) {
				 if (!this.mergeItemStack(itemstack1, this.HolderNum, this.inventorySlots.size(), true)) {
					 return null;
				 }
			 }  else if(itemstack1.getItem() instanceof ItemMultiToolHolder || itemstack1.isStackable())
				 return null;
			 else if (!this.mergeItemStack(itemstack1, 0, this.HolderNum, false)) {
				 return null;
			 }

			 if (itemstack1.stackSize == 0) {
				 slot.putStack(null);
			 } else {
				 slot.onSlotChanged();
			 }
		 }

		 return itemstack;
	 }

	 /**
	  * Callback for when the crafting gui is closed.
	  */
	 public void onContainerClosed(EntityPlayer player) {
		 super.onContainerClosed(player);
		 this.holderInventory.closeInventory();
         player.inventory.setInventorySlotContents(player.inventory.currentItem, this.holderStack.copy());
	 }
}
