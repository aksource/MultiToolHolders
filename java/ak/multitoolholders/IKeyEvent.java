package ak.multitoolholders;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Created by A.K. on 14/05/28.
 */
public interface IKeyEvent {
    void doKeyAction(@Nonnull ItemStack itemStack, @Nonnull EntityPlayer player, byte key);
}
