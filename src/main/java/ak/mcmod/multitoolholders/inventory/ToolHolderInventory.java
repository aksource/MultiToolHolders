package ak.mcmod.multitoolholders.inventory;

import ak.mcmod.multitoolholders.item.HolderType;
import ak.mcmod.multitoolholders.item.MultiToolHolderItem;
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
  public int getMaxStackSize() {
    return 1;
  }

  @Override
  public void setChanged() {

  }

  @Override
  public boolean stillValid(PlayerEntity player) {
    return this.holder.sameItem(player.getMainHandItem());
  }

  @Override
  public int getContainerSize() {
    return itemList.size();
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public ItemStack getItem(int index) {
    return itemList.get(index % itemList.size());
  }

  @Override
  public ItemStack removeItem(int index, int count) {
    ItemStack itemStack = this.getItem(index);
    if (!itemStack.isEmpty()) {
      if (itemStack.getCount() <= count) {
        this.setItem(index, ItemStack.EMPTY);
      } else {
        itemStack = itemStack.split(count);
      }
      return itemStack;
    }
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack removeItemNoUpdate(int index) {
    ItemStack itemStack = this.getItem(index);
    this.setItem(index, ItemStack.EMPTY);
    return itemStack;
  }

  @Override
  public void setItem(int index, ItemStack stack) {
    itemList.set(index, stack);
    writeToNBT(Objects.requireNonNull(holder.getTag()));
  }

  @Override
  public void startOpen(PlayerEntity player) {
    if (!holder.hasTag()) {
      holder.setTag(new CompoundNBT());
    }
    readFromNBT(Objects.requireNonNull(holder.getTag()));
  }

  @Override
  public void stopOpen(PlayerEntity player) {
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

      if (slot < this.getContainerSize()) {
        this.setItem(slot, ItemStack.of(nbtTagCompound));
      }
    }
  }

  public void writeToNBT(CompoundNBT nbt) {
    ListNBT tagList = new ListNBT();

    for (int i = 0; i < this.getContainerSize(); ++i) {
      if (!this.getItem(i).isEmpty()) {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        nbtTagCompound.putByte("Slot", (byte) i);
        this.getItem(i).save(nbtTagCompound);
        tagList.add(nbtTagCompound);
      }
    }

    nbt.put("Items", tagList);
  }

  @Override
  public void clearContent() {

  }
}
