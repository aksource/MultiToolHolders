package ak.mcmod.multitoolholders.util;

import ak.mcmod.multitoolholders.Constants;
import ak.mcmod.multitoolholders.inventory.ToolHolderContainer;
import ak.mcmod.multitoolholders.item.HolderType;
import ak.mcmod.multitoolholders.item.MultiToolHolderItem;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by A.K. on 2021/08/28.
 */
@ParametersAreNonnullByDefault
public class RegistrationHandler {
  private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
  private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Constants.MOD_ID);
  public static final RegistryObject<Item> itemMultiToolHolder3 = ITEMS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_3, () -> new MultiToolHolderItem(HolderType.HOLDER3));
  public static final RegistryObject<Item> itemMultiToolHolder5 = ITEMS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_5, () -> new MultiToolHolderItem(HolderType.HOLDER5));
  public static final RegistryObject<Item> itemMultiToolHolder7 = ITEMS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_7, () -> new MultiToolHolderItem(HolderType.HOLDER7));
  public static final RegistryObject<Item> itemMultiToolHolder9 = ITEMS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_9, () -> new MultiToolHolderItem(HolderType.HOLDER9));

  public static void register(IEventBus eventBus) {
    ITEMS.register(eventBus);
    CONTAINERS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_3, () -> ToolHolderContainer.TOOL_HOLDER_3_CONTAINER_TYPE);
    CONTAINERS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_5, () -> ToolHolderContainer.TOOL_HOLDER_5_CONTAINER_TYPE);
    CONTAINERS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_7, () -> ToolHolderContainer.TOOL_HOLDER_7_CONTAINER_TYPE);
    CONTAINERS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_9, () -> ToolHolderContainer.TOOL_HOLDER_9_CONTAINER_TYPE);
    CONTAINERS.register(eventBus);
  }
}
