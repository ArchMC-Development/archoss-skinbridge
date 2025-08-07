package mc.arch.skin.bridge

import java.util.UUID

data class SkinConversionTask(
    val playerId: UUID,
    val id: String,
    val base64Data: String,
    val submittedAt: Long = System.currentTimeMillis()
)
