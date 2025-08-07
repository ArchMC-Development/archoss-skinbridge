package mc.arch.skin.bridge

/**
 * @author Subham
 * @since 8/6/25
 */
fun SkinConversionAgent.withQueue(
    maxConcurrentJobs: Int = 5
): SkinConversionQueue {
    return SkinConversionQueue(this, maxConcurrentJobs)
}
