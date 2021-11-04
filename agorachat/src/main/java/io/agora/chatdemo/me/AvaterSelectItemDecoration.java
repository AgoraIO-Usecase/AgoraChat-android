package io.agora.chatdemo.me;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

/**
 * Created by 许成谱 on 2021/11/4 0004 20:18.
 * qq:1550540124
 * 热爱生活每一天
 */
public class AvaterSelectItemDecoration extends RecyclerView.ItemDecoration {
    private int space;
    public AvaterSelectItemDecoration(int space) {
        this.space=space;
    }

    @Override
    public void getItemOffsets(@NonNull @NotNull Rect outRect, @NonNull @NotNull View view, @NonNull @NotNull RecyclerView parent, @NonNull @NotNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.top = space * 2;
        if (parent.getChildLayoutPosition(view) % 2 == 0) {
            outRect.left = space * 2;
            outRect.right = space;
        } else {
            outRect.left = space;
            outRect.right = space * 2;
        }
    }
}
