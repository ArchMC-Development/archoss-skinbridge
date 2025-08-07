package mc.arch.skin.bridge

import dev.cubxity.plugins.metrics.api.metric.collector.Collector
import dev.cubxity.plugins.metrics.api.metric.collector.CollectorCollection
import dev.cubxity.plugins.metrics.api.metric.data.GaugeMetric
import dev.cubxity.plugins.metrics.api.metric.data.Metric
import gg.scala.commons.metrics.Metrics
import gg.scala.commons.metrics.TimeAverage
import gg.scala.commons.metrics.Tracker

@Metrics
object SkinConversionMetrics : CollectorCollection
{
    val completedTasks = Tracker(
        name = "skin_conversion_completed_tasks"
    )

    val failedTasks = Tracker(
        name = "skin_conversion_failed_tasks"
    )

    val averageProcessingTime = TimeAverage(
        name = "skin_conversion_average_processing_time"
    )

    override val collectors = listOf(
        object : Collector
        {
            override fun collect() = listOf(
                GaugeMetric(
                    "skin_conversion_active_tasks",
                    mapOf(),
                    SkinConversionDataSync.toQueue().getActiveJobCount()
                ),
                GaugeMetric(
                    "skin_conversion_queued_tasks",
                    mapOf(),
                    SkinConversionDataSync.toQueue().getQueueSize()
                )
            )
        },
        completedTasks,
        failedTasks,
        averageProcessingTime
    )
}
