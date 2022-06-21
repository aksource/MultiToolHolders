package ak.mcmod.multitoolholders;

import ak.mcmod.multitoolholders.client.ClientSettingUtility;
import ak.mcmod.multitoolholders.network.PacketHandler;
import ak.mcmod.multitoolholders.util.RegistrationHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

import static ak.mcmod.multitoolholders.Constants.MOD_ID;

@Mod(MOD_ID)
public class MultiToolHolders {

  public MultiToolHolders() {
    final var modEventBus =
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
    if (Objects.isNull(item) || Objects.isNull(enchantment) || lv < 0) {
      return;
    }

    if (!item.getOrCreateTag().contains(Constants.NBT_KEY_ENCHANT)) {
      item.getOrCreateTag().put(Constants.NBT_KEY_ENCHANT, new ListTag());
    }

    var tagList = item.getOrCreateTag().getList(Constants.NBT_KEY_ENCHANT,
            Tag.TAG_COMPOUND);
    var nbtTagCompound = new CompoundTag();
    nbtTagCompound.putString(Constants.NBT_KEY_ENCHANT_ID,
            Objects.requireNonNull(ForgeRegistries.ENCHANTMENTS.getKey(enchantment)).toString());
    nbtTagCompound.putShort(Constants.NBT_KEY_ENCHANT_LEVEL, (short) (lv));
    tagList.add(nbtTagCompound);
  }

  private void preInit(final FMLCommonSetupEvent event) {
    PacketHandler.init();
  }

  private void doClientStuff(final FMLClientSetupEvent event) {
    var proxy = new ClientSettingUtility();
    final var modEventBus =
            FMLJavaModLoadingContext.get().getModEventBus();
    modEventBus.addListener(proxy::bakedModelRegister);
    proxy.registerClientInformation(event);
  }
}