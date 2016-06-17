package ak.MultiToolHolders.Client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.List;

/**
 * GUIアイコンと手持ちアイコンの描画を変えるモデルクラス
 * Created by AKIRA on 15/01/30.
 */
public class PerspectiveAwareModel implements IPerspectiveAwareModel{
    //インベントリアイコン用モデル
    private IFlexibleBakedModel guiModel;
    //一人称・三人称視点用モデル
    private IFlexibleBakedModel handHeldModel;
    public PerspectiveAwareModel(IFlexibleBakedModel model1, IFlexibleBakedModel model2) {
        this.guiModel = model1;
        this.handHeldModel = model2;
    }

    @Override
    public Pair<? extends IFlexibleBakedModel, Matrix4f>  handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        IFlexibleBakedModel model = (cameraTransformType == ItemCameraTransforms.TransformType.GUI) ? this.guiModel: this.handHeldModel;
        model.getItemCameraTransforms().applyTransform(cameraTransformType);
        Pair<? extends IFlexibleBakedModel, Matrix4f> pair = Pair.of(model,
                ForgeHooksClient.getMatrix(model.getItemCameraTransforms().getTransform(cameraTransformType)));
        if (model instanceof IPerspectiveAwareModel) {
            pair = ((IPerspectiveAwareModel)model).handlePerspective(cameraTransformType);
        }
        return pair;
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing facing) {
        return this.guiModel.getFaceQuads(facing);
    }

    @Override
    public List<BakedQuad> getGeneralQuads() {
        return this.guiModel.getGeneralQuads();
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
    public TextureAtlasSprite getParticleTexture() {
        return this.guiModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return this.guiModel.getItemCameraTransforms();
    }

    @Override
    public VertexFormat getFormat() {
        return this.guiModel.getFormat();
    }
}
