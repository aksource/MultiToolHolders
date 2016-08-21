package ak.MultiToolHolders.Client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.List;

/**
 * GUIアイコンと手持ちアイコンの描画を変えるモデルクラス
 * Created by AKIRA on 15/01/30.
 */
public class PerspectiveAwareModel implements IPerspectiveAwareModel{
    //インベントリアイコン用モデル
    private IBakedModel guiModel;
    //一人称・三人称視点用モデル
    private IBakedModel handHeldModel;
    public PerspectiveAwareModel(IBakedModel model1, IBakedModel model2) {
        this.guiModel = model1;
        this.handHeldModel = model2;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f>  handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        IBakedModel model = (cameraTransformType == ItemCameraTransforms.TransformType.GUI) ? this.guiModel: this.handHeldModel;
        model.getItemCameraTransforms().applyTransform(cameraTransformType);
        Pair<? extends IBakedModel, Matrix4f> pair = Pair.of(model,
                ForgeHooksClient.getMatrix(model.getItemCameraTransforms().getTransform(cameraTransformType)));
        if (model instanceof IPerspectiveAwareModel) {
            pair = ((IPerspectiveAwareModel)model).handlePerspective(cameraTransformType);
        }
        return pair;
    }

    @Override
    @Nonnull
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return this.guiModel.getQuads(state, side, rand);
    }

    @Override
    public boolean isGui3d() {
        return this.guiModel.isGui3d();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return this.guiModel.isAmbientOcclusion();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return this.guiModel.isBuiltInRenderer();
    }

    @Override
    @Nonnull
    public TextureAtlasSprite getParticleTexture() {
        return this.guiModel.getParticleTexture();
    }

    @Override
    @Nonnull
    public ItemCameraTransforms getItemCameraTransforms() {
        return this.guiModel.getItemCameraTransforms();
    }

    @Override
    @Nonnull
    public ItemOverrideList getOverrides() {
        return this.guiModel.getOverrides();
    }
}
