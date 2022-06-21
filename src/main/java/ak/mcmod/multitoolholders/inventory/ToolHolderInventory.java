package ak.mcmod.multitoolholders.inventory;

import ak.mcmod.multitoolholders.item.MultiToolHolderItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ToolHolderInventory implements Container {

  private final ItemStack holder;
  private final NonNullList<ItemStack> itemList;

  public ToolHolderInventory(ItemStack stack) {
    holder = stack;
    var type = ((MultiToolHolderItem) stack.getItem()).getType();
    itemList = NonNullList.withSize(type.getSize(), ItemStack.EMPTY);
    if (!holder.hasTag()) {
      holder.setTag(new CompoundTag());
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
  public boolean stillValid(Player player) {
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
    var itemStack = this.getItem(index);
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
    var itemStack = this.getItem(index);
    this.setItem(index, ItemStack.EMPTY);
    return itemStack;
  }

  @Override
  public void setItem(int index, ItemStack stack) {
    itemList.set(index, stack);
    writeToNBT(Objects.requireNonNull(holder.getTag()));
  }

  @Override
  public void startOpen(Player player) {
    if (!holder.hasTag()) {
      holder.setTag(new CompoundTag());
    }
    readFromNBT(Objects.requireNonNull(holder.getTag()));
  }

  @Override
  public void stopOpen(Player player) {
    if (!holder.hasTag()) {
      holder.setTag(new CompoundTag());
    }
    writeToNBT(Objects.requireNonNull(holder.getTag()));
  }

  public void readFromNBT(CompoundTag nbt) {

    var tagList = nbt.getList("Items", Tag.TAG_COMPOUND);

    for (var inbt : tagList) {
      var nbtTagCompound = (CompoundTag) inbt;
      int slot = nbtTagCompound.getByte("Slot") & 255;

      if (slot < this.getContainerSize()) {
        this.setItem(slot, ItemStack.of(nbtTagCompound));
      }
    }
  }

  public void writeToNBT(CompoundTag nbt) {
    var tagList = new ListTag();

    for (var i = 0; i < this.getContainerSize(); ++i) {
      if (!this.getItem(i).isEmpty()) {
        var nbtTagCompound = new CompoundTag();
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
