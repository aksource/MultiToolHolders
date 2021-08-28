package ak.multitoolholders.util;

import ak.multitoolholders.Constants;
import ak.multitoolholders.MultiToolHolders;
import ak.multitoolholders.inventory.ToolHolderContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by A.K. on 2021/08/28.
 */
@ParametersAreNonnullByDefault
public class RegistrationHandler {
  private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
  private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Constants.MOD_ID);
  public static void register(IEventBus eventBus) {
    ITEMS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_3, () -> MultiToolHolders.itemMultiToolHolder3);
    ITEMS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_5, () -> MultiToolHolders.itemMultiToolHolder5);
    ITEMS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_7, () -> MultiToolHolders.itemMultiToolHolder7);
    ITEMS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_9, () -> MultiToolHolders.itemMultiToolHolder9);
    ITEMS.register(eventBus);
    CONTAINERS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_3, () -> ToolHolderContainer.TOOL_HOLDER_3_CONTAINER_TYPE);
    CONTAINERS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_5, () -> ToolHolderContainer.TOOL_HOLDER_5_CONTAINER_TYPE);
    CONTAINERS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_7, () -> ToolHolderContainer.TOOL_HOLDER_7_CONTAINER_TYPE);
    CONTAINERS.register(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_9, () -> ToolHolderContainer.TOOL_HOLDER_9_CONTAINER_TYPE);
    CONTAINERS.register(eventBus);
  }
}
