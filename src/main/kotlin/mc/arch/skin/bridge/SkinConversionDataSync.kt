package mc.arch.skin.bridge

import gg.scala.commons.consensus.elections.LeaderElection
import gg.scala.commons.persist.datasync.DataSyncKeys
import gg.scala.commons.persist.datasync.DataSyncService
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import mc.arch.skin.bridge.rpc.SkinConversionRPC
import mc.arch.skin.bridge.rpc.handler.SkinConversionHandler
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * @author Subham
 * @since 8/6/25
 */
@Service
object SkinConversionDataSync : DataSyncService<SkinConversionConfig>()
{
    data object SkinConversionKeys : DataSyncKeys
    {
        override fun newStore() = "skin-conversion"

        override fun store() = keyOf("conversion", "store")
        override fun sync() = keyOf("conversion", "sync")
    }

    private var acceptingSubmissions = false
    fun acceptingRequests() = acceptingSubmissions

    @Configure
    fun configure()
    {
        LeaderElection.withAnyServerSyncServer(
            electionID = "skin-conversion-agent",
            elect = {
                acceptingSubmissions = true
            },
            resign = {
                acceptingSubmissions = false
            }
        )

        SkinConversionRPC.convertSkinRPC.addHandler(SkinConversionHandler())
    }

    private lateinit var agent: SkinConversionAgent
    private lateinit var queue: SkinConversionQueue
    override fun postReload()
    {
        agent = SkinConversionAgent(config = cached())
        if (::queue.isInitialized)
        {
            queue.shutdown()
        }

        queue = agent.withQueue(maxConcurrentJobs = cached().maxConcurrentJobs)
    }

    fun toQueue() = queue
    fun toAgent() = agent

    fun submitTask(
        playerId: UUID,
        base64Data: String,
        taskId: String = SkinConversionQueue.Companion.generateTaskId()
    ): CompletableFuture<TaskResult> = queue.submitTask(
        playerId,
        base64Data,
        taskId
    )

    override fun keys() = SkinConversionKeys
    override fun type() = SkinConversionConfig::class.java
}
