package ak.multitoolholders;


import net.minecraft.util.ResourceLocation;

/**
 * ツールホルダータイプ
 * Created by A.K. on 2017/05/06.
 */
public enum EnumHolderType {
    HOLDER3(3, Constants.GUI_3, Constants.GUI_ID_HOLDER_3),
    HOLDER5(5, Constants.GUI_5, Constants.GUI_ID_HOLDER_5),
    HOLDER7(7, Constants.GUI_7, Constants.GUI_ID_HOLDER_7),
    HOLDER9(9, Constants.GUI_9, Constants.GUI_ID_HOLDER_9);
    private final int size;
    private final ResourceLocation guiFile;
    private final int guiId;

    EnumHolderType(int size, ResourceLocation guiFile, int guiId) {
        this.size = size;
        this.guiFile = guiFile;
        this.guiId = guiId;
    }

    public int getSize() {
        return this.size;
    }

    public ResourceLocation getGuiFile() {
        return this.guiFile;
    }

    public int getGuiId() {
        return this.guiId;
    }
}
