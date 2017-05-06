package ak.MultiToolHolders;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.Set;

/**
 * Created by A.K. on 14/11/10.
 */
public class BlockEventHooks {

    public void clickBlock(PlayerInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack heldItem = player.getCurrentEquippedItem();
        if (heldItem == null || heldItem.getItem() instanceof ItemMultiToolHolder) return;
        ItemStack nowSlotItem = ((ItemMultiToolHolder)heldItem.getItem()).getInventoryFromItemStack(heldItem).getStackInSlot(ItemMultiToolHolder.getSlotNumFromItemStack(heldItem));
        if (nowSlotItem == null) return;
        if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            Set<String> toolClasses = nowSlotItem.getItem().getToolClasses(nowSlotItem);
            int harvestLevel;
            for (String toolClass : toolClasses) {
                harvestLevel = nowSlotItem.getItem().getHarvestLevel(nowSlotItem, toolClass);
            }
        }
    }
}
