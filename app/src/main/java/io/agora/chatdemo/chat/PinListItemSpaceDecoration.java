package io.agora.chatdemo.chat;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class PinListItemSpaceDecoration extends RecyclerView.ItemDecoration {
   private int space;

   public PinListItemSpaceDecoration(int space) {
      this.space = space;
   }

   @Override
   public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
      outRect.left = space;
      outRect.right = space;
      outRect.bottom = space;


      if (parent.getChildAdapterPosition(view) == 0) {
         outRect.top = space;
      } else {
         outRect.top = 0;
      }
   }
}

