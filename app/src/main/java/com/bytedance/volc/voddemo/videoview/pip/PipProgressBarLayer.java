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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.videoview.VideoController;
import com.bytedance.volc.voddemo.videoview.layer.BaseVideoLayer;
import com.bytedance.volc.voddemo.videoview.layer.ILayer;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerEvent;

import java.util.Arrays;
import java.util.List;

public class PipProgressBarLayer extends BaseVideoLayer {

    private ProgressBar mProgressBar;

    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, RelativeLayout parent) {
        mProgressBar = (ProgressBar) inflater.inflate(R.layout.layer_pip_progress_bar, parent, false);
        dismiss();
        return mProgressBar;
    }

    @Override
    public int getZIndex() {
        return ILayer.PIP_PROGRESS_Z_INDEX;
    }

    @NonNull
    @Override
    public List<Integer> getSupportEvents() {
        return Arrays.asList(
                IVideoLayerEvent.VIDEO_LAYER_EVENT_RENDER_START,
                IVideoLayerEvent.VIDEO_LAYER_EVENT_CALL_PLAY,
                IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_ERROR,
                IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_RELEASE,
                IVideoLayerEvent.VIDEO_LAYER_EVENT_PROGRESS_CHANGE,
                IVideoLayerEvent.VIDEO_LAYER_EVENT_BUFFER_UPDATE
        );
    }

    @Override
    public boolean handleVideoEvent(@NonNull IVideoLayerEvent event) {
        final int action = event.getType();
        switch (action) {
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_RENDER_START:
                show();
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_CALL_PLAY:
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_ERROR:
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_RELEASE:
                dismiss();
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_PROGRESS_CHANGE: {
                VideoController controller = mHost != null ? mHost.getVideoController() : null;
                if (controller != null && (controller.isPlaying() || controller.isPaused())) {
                    updateProgress(controller.getCurrentPlaybackTime(), controller.getDuration());
                }
                break;
            }
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_BUFFER_UPDATE:
                int percent = event.getParam(Integer.class);
                if (percent >= 0) {
                    updateBufferProcess(percent);
                }
                break;
        }
        return false;
    }

    private void show() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void dismiss() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void updateProgress(long currentPosition, long duration) {
        if (mProgressBar != null && currentPosition <= duration) {
            mProgressBar.setProgress((int) (currentPosition / (float) duration * 100));
        }
    }

    private void updateBufferProcess(int percent) {
        mProgressBar.setSecondaryProgress(percent);
    }
}
