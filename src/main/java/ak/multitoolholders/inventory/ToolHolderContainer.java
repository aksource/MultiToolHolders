package ak.multitoolholders.inventory;

import ak.multitoolholders.item.HolderType;
import ak.multitoolholders.item.MultiToolHolderItem;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ToolHolderContainer extends Container {

  public static final ContainerType<ToolHolderContainer> TOOL_HOLDER_3_CONTAINER_TYPE = new ContainerType<>(ToolHolderContainer::createHolderType3);
  public static final ContainerType<ToolHolderContainer> TOOL_HOLDER_5_CONTAINER_TYPE = new ContainerType<>(ToolHolderContainer::createHolderType5);
  public static final ContainerType<ToolHolderContainer> TOOL_HOLDER_7_CONTAINER_TYPE = new ContainerType<>(ToolHolderContainer::createHolderType7);
  public static final ContainerType<ToolHolderContainer> TOOL_HOLDER_9_CONTAINER_TYPE = new ContainerType<>(ToolHolderContainer::createHolderType9);
  private final IInventory holderInventory;
  private final int currentSlot;

  public static ToolHolderContainer createHolderType3(int id, PlayerInventory playerInventory) {
    return new ToolHolderContainer(HolderType.HOLDER3, id, playerInventory, new Inventory(3), 0);
  }

  public static ToolHolderContainer createHolderType5(int id, PlayerInventory playerInventory) {
    return new ToolHolderContainer(HolderType.HOLDER5, id, playerInventory, new Inventory(5), 0);
  }

  public static ToolHolderContainer createHolderType7(int id, PlayerInventory playerInventory) {
    return new ToolHolderContainer(HolderType.HOLDER7, id, playerInventory, new Inventory(7), 0);
  }

  public static ToolHolderContainer createHolderType9(int id, PlayerInventory playerInventory) {
    return new ToolHolderContainer(HolderType.HOLDER9, id, playerInventory, new Inventory(9), 0);
  }

  private static ContainerType<?> getContainerType(HolderType type) {
    switch (type) {
      case HOLDER5: return TOOL_HOLDER_5_CONTAINER_TYPE;
      case HOLDER7: return TOOL_HOLDER_7_CONTAINER_TYPE;
      case HOLDER9: return TOOL_HOLDER_9_CONTAINER_TYPE;
      case HOLDER3:
      default: return TOOL_HOLDER_3_CONTAINER_TYPE;
    }
  }

  public ToolHolderContainer(HolderType type, int id, PlayerInventory playerInventory, IInventory holderInventory,
                             int currentSlot) {
    super(getContainerType(type), id);
    this.currentSlot = currentSlot;
      this.holderInventory = holderInventory;
      for (int k = 0; k < this.holderInventory.getContainerSize(); ++k) {
        this.addSlot(new ToolHolderSlot(holderInventory, k, 8 + k * 18, 18));
      }
    bindPlayerInventory(playerInventory);
  }

  private void bindPlayerInventory(PlayerInventory inventoryPlayer) {
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
  public boolean stillValid(PlayerEntity playerIn) {
    return !playerIn.inventory.getSelected().isEmpty() && playerIn.inventory.getSelected()
        .getItem() instanceof MultiToolHolderItem;
  }

  @Override
  public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.getSlot(index);

    if (slot.hasItem()) {
      ItemStack itemstack1 = slot.getItem();
      itemstack = itemstack1.copy();

      if (index < this.holderInventory.getContainerSize()) {
        if (!this
            .moveItemStackTo(itemstack1, this.holderInventory.getContainerSize(), this.slots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (itemstack1.getItem() instanceof MultiToolHolderItem || itemstack1.isStackable()) {
        return ItemStack.EMPTY;
      } else if (!this.moveItemStackTo(itemstack1, 0, this.holderInventory.getContainerSize(), false)) {
        return ItemStack.EMPTY;
      }

      if (itemstack1.getCount() == 0) {
        slot.set(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }
    }

    return itemstack;
  }

  @Override
  public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity playerIn) {
    if (currentSlot == slotId - 27 - this.holderInventory.getContainerSize()) {
      return ItemStack.EMPTY;
    }
    return super.clicked(slotId, dragType, clickTypeIn, playerIn);
  }
}
