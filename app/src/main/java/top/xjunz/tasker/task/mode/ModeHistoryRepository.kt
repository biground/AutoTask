package top.xjunz.tasker.task.mode

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

/**
 * 模式激活历史记录。
 *
 * 记录每个模式最后一次激活的时间戳，支持查询"今天是否激活过"。
 * MVP 使用内存实现；后续可接入 DataStore 持久化。
 */
class ModeHistoryRepository private constructor(
    private val clock: Clock = Clock.systemDefaultZone()
) {

    companion object {
        @Volatile
        var instance: ModeHistoryRepository? = null
            private set

        fun initialize(): ModeHistoryRepository {
            return getOrInitialize()
        }

        @Synchronized
        fun getOrInitialize(): ModeHistoryRepository {
            return instance ?: ModeHistoryRepository().also { instance = it }
        }

        /** 测试工厂方法，可注入固定时钟 */
        fun createForTest(clock: Clock = Clock.systemDefaultZone()): ModeHistoryRepository {
            return ModeHistoryRepository(clock)
        }
    }

    /** key=modeName, value=最后激活时间戳(ms) */
    private val activationHistory = ConcurrentHashMap<String, Long>()

    /** 记录模式激活 */
    fun recordActivation(modeName: String, timestamp: Long = clock.millis()) {
        activationHistory[modeName] = timestamp
    }

    /** 查询今天是否激活过指定模式 */
    fun wasActivatedToday(modeName: String): Boolean {
        val lastActivation = activationHistory[modeName] ?: return false
        val activationDate = Instant.ofEpochMilli(lastActivation)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val today = LocalDate.now(clock)
        return activationDate == today
    }

    /** 获取最后激活时间戳 */
    fun getLastActivationTime(modeName: String): Long? {
        return activationHistory[modeName]
    }
}
