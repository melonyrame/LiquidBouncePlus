package net.ccbluex.liquidbounce.features.module.modules.player;

import net.ccbluex.liquidbounce.features.module.*;
import net.ccbluex.liquidbounce.value.*;
import net.ccbluex.liquidbounce.utils.timer.*;
import java.util.*;
import net.minecraft.util.*;
import net.minecraft.network.play.server.*;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.utils.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.potion.*;
import net.minecraft.network.*;
import net.minecraft.item.*;
import net.minecraft.network.play.client.*;
import org.apache.commons.lang3.tuple.*;
import net.minecraft.init.*;

@ModuleInfo(name = "Heal", description = "Auto Eating GApple (felt bad skidding this)", category = ModuleCategory.PLAYER)
public class Heal extends Module
{
    private final FloatValue percent;
    private final IntegerValue min;
    private final IntegerValue max;
    private final FloatValue regenSec;
    private final BoolValue groundCheck;
    private final BoolValue voidCheck;
    private final BoolValue waitRegen;
    private final BoolValue invCheck;
    private final BoolValue absorpCheck;
    final MSTimer timer;
    int delay;
    boolean isDisable;
    
    public Heal() {
        this.percent = new FloatValue("HealthPercent", 75.0f, 1.0f, 100.0f);
        this.min = new IntegerValue("MinDelay", 75, 1, 5000);
        this.max = new IntegerValue("MaxDelay", 125, 1, 5000);
        this.regenSec = new FloatValue("RegenSec", 4.6f, 0.0f, 10.0f);
        this.groundCheck = new BoolValue("GroundCheck", false);
        this.voidCheck = new BoolValue("VoidCheck", true);
        this.waitRegen = new BoolValue("WaitRegen", true);
        this.invCheck = new BoolValue("InvCheck", false);
        this.absorpCheck = new BoolValue("AbsorpCheck", true);
        this.timer = new MSTimer();
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        this.timer.reset();
        this.isDisable = false;
        this.delay = MathHelper.getRandomIntegerInRange(new Random(), (int)this.min.get(), (int)this.max.get());
    }
    
    @EventTarget
    public void onPacket(final PacketEvent e) {
        if (e.getPacket() instanceof S02PacketChat && ((S02PacketChat)e.getPacket()).getChatComponent().getFormattedText().contains("§r§7 won the game! §r§e\u272a§r")) {
            ClientUtils.displayChatMessage("§f[§cSLHeal§f] §6Temp Disable Heal");
            this.isDisable = true;
        }
    }
    
    @EventTarget
    public void onUpdate(final UpdateEvent event) {
        if (mc.thePlayer.ticksExisted <= 5 && this.isDisable) {
            this.isDisable = false;
            ClientUtils.displayChatMessage("§f[§cSLHeal§f] §6Enable Heal due to World Changed or Player Respawned");
        }
        final int absorp = MathHelper.ceiling_double_int((double)mc.thePlayer.getAbsorptionAmount());
        if ((this.groundCheck.get() && !mc.thePlayer.onGround) || (this.voidCheck.get() && !MovementUtils.isBlockUnder()) || (this.invCheck.get() && mc.currentScreen instanceof GuiContainer) || (absorp != 0 && this.absorpCheck.get())) {
            return;
        }
        if (this.waitRegen.get() && mc.thePlayer.isPotionActive(Potion.regeneration) && mc.thePlayer.getActivePotionEffect(Potion.regeneration).getDuration() > this.regenSec.get() * 20.0f) {
            return;
        }
        final Pair<Integer, ItemStack> pair = this.getGAppleSlot();
        if (!this.isDisable && pair != null && (mc.thePlayer.getHealth() <= this.percent.get() / 100.0f * mc.thePlayer.getMaxHealth() || !mc.thePlayer.isPotionActive(Potion.absorption) || (absorp == 0 && mc.thePlayer.getHealth() == 20.0f && mc.thePlayer.isPotionActive(Potion.absorption))) && this.timer.hasTimePassed(this.delay)) {
            ClientUtils.displayChatMessage("§f[§cSLHeal§f] §6Healed");
            final int lastSlot = mc.thePlayer.inventory.currentItem;
            final int slot = (int)pair.getLeft();
            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(slot));
            final ItemStack stack = (ItemStack)pair.getRight();
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(stack));
            for (int i = 0; i < 32; ++i) {
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer());
            }
            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(lastSlot));
            mc.thePlayer.inventory.currentItem = lastSlot;
            mc.playerController.updateController();
            this.delay = MathHelper.getRandomIntegerInRange(new Random(), this.min.get(), this.max.get());
            this.timer.reset();
        }
    }
    
    private Pair<Integer, ItemStack> getGAppleSlot() {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == Items.golden_apple) {
                return (Pair<Integer, ItemStack>)Pair.of(i, stack);
            }
        }
        return null;
    }
    
    @Override
    public String getTag() {
        return (mc.thePlayer == null || mc.thePlayer.getHealth() == Double.NaN) ? null : String.format("%.2f HP", this.percent.get() / 100.0f * mc.thePlayer.getMaxHealth());
    }
}
