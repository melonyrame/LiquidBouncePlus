package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S45PacketTitle
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C01PacketChatMessage
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.TextValue
import net.ccbluex.liquidbounce.value.IntegerValue

@ModuleInfo(name = "AutoLogin", description = "Automatically login into some servers for you.", category = ModuleCategory.MISC)
class AutoLogin : Module() {

	private val password = TextValue("Password", "example@01")
	private val regRegex = TextValue("Register-Regex", "/register")
	private val loginRegex = TextValue("Login-Regex", "/login")
	private val regCmd = TextValue("Register-Cmd", "/register %p %p")
	private val loginCmd = TextValue("Login-Cmd", "/login %p")

	private val delayValue = IntegerValue("Delay", 5000, 0, 5000)

	private val loginPackets = arrayListOf<C01PacketChatMessage>()
	private val registerPackets = arrayListOf<C01PacketChatMessage>()
	private val regTimer = MSTimer()
	private val logTimer = MSTimer()

	override fun onEnable() = resetEverything()

	@EventTarget
	fun onWorld(event: WorldEvent) = resetEverything()

	@EventTarget
	fun onUpdate(event: UpdateEvent) {
		if (registerPackets.isEmpty())
			regTimer.reset()
		else if (regTimer.hasTimePassed(delayValue.get().toLong())) {
			for (packet in registerPackets)
				PacketUtils.sendPacketNoEvent(packet)
			LiquidBounce.hud.addNotification(Notification("Successfully registered.", Notification.Type.SUCCESS))
			registerPackets.clear()
			regTimer.reset()
		}

		if (loginPackets.isEmpty())
			logTimer.reset()
		else if (logTimer.hasTimePassed(delayValue.get().toLong())) {
			for (packet in loginPackets)
				PacketUtils.sendPacketNoEvent(packet)
			LiquidBounce.hud.addNotification(Notification("Successfully logined.", Notification.Type.SUCCESS))
			loginPackets.clear()
			logTimer.reset()
		}
	}

    @EventTarget
    fun onPacket(event: PacketEvent) {
		if (mc.thePlayer == null)
			return

    	if (event.packet is S45PacketTitle) {
    		val s45 = event.packet as S45PacketTitle
			val messageOrigin = s45.getMessage() ?: return
    		var message : String = messageOrigin.getUnformattedText()

    		if (message.contains(loginRegex.get(), true))
    			sendLogin(loginCmd.get().replace("%p", password.get(), true))

    		if (message.contains(regRegex.get(), true))
    			sendRegister(regCmd.get().replace("%p", password.get(), true))
    	}

    	if (event.packet is S02PacketChat) {
    		val s02 = event.packet as S02PacketChat
    		var message : String = s02.getChatComponent().getUnformattedText()

    		if (message.contains(loginRegex.get(), true))
    			sendLogin(loginCmd.get().replace("%p", password.get(), true))

    		if (message.contains(regRegex.get(), true))
    			sendRegister(regCmd.get().replace("%p", password.get(), true))
    	}
    }

	private fun sendLogin(str: String) = loginPackets.add(C01PacketChatMessage(str))
	private fun sendRegister(str: String) = registerPackets.add(C01PacketChatMessage(str))

	private fun resetEverything() {
		registerPackets.clear()
		loginPackets.clear()
		regTimer.reset()
		logTimer.reset()
	}    

    override val tag: String?
        get() = password.get()

}