package ak.multitoolholders.client;

import ak.multitoolholders.ItemMultiToolHolder;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * ツールホルダー内のアイテム描画モデルクラス
 * Created by A.K. on 14/08/01.
 */
@SideOnly(Side.CLIENT)
public class HolderRenderer implements IBakedModel {
    private final IBakedModel defaultModel;
    private final ItemOverrideList holderItemOverrideList;
    private static final Map<IBakedModel, IBakedModel> PERSPECTIVE_AWARE_MODEL_MAP = Maps.newHashMap();

    public HolderRenderer(IBakedModel defaultModel) {
        this.defaultModel = defaultModel;
        this.holderItemOverrideList = new HolderItemOverrideList(defaultModel);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return this.defaultModel.getQuads(state, side, rand);
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

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return defaultModel.getParticleTexture();
    }

    @Nonnull
    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return defaultModel.getItemCameraTransforms();
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return this.holderItemOverrideList;
    }

    private static class HolderItemOverrideList extends ItemOverrideList {
        private final IBakedModel defaultModel;

        private HolderItemOverrideList(IBakedModel defaultModel) {
            super(defaultModel.getOverrides().getOverrides());
            this.defaultModel = defaultModel;
        }

        @Nonnull
        @Override
        public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, @Nonnull ItemStack stack, World world, EntityLivingBase entity) {
            ItemStack itemStack = ((ItemMultiToolHolder) stack.getItem()).getActiveItemStack(stack);
            if (!itemStack.isEmpty()) {
                IBakedModel itemStackOrgModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(itemStack);
                if (!PERSPECTIVE_AWARE_MODEL_MAP.containsKey(itemStackOrgModel)) {
                    PERSPECTIVE_AWARE_MODEL_MAP.put(itemStackOrgModel, new PerspectiveAwareModel(defaultModel, itemStackOrgModel));
                }
                return PERSPECTIVE_AWARE_MODEL_MAP.get(itemStackOrgModel);
            }
            return defaultModel;
        }
    }
}
