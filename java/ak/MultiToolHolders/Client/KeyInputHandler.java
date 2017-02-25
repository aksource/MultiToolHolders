package ak.MultiToolHolders.Client;

import ak.MultiToolHolders.IKeyEvent;
import ak.MultiToolHolders.ItemMultiToolHolder;
import ak.MultiToolHolders.network.MessageKeyPressed;
import ak.MultiToolHolders.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * キー入力を検知するクラス
 * Created by A.K. on 14/05/28.
 */
@SideOnly(Side.CLIENT)
public class KeyInputHandler {

    private byte getKeyIndex() {
        byte key = -1;
        if (ClientProxy.OpenKey.isPressed()) {
            key = ItemMultiToolHolder.OPEN_KEY;
        } else if (ClientProxy.NextKey.isPressed()) {
            key = ItemMultiToolHolder.NEXT_KEY;
        } else if (ClientProxy.PrevKey.isPressed()) {
            key = ItemMultiToolHolder.PREV_KEY;
        }
        return key;
    }

    @SubscribeEvent
    public void KeyPressEvent(InputEvent.KeyInputEvent event) {
        if (FMLClientHandler.instance().getClient().inGameHasFocus && FMLClientHandler.instance().getClientPlayerEntity() != null) {
            EntityPlayer entityPlayer = FMLClientHandler.instance().getClientPlayerEntity();
            byte keyIndex = getKeyIndex();
            if (keyIndex != -1 && !entityPlayer.getHeldItemMainhand().isEmpty() && entityPlayer.getHeldItemMainhand().getItem() instanceof IKeyEvent) {
                if (entityPlayer.getEntityWorld().isRemote) {
                    PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(keyIndex));
                } else {
                    ((IKeyEvent)entityPlayer.getHeldItemMainhand().getItem()).doKeyAction(entityPlayer.getHeldItemMainhand(), entityPlayer, keyIndex);
                }
            }
        }
    }
}
