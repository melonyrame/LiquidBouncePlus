/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.color.ColorMixer
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.client.hud.element.Side.Horizontal
import net.ccbluex.liquidbounce.ui.client.hud.element.Side.Vertical
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowShader
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

/**
 * CustomHUD Arraylist element
 *
 * Shows a list of enabled modules
 */
@ElementInfo(name = "Arraylist", single = true)
class Arraylist(x: Double = 1.0, y: Double = 2.0, scale: Float = 1F,
                side: Side = Side(Horizontal.RIGHT, Vertical.UP)) : Element(x, y, scale, side) {
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Random", "Sky", "CRainbow", "LiquidSlowly", "Fade", "Mixer"), "Custom")
    val colorRedValue = IntegerValue("Red", 0, 0, 255)
    val colorGreenValue = IntegerValue("Green", 111, 0, 255)
    val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    val colorAlphaValue = IntegerValue("Alpha", 255, 0, 255)
    private val saturationValue = FloatValue("Saturation", 0.9f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val skyDistanceValue = IntegerValue("Sky-Distance", 2, 0, 4)
    private val cRainbowSecValue = IntegerValue("CRainbow-Seconds", 2, 1, 10)
    private val cRainbowDistValue = IntegerValue("CRainbow-Distance", 2, 1, 6)
    private val mixerSecValue = IntegerValue("Mixer-Seconds", 2, 1, 10)
    private val mixerDistValue = IntegerValue("Mixer-Distance", 2, 0, 10)
    private val liquidSlowlyDistanceValue = IntegerValue("LiquidSlowly-Distance", 90, 1, 90)
    private val fadeDistanceValue = IntegerValue("Fade-Distance", 50, 1, 100)
    private val hAnimation = ListValue("HorizontalAnimation", arrayOf("Default", "None", "Slide", "Astolfo"), "Default")
    private val vAnimation = ListValue("VerticalAnimation", arrayOf("None", "LiquidSense", "Slide", "Rise", "Astolfo"), "None")
    private val animationSpeed = FloatValue("Animation-Speed", 0.25F, 0.01F, 1F)
    private val nameBreak = BoolValue("NameBreak", true)
    private val abcOrder = BoolValue("Alphabetical-Order", false)
    private val tags = BoolValue("Tags", true)
    private val tagsStyleValue = ListValue("TagsStyle", arrayOf("-", "()", "[]", "Default"), "-")
    private val shadow = BoolValue("ShadowText", true)
    private val backgroundColorRedValue = IntegerValue("Background-R", 0, 0, 255)
    private val backgroundColorGreenValue = IntegerValue("Background-G", 0, 0, 255)
    private val backgroundColorBlueValue = IntegerValue("Background-B", 0, 0, 255)
    private val backgroundColorAlphaValue = IntegerValue("Background-Alpha", 0, 0, 255)
    private val rectValue = ListValue("Rect", arrayOf("None", "Left", "Right", "Outline", "Special"), "None")
    private val lowerCaseValue = BoolValue("LowerCase", false)
    private val spaceValue = FloatValue("Space", 0F, 0F, 5F)
    private val textHeightValue = FloatValue("TextHeight", 11F, 1F, 20F)
    private val textYValue = FloatValue("TextY", 1F, 0F, 20F)
    private val tagsArrayColor = BoolValue("TagsArrayColor", false)
    private val fontValue = FontValue("Font", Fonts.font40)

    private var x2 = 0
    private var y2 = 0F

    private var modules = emptyList<Module>()
    private var sortedModules = emptyList<Module>()

    override fun drawElement(): Border? {
        val fontRenderer = fontValue.get()
        val counter = intArrayOf(0)

        AWTFontRenderer.assumeNonVolatile = true

        // Slide animation - update every render
        val delta = RenderUtils.deltaTime
        
        // Draw arraylist
        val colorMode = colorModeValue.get()
        val rectColorMode = colorModeValue.get()
        val customColor = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get()).rgb
        val rectCustomColor = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get()).rgb
        val space = spaceValue.get()
        val textHeight = textHeightValue.get()
        val textY = textYValue.get()
        val rectMode = rectValue.get()
        val backgroundCustomColor = Color(backgroundColorRedValue.get(), backgroundColorGreenValue.get(),
                backgroundColorBlueValue.get(), backgroundColorAlphaValue.get()).rgb
        val textShadow = shadow.get()
        val textSpacer = textHeight + space
        val saturation = saturationValue.get()
        val brightness = brightnessValue.get()

        var inx = 0
        for (module in sortedModules) {
            val shouldAdd = module.array && module.slide > 0F
            // update slide y
            var yPos = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) *
                            if (side.vertical == Vertical.DOWN) inx + 1 else inx
            
            if (shouldAdd) {
                if (vAnimation.get().equals("Rise", ignoreCase = true) && !module.state) 
                    yPos = -fontRenderer.FONT_HEIGHT - textY

                val size = modules.size * 2.0E-2f

                when (vAnimation.get()) {
                    "LiquidSense" -> {
                        if (module.state) {
                            if (module.arrayY < yPos) {
                                module.arrayY += (size -
                                        Math.min(module.arrayY * 0.002f
                                                , size - (module.arrayY * 0.0001f) )) * delta
                                module.arrayY = Math.min(yPos, module.arrayY)
                            } else {
                                module.arrayY -= (size -
                                        Math.min(module.arrayY * 0.002f
                                                , size - (module.arrayY * 0.0001f) )) * delta
                                module.arrayY = Math.max(module.arrayY, yPos)
                            }
                        }
                    }
                    "Slide", "Rise" -> module.arrayY = net.ccbluex.liquidbounce.utils.AnimationUtils.animate(yPos.toDouble(), module.arrayY.toDouble(), animationSpeed.get().toDouble()).toFloat()
                    "Astolfo" -> {
                        if (module.arrayY < yPos) {
                            module.arrayY += animationSpeed.get() / 2F * RenderUtils.deltaTime
                            module.arrayY = Math.min(yPos, module.arrayY)
                        } else {
                            module.arrayY -= animationSpeed.get() / 2F * RenderUtils.deltaTime
                            module.arrayY = Math.max(module.arrayY, yPos)
                        }
                    }
                    else -> module.arrayY = yPos
                }
                inx++
            } else //instant update
                module.arrayY = yPos
        }

        for (module in LiquidBounce.moduleManager.modules) {
            // update slide x
            if (!module.array || (!module.state && module.slide == 0F)) continue

            var displayString = getModName(module)

            val width = fontRenderer.getStringWidth(displayString)

            when (hAnimation.get()) {
                "Astolfo" -> {
                    if (module.state) {
                        if (module.slide < width) {
                            module.slide += animationSpeed.get() * RenderUtils.deltaTime
                            module.slideStep = delta / 1F
                        }
                    } else if (module.slide > 0) {
                        module.slide -= animationSpeed.get() * RenderUtils.deltaTime
                        module.slideStep = 0F
                    }

                    if (module.slide > width) module.slide = width.toFloat()
                }
                "Slide" -> {
                    if (module.state) {
                        if (module.slide < width) {
                            module.slide = net.ccbluex.liquidbounce.utils.AnimationUtils.animate(width.toDouble(), module.slide.toDouble(), animationSpeed.get().toDouble()).toFloat()
                            module.slideStep = delta / 1F
                        }
                    } else if (module.slide > 0) {
                        module.slide = net.ccbluex.liquidbounce.utils.AnimationUtils.animate(-width.toDouble(), module.slide.toDouble(), animationSpeed.get().toDouble()).toFloat()
                        module.slideStep = 0F
                    }
                }
                "Default" -> {
                    if (module.state) {
                        if (module.slide < width) {
                            module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                            module.slideStep += delta / 4F
                        }
                    } else if (module.slide > 0) {
                        module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                        module.slideStep -= delta / 4F
                    }
                }
                else -> {
                    module.slide = if (module.state) width.toFloat() else 0f
                    module.slideStep += (if (module.state) delta else -delta).toFloat()
                }
            }

            module.slide = module.slide.coerceIn(0F, width.toFloat())
            module.slideStep = module.slideStep.coerceIn(0F, width.toFloat())
        }

        when (side.horizontal) {
            Horizontal.RIGHT, Horizontal.MIDDLE -> {
                modules.forEachIndexed { index, module ->
                    var displayString = getModName(module)

                    val width = fontRenderer.getStringWidth(displayString)
                    val xPos = -module.slide - 2

                    val moduleColor = Color.getHSBColor(module.hue, saturation, brightness).rgb

                    var Sky: Int
                    Sky = RenderUtils.SkyRainbow(counter[0] * (skyDistanceValue.get() * 50), saturationValue.get(), brightnessValue.get())
                    var CRainbow: Int
                    CRainbow = RenderUtils.getRainbowOpaque(cRainbowSecValue.get(), saturationValue.get(), brightnessValue.get(), counter[0] * (50 * cRainbowDistValue.get()))
                    var FadeColor: Int = ColorUtils.fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get()), index * fadeDistanceValue.get(), 100).rgb
                    counter[0] = counter[0] - 1 

                    val test = ColorUtils.LiquidSlowly(System.nanoTime(), index * liquidSlowlyDistanceValue.get(), saturationValue.get(), brightnessValue.get())?.rgb
                    var LiquidSlowly : Int = test!!

                    val mixerColor = ColorMixer.getMixedColor(-index * mixerDistValue.get() * 10, mixerSecValue.get()).rgb

                    RenderUtils.drawRect(
                            xPos - if (rectMode.equals("right", true)) 5 else 2,
                            module.arrayY,
                            if (rectMode.equals("right", true)) -3F else 0F,
                            module.arrayY + textHeight, backgroundCustomColor
                    )
                    
                    fontRenderer.drawString(displayString, xPos - if (rectMode.equals("right", true)) 3 else 0, module.arrayY + textY, when {
                        colorMode.equals("Random", ignoreCase = true) -> moduleColor
                        colorMode.equals("Sky", ignoreCase = true) -> Sky
                        colorMode.equals("CRainbow", ignoreCase = true) -> CRainbow
                        colorMode.equals("LiquidSlowly", ignoreCase = true) -> LiquidSlowly
                        colorMode.equals("Fade", ignoreCase = true) -> FadeColor
                        colorMode.equals("Mixer", ignoreCase = true) -> mixerColor
                        else -> customColor
                    }, textShadow)
                    

                    if (!rectMode.equals("none", true)) {
                        val rectColor = when {
                                rectColorMode.equals("Random", ignoreCase = true) -> moduleColor
                                rectColorMode.equals("Sky", ignoreCase = true) -> Sky
                                rectColorMode.equals("CRainbow", ignoreCase = true) -> CRainbow
                                rectColorMode.equals("LiquidSlowly", ignoreCase = true) -> LiquidSlowly
                                rectColorMode.equals("Fade", ignoreCase = true) -> FadeColor
                                rectColorMode.equals("Mixer", ignoreCase = true) -> mixerColor
                                else -> rectCustomColor
                            }

                            when {
                                rectMode.equals("left", true) -> RenderUtils.drawRect(xPos - 5, module.arrayY, xPos - 2, module.arrayY + textHeight,
                                        rectColor)
                                rectMode.equals("right", true) -> RenderUtils.drawRect(-3F, module.arrayY, 0F,
                                        module.arrayY + textHeight, rectColor)
                                rectMode.equals("outline", true) -> {                          
                                    RenderUtils.drawRect(-1F, module.arrayY - 1F, 0F,
                                            module.arrayY + textHeight, rectColor)
                                    RenderUtils.drawRect(xPos - 3, module.arrayY, xPos - 2, module.arrayY + textHeight,
                                            rectColor)
                                    if (module != modules[0]) {
                                        var displayStrings = getModName(modules[index - 1])

                                        RenderUtils.drawRect(xPos - 3 - (fontRenderer.getStringWidth(displayStrings) - fontRenderer.getStringWidth(displayString)), module.arrayY, xPos - 2, module.arrayY + 1,
                                                rectColor)
                                        if (module == modules[modules.size - 1]) {
                                            RenderUtils.drawRect(xPos - 3, module.arrayY + textHeight, 0.0F, module.arrayY + textHeight + 1,
                                                    rectColor)
                                        }
                                    } else {
                                        RenderUtils.drawRect(xPos - 3, module.arrayY, 0F, module.arrayY - 1, rectColor)
                                    }
                                }
                                rectMode.equals("special", true) -> {
                                    if (module == modules[0]) {
                                        RenderUtils.drawRect(xPos - 2, module.arrayY, 0F, module.arrayY - 1, rectColor)
                                    }
                                    if (module == modules[modules.size - 1]) {
                                        RenderUtils.drawRect(xPos - 2, module.arrayY + textHeight, 0F, module.arrayY + textHeight + 1, rectColor)
                                    }
                                }
                            }
                    }
                }
            }

            Horizontal.LEFT -> {
                modules.forEachIndexed { index, module ->
                    var displayString = getModName(module)

                    val width = fontRenderer.getStringWidth(displayString)
                    val xPos = -(width - module.slide) + if (rectMode.equals("left", true)) 5 else 2
                    
                    val moduleColor = Color.getHSBColor(module.hue, saturation, brightness).rgb 

                    var Sky: Int
                    Sky = RenderUtils.SkyRainbow(counter[0] * (skyDistanceValue.get() * 50), saturationValue.get(), brightnessValue.get())
                    var CRainbow: Int
                    CRainbow = RenderUtils.getRainbowOpaque(cRainbowSecValue.get(), saturationValue.get(), brightnessValue.get(), counter[0] * (50 * cRainbowDistValue.get()))
                    var FadeColor: Int = ColorUtils.fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get()), index * fadeDistanceValue.get(), 100).rgb
                    counter[0] = counter[0] - 1
                    val test = ColorUtils.LiquidSlowly(System.nanoTime(), index * liquidSlowlyDistanceValue.get(), saturationValue.get(), brightnessValue.get())?.rgb
                    var LiquidSlowly : Int = test!!

                    val mixerColor = ColorMixer.getMixedColor(-index * mixerDistValue.get() * 10, mixerSecValue.get()).rgb
                        
                    RenderUtils.drawRect(
                            0F,
                            module.arrayY,
                            xPos + width + if (rectMode.equals("right", true)) 5 else 2,
                            module.arrayY + textHeight, backgroundCustomColor
                    )

                    fontRenderer.drawString(displayString, xPos, module.arrayY + textY, when {
                        colorMode.equals("Random", ignoreCase = true) -> moduleColor
                        colorMode.equals("Sky", ignoreCase = true) -> Sky
                        colorMode.equals("CRainbow", ignoreCase = true) -> CRainbow
                        colorMode.equals("LiquidSlowly", ignoreCase = true) -> LiquidSlowly
                        colorMode.equals("Fade", ignoreCase = true) -> FadeColor
                        colorMode.equals("Mixer", ignoreCase = true) -> mixerColor
                        else -> customColor
                    }, textShadow)
                    
                    if (!rectMode.equals("none", true)) {
                        val rectColor = when {
                            rectColorMode.equals("Random", ignoreCase = true) -> moduleColor
                            rectColorMode.equals("Sky", ignoreCase = true) -> Sky
                            rectColorMode.equals("CRainbow", ignoreCase = true) -> CRainbow
                            rectColorMode.equals("LiquidSlowly", ignoreCase = true) -> LiquidSlowly
                            rectColorMode.equals("Fade", ignoreCase = true) -> FadeColor
                            rectColorMode.equals("Mixer", ignoreCase = true) -> mixerColor
                            else -> rectCustomColor
                        }

                        when {
                            rectMode.equals("left", true) -> RenderUtils.drawRect(0F,
                                    module.arrayY - 1, 3F, module.arrayY + textHeight, rectColor)
                            rectMode.equals("right", true) ->
                                RenderUtils.drawRect(xPos + width + 2, module.arrayY, xPos + width + 2 + 3,
                                        module.arrayY + textHeight, rectColor)
                        }
                    }
                    
                }
            }
        }

        // Draw border
        if (mc.currentScreen is GuiHudDesigner) {
            x2 = Int.MIN_VALUE

            if (modules.isEmpty()) {
                return if (side.horizontal == Horizontal.LEFT)
                    Border(0F, -1F, 20F, 20F)
                else
                    Border(0F, -1F, -20F, 20F)
            }

            for (module in modules) {
                when (side.horizontal) {
                    Horizontal.RIGHT, Horizontal.MIDDLE -> {
                        val xPos = -module.slide.toInt() - 2
                        if (x2 == Int.MIN_VALUE || xPos < x2) x2 = xPos
                    }
                    Horizontal.LEFT -> {
                        val xPos = module.slide.toInt() + 14
                        if (x2 == Int.MIN_VALUE || xPos > x2) x2 = xPos
                    }
                }
            }
            y2 = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) * modules.size

            return Border(0F, 0F, x2 - 7F, y2 - if (side.vertical == Vertical.DOWN) 1F else 0F)
        }

        AWTFontRenderer.assumeNonVolatile = false
        GlStateManager.resetColor()
        return null
    }

    override fun updateElement() {
        modules = if (abcOrder.get()) LiquidBounce.moduleManager.modules
                .filter { it.array && (if (hAnimation.get().equals("none", ignoreCase = true)) it.state else it.slide > 0) }
                else LiquidBounce.moduleManager.modules
                .filter { it.array && (if (hAnimation.get().equals("none", ignoreCase = true)) it.state else it.slide > 0) }
                .sortedBy { -fontValue.get().getStringWidth(getModName(it)) }
        sortedModules = if (abcOrder.get()) LiquidBounce.moduleManager.modules.toList()
                else LiquidBounce.moduleManager.modules.sortedBy { -fontValue.get().getStringWidth(getModName(it)) }.toList()
    }

    fun getModName(mod: Module): String {
        var displayName : String =  if (!tags.get()) //terrible fix
                                        mod.name
                                    else if (tagsArrayColor.get())
                                        when (tagsStyleValue.get()) {
                                            "-" -> mod.colorlessTagName
                                            "[]" -> mod.colorlessTagName.replaceFirst("- ", "[") + if (mod.colorlessTagName.contains("- ")) "]" else ""
                                            "()" -> mod.colorlessTagName.replaceFirst("- ", "(") + if (mod.colorlessTagName.contains("- ")) ")" else ""
                                            else -> mod.colorlessTagName.replaceFirst("- ", "")
                                        }
                                    else when (tagsStyleValue.get()) {
                                            "-" -> mod.tagName
                                            "[]" -> mod.tagName.replaceFirst("- ", "[") + if (mod.tagName.contains("- ")) "]" else ""
                                            "()" -> mod.tagName.replaceFirst("- ", "(") + if (mod.tagName.contains("- ")) ")" else ""
                                            else -> mod.tagName.replaceFirst("- ", "")
                                        }

        if (nameBreak.get()) {
            displayName = displayName.replaceFirst("AutoArmor", "Auto Armor", ignoreCase = false)
                //.replaceFirst("AutoBow", "Auto Bow", ignoreCase = false)
                //.replaceFirst("AutoLeave", "Auto Leave", ignoreCase = false)
                .replaceFirst("AutoPot", "Auto Pot", ignoreCase = false)
                //.replaceFirst("AutoSoup", "Auto Soup", ignoreCase = false)
                .replaceFirst("AutoWeapon", "Auto Weapon", ignoreCase = false)
                .replaceFirst("BowAimbot", "Bow Aimbot", ignoreCase = false)
                .replaceFirst("KillAura", "Kill Aura", ignoreCase = false)
                //.replaceFirst("VanillaAura", "Vanilla Aura", ignoreCase = false)
                .replaceFirst("HighJump", "High Jump", ignoreCase = false)
                .replaceFirst("InvMove", "Inv Move", ignoreCase = false)
                .replaceFirst("NoSlowBreak", "No Slow Break", ignoreCase = false)
                .replaceFirst("NoSlow", "No Slow", ignoreCase = false)
                .replaceFirst("LiquidWalk", "Liquid Walk", ignoreCase = false)
                //.replaceFirst("SafeWalk", "Safe Walk", ignoreCase = false)
                //.replaceFirst("WallClimb", "Wall Climb", ignoreCase = false)
                .replaceFirst("NoRotateSet", "No Rotate Set", ignoreCase = false)
                .replaceFirst("AntiBot", "Anti Bot", ignoreCase = false)
                .replaceFirst("ChestStealer", "Chest Stealer", ignoreCase = false)
                //.replaceFirst("CivBreak", "Civ Break", ignoreCase = false)
                .replaceFirst("FastBreak", "Fast Break", ignoreCase = false)
                .replaceFirst("FastPlace", "Fast Place", ignoreCase = false)
                .replaceFirst("NameTags", "Name Tags", ignoreCase = false)
                .replaceFirst("FastUse", "Fast Use", ignoreCase = false)
                .replaceFirst("ItemESP", "Item ESP", ignoreCase = false)
                .replaceFirst("StorageESP", "Storage ESP", ignoreCase = false)
                //.replaceFirst("NoClip", "No Clip", ignoreCase = false)
                .replaceFirst("PingSpoof", "Ping Spoof", ignoreCase = false)
                //.replaceFirst("FastClimb", "Fast Climb", ignoreCase = false)
                .replaceFirst("AutoRespawn", "Auto Respawn", ignoreCase = false)
                .replaceFirst("AutoTool", "Auto Tool", ignoreCase = false)
                .replaceFirst("NoWeb", "No Web", ignoreCase = false)
                //.replaceFirst("IceSpeed", "Ice Speed", ignoreCase = false)
                .replaceFirst("NoFall", "No Fall", ignoreCase = false)
                .replaceFirst("NameProtect", "Name Protect", ignoreCase = false)
                .replaceFirst("NoHurtCam", "No Hurt Cam", ignoreCase = false)
                //.replaceFirst("SkinDerp", "Skin Derp", ignoreCase = false)
                //.replaceFirst("GhostHand", "Ghost Hand", ignoreCase = false)
                //.replaceFirst("AutoWalk", "Auto Walk", ignoreCase = false)
                //.replaceFirst("AutoBreak", "Auto Break", ignoreCase = false)
                .replaceFirst("FreeCam", "Free Cam", ignoreCase = false)
                .replaceFirst("HitBox", "Hit Box", ignoreCase = false)
                //.replaceFirst("AntiCactus", "Anti Cactus", ignoreCase = false)
                //.replaceFirst("AntiHunger", "Anti Hunger", ignoreCase = false)
                .replaceFirst("ConsoleSpammer", "Console Spammer", ignoreCase = false)
                .replaceFirst("LongJump", "Long Jump", ignoreCase = false)
                //.replaceFirst("LadderJump", "Ladder Jump", ignoreCase = false)
                //.replaceFirst("FastBow", "Fast Bow", ignoreCase = false)
                .replaceFirst("MultiActions", "Multi Actions", ignoreCase = false)
                //.replaceFirst("AirJump", "Air Jump", ignoreCase = false)
                .replaceFirst("AutoClicker", "Auto Clicker", ignoreCase = false)
                .replaceFirst("NoBob", "No Bob", ignoreCase = false)
                .replaceFirst("BlockOverlay", "Block Overlay", ignoreCase = false)
                .replaceFirst("NoFriends", "No Friends", ignoreCase = false)
                .replaceFirst("BlockESP", "Block ESP", ignoreCase = false)
                //.replaceFirst("ServerCrasher", "Server Crasher", ignoreCase = false)
                .replaceFirst("NoFOV", "No FOV", ignoreCase = false)
                //.replaceFirst("FastStairs", "Fast Stairs", ignoreCase = false)
                .replaceFirst("ReverseStep", "Reverse Step", ignoreCase = false)
                .replaceFirst("TNTBlock", "TNT Block", ignoreCase = false)
                .replaceFirst("InventoryCleaner", "Inventory Cleaner", ignoreCase = false)
                .replaceFirst("TrueSight", "True Sight", ignoreCase = false)
                .replaceFirst("LiquidChat", "Liquid Chat", ignoreCase = false)
                .replaceFirst("AntiBlind", "Anti Blind", ignoreCase = false)
                .replaceFirst("BedGodMode", "Bed God Mode", ignoreCase = false)
                .replaceFirst("AntiVoid", "Anti Void", ignoreCase = false)
                .replaceFirst("AbortBreaking", "Abort Breaking", ignoreCase = false)
                .replaceFirst("PotionSaver", "Potion Saver", ignoreCase = false)
                .replaceFirst("CameraClip", "Camera Clip", ignoreCase = false)
                .replaceFirst("WaterSpeed", "Water Speed", ignoreCase = false)
                //.replaceFirst("SlimeJump", "Slime Jump", ignoreCase = false)
                //.replaceFirst("MoreCarry", "More Carry", ignoreCase = false)
                //.replaceFirst("NoPitchLimit", "No Pitch Limit", ignoreCase = false)
                //.replaceFirst("AtAllProvider", "At All Provider", ignoreCase = false)
                //.replaceFirst("AirLadder", "Air Ladder", ignoreCase = false)
                //.replaceFirst("GodMode", "God Mode", ignoreCase = false)
                //.replaceFirst("TeleportHit", "Teleport Hit", ignoreCase = false)
                .replaceFirst("ForceUnicodeChat", "Force Unicode Chat", ignoreCase = false)
                .replaceFirst("ItemTeleport", "Item Teleport", ignoreCase = false)
                //.replaceFirst("BufferSpeed", "Buffer Speed", ignoreCase = false)
                .replaceFirst("SuperKnockback", "Super Knockback", ignoreCase = false)
                //.replaceFirst("ProphuntESP", "Prophunt ESP", ignoreCase = false)
                .replaceFirst("AutoFish", "Auto Fish", ignoreCase = false)
                //.replaceFirst("KeepContainer", "Keep Container", ignoreCase = false)
                .replaceFirst("VehicleOneHit", "Vehicle One Hit", ignoreCase = false)
                .replaceFirst("NoJumpDelay", "No Jump Delay", ignoreCase = false)
                //.replaceFirst("BlockWalk", "Block Walk", ignoreCase = false)
                //.replaceFirst("AntiAFK", "Anti AFK", ignoreCase = false)
                //.replaceFirst("PerfectHorseJump", "Perfect Horse Jump", ignoreCase = false)
                .replaceFirst("TNTESP", "TNT ESP", ignoreCase = false)
                //.replaceFirst("ComponentOnHover", "Component On Hover", ignoreCase = false)
                //.replaceFirst("KeepAlive", "Keep Alive", ignoreCase = false)
                .replaceFirst("ResourcePackSpoof", "Resource Pack Spoof", ignoreCase = false)
                .replaceFirst("PortalMenu", "Portal Menu", ignoreCase = false)
                .replaceFirst("LagBack", "Lag Back", ignoreCase = false)
                //.replaceFirst("NoFire", "No Fire", ignoreCase = false)
                .replaceFirst("EnchantEffect", "Enchant Effect", ignoreCase = false)
                .replaceFirst("NoScoreboard", "No Scoreboard", ignoreCase = false)
                .replaceFirst("ChestAura", "Chest Aura", ignoreCase = false)
                .replaceFirst("TargetStrafe", "Target Strafe", ignoreCase = false)
                .replaceFirst("ItemPhysics", "Item Physics", ignoreCase = false)
                .replaceFirst("NoRender", "No Render", ignoreCase = false)
                .replaceFirst("AntiVanish", "Anti Vanish", ignoreCase = false)
                .replaceFirst("DamageParticle", "Damage Particle", ignoreCase = false)
                //.replaceFirst("ChatBypass", "Chat Bypass", ignoreCase = false)
                .replaceFirst("HackerDetector", "Hacker Detector", ignoreCase = false)
                //.replaceFirst("TargetMark", "Target Mark", ignoreCase = false)
                //.replaceFirst("LightningDetect", "Lightning Detect", ignoreCase = false)
                .replaceFirst("AutoLogin", "Auto Login", ignoreCase = false)
                .replaceFirst("AuthBypass", "Auth Bypass", ignoreCase = false)
                .replaceFirst("AutoDisable", "Auto Disable", ignoreCase = false)
                .replaceFirst("SpinBot", "Spin Bot", ignoreCase = false)
                //.replaceFirst("VPNBypass", "VPN Bypass", ignoreCase = false)
        }

        if (lowerCaseValue.get()) 
            displayName = displayName.toLowerCase()

        return displayName        
    }
}