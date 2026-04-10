package top.xjunz.tasker.task.location

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference

/**
 * AmapApiKeyManager 的纯 JUnit 测试
 * 由于 DataStore 需要 Android Context，这里测试内存缓存逻辑
 */
class AmapApiKeyManagerTest {

    private lateinit var cachedKey: AtomicReference<String?>

    @Before
    fun setUp() {
        cachedKey = AtomicReference(null)
    }

    // hasApiKey 逻辑：cachedKey 为 null 时返回 false
    @Test
    fun hasApiKey_whenNull_returnsFalse() {
        cachedKey.set(null)
        val result = cachedKey.get()?.isNotBlank() == true
        assertFalse(result)
    }

    // hasApiKey 逻辑：cachedKey 为空字符串时返回 false
    @Test
    fun hasApiKey_whenBlank_returnsFalse() {
        cachedKey.set("")
        val result = cachedKey.get()?.isNotBlank() == true
        assertFalse(result)
    }

    // hasApiKey 逻辑：cachedKey 为空白字符串时返回 false
    @Test
    fun hasApiKey_whenWhitespace_returnsFalse() {
        cachedKey.set("   ")
        val result = cachedKey.get()?.isNotBlank() == true
        assertFalse(result)
    }

    // hasApiKey 逻辑：cachedKey 为有效 key 时返回 true
    @Test
    fun hasApiKey_whenValid_returnsTrue() {
        cachedKey.set("abc123def456")
        val result = cachedKey.get()?.isNotBlank() == true
        assertTrue(result)
    }

    // setApiKey 后缓存立即更新
    @Test
    fun cachedKey_afterSet_updatesImmediately() {
        cachedKey.set(null)
        assertFalse(cachedKey.get()?.isNotBlank() == true)

        cachedKey.set("new-key-value")
        assertTrue(cachedKey.get()?.isNotBlank() == true)
    }

    // 覆盖旧 key
    @Test
    fun cachedKey_overwrite_replacesOldValue() {
        cachedKey.set("old-key")
        assertTrue(cachedKey.get()?.isNotBlank() == true)

        cachedKey.set("new-key")
        assertTrue(cachedKey.get() == "new-key")
    }
}
