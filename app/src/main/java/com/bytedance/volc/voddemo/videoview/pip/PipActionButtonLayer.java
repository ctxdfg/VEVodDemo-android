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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.videoview.VideoController;
import com.bytedance.volc.voddemo.videoview.layer.BaseVideoLayer;
import com.bytedance.volc.voddemo.videoview.layer.ILayer;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerEvent;

import java.util.Arrays;
import java.util.List;

public class PipActionButtonLayer extends BaseVideoLayer {
    private View mRootView;
    private View mPlayPause, mFullScreen;
    private boolean mShowing;

    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, RelativeLayout parent) {
        mRootView = inflater.inflate(R.layout.layer_pip_action, parent, false);
        mPlayPause = mRootView.findViewById(R.id.playPause);
        mFullScreen = mRootView.findViewById(R.id.fullScreen);
        mPlayPause.setOnClickListener(v -> togglePlayPause());
        mFullScreen.setOnClickListener(v -> fullScreen());
        dismiss(false);
        return mRootView;
    }

    @Override
    public int getZIndex() {
        return ILayer.PIP_ACTION_Z_INDEX;
    }

    @NonNull
    @Override
    public List<Integer> getSupportEvents() {
        return Arrays.asList(IVideoLayerEvent.VIDEO_LAYER_EVENT_CALL_PLAY,
                IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_VIEW_CLICK);
    }

    @Override
    public boolean handleVideoEvent(@NonNull IVideoLayerEvent event) {
        final int action = event.getType();
        if (action == IVideoLayerEvent.VIDEO_LAYER_EVENT_CALL_PLAY) {
            dismiss(false);
        }
        if (action == IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_VIEW_CLICK) {
            toggleShow();
            return true;
        }
        return false;
    }

    protected void togglePlayPause() {
        if (mHost == null) return;
        if (mPlayPause == null) return;

        VideoController controller = mHost.getVideoController();
        if (controller != null) {
            if (controller.isPlaying()) {
                controller.pause();
            } else {
                controller.play();
            }
            syncPlayPause();
        }
    }

    private void syncPlayPause() {
        VideoController controller = mHost.getVideoController();
        if (controller != null) {
            if (controller.isPlaying()) {
                mPlayPause.setBackgroundResource(R.drawable.pip_pause);
            } else {
                mPlayPause.setBackgroundResource(R.drawable.pip_play);
            }
        }
    }

    @CallSuper
    protected void fullScreen() {
        // bugfix: fix fullscreen button multi click
        mFullScreen.setEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mFullScreen.getViewTreeObserver().addOnWindowAttachListener(new ViewTreeObserver.OnWindowAttachListener() {
                @Override
                public void onWindowAttached() {
                    mFullScreen.getViewTreeObserver().removeOnWindowAttachListener(this);
                    mFullScreen.setEnabled(true);
                }

                @Override
                public void onWindowDetached() {
                    mFullScreen.getViewTreeObserver().removeOnWindowAttachListener(this);
                    mFullScreen.setEnabled(true);
                }
            });
        }
    }

    protected void toggleShow() {
        if (mRootView == null) return;
        if (!mShowing) {
            show(true);
            mRootView.postDelayed(DISMISS, 3000);
        } else {
            dismiss(true);
        }
    }

    protected void show(boolean animate) {
        if (mRootView == null) return;
        mRootView.removeCallbacks(DISMISS);
        mFullScreen.setEnabled(true);
        mShowing = true;
        animateShow(mRootView, animate);
        syncPlayPause();
    }

    protected void dismiss(boolean animate) {
        if (mRootView == null) return;
        mRootView.removeCallbacks(DISMISS);
        mShowing = false;
        animateDismiss(mRootView, animate);
    }

    private static void animateShow(View view, boolean animate) {
        if (animate) {
            if (view.getVisibility() != View.VISIBLE) {
                view.setVisibility(View.VISIBLE);
                view.setAlpha(0);
                view.animate().setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                        view.setAlpha(1);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                        view.setAlpha(1);
                    }
                }).alpha(1).start();
            }
        } else {
            view.setVisibility(View.VISIBLE);
            view.setAlpha(1);
        }
    }

    private static void animateDismiss(View view, boolean animate) {
        if (animate) {
            if (view.getVisibility() == View.VISIBLE) {
                view.setAlpha(1);
                view.animate().setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        view.setVisibility(View.GONE);
                        view.setAlpha(1);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                        view.setAlpha(1);
                    }
                }).alpha(0).start();
            }
        } else {
            view.setVisibility(View.GONE);
            view.setAlpha(1);
        }
    }

    private final Runnable DISMISS = () -> {
        dismiss(true);
    };
}