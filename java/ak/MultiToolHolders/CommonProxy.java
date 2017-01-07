package ak.MultiToolHolders;

import ak.MultiToolHolders.Client.GuiToolHolder;
import ak.MultiToolHolders.inventory.ContainerToolHolder;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CommonProxy implements IGuiHandler {
    public CommonProxy() {}

    public void registerClientInformation() {}

    public EntityPlayer getPlayer() {return null;}

    //returns an instance of the Container you made earlier
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (player.getHeldItemMainhand() != ItemStack.EMPTY && player.getHeldItemMainhand().getItem() instanceof ItemMultiToolHolder) {
            ItemStack stack = player.getHeldItemMainhand();
            ItemMultiToolHolder itemMultiToolHolder = (ItemMultiToolHolder) stack.getItem();
            int currentSlot = player.inventory.currentItem;
            return new ContainerToolHolder(player, stack, itemMultiToolHolder.inventorySize, currentSlot);
        }
        return null;
    }

    //returns an instance of the Gui you made earlier
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (player.getHeldItemMainhand() != ItemStack.EMPTY && player.getHeldItemMainhand().getItem() instanceof ItemMultiToolHolder) {
            ItemStack stack = player.getHeldItemMainhand();
            ItemMultiToolHolder itemMultiToolHolder = (ItemMultiToolHolder) stack.getItem();
            int currentSlot = player.inventory.currentItem;
            return new GuiToolHolder(player, stack, itemMultiToolHolder.inventorySize, currentSlot);
        }
        return null;
    }
}