package ak.multitoolholders.network;

import ak.multitoolholders.IKeyEvent;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * キー判定用Handler Created by A.K. on 14/07/31.
 */
public class MessageKeyPressedHandler implements BiConsumer<MessageKeyPressed, Supplier<Context>> {

  @Override
  public void accept(MessageKeyPressed message, Supplier<Context> contextSupplier) {
    EntityPlayer entityPlayer = contextSupplier.get().getSender();
    if (entityPlayer != null && !entityPlayer.getHeldItemMainhand().isEmpty()
        && entityPlayer.getHeldItemMainhand().getItem() instanceof IKeyEvent) {
      ((IKeyEvent) entityPlayer.getHeldItemMainhand().getItem()).doKeyAction(
          entityPlayer.getHeldItemMainhand(), entityPlayer, message.getKey());
    }
  }
}
