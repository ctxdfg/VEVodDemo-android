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
 * Create Date : 2021/7/29
 */
package com.bytedance.volc.voddemo.longvideo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.base.BaseAdapter;
import com.bytedance.volc.voddemo.data.VideoItem;
import com.bytedance.volc.voddemo.videoview.VOLCVideoController;
import com.bytedance.volc.voddemo.videoview.VOLCVideoView;
import com.bytedance.volc.voddemo.videoview.layer.ILayerHost;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerCommand;
import com.bytedance.volc.voddemo.videoview.layer.LayerRoot;
import com.bytedance.volc.voddemo.videoview.layers.BeforeLayer;
import java.util.Collections;

public class LongVideoAdapter extends BaseAdapter<VideoItem> {
    public static final int ITEM_TYPE_TOP = 0;
    public static final int ITEM_TYPE_NORMAL = 1;

    private final LayerRoot.VideoViewCommandListener mListener;

    public LongVideoAdapter(LayerRoot.VideoViewCommandListener listener) {
        super(Collections.emptyList());
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder<VideoItem> onCreateViewHolder(@NonNull final ViewGroup parent,
            final int viewType) {
        if (viewType == ITEM_TYPE_TOP) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_long_video_top, parent, false);
            return new TopItemViewHolder(view);
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_long_video, parent, false);
        return new NormalItemViewHolder(view);
    }

    @Override
    public int getItemViewType(final int position) {
        if (position == 0) {
            return ITEM_TYPE_TOP;
        }
        return ITEM_TYPE_NORMAL;
    }

    private class NormalItemViewHolder extends BaseAdapter.ViewHolder<VideoItem> {
        public NormalItemViewHolder(@NonNull final View itemView) {
            super(itemView);
        }

        @Override
        public void setupViews(final int position, final VideoItem data) {
            final TextView tvTitle = (TextView) getView(R.id.tv_title);
            tvTitle.setText(data.getTitle());
            VOLCVideoView videoView = (VOLCVideoView) getView(R.id.video_view);
            videoView.setVideoController(
                    new VOLCVideoController(videoView.getContext(), data));
            videoView.addLayer(new BeforeLayer(false));
            videoView.setCommandListener(mListener);
            videoView.refreshLayers();
        }
    }

    private class TopItemViewHolder extends BaseAdapter.ViewHolder<VideoItem> {
        public TopItemViewHolder(@NonNull final View itemView) {
            super(itemView);
        }

        public void setupViews(final int position, VideoItem data) {
            VOLCVideoView videoView = (VOLCVideoView) getView(R.id.top_video_view);
            videoView.setVideoController(
                    new VOLCVideoController(videoView.getContext(), data));
            videoView.addLayer(new BeforeLayer(true));
            videoView.setCommandListener(mListener);
            videoView.refreshLayers();
        }
    }
}
