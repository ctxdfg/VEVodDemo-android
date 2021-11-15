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

package com.bytedance.volc.voddemo.ui.video.scene.shortvideo;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.playerkit.player.playback.DisplayModeHelper;
import com.bytedance.playerkit.player.playback.DisplayView;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.ui.layer.CoverLayer;
import com.bytedance.playerkit.player.ui.layer.FullScreenLayer;
import com.bytedance.playerkit.player.ui.layer.GestureLayer;
import com.bytedance.playerkit.player.ui.layer.LoadingLayer;
import com.bytedance.playerkit.player.ui.layer.LockLayer;
import com.bytedance.playerkit.player.ui.layer.PlayCompleteLayer;
import com.bytedance.playerkit.player.ui.layer.PlayErrorLayer;
import com.bytedance.playerkit.player.ui.layer.PlayPauseLayer;
import com.bytedance.playerkit.player.ui.layer.SyncStartTimeLayer;
import com.bytedance.playerkit.player.ui.layer.TimeProgressBarLayer;
import com.bytedance.playerkit.player.ui.layer.TipsLayer;
import com.bytedance.playerkit.player.ui.layer.TitleBarLayer;
import com.bytedance.playerkit.player.ui.layer.VolumeBrightnessIconLayer;
import com.bytedance.playerkit.player.ui.layer.dialog.MoreDialogLayer;
import com.bytedance.playerkit.player.ui.layer.dialog.QualitySelectDialogLayer;
import com.bytedance.playerkit.player.ui.layer.dialog.SpeedSelectDialogLayer;
import com.bytedance.playerkit.player.ui.layer.dialog.TimeProgressDialogLayer;
import com.bytedance.playerkit.player.ui.layer.dialog.VolumeBrightnessDialogLayer;
import com.bytedance.playerkit.player.ui.scene.PlayScene;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.data.model.VideoItem;

import java.util.ArrayList;
import java.util.List;


public class ShortVideoAdapter extends RecyclerView.Adapter<ShortVideoAdapter.ViewHolder> {

    public interface OnItemViewListener {
        void onItemClick(ViewHolder holder);

        void onVideoViewClick(ViewHolder holder);

        void onEvent(ViewHolder viewHolder, Event event);
    }

    final List<VideoItem> mItems = new ArrayList<>();
    final OnItemViewListener mOnItemViewListener;

    public ShortVideoAdapter(OnItemViewListener listener) {
        this.mOnItemViewListener = listener;
    }

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
                notifyItemRangeInserted(count, videoItems.size());
            } else {
                notifyDataSetChanged();
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.short_video_item, parent, false), mOnItemViewListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final VideoItem videoItem = mItems.get(position);
        holder.bindSource(position, videoItem, mItems);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public VideoItem getItem(int position) {
        return mItems.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final FrameLayout videoViewContainer;
        public VideoView sharedVideoView;
        public final TextView titleView;
        public final PlaybackController controller;

        public ViewHolder(@NonNull View itemView, OnItemViewListener listener) {
            super(itemView);

            videoViewContainer = itemView.findViewById(R.id.videoViewContainer);

            titleView = itemView.findViewById(R.id.videoDesc);
            sharedVideoView = itemView.findViewById(R.id.videoView);

            VideoLayerHost layerHost = new VideoLayerHost(itemView.getContext());
            layerHost.addLayer(new GestureLayer());
            layerHost.addLayer(new FullScreenLayer());
            layerHost.addLayer(new CoverLayer());
            layerHost.addLayer(new TimeProgressBarLayer());
            layerHost.addLayer(new TitleBarLayer());
            layerHost.addLayer(new QualitySelectDialogLayer());
            layerHost.addLayer(new SpeedSelectDialogLayer());
            layerHost.addLayer(new MoreDialogLayer());
            layerHost.addLayer(new TipsLayer());
            layerHost.addLayer(new SyncStartTimeLayer());
            layerHost.addLayer(new VolumeBrightnessIconLayer());
            layerHost.addLayer(new VolumeBrightnessDialogLayer());
            layerHost.addLayer(new TimeProgressDialogLayer());
            layerHost.addLayer(new PlayErrorLayer());
            layerHost.addLayer(new PlayPauseLayer());
            layerHost.addLayer(new LockLayer());
            layerHost.addLayer(new LoadingLayer());
            layerHost.addLayer(new PlayCompleteLayer());
            layerHost.attachToVideoView(sharedVideoView);

            sharedVideoView.setBackgroundColor(itemView.getResources().getColor(android.R.color.black));
            sharedVideoView.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FIT);
            sharedVideoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW);
            sharedVideoView.setPlayScene(PlayScene.SCENE_SHORT);

            controller = new PlaybackController();
            controller.bind(sharedVideoView);

            sharedVideoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onVideoViewClick(ViewHolder.this);
                    }
                }
            });
            controller.addPlaybackListener(new Dispatcher.EventListener() {
                @Override
                public void onEvent(Event event) {
                    if (listener != null) {
                        listener.onEvent(ViewHolder.this, event);
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(ViewHolder.this);
                    }
                }
            });
        }

        void bindSource(int position, VideoItem videoItem, List<VideoItem> videoItems) {
            titleView.setText(videoItem.getTitle());
            VideoView videoView = sharedVideoView;
            MediaSource mediaSource = videoView.getDataSource();
            if (mediaSource == null) {
                mediaSource = VideoItem.toMediaSource(videoItem, true);
                videoView.bindDataSource(mediaSource);
            } else {
                if (TextUtils.equals(videoItem.getVid(), mediaSource.getMediaId())) {
                    // do nothing
                } else {
                    videoView.stopPlayback();
                    mediaSource = VideoItem.toMediaSource(videoItem, true);
                    videoView.bindDataSource(mediaSource);
                }
            }
        }
    }
}


