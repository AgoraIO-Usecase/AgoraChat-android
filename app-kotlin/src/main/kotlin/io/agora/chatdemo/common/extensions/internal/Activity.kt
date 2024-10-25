package io.agora.chatdemo.common.extensions.internal

import android.app.Activity
import android.app.ActivityManager
import android.content.Context

internal fun Activity.makeTaskToFront() {
    (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
        ?.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
}