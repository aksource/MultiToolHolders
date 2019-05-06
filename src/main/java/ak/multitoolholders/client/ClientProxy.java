package ak.multitoolholders.client;

import static ak.multitoolholders.recipe.RecipeHandler.addRecipe;

import ak.multitoolholders.CommonProxy;
import ak.multitoolholders.ConfigUtils;
import ak.multitoolholders.Constants;
import ak.multitoolholders.EnumHolderType;
import ak.multitoolholders.ItemMultiToolHolder;
import ak.multitoolholders.MultiToolHolders;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.network.FMLPlayMessages.OpenContainer;
import org.lwjgl.glfw.GLFW;

public class ClientProxy extends CommonProxy {

  public static final KeyBinding OPEN_KEY = new KeyBinding("Key.openToolHolder", GLFW.GLFW_KEY_H,
      "MultiToolHolders");
  public static final KeyBinding NEXT_KEY = new KeyBinding("Key.nextToolHolder", GLFW.GLFW_KEY_U,
      "MultiToolHolders");
  public static final KeyBinding PREV_KEY = new KeyBinding("Key.prevToolHolder", GLFW.GLFW_KEY_Y,
      "MultiToolHolders");

  private static final Map<String, ModelResourceLocation> MODEL_RESOURCE_LOCATION_MAP = Maps
      .newHashMap();

  private boolean initialized = false;

  public static GuiScreen openGui(OpenContainer openContainer) {
    ResourceLocation guiId = openContainer.getId();
    if (Constants.MOD_ID.equals(guiId.getNamespace())) {
      EntityPlayer player = Minecraft.getInstance().player;
      ItemStack itemStack = player.getHeldItemMainhand();
      if (itemStack.getItem() instanceof ItemMultiToolHolder) {
        return openToolHolderGui(player, itemStack);
      }
    }
    return null;
  }

  public static GuiScreen openToolHolderGui(EntityPlayer player, ItemStack itemStack) {
    ItemMultiToolHolder toolHolder = (ItemMultiToolHolder) itemStack.getItem();
    EnumHolderType type = toolHolder.getType();
    int currentSlot = player.inventory.currentItem;
    return new GuiToolHolder(player, itemStack, type, currentSlot);
  }

  @Override
  public void registerClientPreInformation() {
  }

  @Override
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

  }

  private void registerMap(Item item) {
    if (item.getRegistryName() != null) {
      String name = item.getRegistryName().getPath();
      MODEL_RESOURCE_LOCATION_MAP
          .put(name, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
  }

  @Override
  public EntityPlayer getPlayer() {
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

  private void changeModel(Map<ModelResourceLocation, IBakedModel> modelRegistry, String name) {
    ModelResourceLocation rl = MODEL_RESOURCE_LOCATION_MAP.get(name);
    modelRegistry.computeIfPresent(rl, (r, m) -> new HolderRenderer(m));
  }

  @SubscribeEvent
  @SuppressWarnings("unused")
  public void joinIn(final EntityJoinWorldEvent event) {
    if (!this.initialized) {
      MinecraftServer server = Minecraft.getInstance().getIntegratedServer();
      if (Objects.nonNull(server)) {
        RecipeManager recipeManager = server.getRecipeManager();
        addRecipe(recipeManager::addRecipe);
      }
      this.initialized = true;
    }
  }
}