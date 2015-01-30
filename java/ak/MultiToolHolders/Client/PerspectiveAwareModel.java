package ak.MultiToolHolders.Client;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.List;

/**
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
    public Pair<IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        if (cameraTransformType == ItemCameraTransforms.TransformType.GUI) {
            //GL返還命令がバイパスされるので、適用。
            RenderItem.applyVanillaTransform(this.guiModel.getItemCameraTransforms().field_178354_e);
            return Pair.of(this.guiModel, null);
        }
        //同上
        switch (cameraTransformType) {
            case FIRST_PERSON:
                RenderItem.applyVanillaTransform(this.handHeldModel.getItemCameraTransforms().field_178356_c);
                break;
            case HEAD:
                RenderItem.applyVanillaTransform(this.handHeldModel.getItemCameraTransforms().field_178353_d);
                break;
            case THIRD_PERSON:
                RenderItem.applyVanillaTransform(this.handHeldModel.getItemCameraTransforms().field_178355_b);
                break;
        }
        return Pair.of(this.handHeldModel, null);
    }

    @Override
    public List func_177551_a(EnumFacing facing) {
        return this.guiModel.func_177551_a(facing);
    }

    @Override
    public List func_177550_a() {
        return this.guiModel.func_177550_a();
    }

    @Override
    public boolean isGui3d() {
        return this.guiModel.isGui3d();
    }

    @Override
    public boolean isAmbientOcclusionEnabled() {
        return this.guiModel.isAmbientOcclusionEnabled();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return this.guiModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getTexture() {
        return this.guiModel.getTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return this.guiModel.getItemCameraTransforms();
    }
}
