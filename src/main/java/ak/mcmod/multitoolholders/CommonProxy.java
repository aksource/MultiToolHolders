package ak.mcmod.multitoolholders;

import ak.mcmod.multitoolholders.client.GuiToolHolder;
import ak.mcmod.multitoolholders.inventory.ContainerToolHolder;
import ak.mcmod.multitoolholders.item.ItemMultiToolHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler {
  public CommonProxy() {}

  public void registerClientPreInformation() {}

  public void registerClientInformation() {}

  public EntityPlayer getPlayer() {return null;}

  //returns an instance of the Container you made earlier
  @Override
  public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
    if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemMultiToolHolder) {
      ItemStack stack = player.getHeldItemMainhand();
      ItemMultiToolHolder itemMultiToolHolder = (ItemMultiToolHolder) stack.getItem();
      int currentSlot = player.inventory.currentItem;
      return new ContainerToolHolder(player, stack, itemMultiToolHolder.getType(), currentSlot);
    }
    return null;
  }

  //returns an instance of the Gui you made earlier
  @Override
  public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
    if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemMultiToolHolder) {
      ItemStack stack = player.getHeldItemMainhand();
      ItemMultiToolHolder itemMultiToolHolder = (ItemMultiToolHolder) stack.getItem();
      int currentSlot = player.inventory.currentItem;
      return new GuiToolHolder(player, stack, itemMultiToolHolder.getType(), currentSlot);
    }
    return null;
  }
}