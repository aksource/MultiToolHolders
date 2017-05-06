package ak.MultiToolHolders.inventory;

import ak.MultiToolHolders.EnumHolderType;
import ak.MultiToolHolders.ItemMultiToolHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerToolHolder extends Container {
    private final IInventory holderInventory;
    private final EnumHolderType type;
    private final ItemStack holderStack;
    private final int currentSlot;

    public ContainerToolHolder(InventoryPlayer inventoryPlayer, ItemStack holderStack, EnumHolderType type, int currentSlot) {
        this.holderInventory = ((ItemMultiToolHolder) holderStack.getItem()).getInventoryFromItemStack(holderStack);
        this.type = type;
        this.holderStack = holderStack;
        this.currentSlot = currentSlot;
        holderInventory.openInventory();
        for (int k = 0; k < this.type.getSize(); ++k) {
            this.addSlotToContainer(new SlotToolHolder(holderInventory, k, 8 + k * 18, 18));
        }
        bindPlayerInventory(inventoryPlayer);
    }

    private void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
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

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.inventory.getCurrentItem() != null && playerIn.inventory.getCurrentItem().getItem() instanceof ItemMultiToolHolder;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstack = null;
        Slot slot = this.getSlot(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < this.type.getSize()) {
                if (!this.mergeItemStack(itemstack1, this.type.getSize(), this.inventorySlots.size(), true)) {
                    return null;
                }
            } else if (itemstack1.getItem() instanceof ItemMultiToolHolder || itemstack1.isStackable())
                return null;
            else if (!this.mergeItemStack(itemstack1, 0, this.type.getSize(), false)) {
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

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn) {

        if (currentSlot == slotId - 27 - this.type.getSize()) {
            return null;
        }
        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        this.holderInventory.closeInventory();
        player.inventory.setInventorySlotContents(player.inventory.currentItem, this.holderStack.copy());
    }
}
