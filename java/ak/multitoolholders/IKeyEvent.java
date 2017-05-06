package ak.multitoolholders;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * キーボードイベントを受け取って処理を行うアイテム用
 * Created by A.K. on 14/05/28.
 */
public interface IKeyEvent {
    void doKeyAction(ItemStack itemStack, EntityPlayer player, byte key);
}
