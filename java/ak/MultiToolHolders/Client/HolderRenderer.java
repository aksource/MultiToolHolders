package ak.MultiToolHolders.Client;

import ak.MultiToolHolders.ItemMultiToolHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Created by A.K. on 14/08/01.
 */
@SideOnly(Side.CLIENT)
public class HolderRenderer implements ISmartItemModel {
    private Minecraft mc = Minecraft.getMinecraft();
    private IBakedModel defaultModel;

    public HolderRenderer(IBakedModel defaultModel) {
        this.defaultModel = defaultModel;
    }
    @Override
    public IBakedModel handleItemState(ItemStack stack) {
        ItemStack itemStack = ((ItemMultiToolHolder)stack.getItem()).getActiveItemStack(stack);
        if (itemStack != null) {
            return mc.getRenderItem().getItemModelMesher().getItemModel(itemStack);
        }
        return defaultModel;
    }

    @Override
    public List func_177551_a(EnumFacing p_177551_1_) {
        return defaultModel.func_177551_a(p_177551_1_);
    }

    @Override
    public List func_177550_a() {
        return defaultModel.func_177550_a();
    }

    @Override
    public boolean isGui3d() {
        return defaultModel.isGui3d();
    }

    @Override
    public boolean isAmbientOcclusionEnabled() {
        return defaultModel.isAmbientOcclusionEnabled();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return defaultModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getTexture() {
        return defaultModel.getTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return defaultModel.getItemCameraTransforms();
    }
}
