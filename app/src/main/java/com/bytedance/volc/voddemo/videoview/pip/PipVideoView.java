/*
 * Copyright 2021 bytedance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Create Date : 2021/8/1
 */
package com.bytedance.volc.voddemo.videoview.pip;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewManager;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.utils.UIUtils;
import com.bytedance.volc.voddemo.videoview.VOLCVideoView;

public class PipVideoView extends VOLCVideoView {
    private final ViewManager mParent;
    private final WindowManager.LayoutParams mParams;
    private float mDownX;
    private float mDownY;

    private float mTouchX;
    private float mTouchY;

    private final int mTouchSlop;
    private final int mSize;
    private final int mPadding;
    private final int mScreenWidth;
    private final int mScreenHeight;

    PipVideoView(@NonNull Context context) {
        super(context);
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mParent = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.mScreenWidth = UIUtils.getScreenWidth(getContext());
        this.mScreenHeight = UIUtils.getScreenHeight(getContext());
        this.mSize = (int) (Math.min(mScreenWidth, mScreenHeight) / 2);
        this.mPadding = (int) UIUtils.dip2Px(context, 16);
        this.mParams = createParams();
    }

    private WindowManager.LayoutParams createParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  // >= 8.0
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // >= 7.0
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // >= 4.4
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.windowAnimations = R.style.pipAnimation;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.START | Gravity.TOP;
        params.width = mSize;
        params.height = (int) (mSize / (16 / 9f));
        params.x = maxX(params.width);
        params.y = maxY(params.height);
        params.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;

        return params;
    }

    void addToParent() {
        try {
            mParent.addView(this, mParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void removeFromParent() {
        mParent.removeView(this);
    }

    void layoutInParent() {
        mParent.updateViewLayout(this, mParams);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mDownX = mTouchX = event.getRawX();
                mDownY = mTouchY = event.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float touchX = event.getRawX();
                float touchY = event.getRawY();
                final float dx = touchX - mDownX;
                final float dy = touchY - mDownY;
                mTouchX = touchX;
                mTouchY = touchY;
                if (Math.max(Math.abs(dx), Math.abs(dy)) >= mTouchSlop) {
                    return true;
                }
                break;
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mDownX = mTouchX = event.getRawX();
                mDownY = mTouchY = event.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float touchX = event.getRawX();
                float touchY = event.getRawY();
                final float dx = touchX - mTouchX;
                final float dy = touchY - mTouchY;
                mTouchX = touchX;
                mTouchY = touchY;
                if (dx != 0 || dy != 0) {
                    int x = (int) (mParams.x + dx + 0.5f);
                    int y = (int) (mParams.y + dy + 0.5f);
                    layoutInParent(x, y, getWidth(), getHeight());
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                mDownX = mTouchX = 0;
                mDownY = mTouchY = 0;
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    void layoutInParent(int x, int y, int width, int height) {
        mParams.width = width;
        mParams.height = height;
        int maxX = maxX(width);
        int maxY = maxY(height);
        int minX = minX();
        int minY = minY();
        mParams.x = Math.min(Math.max(minX, x), maxX);
        mParams.y = Math.min(Math.max(minY, y), maxY);
        layoutInParent();
    }

    int minX() {
        return mPadding;
    }

    int minY() {
        return mPadding;
    }

    int maxX(int width) {
        return mScreenWidth - width - mPadding;
    }

    int maxY(int height) {
        return mScreenHeight - height - mPadding;
    }
}
