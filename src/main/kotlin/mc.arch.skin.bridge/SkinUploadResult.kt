package mc.arch.skin.bridge

import org.mineskin.data.Skin

data class SkinUploadResult(
    val skinInfo: Skin?,
    val textureValue: String?,
    val textureSignature: String?,
    val skinUuid: String?,
    val error: String?
)
