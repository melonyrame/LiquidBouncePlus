package net.ccbluex.liquidbounce.features.module.modules.world;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;

@ModuleInfo(name = "Lightning", description = "check lightning spawn", category = ModuleCategory.WORLD)
public class Lightning extends Module {
    @EventTarget
    public void onPacket(PacketEvent event){
        if(event.getPacket() instanceof S2CPacketSpawnGlobalEntity && ((S2CPacketSpawnGlobalEntity) event.getPacket()).func_149053_g() == 1){
            S2CPacketSpawnGlobalEntity entity = ((S2CPacketSpawnGlobalEntity) event.getPacket());
            //ClientUtils.displayChatMessage("Detected lightning at X:" + entity.func_149051_d() + " Y:" + entity.func_149050_e() + " Z:" + entity.func_149049_f());
            LiquidBounce.hud.addNotification(new Notification("Detected lightning at X:" + entity.func_149051_d() + " Y:" + entity.func_149050_e() + " Z:" + entity.func_149049_f(), Notification.Type.WARNING, 3000L));
        }
    }
}