package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.ListValue

import net.ccbluex.liquidbounce.event.*

@ModuleInfo(name = "BetterFPS", description = "Make math calc faster.", category = ModuleCategory.MISC)
class BetterFPS : Module() {
    val sinMode = ListValue("SinMode", arrayOf("Vanilla", "Taylor", "LibGDX", "RivensFull", "RivensHalf", "Rivens", "Java", "1.16"), "Vanilla")
    val cosMode = ListValue("CosMode", arrayOf("Vanilla", "Taylor", "LibGDX", "RivensFull", "RivensHalf", "Rivens", "Java", "1.16"), "Vanilla")

    @EventTarget
    fun onTick(event: TickEvent) {
        this.state = false
    }
}