/*
 * Copyright (c) 2023 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.option.registry

import top.xjunz.tasker.R
import top.xjunz.tasker.bridge.NotificationManagerBridge
import top.xjunz.tasker.bridge.OverlayDialogBridge
import top.xjunz.tasker.engine.applet.action.createAction
import top.xjunz.tasker.engine.applet.action.createProcessor
import top.xjunz.tasker.task.applet.anno.AppletOrdinal

/**
 * 界面相关动作的 Registry。
 */
class UiActionRegistry(id: Int) : AppletOptionRegistry(id) {

    @AppletOrdinal(0x0000)
    val displayNotification = appletOption(R.string.display_notification) {
        createProcessor<Boolean> { args, _ ->
            val title = args[0] as? String ?: ""
            val content = args[1] as? String ?: ""
            NotificationManagerBridge.showNotification(title, content)
            true
        }
    }.withValueArgument<String>(R.string.notification_title)
        .withValueArgument<String>(R.string.notification_content)
        .hasCompositeTitle()

    @AppletOrdinal(0x0001)
    val displayDialog = appletOption(R.string.display_dialog) {
        createAction { args, _ ->
            val title = args[0] as? String ?: ""
            val content = args[1] as? String ?: ""
            val timeout = args[2] as? Int ?: 0
            OverlayDialogBridge.showDialog(title, content, timeout)
            true
        }
    }.withValueArgument<String>(R.string.dialog_title)
        .withValueArgument<String>(R.string.dialog_content)
        .withValueArgument<Int>(R.string.dialog_timeout)
        .hasCompositeTitle()
}
