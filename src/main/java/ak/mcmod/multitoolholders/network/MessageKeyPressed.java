package ak.mcmod.multitoolholders.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * キー判定Message Created by A.K. on 14/05/28.
 */
public class MessageKeyPressed {

  public static final BiConsumer<MessageKeyPressed, FriendlyByteBuf> encoder = ((messageKeyPressed, packetBuffer) -> packetBuffer
          .writeByte(messageKeyPressed.getKey()));
  public static final Function<FriendlyByteBuf, MessageKeyPressed> decoder = packetBuffer -> new MessageKeyPressed(
          packetBuffer.readByte());
  private byte key;

  @SuppressWarnings("unused")
  public MessageKeyPressed() {
  }

  public MessageKeyPressed(byte keyPressed) {
    this.key = keyPressed;
  }

  byte getKey() {
    return key;
  }
}
