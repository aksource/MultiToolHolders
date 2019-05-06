package ak.multitoolholders.client;

import ak.multitoolholders.EnumHolderType;
import ak.multitoolholders.inventory.ContainerToolHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiToolHolder extends GuiContainer {

  private final EnumHolderType type;

  public GuiToolHolder(EntityPlayer entityPlayer, ItemStack holderStack, EnumHolderType type,
      int currentSlot) {
    super(new ContainerToolHolder(entityPlayer, holderStack, type, currentSlot));
    this.type = type;
  }

  @Override
  public void render(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
    this.drawDefaultBackground();
    super.render(p_73863_1_, p_73863_2_, p_73863_3_);
    this.renderHoveredToolTip(p_73863_1_, p_73863_2_);

  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    fontRenderer.drawString(I18n.format("container.toolholder"), 8, 6, 4210752);
    fontRenderer.drawString(I18n.format("container.inventory"), 8, 40, 4210752);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getInstance().getTextureManager().bindTexture(type.getGuiFile());
    int x = (width - xSize) / 2;
    int y = (height - ySize) / 2;
    this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
  }
}
