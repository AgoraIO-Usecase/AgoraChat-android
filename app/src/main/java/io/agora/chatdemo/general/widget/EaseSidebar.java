/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.agora.chatdemo.general.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import io.agora.chatdemo.R;
import io.agora.util.EMLog;


/**
 * side bar
 */
public class EaseSidebar extends View{
	private static final int RESET = 1;
	private Paint paint;
	private float ItemHeight;
	private Context context;
	private OnTouchEventListener mListener;
	private String[] sections = new String[]{"A","B","C","D","E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z","#"};
	private String topText;
	private int mTextColor;
	private static final String DEFAULT_COLOR = "#8C8C8C";
	private static final float DEFAULT_TEXT_SIZE = 10;
	private float mTextSize;
	private int mBgColor;
	private int mWidth, mHeight;
	private float mTextCoefficient = 1;
	private int pointer = -1;
	private Paint selectedPaint;
	private int mFocusBgColor;
	private int delayDisappearTime;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case RESET:
					pointer = -1;
					invalidate();
			        break;
			}
		}
	};

	public EaseSidebar(Context context) {
		this(context, null);
	}

	public EaseSidebar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EaseSidebar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
		initAttrs(attrs);
		init();
	}

	private void initAttrs(AttributeSet attrs) {
		if(attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EaseSidebar);
			int topTextId = a.getResourceId(R.styleable.EaseSidebar_ease_side_bar_top_text, -1);
			if(topTextId != -1) {
				topText = context.getResources().getString(topTextId);
			}else {
				topText = a.getString(R.styleable.EaseSidebar_ease_side_bar_top_text);
			}
			int textColorId = a.getResourceId(R.styleable.EaseSidebar_ease_side_bar_text_color, -1);
			if(textColorId != -1) {
				mTextColor = ContextCompat.getColor(context, textColorId);
			}else {
				mTextColor = a.getColor(R.styleable.EaseSidebar_ease_side_bar_text_color, Color.parseColor(DEFAULT_COLOR));
			}
			int focusBgColor = a.getResourceId(R.styleable.EaseSidebar_ease_side_bar_focus_bg_color, -1);
			if(focusBgColor != -1) {
				mFocusBgColor = ContextCompat.getColor(context, focusBgColor);
			}else {
				mFocusBgColor = a.getColor(R.styleable.EaseSidebar_ease_side_bar_focus_bg_color, Color.TRANSPARENT);
			}
			delayDisappearTime = a.getInteger(R.styleable.EaseSidebar_ease_side_bar_delay_disappear_time, 500);
			mTextSize = a.getDimension(R.styleable.EaseSidebar_ease_side_bar_text_size, DEFAULT_TEXT_SIZE);
			int bgId = a.getResourceId(R.styleable.EaseSidebar_ease_side_bar_background, -1);
			if(bgId != -1) {
				mBgColor = ContextCompat.getColor(context, textColorId);
			}else {
				mBgColor = a.getColor(R.styleable.EaseSidebar_ease_side_bar_background, Color.TRANSPARENT);
			}
			int headArrays = a.getResourceId(R.styleable.EaseSidebar_ease_side_bar_head_arrays, -1);
			if(headArrays != -1) {
			    sections = getResources().getStringArray(headArrays);
			}else {
				sections = new String[]{"A","B","C","D","E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z","#"};
			}
		}
	}

	private void init(){
		if(sections.length > 27) {
		    if(!TextUtils.isEmpty(topText)) {
		        sections[0] = topText;
		    }
		}
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(mTextColor);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(mTextSize);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if(handler != null) {
		    handler.removeCallbacksAndMessages(null);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// get the view's height
		mWidth = w;
		mHeight = h;
		checkTextSize();
	}

	/**
	 * Verify that the text size is appropriate
	 */
	private void checkTextSize() {
		if(paint != null) {
			Paint.FontMetrics metrics = paint.getFontMetrics();
			float textItemHeight = metrics.bottom - metrics.top;
			if(sections.length * textItemHeight > mHeight) {
				mTextCoefficient = mHeight / (sections.length * textItemHeight);
				paint.setTextSize(paint.getTextSize() * mTextCoefficient);
			}else {
				paint.setTextSize(mTextSize);
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		EMLog.d("EaseSidebar", "onDraw pointer: "+pointer);
		if(mBgColor != Color.TRANSPARENT) {
			canvas.drawColor(mBgColor);
		}
		float center = getWidth() / 2;
		ItemHeight = getHeight() / sections.length;
		for (int i = sections.length - 1; i > -1; i--) {
			if (i == pointer && mFocusBgColor != Color.TRANSPARENT) {
				paint.setColor(mFocusBgColor);
				canvas.drawCircle(center, ItemHeight * (i+0.75f), (float) (mTextSize * 0.6), paint);
				paint.setColor(Color.WHITE);
			} else {
				paint.setColor(mTextColor);
			}
			canvas.drawText(sections[i], center, ItemHeight * (i+1), paint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		pointer = sectionForPoint(event.getY());
		String section = sections[pointer];
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				handler.removeCallbacksAndMessages(null);
				invalidate();
				// Provides external interfaces for developers to operate
				if(mListener != null) {
					mListener.onActionDown(event, section);
				}
				return true;
			case MotionEvent.ACTION_MOVE:
				handler.removeCallbacksAndMessages(null);
				invalidate();
				// Provides external interfaces for developers to operate
				if(mListener != null) {
					mListener.onActionMove(event, section);
				}
				return true;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				handler.removeCallbacksAndMessages(null);
				handler.sendEmptyMessageDelayed(RESET, delayDisappearTime);
				if(mListener != null) {
					mListener.onActionUp(event);
				}
				return true;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * Gets the character as it moves
	 * @param y
	 * @return
	 */
	private int sectionForPoint(float y) {
		int index = (int) (y / ItemHeight);
		if(index < 0) {
		    index = 0;
		}
		if(index > sections.length -1) {
		    index = sections.length - 1;
		}
		return index;
	}

	/**
	 * Draw background
	 * @param color
	 */
	public void drawBackground(@ColorRes int color) {
		mBgColor = ContextCompat.getColor(context, color);
		postInvalidate();
	}

	public void drawBackgroundDrawable(@DrawableRes int drawableId) {
		setBackground(ContextCompat.getDrawable(context, drawableId));
	}

	public void drawBackgroundDrawable( Drawable drawable) {
		setBackground(drawable);
	}

	/**
	 * set touch event listener
	 * @param listener
	 */
	public void setOnTouchEventListener(OnTouchEventListener listener) {
		this.mListener = listener;
	}

	public interface OnTouchEventListener {
		/**
		 * Down event
		 * @param event
		 * @param pointer
		 */
		void onActionDown(MotionEvent event, String pointer);

		/**
		 * Move event
		 * @param event
		 * @param pointer
		 */
		void onActionMove(MotionEvent event, String pointer);

		/**
		 * Up event
		 * @param event
		 */
		void onActionUp(MotionEvent event);
	}

}
