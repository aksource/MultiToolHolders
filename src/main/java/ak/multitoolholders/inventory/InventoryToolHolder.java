package ak.multitoolholders.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class InventoryToolHolder extends InventoryBasic{

    private ItemStack holder;

    public InventoryToolHolder(ItemStack stack) {
        super("ToolHolder", false, 9);
        holder = stack;
        if (!holder.hasTagCompound()) {
            holder.setTagCompound(new NBTTagCompound());
        }
        readFromNBT(holder.getTagCompound());
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
        if (!holder.hasTagCompound()) {
            holder.setTagCompound(new NBTTagCompound());
        }
        readFromNBT(holder.getTagCompound());
    }

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {
        super.closeInventory(player);
        writeToNBT(holder.getTagCompound());
    }

    public void readFromNBT(NBTTagCompound nbt) {

        NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);

        for (int var3 = 0; var3 < tagList.tagCount(); ++var3) {
            NBTTagCompound var4 = tagList.getCompoundTagAt(var3);
            int var5 = var4.getByte("Slot") & 255;

            if (var5 >= 0 && var5 < this.getSizeInventory()) {
                this.setInventorySlotContents(var5, new ItemStack(var4));
            }
        }
    }

    public void writeToNBT(NBTTagCompound nbt) {
        NBTTagList tagList = new NBTTagList();

        for (int var3 = 0; var3 < this.getSizeInventory(); ++var3) {
            if (!this.getStackInSlot(var3).isEmpty()) {
                NBTTagCompound var4 = new NBTTagCompound();
                var4.setByte("Slot", (byte)var3);
                this.getStackInSlot(var3).writeToNBT(var4);
                tagList.appendTag(var4);
            }
        }

        nbt.setTag("Items", tagList);
    }
}
