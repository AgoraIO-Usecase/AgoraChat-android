package io.agora.chatdemo.general.dialog;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;


class DialogViewHelper {
    private final Context mContext;
    private final View mContentView;
    private SparseArray<WeakReference<View>> views = new SparseArray<>();//使用若引用，防止内存泄露


    public DialogViewHelper(Context mContext, View contentView) {
        this.mContext = mContext;
        this.mContentView = contentView;
    }

    public DialogViewHelper(Context mContext, int contentViewId) {
        this.mContext = mContext;
        this.mContentView = LayoutInflater.from(mContext).inflate(contentViewId, null);
    }

    /**
     * 设置点击事件
     * @param viewId
     * @param onClickListener
     */
    public void setOnClickListener(int viewId, View.OnClickListener onClickListener) {
        View view = getViewById(viewId);
        view.setOnClickListener(onClickListener);
    }

    <T extends View> T getViewById(int viewId) {
        View view = null;
        WeakReference<View> weakReference = views.get(viewId);
        if (weakReference != null) {
            view = weakReference.get();
        } else {
            view = mContentView.findViewById(viewId);
            views.put(viewId, new WeakReference<View>(view));
        }
        return (T) view;

    }

    /**
     * 设置文本
     * @param viewId
     * @param text
     */
    public void setText(int viewId, CharSequence text) {
        TextView view = getViewById(viewId);
        if (view != null) {
            view.setText(text);
            //设置超链接可点击
            view.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    /**
     * @return dialog的布局view
     */
    public View getContentView() {
        return mContentView;
    }

    /**
     * 设置图片
     * @param viewId
     * @param resId
     */
    public void setImageView(int viewId, int resId) {
        ImageView imageView = getViewById(viewId);
        if(imageView!=null) {
            imageView.setImageResource(resId);
        }
    }
}
