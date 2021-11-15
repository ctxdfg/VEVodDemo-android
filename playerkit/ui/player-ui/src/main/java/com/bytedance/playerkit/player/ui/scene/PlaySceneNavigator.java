/*
 * Copyright (C) 2021 bytedance
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
 * Create Date : 2021/12/30
 */

package com.bytedance.playerkit.player.ui.scene;


import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.playback.DisplayModeHelper;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.ui.utils.UIUtils;

public class PlaySceneNavigator {

    private static final String TAG = "PlaySceneNavigator";

    public static void navigateTo(VideoView videoView, PlaySceneInfo playSceneInfo) {
        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent != null) {
            parent.removeView(videoView);
        }
        playSceneInfo.container.addView(videoView, playSceneInfo.lp);
        videoView.setRatio(playSceneInfo.ratio);
        videoView.setRatioMode(playSceneInfo.ratioMode);
        videoView.setDisplayMode(playSceneInfo.displayMode);

        final Activity activity = (Activity) videoView.getContext();
        final View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(playSceneInfo.systemUiVisibility);

        if (playSceneInfo.windowLp != null) {
            activity.getWindow().setAttributes(playSceneInfo.windowLp);
        }
        videoView.setPlayScene(playSceneInfo.videoScene);
    }

    public static class PlaySceneInfo {
        private final WindowManager.LayoutParams windowLp;
        private final ViewGroup container;
        private final ViewGroup.LayoutParams lp;
        @DisplayModeHelper.DisplayMode
        private final int displayMode;
        private final float ratio;
        private final int ratioMode;
        private final int systemUiVisibility;
        private final int videoScene;

        @Nullable
        public static PlaySceneInfo current(VideoView videoView) {
            Context context = videoView.getContext();
            if (!(context instanceof Activity)) return null;

            final Activity activity = (Activity) videoView.getContext();
            final View decorView = activity.getWindow().getDecorView();
            return new PlaySceneInfo(
                    videoView.getPlayScene(),
                    UIUtils.getWindowLayoutParams(videoView),
                    (ViewGroup) videoView.getParent(),
                    videoView.getLayoutParams(),
                    videoView.getDisplayMode(),
                    videoView.getRatio(),
                    videoView.getRatioMode(),
                    decorView.getSystemUiVisibility()
            );
        }

        public static PlaySceneInfo target(int videoScene, FrameLayout container, VideoView videoView, int systemUiVisibility) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) videoView.getLayoutParams();
            FrameLayout.LayoutParams copy = new FrameLayout.LayoutParams(layoutParams.width, layoutParams.height, layoutParams.gravity);
            return target(videoScene, container, copy, videoView.getDisplayMode(), videoView.getRatio(), videoView.getRatioMode(), systemUiVisibility);
        }

        public static PlaySceneInfo target(int videoScene, FrameLayout container, FrameLayout.LayoutParams layoutParams, int displayMode, float ratio, int ratioMode, int systemUiVisibility) {
            WindowManager.LayoutParams windowLp = null;
            WindowManager.LayoutParams curWindowLp = UIUtils.getWindowLayoutParams(container);
            if (curWindowLp != null) {
                windowLp = new WindowManager.LayoutParams();
                windowLp.copyFrom(curWindowLp);
            }
            return new PlaySceneInfo(
                    videoScene,
                    windowLp,
                    container,
                    layoutParams,
                    displayMode,
                    ratio,
                    ratioMode,
                    systemUiVisibility
            );
        }

        public static PlaySceneInfo target(int videoScene, WindowManager.LayoutParams windowLp, FrameLayout container, FrameLayout.LayoutParams layoutParams, int displayMode, float ratio, int ratioMode, int systemUiVisibility) {
            return new PlaySceneInfo(
                    videoScene,
                    windowLp,
                    container,
                    layoutParams,
                    displayMode,
                    ratio,
                    ratioMode,
                    systemUiVisibility
            );
        }

        private PlaySceneInfo(int videoScene, WindowManager.LayoutParams windowLp, ViewGroup container, ViewGroup.LayoutParams lp, int displayMode, float ratio, int ratioMode, int systemUiVisibility) {
            this.videoScene = videoScene;
            this.windowLp = windowLp;
            this.container = container;
            this.lp = lp;
            this.displayMode = displayMode;
            this.ratio = ratio;
            this.ratioMode = ratioMode;
            this.systemUiVisibility = systemUiVisibility;
        }
    }
}
