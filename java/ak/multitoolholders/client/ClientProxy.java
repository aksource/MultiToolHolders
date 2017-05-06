package ak.multitoolholders.client;

import ak.multitoolholders.CommonProxy;
import ak.multitoolholders.Constants;
import ak.multitoolholders.MultiToolHolders;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.Map;

public class ClientProxy extends CommonProxy {
    public static final KeyBinding OpenKey = new KeyBinding("Key.openToolHolder", Keyboard.KEY_H, "multitoolholders");
    public static final KeyBinding NextKey = new KeyBinding("Key.nextToolHolder", Keyboard.KEY_U, "multitoolholders");
    public static final KeyBinding PrevKey = new KeyBinding("Key.prevToolHolder", Keyboard.KEY_Y, "multitoolholders");

    private static final Map<String, ModelResourceLocation> MODEL_RESOURCE_LOCATION_MAP = Maps.newHashMap();

    @Override
    public void registerClientInformation() {
        FMLCommonHandler.instance().bus().register(new KeyInputHandler());
        if (MultiToolHolders.enableDisplayToolHolderInventory) {
            MinecraftForge.EVENT_BUS.register(new RenderingHolderInventoryHUD());
        }
        ClientRegistry.registerKeyBinding(OpenKey);
        ClientRegistry.registerKeyBinding(NextKey);
        ClientRegistry.registerKeyBinding(PrevKey);

        registerItemClient(MultiToolHolders.ItemMultiToolHolder3);
        registerItemClient(MultiToolHolders.ItemMultiToolHolder5);
        registerItemClient(MultiToolHolders.ItemMultiToolHolder7);
        registerItemClient(MultiToolHolders.ItemMultiToolHolder9);
    }

    private void registerItemClient(Item item) {
        if (item.getRegistryName() != null) {
            String name = item.getRegistryName().getResourcePath();
            MODEL_RESOURCE_LOCATION_MAP.put(name, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, MODEL_RESOURCE_LOCATION_MAP.get(name));
        }
    }

    @Override
    public EntityPlayer getPlayer() {
        return Minecraft.getMinecraft().player;
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void bakedModelRegister(ModelBakeEvent event) {
        changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_3);
        changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_5);
        changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_7);
        changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_9);
    }

    private void changeModel(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry, String name) {
        IBakedModel holderOrgModel = modelRegistry.getObject(MODEL_RESOURCE_LOCATION_MAP.get(name));
        modelRegistry.putObject(MODEL_RESOURCE_LOCATION_MAP.get(name), new HolderRenderer(holderOrgModel));
    }
}