package ak.MultiToolHolders.Client;

import ak.MultiToolHolders.ItemMultiToolHolder;
import ak.MultiToolHolders.inventory.InventoryToolHolder;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED;

/**
 * Created by A.K. on 14/08/01.
 */
public class HolderRenderer implements IItemRenderer {
    @SideOnly(Side.CLIENT)
    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        InventoryToolHolder tools = ((ItemMultiToolHolder)item.getItem()).getInventoryFromItemStack(item);
        int SlotNum = ItemMultiToolHolder.getSlotNumFromItemStack(item);
        if (tools != null && tools.getStackInSlot(SlotNum) != null) {
            IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(tools.getStackInSlot(SlotNum),
                    EQUIPPED);
            if (customRenderer != null)
                return customRenderer.shouldUseRenderHelper(type, tools.getStackInSlot(SlotNum), helper);
            else
                return helper == ItemRendererHelper.EQUIPPED_BLOCK;
        } else
            return helper == ItemRendererHelper.EQUIPPED_BLOCK;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        InventoryToolHolder tools = ((ItemMultiToolHolder)item.getItem()).getInventoryFromItemStack(item);
        int SlotNum = ItemMultiToolHolder.getSlotNumFromItemStack(item);
        if (tools != null && tools.getStackInSlot(SlotNum) != null) {
            IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(tools.getStackInSlot(SlotNum),
                    EQUIPPED);
            if (customRenderer != null)
                customRenderer.renderItem(type, tools.getStackInSlot(SlotNum), data);
            else {
                ItemStack itemInHolder = tools.getStackInSlot(SlotNum);
                if (itemInHolder.getItem().requiresMultipleRenderPasses()) {
                    for (int pass = 0; pass < itemInHolder.getItem().getRenderPasses(itemInHolder.getItemDamage()); pass++) {
                        int color = itemInHolder.getItem().getColorFromItemStack(itemInHolder, pass);
                        float colorR = (float)(color >> 16 & 255) / 255.0F;
                        float colorG = (float)(color >> 8 & 255) / 255.0F;
                        float colorB = (float)(color & 255) / 255.0F;
                        GL11.glColor4f(1.0F * colorR, 1.0F * colorG, 1.0F * colorB, 1.0F);
                        renderToolHolder((EntityLivingBase) data[1], tools.getStackInSlot(SlotNum), pass);
                    }
                } else {
                    renderToolHolder((EntityLivingBase) data[1], tools.getStackInSlot(SlotNum), 0);
                }
            }
        } else {
            renderToolHolder((EntityLivingBase) data[1], item, 0);
        }
    }

    @SideOnly(Side.CLIENT)
    public void renderToolHolder(EntityLivingBase entity, ItemStack stack, int pass)
    {
        GL11.glPushMatrix();
        Minecraft mc = Minecraft.getMinecraft();
        TextureManager texturemanager = mc.getTextureManager();
        IIcon icon = entity.getItemIcon(stack, pass);
        if (icon == null) {
            GL11.glPopMatrix();
            return;
        }

        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        texturemanager.bindTexture(texturemanager.getResourceLocation(stack.getItemSpriteNumber()));
        TextureUtil.func_152777_a(false, false, 1.0F);
        Tessellator tessellator = Tessellator.instance;
        float f = icon.getMinU();
        float f1 = icon.getMaxU();
        float f2 = icon.getMinV();
        float f3 = icon.getMaxV();
        float f4 = 0.0F;
        float f5 = 0.3F;
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef(-f4, -f5, 0.0F);
        float f6 = 1.5F;
        GL11.glScalef(f6, f6, f6);
        GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
        ItemRenderer.renderItemIn2D(tessellator, f1, f2, f, f3, icon.getIconWidth(),
                icon.getIconHeight(), 0.0625F);

        if (stack.hasEffect(pass)) {
            GL11.glDepthFunc(GL11.GL_EQUAL);
            GL11.glDisable(GL11.GL_LIGHTING);
            texturemanager.bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
            float f7 = 0.76F;
            GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glPushMatrix();
            float f8 = 0.125F;
            GL11.glScalef(f8, f8, f8);
            float f9 = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
            GL11.glTranslatef(f9, 0.0F, 0.0F);
            GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
            ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glScalef(f8, f8, f8);
            f9 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
            GL11.glTranslatef(-f9, 0.0F, 0.0F);
            GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
            ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
        }
        GL11.glPopMatrix();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

}
