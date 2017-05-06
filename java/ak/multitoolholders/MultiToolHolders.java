package ak.multitoolholders;

import ak.multitoolholders.network.PacketHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.config.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Mod(modid = Constants.MOD_ID, name = "multitoolholders", version = "@VERSION@", dependencies = "required-after:Forge@[10.12.1.1090,)", useMetadata = true)
public class MultiToolHolders {
    //Logger
    public static final Logger logger = Logger.getLogger(Constants.MOD_ID);
    public static Item itemMultiToolHolder3 = (new ItemMultiToolHolder(EnumHolderType.HOLDER3)).setUnlocalizedName(Constants.MOD_ID.toLowerCase() + ":Holder3").setTextureName(Constants.MOD_ID.toLowerCase() + ":Holder3");
    public static Item itemMultiToolHolder5 = (new ItemMultiToolHolder(EnumHolderType.HOLDER5)).setUnlocalizedName(Constants.MOD_ID.toLowerCase() + ":Holder5").setTextureName(Constants.MOD_ID.toLowerCase() + ":Holder5");
    public static Item itemMultiToolHolder7 = (new ItemMultiToolHolder(EnumHolderType.HOLDER7)).setUnlocalizedName(Constants.MOD_ID.toLowerCase() + ":Holder7").setTextureName(Constants.MOD_ID.toLowerCase() + ":Holder7");
    public static Item itemMultiToolHolder9 = (new ItemMultiToolHolder(EnumHolderType.HOLDER9)).setUnlocalizedName(Constants.MOD_ID.toLowerCase() + ":Holder9").setTextureName(Constants.MOD_ID.toLowerCase() + ":Holder9");
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
        if (item.stackTagCompound == null) {
            item.setTagCompound(new NBTTagCompound());
        }

        if (!item.stackTagCompound.hasKey(Constants.NBT_KEY_ENCHANT, net.minecraftforge.common.util.Constants.NBT.TAG_LIST)) {
            item.stackTagCompound.setTag(Constants.NBT_KEY_ENCHANT, new NBTTagList());
        }

        NBTTagList tagList = item.stackTagCompound.getTagList(Constants.NBT_KEY_ENCHANT, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.setShort(Constants.NBT_KEY_ENCHANT_ID, (short) enchantment.effectId);
        nbtTagCompound.setShort(Constants.NBT_KEY_ENCHANT_LEVEL, (short) (Lv));
        tagList.appendTag(nbtTagCompound);
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

        GameRegistry.registerItem(itemMultiToolHolder3, Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_3);
        GameRegistry.registerItem(itemMultiToolHolder5, Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_5);
        GameRegistry.registerItem(itemMultiToolHolder9, Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_9);
        GameRegistry.registerItem(itemMultiToolHolder7, Constants.REG_NAME_ITEM_MULTI_TOOL_HOLDER_7);

        PacketHandler.init();
    }

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void load(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

        ItemStack[] toolHolders = new ItemStack[]{new ItemStack(itemMultiToolHolder3), new ItemStack(itemMultiToolHolder5), new ItemStack(itemMultiToolHolder7), new ItemStack(itemMultiToolHolder9)};
        ItemStack[] holderMaterials = new ItemStack[]{new ItemStack(Items.iron_ingot), new ItemStack(Items.dye, 1, 4), new ItemStack(Items.gold_ingot), new ItemStack(Items.diamond)};
        for (int i = 0; i < toolHolders.length; i++) {
            GameRegistry.addRecipe(toolHolders[i],
                    "AAA",
                    "ABA",
                    "CCC",
                    'A', holderMaterials[i],
                    'B', Blocks.chest,
                    'C', Blocks.tripwire_hook);
        }
        proxy.registerClientInformation();
    }
}