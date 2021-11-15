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

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.cache.CacheLoader;
import com.bytedance.playerkit.player.playback.PlaybackEvent;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.data.model.VideoItem;

import java.util.List;


public class SmallVideoPageView extends FrameLayout {
    private Lifecycle mLifeCycle;
    private final SwipeRefreshLayout mRefreshLayout;
    private final ContentLoadingProgressBar mLoadMoreProgressBar;
    private final ViewPager2 mViewPager;
    private final ViewPager2Helper mPagerHelper;
    private final SmallVideoAdapter mSmallVideoAdapter;
    private VideoView mVideoView;
    private SwipeRefreshLayout.OnRefreshListener mLoadMoreListener;
    private boolean mLoadingMore;

    public SmallVideoPageView(@NonNull Context context) {
        this(context, null);
    }

    public SmallVideoPageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmallVideoPageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mViewPager = new ViewPager2(context);
        mViewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        mSmallVideoAdapter = new SmallVideoAdapter() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                final ViewHolder holder = super.onCreateViewHolder(parent, viewType);
                holder.videoView.controller().addPlaybackListener(new Dispatcher.EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        switch (event.code()) {
                            case PlaybackEvent.Action.START_PLAYBACK:
                                mVideoView = holder.videoView;
                                break;
                            case PlayerEvent.Action.PREPARE:
                                Player player = event.owner(Player.class);
                                player.setLooping(true);
                                break;
                            case PlayerEvent.Info.VIDEO_RENDERING_START:
                                int position = mViewPager.getCurrentItem();
                                starPreload(position);
                                break;
                            case PlaybackEvent.Action.STOP_PLAYBACK:
                                if (mVideoView == holder.videoView) {
                                    mVideoView = null;
                                }
                                break;
                        }
                    }
                });
                return holder;
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
                //holder.videoView.stopPlayback();
            }

            @Override
            public void onViewRecycled(@NonNull ViewHolder holder) {
                //holder.videoView.stopPlayback();
            }
        };
        mViewPager.setAdapter(mSmallVideoAdapter);
        mViewPager.registerOnPageChangeCallback(mOnPageChangeCallback);
        mPagerHelper = new ViewPager2Helper(mViewPager);

        mRefreshLayout = new SwipeRefreshLayout(context);
        mRefreshLayout.addView(mViewPager, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mRefreshLayout, new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mLoadMoreProgressBar = (ContentLoadingProgressBar) LayoutInflater.from(context).inflate(R.layout.small_video_view_loading_more, this, false);
        mLoadMoreProgressBar.setVisibility(GONE);
        addView(mLoadMoreProgressBar, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
    }

    public void setRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        mRefreshLayout.setOnRefreshListener(listener);
    }

    public void setLifeCycle(Lifecycle lifeCycle) {
        if (mLifeCycle != lifeCycle) {
            if (mLifeCycle != null) {
                mLifeCycle.removeObserver(mLifecycleEventObserver);
            }
            mLifeCycle = lifeCycle;
        }
        if (mLifeCycle != null) {
            mLifeCycle.addObserver(mLifecycleEventObserver);
        }
    }

    private final LifecycleEventObserver mLifecycleEventObserver = new LifecycleEventObserver() {
        @Override
        public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
            switch (event) {
                case ON_RESUME:
                    play();
                    break;
                case ON_PAUSE:
                    pause();
                    break;
                case ON_DESTROY:
                    mLifeCycle.removeObserver(mLifecycleEventObserver);
                    mLifeCycle = null;
                    stop();
                    break;
            }
        }
    };

    private final ViewPager2.OnPageChangeCallback mOnPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            togglePlayback(position);
            triggerLoadMore(position);
        }
    };

    private void triggerLoadMore(int position) {
        int count = mSmallVideoAdapter.getItemCount();
        if (position == count - 1) {
            if (mLoadMoreListener != null) {
                mLoadMoreListener.onRefresh();
            }
        }
    }

    public void setList(List<VideoItem> videoItems) {
        mSmallVideoAdapter.setList(videoItems);
        mViewPager.getChildAt(0).post(new Runnable() {
            @Override
            public void run() {
                play();
            }
        });
    }

    public void append(List<VideoItem> videoItems) {
        mSmallVideoAdapter.append(videoItems);
    }

    public void togglePlayback(int currentPosition) {
        if (!mLifeCycle.getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            return;
        }
        final VideoView videoView = (VideoView) mPagerHelper.findItemViewByPosition(currentPosition);
        if (mVideoView == null) {
            if (videoView != null) {
                videoView.startPlayback();
            }
        } else {
            if (videoView != null && videoView != mVideoView) {
                mVideoView.stopPlayback();
                videoView.startPlayback();
            } else {
                mVideoView.startPlayback();
            }
        }
    }

    public void play() {
        final int currentPosition = mViewPager.getCurrentItem();
        if (currentPosition >= 0) {
            togglePlayback(currentPosition);
        }
    }

    public void pause() {
        if (mVideoView != null) {
            mVideoView.pausePlayback();
        }
    }

    public void stop() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }

    private void starPreload(int position) {
        int next = position + 1;
        // preload next 5 videos
        int target = Math.min(mSmallVideoAdapter.getItemCount(), next + 5);
        while (next < target) {
            VideoItem videoItem = mSmallVideoAdapter.getItem(next);
            if (videoItem != null) {
                CacheLoader.Default.get().preload(VideoItem.toMediaSource(videoItem, false),
                        null);
            }
            next++;
        }
    }

    public boolean isRefreshing() {
        return mRefreshLayout.isRefreshing();
    }

    public void showRefreshing() {
        mRefreshLayout.setRefreshing(true);
    }

    public void dismissRefreshing() {
        mRefreshLayout.setRefreshing(false);
    }

    public boolean isLoadingMore() {
        return mLoadingMore;
    }

    public void showLoadingMore() {
        mLoadingMore = true;
        mLoadMoreProgressBar.setIndeterminate(true);
        mLoadMoreProgressBar.show();
    }

    public void dismissLoadingMore() {
        mLoadingMore = false;
        mLoadMoreProgressBar.setIndeterminate(false);
        mLoadMoreProgressBar.hide();
    }

    public void setLoadMoreListener(SwipeRefreshLayout.OnRefreshListener loadMoreListener) {
        mLoadMoreListener = loadMoreListener;
    }
}
