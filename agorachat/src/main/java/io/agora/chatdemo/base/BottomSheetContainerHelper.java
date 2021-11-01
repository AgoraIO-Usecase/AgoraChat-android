package io.agora.chatdemo.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by 许成谱 on 2021/10/30 0030 6:45.
 * qq:1550540124
 */
public interface BottomSheetContainerHelper {
    void startFragment(@NonNull Fragment fragment, @Nullable String tag);
}
