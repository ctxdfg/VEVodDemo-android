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
 * Create Date : 2021/7/30
 */
package com.bytedance.volc.voddemo.base;

import android.content.pm.ActivityInfo;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.bytedance.volc.voddemo.videoview.DisplayMode;
import com.bytedance.volc.voddemo.videoview.Transformer;
import com.bytedance.volc.voddemo.videoview.VOLCVideoView;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;

public class TransActivity extends AppCompatActivity implements Transformer {

    private static final String TAG = "Transformer";
    private ViewGroup mFullScreenContainer;
    private boolean mFullScreen;
    private VOLCVideoView mVideoView;

    @Override
    public boolean isFullScreen() {
        return mFullScreen;
    }

    @Override
    public void exitFullScreen(final VOLCVideoView videoView) {
        TTVideoEngineLog.d(TAG, "exitFullScreen " + videoView);
        ViewGroup originParent = videoView.getOriginParentLayout();
        if (originParent == null) {
            return;
        }

        mFullScreen = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestOrientation(false);

        ((ViewGroup) videoView.getParent()).removeView(videoView);
        originParent.addView(videoView,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        mVideoView = null;
    }

    @Override
    public void enterFullScreen(final VOLCVideoView videoView) {
        TTVideoEngineLog.d(TAG, "enterFullScreen " + videoView);
        if (mFullScreenContainer == null) {
            return;
        }

        mFullScreen = true;
        requestOrientation(true);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ViewGroup parent = (ViewGroup) videoView.getParent();
        ViewGroup.LayoutParams params = parent.getLayoutParams();
        params.height = parent.getHeight();
        params.width = parent.getWidth();
        parent.setLayoutParams(params);
        parent.removeView(videoView);

        mFullScreenContainer.setVisibility(View.VISIBLE);
        mFullScreenContainer.addView(videoView,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        videoView.setDisplayMode(DisplayMode.DISPLAY_MODE_ASPECT_FIT);
        mVideoView = videoView;
    }

    public void setFullScreenContainer(final ViewGroup fullScreenContainer) {
        mFullScreenContainer = fullScreenContainer;
    }

    private void requestOrientation(boolean fullScreen) {
        int targetOrientation = getTargetOrientation(fullScreen);
        if (needRequestOrientation(targetOrientation)) {
            setRequestedOrientation(targetOrientation);
        }
    }

    private boolean needRequestOrientation(int targetOrientation) {
        int currentOrientation = getRequestedOrientation();
        return targetOrientation != OrientationEventListener.ORIENTATION_UNKNOWN
                && targetOrientation != currentOrientation;
    }

    private int getTargetOrientation(boolean fullScreen) {
        if (fullScreen) {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }

    @Override
    public void onBackPressed() {
        if (mFullScreen) {
            mVideoView.exitFullScreen();
        } else {
            super.onBackPressed();
        }
    }
}
