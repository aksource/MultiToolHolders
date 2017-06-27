package ak.multitoolholders;

import ak.multitoolholders.network.PacketHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Mod(modid = Constants.MOD_ID,
        name = Constants.MOD_NAME,
        version = Constants.MOD_VERSION,
        dependencies = Constants.MOD_DEPENDENCIES,
        useMetadata = true,
        acceptedMinecraftVersions = Constants.MOD_MC_VERSION)
public class MultiToolHolders {
    //Logger
    public static final Logger logger = Logger.getLogger("MultiToolHolders");
    public static Item itemMultiToolHolder3 = (new ItemMultiToolHolder(EnumHolderType.HOLDER3)).setRegistryName(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_3).setUnlocalizedName(Constants.MOD_ID.toLowerCase() + ":Holder3");
    public static Item itemMultiToolHolder5 = (new ItemMultiToolHolder(EnumHolderType.HOLDER5)).setRegistryName(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_5).setUnlocalizedName(Constants.MOD_ID.toLowerCase() + ":Holder5");
    public static Item itemMultiToolHolder7 = (new ItemMultiToolHolder(EnumHolderType.HOLDER7)).setRegistryName(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_7).setUnlocalizedName(Constants.MOD_ID.toLowerCase() + ":Holder7");
    public static Item itemMultiToolHolder9 = (new ItemMultiToolHolder(EnumHolderType.HOLDER9)).setRegistryName(Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_9).setUnlocalizedName(Constants.MOD_ID.toLowerCase() + ":Holder9");
    public static int toolHolderInvX = 0;
    public static int toolHolderInvY = 0;
    public static String[] toolStrArray = new String[]{};
    public static Set<String> toolNameSet = new HashSet<>();
    public static boolean enableDisplayToolHolderInventory = true;
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

        if (!item.getTagCompound().hasKey(Constants.NBT_KEY_ENCHANT, 9)) {
            item.getTagCompound().setTag("ench", new NBTTagList());
        }

        NBTTagList var3 = item.getTagCompound().getTagList(Constants.NBT_KEY_ENCHANT, 10);
        NBTTagCompound var4 = new NBTTagCompound();
        var4.setShort(Constants.NBT_KEY_ENCHANT_ID, (short) Enchantment.getEnchantmentID(enchantment));
        var4.setShort(Constants.NBT_KEY_ENCHANT_LEVEL, (short) (Lv));
        var3.appendTag(var4);
    }

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void preInit(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        enableDisplayToolHolderInventory = config.get(Configuration.CATEGORY_GENERAL, "enableDisplayToolHolderInventory", enableDisplayToolHolderInventory, "enable to display toolholder inventory in HUD").getBoolean();
        toolHolderInvX = config.get(Configuration.CATEGORY_GENERAL, "toolHolderInvX", toolHolderInvX, "ToolHolder Inventory x-position in HUD").getInt();
        toolHolderInvY = config.get(Configuration.CATEGORY_GENERAL, "toolHolderInvY", toolHolderInvY, "ToolHolder Inventory y-position in HUD").getInt();
        toolStrArray = config.get(Configuration.CATEGORY_GENERAL, "toolStrArray", toolStrArray, "Tool ids that can set ToolHolders.").getStringList();
        toolNameSet.addAll(Arrays.asList(toolStrArray));
        config.save();
        MinecraftForge.EVENT_BUS.register(this);
        proxy.registerClientPreInformation();
        MinecraftForge.EVENT_BUS.register(proxy);
        PacketHandler.init();
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(itemMultiToolHolder3);
        registry.register(itemMultiToolHolder5);
        registry.register(itemMultiToolHolder9);
        registry.register(itemMultiToolHolder7);
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        addRecipe(event.getRegistry());
    }

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void load(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
        proxy.registerClientInformation();
    }

    private void addRecipe(IForgeRegistry<IRecipe> registry) {
        ItemStack[] toolHolders = new ItemStack[]{new ItemStack(itemMultiToolHolder3), new ItemStack(itemMultiToolHolder5), new ItemStack(itemMultiToolHolder7), new ItemStack(itemMultiToolHolder9)};
        ItemStack[] holderMaterials = new ItemStack[]{new ItemStack(Items.IRON_INGOT), new ItemStack(Items.DYE, 1, 4), new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.DIAMOND)};
        ResourceLocation rl = new ResourceLocation("","");
        for (int i = 0; i < toolHolders.length; i++) {
            registry.register(new ShapedOreRecipe(rl, toolHolders[i],
                    "AAA",
                    "ABA",
                    "CCC",
                    'A', holderMaterials[i],
                    'B', new ItemStack(Blocks.CHEST),
                    'C', new ItemStack(Blocks.TRIPWIRE_HOOK)).setRegistryName(toolHolders[i].getItem().getRegistryName()));
        }
    }
}