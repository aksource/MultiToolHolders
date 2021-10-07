package ak.mcmod.multitoolholders.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;

/**
 * GUIアイコンと手持ちアイコンの描画を変えるモデルクラス Created by A.K. on 15/01/30.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PerspectiveAwareModel implements IBakedModel {

  //インベントリアイコン用モデル
  private final IBakedModel guiModel;
  //一人称・三人称視点用モデル
  private final IBakedModel handHeldModel;

  PerspectiveAwareModel(IBakedModel model1, IBakedModel model2) {
    this.guiModel = model1;
    this.handHeldModel = model2;
  }

  @Override
  public IBakedModel handlePerspective(
          ItemCameraTransforms.TransformType cameraTransformType, MatrixStack matrixStack) {
    IBakedModel model =
            (cameraTransformType == ItemCameraTransforms.TransformType.GUI) ? this.guiModel
                    : this.handHeldModel;
    return model.handlePerspective(cameraTransformType, matrixStack);
  }

  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                  Random rand) {
    return this.guiModel.getQuads(state, side, rand);
  }

  @Override
  public boolean usesBlockLight() {
    return this.guiModel.usesBlockLight();
  }

  @Override
  public boolean isGui3d() {
    return this.guiModel.isGui3d();
  }

  @Override
  public boolean useAmbientOcclusion() {
    return this.guiModel.useAmbientOcclusion();
  }

  @Override
  public boolean isCustomRenderer() {
    return this.guiModel.isCustomRenderer();
  }

  @Override
  public TextureAtlasSprite getParticleIcon() {
    return this.guiModel.getParticleIcon();
  }

  @Override
  public ItemCameraTransforms getTransforms() {
    return this.guiModel.getTransforms();
  }

  @Override
  public ItemOverrideList getOverrides() {
    return this.guiModel.getOverrides();
  }
}
