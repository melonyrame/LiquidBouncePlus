/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.features.module.modules.combat.*
import net.ccbluex.liquidbounce.features.module.modules.exploit.*
import net.ccbluex.liquidbounce.features.module.modules.misc.*
import net.ccbluex.liquidbounce.features.module.modules.movement.*
import net.ccbluex.liquidbounce.features.module.modules.player.*
import net.ccbluex.liquidbounce.features.module.modules.render.*
import net.ccbluex.liquidbounce.features.module.modules.world.*
import net.ccbluex.liquidbounce.features.module.modules.color.ColorMixer
import net.ccbluex.liquidbounce.utils.ClientUtils
import java.util.*


class ModuleManager : Listenable {

    public val modules = TreeSet<Module> { module1, module2 -> module1.name.compareTo(module2.name) }
    private val moduleClassMap = hashMapOf<Class<*>, Module>()

    public var shouldNotify : Boolean = false
    public var toggleSoundMode = 0

    init {
        LiquidBounce.eventManager.registerListener(this)
    }

    /**
     * Register all modules
     */
    fun registerModules() {
        ClientUtils.getLogger().info("[ModuleManager] Loading modules...")

        registerModules(
                AutoArmor::class.java,
                AutoPot::class.java,
                AutoWeapon::class.java,
                BowAimbot::class.java,
                Criticals::class.java,
                KillAura::class.java,
                Velocity::class.java,
                Fly::class.java,
                ClickGUI::class.java,
                HighJump::class.java,
                InventoryMove::class.java,
                NoSlow::class.java,
                LiquidWalk::class.java,
                Strafe::class.java,
                Sprint::class.java,
                Teams::class.java,
                NoRotateSet::class.java,
                AntiBot::class.java,
                ChestStealer::class.java,
                Scaffold::class.java,
                Tower::class.java,
                FastBreak::class.java,
                FastPlace::class.java,
                ESP::class.java,
                Speed::class.java,
                Tracers::class.java,
                NameTags::class.java,
                FastUse::class.java,
                Fullbright::class.java,
                ItemESP::class.java,
                StorageESP::class.java,
                Projectiles::class.java,
                PingSpoof::class.java,
                Step::class.java,
                AutoRespawn::class.java,
                AutoTool::class.java,
                NoWeb::class.java,
                Spammer::class.java,
                Regen::class.java,
                NoFall::class.java,
                Blink::class.java,
                NameProtect::class.java,
                NoHurtCam::class.java,
                MidClick::class.java,
                XRay::class.java,
                Timer::class.java,
                FreeCam::class.java,     
                HitBox::class.java,
                Plugins::class.java,
                LongJump::class.java,
                AutoClicker::class.java,
                BlockOverlay::class.java,
                NoFriends::class.java,
                BlockESP::class.java,
                Chams::class.java,
                Clip::class.java,
                Phase::class.java,
                ServerCrasher::class.java,
                NoFOV::class.java,
                Animations::class.java,
                ReverseStep::class.java,
                TNTBlock::class.java,
                InventoryCleaner::class.java,
                TrueSight::class.java,
                AntiBlind::class.java,
                Breadcrumbs::class.java,
                AbortBreaking::class.java,
                PotionSaver::class.java,
                CameraClip::class.java,
                WaterSpeed::class.java,
                ForceUnicodeChat::class.java,
                SuperKnockback::class.java,
                Reach::class.java,
                Rotations::class.java,
                NoJumpDelay::class.java,
                HUD::class.java,
                TNTESP::class.java,
                ResourcePackSpoof::class.java,
                NoSlowBreak::class.java,
                PortalMenu::class.java,
                Aesthetic::class.java,
                EnchantEffect::class.java,
                Cape::class.java,
                NoRender::class.java,
                DamageParticle::class.java,
                AntiVanish::class.java,
                Lightning::class.java,
                Skeletal::class.java,
                ItemPhysics::class.java,
                AutoLogin::class.java,
                Heal::class.java,
                AuthBypass::class.java,
                Gapple::class.java,
                ColorMixer::class.java,
                Disabler::class.java,
                AutoDisable::class.java,
                Crosshair::class.java,
                VehicleOneHit::class.java,
                BetterFPS::class.java,
                SpinBot::class.java,
                MultiActions::class.java,
                AntiVoid::class.java,
                Fucker::class.java,
                ChestAura::class.java
        )

        ClientUtils.getLogger().info("[ModuleManager] Loaded ${modules.size} modules.")
    }

    /**
     * Register [module]
     */
    fun registerModule(module: Module) {
        modules += module
        moduleClassMap[module.javaClass] = module

        module.onInitialize()
        generateCommand(module)
        LiquidBounce.eventManager.registerListener(module)
    }

    /**
     * Register [moduleClass]
     */
    private fun registerModule(moduleClass: Class<out Module>) {
        try {
            registerModule(moduleClass.newInstance())
        } catch (e: Throwable) {
            ClientUtils.getLogger().error("Failed to load module: ${moduleClass.name} (${e.javaClass.name}: ${e.message})")
        }
    }

    /**
     * Register a list of modules
     */
    @SafeVarargs
    fun registerModules(vararg modules: Class<out Module>) {
        modules.forEach(this::registerModule)
    }

    /**
     * Unregister module
     */
    fun unregisterModule(module: Module) {
        modules.remove(module)
        moduleClassMap.remove(module::class.java)
        LiquidBounce.eventManager.unregisterListener(module)
    }

    /**
     * Generate command for [module]
     */
    internal fun generateCommand(module: Module) {
        val values = module.values

        if (values.isEmpty())
            return

        LiquidBounce.commandManager.registerCommand(ModuleCommand(module, values))
    }

    /**
     * Legacy stuff
     *
     * TODO: Remove later when everything is translated to Kotlin
     */

    /**
     * Get module by [moduleClass]
     */
    fun getModule(moduleClass: Class<*>) = moduleClassMap[moduleClass]

    operator fun get(clazz: Class<*>) = getModule(clazz)

    /**
     * Get module by [moduleName]
     */
    fun getModule(moduleName: String?) = modules.find { it.name.equals(moduleName, ignoreCase = true) }

    /**
     * Module related events
     */

    /**
     * Handle incoming key presses
     */
    @EventTarget
    private fun onKey(event: KeyEvent) = modules.filter { it.keyBind == event.key }.forEach { it.toggle() }

    override fun handleEvents() = true
}
