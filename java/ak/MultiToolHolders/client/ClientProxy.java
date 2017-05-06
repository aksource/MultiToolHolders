package ak.MultiToolHolders.client;

import ak.MultiToolHolders.CommonProxy;
import ak.MultiToolHolders.MultiToolHolders;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy
{
	public static final KeyBinding OpenKey = new KeyBinding("Key.openToolHolder",Keyboard.KEY_F, "MultiToolHolders");
	public static final KeyBinding NextKey = new KeyBinding("Key.nextToolHolder",Keyboard.KEY_T, "MultiToolHolders");
	public static final KeyBinding PrevKey = new KeyBinding("Key.prevToolHolder",Keyboard.KEY_R, "MultiToolHolders");
	@Override
	public void registerClientInformation()
	{
        IItemRenderer multiToolRenderer = new HolderRenderer();
        FMLCommonHandler.instance().bus().register(new KeyInputHandler());
        if (MultiToolHolders.enableDisplayToolHolderInventory) {
            MinecraftForge.EVENT_BUS.register(new RenderingHolderInventoryHUD());
        }
        ClientRegistry.registerKeyBinding(OpenKey);
		ClientRegistry.registerKeyBinding(NextKey);
		ClientRegistry.registerKeyBinding(PrevKey);
		MinecraftForgeClient.registerItemRenderer(MultiToolHolders.itemMultiToolHolder3, multiToolRenderer);
		MinecraftForgeClient.registerItemRenderer(MultiToolHolders.itemMultiToolHolder5, multiToolRenderer);
		MinecraftForgeClient.registerItemRenderer(MultiToolHolders.itemMultiToolHolder9, multiToolRenderer);
		MinecraftForgeClient.registerItemRenderer(MultiToolHolders.itemMultiToolHolder7, multiToolRenderer);
	}

    @Override
    public EntityPlayer getPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }
}