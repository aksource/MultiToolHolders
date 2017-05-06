package ak.multitoolholders.network;

import ak.multitoolholders.IKeyEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by A.K. on 14/07/31.
 */
public class MessageKeyPressedHandler implements IMessageHandler<MessageKeyPressed, IMessage> {
    @Override
    public IMessage onMessage(MessageKeyPressed message, MessageContext ctx) {
        EntityPlayer entityPlayer = ctx.getServerHandler().playerEntity;
        if (entityPlayer != null && entityPlayer.getCurrentEquippedItem() != null && entityPlayer.getCurrentEquippedItem().getItem() instanceof IKeyEvent) {
            ((IKeyEvent)entityPlayer.getCurrentEquippedItem().getItem()).doKeyAction(entityPlayer.getCurrentEquippedItem(), entityPlayer, message.key);
        }
        return null;
    }
}
