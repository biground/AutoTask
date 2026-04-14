/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.action

import android.media.MediaPlayer
import top.xjunz.tasker.engine.applet.action.SingleArgAction
import top.xjunz.tasker.engine.applet.base.AppletResult
import top.xjunz.tasker.engine.runtime.TaskRuntime
import java.io.File
import java.io.IOException

/**
 * 播放指定路径的音频文件。
 * 采用 fire-and-forget 模式：start() 后立即返回，播放完成后自动释放 MediaPlayer。
 */
class PlaySoundAction : SingleArgAction<String>() {

    override suspend fun doAction(arg: String?, runtime: TaskRuntime): AppletResult {
        val filePath = arg ?: return AppletResult.EMPTY_FAILURE

        // 安全: 路径校验 — 禁止 path traversal，文件必须存在
        if (filePath.contains("..") || !File(filePath).exists()) {
            return AppletResult.EMPTY_FAILURE
        }

        return try {
            val player = MediaPlayer()
            player.setOnCompletionListener { it.release() }
            player.setOnErrorListener { mp, _, _ ->
                mp.release()
                true
            }
            player.setDataSource(filePath)
            player.prepare()
            player.start()
            AppletResult.EMPTY_SUCCESS
        } catch (_: IOException) {
            AppletResult.EMPTY_FAILURE
        } catch (_: IllegalStateException) {
            AppletResult.EMPTY_FAILURE
        }
    }
}
