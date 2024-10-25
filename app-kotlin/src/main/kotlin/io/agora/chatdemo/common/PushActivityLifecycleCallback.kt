package io.agora.chatdemo.common

import android.app.Activity
import android.app.Application
import android.os.Bundle
import io.agora.chatdemo.MainActivity

class PushActivityLifecycleCallback: Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // When MainActivity is created, get the push token and send it to the server
        if (activity is MainActivity) {
            PushManager.getPushTokenAndSend(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {
        
    }

    override fun onActivityStopped(activity: Activity) {
        
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        
    }

    override fun onActivityDestroyed(activity: Activity) {
        
    }
}