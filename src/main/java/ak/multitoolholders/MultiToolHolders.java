package ak.multitoolholders;

import static ak.multitoolholders.Constants.MOD_ID;
import static ak.multitoolholders.recipe.RecipeHandler.addRecipe;

import ak.multitoolholders.client.ClientProxy;
import ak.multitoolholders.network.PacketHandler;
import java.util.logging.Logger;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

@Mod(MOD_ID)
public class MultiToolHolders {

  //Logger
  public static final Logger LOGGER = Logger.getLogger(MOD_ID);
  public static Item itemMultiToolHolder3 = (new ItemMultiToolHolder(EnumHolderType.HOLDER3))
      .setRegistryName(MOD_ID, Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_3);
  public static Item itemMultiToolHolder5 = (new ItemMultiToolHolder(EnumHolderType.HOLDER5))
      .setRegistryName(MOD_ID, Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_5);
  public static Item itemMultiToolHolder7 = (new ItemMultiToolHolder(EnumHolderType.HOLDER7))
      .setRegistryName(MOD_ID, Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_7);
  public static Item itemMultiToolHolder9 = (new ItemMultiToolHolder(EnumHolderType.HOLDER9))
      .setRegistryName(MOD_ID, Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_9);

  public MultiToolHolders() {
    final IEventBus modEventBus =
        FMLJavaModLoadingContext.get().getModEventBus();
    modEventBus.addListener(this::preInit);
    modEventBus.addListener(this::doClientStuff);
    modEventBus.addListener(this::doServerStuff);
    MinecraftForge.EVENT_BUS.register(this);
    ModLoadingContext.get()
        .registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> ClientProxy::openGui);
    ModLoadingContext.get().registerConfig(Type.COMMON, ConfigUtils.configSpec);
  }

  public static void addEnchantmentToItem(ItemStack item,
      Enchantment enchantment, int lv) {
    if (item == null || enchantment == null || lv < 0) {
      return;
    }

    if (!item.getOrCreateTag().contains(Constants.NBT_KEY_ENCHANT)) {
      item.getOrCreateTag().put(Constants.NBT_KEY_ENCHANT, new NBTTagList());
    }

    NBTTagList tagList = item.getOrCreateTag().getList(Constants.NBT_KEY_ENCHANT,
        NBT.TAG_COMPOUND);
    NBTTagCompound nbtTagCompound = new NBTTagCompound();
    nbtTagCompound.putString(Constants.NBT_KEY_ENCHANT_ID,
        ForgeRegistries.ENCHANTMENTS.getKey(enchantment).toString());
    nbtTagCompound.putShort(Constants.NBT_KEY_ENCHANT_LEVEL, (short) (lv));
    tagList.add(nbtTagCompound);
  }

  private void preInit(final FMLCommonSetupEvent event) {
    PacketHandler.init();
  }

  private void doClientStuff(final FMLClientSetupEvent event) {
    new ClientProxy().registerClientInformation(event);
  }

  private void doServerStuff(final FMLDedicatedServerSetupEvent event) {
    MinecraftServer server = event.getServerSupplier().get();
    RecipeManager recipeManager = server.getRecipeManager();
    addRecipe(recipeManager::addRecipe);
  }

  @SuppressWarnings("unused")
  @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
  public static class RegistryEvents {

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void registerItems(final RegistryEvent.Register<Item> event) {
      IForgeRegistry<Item> registry = event.getRegistry();
      registry.register(itemMultiToolHolder3);
      registry.register(itemMultiToolHolder5);
      registry.register(itemMultiToolHolder9);
      registry.register(itemMultiToolHolder7);
    }
  }
}