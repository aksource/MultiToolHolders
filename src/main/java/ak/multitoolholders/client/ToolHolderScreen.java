package ak.multitoolholders.client;

import ak.multitoolholders.inventory.ToolHolderContainer;
import ak.multitoolholders.item.HolderType;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ToolHolderScreen extends ContainerScreen<ToolHolderContainer> {
  private static final int ROW_SIZE = 1;
  private final HolderType type;

  public ToolHolderScreen(ToolHolderContainer toolHolderContainer, PlayerInventory playerInventory, ITextComponent textComponent, HolderType type) {
    super(toolHolderContainer, playerInventory, textComponent);
    this.type = type;
    this.ySize = 114 + ROW_SIZE * 18;
    this.playerInventoryTitleY = this.ySize - 94;
  }

  @Override
  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(matrixStack);
    super.render(matrixStack, mouseX, mouseY, partialTicks);
    this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.getMinecraft().getTextureManager().bindTexture(type.getGuiFile());
    int i = (width - xSize) / 2;
    int j = (height - ySize) / 2;
    this.blit(matrixStack, i, j, 0, 0, this.xSize, this.ySize);
  }
}
