package ak.multitoolholders;

import ak.multitoolholders.client.ClientSettingUtility;
import ak.multitoolholders.item.HolderType;
import ak.multitoolholders.item.MultiToolHolderItem;
import ak.multitoolholders.network.PacketHandler;
import ak.multitoolholders.util.RegistrationHandler;
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

import static ak.multitoolholders.Constants.MOD_ID;

@Mod(MOD_ID)
public class MultiToolHolders {
  public static Item itemMultiToolHolder3 = (new MultiToolHolderItem(HolderType.HOLDER3));
  public static Item itemMultiToolHolder5 = (new MultiToolHolderItem(HolderType.HOLDER5));
  public static Item itemMultiToolHolder7 = (new MultiToolHolderItem(HolderType.HOLDER7));
  public static Item itemMultiToolHolder9 = (new MultiToolHolderItem(HolderType.HOLDER9));

  public MultiToolHolders() {
    final IEventBus modEventBus =
        FMLJavaModLoadingContext.get().getModEventBus();
    modEventBus.addListener(this::preInit);
    modEventBus.addListener(this::doClientStuff);
    MinecraftForge.EVENT_BUS.register(this);
    RegistrationHandler.register(modEventBus);
    ModLoadingContext.get().registerConfig(Type.COMMON, ConfigUtils.configSpec);
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
        ForgeRegistries.ENCHANTMENTS.getKey(enchantment).toString());
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