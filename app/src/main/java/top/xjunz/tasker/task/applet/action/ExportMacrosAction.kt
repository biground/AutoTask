/*
 * Copyright (c) 2024 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.action

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import top.xjunz.tasker.engine.applet.action.Action
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.dto.XTaskDTO
import top.xjunz.tasker.engine.dto.XTaskJson
import top.xjunz.tasker.engine.dto.toDTO
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.bridge.ContextBridge
import top.xjunz.tasker.task.storage.TaskStorage
import android.os.Environment
import java.io.File
import java.io.IOException

/**
 * 导出所有宏到指定 JSON 文件。
 *
 * @author autopilot
 */
class ExportMacrosAction : Action() {

    override suspend fun apply(runtime: TaskRuntime): AppletResult {
        val filePath = getArgument(0, runtime) as? String
            ?: return AppletResult.EMPTY_FAILURE

        // 安全: path traversal 防护 — 基于 canonicalPath 白名单验证
        val file = File(filePath)
        val canonicalPath = try {
            file.canonicalPath
        } catch (_: IOException) {
            return AppletResult.EMPTY_FAILURE
        }
        val allowedDirs = listOf(
            ContextBridge.getContext().getExternalFilesDir(null)?.canonicalPath,
            Environment.getExternalStorageDirectory().canonicalPath
        ).filterNotNull()
        if (allowedDirs.none { canonicalPath.startsWith(it) }) {
            return AppletResult.EMPTY_FAILURE
        }

        return try {
            withContext(Dispatchers.IO) {
                val tasks = TaskStorage.getAllTasks()
                val dtoList: List<XTaskDTO> = tasks.map { it.toDTO() }
                val json = XTaskJson.encodeToString(dtoList)

                val outFile = File(filePath)
                outFile.parentFile?.let { parent ->
                    if (!parent.exists()) parent.mkdirs()
                }
                outFile.writeText(json, Charsets.UTF_8)
            }
            AppletResult.EMPTY_SUCCESS
        } catch (_: IOException) {
            AppletResult.EMPTY_FAILURE
        } catch (_: SecurityException) {
            AppletResult.EMPTY_FAILURE
        }
    }
}
