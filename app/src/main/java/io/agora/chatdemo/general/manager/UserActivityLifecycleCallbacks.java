package io.agora.chatdemo.general.manager;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.callkit.general.EaseCallFloatWindow;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.callbacks.ActivityState;
import io.agora.chatdemo.sign.SplashActivity;
import io.agora.util.EMLog;

public class UserActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks, ActivityState {
    private List<Activity> activityList=new ArrayList<>();
    private List<Activity> resumeActivity=new ArrayList<>();

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        EMLog.i("ActivityLifecycle", "onActivityCreated "+activity.getLocalClassName());
        activityList.add(0, activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        EMLog.i("ActivityLifecycle", "onActivityStarted "+activity.getLocalClassName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        EMLog.i("ActivityLifecycle", "onActivityResumed activity's taskId = "+activity.getTaskId() + " name: "+activity.getLocalClassName());
        if (!resumeActivity.contains(activity)) {
            resumeActivity.add(activity);
            if(resumeActivity.size() == 1) {
                //do nothing
            }
            restartSingleInstanceActivity(activity);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        EMLog.i("ActivityLifecycle", "onActivityPaused "+activity.getLocalClassName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        EMLog.i("ActivityLifecycle", "onActivityStopped "+activity.getLocalClassName());
        resumeActivity.remove(activity);
        if(resumeActivity.isEmpty()) {
            Activity a = getOtherTaskSingleInstanceActivity(activity.getTaskId());
            if(isTargetSingleInstance(a) && !EaseCallFloatWindow.getInstance().isShowing()) {
                makeTaskToFront(a);
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        EMLog.i("ActivityLifecycle", "onActivitySaveInstanceState "+activity.getLocalClassName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        EMLog.i("ActivityLifecycle", "onActivityDestroyed "+activity.getLocalClassName());
        activityList.remove(activity);
    }

    @Override
    public Activity current() {
        return activityList.size()>0 ? activityList.get(0):null;
    }

    @Override
    public List<Activity> getActivityList() {
        return activityList;
    }

    @Override
    public int count() {
        return activityList.size();
    }

    @Override
    public boolean isFront() {
        return resumeActivity.size() > 0;
    }

    /**
     * Jump to target activity
     * @param cls
     */
    public void skipToTarget(Class<?> cls) {
        if(activityList != null && activityList.size() > 0) {
            current().startActivity(new Intent(current(), cls));
            for (Activity activity : activityList) {
                activity.finish();
            }
        }

    }

    /**
     * finish target activity
     * @param cls
     */
    public void finishTarget(Class<?> cls) {
        if(activityList != null && !activityList.isEmpty()) {
            for (Activity activity : activityList) {
                if(activity.getClass() == cls) {
                    activity.finish();
                }
            }
        }
    }

    /**
     * Determine whether the app is in the foreground
     * @return
     */
    public boolean isOnForeground() {
        return resumeActivity != null && !resumeActivity.isEmpty();
    }


    private void restartSingleInstanceActivity(Activity activity) {
        boolean isClickByFloat = activity.getIntent().getBooleanExtra("isClickByFloat", false);
        if(isClickByFloat) {
            return;
        }
        //Just launched, or return to the app from the desktop
        if(resumeActivity.size() == 1 && resumeActivity.get(0) instanceof SplashActivity) {
            return;
        }
        //At least two activities in the activityList are required
        if(resumeActivity.size() >= 1 && activityList.size() > 1) {
            Activity a = getOtherTaskSingleInstanceActivity(resumeActivity.get(0).getTaskId());
            if(a != null && !a.isFinishing() //not finishing
                    && a != activity //The current activity is not the same as the first activity in the list
                    && a.getTaskId() != activity.getTaskId()
                    && !EaseCallFloatWindow.getInstance().isShowing()
            ){
                EMLog.i("ActivityLifecycle", "start up activity = "+a.getClass().getName());
                activity.startActivity(new Intent(activity, a.getClass()));
            }
        }
    }


    private Activity getOtherTaskSingleInstanceActivity(int taskId) {
        if(taskId != 0 && activityList.size() > 1) {
            for (Activity activity : activityList) {
                if(activity.getTaskId() != taskId) {
                    if(isTargetSingleInstance(activity)) {
                        return activity;
                    }
                }
            }
        }
        return null;
    }

    private boolean isTargetSingleInstance(Activity activity) {
        if(activity == null) {
            return false;
        }
        CharSequence title = activity.getTitle();
        if(TextUtils.equals(title, activity.getString(R.string.demo_activity_label_video_call))
                || TextUtils.equals(title, activity.getString(R.string.demo_activity_label_multi_call))) {
            return true;
        }
        return false;
    }

    private void makeTaskToFront(Activity activity) {
        EMLog.i("ActivityLifecycle", "makeTaskToFront activity: "+activity.getLocalClassName());
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        manager.moveTaskToFront(activity.getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
    }

}
