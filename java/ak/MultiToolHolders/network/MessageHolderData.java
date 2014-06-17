package ak.MultiToolHolders.network;

import ak.MultiToolHolders.ItemMultiToolHolder;
import ak.MultiToolHolders.inventory.ToolHolderData;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by A.K. on 14/06/17.
 */
public class MessageHolderData implements IMessage, IMessageHandler<MessageHolderData, IMessage> {

    private NBTTagCompound nbt = new NBTTagCompound();

    public MessageHolderData(){}

    public MessageHolderData(ToolHolderData par1) {
        par1.writeToNBT(nbt);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.nbt);
    }

    @Override
    public IMessage onMessage(MessageHolderData message, MessageContext ctx) {
        EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
        if (player != null && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder) {
            ItemMultiToolHolder toolHolder = (ItemMultiToolHolder)player.getCurrentEquippedItem().getItem();
            ToolHolderData data = toolHolder.getInventoryFromItemStack(player.getCurrentEquippedItem()).data;
            data.readFromNBT(message.nbt);
        }
        return null;
    }
}
