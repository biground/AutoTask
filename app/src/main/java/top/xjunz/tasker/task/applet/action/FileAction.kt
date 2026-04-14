/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.action

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.xjunz.tasker.engine.applet.action.Action
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.Referent
import top.xjunz.tasker.engine.runtime.TaskRuntime
import java.io.File

/**
 * @author xjunz 2023/10/15
 */
class FileAction(val action: Int) : Action(), Referent {

    companion object {
        const val ACTION_DELETE = 0
        const val ACTION_COPY = 1
        const val ACTION_RENAME = 2
        const val ACTION_WRITE = 3

        /** 写入模式：覆盖 */
        const val WRITE_MODE_OVERWRITE = 0
        /** 写入模式：追加 */
        const val WRITE_MODE_APPEND = 1

        /**
         * 验证写入路径是否在白名单目录内。
         * 使用 canonicalPath 规范化以防止路径遍历攻击。
         */
        fun validateWritePath(filePath: String, allowedDirPrefixes: List<String>): Boolean {
            val canonical = File(filePath).canonicalPath
            return allowedDirPrefixes.any { prefix ->
                val canonicalPrefix = File(prefix).canonicalPath
                canonical.startsWith(canonicalPrefix + File.separator) || canonical == canonicalPrefix
            }
        }

        /**
         * 写入文件内容，自动创建父目录。
         * @param filePath 目标文件路径
         * @param content 写入内容
         * @param mode WRITE_MODE_OVERWRITE(覆盖) 或 WRITE_MODE_APPEND(追加)
         */
        fun writeToFile(filePath: String, content: String, mode: Int) {
            val file = File(filePath)
            file.parentFile?.mkdirs()
            when (mode) {
                WRITE_MODE_OVERWRITE -> file.writeText(content)
                WRITE_MODE_APPEND -> file.appendText(content)
                else -> throw IllegalArgumentException("未知写入模式: $mode")
            }
        }
    }

    private lateinit var firstFilePath: String

    private lateinit var secondFilePath: String

    private var writeContent: String = ""

    private var writeMode: Int = WRITE_MODE_OVERWRITE

    override fun onPreApply(runtime: TaskRuntime) {
        super.onPreApply(runtime)
        firstFilePath = getArgument(0, runtime) as String
        if (action == ACTION_COPY || action == ACTION_RENAME) {
            secondFilePath = getArgument(1, runtime) as String
        }
        if (action == ACTION_WRITE) {
            writeContent = getArgument(1, runtime) as String
            writeMode = getArgument(2, runtime) as Int
        }
        runtime.registerReferent(this)
    }

    override suspend fun apply(runtime: TaskRuntime): AppletResult {
        when (action) {
            ACTION_DELETE -> withContext(Dispatchers.IO) {
                File(firstFilePath).deleteRecursively()
            }

            ACTION_COPY -> withContext(Dispatchers.IO) {
                File(firstFilePath).copyRecursively(File(secondFilePath))
            }

            ACTION_RENAME -> withContext(Dispatchers.IO) {
                val destFile = File(secondFilePath)
                var dest = destFile
                if (destFile.exists() && destFile.isDirectory) {
                    dest = File(destFile, File(firstFilePath).name)
                }
                File(firstFilePath).renameTo(dest)
            }

            ACTION_WRITE -> withContext(Dispatchers.IO) {
                writeToFile(firstFilePath, writeContent, writeMode)
            }
        }
        return AppletResult.EMPTY_SUCCESS
    }


    override fun getReferredValue(which: Int, runtime: TaskRuntime) {
        when (which) {
            0 -> firstFilePath
            1 -> secondFilePath
        }
    }

}