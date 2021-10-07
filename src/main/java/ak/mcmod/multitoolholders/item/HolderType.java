package ak.mcmod.multitoolholders.item;

import ak.mcmod.multitoolholders.Constants;
import net.minecraft.util.ResourceLocation;

/**
 * ツールホルダータイプ Created by A.K. on 2017/05/06.
 */
public enum HolderType {
  HOLDER3(3, Constants.GUI_3),
  HOLDER5(5, Constants.GUI_5),
  HOLDER7(7, Constants.GUI_7),
  HOLDER9(9, Constants.GUI_9);
  private final int size;
  private final ResourceLocation guiFile;

  HolderType(int size, ResourceLocation guiFile) {
    this.size = size;
    this.guiFile = guiFile;
  }

  public int getSize() {
    return this.size;
  }

  public ResourceLocation getGuiFile() {
    return this.guiFile;
  }
}
