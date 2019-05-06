package ak.multitoolholders;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * BCとの連携用 Created by A.K. on 14/11/02.
 */
public class CoopBC {

  public static boolean canWrench(ItemStack itemStack, EntityPlayer player, int x, int y, int z) {
/*        if (itemStack.getItem() instanceof IToolWrench) {
            ((IToolWrench)itemStack.getItem()).canWrench(player, x, y, z);
        }*/
    return false;
  }

  public static void wrenchUsed(ItemStack itemStack, EntityPlayer player, int x, int y, int z) {
/*        if (itemStack.getItem() instanceof IToolWrench) {
            ((IToolWrench)itemStack.getItem()).wrenchUsed(player, x, y, z);
        }*/
  }
}
