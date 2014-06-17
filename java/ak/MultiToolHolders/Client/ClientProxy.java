package ak.MultiToolHolders.Client;

import ak.MultiToolHolders.CommonProxy;
import ak.MultiToolHolders.MultiToolHolders;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy
{
	public static final KeyBinding OpenKey = new KeyBinding("Key.openToolHolder",Keyboard.KEY_F, "MultiToolHolders");
	public static final KeyBinding NextKey = new KeyBinding("Key.nextToolHolder",Keyboard.KEY_T, "MultiToolHolders");
	public static final KeyBinding PrevKey = new KeyBinding("Key.prevToolHolder",Keyboard.KEY_R, "MultiToolHolders");
	@Override
	public void registerClientInformation()
	{
        FMLCommonHandler.instance().bus().register(new KeyInputHandler());
		ClientRegistry.registerKeyBinding(OpenKey);
		ClientRegistry.registerKeyBinding(NextKey);
		ClientRegistry.registerKeyBinding(PrevKey);
		MinecraftForgeClient.registerItemRenderer(MultiToolHolders.ItemMultiToolHolder3, (IItemRenderer) MultiToolHolders.ItemMultiToolHolder3);
		MinecraftForgeClient.registerItemRenderer(MultiToolHolders.ItemMultiToolHolder5, (IItemRenderer) MultiToolHolders.ItemMultiToolHolder5);
		MinecraftForgeClient.registerItemRenderer(MultiToolHolders.ItemMultiToolHolder9, (IItemRenderer) MultiToolHolders.ItemMultiToolHolder9);
		MinecraftForgeClient.registerItemRenderer(MultiToolHolders.ItemMultiToolHolder7, (IItemRenderer) MultiToolHolders.ItemMultiToolHolder7);
	}
}