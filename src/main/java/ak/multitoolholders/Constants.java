package ak.multitoolholders;

import net.minecraft.util.ResourceLocation;

/**
 * 定数クラス
 * Created by A.K. on 2017/03/04.
 */
public class Constants {
    public static final String TEXTURES_GUI_TOOL_HOLDER3_PNG = "textures/gui/ToolHolder3.png";
    public static final String MOD_ID = "multitoolholders";
    public static final ResourceLocation GUI_3 = new ResourceLocation(MOD_ID.toLowerCase(), TEXTURES_GUI_TOOL_HOLDER3_PNG);
    public static final String TEXTURES_GUI_TOOL_HOLDER5_PNG = "textures/gui/toolholder5.png";
    public static final ResourceLocation GUI_5 = new ResourceLocation(MOD_ID.toLowerCase(), TEXTURES_GUI_TOOL_HOLDER5_PNG);
    public static final String TEXTURES_GUI_TOOL_HOLDER7_PNG = "textures/gui/toolholder7.png";
    public static final ResourceLocation GUI_7 = new ResourceLocation(MOD_ID.toLowerCase(), TEXTURES_GUI_TOOL_HOLDER7_PNG);
    public static final String TEXTURES_GUI_TOOL_HOLDER9_PNG = "textures/gui/toolholder9.png";
    public static final ResourceLocation GUI_9 = new ResourceLocation(MOD_ID.toLowerCase(), TEXTURES_GUI_TOOL_HOLDER9_PNG);
    public static final String MOD_NAME = "MultiToolHolders";
    public static final String MOD_VERSION = "@VERSION@";
    public static final String MOD_DEPENDENCIES = "required-after:forge@[13.19.1,)";
    public static final String MOD_MC_VERSION = "[1.11,1.19.99]";
    public static final String NBT_KEY_MTH = "MultiToolHolders";
    public static final String NBT_KEY_SLOT = "slot";
    public static final String NBT_KEY_INCLUDE_MTH = "include_mth";
    public static final int GUI_ID_HOLDER_3 = 0;
    public static final int GUI_ID_HOLDER_5 = 1;
    public static final int GUI_ID_HOLDER_9 = 2;
    public static final int GUI_ID_HOLDER_7 = 3;
    public static final String REG_NAME_ITEM_MULTI_TOOL_HOLDER_3 = "itemmultitoolholder3";
    public static final String REG_NAME_ITEM_MULTI_TOOL_HOLDER_5 = "itemmultitoolholder5";
    public static final String REG_NAME_ITEM_MULTI_TOOL_HOLDER_9 = "itemmultitoolholder9";
    public static final String REG_NAME_ITEM_MULTI_TOOL_HOLDER_7 = "itemmultitoolholder7";
    public static final String NBT_KEY_ENCHANT = "ench";
    public static final String NBT_KEY_ENCHANT_ID = "id";
    public static final String NBT_KEY_ENCHANT_LEVEL = "lvl";

    public static final ResourceLocation ADVANCEMENT_RECIPE_ROOT = new ResourceLocation("minecraft", ":recipes/root");
}
