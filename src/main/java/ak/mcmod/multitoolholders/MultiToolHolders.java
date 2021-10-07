package ak.mcmod.multitoolholders;

import ak.mcmod.multitoolholders.client.ClientSettingUtility;
import ak.mcmod.multitoolholders.item.HolderType;
import ak.mcmod.multitoolholders.item.MultiToolHolderItem;
import ak.mcmod.multitoolholders.network.PacketHandler;
import ak.mcmod.multitoolholders.util.RegistrationHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

import static ak.mcmod.multitoolholders.Constants.MOD_ID;

@Mod(MOD_ID)
public class MultiToolHolders {
  public static final Item itemMultiToolHolder3 = (new MultiToolHolderItem(HolderType.HOLDER3));
  public static final Item itemMultiToolHolder5 = (new MultiToolHolderItem(HolderType.HOLDER5));
  public static final Item itemMultiToolHolder7 = (new MultiToolHolderItem(HolderType.HOLDER7));
  public static final Item itemMultiToolHolder9 = (new MultiToolHolderItem(HolderType.HOLDER9));
  public static final Logger LOGGER = LogManager.getLogger();

  public MultiToolHolders() {
    final IEventBus modEventBus =
            FMLJavaModLoadingContext.get().getModEventBus();
    modEventBus.addListener(this::preInit);
    modEventBus.addListener(this::doClientStuff);
    MinecraftForge.EVENT_BUS.register(this);
    MinecraftForge.EVENT_BUS.register(ToolHolderEventHook.class);
    RegistrationHandler.register(modEventBus);
    ModLoadingContext.get().registerConfig(Type.COMMON, ConfigUtils.configSpec);
    modEventBus.register(ConfigUtils.class);
  }

  public static void addEnchantmentToItem(ItemStack item,
                                          Enchantment enchantment, int lv) {
    if (item == null || enchantment == null || lv < 0) {
      return;
    }

    if (!item.getOrCreateTag().contains(Constants.NBT_KEY_ENCHANT)) {
      item.getOrCreateTag().put(Constants.NBT_KEY_ENCHANT, new ListNBT());
    }

    ListNBT tagList = item.getOrCreateTag().getList(Constants.NBT_KEY_ENCHANT,
            NBT.TAG_COMPOUND);
    CompoundNBT nbtTagCompound = new CompoundNBT();
    nbtTagCompound.putString(Constants.NBT_KEY_ENCHANT_ID,
            Objects.requireNonNull(ForgeRegistries.ENCHANTMENTS.getKey(enchantment)).toString());
    nbtTagCompound.putShort(Constants.NBT_KEY_ENCHANT_LEVEL, (short) (lv));
    tagList.add(nbtTagCompound);
  }

  private void preInit(final FMLCommonSetupEvent event) {
    PacketHandler.init();
  }

  private void doClientStuff(final FMLClientSetupEvent event) {
    ClientSettingUtility proxy = new ClientSettingUtility();
    final IEventBus modEventBus =
            FMLJavaModLoadingContext.get().getModEventBus();
    modEventBus.addListener(proxy::bakedModelRegister);
    proxy.registerClientInformation(event);
  }
}