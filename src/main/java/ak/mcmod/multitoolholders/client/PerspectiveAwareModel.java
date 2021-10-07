package ak.mcmod.multitoolholders.client;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.vecmath.Matrix4f;
import java.util.List;

/**
 * GUIアイコンと手持ちアイコンの描画を変えるモデルクラス
 * Created by AKIRA on 15/01/30.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PerspectiveAwareModel implements IBakedModel {
  //インベントリアイコン用モデル
  private final IBakedModel guiModel;
  //一人称・三人称視点用モデル
  private final IBakedModel handHeldModel;

  public PerspectiveAwareModel(IBakedModel model1, IBakedModel model2) {
    this.guiModel = model1;
    this.handHeldModel = model2;
  }

  @Override
  public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
    IBakedModel model = (cameraTransformType == ItemCameraTransforms.TransformType.GUI) ? this.guiModel : this.handHeldModel;
    model.getItemCameraTransforms().applyTransform(cameraTransformType);
    return model.handlePerspective(cameraTransformType);
  }

  @Override
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
  public TextureAtlasSprite getParticleTexture() {
    return this.guiModel.getParticleTexture();
  }

  @Override
  public ItemCameraTransforms getItemCameraTransforms() {
    return this.guiModel.getItemCameraTransforms();
  }

  @Override
  public ItemOverrideList getOverrides() {
    return this.guiModel.getOverrides();
  }
}
