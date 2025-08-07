package mc.arch.skin.bridge

data class SkinConversionConfig(
    var mineSkinApiKey: String = "",
    var userAgent: String = "EaglercraftSkinBridge/v1.0",
    var defaultWidth: Int = 64,
    var defaultHeight: Int = 64,
    var maxConcurrentJobs: Int = 5,
    var cacheEnabled: Boolean = true,
    var skinCacheTtlMinutes: Long = 1440 * 7, // Cache skin responses for 1w
    var pngCacheTtlMinutes: Long = 1440 * 7, // Cache PNG files for 1w
    var fallbackHashEnabled: Boolean = true
)
