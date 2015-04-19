package ak.MultiToolHolders.Client;

import ak.MultiToolHolders.IKeyEvent;
import ak.MultiToolHolders.ItemMultiToolHolder;
import ak.MultiToolHolders.network.MessageKeyPressed;
import ak.MultiToolHolders.network.PacketHandler;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;

/**
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
            if (keyIndex != -1 && entityPlayer.getCurrentEquippedItem() != null && entityPlayer.getCurrentEquippedItem().getItem() instanceof IKeyEvent) {
                if (entityPlayer.worldObj.isRemote) {
                    PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(keyIndex));
                } else {
                    ((IKeyEvent)entityPlayer.getCurrentEquippedItem().getItem()).doKeyAction(entityPlayer.getCurrentEquippedItem(), entityPlayer, keyIndex);
                }
            }
        }
    }
}
