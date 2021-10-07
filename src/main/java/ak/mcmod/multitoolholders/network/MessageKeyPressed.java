package ak.mcmod.multitoolholders.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * キー判定Message
 * Created by A.K. on 14/05/28.
 */
public class MessageKeyPressed implements IMessage {

  public byte key;

  public MessageKeyPressed() {}

  public MessageKeyPressed(byte keyPressed) {
    this.key = keyPressed;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.key = buf.readByte();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeByte(this.key);
  }
}
