package ak.MultiToolHolders.network;

import ak.MultiToolHolders.ItemMultiToolHolder;
import ak.MultiToolHolders.MultiToolHolders;
import ak.MultiToolHolders.inventory.ToolHolderData;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by A.K. on 14/07/31.
 */
@SideOnly(Side.CLIENT)
public class MessageHolderDataHandler implements IMessageHandler<MessageHolderData, IMessage> {
    @Override
    public IMessage onMessage(MessageHolderData message, MessageContext ctx) {
        EntityPlayer player = MultiToolHolders.proxy.getPlayer();
        if (player != null && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder) {
            ItemMultiToolHolder toolHolder = (ItemMultiToolHolder)player.getCurrentEquippedItem().getItem();
            ToolHolderData data = toolHolder.getInventoryFromItemStack(player.getCurrentEquippedItem()).data;
            data.readFromNBT(message.nbt);
        }
        return null;
    }
}
