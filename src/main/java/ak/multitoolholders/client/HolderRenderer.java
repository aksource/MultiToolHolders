package ak.multitoolholders.client;

import ak.multitoolholders.item.MultiToolHolderItem;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * ツールホルダー内のアイテム描画モデルクラス Created by A.K. on 14/08/01.
 */
@OnlyIn(Dist.CLIENT)
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
  public boolean isSideLit() {
    return true;
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
    public IBakedModel getOverrideModel(IBakedModel originalModel,
                                        ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
      ItemStack itemStack = ((MultiToolHolderItem) stack.getItem()).getActiveItemStack(stack);
      if (!itemStack.isEmpty()) {
        IBakedModel itemStackOrgModel = Minecraft.getInstance().getItemRenderer()
            .getItemModelMesher().getItemModel(itemStack);
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
