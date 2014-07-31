package ak.MultiToolHolders.network;

import ak.MultiToolHolders.inventory.ToolHolderData;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by A.K. on 14/06/17.
 */
public class MessageHolderData implements IMessage {

    public NBTTagCompound nbt = new NBTTagCompound();

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
}
