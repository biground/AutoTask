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
import top.xjunz.tasker.task.storage.TaskStorage
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

        // 安全: 验证文件路径不含 path traversal
        if (filePath.contains("..")) {
            return AppletResult.EMPTY_FAILURE
        }

        return try {
            withContext(Dispatchers.IO) {
                val tasks = TaskStorage.getAllTasks()
                val dtoList: List<XTaskDTO> = tasks.map { it.toDTO() }
                val json = XTaskJson.encodeToString(dtoList)

                val file = File(filePath)
                file.parentFile?.let { parent ->
                    if (!parent.exists()) parent.mkdirs()
                }
                file.writeText(json, Charsets.UTF_8)
            }
            AppletResult.EMPTY_SUCCESS
        } catch (_: IOException) {
            AppletResult.EMPTY_FAILURE
        } catch (_: SecurityException) {
            AppletResult.EMPTY_FAILURE
        }
    }
}
