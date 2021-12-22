package ak.mcmod.multitoolholders.client;

import ak.mcmod.multitoolholders.ConfigUtils;
import ak.mcmod.multitoolholders.Constants;
import ak.mcmod.multitoolholders.MultiToolHolders;
import ak.mcmod.multitoolholders.inventory.ToolHolderContainer;
import ak.mcmod.multitoolholders.item.HolderType;
import com.google.common.collect.Maps;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.Map;

public class ClientSettingUtility {

  public static final KeyMapping OPEN_KEY = new KeyMapping(Constants.KEY_OPEN, GLFW.GLFW_KEY_H, Constants.KEY_CATEGORY);
  public static final KeyMapping NEXT_KEY = new KeyMapping(Constants.KEY_NEXT, GLFW.GLFW_KEY_U, Constants.KEY_CATEGORY);
  public static final KeyMapping PREV_KEY = new KeyMapping(Constants.KEY_PREVIOUS, GLFW.GLFW_KEY_Y, Constants.KEY_CATEGORY);

  private static final Map<String, ModelResourceLocation> MODEL_RESOURCE_LOCATION_MAP = Maps
          .newHashMap();

  public void registerClientInformation(final FMLClientSetupEvent event) {
    registerMap(MultiToolHolders.itemMultiToolHolder3);
    registerMap(MultiToolHolders.itemMultiToolHolder5);
    registerMap(MultiToolHolders.itemMultiToolHolder7);
    registerMap(MultiToolHolders.itemMultiToolHolder9);
    MinecraftForge.EVENT_BUS.register(this);
    MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    if (ConfigUtils.COMMON.enableDisplayToolHolderInventory) {
      MinecraftForge.EVENT_BUS.register(new RenderingHolderInventoryHUD());
    }
    ClientRegistry.registerKeyBinding(OPEN_KEY);
    ClientRegistry.registerKeyBinding(NEXT_KEY);
    ClientRegistry.registerKeyBinding(PREV_KEY);
    MenuScreens.register(ToolHolderContainer.TOOL_HOLDER_3_CONTAINER_TYPE,
            new MenuScreens.ScreenConstructor<ToolHolderContainer, ToolHolderScreen>() {
              @Override
              @Nonnull
              public ToolHolderScreen create(@Nonnull ToolHolderContainer toolContainer, @Nonnull Inventory playerInventory, @Nonnull Component textComponent) {
                return new ToolHolderScreen(toolContainer, playerInventory, textComponent, HolderType.HOLDER3);
              }
            });
    MenuScreens.register(ToolHolderContainer.TOOL_HOLDER_5_CONTAINER_TYPE,
            new MenuScreens.ScreenConstructor<ToolHolderContainer, ToolHolderScreen>() {
              @Override
              @Nonnull
              public ToolHolderScreen create(@Nonnull ToolHolderContainer toolContainer, @Nonnull Inventory playerInventory, @Nonnull Component textComponent) {
                return new ToolHolderScreen(toolContainer, playerInventory, textComponent, HolderType.HOLDER5);
              }
            });
    MenuScreens.register(ToolHolderContainer.TOOL_HOLDER_7_CONTAINER_TYPE,
            new MenuScreens.ScreenConstructor<ToolHolderContainer, ToolHolderScreen>() {
              @Override
              @Nonnull
              public ToolHolderScreen create(@Nonnull ToolHolderContainer toolContainer, @Nonnull Inventory playerInventory, @Nonnull Component textComponent) {
                return new ToolHolderScreen(toolContainer, playerInventory, textComponent, HolderType.HOLDER7);
              }
            });
    MenuScreens.register(ToolHolderContainer.TOOL_HOLDER_9_CONTAINER_TYPE,
            new MenuScreens.ScreenConstructor<ToolHolderContainer, ToolHolderScreen>() {
              @Override
              @Nonnull
              public ToolHolderScreen create(@Nonnull ToolHolderContainer toolContainer, @Nonnull Inventory playerInventory, @Nonnull Component textComponent) {
                return new ToolHolderScreen(toolContainer, playerInventory, textComponent, HolderType.HOLDER9);
              }
            });

  }

  private void registerMap(Item item) {
    if (item.getRegistryName() != null) {
      String name = item.getRegistryName().getPath();
      MODEL_RESOURCE_LOCATION_MAP
              .put(name, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
  }

  public Player getPlayer() {
    return Minecraft.getInstance().player;
  }

  @SubscribeEvent
  public void bakedModelRegister(final ModelBakeEvent event) {
    changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_3);
    changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_5);
    changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_7);
    changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_9);
  }

  private void changeModel(Map<ResourceLocation, BakedModel> modelRegistry, String name) {
    ModelResourceLocation rl = MODEL_RESOURCE_LOCATION_MAP.get(name);
    modelRegistry.computeIfPresent(rl, (r, m) -> new HolderRenderer(m));
  }
}