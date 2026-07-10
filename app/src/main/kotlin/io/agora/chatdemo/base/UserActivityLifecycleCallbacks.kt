package io.agora.chatdemo.base

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * Used for maintenance Activity lifecycle
 */
class UserActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks, ActivityState {
    override val activityList: MutableList<Activity> = ArrayList()
    private val resumeActivity: MutableList<Activity> = ArrayList()
    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        Log.e("ActivityLifecycle", "onActivityCreated " + activity.localClassName)
        activityList.add(0, activity)
    }

    override fun onActivityStarted(activity: Activity) {
        Log.e("ActivityLifecycle", "onActivityStarted " + activity.localClassName)
    }

    override fun onActivityResumed(activity: Activity) {
        Log.e(
            "ActivityLifecycle",
            "onActivityResumed activity's taskId = " + activity.taskId + " name: " + activity.localClassName
        )
        if (!resumeActivity.contains(activity)) {
            resumeActivity.add(activity)
            if (resumeActivity.size == 1) {
                //do nothing
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {
        Log.e("ActivityLifecycle", "onActivityPaused " + activity.localClassName)
    }

    override fun onActivityStopped(activity: Activity) {
        Log.e("ActivityLifecycle", "onActivityStopped " + activity.localClassName)
        resumeActivity.remove(activity)
        if (resumeActivity.isEmpty()) {
            Log.e("ActivityLifecycle", "在后台了")
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
        Log.e("ActivityLifecycle", "onActivitySaveInstanceState " + activity.localClassName)
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.e("ActivityLifecycle", "onActivityDestroyed " + activity.localClassName)
        activityList.remove(activity)
    }

    override fun current(): Activity? {
        return if (activityList.size > 0) activityList[0] else null
    }

    override fun count(): Int {
        return activityList.size
    }

    override val isFront: Boolean
        get() = resumeActivity.size > 0

    /**
     * 跳转到目标activity
     * @param cls
     */
    fun skipToTarget(cls: Class<*>?) {
        if (activityList.size > 0) {
            current()?.startActivity(Intent(current(), cls))
            for (activity in activityList) {
                activity.finish()
            }
        }
    }

    /**
     * finish target activity
     * @param cls
     */
    fun finishTarget(cls: Class<*>) {
        if (activityList.isNotEmpty()) {
            for (activity in activityList) {
                if (activity.javaClass == cls) {
                    activity.finish()
                }
            }
        }
    }

    val isOnForeground: Boolean
        /**
         * 判断app是否在前台
         * @return
         */
        get() = resumeActivity.isNotEmpty()


}