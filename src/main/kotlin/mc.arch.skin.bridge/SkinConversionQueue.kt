package mc.arch.skin.bridge

import java.util.UUID
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class SkinConversionQueue(
    private val conversionService: SkinConversionAgent,
    private val maxConcurrentJobs: Int = 5
)
{
    private val executorService = ThreadPoolExecutor(
        maxConcurrentJobs,
        maxConcurrentJobs,
        60L, TimeUnit.SECONDS,
        LinkedBlockingQueue()
    ) { runnable ->
        Thread(runnable, "SkinConversion-${threadCounter.incrementAndGet()}").apply {
            isDaemon = true
        }
    }

    private val pendingTasks = LinkedBlockingQueue<Pair<SkinConversionTask, CompletableFuture<TaskResult>>>()
    private val activeTasks = ConcurrentHashMap<String, CompletableFuture<TaskResult>>()
    private val processorThread = Thread(::processQueue, "SkinQueue-Processor").apply {
        isDaemon = true
        start()
    }

    @Volatile
    private var isShutdown = false

    companion object
    {
        private val threadCounter = AtomicInteger(0)
        private val taskIdCounter = AtomicLong(0)

        fun generateTaskId(): String = "task-${taskIdCounter.incrementAndGet()}-${System.currentTimeMillis()}"
    }

    fun submitTask(
        playerId: UUID,
        base64Data: String,
        taskId: String = generateTaskId()
    ): CompletableFuture<TaskResult>
    {
        if (isShutdown)
        {
            return CompletableFuture.failedFuture(IllegalStateException("Queue is shutdown"))
        }

        val task = SkinConversionTask(
            id = taskId,
            base64Data = base64Data,
            playerId = playerId
        )

        val future = CompletableFuture<TaskResult>()

        pendingTasks.offer(task to future)
        activeTasks[task.id] = future
        return future
    }

    private fun processQueue()
    {
        while (!isShutdown)
        {
            runCatching {
                val (task, future) = pendingTasks.poll(1, TimeUnit.SECONDS)
                    ?: return@runCatching null

                if (executorService.activeCount >= maxConcurrentJobs)
                {
                    pendingTasks.offer(task to future)
                    Thread.sleep(100)
                    return@runCatching null
                }

                executorService.submit {
                    processTask(task, future)
                }
            }.onFailure { throwable ->
                throwable.printStackTrace()
            }
        }
    }

    /**
     * Process a single task
     */
    private fun processTask(task: SkinConversionTask, future: CompletableFuture<TaskResult>)
    {
        val startTime = System.currentTimeMillis()
        try
        {
            val uploadResult = conversionService
                .convertAndUploadToMineSkin(
                    base64Data = task.base64Data,
                )
                .join() // Block until complete

            val processingTime = System.currentTimeMillis() - startTime

            val taskResult = TaskResult(
                task = task,
                result = uploadResult,
                processingTimeMs = processingTime
            )

            // Update metrics
            SkinConversionMetrics.averageProcessingTime.trackTime(processingTime)

            if (uploadResult.error == null)
            {
                SkinConversionMetrics.completedTasks += 1
            } else
            {
                SkinConversionMetrics.failedTasks += 1
            }

            // Complete future and cleanup
            activeTasks.remove(task.id)
            future.complete(taskResult)
        } catch (exception: Exception)
        {
            val processingTime = System.currentTimeMillis() - startTime
            val taskResult = TaskResult(
                task = task,
                result = null,
                processingTimeMs = processingTime,
                error = exception
            )

            // Update metrics
            SkinConversionMetrics.failedTasks += 1
            SkinConversionMetrics.averageProcessingTime.trackTime(processingTime)

            exception.printStackTrace()

            activeTasks.remove(task.id)
            future.complete(taskResult)
        }
    }

    fun getQueueSize() = pendingTasks.size
    fun getActiveJobCount() = activeTasks.size

    fun shutdown()
    {
        isShutdown = true

        processorThread.interrupt()
        executorService.shutdownNow()

        activeTasks.values.forEach { future ->
            future.cancel(true)
        }
    }
}
