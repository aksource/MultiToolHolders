package ak.multitoolholders;

import ak.multitoolholders.network.PacketHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import static ak.multitoolholders.Constants.NBT_KEY_ENCHANT;
import static ak.multitoolholders.Constants.NBT_KEY_ENCHANT_ID;
import static ak.multitoolholders.Constants.NBT_KEY_ENCHANT_LEVEL;

@Mod(modid = Constants.MOD_ID,
        name = Constants.MOD_NAME,
        version = Constants.MOD_VERSION,
        dependencies = Constants.MOD_DEPENDENCIES,
        useMetadata = true,
        acceptedMinecraftVersions = Constants.MOD_MC_VERSION)
public class MultiToolHolders {
    //Logger
    public static final Logger logger = Logger.getLogger(Constants.MOD_ID);
    public static Item ItemMultiToolHolder3 = (new ItemMultiToolHolder(EnumHolderType.HOLDER3)).setRegistryName(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_3).setUnlocalizedName(Constants.MOD_ID.toLowerCase() + ":Holder3");
    public static Item ItemMultiToolHolder5 = (new ItemMultiToolHolder(EnumHolderType.HOLDER5)).setRegistryName(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_5).setUnlocalizedName(Constants.MOD_ID.toLowerCase() + ":Holder5");
    public static Item ItemMultiToolHolder7 = (new ItemMultiToolHolder(EnumHolderType.HOLDER7)).setRegistryName(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_7).setUnlocalizedName(Constants.MOD_ID.toLowerCase() + ":Holder7");
    public static Item ItemMultiToolHolder9 = (new ItemMultiToolHolder(EnumHolderType.HOLDER9)).setRegistryName(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_9).setUnlocalizedName(Constants.MOD_ID.toLowerCase() + ":Holder9");
    public static int toolHolderInvX = 0;
    public static int toolHolderInvY = 0;
    public static boolean enableDisplayToolHolderInventory = true;
    public static String[] toolStrArray = new String[]{};
    public static Set<String> toolNameSet = new HashSet<>();
    @Mod.Instance(Constants.MOD_ID)
    public static MultiToolHolders instance;
    @SidedProxy(clientSide = "ak.multitoolholders.client.ClientProxy", serverSide = "ak.multitoolholders.CommonProxy")
    public static CommonProxy proxy;

    public static void addEnchantmentToItem(ItemStack item,
                                            Enchantment enchantment, int Lv) {
        if (item == null || enchantment == null || Lv < 0) {
            return;
        }
        if (item.hasTagCompound()) {
            item.setTagCompound(new NBTTagCompound());
        }

        if (!item.getTagCompound().hasKey(NBT_KEY_ENCHANT, net.minecraftforge.common.util.Constants.NBT.TAG_LIST)) {
            item.getTagCompound().setTag(NBT_KEY_ENCHANT, new NBTTagList());
        }

        NBTTagList tagList = item.getTagCompound().getTagList(NBT_KEY_ENCHANT, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.setShort(NBT_KEY_ENCHANT_ID, (short) Enchantment.getEnchantmentID(enchantment));
        nbtTagCompound.setShort(NBT_KEY_ENCHANT_LEVEL, (short) (Lv));
        tagList.appendTag(nbtTagCompound);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        enableDisplayToolHolderInventory = config.get(Configuration.CATEGORY_GENERAL, "enableDisplayToolHolderInventory", enableDisplayToolHolderInventory, "enable to display toolholder inventory in HUD").getBoolean();
        toolHolderInvX = config.get(Configuration.CATEGORY_GENERAL, "toolHolderInvX", toolHolderInvX, "ToolHolder Inventory x-position in HUD").getInt();
        toolHolderInvY = config.get(Configuration.CATEGORY_GENERAL, "toolHolderInvY", toolHolderInvY, "ToolHolder Inventory y-position in HUD").getInt();
        toolStrArray = config.get(Configuration.CATEGORY_GENERAL, "toolStrArray", toolStrArray, "Tool ids that can set ToolHolders.").getStringList();
        toolNameSet.addAll(Arrays.asList(toolStrArray));
        config.save();

        GameRegistry.register(ItemMultiToolHolder3);
        GameRegistry.register(ItemMultiToolHolder5);
        GameRegistry.register(ItemMultiToolHolder9);
        GameRegistry.register(ItemMultiToolHolder7);

        PacketHandler.init();
    }

    @Mod.EventHandler
    public void load(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
        MinecraftForge.EVENT_BUS.register(proxy);
        ItemStack[] toolHolders = new ItemStack[]{new ItemStack(ItemMultiToolHolder3), new ItemStack(ItemMultiToolHolder5), new ItemStack(ItemMultiToolHolder7), new ItemStack(ItemMultiToolHolder9)};
        ItemStack[] holderMaterials = new ItemStack[]{new ItemStack(Items.IRON_INGOT), new ItemStack(Items.DYE, 1, 4), new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.DIAMOND)};
        for (int i = 0; i < toolHolders.length; i++) {
            GameRegistry.addRecipe(toolHolders[i],
                    "AAA",
                    "ABA",
                    "CCC",
                    'A', holderMaterials[i],
                    'B', Blocks.CHEST,
                    'C', Blocks.TRIPWIRE_HOOK);
        }
        proxy.registerClientInformation();
//        if(Debug) DebugSystem();
    }
}