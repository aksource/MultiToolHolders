package ak.multitoolholders.client;

import ak.multitoolholders.ConfigUtils;
import ak.multitoolholders.Constants;
import ak.multitoolholders.MultiToolHolders;
import ak.multitoolholders.inventory.ToolHolderContainer;
import ak.multitoolholders.item.HolderType;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.Map;

public class ClientSettingUtility {

  public static final KeyBinding OPEN_KEY = new KeyBinding("Key.openToolHolder", GLFW.GLFW_KEY_H,
          "MultiToolHolders");
  public static final KeyBinding NEXT_KEY = new KeyBinding("Key.nextToolHolder", GLFW.GLFW_KEY_U,
          "MultiToolHolders");
  public static final KeyBinding PREV_KEY = new KeyBinding("Key.prevToolHolder", GLFW.GLFW_KEY_Y,
          "MultiToolHolders");

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
    ScreenManager.registerFactory(ToolHolderContainer.TOOL_HOLDER_3_CONTAINER_TYPE,
            new ScreenManager.IScreenFactory<ToolHolderContainer, ToolHolderScreen>() {
              @Override
              @Nonnull
              public ToolHolderScreen create(@Nonnull ToolHolderContainer toolContainer, @Nonnull PlayerInventory playerInventory, @Nonnull ITextComponent textComponent) {
                return new ToolHolderScreen(toolContainer, playerInventory, textComponent, HolderType.HOLDER3);
              }
            });
    ScreenManager.registerFactory(ToolHolderContainer.TOOL_HOLDER_5_CONTAINER_TYPE,
            new ScreenManager.IScreenFactory<ToolHolderContainer, ToolHolderScreen>() {
              @Override
              @Nonnull
              public ToolHolderScreen create(@Nonnull ToolHolderContainer toolContainer, @Nonnull PlayerInventory playerInventory, @Nonnull ITextComponent textComponent) {
                return new ToolHolderScreen(toolContainer, playerInventory, textComponent, HolderType.HOLDER5);
              }
            });
    ScreenManager.registerFactory(ToolHolderContainer.TOOL_HOLDER_7_CONTAINER_TYPE,
            new ScreenManager.IScreenFactory<ToolHolderContainer, ToolHolderScreen>() {
              @Override
              @Nonnull
              public ToolHolderScreen create(@Nonnull ToolHolderContainer toolContainer, @Nonnull PlayerInventory playerInventory, @Nonnull ITextComponent textComponent) {
                return new ToolHolderScreen(toolContainer, playerInventory, textComponent, HolderType.HOLDER7);
              }
            });
    ScreenManager.registerFactory(ToolHolderContainer.TOOL_HOLDER_9_CONTAINER_TYPE,
            new ScreenManager.IScreenFactory<ToolHolderContainer, ToolHolderScreen>() {
              @Override
              @Nonnull
              public ToolHolderScreen create(@Nonnull ToolHolderContainer toolContainer, @Nonnull PlayerInventory playerInventory, @Nonnull ITextComponent textComponent) {
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

  public PlayerEntity getPlayer() {
    return Minecraft.getInstance().player;
  }

  @SubscribeEvent
  @SuppressWarnings("unused")
  public void bakedModelRegister(final ModelBakeEvent event) {
    changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_3);
    changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_5);
    changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_7);
    changeModel(event.getModelRegistry(), Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_9);
  }

  private void changeModel(Map<ResourceLocation, IBakedModel> modelRegistry, String name) {
    ModelResourceLocation rl = MODEL_RESOURCE_LOCATION_MAP.get(name);
    modelRegistry.computeIfPresent(rl, (r, m) -> new HolderRenderer(m));
  }
}