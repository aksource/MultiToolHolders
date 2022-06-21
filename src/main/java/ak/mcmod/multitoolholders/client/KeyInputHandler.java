package ak.mcmod.multitoolholders.client;

import ak.mcmod.multitoolholders.IKeyEvent;
import ak.mcmod.multitoolholders.item.MultiToolHolderItem;
import ak.mcmod.multitoolholders.network.MessageKeyPressed;
import ak.mcmod.multitoolholders.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Objects;

/**
 * キー入力を検知するクラス Created by A.K. on 14/05/28.
 */
public class KeyInputHandler {

  /**
   * Instance of {@link Minecraft}
   */
  private final Minecraft mc = Minecraft.getInstance();

  private byte getKeyIndex() {
    byte key = -1;
    if (ClientSettingUtility.OPEN_KEY.consumeClick()) {
      key = MultiToolHolderItem.OPEN_KEY;
    } else if (ClientSettingUtility.NEXT_KEY.consumeClick()) {
      key = MultiToolHolderItem.NEXT_KEY;
    } else if (ClientSettingUtility.PREV_KEY.consumeClick()) {
      key = MultiToolHolderItem.PREV_KEY;
    }
    return key;
  }

  @SubscribeEvent
  public void keyPressEvent(InputEvent event) {
    if (mc.isWindowActive() && Objects.nonNull(mc.player)) {
      var entityPlayer = mc.player;
      var keyIndex = getKeyIndex();
      if (keyIndex != -1 && !entityPlayer.getMainHandItem().isEmpty() && entityPlayer
              .getMainHandItem().getItem() instanceof IKeyEvent) {
        if (entityPlayer.getCommandSenderWorld().isClientSide) {
          PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(keyIndex));
        }
      }
    }
  }
}
