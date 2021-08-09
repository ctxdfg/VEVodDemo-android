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
 * Create Date : 2021/2/25
 */
package com.bytedance.volc.voddemo.longvideo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.data.VideoItem;
import com.bytedance.volc.voddemo.videoview.DisplayMode;
import com.bytedance.volc.voddemo.videoview.Transformer;
import com.bytedance.volc.voddemo.videoview.VOLCVideoController;
import com.bytedance.volc.voddemo.videoview.VOLCVideoView;
import com.bytedance.volc.voddemo.videoview.layer.ILayerHost;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerCommand;
import com.bytedance.volc.voddemo.videoview.layer.LayerRoot;
import com.bytedance.volc.voddemo.videoview.layers.CoverLayer;
import com.bytedance.volc.voddemo.videoview.layers.DebugLayer;
import com.bytedance.volc.voddemo.videoview.layers.LoadFailLayer;
import com.bytedance.volc.voddemo.videoview.layers.LoadingLayer;
import com.bytedance.volc.voddemo.videoview.layers.ToolbarLayer;
import com.bytedance.volc.voddemo.videoview.pip.PipController;

public class DetailFragment extends Fragment implements LayerRoot.VideoViewCommandListener {
    public static final String TAG = "DetailFragment";

    private static final String DETAIL_ARG_ITEM = "DETAIL_ARG_ITEM";

    public static DetailFragment newInstance(VideoItem item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DETAIL_ARG_ITEM, item);
        DetailFragment detailFragment = new DetailFragment();
        detailFragment.setArguments(bundle);
        return detailFragment;
    }

    private VideoItem mVideoItem = null;
    private VOLCVideoView mVideoView = null;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments == null) {
            return;
        }
        mVideoItem = arguments.getParcelable(DETAIL_ARG_ITEM);
        getActivity().getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        setEnabled(false);
                        showPip();
                        getActivity().onBackPressed();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mVideoItem != null) {
            mVideoView = view.findViewById(R.id.video_view);
            mVideoView.setVideoController(
                    new VOLCVideoController(getContext(), mVideoItem));
            mVideoView.setParentLayout(view.findViewById(R.id.video_container));
            dismissPip();
            setupViews(mVideoView);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mVideoView != null) {
            mVideoView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.release();
        }
    }

    private void setupViews(VOLCVideoView videoView) {
        videoView.setTransformer((Transformer) getActivity());
        videoView.setCommandListener(this);

        videoView.setDisplayMode(DisplayMode.DISPLAY_MODE_ASPECT_FILL_X);

        videoView.addLayer(new CoverLayer());
        videoView.addLayer(new DebugLayer());
        videoView.addLayer(new ToolbarLayer());
        videoView.addLayer(new LoadFailLayer());
        videoView.addLayer(new LoadingLayer());
        videoView.refreshLayers();
        videoView.play();
    }

    @Override
    public boolean onVideoViewCommand(final ILayerHost layerHost, final IVideoLayerCommand action) {
        if (action.getCommand() == IVideoLayerCommand.VIDEO_HOST_CMD_EXIT_DETAIL) {
            requireActivity().onBackPressed();
            return true;
        }
        return false;
    }

    private void dismissPip() {
        PipController.instance().requestDismiss(mVideoView);
    }

    public void showPip() {
        PipController.instance().requestShow(mVideoView);
    }
}
