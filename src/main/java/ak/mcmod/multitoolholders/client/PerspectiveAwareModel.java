package ak.mcmod.multitoolholders.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * GUIアイコンと手持ちアイコンの描画を変えるモデルクラス Created by A.K. on 15/01/30.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PerspectiveAwareModel implements BakedModel {

  //インベントリアイコン用モデル
  private final BakedModel guiModel;
  //一人称・三人称視点用モデル
  private final BakedModel handHeldModel;

  PerspectiveAwareModel(BakedModel model1, BakedModel model2) {
    this.guiModel = model1;
    this.handHeldModel = model2;
  }

  @Override
  public BakedModel handlePerspective(
          ItemTransforms.TransformType cameraTransformType, PoseStack poseStack) {
    var model =
            (cameraTransformType == ItemTransforms.TransformType.GUI) ? this.guiModel
                    : this.handHeldModel;
    return model.handlePerspective(cameraTransformType, poseStack);
  }

  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
    return this.guiModel.getQuads(blockState, direction, randomSource);
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
  public ItemTransforms getTransforms() {
    return this.guiModel.getTransforms();
  }

  @Override
  public ItemOverrides getOverrides() {
    return this.guiModel.getOverrides();
  }
}
