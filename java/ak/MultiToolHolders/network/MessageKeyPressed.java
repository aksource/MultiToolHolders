package ak.MultiToolHolders.network;

import ak.MultiToolHolders.IKeyEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by A.K. on 14/05/28.
 */
public class MessageKeyPressed implements IMessage, IMessageHandler<MessageKeyPressed, IMessage> {

    private byte key;

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

    @Override
    public IMessage onMessage(MessageKeyPressed message, MessageContext ctx) {
        EntityPlayer entityPlayer = ctx.getServerHandler().playerEntity;
        if (entityPlayer != null && entityPlayer.getCurrentEquippedItem() != null && entityPlayer.getCurrentEquippedItem().getItem() instanceof IKeyEvent) {
            ((IKeyEvent)entityPlayer.getCurrentEquippedItem().getItem()).doKeyAction(entityPlayer.getCurrentEquippedItem(), entityPlayer, message.key);
        }
        return null;
    }
}
