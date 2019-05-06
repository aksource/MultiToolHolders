package ak.multitoolholders;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * TEとの連携用 Created by A.K. on 14/11/02.
 */
public class CoopTE {

  public static boolean isUsable(ItemStack itemStack, EntityLivingBase entityLivingBase, int x,
      int y, int z) {
//        if (itemStack.getItem() instanceof IToolHammer) {
//            ((IToolHammer)itemStack.getItem()).isUsable(itemStack, entityLivingBase, x, y, z);
//        }
    return false;
  }

  public static void toolUsed(ItemStack itemStack, EntityLivingBase entityLivingBase, int x, int y,
      int z) {
//        if (itemStack.getItem() instanceof IToolHammer) {
//            ((IToolHammer)itemStack.getItem()).toolUsed(itemStack, entityLivingBase, x, y, z);
//        }
  }
}
