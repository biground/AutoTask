package top.xjunz.tasker.task.applet.criterion

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

/**
 * 执行频率计数器，记录任务执行时间戳，支持按时间窗口查询执行次数。
 *
 * MVP 使用内存实现；后续可接入 DataStore 持久化。
 */
class FrequencyCounterRepository private constructor(
    private val clockProvider: () -> Long = { System.currentTimeMillis() }
) {

    companion object {
        /** 时间窗口类型 */
        const val WINDOW_HOURLY = 0
        const val WINDOW_DAILY = 1
        const val WINDOW_WEEKLY = 2

        @Volatile
        var instance: FrequencyCounterRepository? = null
            private set

        fun initialize(): FrequencyCounterRepository {
            return FrequencyCounterRepository().also { instance = it }
        }

        /** 测试工厂方法，可注入时钟 */
        fun createForTest(clockProvider: () -> Long = { System.currentTimeMillis() }): FrequencyCounterRepository {
            return FrequencyCounterRepository(clockProvider)
        }
    }

    /** key=taskKey, value=执行时间戳列表 */
    private val records = ConcurrentHashMap<String, MutableList<Long>>()

    /** 记录一次执行 */
    fun recordExecution(taskKey: String) {
        val now = clockProvider()
        records.getOrPut(taskKey) { mutableListOf() }.add(now)
    }

    /** 获取指定时间窗口内的执行次数 */
    fun getExecutionCount(taskKey: String, windowType: Int): Int {
        val timestamps = records[taskKey] ?: return 0
        val windowStart = getWindowStart(windowType)
        return timestamps.count { it >= windowStart }
    }

    /** 清理超出 7 天的旧记录 */
    fun cleanup(taskKey: String) {
        val timestamps = records[taskKey] ?: return
        val cutoff = clockProvider() - 7L * 24 * 60 * 60 * 1000
        timestamps.removeAll { it < cutoff }
        if (timestamps.isEmpty()) {
            records.remove(taskKey)
        }
    }

    /** 计算时间窗口起始时间戳（使用 clockProvider 确定"现在"） */
    private fun getWindowStart(windowType: Int): Long {
        val nowMillis = clockProvider()
        val now = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(nowMillis),
            ZoneId.systemDefault()
        )
        val startOfWindow = when (windowType) {
            WINDOW_HOURLY -> now.withMinute(0).withSecond(0).withNano(0)
            WINDOW_DAILY -> now.toLocalDate().atStartOfDay()
            WINDOW_WEEKLY -> now.minusDays(7).withMinute(0).withSecond(0).withNano(0)
            else -> now.toLocalDate().atStartOfDay()
        }
        return startOfWindow.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
