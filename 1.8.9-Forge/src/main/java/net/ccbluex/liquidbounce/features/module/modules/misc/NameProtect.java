/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.TextEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.file.configs.FriendsConfig;
import net.ccbluex.liquidbounce.utils.misc.StringUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.TextValue;
import net.minecraft.client.network.NetworkPlayerInfo;

@ModuleInfo(name = "NameProtect", description = "Changes playernames clientside.", category = ModuleCategory.MISC)
public class NameProtect extends Module {

    private final TextValue fakeNameValue = new TextValue("FakeName", "&cMe");
    private final TextValue allFakeNameValue = new TextValue("AllPlayersFakeName", "Censored");
    public final BoolValue selfValue = new BoolValue("Yourself", false);
    public final BoolValue allPlayersValue = new BoolValue("AllPlayers", false);
    public final BoolValue skinProtectValue = new BoolValue("SkinProtect", true);

    @EventTarget(ignoreCondition = true)
    public void onText(final TextEvent event) {
        if(mc.thePlayer == null || event.getText().contains("§8[§9§l" + LiquidBounce.CLIENT_NAME + "§8] §3") || event.getText().startsWith("/") || event.getText().startsWith(LiquidBounce.commandManager.getPrefix()+""))
            return;

        if(!getState())
            return;

        for (final FriendsConfig.Friend friend : LiquidBounce.fileManager.friendsConfig.getFriends())
            event.setText(StringUtils.replace(event.getText(), friend.getPlayerName(), ColorUtils.translateAlternateColorCodes(friend.getAlias()) + "§f"));

        event.setText(StringUtils.replace(event.getText(), mc.thePlayer.getName(), (selfValue.get() ? ColorUtils.translateAlternateColorCodes(fakeNameValue.get()) + "§r" : mc.thePlayer.getName())));

        if(allPlayersValue.get())
            for(final NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap())
                event.setText(StringUtils.replace(event.getText(), playerInfo.getGameProfile().getName(), ColorUtils.translateAlternateColorCodes(allFakeNameValue.get()) + "§f"));
    }

}
