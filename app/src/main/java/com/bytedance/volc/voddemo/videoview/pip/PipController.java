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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import com.bytedance.volc.voddemo.data.VideoItem;
import com.bytedance.volc.voddemo.longvideo.DetailActivity;
import com.bytedance.volc.voddemo.utils.UIUtils;
import com.bytedance.volc.voddemo.videoview.DisplayMode;
import com.bytedance.volc.voddemo.videoview.VOLCVideoController;
import com.bytedance.volc.voddemo.videoview.VOLCVideoView;
import com.bytedance.volc.voddemo.videoview.VideoController;
import com.bytedance.volc.voddemo.videoview.layers.LoadFailLayer;
import com.bytedance.volc.voddemo.videoview.layers.LoadingLayer;
import com.yanzhenjie.permission.AndPermission;

public class PipController {

    private static PipController sInstance;

    private final Context mContext;
    private PipVideoView mPipView;
    private int mDisplayMode = DisplayMode.DISPLAY_MODE_ASPECT_FIT;

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new PipController(context);
        }
    }

    public static PipController instance() {
        return sInstance;
    }

    private PipController(Context context) {
        this.mContext = context.getApplicationContext();
    }

    private PipVideoView createPipView(Context context) {
        final PipVideoView pipView = new PipVideoView(context);
        pipView.addLayer(new LoadingLayer());
        pipView.addLayer(new PipProgressBarLayer());
        pipView.addLayer(new LoadFailLayer());
        pipView.addLayer(new PipCloseLayer() {
            @Override
            protected void close() {
                dismiss();
            }
        });
        pipView.addLayer(new PipActionButtonLayer() {

            @Override
            protected void fullScreen() {
                super.fullScreen();
                VideoController controller = pipView.getVideoController();
                if (controller != null) {
                    VideoItem videoItem = controller.getVideoItem();
                    if (videoItem != null) {
                        DetailActivity.intentInto(context, videoItem);
                    }
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pipView.setElevation(UIUtils.dip2Px(context, 4));
        }
        return pipView;
    }

    public void setDisplayMode(int displayMode) {
        this.mDisplayMode = displayMode;
    }

    public void requestDismiss(VOLCVideoView current) {
        final VideoController controller = current != null ? current.getVideoController() : null;
        final VideoItem videoItem = controller != null ? controller.getVideoItem() : null;
        if (isShowing()) {
            VideoItem pipSource = getDataSource();
            if (VideoItem.isSameVideo(pipSource, videoItem)) {
                long startTime = getCurrentPosition();
                if (startTime >= 0) {
                    if (controller != null) {
                        controller.setStartTime(startTime);
                    }
                }
            }
            dismiss();
        }
    }

    public void requestShow(VOLCVideoView current) {
        final VideoController controller = current != null ? current.getVideoController() : null;
        final long currentPosition = controller != null ? controller.getCurrentPlaybackTime() : null;
        final VideoItem videoItem = controller != null ? controller.getVideoItem() : null;
        AndPermission.with(mContext).overlay().onGranted(granted -> {
            setDisplayMode(DisplayMode.DISPLAY_MODE_ASPECT_FIT);
            show();
            if (isShowing()) {
                VideoController player = new VOLCVideoController(mContext, videoItem);
                player.setStartTime(currentPosition);
                setVideoController(player);
                current.release();
                play();
            }
        }).onDenied(denied -> {
            Toast.makeText(current.getContext(), "Permission Denied! Pip mode failed.",
                    Toast.LENGTH_LONG).show();
        }).start();
    }

    public void show() {
        if (isShowing()) return;

        if (mPipView == null) {
            mPipView = createPipView(mContext);
        }
        mPipView.setVisibility(View.VISIBLE);
        mPipView.addToParent();
        mPipView.setDisplayMode(mDisplayMode);
        register();
    }

    public boolean isShowing() {
        if (mPipView == null) return false;
        return mPipView.getParent() != null && mPipView.getVisibility() == View.VISIBLE;
    }

    public void dismiss() {
        if (!isShowing()) return;
        if (mPipView == null) return;

        mPipView.release();
        mPipView.removeFromParent();
        unregister();
    }

    public void setVideoController(VideoController videoController) {
        if (mPipView != null) {
            mPipView.setVideoController(videoController);
        }
    }

    public void play() {
        if (!isShowing()) return;
        mPipView.play();
    }

    public boolean isPlaying() {
        if (mPipView != null) {
            VideoController controller = mPipView.getVideoController();
            if (controller != null) {
                return controller.isPlaying();
            }
        }
        return false;
    }

    public void pause() {
        if (mPipView == null) return;
        mPipView.pause();
    }

    public long getCurrentPosition() {
        if (mPipView != null) {
            VideoController videoController = mPipView.getVideoController();
            if (videoController != null) {
                return videoController.getCurrentPlaybackTime();
            }
        }
        return 0;
    }

    public VideoItem getDataSource() {
        if (mPipView != null) {
            VideoController videoController = mPipView.getVideoController();
            if (videoController != null) {
                return videoController.getVideoItem();
            }
        }
        return null;
    }


    private boolean mIsRegister;
    private void register() {
        if (mIsRegister) return;
        mIsRegister = true;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mReceiver, intentFilter);
    }

    private void unregister() {
        if (!mIsRegister) return;
        mIsRegister = false;
        mContext.unregisterReceiver(mReceiver);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case Intent.ACTION_SCREEN_OFF:
                    if (isShowing() && isPlaying()) {
                        pause();
                    }
                    break;
                case Intent.ACTION_USER_PRESENT:
                    if (isShowing() && !isPlaying()) {
                        play();
                    }
                    break;
            }
        }
    };
}
