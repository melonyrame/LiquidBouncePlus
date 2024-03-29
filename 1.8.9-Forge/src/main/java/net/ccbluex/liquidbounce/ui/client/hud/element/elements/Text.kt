/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import de.enzaxd.viaforge.ViaForge
import de.enzaxd.viaforge.protocols.ProtocolCollection
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.color.ColorMixer
import net.ccbluex.liquidbounce.features.module.modules.misc.NameProtect
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.CPSCounter
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.ServerUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.UiUtils
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowShader
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import kotlin.math.sqrt
import java.lang.Math.pow

/**
 * CustomHUD text element
 *
 * Allows to draw custom text
 */
@ElementInfo(name = "Text")
class Text(x: Double = 10.0, y: Double = 10.0, scale: Float = 1F,
           side: Side = Side.default()) : Element(x, y, scale, side) {

    companion object {

        val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd")
        val HOUR_FORMAT = SimpleDateFormat("HH:mm")

        val DECIMAL_FORMAT = DecimalFormat("0.00")
        val DECIMAL_FORMAT_INT = DecimalFormat("0")

        /**
         * Create default element
         */
        fun defaultClient(): Text {
            val text = Text(x = 2.0, y = 2.0, scale = 1F)

            text.displayString.set("%clientName%")
            text.shadow.set(true)
            text.fontValue.set(Fonts.font40)
            text.setColor(Color(255, 255, 255))

            return text
        }

    }

    private val displayString = TextValue("DisplayText", "")
    private val backgroundValue = BoolValue("Background", true)
    private val skeetRectValue = BoolValue("SkeetRect", false)
    private val newSkeetRectValue = BoolValue("NewSkeetRect", false)
    private val lineValue = BoolValue("Line", true)
    private val redValue = IntegerValue("Red", 255, 0, 255)
    private val greenValue = IntegerValue("Green", 255, 0, 255)
    private val blueValue = IntegerValue("Blue", 255, 0, 255)
    private val alphaValue = IntegerValue("Alpha", 255, 0, 255)
    private val bgredValue = IntegerValue("Background-Red", 0, 0, 255)
    private val bggreenValue = IntegerValue("Background-Green", 0, 0, 255)
    private val bgblueValue = IntegerValue("Background-Blue", 0, 0, 255)
    private val bgalphaValue = IntegerValue("Background-Alpha", 120, 0, 255)
    private val rainbowList = ListValue("Rainbow", arrayOf("Off", "CRainbow", "Sky", "LiquidSlowly", "Fade", "Mixer"), "Off")
    private val rainbowX = FloatValue("Rainbow-X", -1000F, -2000F, 2000F)
    private val rainbowY = FloatValue("Rainbow-Y", -1000F, -2000F, 2000F)
    private val saturationValue = FloatValue("Saturation", 0.9f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val cRainbowSecValue = IntegerValue("Seconds", 2, 1, 10)
    private val shadow = BoolValue("Shadow", true)
    private var fontValue = FontValue("Font", Fonts.font40)

    private var editMode = false
    private var editTicks = 0
    private var prevClick = 0L

    private var lastX : Double = 0.0
    private var lastZ : Double = 0.0

    private var speedStr = "";

    private var displayText = display

    private val display: String
        get() {
            val textContent = if (displayString.get().isEmpty() && !editMode)
                "Text Element"
            else
                displayString.get()


            return ColorUtils.translateAlternateColorCodes(multiReplace(textContent))
        }

    private fun getReplacement(str: String): String? {
        if (mc.thePlayer != null) {
            when (str) {
                "x" -> return DECIMAL_FORMAT.format(mc.thePlayer.posX)
                "y" -> return DECIMAL_FORMAT.format(mc.thePlayer.posY)
                "z" -> return DECIMAL_FORMAT.format(mc.thePlayer.posZ)
                "xInt" -> return DECIMAL_FORMAT_INT.format(mc.thePlayer.posX)
                "yInt" -> return DECIMAL_FORMAT_INT.format(mc.thePlayer.posY)
                "zInt" -> return DECIMAL_FORMAT_INT.format(mc.thePlayer.posZ)
                "xdp" -> return mc.thePlayer.posX.toString()
                "ydp" -> return mc.thePlayer.posY.toString()
                "zdp" -> return mc.thePlayer.posZ.toString()
                "velocity" -> return DECIMAL_FORMAT.format(sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ))
                "ping" -> return EntityUtils.getPing(mc.thePlayer).toString()
                "health" -> return DECIMAL_FORMAT.format(mc.thePlayer.health)
                "maxHealth" -> return DECIMAL_FORMAT.format(mc.thePlayer.maxHealth)
                "healthInt" -> return DECIMAL_FORMAT_INT.format(mc.thePlayer.health)
                "maxHealthInt" -> return DECIMAL_FORMAT_INT.format(mc.thePlayer.maxHealth)
                "yaw" -> return DECIMAL_FORMAT.format(mc.thePlayer.rotationYaw)
                "pitch" -> return DECIMAL_FORMAT.format(mc.thePlayer.rotationPitch)
                "yawInt" -> return DECIMAL_FORMAT_INT.format(mc.thePlayer.rotationYaw)
                "pitchInt" -> return DECIMAL_FORMAT_INT.format(mc.thePlayer.rotationPitch)
                "bps" -> return speedStr
                "inBound" -> return PacketUtils.avgInBound.toString()
                "outBound" -> return PacketUtils.avgOutBound.toString()
                "hurtTime" -> return mc.thePlayer.hurtTime.toString()
                "onGround" -> return mc.thePlayer.onGround.toString()
            }
        }

        return when (str) {
            "userName" -> mc.session.username
            "clientName" -> LiquidBounce.CLIENT_NAME
            "clientVersion" -> LiquidBounce.CLIENT_VERSION.toString()
            "clientCreator" -> LiquidBounce.CLIENT_CREATOR
            "fps" -> Minecraft.getDebugFPS().toString()
            "date" -> DATE_FORMAT.format(System.currentTimeMillis())
            "time" -> HOUR_FORMAT.format(System.currentTimeMillis())
            "serverIp" -> ServerUtils.getRemoteIp()
            "cps", "lcps" -> return CPSCounter.getCPS(CPSCounter.MouseButton.LEFT).toString()
            "mcps" -> return CPSCounter.getCPS(CPSCounter.MouseButton.MIDDLE).toString()
            "rcps" -> return CPSCounter.getCPS(CPSCounter.MouseButton.RIGHT).toString()
            "portalVersion" -> ProtocolCollection.getProtocolById(ViaForge.getInstance().version).getName()
            else -> null // Null = don't replace
        }
    }

    private fun multiReplace(str: String): String {
        var lastPercent = -1
        val result = StringBuilder()
        for (i in str.indices) {
            if (str[i] == '%') {
                if (lastPercent != -1) {
                    if (lastPercent + 1 != i) {
                        val replacement = getReplacement(str.substring(lastPercent + 1, i))

                        if (replacement != null) {
                            result.append(replacement)
                            lastPercent = -1
                            continue
                        }
                    }
                    result.append(str, lastPercent, i)
                }
                lastPercent = i
            } else if (lastPercent == -1) {
                result.append(str[i])
            }
        }

        if (lastPercent != -1) {
            result.append(str, lastPercent, str.length)
        }

        return result.toString()
    }

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        val color = Color(redValue.get(), greenValue.get(), blueValue.get(), alphaValue.get()).rgb

        val fontRenderer = fontValue.get()

        val rainbowType = rainbowList.get()

        when (side.horizontal) {
            Side.Horizontal.LEFT -> GL11.glTranslatef(0F, 0F, 0F)
            Side.Horizontal.MIDDLE -> GL11.glTranslatef(-fontRenderer.getStringWidth(displayText).toFloat() / 2F, 0F, -fontRenderer.getStringWidth(displayText).toFloat() / 2F)
            Side.Horizontal.RIGHT -> GL11.glTranslatef(-fontRenderer.getStringWidth(displayText).toFloat(), 0F, -fontRenderer.getStringWidth(displayText).toFloat())
        }

        if (backgroundValue.get()) {
            RenderUtils.drawRect(-2F, -2F, fontRenderer.getStringWidth(displayText) + 2F, fontRenderer.FONT_HEIGHT + 0F, Color(bgredValue.get(), bggreenValue.get(), bgblueValue.get(), bgalphaValue.get()))
        }

        if (skeetRectValue.get()) {
            UiUtils.drawRect(-11.0, -9.5, (fontRenderer.getStringWidth(displayText) + 9).toDouble(), fontRenderer.FONT_HEIGHT.toDouble()+6,Color(0,0,0).rgb)
            UiUtils.outlineRect(-10.0, -8.5, (fontRenderer.getStringWidth(displayText) + 8).toDouble(), fontRenderer.FONT_HEIGHT.toDouble()+5,8.0, Color(59,59,59).rgb,Color(59,59,59).rgb)
            UiUtils.outlineRect(-9.0, -7.5, (fontRenderer.getStringWidth(displayText) + 7).toDouble(), fontRenderer.FONT_HEIGHT.toDouble()+4,4.0, Color(59,59,59).rgb,Color(40,40,40).rgb)
            UiUtils.outlineRect(-4.0, -3.0, (fontRenderer.getStringWidth(displayText) + 2).toDouble(), fontRenderer.FONT_HEIGHT.toDouble()+0,1.0, Color(18,18,18).rgb,Color(0,0,0).rgb)
        }

        if (newSkeetRectValue.get()) {
            drawExhiRect(-4F, -4F, fontRenderer.getStringWidth(displayText) + 4F, fontRenderer.FONT_HEIGHT + 2F)
        }

        var FadeColor : Int = ColorUtils.fade(Color(redValue.get(), greenValue.get(), blueValue.get(), alphaValue.get()), 0, 100).rgb
        val LiquidSlowly = ColorUtils.LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get())?.rgb
        var liquidSlowli : Int = LiquidSlowly!!

        val mixerColor = ColorMixer.getMixedColor(0, cRainbowSecValue.get()).rgb

        if (lineValue.get()) {
            RenderUtils.drawRect(-2F, -3F, fontRenderer.getStringWidth(displayText) + 2F, -2F, when (rainbowType) {
                "CRainbow" -> RenderUtils.getRainbowOpaque(cRainbowSecValue.get(), saturationValue.get(), brightnessValue.get(), 0)
                "Sky" -> RenderUtils.SkyRainbow(0, saturationValue.get(), brightnessValue.get())
                "LiquidSlowly" -> liquidSlowli
                "Fade" -> FadeColor
                "Mixer" -> mixerColor
                else -> color
            })
        }

        fontRenderer.drawString(displayText, 0F, 0F, when (rainbowType) {
            "CRainbow" -> RenderUtils.getRainbowOpaque(cRainbowSecValue.get(), saturationValue.get(), brightnessValue.get(), 0)
            "Sky" -> RenderUtils.SkyRainbow(0, saturationValue.get(), brightnessValue.get())
            "LiquidSlowly" -> liquidSlowli
            "Fade" -> FadeColor
            "Mixer" -> mixerColor
            else -> color
        }, shadow.get())

        if (editMode && mc.currentScreen is GuiHudDesigner && editTicks <= 40)
            fontRenderer.drawString("_", fontRenderer.getStringWidth(displayText) + 2F,
                0F, when (rainbowType) {
            "CRainbow" -> RenderUtils.getRainbowOpaque(cRainbowSecValue.get(), saturationValue.get(), brightnessValue.get(), 0)
            "Sky" -> RenderUtils.SkyRainbow(0, saturationValue.get(), brightnessValue.get())
            "LiquidSlowly" -> liquidSlowli
            "Fade" -> FadeColor
            "Mixer" -> mixerColor
            else -> color
        }, shadow.get()) 

        if (editMode && mc.currentScreen !is GuiHudDesigner) {
            editMode = false
            updateElement()
        }

        when (side.horizontal) {
            Side.Horizontal.LEFT -> GL11.glTranslatef(0F, 0F, 0F)
            Side.Horizontal.MIDDLE -> GL11.glTranslatef(fontRenderer.getStringWidth(displayText).toFloat() / 2F, 0F, fontRenderer.getStringWidth(displayText).toFloat() / 2F)
            Side.Horizontal.RIGHT -> GL11.glTranslatef(fontRenderer.getStringWidth(displayText).toFloat(), 0F, fontRenderer.getStringWidth(displayText).toFloat())
        }

        return when (side.horizontal) {
            Side.Horizontal.LEFT -> Border(
                -2F,
                -2F,
                fontRenderer.getStringWidth(displayText) + 2F,
                fontRenderer.FONT_HEIGHT.toFloat()
            )
            Side.Horizontal.MIDDLE -> Border(
                -fontRenderer.getStringWidth(displayText).toFloat() / 2F,
                -2F,
                fontRenderer.getStringWidth(displayText).toFloat() / 2F + 2F,
                fontRenderer.FONT_HEIGHT.toFloat()
            )
            Side.Horizontal.RIGHT -> Border(
                2F,
                -2F,
                -fontRenderer.getStringWidth(displayText) - 2F,
                fontRenderer.FONT_HEIGHT.toFloat()
            )
        }
    }

    private fun drawExhiRect(x: Float, y: Float, x2: Float, y2: Float) {
        RenderUtils.drawRect(x - 1, y - 1, x2 + 1, y2 + 1, Color(59, 59, 59).rgb)
        RenderUtils.drawBorderedRect(x + 2F, y + 2F, x2 - 2F, y2 - 2F, 0.5F, Color(18, 18, 18).rgb, Color(28, 28, 28).rgb)
    }

    override fun updateElement() {
        editTicks += 5
        if (editTicks > 80) editTicks = 0

        displayText = if (editMode) displayString.get() else display

        //blocks per sec counter
        if (mc.thePlayer == null) return
        speedStr = DECIMAL_FORMAT.format(sqrt(pow(lastX - mc.thePlayer.posX, 2.0) + pow(lastZ - mc.thePlayer.posZ, 2.0)) * 20 * mc.timer.timerSpeed)
        lastX = mc.thePlayer.posX
        lastZ = mc.thePlayer.posZ
    }

    override fun handleMouseClick(x: Double, y: Double, mouseButton: Int) {
        if (isInBorder(x, y) && mouseButton == 0) {
            if (System.currentTimeMillis() - prevClick <= 250L)
                editMode = true

            prevClick = System.currentTimeMillis()
        } else {
            editMode = false
        }
    }

    override fun handleKey(c: Char, keyCode: Int) {
        if (editMode && mc.currentScreen is GuiHudDesigner) {
            if (keyCode == Keyboard.KEY_BACK) {
                if (displayString.get().isNotEmpty())
                    displayString.set(displayString.get().substring(0, displayString.get().length - 1))

                updateElement()
                return
            }

            if (ChatAllowedCharacters.isAllowedCharacter(c) || c == '§')
                displayString.set(displayString.get() + c)

            updateElement()
        }
    }

    fun setColor(c: Color): Text {
        redValue.set(c.red)
        greenValue.set(c.green)
        blueValue.set(c.blue)
        return this
    }

}