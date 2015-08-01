package ak.MultiToolHolders.Client;

import ak.MultiToolHolders.MultiToolHolders;
import ak.MultiToolHolders.inventory.ContainerToolHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiToolHolder extends GuiContainer
{
	private int Num;
	private static final ResourceLocation gui3 = new ResourceLocation(MultiToolHolders.Assets, MultiToolHolders.GuiToolHolder3);
	private static final ResourceLocation gui5 = new ResourceLocation(MultiToolHolders.Assets, MultiToolHolders.GuiToolHolder5);
	private static final ResourceLocation gui7 = new ResourceLocation(MultiToolHolders.Assets, MultiToolHolders.GuiToolHolder7);
	private static final ResourceLocation gui9 = new ResourceLocation(MultiToolHolders.Assets, MultiToolHolders.GuiToolHolder9);
	public GuiToolHolder(EntityPlayer entityPlayer, ItemStack holderStack, int num)
	{
		super(new ContainerToolHolder(entityPlayer, holderStack, num));
		this.Num = num;
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		//draw text and stuff here
		//the parameters for drawString are: string, x, y, color
		fontRendererObj.drawString(StatCollector.translateToLocal("container.toolholder"), 8, 6, 4210752);
		//draws "Inventory" or your regional equivalent
		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, 40, 4210752);
	}

	/**
	 * Draw the background layer for the GuiContainer (everything behind the items)
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		ResourceLocation res;
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		if(this.Num == 3)
			res = gui3;
		else if(this.Num == 5)
			res = gui5;
		else if(this.Num == 7)
			res = gui7;
		else
			res = gui9;
		Minecraft.getMinecraft().getTextureManager().bindTexture(res);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}
}
