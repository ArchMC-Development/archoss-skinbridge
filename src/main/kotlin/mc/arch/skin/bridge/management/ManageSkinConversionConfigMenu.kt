package mc.arch.skin.bridge.management

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.configurable.editBoolean
import gg.scala.commons.configurable.editDuration
import gg.scala.commons.configurable.editInt
import gg.scala.commons.configurable.editString
import mc.arch.skin.SkinConversionConfig
import mc.arch.skin.SkinConversionDataSync
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 8/6/25
 */
class ManageSkinConversionConfigMenu : Menu("Managing Skin Conversion Config")
{
    init
    {
        shouldLoadInSync()
        placeholder = true
    }

    override fun size(buttons: Map<Int, Button>) = 45

    override fun getButtons(player: Player) = mapOf(
        10 to editString(
            SkinConversionDataSync,
            title = "MineSkin API Key",
            material = XMaterial.NAME_TAG,
            SkinConversionConfig::mineSkinApiKey,
            SkinConversionConfig::mineSkinApiKey::set,
        ),
        11 to editString(
            SkinConversionDataSync,
            title = "User Agent",
            material = XMaterial.COMPASS,
            SkinConversionConfig::userAgent,
            SkinConversionConfig::userAgent::set,
        ),
        12 to editInt(
            SkinConversionDataSync,
            title = "Max Concurrent Jobs",
            material = XMaterial.CHEST,
            SkinConversionConfig::maxConcurrentJobs,
            SkinConversionConfig::maxConcurrentJobs::set,
        ),
        19 to editInt(
            SkinConversionDataSync,
            title = "Default Width",
            material = XMaterial.ARROW,
            SkinConversionConfig::defaultWidth,
            SkinConversionConfig::defaultWidth::set,
            range = 16..256
        ),
        20 to editInt(
            SkinConversionDataSync,
            title = "Default Height",
            material = XMaterial.ARROW,
            SkinConversionConfig::defaultHeight,
            SkinConversionConfig::defaultHeight::set,
            range = 16..256
        ),
        28 to editBoolean(
            SkinConversionDataSync,
            title = "Cache Enabled",
            material = XMaterial.HOPPER,
            SkinConversionConfig::cacheEnabled,
            SkinConversionConfig::cacheEnabled::set,
        ),
        29 to editDuration(
            SkinConversionDataSync,
            title = "Skin Cache TTL (Minutes)",
            material = XMaterial.CLOCK,
            SkinConversionConfig::skinCacheTtlMinutes,
            SkinConversionConfig::skinCacheTtlMinutes::set,
            range = 1L..10080L // 1 minute to 1 week
        ),
        30 to editDuration(
            SkinConversionDataSync,
            title = "PNG Cache TTL (Minutes)",
            material = XMaterial.REDSTONE_TORCH,
            SkinConversionConfig::pngCacheTtlMinutes,
            SkinConversionConfig::pngCacheTtlMinutes::set,
            range = 1L..43200L // 1 minute to 30 days
        ),
        31 to editBoolean(
            SkinConversionDataSync,
            title = "Fallback Hash Enabled",
            material = XMaterial.PAPER,
            SkinConversionConfig::fallbackHashEnabled,
            SkinConversionConfig::fallbackHashEnabled::set,
        )
    )
}
