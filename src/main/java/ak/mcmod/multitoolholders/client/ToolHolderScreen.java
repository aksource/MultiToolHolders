package ak.mcmod.multitoolholders.client;

import ak.mcmod.multitoolholders.inventory.ToolHolderContainer;
import ak.mcmod.multitoolholders.item.HolderType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ToolHolderScreen extends AbstractContainerScreen<ToolHolderContainer> {
  private static final int ROW_SIZE = 1;
  private final HolderType type;

  public ToolHolderScreen(ToolHolderContainer toolHolderContainer, Inventory inventory, Component component, HolderType type) {
    super(toolHolderContainer, inventory, component);
    this.type = type;
    this.imageHeight = 114 + ROW_SIZE * 18;
    this.inventoryLabelY = this.imageHeight - 94;
  }

  @Override
  public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(poseStack);
    super.render(poseStack, mouseX, mouseY, partialTicks);
    this.renderTooltip(poseStack, mouseX, mouseY);
  }

  @Override
  protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShaderTexture(0, type.getGuiFile());
    int i = (width - imageWidth) / 2;
    int j = (height - imageHeight) / 2;
    this.blit(poseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
  }
}
