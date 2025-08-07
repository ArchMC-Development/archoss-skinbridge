package mc.arch.skin.bridge

data class TaskResult(
    val task: SkinConversionTask,
    val result: SkinUploadResult?,
    val processingTimeMs: Long,
    val completedAt: Long = System.currentTimeMillis(),
    val error: Throwable? = null
)
