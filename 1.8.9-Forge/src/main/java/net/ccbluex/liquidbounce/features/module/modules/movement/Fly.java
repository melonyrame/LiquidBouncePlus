/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.misc.RandomUtils;
import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import net.ccbluex.liquidbounce.utils.timer.TickTimer;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification;
import net.minecraft.block.BlockAir;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;

import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;

@ModuleInfo(name = "Fly", description = "Allows you to fly in survival mode.", category = ModuleCategory.MOVEMENT, keyBind = Keyboard.KEY_F)
public class Fly extends Module {

    public final ListValue modeValue = new ListValue("Mode", new String[]{
            "Motion",
            "Creative",
            "Damage",

            // Hypixel
            "Hypixel-Pearl",

            // Rewinside
            "Rewinside",

            // Other anticheats
            "Verus",

            // Spartan
            "Spartan",
            "Spartan2",
            "BugSpartan",
            
            // AAC
            "AAC5-Vanilla",

            // Other
            "Jetpack",
            "KeepAlive",
            "Jump",
            "Derp",
            "Collide"
    }, "Motion");

    private final FloatValue vanillaSpeedValue = new FloatValue("MotionSpeed", 2F, 0F, 5F);
    private final BoolValue vanillaKickBypassValue = new BoolValue("KickBypass", false);

    // Verus
    private final ListValue verusDmgModeValue = new ListValue("Verus-DamageMode", new String[]{"None", "Instant", "One-Hit"}, "None");
    private final ListValue verusBoostModeValue = new ListValue("Verus-BoostMode", new String[]{"Static", "Gradual"}, "Gradual");
    private final BoolValue verusVisualValue = new BoolValue("Verus-VisualPos", false);
    private final FloatValue verusVisualHeightValue = new FloatValue("Verus-VisualHeight", 0.42F, 0F, 1F);
    private final FloatValue verusSpeedValue = new FloatValue("Verus-Speed", 5F, 0F, 10F);
    private final FloatValue verusTimerValue = new FloatValue("Verus-Timer", 1F, 0.1F, 10F);
    private final IntegerValue verusDmgTickValue = new IntegerValue("Verus-Ticks", 20, 0, 300);

    // AAC
    private final BoolValue aac5SkidNotificationValue = new BoolValue("AAC5-SkidInfo", false);
    private final BoolValue aac5NoClipValue = new BoolValue("AAC5-NoClip", true);
    private final BoolValue aac5UseC04Packet = new BoolValue("AAC5-UseC04", true);
    private final IntegerValue aac5PursePacketsValue = new IntegerValue("AAC5-Purse", 7, 3, 20);

    // Visuals
    private final BoolValue markValue = new BoolValue("Mark", true);

    private BlockPos lastPosition;

    private double startY;

    private final MSTimer groundTimer = new MSTimer();
    
    private final TickTimer spartanTimer = new TickTimer();

    private boolean noPacketModify;

    private boolean noFlag;
    private int pearlState = 0;

    private boolean wasDead;

    private int boostTicks = 0;

    private float lastYaw, lastPitch;
    
    @Override
    public void onEnable() {
        if(mc.thePlayer == null)
            return;

        noPacketModify = true;

        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;

        lastYaw = mc.thePlayer.rotationYaw;
        lastPitch = mc.thePlayer.rotationPitch;

        final String mode = modeValue.get();

        boostTicks = 0;
        pearlState = 0;

        if (mode.equalsIgnoreCase("Verus")) {
            if (verusDmgModeValue.get().equalsIgnoreCase("Instant")) {
                if (mc.thePlayer.onGround && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0, 3.4, 0).expand(0, 0, 0)).isEmpty()) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y + 3.4, mc.thePlayer.posZ, false));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y + 0.00125, mc.thePlayer.posZ, false));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, false));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, true));
                    mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
                }
            }

            if (verusDmgModeValue.get().equalsIgnoreCase("One-Hit")) {
                if (mc.thePlayer.onGround && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0, 4, 0).expand(0, 0, 0)).isEmpty()) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y + 4, mc.thePlayer.posZ, false));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y + 4, mc.thePlayer.posZ, false));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, false));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, true));
                    mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
                }
            }
            if (verusVisualValue.get()) mc.thePlayer.setPosition(mc.thePlayer.posX, y + verusVisualHeightValue.get(), mc.thePlayer.posZ);
        } else if (mode.equalsIgnoreCase("BugSpartan")) {
            for(int i = 0; i < 65; ++i) {
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.049D, z, false));
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
            }
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.1D, z, true));
            mc.thePlayer.motionX *= 0.1D;
            mc.thePlayer.motionZ *= 0.1D;
            mc.thePlayer.swingItem();
        } else if (mode.equalsIgnoreCase("AAC5-Vanilla")) {
            if (aac5SkidNotificationValue.get()) LiquidBounce.hud.addNotification(new Notification("FDPClient skid (gone wrong)", 2000L));
            //if (aac5PursePacketsValue.get() != 7) LiquidBounce.hud.addNotification(new Notification("Only change the values if you know what you are going to do!", Notification.Type.WARNING, 3000L));
        }

        startY = mc.thePlayer.posY;
        noPacketModify = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        wasDead = false;

        if (mc.thePlayer == null)
            return;

        noFlag = false;

        final String mode = modeValue.get();

        if ((!mode.equalsIgnoreCase("Collide") && !mode.equalsIgnoreCase("Verus")) || (mode.equalsIgnoreCase("Hypixel-Pearl") && pearlState != -1)) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionZ = 0;
        }

        if (boostTicks > 0 && mode.equalsIgnoreCase("Verus")) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
        }

        if (mode.equalsIgnoreCase("AAC5-Vanilla")) {
            sendAAC5Packets();
        }

        mc.thePlayer.capabilities.isFlying = false;

        mc.timer.timerSpeed = 1F;
        mc.thePlayer.speedInAir = 0.02F;
    }

    @EventTarget
    public void onUpdate(final UpdateEvent event) {
        final float vanillaSpeed = vanillaSpeedValue.get();

        mc.thePlayer.noClip = false;

        switch (modeValue.get().toLowerCase()) {
            case "motion":
                mc.thePlayer.capabilities.isFlying = false;
                mc.thePlayer.motionY = 0;
                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionZ = 0;
                if (mc.gameSettings.keyBindJump.isKeyDown())
                    mc.thePlayer.motionY += vanillaSpeed;
                if (mc.gameSettings.keyBindSneak.isKeyDown())
                    mc.thePlayer.motionY -= vanillaSpeed;
                MovementUtils.strafe(vanillaSpeed);

                handleVanillaKickBypass();
                break;
            case "damage":
                mc.thePlayer.capabilities.isFlying = false;
                if (mc.thePlayer.hurtTime <= 0) break;
            case "derp":
            case "aac5-vanilla":
                if (aac5NoClipValue.get()) mc.thePlayer.noClip = true;
            case "bugspartan":
                mc.thePlayer.capabilities.isFlying = false;
                mc.thePlayer.motionY = 0;
                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionZ = 0;
                if (mc.gameSettings.keyBindJump.isKeyDown())
                    mc.thePlayer.motionY += vanillaSpeed;
                if (mc.gameSettings.keyBindSneak.isKeyDown())
                    mc.thePlayer.motionY -= vanillaSpeed;
                MovementUtils.strafe(vanillaSpeed);
                break;
            case "verus":
                mc.thePlayer.capabilities.isFlying = false;
                mc.thePlayer.motionX = mc.thePlayer.motionZ = mc.thePlayer.motionY = 0;

                if (boostTicks <= 0 && mc.thePlayer.hurtTime > 0) boostTicks = verusDmgTickValue.get();

                //if (verusGlideValue.get() && mc.thePlayer.ticksExisted % 6 == 0 && boostTicks <= 0) mc.thePlayer.motionY -= 0.2125;
                //else mc.thePlayer.motionY = 0;
                if (boostTicks > 0) {
                    mc.timer.timerSpeed = verusTimerValue.get();
                    float motion = 0F;
                    
                    if (verusBoostModeValue.get().equalsIgnoreCase("static")) motion = verusSpeedValue.get(); else motion = ((float)boostTicks / (float)verusDmgTickValue.get()) * verusSpeedValue.get();
                    boostTicks--;

                    MovementUtils.strafe(motion);
                } else {
                    mc.timer.timerSpeed = 1F;
                    MovementUtils.strafe(0.18F);
                }
                break;
            case "creative":
                mc.thePlayer.capabilities.isFlying = true;

                handleVanillaKickBypass();
                break;
            case "keepalive":
                mc.getNetHandler().addToSendQueue(new C00PacketKeepAlive());

                mc.thePlayer.capabilities.isFlying = false;
                mc.thePlayer.motionY = 0;
                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionZ = 0;
                if(mc.gameSettings.keyBindJump.isKeyDown())
                    mc.thePlayer.motionY += vanillaSpeed;
                if(mc.gameSettings.keyBindSneak.isKeyDown())
                    mc.thePlayer.motionY -= vanillaSpeed;
                MovementUtils.strafe(vanillaSpeed);
                break;
            case "jetpack":
                if(mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.effectRenderer.spawnEffectParticle(EnumParticleTypes.FLAME.getParticleID(), mc.thePlayer.posX, mc.thePlayer.posY + 0.2D, mc.thePlayer.posZ, -mc.thePlayer.motionX, -0.5D, -mc.thePlayer.motionZ);
                    mc.thePlayer.motionY += 0.15D;
                    mc.thePlayer.motionX *= 1.1D;
                    mc.thePlayer.motionZ *= 1.1D;
                }
                break;
            case "spartan":
                mc.thePlayer.motionY = 0;
                spartanTimer.update();
                if(spartanTimer.hasTimePassed(12)) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 8, mc.thePlayer.posZ, true));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 8, mc.thePlayer.posZ, true));
                    spartanTimer.reset();
                }
                break;
            case "spartan2":
                MovementUtils.strafe(0.264F);

                if(mc.thePlayer.ticksExisted % 8 == 0)
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 10, mc.thePlayer.posZ, true));
                break;
            case "hypixel-pearl":
                mc.thePlayer.capabilities.isFlying = false;
                mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;

                int enderPearlSlot = getPearlSlot();
                if (pearlState == 0) {
                    if (enderPearlSlot == -1) {
                        LiquidBounce.hud.addNotification(new Notification("You don't have any ender pearl!", Notification.Type.ERROR));
                        pearlState = -1;
                        this.setState(false);
                        return;
                    }

                    if (mc.thePlayer.inventory.currentItem != enderPearlSlot) {
                        mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(enderPearlSlot));
                    }

                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(mc.thePlayer.rotationYaw, 90, mc.thePlayer.onGround));
                    mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, mc.thePlayer.inventoryContainer.getSlot(enderPearlSlot + 36).getStack(), 0, 0, 0));
                    if (enderPearlSlot != mc.thePlayer.inventory.currentItem) {
                        mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    }
                    pearlState = 1;    
                }

                if (pearlState == 1 && mc.thePlayer.hurtTime > 0) pearlState = 2;

                if (pearlState == 2) {
                    if (mc.gameSettings.keyBindJump.isKeyDown())
                        mc.thePlayer.motionY += vanillaSpeed;
                    if (mc.gameSettings.keyBindSneak.isKeyDown())
                        mc.thePlayer.motionY -= vanillaSpeed;
                    MovementUtils.strafe(vanillaSpeed);
                }
                break;
            case "jump":
                if (mc.thePlayer.onGround)
                    mc.thePlayer.jump();
                break;
        }
    }

    @EventTarget
    public void onRender3D(final Render3DEvent event) {
        final String mode = modeValue.get();

        if (!markValue.get() || mode.equalsIgnoreCase("Motion") || mode.equalsIgnoreCase("Creative") || mode.equalsIgnoreCase("Damage") || mode.equalsIgnoreCase("AAC5-Vanilla") || mode.equalsIgnoreCase("Derp") || mode.equalsIgnoreCase("KeepAlive"))
            return;

        double y = startY + 2D;

        RenderUtils.drawPlatform(y, mc.thePlayer.getEntityBoundingBox().maxY < y ? new Color(0, 255, 0, 90) : new Color(255, 0, 0, 90), 1);
    }

    @EventTarget
    public void onRender2D(final Render2DEvent event) {
        final String mode = modeValue.get();
        ScaledResolution scaledRes = new ScaledResolution(mc);
        if (mode.equalsIgnoreCase("Verus") && boostTicks > 0) {
            float width = (float)(verusDmgTickValue.get() - boostTicks) / (float)verusDmgTickValue.get() * 60F;
            RenderUtils.drawRect(scaledRes.getScaledWidth() / 2F - 31F, scaledRes.getScaledHeight() / 2F + 14F, scaledRes.getScaledWidth() / 2F + 31F, scaledRes.getScaledHeight() / 2F + 18F, 0xA0000000);
            RenderUtils.drawRect(scaledRes.getScaledWidth() / 2F - 30F, scaledRes.getScaledHeight() / 2F + 15F, scaledRes.getScaledWidth() / 2F - 30F + width, scaledRes.getScaledHeight() / 2F + 17F, 0xFFFFFFFF);
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        final Packet<?> packet = event.getPacket();
        final String mode = modeValue.get();

        if(noPacketModify)
            return;

        if(packet instanceof C03PacketPlayer) {
            final C03PacketPlayer packetPlayer = (C03PacketPlayer) packet;

            if (mode.equalsIgnoreCase("Rewinside") || mode.equalsIgnoreCase("Verus"))
                packetPlayer.onGround = true;

            if (mode.equalsIgnoreCase("Derp")) {
                packetPlayer.yaw = RandomUtils.nextFloat(0F, 360F);
                packetPlayer.pitch = RandomUtils.nextFloat(-90F, 90F);
            }

            if (mode.equalsIgnoreCase("AAC5-Vanilla")) {
                aac5C03List.add(packetPlayer);
                event.cancelEvent();
                if(aac5C03List.size()>aac5PursePacketsValue.get())
                    sendAAC5Packets();
            }
        }
    }

    private final ArrayList<C03PacketPlayer> aac5C03List=new ArrayList<>();

    private void sendAAC5Packets(){
        float yaw = mc.thePlayer.rotationYaw;
        float pitch = mc.thePlayer.rotationPitch;
        for(C03PacketPlayer packet : aac5C03List){
            PacketUtils.sendPacketNoEvent(packet);
            if(packet.isMoving()){
                if (packet.getRotating()) {
                    yaw = packet.yaw;
                    pitch = packet.pitch;
                }
                if (aac5UseC04Packet.get()) {
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(packet.x,1e+159,packet.z, true));
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(packet.x,packet.y,packet.z, true));
                } else {
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(packet.x,1e+159,packet.z, yaw, pitch, true));
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(packet.x,packet.y,packet.z, yaw, pitch, true));
                }
            }
        }
        aac5C03List.clear();
    }

    @EventTarget
    public void onMove(final MoveEvent event) {
        switch(modeValue.get().toLowerCase()) {
            case "hypixel-pearl": {
                if (pearlState != 2) {
                    event.cancelEvent();
                }
            }
        }
    }

    @EventTarget
    public void onBB(final BlockBBEvent event) {
        if (mc.thePlayer == null) return;

        final String mode = modeValue.get();

        if (event.getBlock() instanceof BlockAir && mode.equalsIgnoreCase("Jump") && event.getY() < startY) {
            event.setBoundingBox(AxisAlignedBB.fromBounds(event.getX(), event.getY(), event.getZ(), event.getX() + 1, startY, event.getZ() + 1));
        }

        if ((mode.equalsIgnoreCase("collide")) && !mc.thePlayer.isSneaking()) 
            event.setBoundingBox(new AxisAlignedBB(-2, -1, -2, 2, 1, 2).offset(event.getX(), event.getY(), event.getZ()));

        if (event.getBlock() instanceof BlockAir && (mode.equalsIgnoreCase("Rewinside") || mode.equalsIgnoreCase("Verus")) 
            && event.getY() < mc.thePlayer.posY)
            event.setBoundingBox(AxisAlignedBB.fromBounds(event.getX(), event.getY(), event.getZ(), event.getX() + 1, mc.thePlayer.posY, event.getZ() + 1));
    }

    @EventTarget
    public void onJump(final JumpEvent e) {
        final String mode = modeValue.get();

        if (mode.equalsIgnoreCase("Rewinside"))
            e.cancelEvent();
    }

    @EventTarget
    public void onStep(final StepEvent e) {
        final String mode = modeValue.get();

        if (mode.equalsIgnoreCase("Rewinside"))
            e.setStepHeight(0F);
    }

    private void handleVanillaKickBypass() {
        if(!vanillaKickBypassValue.get() || !groundTimer.hasTimePassed(1000)) return;

        final double ground = calculateGround();

        for(double posY = mc.thePlayer.posY; posY > ground; posY -= 8D) {
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, posY, mc.thePlayer.posZ, true));

            if(posY - 8D < ground) break; // Prevent next step
        }

        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, ground, mc.thePlayer.posZ, true));


        for(double posY = ground; posY < mc.thePlayer.posY; posY += 8D) {
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, posY, mc.thePlayer.posZ, true));

            if(posY + 8D > mc.thePlayer.posY) break; // Prevent next step
        }

        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));

        groundTimer.reset();
    }

    // TODO: Make better and faster calculation lol
    private double calculateGround() {
        final AxisAlignedBB playerBoundingBox = mc.thePlayer.getEntityBoundingBox();
        double blockHeight = 1D;

        for(double ground = mc.thePlayer.posY; ground > 0D; ground -= blockHeight) {
            final AxisAlignedBB customBox = new AxisAlignedBB(playerBoundingBox.maxX, ground + blockHeight, playerBoundingBox.maxZ, playerBoundingBox.minX, ground, playerBoundingBox.minZ);

            if(mc.theWorld.checkBlockCollision(customBox)) {
                if(blockHeight <= 0.05D)
                    return ground + blockHeight;

                ground += blockHeight;
                blockHeight = 0.05D;
            }
        }

        return 0F;
    }

    // Hypixel things
    private int getPearlSlot() {
        for(int i = 36; i < 45; ++i) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stack.getItem() instanceof ItemEnderPearl) {
                return i - 36;
            }
        }
        return -1;
    }

    @Override
    public String getTag() {
        return modeValue.get();
    }
}
