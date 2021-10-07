package ak.mcmod.multitoolholders.inventory;

import ak.mcmod.multitoolholders.item.HolderType;
import ak.mcmod.multitoolholders.item.ItemMultiToolHolder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ContainerToolHolder extends Container {
  private final IInventory holderInventory;
  private final HolderType type;
  private final ItemStack holderStack;
  private final int currentSlot;

  public ContainerToolHolder(EntityPlayer entityPlayer, ItemStack holderStack, HolderType type, int currentSlot) {
    this.holderInventory = ItemMultiToolHolder.getInventoryFromItemStack(holderStack);
    this.type = type;
    this.holderStack = holderStack;
    holderInventory.openInventory(entityPlayer);
    this.currentSlot = currentSlot;
    for (int k = 0; k < this.type.getSize(); ++k) {
      this.addSlotToContainer(new SlotToolHolder(holderInventory, k, 8 + k * 18, 18));
    }
    bindPlayerInventory(entityPlayer.inventory);
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
    return !playerIn.inventory.getCurrentItem().isEmpty() && playerIn.inventory.getCurrentItem().getItem() instanceof ItemMultiToolHolder;
  }

  @Override
  public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.getSlot(index);

    if (slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();

      if (index < this.type.getSize()) {
        if (!this.mergeItemStack(itemstack1, this.type.getSize(), this.inventorySlots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (itemstack1.getItem() instanceof ItemMultiToolHolder || itemstack1.isStackable())
        return ItemStack.EMPTY;
      else if (!this.mergeItemStack(itemstack1, 0, this.type.getSize(), false)) {
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
    if (currentSlot == slotId - 27 - this.type.getSize()) {
      return ItemStack.EMPTY;
    }
    return super.slotClick(slotId, dragType, clickTypeIn, playerIn);
  }

  @Override
  public void onContainerClosed(EntityPlayer player) {
    super.onContainerClosed(player);
    this.holderInventory.closeInventory(player);
    player.inventory.setInventorySlotContents(player.inventory.currentItem, this.holderStack.copy());
  }
}
