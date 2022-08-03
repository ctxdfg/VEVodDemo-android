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
 * Create Date : 2021/6/10
 */
package com.bytedance.volc.voddemo.smallvideo;

import static com.bytedance.volc.voddemo.data.VideoItem.VIDEO_TYPE_SMALL;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_SCENE_SMALL_VIDEO;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_TYPE_PRELOAD;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_TYPE_PRE_RENDER;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.VodApp;
import com.bytedance.volc.voddemo.base.BaseAdapter;
import com.bytedance.volc.voddemo.data.VideoItem;
import com.bytedance.volc.voddemo.data.VideoViewModel;
import com.bytedance.volc.voddemo.preload.PreloadManager;
import com.bytedance.volc.voddemo.preload.SimplePreloadStrategy;
import com.bytedance.volc.voddemo.settings.ClientSettings;
import com.bytedance.volc.voddemo.videoview.DisplayMode;
import com.bytedance.volc.voddemo.videoview.VOLCVideoController;
import com.bytedance.volc.voddemo.videoview.VOLCVideoView;
import com.bytedance.volc.voddemo.videoview.layers.CoverLayer;
import com.bytedance.volc.voddemo.videoview.layers.DebugLayer;
import com.bytedance.volc.voddemo.videoview.layers.LoadFailLayer;
import com.bytedance.volc.voddemo.videoview.layers.LoadingLayer;
import com.bytedance.volc.voddemo.videoview.layers.SmallToolbarLayer;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.source.VidPlayAuthTokenSource;
import com.ss.ttvideoengine.strategy.EngineStrategyListener;
import com.ss.ttvideoengine.strategy.source.StrategySource;

import java.util.ArrayList;
import java.util.List;

public class SmallVideoFragment extends Fragment {
    private static final String TAG = "SmallFragment";

    private int mPageIndex = 0;
    private int mPageSize = 5;

    private BaseAdapter<VideoItem> mAdapter;
    private VOLCVideoView mCurrentVideoView;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ViewPager2 mViewPager;
    private VideoViewModel mVideoViewModel;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ClientSettings settings = VodApp.getClientSettings();

        if (settings.enableStrategyPreload()) {
            // VOD key step Strategy Preload 1: enable
            TTVideoEngine.enableEngineStrategy(STRATEGY_TYPE_PRELOAD, STRATEGY_SCENE_SMALL_VIDEO);
        } else {
            PreloadManager.getInstance().setPreloadStrategy(new SimplePreloadStrategy());
        }

        if (settings.enableStrategyPreRender()) {
            // VOD key step Strategy PreRender 1: enable
            TTVideoEngine.enableEngineStrategy(STRATEGY_TYPE_PRE_RENDER,
                    STRATEGY_SCENE_SMALL_VIDEO);
            // VOD key step Strategy PreRender 3: set listener
            TTVideoEngine.setEngineStrategyListener(new EngineStrategyListener() {
                @Override
                public void onPreRenderEngineCreated(final TTVideoEngine engine) {
                    // VOD key step Strategy PreRender 4: config preRender engine
                    VOLCVideoController.configEngine(engine);
                }
            });
        }

        mVideoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        mAdapter = new BaseAdapter<VideoItem>(new ArrayList<>()) {

            @Override
            public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
                super.onViewDetachedFromWindow(holder);
                if (mCurrentVideoView == holder.itemView.findViewById(R.id.video_view)) {
                    release();
                }
            }

            @Override
            public int getLayoutId(final int viewType) {
                return R.layout.list_item_small_video;
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, final VideoItem data,
                                         final int position) {
                VOLCVideoView videoView = holder.getView(R.id.video_view);
                videoView.setVideoController(new VOLCVideoController(videoView.getContext(), data,
                        videoView));

                // DisplayMode is not required when using PLAYER_OPTION_USE_TEXTURE_RENDER
                if (!settings.enableStrategyPreRender()) {
                    videoView.setDisplayMode(DisplayMode.DISPLAY_MODE_ASPECT_FIT);
                }

                videoView.addLayer(new CoverLayer());
                videoView.addLayer(new DebugLayer());
                videoView.addLayer(new SmallToolbarLayer());
                videoView.addLayer(new LoadFailLayer());
                videoView.addLayer(new LoadingLayer());
                videoView.refreshLayers();
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_small_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = view.findViewById(R.id.recycler_view);
        mViewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        mViewPager.setAdapter(mAdapter);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                togglePlayback(position);
                if (position == mAdapter.getItemCount() - 1) {
                    loadMore();
                }
            }
        });

        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        refresh();
    }

    private void setStrategySources(final List<VideoItem> videoItems) {
        List<StrategySource> sources = videoItems2StrategySources(videoItems);
        // VOD key step Strategy PreRender 2: set sources
        // VOD key step Strategy Preload 2: set sources
        TTVideoEngine.setStrategySources(sources);
    }

    private void addStrategySources(final List<VideoItem> videoItems) {
        List<StrategySource> sources = videoItems2StrategySources(videoItems);
        // VOD key step Strategy PreRender 2: add sources
        // VOD key step Strategy Preload 2: add sources
        TTVideoEngine.addStrategySources(sources);
    }

    @NonNull
    private List<StrategySource> videoItems2StrategySources(List<VideoItem> videoItems) {
        String encodeType = VodApp.getClientSettings().videoEnableH265()
                ? TTVideoEngine.CODEC_TYPE_h265 : TTVideoEngine.CODEC_TYPE_H264;
        List<StrategySource> sources = new ArrayList<>();
        for (VideoItem videoItem : videoItems) {
            StrategySource vidSource = new VidPlayAuthTokenSource.Builder()
                    .setVid(videoItem.getVid())
                    .setPlayAuthToken(videoItem.getAuthToken())
                    .setEncodeType(encodeType)
                    .build();
            sources.add(vidSource);
        }
        return sources;
    }

    private boolean mLoading;
    private boolean mHasMore;

    private void refresh() {
        if (mLoading) return;

        mLoading = true;
        mPageIndex = 0;
        mVideoViewModel.getVideoList(VIDEO_TYPE_SMALL,  mPageIndex, mPageSize, videoItems -> {
            mLoading = false;
            mSwipeRefreshLayout.setRefreshing(false);
            if (videoItems != null && videoItems.size() > 0) {
                if (videoItems.size() >= mPageSize) {
                    mPageIndex++;
                    mHasMore = true;
                } else {
                    mHasMore = false;
                }
                mAdapter.replaceAll(videoItems);
                PreloadManager.getInstance().videoListUpdate(videoItems);
                setStrategySources(videoItems);
                mViewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        play();
                    }
                });
            } else {
                mHasMore = false;
            }
        });
    }

    private void loadMore() {
        if (!mHasMore) {
            return;
        }

        if (mLoading) return;
        mLoading = true;

        mVideoViewModel.getVideoList(VIDEO_TYPE_SMALL, mPageIndex, mPageSize, videoItems -> {
            mLoading = false;
            if (videoItems != null && videoItems.size() > 0) {
                if (videoItems.size() >= mPageSize) {
                    mPageIndex++;
                    mHasMore = true;
                } else {
                    mHasMore = false;
                }
                mAdapter.addAll(videoItems);
                PreloadManager.getInstance().videoListUpdate(mAdapter.getAll());
                addStrategySources(videoItems);
                mViewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        play();
                    }
                });
            } else {
                mHasMore = false;
            }
        });
    }

    private void play() {
        final int currentPosition = mViewPager.getCurrentItem();
        if (currentPosition >= 0) {
            togglePlayback(currentPosition);
        }
    }

    private void togglePlayback(int currentPosition) {
        View itemView = findItemViewByPosition(mViewPager, currentPosition);
        final VOLCVideoView videoView;
        if (itemView == null) {
            videoView = null;
        } else {
            videoView = itemView.findViewById(R.id.video_view);
        }
        if (mCurrentVideoView == null) {
            if (videoView != null) {
                videoView.play();
                mCurrentVideoView = videoView;
            }
        } else {
            if (videoView != null && videoView != mCurrentVideoView) {
                mCurrentVideoView.release();
                videoView.play();
                mCurrentVideoView = videoView;
            } else {
                mCurrentVideoView.play();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        play();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCurrentVideoView != null) {
            mCurrentVideoView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    private void release() {
        if (mCurrentVideoView == null) {
            return;
        }
        mCurrentVideoView.release();
        mCurrentVideoView = null;
    }

    @Nullable
    private static View findItemViewByPosition(ViewPager2 pager, int position) {
        final RecyclerView recyclerView = (RecyclerView) pager.getChildAt(0);
        if (recyclerView != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                return layoutManager.findViewByPosition(position);
            }
        }
        return null;
    }
}
