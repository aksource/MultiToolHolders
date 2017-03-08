package ak.MultiToolHolders.Client;

import ak.MultiToolHolders.Constants;
import ak.MultiToolHolders.inventory.ContainerToolHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiToolHolder extends GuiContainer {
    private int Num;

    public GuiToolHolder(EntityPlayer entityPlayer, ItemStack holderStack, int num, int currentSlot) {
        super(new ContainerToolHolder(entityPlayer, holderStack, num, currentSlot));
        this.Num = num;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        //draw text and stuff here
        //the parameters for drawString are: string, x, y, color
        fontRendererObj.drawString(I18n.translateToLocal("container.toolholder"), 8, 6, 4210752);
        //draws "Inventory" or your regional equivalent
        fontRendererObj.drawString(I18n.translateToLocal("container.inventory"), 8, 40, 4210752);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        ResourceLocation res;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.Num == 3)
            res = Constants.GUI_3;
        else if (this.Num == 5)
            res = Constants.GUI_5;
        else if (this.Num == 7)
            res = Constants.GUI_7;
        else
            res = Constants.GUI_9;
        Minecraft.getMinecraft().getTextureManager().bindTexture(res);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }
}
