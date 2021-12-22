package ak.mcmod.multitoolholders.network;

import ak.mcmod.multitoolholders.IKeyEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * キー判定用Handler Created by A.K. on 14/07/31.
 */
public class MessageKeyPressedHandler implements BiConsumer<MessageKeyPressed, Supplier<NetworkEvent.Context>> {

  @Override
  public void accept(MessageKeyPressed message, Supplier<NetworkEvent.Context> contextSupplier) {
    Player entityPlayer = contextSupplier.get().getSender();
    if (entityPlayer != null && !entityPlayer.getMainHandItem().isEmpty()
            && entityPlayer.getMainHandItem().getItem() instanceof IKeyEvent) {
      ((IKeyEvent) entityPlayer.getMainHandItem().getItem()).doKeyAction(
              entityPlayer.getMainHandItem(), entityPlayer, message.getKey());
    }
  }
}
