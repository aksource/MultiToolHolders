package ak.MultiToolHolders.inventory;

import ak.MultiToolHolders.ItemMultiToolHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerToolHolder extends Container {
    private IInventory holderInventory;
    private int holderNum;
    private ItemStack holderStack;
    private int currentSlot;

    public ContainerToolHolder(EntityPlayer entityPlayer, ItemStack holderStack, int num, int currentSlot) {
        this.holderInventory = ((ItemMultiToolHolder) holderStack.getItem()).getInventoryFromItemStack(holderStack);
        this.holderNum = num;
        this.holderStack = holderStack;
        holderInventory.openInventory(entityPlayer);
        this.currentSlot = currentSlot;
        for (int k = 0; k < holderNum; ++k) {
            this.addSlotToContainer(new SlotToolHolder(holderInventory, k, 8 + k * 18, 18));
        }
        bindPlayerInventory(entityPlayer.inventory);
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

    public boolean canInteractWith(EntityPlayer playerIn) {
        return !playerIn.inventory.getCurrentItem().isEmpty() && playerIn.inventory.getCurrentItem().getItem() instanceof ItemMultiToolHolder;
    }

    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.getSlot(par2);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (par2 < this.holderNum) {
                if (!this.mergeItemStack(itemstack1, this.holderNum, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemstack1.getItem() instanceof ItemMultiToolHolder || itemstack1.isStackable())
                return ItemStack.EMPTY;
            else if (!this.mergeItemStack(itemstack1, 0, this.holderNum, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer playerIn) {
        if (currentSlot == slotId - 27 - this.holderNum) {
            return ItemStack.EMPTY;
        }
        return super.slotClick(slotId, dragType, clickTypeIn, playerIn);
    }

    /**
     * Callback for when the crafting gui is closed.
     */
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        this.holderInventory.closeInventory(player);
        player.inventory.setInventorySlotContents(player.inventory.currentItem, this.holderStack.copy());
    }
}
