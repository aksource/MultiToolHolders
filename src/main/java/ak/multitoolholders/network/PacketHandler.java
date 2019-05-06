package ak.multitoolholders.network;

import ak.multitoolholders.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;


/**
 * PacketHandler Created by A.K. on 14/05/28.
 */
public class PacketHandler {

  public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
      .named(new ResourceLocation(Constants.MOD_ID.toLowerCase(), "channel"))
      .networkProtocolVersion(() -> "1").clientAcceptedVersions(e -> true)
      .serverAcceptedVersions(e -> true).simpleChannel();

  public static void init() {
    INSTANCE
        .registerMessage(0, MessageKeyPressed.class, MessageKeyPressed.encoder,
            MessageKeyPressed.decoder, new MessageKeyPressedHandler());
  }
}
