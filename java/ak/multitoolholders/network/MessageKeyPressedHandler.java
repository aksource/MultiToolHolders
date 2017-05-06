package ak.multitoolholders.network;

import ak.multitoolholders.IKeyEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by A.K. on 14/07/31.
 */
public class MessageKeyPressedHandler implements IMessageHandler<MessageKeyPressed, IMessage> {
    @Override
    public IMessage onMessage(MessageKeyPressed message, MessageContext ctx) {
        EntityPlayer entityPlayer = ctx.getServerHandler().playerEntity;
        if (entityPlayer != null && entityPlayer.getHeldItemMainhand() != null && entityPlayer.getHeldItemMainhand().getItem() instanceof IKeyEvent) {
            ((IKeyEvent)entityPlayer.getHeldItemMainhand().getItem()).doKeyAction(entityPlayer.getHeldItemMainhand(), entityPlayer, message.key);
        }
        return null;
    }
}