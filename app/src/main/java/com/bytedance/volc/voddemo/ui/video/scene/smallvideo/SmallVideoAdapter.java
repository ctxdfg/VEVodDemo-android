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
 * Create Date : 2021/12/28
 */

package com.bytedance.volc.voddemo.ui.video.scene.smallvideo;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.playerkit.player.playback.DisplayModeHelper;
import com.bytedance.playerkit.player.playback.DisplayView;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.ui.layer.CoverLayer;
import com.bytedance.playerkit.player.ui.layer.LoadingLayer;
import com.bytedance.playerkit.player.ui.layer.PauseLayer;
import com.bytedance.playerkit.player.ui.layer.PlayErrorLayer;
import com.bytedance.playerkit.player.ui.layer.SimpleProgressBarLayer;
import com.bytedance.playerkit.player.ui.scene.PlayScene;
import com.bytedance.volc.voddemo.data.model.VideoItem;

import java.util.ArrayList;
import java.util.List;


public class SmallVideoAdapter extends RecyclerView.Adapter<SmallVideoAdapter.ViewHolder> {

    private final List<VideoItem> mItems = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<VideoItem> videoItems) {
        mItems.clear();
        mItems.addAll(videoItems);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void append(List<VideoItem> videoItems) {
        if (videoItems != null) {
            int count = mItems.size();
            mItems.addAll(videoItems);
            if (count > 0) {
                notifyItemRangeInserted(count, mItems.size());
            } else {
                notifyDataSetChanged();
            }
        }
    }

    public VideoItem getItem(int position) {
        return mItems.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoItem videoItem = mItems.get(position);
        holder.bind(position, videoItem);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final VideoView videoView;

        public static ViewHolder create(ViewGroup parent) {
            VideoView videoView = createVideoView(parent);
            videoView.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
            return new ViewHolder(videoView);
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = (VideoView) itemView;
            videoView.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        }

        public void bind(int position, VideoItem videoItem) {
            MediaSource mediaSource = videoView.getDataSource();
            if (mediaSource == null) {
                mediaSource = VideoItem.toMediaSource(videoItem, false);
                videoView.bindDataSource(mediaSource);
            } else {
                if (!TextUtils.equals(videoItem.getVid(), mediaSource.getMediaId())) {
                    videoView.stopPlayback();
                    mediaSource = VideoItem.toMediaSource(videoItem, false);
                    videoView.bindDataSource(mediaSource);
                } else {
                    // do nothing
                }
            }
        }
    }

    static VideoView createVideoView(ViewGroup parent) {
        VideoView videoView = new VideoView(parent.getContext());
        VideoLayerHost layerHost = new VideoLayerHost(parent.getContext());
        layerHost.addLayer(new CoverLayer());
        layerHost.addLayer(new LoadingLayer());
        layerHost.addLayer(new PauseLayer());
        layerHost.addLayer(new SimpleProgressBarLayer());
        layerHost.addLayer(new PlayErrorLayer());
        //layerHost.addLayer(new LogLayer());

        layerHost.attachToVideoView(videoView);
        videoView.setBackgroundColor(parent.getResources().getColor(android.R.color.black));
        //videoView.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FIT); // fit mode
        videoView.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FILL_Y); // immersive mode
        videoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW);
        videoView.setPlayScene(PlayScene.SCENE_SMALL);

        PlaybackController controller = new PlaybackController();
        controller.bind(videoView);
        return videoView;
    }
}

