package ak.multitoolholders.network;

import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.PacketBuffer;

/**
 * キー判定Message Created by A.K. on 14/05/28.
 */
public class MessageKeyPressed {

  public static BiConsumer<MessageKeyPressed, PacketBuffer> encoder = ((messageKeyPressed, packetBuffer) -> packetBuffer
      .writeByte(messageKeyPressed.getKey()));
  public static Function<PacketBuffer, MessageKeyPressed> decoder = packetBuffer -> new MessageKeyPressed(
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
