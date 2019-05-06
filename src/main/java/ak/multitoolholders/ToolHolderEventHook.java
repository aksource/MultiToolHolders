package ak.multitoolholders;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

/**
 * ツールホルダー用イベントフッククラス Created by A.K. on 2017/03/08.
 */
public class ToolHolderEventHook {

  //    @SubscribeEvent
  public void destroyItemEvent(PlayerDestroyItemEvent event) {
    ItemStack original = event.getOriginal();
    if (original.hasTag()
        && original.getTag().contains(Constants.NBT_KEY_INCLUDE_MTH,
        net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND)) {
      NBTTagCompound mthNBTTagCompound = original.getTag()
          .getCompound(Constants.NBT_KEY_INCLUDE_MTH);
      ItemStack mth = ItemStack.read(mthNBTTagCompound);

    }
  }
}
