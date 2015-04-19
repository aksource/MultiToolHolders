package ak.MultiToolHolders;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Created by A.K. on 14/05/28.
 */
public interface IKeyEvent {
    public void doKeyAction(ItemStack itemStack, EntityPlayer player, byte key);
}
