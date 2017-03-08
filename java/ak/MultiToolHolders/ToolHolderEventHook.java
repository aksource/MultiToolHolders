package ak.MultiToolHolders;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * ツールホルダー用イベントフッククラス
 * Created by A.K. on 2017/03/08.
 */
public class ToolHolderEventHook {

    @SubscribeEvent
    public void destroyItemEvent(PlayerDestroyItemEvent event) {
        ItemStack original = event.getOriginal();
        if (original.hasTagCompound()
                && original.getTagCompound().hasKey(Constants.NBT_KEY_INCLUDE_MTH, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound mthNBTTagCompound = original.getTagCompound().getCompoundTag(Constants.NBT_KEY_INCLUDE_MTH);
            ItemStack mth = new ItemStack(mthNBTTagCompound);

        }
    }
}
