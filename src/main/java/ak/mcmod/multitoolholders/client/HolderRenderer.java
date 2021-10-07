package ak.mcmod.multitoolholders.client;

import ak.mcmod.multitoolholders.item.ItemMultiToolHolder;
import com.google.common.collect.Maps;
import mcp.MethodsReturnNonnullByDefault;
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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;

/**
 * ツールホルダー内のアイテム描画モデルクラス
 * Created by A.K. on 14/08/01.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HolderRenderer implements IBakedModel {
  private final IBakedModel defaultModel;
  private final ItemOverrideList holderItemOverrideList;
  private static final Map<IBakedModel, IBakedModel> PERSPECTIVE_AWARE_MODEL_MAP = Maps.newHashMap();

  public HolderRenderer(IBakedModel defaultModel) {
    this.defaultModel = defaultModel;
    this.holderItemOverrideList = new HolderItemOverrideList(defaultModel);
  }

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
      super(defaultModel.getOverrides().getOverrides());
      this.defaultModel = defaultModel;
    }

    @Override
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
      ItemStack itemStack = ItemMultiToolHolder.getActiveItemStack(stack);
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
