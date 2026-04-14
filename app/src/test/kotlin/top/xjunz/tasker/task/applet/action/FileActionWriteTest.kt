package top.xjunz.tasker.task.applet.action

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * FileAction ACTION_WRITE 写入动作单元测试
 * 验证: 路径安全检查、覆盖写入、追加写入
 */
class FileActionWriteTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var allowedDir: File

    @Before
    fun setUp() {
        allowedDir = tempFolder.newFolder("safe")
    }

    // ---- 路径安全检查 ----

    @Test
    fun `validateWritePath 拒绝白名单外的路径`() {
        val outsidePath = "/etc/passwd"
        val result = FileAction.validateWritePath(outsidePath, listOf(allowedDir.absolutePath))
        assertFalse("白名单外路径应被拒绝", result)
    }

    @Test
    fun `validateWritePath 拒绝包含路径遍历的路径`() {
        // 构造一个用 ".." 试图逃逸白名单目录的路径
        val traversalPath = File(allowedDir, "../../../etc/passwd").path
        val result = FileAction.validateWritePath(traversalPath, listOf(allowedDir.absolutePath))
        assertFalse("路径遍历应被拒绝", result)
    }

    @Test
    fun `validateWritePath 接受白名单内的合法路径`() {
        val validPath = File(allowedDir, "output.txt").absolutePath
        val result = FileAction.validateWritePath(validPath, listOf(allowedDir.absolutePath))
        assertTrue("白名单内路径应被接受", result)
    }

    @Test
    fun `validateWritePath 使用 canonicalPath 解析符号链接后仍在白名单内`() {
        val subDir = File(allowedDir, "sub")
        subDir.mkdirs()
        val validPath = File(subDir, "test.txt").absolutePath
        val result = FileAction.validateWritePath(validPath, listOf(allowedDir.absolutePath))
        assertTrue("子目录路径应被接受", result)
    }

    // ---- 覆盖写入 ----

    @Test
    fun `writeToFile 覆盖模式 写入新文件`() {
        val target = File(allowedDir, "new.txt")
        FileAction.writeToFile(target.absolutePath, "hello", FileAction.WRITE_MODE_OVERWRITE)
        assertEquals("hello", target.readText())
    }

    @Test
    fun `writeToFile 覆盖模式 覆盖已有内容`() {
        val target = File(allowedDir, "existing.txt")
        target.writeText("old content")

        FileAction.writeToFile(target.absolutePath, "new content", FileAction.WRITE_MODE_OVERWRITE)
        assertEquals("new content", target.readText())
    }

    // ---- 追加写入 ----

    @Test
    fun `writeToFile 追加模式 追加到已有内容`() {
        val target = File(allowedDir, "append.txt")
        target.writeText("line1")

        FileAction.writeToFile(target.absolutePath, "\nline2", FileAction.WRITE_MODE_APPEND)
        assertEquals("line1\nline2", target.readText())
    }

    @Test
    fun `writeToFile 追加模式 空文件追加`() {
        val target = File(allowedDir, "empty.txt")
        target.createNewFile()

        FileAction.writeToFile(target.absolutePath, "first", FileAction.WRITE_MODE_APPEND)
        assertEquals("first", target.readText())
    }

    // ---- 自动创建父目录 ----

    @Test
    fun `writeToFile 自动创建不存在的父目录`() {
        val target = File(allowedDir, "deep/nested/file.txt")

        FileAction.writeToFile(target.absolutePath, "deep write", FileAction.WRITE_MODE_OVERWRITE)
        assertTrue("文件应存在", target.exists())
        assertEquals("deep write", target.readText())
    }
}
