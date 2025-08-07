package mc.arch.skin.bridge.rpc

import java.util.UUID

/**
 * @author Subham
 * @since 8/6/25
 */
data class NewSkinAvailableRequest(
    val playerId: UUID,
    val skinValue: String,
    val skinSignature: String
)
