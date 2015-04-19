package ak.MultiToolHolders.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * Created by A.K. on 14/05/28.
 */
public class MessageKeyPressed implements IMessage {

    public byte key;

    public MessageKeyPressed(){}

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
