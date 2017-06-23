package ak.MultiToolHolders.network;

import ak.MultiToolHolders.IKeyEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * キー判定用Handler
 * Created by A.K. on 14/07/31.
 */
public class MessageKeyPressedHandler implements IMessageHandler<MessageKeyPressed, IMessage> {
    @Override
    public IMessage onMessage(MessageKeyPressed message, MessageContext ctx) {
        EntityPlayer entityPlayer = ctx.getServerHandler().player;
        if (entityPlayer != null && !entityPlayer.getHeldItemMainhand().isEmpty()
                && entityPlayer.getHeldItemMainhand().getItem() instanceof IKeyEvent) {
            ((IKeyEvent)entityPlayer.getHeldItemMainhand().getItem()).doKeyAction(
                    entityPlayer.getHeldItemMainhand(), entityPlayer, message.key);
        }
        return null;
    }
}
