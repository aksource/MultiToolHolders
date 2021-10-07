package ak.mcmod.multitoolholders.client;

import ak.mcmod.multitoolholders.inventory.ContainerToolHolder;
import ak.mcmod.multitoolholders.item.HolderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

public class GuiToolHolder extends GuiContainer {
  private final HolderType type;

  public GuiToolHolder(EntityPlayer entityPlayer, ItemStack holderStack, HolderType type, int currentSlot) {
    super(new ContainerToolHolder(entityPlayer, holderStack, type, currentSlot));
    this.type = type;
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    fontRenderer.drawString(I18n.translateToLocal("multitoolholders.container.toolholder"), 8, 6, 4210752);
    fontRenderer.drawString(I18n.translateToLocal("container.inventory"), 8, 40, 4210752);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getMinecraft().getTextureManager().bindTexture(type.getGuiFile());
    int x = (width - xSize) / 2;
    int y = (height - ySize) / 2;
    this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
  }
}
