package io.agora.chatdemo.global;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public interface BottomSheetContainerHelper {
    void startFragment(@NonNull Fragment fragment, @Nullable String tag);
    void hide();
    void back();
    void changeNextColor(boolean isChange);
}
