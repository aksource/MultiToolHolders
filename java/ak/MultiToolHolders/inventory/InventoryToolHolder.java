package ak.MultiToolHolders.inventory;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

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
//		data.upDate = true;
//        writeToNBT(holder.getTagCompound());
	}

    @Override
    public void openInventory() {
        super.openInventory();
        if (!holder.hasTagCompound()) {
            holder.setTagCompound(new NBTTagCompound());
        }
        readFromNBT(holder.getTagCompound());
    }

    @Override
    public void closeInventory() {
        super.closeInventory();
        writeToNBT(holder.getTagCompound());
    }

    public void readFromNBT(NBTTagCompound nbt) {

//        for (int i = 0; i < this.getSizeInventory(); ++i) {
//            this.setInventorySlotContents(i, null);
//        }
        NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);

        for (int var3 = 0; var3 < tagList.tagCount(); ++var3) {
            NBTTagCompound var4 = tagList.getCompoundTagAt(var3);
            int var5 = var4.getByte("Slot") & 255;

            if (var5 >= 0 && var5 < this.getSizeInventory()) {
                this.setInventorySlotContents(var5, ItemStack.loadItemStackFromNBT(var4));
            }
        }
    }

    public void writeToNBT(NBTTagCompound nbt) {
        NBTTagList tagList = new NBTTagList();

        for (int var3 = 0; var3 < this.getSizeInventory(); ++var3) {
            if (this.getStackInSlot(var3) != null) {
                NBTTagCompound var4 = new NBTTagCompound();
                var4.setByte("Slot", (byte)var3);
                this.getStackInSlot(var3).writeToNBT(var4);
                tagList.appendTag(var4);
            }
        }

        nbt.setTag("Items", tagList);
    }
}
