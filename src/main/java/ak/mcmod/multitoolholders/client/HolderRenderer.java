package ak.mcmod.multitoolholders.client;

import ak.mcmod.multitoolholders.item.MultiToolHolderItem;
import com.google.common.collect.Maps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;

/**
 * ツールホルダー内のアイテム描画モデルクラス Created by A.K. on 14/08/01.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HolderRenderer implements BakedModel {

  private static final Map<BakedModel, BakedModel> PERSPECTIVE_AWARE_MODEL_MAP = Maps
          .newHashMap();
  private final BakedModel defaultModel;
  private final ItemOverrides holderItemOverrideList;

  public HolderRenderer(BakedModel defaultModel) {
    this.defaultModel = defaultModel;
    this.holderItemOverrideList = new HolderItemOverrideList(defaultModel);
  }

  @Override
  public boolean usesBlockLight() {
    return true;
  }

  @Override
  public boolean isGui3d() {
    return defaultModel.isGui3d();
  }

  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
    return this.defaultModel.getQuads(blockState, direction, randomSource);
  }

  @Override
  public boolean useAmbientOcclusion() {
    return defaultModel.useAmbientOcclusion();
  }

  @Override
  public boolean isCustomRenderer() {
    return defaultModel.isCustomRenderer();
  }

  @Override
  public TextureAtlasSprite getParticleIcon() {
    return defaultModel.getParticleIcon();
  }

  @Override
  public ItemTransforms getTransforms() {
    return defaultModel.getTransforms();
  }

  @Override
  public ItemOverrides getOverrides() {
    return this.holderItemOverrideList;
  }

  @ParametersAreNonnullByDefault
  @MethodsReturnNonnullByDefault
  private static class HolderItemOverrideList extends ItemOverrides {

    private final BakedModel defaultModel;

    private HolderItemOverrideList(BakedModel defaultModel) {
      super();
      this.defaultModel = defaultModel;
    }

    @Override
    public BakedModel resolve(BakedModel originalModel,
                              ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int p_173469_) {
      var itemStack = MultiToolHolderItem.getActiveItemStack(stack);
      if (!itemStack.isEmpty()) {
        var itemStackOrgModel = Minecraft.getInstance().getItemRenderer()
                .getItemModelShaper().getItemModel(itemStack);
        if (!PERSPECTIVE_AWARE_MODEL_MAP.containsKey(itemStackOrgModel)) {
          PERSPECTIVE_AWARE_MODEL_MAP
                  .put(itemStackOrgModel, new PerspectiveAwareModel(defaultModel, itemStackOrgModel));
        }
        return PERSPECTIVE_AWARE_MODEL_MAP.get(itemStackOrgModel);
      }
      return defaultModel;
    }
  }
}
