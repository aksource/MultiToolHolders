package ak.MultiToolHolders;

import ak.MultiToolHolders.network.PacketHandler;
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

import java.util.logging.Logger;

@Mod(modid = MultiToolHolders.MOD_ID,
        name = MultiToolHolders.MOD_NAME,
        version = MultiToolHolders.MOD_VERSION,
        dependencies = MultiToolHolders.MOD_DEPENDENCIES,
        useMetadata = true,
        acceptedMinecraftVersions = MultiToolHolders.MOD_MC_VERSION)
public class MultiToolHolders {
    public static final String MOD_ID = "MultiToolHolders";
    public static final String MOD_NAME = "MultiToolHolders";
    public static final String MOD_VERSION = "@VERSION@";
    public static final String MOD_DEPENDENCIES = "required-after:Forge@[12.17.0,)";
    public static final String MOD_MC_VERSION = "[1.9,1.10.99]";
    public static Item ItemMultiToolHolder3;
    public static Item ItemMultiToolHolder5;
    public static Item ItemMultiToolHolder7;
    public static Item ItemMultiToolHolder9;

    public static int toolHolderInvX = 0;
    public static int toolHolderInvY = 0;
    public static boolean enableDisplayToolHolderInventory = true;

    public static final String GuiToolHolder3 = "textures/gui/ToolHolder3.png";
    public static final String GuiToolHolder5 = "textures/gui/ToolHolder5.png";
    public static final String GuiToolHolder7 = "textures/gui/ToolHolder7.png";
    public static final String GuiToolHolder9 = "textures/gui/ToolHolder9.png";
    public static final String TextureDomain = "multitoolholders:";
    public static final String Assets = "multitoolholders";

    public static final String NBT_KEY_MTH = "multitoolholders";
    public static final String NBT_KEY_SLOT = "slot";


    @Mod.Instance("MultiToolHolders")
    public static MultiToolHolders instance;
    @SidedProxy(clientSide = "ak.MultiToolHolders.Client.ClientProxy", serverSide = "ak.MultiToolHolders.CommonProxy")
    public static CommonProxy proxy;
    public static final int guiIdHolder3 = 0;
    public static final int guiIdHolder5 = 1;
    public static final int guiIdHolder9 = 2;
    public static final int guiIdHolder7 = 3;

    //Logger
    public static final Logger logger = Logger.getLogger("MultiToolHolders");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        enableDisplayToolHolderInventory = config.get(Configuration.CATEGORY_GENERAL, "enableDisplayToolHolderInventory", enableDisplayToolHolderInventory, "enable to display toolholder inventory in HUD").getBoolean();
        toolHolderInvX = config.get(Configuration.CATEGORY_GENERAL, "toolHolderInvX", toolHolderInvX, "ToolHolder Inventory x-position in HUD").getInt();
        toolHolderInvY = config.get(Configuration.CATEGORY_GENERAL, "toolHolderInvY", toolHolderInvY, "ToolHolder Inventory y-position in HUD").getInt();

        config.save();

        ItemMultiToolHolder3 = (new ItemMultiToolHolder(3, guiIdHolder3)).setUnlocalizedName(TextureDomain + "Holder3")/*.setTextureName(TextureDomain + "Holder3")*/;
        GameRegistry.registerItem(ItemMultiToolHolder3, "itemmultitoolholder3");
        ItemMultiToolHolder5 = (new ItemMultiToolHolder(5, guiIdHolder5)).setUnlocalizedName(TextureDomain + "Holder5")/*.setTextureName(TextureDomain + "Holder5")*/;
        GameRegistry.registerItem(ItemMultiToolHolder5, "itemmultitoolholder5");
        ItemMultiToolHolder9 = (new ItemMultiToolHolder(9, guiIdHolder9)).setUnlocalizedName(TextureDomain + "Holder9")/*.setTextureName(TextureDomain + "Holder9")*/;
        GameRegistry.registerItem(ItemMultiToolHolder9, "itemmultitoolholder9");
        ItemMultiToolHolder7 = (new ItemMultiToolHolder(7, guiIdHolder7)).setUnlocalizedName(TextureDomain + "Holder7")/*.setTextureName(TextureDomain + "Holder7")*/;
        GameRegistry.registerItem(ItemMultiToolHolder7, "itemmultitoolholder7");

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

    public static void addEnchantmentToItem(ItemStack item,
                                            Enchantment enchantment, int Lv) {
        if (item == null || enchantment == null || Lv < 0) {
            return;
        }
        if (item.hasTagCompound()) {
            item.setTagCompound(new NBTTagCompound());
        }

        if (!item.getTagCompound().hasKey("ench", 9)) {
            item.getTagCompound().setTag("ench", new NBTTagList());
        }

        NBTTagList var3 = item.getTagCompound().getTagList("ench", 10);
        NBTTagCompound var4 = new NBTTagCompound();
        var4.setShort("id", (short) Enchantment.getEnchantmentID(enchantment));
        var4.setShort("lvl", (short) (Lv));
        var3.appendTag(var4);
    }
}