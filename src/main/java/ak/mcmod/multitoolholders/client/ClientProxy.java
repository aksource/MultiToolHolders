package ak.mcmod.multitoolholders.client;

import ak.mcmod.multitoolholders.CommonProxy;
import ak.mcmod.multitoolholders.Constants;
import ak.mcmod.multitoolholders.MultiToolHolders;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.Map;
import java.util.Objects;

public class ClientProxy extends CommonProxy {
  public static final KeyBinding OpenKey = new KeyBinding(Constants.KEY_OPEN, Keyboard.KEY_H, Constants.KEY_CATEGORY);
  public static final KeyBinding NextKey = new KeyBinding(Constants.KEY_NEXT, Keyboard.KEY_U, Constants.KEY_CATEGORY);
  public static final KeyBinding PrevKey = new KeyBinding(Constants.KEY_PREVIOUS, Keyboard.KEY_Y, Constants.KEY_CATEGORY);

  private static final Map<String, ModelResourceLocation> MODEL_RESOURCE_LOCATION_MAP = Maps.newHashMap();

  @Override
  public void registerClientPreInformation() {
    registerMap(MultiToolHolders.itemMultiToolHolder3);
    registerMap(MultiToolHolders.itemMultiToolHolder5);
    registerMap(MultiToolHolders.itemMultiToolHolder7);
    registerMap(MultiToolHolders.itemMultiToolHolder9);
  }

  @Override
  public void registerClientInformation() {
    MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    if (MultiToolHolders.enableDisplayToolHolderInventory) {
      MinecraftForge.EVENT_BUS.register(new RenderingHolderInventoryHUD());
    }
    ClientRegistry.registerKeyBinding(OpenKey);
    ClientRegistry.registerKeyBinding(NextKey);
    ClientRegistry.registerKeyBinding(PrevKey);

    registerItemClient(MultiToolHolders.itemMultiToolHolder3);
    registerItemClient(MultiToolHolders.itemMultiToolHolder5);
    registerItemClient(MultiToolHolders.itemMultiToolHolder7);
    registerItemClient(MultiToolHolders.itemMultiToolHolder9);
  }

  private void registerItemClient(Item item) {
    if (item.getRegistryName() != null) {
      String name = item.getRegistryName().getPath();
      Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, MODEL_RESOURCE_LOCATION_MAP.get(name));
    }
  }

  private void registerMap(Item item) {
    if (item.getRegistryName() != null) {
      String name = item.getRegistryName().getPath();
      MODEL_RESOURCE_LOCATION_MAP.put(name, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
  }

  @Override
  public EntityPlayer getPlayer() {
    return Minecraft.getMinecraft().player;
  }

  @SubscribeEvent
  public void bakedModelRegister(final ModelBakeEvent event) {
    changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_3);
    changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_5);
    changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_7);
    changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_9);
  }

  private void changeModel(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry, String name) {
    ModelResourceLocation rl = MODEL_RESOURCE_LOCATION_MAP.get(name);
    IBakedModel holderOrgModel = modelRegistry.getObject(rl);
    if (Objects.nonNull(holderOrgModel)) {
      modelRegistry.putObject(rl, new HolderRenderer(holderOrgModel));
    }
  }

  static {

  }
}