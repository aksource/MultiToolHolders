package ak.multitoolholders.inventory;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;

public class InventoryToolHolder extends InventoryBasic {

  private ItemStack holder;

  public InventoryToolHolder(ItemStack stack) {
    super(new TextComponentString("ToolHolder"), 9);
    holder = stack;
    if (!holder.hasTag()) {
      holder.setTag(new NBTTagCompound());
    }
    readFromNBT(holder.getTag());
  }

  @Override
  public int getInventoryStackLimit() {
    return 1;
  }

  @Override
  public void markDirty() {
    super.markDirty();
  }

  @Override
  public void openInventory(@Nonnull EntityPlayer player) {
    super.openInventory(player);
    if (!holder.hasTag()) {
      holder.setTag(new NBTTagCompound());
    }
    readFromNBT(holder.getTag());
  }

  @Override
  public void closeInventory(@Nonnull EntityPlayer player) {
    super.closeInventory(player);
    writeToNBT(holder.getTag());
  }

  public void readFromNBT(NBTTagCompound nbt) {

    NBTTagList tagList = nbt.getList("Items", Constants.NBT.TAG_COMPOUND);

    for (int i = 0; i < tagList.size(); ++i) {
      NBTTagCompound nbtTagCompound = (NBTTagCompound) tagList.get(i);
      int slot = nbtTagCompound.getByte("Slot") & 255;

      if (slot >= 0 && slot < this.getSizeInventory()) {
        this.setInventorySlotContents(slot, ItemStack.read(nbtTagCompound));
      }
    }
  }

  public void writeToNBT(NBTTagCompound nbt) {
    NBTTagList tagList = new NBTTagList();

    for (int i = 0; i < this.getSizeInventory(); ++i) {
      if (!this.getStackInSlot(i).isEmpty()) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.putByte("Slot", (byte) i);
        this.getStackInSlot(i).write(nbtTagCompound);
        tagList.add(nbtTagCompound);
      }
    }

    nbt.put("Items", tagList);
  }
}
