package ak.multitoolholders.inventory;

import ak.multitoolholders.item.HolderType;
import ak.multitoolholders.item.MultiToolHolderItem;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ToolHolderInventory implements IInventory {

  private final ItemStack holder;
  private final NonNullList<ItemStack> itemList;

  public ToolHolderInventory(ItemStack stack) {
    holder = stack;
    HolderType type = ((MultiToolHolderItem) stack.getItem()).getType();
    itemList = NonNullList.withSize(type.getSize(), ItemStack.EMPTY);
    if (!holder.hasTag()) {
      holder.setTag(new CompoundNBT());
    }
    readFromNBT(Objects.requireNonNull(holder.getTag()));
  }

  @Override
  public int getInventoryStackLimit() {
    return 1;
  }

  @Override
  public void markDirty() {

  }

  @Override
  public boolean isUsableByPlayer(PlayerEntity player) {
    return this.holder.isItemEqual(player.getHeldItemMainhand());
  }

  @Override
  public int getSizeInventory() {
    return itemList.size();
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public ItemStack getStackInSlot(int index) {
    return itemList.get(index % itemList.size());
  }

  @Override
  public ItemStack decrStackSize(int index, int count) {
    ItemStack itemStack = this.getStackInSlot(index);
    if (!itemStack.isEmpty()) {
      if (itemStack.getCount() <= count) {
        this.setInventorySlotContents(index, ItemStack.EMPTY);
      } else {
        itemStack = itemStack.split(count);
      }
      return itemStack;
    }
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack removeStackFromSlot(int index) {
    ItemStack itemStack = this.getStackInSlot(index);
    this.setInventorySlotContents(index, ItemStack.EMPTY);
    return itemStack;
  }

  @Override
  public void setInventorySlotContents(int index, ItemStack stack) {
    itemList.set(index, stack);
    writeToNBT(Objects.requireNonNull(holder.getTag()));
  }

  @Override
  public void openInventory(PlayerEntity player) {
    if (!holder.hasTag()) {
      holder.setTag(new CompoundNBT());
    }
    readFromNBT(Objects.requireNonNull(holder.getTag()));
  }

  @Override
  public void closeInventory(PlayerEntity player) {
    if (!holder.hasTag()) {
      holder.setTag(new CompoundNBT());
    }
    writeToNBT(Objects.requireNonNull(holder.getTag()));
  }

  public void readFromNBT(CompoundNBT nbt) {

    ListNBT tagList = nbt.getList("Items", Constants.NBT.TAG_COMPOUND);

    for (net.minecraft.nbt.INBT inbt : tagList) {
      CompoundNBT nbtTagCompound = (CompoundNBT) inbt;
      int slot = nbtTagCompound.getByte("Slot") & 255;

      if (slot < this.getSizeInventory()) {
        this.setInventorySlotContents(slot, ItemStack.read(nbtTagCompound));
      }
    }
  }

  public void writeToNBT(CompoundNBT nbt) {
    ListNBT tagList = new ListNBT();

    for (int i = 0; i < this.getSizeInventory(); ++i) {
      if (!this.getStackInSlot(i).isEmpty()) {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        nbtTagCompound.putByte("Slot", (byte) i);
        this.getStackInSlot(i).write(nbtTagCompound);
        tagList.add(nbtTagCompound);
      }
    }

    nbt.put("Items", tagList);
  }

  @Override
  public void clear() {

  }
}
