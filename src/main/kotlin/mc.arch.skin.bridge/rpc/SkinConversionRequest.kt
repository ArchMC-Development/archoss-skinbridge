package mc.arch.skin.bridge.rpc

import java.util.UUID

/**
 * @author Subham
 * @since 8/6/25
 */
data class SkinConversionRequest(
    val playerId: UUID,
    val eaglerSkinData: String
)
