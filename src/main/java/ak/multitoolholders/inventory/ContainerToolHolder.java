package ak.multitoolholders.inventory;

import ak.multitoolholders.EnumHolderType;
import ak.multitoolholders.ItemMultiToolHolder;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerToolHolder extends Container {

  private final IInventory holderInventory;
  private final EnumHolderType type;
  private final ItemStack holderStack;
  private final int currentSlot;

  public ContainerToolHolder(EntityPlayer entityPlayer, ItemStack holderStack, EnumHolderType type,
      int currentSlot) {
    this.holderInventory = ((ItemMultiToolHolder) holderStack.getItem())
        .getInventoryFromItemStack(holderStack);
    this.type = type;
    this.holderStack = holderStack;
    holderInventory.openInventory(entityPlayer);
    this.currentSlot = currentSlot;
    for (int k = 0; k < this.type.getSize(); ++k) {
      this.addSlot(new SlotToolHolder(holderInventory, k, 8 + k * 18, 18));
    }
    bindPlayerInventory(entityPlayer.inventory);
  }

  private void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 9; j++) {
        addSlot(new Slot(inventoryPlayer, j + i * 9 + 9,
            8 + j * 18, 50 + i * 18));
      }
    }

    for (int i = 0; i < 9; i++) {
      addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 108));
    }
  }

  @Override
  public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
    return !playerIn.inventory.getCurrentItem().isEmpty() && playerIn.inventory.getCurrentItem()
        .getItem() instanceof ItemMultiToolHolder;
  }

  @Override
  @Nonnull
  public ItemStack transferStackInSlot(@Nonnull EntityPlayer playerIn, int index) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.getSlot(index);

    if (slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();

      if (index < this.type.getSize()) {
        if (!this
            .mergeItemStack(itemstack1, this.type.getSize(), this.inventorySlots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (itemstack1.getItem() instanceof ItemMultiToolHolder || itemstack1.isStackable()) {
        return ItemStack.EMPTY;
      } else if (!this.mergeItemStack(itemstack1, 0, this.type.getSize(), false)) {
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
  @Nonnull
  public ItemStack slotClick(int slotId, int dragType, @Nonnull ClickType clickTypeIn,
      @Nonnull EntityPlayer playerIn) {
    if (currentSlot == slotId - 27 - this.type.getSize()) {
      return ItemStack.EMPTY;
    }
    return super.slotClick(slotId, dragType, clickTypeIn, playerIn);
  }

  @Override
  public void onContainerClosed(@Nonnull EntityPlayer player) {
    super.onContainerClosed(player);
    this.holderInventory.closeInventory(player);
    player.inventory
        .setInventorySlotContents(player.inventory.currentItem, this.holderStack.copy());
  }
}
