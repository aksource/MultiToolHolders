package ak.mcmod.multitoolholders.client;

import ak.mcmod.multitoolholders.item.MultiToolHolderItem;
import com.google.common.collect.Maps;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * ツールホルダー内のアイテム描画モデルクラス Created by A.K. on 14/08/01.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HolderRenderer implements IBakedModel {

  private static final Map<IBakedModel, IBakedModel> PERSPECTIVE_AWARE_MODEL_MAP = Maps
          .newHashMap();
  private final IBakedModel defaultModel;
  private final ItemOverrideList holderItemOverrideList;

  public HolderRenderer(IBakedModel defaultModel) {
    this.defaultModel = defaultModel;
    this.holderItemOverrideList = new HolderItemOverrideList(defaultModel);
  }

  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                  Random rand) {
    return this.defaultModel.getQuads(state, side, rand);
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
  public ItemCameraTransforms getTransforms() {
    return defaultModel.getTransforms();
  }

  @Override
  public ItemOverrideList getOverrides() {
    return this.holderItemOverrideList;
  }

  @ParametersAreNonnullByDefault
  @MethodsReturnNonnullByDefault
  private static class HolderItemOverrideList extends ItemOverrideList {

    private final IBakedModel defaultModel;

    private HolderItemOverrideList(IBakedModel defaultModel) {
      super();
      this.defaultModel = defaultModel;
    }

    @Override
    public IBakedModel resolve(IBakedModel originalModel,
                               ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
      ItemStack itemStack = MultiToolHolderItem.getActiveItemStack(stack);
      if (!itemStack.isEmpty()) {
        IBakedModel itemStackOrgModel = Minecraft.getInstance().getItemRenderer()
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
