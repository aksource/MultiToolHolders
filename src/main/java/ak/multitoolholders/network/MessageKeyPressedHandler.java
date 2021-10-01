package ak.multitoolholders.network;

import ak.multitoolholders.IKeyEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * キー判定用Handler Created by A.K. on 14/07/31.
 */
public class MessageKeyPressedHandler implements BiConsumer<MessageKeyPressed, Supplier<Context>> {

  @Override
  public void accept(MessageKeyPressed message, Supplier<Context> contextSupplier) {
    PlayerEntity entityPlayer = contextSupplier.get().getSender();
    if (entityPlayer != null && !entityPlayer.getMainHandItem().isEmpty()
        && entityPlayer.getMainHandItem().getItem() instanceof IKeyEvent) {
      ((IKeyEvent) entityPlayer.getMainHandItem().getItem()).doKeyAction(
          entityPlayer.getMainHandItem(), entityPlayer, message.getKey());
    }
  }
}
