package ak.MultiToolHolders.network;

import ak.MultiToolHolders.MultiToolHolders;
import ak.MultiToolHolders.network.MessageKeyPressed;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/**
 * Created by A.K. on 14/05/28.
 */
public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(MultiToolHolders.MOD_ID.toLowerCase());

    public static void init() {
        INSTANCE.registerMessage(MessageKeyPressedHandler.class, MessageKeyPressed.class, 0, Side.SERVER);
        INSTANCE.registerMessage(MessageHolderDataHandler.class, MessageHolderData.class, 1, Side.CLIENT);
    }
}
