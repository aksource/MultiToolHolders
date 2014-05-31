package ak.MultiToolHolders.inventory;

import ak.MultiToolHolders.ItemMultiToolHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class InventoryToolHolder implements IInventory{
    public ToolHolderData data;

    public InventoryToolHolder(ItemStack stack, World world) {
        data = ((ItemMultiToolHolder)stack.getItem()).getData(stack, world);
    }
    @Override
    public int getSizeInventory()
    {
        return 9;
    }

    @Override
    public ItemStack getStackInSlot(int var1)
    {
        return data.tools[var1];
    }

    @Override
    public ItemStack decrStackSize(int var1, int var2)
    {
        if(data.tools[var1] != null)
        {
            ItemStack var3;
            if(data.tools[var1].stackSize <= var2)
            {
                var3 = data.tools[var1];
                data.tools[var1] = null;
                this.markDirty();
                return var3;
            }
            else
            {
                var3 = data.tools[var1].splitStack(var2);

                if (data.tools[var1].stackSize == 0)
                {
                    data.tools[var1] = null;
                }

                this.markDirty();
                return var3;
            }
        }
        else
            return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int var1)
    {
        return null;
    }

    @Override
    public void setInventorySlotContents(int var1, ItemStack var2) {
        data.tools[var1] = var2;
    }

    @Override
    public String getInventoryName() {
        return "ToolHolder";
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

	@Override
	public void markDirty() {
		data.upDate = true;
	}

    @Override
    public boolean isUseableByPlayer(EntityPlayer var1) {
        return true;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {
        this.markDirty();
    }
    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }
    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return false;
    }
}
