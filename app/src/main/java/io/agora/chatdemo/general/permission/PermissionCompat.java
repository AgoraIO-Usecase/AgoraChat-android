package io.agora.chatdemo.general.permission;

import android.Manifest;
import android.content.Context;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionCompat {

    public enum StorageAccess {
        Full, Partial, Denied
    }

    /**
     * Check media permission, such as READ_EXTERNAL_STORAGE, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO.
     * Note: Do not check other permissions via this method.
     * @param context
     * @param launcher
     * @param permissions
     * @return
     */
    public static boolean checkMediaPermission(Context context, ActivityResultLauncher<String[]> launcher, String... permissions) {
        if(context == null || permissions == null) {
            return false;
        }
        List<String> permissionList = new ArrayList<>();
        permissionList.addAll(Arrays.asList(permissions));
        String[] permissionArray;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            permissionList.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
            permissionArray = permissionList.toArray(new String[0]);
        }else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            permissionArray = permissionList.toArray(new String[0]);
        }else {
            permissionList.clear();
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissionArray = permissionList.toArray(new String[0]);
        }
        if(!PermissionsManager.getInstance().hasAllPermissions(context, permissionArray)) {
            if(launcher != null) {
                launcher.launch(permissionArray);
            }
            return false;
        }
        return true;
    }

    /**
     * Get media access status.
     * @param context
     * @return
     */
    public static StorageAccess getMediaAccess(Context context) {
        if (PermissionsManager.getInstance().hasPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ||
                PermissionsManager.getInstance().hasPermission(context, Manifest.permission.READ_MEDIA_VIDEO)) {
            // Full access on Android 13+
            return StorageAccess.Full;
        }else if(PermissionsManager.getInstance().hasPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)) {
            // Partial access on Android 13+
            return StorageAccess.Partial;
        }else if(PermissionsManager.getInstance().hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Full access up to Android 12
            return StorageAccess.Full;
        }else {
            return StorageAccess.Denied;
        }
    }


}
