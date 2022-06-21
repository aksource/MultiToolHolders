package ak.mcmod.multitoolholders.inventory;

import ak.mcmod.multitoolholders.item.HolderType;
import ak.mcmod.multitoolholders.item.MultiToolHolderItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ToolHolderContainer extends AbstractContainerMenu {

  public static final MenuType<ToolHolderContainer> TOOL_HOLDER_3_CONTAINER_TYPE = new MenuType<>(ToolHolderContainer::createHolderType3);
  public static final MenuType<ToolHolderContainer> TOOL_HOLDER_5_CONTAINER_TYPE = new MenuType<>(ToolHolderContainer::createHolderType5);
  public static final MenuType<ToolHolderContainer> TOOL_HOLDER_7_CONTAINER_TYPE = new MenuType<>(ToolHolderContainer::createHolderType7);
  public static final MenuType<ToolHolderContainer> TOOL_HOLDER_9_CONTAINER_TYPE = new MenuType<>(ToolHolderContainer::createHolderType9);
  private final Container holderInventory;
  private final int currentSlot;

  public static ToolHolderContainer createHolderType3(int id, Inventory playerInventory) {
    return new ToolHolderContainer(HolderType.HOLDER3, id, playerInventory, new SimpleContainer(3), 0);
  }

  public static ToolHolderContainer createHolderType5(int id, Inventory playerInventory) {
    return new ToolHolderContainer(HolderType.HOLDER5, id, playerInventory, new SimpleContainer(5), 0);
  }

  public static ToolHolderContainer createHolderType7(int id, Inventory playerInventory) {
    return new ToolHolderContainer(HolderType.HOLDER7, id, playerInventory, new SimpleContainer(7), 0);
  }

  public static ToolHolderContainer createHolderType9(int id, Inventory playerInventory) {
    return new ToolHolderContainer(HolderType.HOLDER9, id, playerInventory, new SimpleContainer(9), 0);
  }

  private static MenuType<?> getContainerType(HolderType type) {
    return switch (type) {
      case HOLDER5 -> TOOL_HOLDER_5_CONTAINER_TYPE;
      case HOLDER7 -> TOOL_HOLDER_7_CONTAINER_TYPE;
      case HOLDER9 -> TOOL_HOLDER_9_CONTAINER_TYPE;
      case HOLDER3 -> TOOL_HOLDER_3_CONTAINER_TYPE;
    };
  }

  public ToolHolderContainer(HolderType type, int id, Inventory playerInventory, Container holderInventory,
                             int currentSlot) {
    super(getContainerType(type), id);
    this.currentSlot = currentSlot;
    this.holderInventory = holderInventory;
    for (var k = 0; k < this.holderInventory.getContainerSize(); ++k) {
      this.addSlot(new ToolHolderSlot(holderInventory, k, 8 + k * 18, 18));
    }
    bindPlayerInventory(playerInventory);
  }

  private void bindPlayerInventory(Inventory inventoryPlayer) {
    for (var i = 0; i < 3; i++) {
      for (var j = 0; j < 9; j++) {
        addSlot(new Slot(inventoryPlayer, j + i * 9 + 9,
                8 + j * 18, 50 + i * 18));
      }
    }

    for (var i = 0; i < 9; i++) {
      addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 108));
    }
  }

  @Override
  public boolean stillValid(Player playerIn) {
    return !playerIn.getInventory().getSelected().isEmpty() && playerIn.getInventory().getSelected()
            .getItem() instanceof MultiToolHolderItem;
  }

  @Override
  public ItemStack quickMoveStack(Player playerIn, int index) {
    var itemstack = ItemStack.EMPTY;
    var slot = this.getSlot(index);

    if (slot.hasItem()) {
      var itemstack1 = slot.getItem();
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
}
