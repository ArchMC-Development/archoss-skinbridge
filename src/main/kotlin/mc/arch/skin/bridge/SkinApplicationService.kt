package mc.arch.skin.bridge

import com.destroystokyo.paper.profile.ProfileProperty
import gg.scala.commons.ScalaCommons
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.commons.communications.rpc.RPCContext
import mc.arch.commons.communications.rpc.RPCHandler
import mc.arch.skin.cache.CachedEaglerProfile
import mc.arch.skin.rpc.NewSkinAvailableRequest
import mc.arch.skin.rpc.SkinConversionRPC
import mc.arch.skin.rpc.SkinConversionRequest
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import net.evilblock.cubed.serializers.Serializers
import org.bukkit.Bukkit
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

/**
 * @author Subham
 * @since 8/6/25
 */
@Service
object SkinApplicationService : RPCHandler<mc.arch.skin.bridge.rpc.NewSkinAvailableRequest, Unit>
{
    private var shouldEnqueueConversions = false
    fun enqueueConversions()
    {
        shouldEnqueueConversions = true
    }

    @Configure
    fun configure()
    {
        _root_ide_package_.mc.arch.skin.bridge.rpc.SkinConversionRPC.availableRPC.addHandler(this)

        Events
            .subscribe(AsyncPlayerPreLoginEvent::class.java)
            .handler { event ->
                val profile = event.playerProfile
                val metadata = ScalaCommons
                    .bundle().globals().redis()
                    .sync()
                    .hget("eaglercraft:metadata", event.uniqueId.toString())
                    ?: return@handler

                val eaglerProfile = Serializers.gson
                    .fromJson(metadata, CachedEaglerProfile::class.java)
                if (eaglerProfile.skinABGR864x64Base64Encoded == null)
                {
                    return@handler
                }

                val hash = SkinConversionDataSync
                    .toAgent()
                    .generateSkinHash(eaglerProfile.skinABGR864x64Base64Encoded)

                val cachedSkin = SkinConversionDataSync.toAgent()
                    .getCachedSkinResponse(hash)
                    ?: return@handler run {
                        if (shouldEnqueueConversions)
                        {
                            SkinConversionRPC.convertSkinRPC.call(
                                SkinConversionRequest(
                                    playerId = event.uniqueId,
                                    eaglerSkinData = eaglerProfile.skinABGR864x64Base64Encoded
                                )
                            )
                        }
                    }

                profile.removeProperty("textures")
                profile.setProperty(ProfileProperty(
                    "textures",
                    cachedSkin.skinValue,
                    cachedSkin.skinSignature
                ))
            }
    }

    override fun handle(
        request: NewSkinAvailableRequest,
        context: RPCContext<Unit>
    )
    {
        val player = Bukkit.getPlayer(request.playerId)
            ?: return

        val profile = player.playerProfile
        profile.removeProperty("textures")
        profile.setProperty(ProfileProperty(
            "textures",
            request.skinValue,
            request.skinSignature
        ))

        Schedulers
            .sync()
            .run {
                player.playerProfile = profile
            }
    }
}
