package ak.multitoolholders.client;

import ak.multitoolholders.IKeyEvent;
import ak.multitoolholders.ItemMultiToolHolder;
import ak.multitoolholders.network.MessageKeyPressed;
import ak.multitoolholders.network.PacketHandler;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * キー入力を検知するクラス Created by A.K. on 14/05/28.
 */
@OnlyIn(Dist.CLIENT)
public class KeyInputHandler {

  /**
   * Instance of {@link Minecraft}
   */
  private Minecraft mc = Minecraft.getInstance();

  private byte getKeyIndex() {
    byte key = -1;
    if (ClientProxy.OPEN_KEY.isPressed()) {
      key = ItemMultiToolHolder.OPEN_KEY;
    } else if (ClientProxy.NEXT_KEY.isPressed()) {
      key = ItemMultiToolHolder.NEXT_KEY;
    } else if (ClientProxy.PREV_KEY.isPressed()) {
      key = ItemMultiToolHolder.PREV_KEY;
    }
    return key;
  }

  @SuppressWarnings("unused")
  @SubscribeEvent
  public void keyPressEvent(InputEvent event) {
    if (mc.isGameFocused() && Objects.nonNull(mc.player)) {
      EntityPlayer entityPlayer = mc.player;
      byte keyIndex = getKeyIndex();
      if (keyIndex != -1 && !entityPlayer.getHeldItemMainhand().isEmpty() && entityPlayer
          .getHeldItemMainhand().getItem() instanceof IKeyEvent) {
        if (entityPlayer.getEntityWorld().isRemote) {
          PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(keyIndex));
        }
      }
    }
  }
}
