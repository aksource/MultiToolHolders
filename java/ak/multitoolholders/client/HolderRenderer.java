package ak.multitoolholders.client;

import ak.multitoolholders.ItemMultiToolHolder;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;

/**
 * ツールホルダー内のアイテム描画モデルクラス
 * Created by A.K. on 14/08/01.
 */
@SideOnly(Side.CLIENT)
public class HolderRenderer implements ISmartItemModel {
    private Minecraft mc = Minecraft.getMinecraft();
    private final IFlexibleBakedModel defaultModel;
    private static final Map<IBakedModel, IPerspectiveAwareModel> PERSPECTIVE_AWARE_MODEL_MAP = Maps.newHashMap();

    public HolderRenderer(IFlexibleBakedModel defaultModel) {
        this.defaultModel = defaultModel;
    }
    @Override
    public IBakedModel handleItemState(ItemStack stack) {
        ItemStack itemStack = ((ItemMultiToolHolder)stack.getItem()).getActiveItemStack(stack);
        if (itemStack != null) {
            IBakedModel itemStackOrgModel = mc.getRenderItem().getItemModelMesher().getItemModel(itemStack);
            if (!PERSPECTIVE_AWARE_MODEL_MAP.containsKey(itemStackOrgModel)) {
                IFlexibleBakedModel itemStackFlexModel = (itemStackOrgModel instanceof IFlexibleBakedModel) ?
                        (IFlexibleBakedModel) itemStackOrgModel: new IFlexibleBakedModel.Wrapper(itemStackOrgModel, null);
                PERSPECTIVE_AWARE_MODEL_MAP.put(itemStackOrgModel, new PerspectiveAwareModel(defaultModel, itemStackFlexModel));
            }
            return PERSPECTIVE_AWARE_MODEL_MAP.get(itemStackOrgModel);
        }
        return defaultModel;
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_) {
        return defaultModel.getFaceQuads(p_177551_1_);
    }

    @Override
    public List<BakedQuad> getGeneralQuads() {
        return defaultModel.getGeneralQuads();
    }

    @Override
    public boolean isGui3d() {
        return defaultModel.isGui3d();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return defaultModel.isAmbientOcclusion();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return defaultModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return defaultModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return defaultModel.getItemCameraTransforms();
    }
}
