package ak.mcmod.multitoolholders;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * キーボードイベントを受け取って処理を行うアイテム用 Created by A.K. on 14/05/28.
 */
public interface IKeyEvent {

  void doKeyAction(@Nonnull ItemStack itemStack, @Nonnull Player player, byte key);
}
