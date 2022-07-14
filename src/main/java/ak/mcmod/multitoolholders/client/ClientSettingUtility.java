package ak.mcmod.multitoolholders.client;

import ak.mcmod.multitoolholders.ConfigUtils;
import ak.mcmod.multitoolholders.Constants;
import ak.mcmod.multitoolholders.inventory.ToolHolderContainer;
import ak.mcmod.multitoolholders.item.HolderType;
import ak.mcmod.multitoolholders.util.RegistrationHandler;
import com.google.common.collect.Maps;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.Objects;

public class ClientSettingUtility {

  public static final KeyMapping OPEN_KEY = new KeyMapping(Constants.KEY_OPEN, GLFW.GLFW_KEY_H, Constants.KEY_CATEGORY);
  public static final KeyMapping NEXT_KEY = new KeyMapping(Constants.KEY_NEXT, GLFW.GLFW_KEY_U, Constants.KEY_CATEGORY);
  public static final KeyMapping PREV_KEY = new KeyMapping(Constants.KEY_PREVIOUS, GLFW.GLFW_KEY_Y, Constants.KEY_CATEGORY);

  private static final Map<String, ModelResourceLocation> MODEL_RESOURCE_LOCATION_MAP = Maps
          .newHashMap();

  public void registerKeyBinding(final RegisterKeyMappingsEvent event) {
    event.register(OPEN_KEY);
    event.register(NEXT_KEY);
    event.register(PREV_KEY);
  }

  public void registerClientInformation(final FMLClientSetupEvent event) {
    registerMap(RegistrationHandler.itemMultiToolHolder3.get());
    registerMap(RegistrationHandler.itemMultiToolHolder5.get());
    registerMap(RegistrationHandler.itemMultiToolHolder7.get());
    registerMap(RegistrationHandler.itemMultiToolHolder9.get());
    MinecraftForge.EVENT_BUS.register(this);
    MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    if (ConfigUtils.COMMON.enableDisplayToolHolderInventory) {
      MinecraftForge.EVENT_BUS.register(new RenderingHolderInventoryHUD());
    }

    MenuScreens.register(ToolHolderContainer.TOOL_HOLDER_3_CONTAINER_TYPE,(ToolHolderContainer container, Inventory inventory, Component component) -> new ToolHolderScreen(container, inventory, component, HolderType.HOLDER3));
    MenuScreens.register(ToolHolderContainer.TOOL_HOLDER_5_CONTAINER_TYPE,(ToolHolderContainer container, Inventory inventory, Component component) -> new ToolHolderScreen(container, inventory, component, HolderType.HOLDER5));
    MenuScreens.register(ToolHolderContainer.TOOL_HOLDER_7_CONTAINER_TYPE,(ToolHolderContainer container, Inventory inventory, Component component) -> new ToolHolderScreen(container, inventory, component, HolderType.HOLDER7));
    MenuScreens.register(ToolHolderContainer.TOOL_HOLDER_9_CONTAINER_TYPE,(ToolHolderContainer container, Inventory inventory, Component component) -> new ToolHolderScreen(container, inventory, component, HolderType.HOLDER9));
  }

  private void registerMap(Item item) {
    var key = ForgeRegistries.ITEMS.getKey(item);
    if (Objects.nonNull(key)) {
      var path = key.getPath();
      MODEL_RESOURCE_LOCATION_MAP
              .put(path, new ModelResourceLocation(key, "inventory"));
    }
  }

  @SubscribeEvent
  public void bakedModelRegister(final BakingCompleted event) {
    changeModel(event.getModels(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_3);
    changeModel(event.getModels(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_5);
    changeModel(event.getModels(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_7);
    changeModel(event.getModels(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_9);
  }

  private void changeModel(Map<ResourceLocation, BakedModel> models, String name) {
    var rl = MODEL_RESOURCE_LOCATION_MAP.get(name);
    models.computeIfPresent(rl, (r, m) -> new HolderRenderer(m));
  }
}