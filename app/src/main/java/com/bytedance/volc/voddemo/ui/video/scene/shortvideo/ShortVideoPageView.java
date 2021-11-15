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


import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.cache.CacheLoader;
import com.bytedance.playerkit.player.playback.PlaybackEvent;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.ui.scene.PlayScene;
import com.bytedance.playerkit.player.ui.utils.UIUtils;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.voddemo.data.model.VideoItem;
import com.bytedance.volc.voddemo.ui.video.scene.detail.DetailVideoFragment;
import com.bytedance.volc.voddemo.ui.video.scene.shortvideo.ShortVideoAdapter.OnItemViewListener;
import com.bytedance.volc.voddemo.widgets.loadmore.LoadMoreHelper;
import com.bytedance.volc.voddemo.widgets.loadmore.OnLoadMoreListener;

import java.util.List;


public class ShortVideoPageView extends FrameLayout {
    private Lifecycle mLifeCycle;
    private final SwipeRefreshLayout mRefreshLayout;
    private final RecyclerView mRecyclerView;
    private final ShortVideoAdapter mShortVideoAdapter;
    private final LoadMoreHelper.LoadMoreAble mLoadMoreAble;

    private VideoView mCurrentVideoView;

    public ShortVideoPageView(@NonNull Context context) {
        this(context, null);
    }

    public ShortVideoPageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShortVideoPageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false) {
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                               int position) {
                LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(context) {
                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return super.calculateSpeedPerPixel(displayMetrics) * 4;
                    }
                };
                linearSmoothScroller.setTargetPosition(position);
                startSmoothScroll(linearSmoothScroller);
            }
        };
        mShortVideoAdapter = new ShortVideoAdapter(mAdapterListener) {
            @Override
            public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
                if (!isFullScreen()) {
                    if (holder.sharedVideoView != null) {
                        holder.sharedVideoView.stopPlayback();
                    }
                }
            }

            @Override
            public void onViewRecycled(@NonNull ViewHolder holder) {
                super.onViewRecycled(holder);
                if (!isFullScreen()) {
                    if (holder.sharedVideoView != null) {
                        holder.sharedVideoView.stopPlayback();
                    }
                }
            }
        };

        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mShortVideoAdapter);

        mRefreshLayout = new SwipeRefreshLayout(context);
        mRefreshLayout.addView(mRecyclerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mRefreshLayout, new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mLoadMoreAble = LoadMoreHelper.help(mRecyclerView);
    }

    final OnItemViewListener mAdapterListener = new OnItemViewListener() {
        @Override
        public void onItemClick(ShortVideoAdapter.ViewHolder holder) {
            enterDetail(holder);
        }

        @Override
        public void onVideoViewClick(ShortVideoAdapter.ViewHolder holder) {
            // click to play
            VideoView videoView = holder.sharedVideoView;
            if (videoView == null) return;

            final Player player = videoView.player();
            if (player == null) {
                videoView.startPlayback();
                int position = holder.getAbsoluteAdapterPosition();
                if (position >= 0) {
                    mRecyclerView.smoothScrollToPosition(position);
                }
            }
        }

        @Override
        public void onEvent(ShortVideoAdapter.ViewHolder viewHolder, Event event) {
            switch (event.code()) {
                case PlaybackEvent.Action.START_PLAYBACK: {
                    // toggle play
                    final VideoView videoView = viewHolder.controller.videoView();
                    if (mCurrentVideoView != null && videoView != null) {
                        if (mCurrentVideoView != videoView) {
                            mCurrentVideoView.stopPlayback();
                        }
                    }
                    mCurrentVideoView = videoView;
                    break;
                }
                case PlayerEvent.Info.VIDEO_RENDERING_START:
                    final int position = viewHolder.getAbsoluteAdapterPosition();
                    starPreload(position);
                    break;
                case PlaybackEvent.Action.STOP_PLAYBACK:
                    break;

            }
        }
    };

    public void setRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        mRefreshLayout.setOnRefreshListener(listener);
    }

    public void setLoadMoreListener(OnLoadMoreListener listener) {
        mLoadMoreAble.setOnLoadMoreListener(listener);
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


    public void setList(List<VideoItem> videoItems) {
        mShortVideoAdapter.setList(videoItems);
    }

    public void append(List<VideoItem> videoItems) {
        mShortVideoAdapter.append(videoItems);
    }

    public void play() {
        if (mCurrentVideoView != null) {
            mCurrentVideoView.startPlayback();
        }
    }

    public void pause() {
        if (mCurrentVideoView != null) {
            mCurrentVideoView.pausePlayback();
        }
    }

    public void stop() {
        if (mCurrentVideoView != null) {
            mCurrentVideoView.stopPlayback();
        }
        mCurrentVideoView = null;
    }

    private void starPreload(int position) {
        int next = position + 1;
        // preload next 5 videos
        int target = Math.min(mShortVideoAdapter.getItemCount(), next + 5);
        while (next < target) {
            VideoItem videoItem = mShortVideoAdapter.getItem(next);
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

    public void showLoadingMore() {
        mLoadMoreAble.setLoadingMore(true);
    }

    public void dismissLoadingMore() {
        mLoadMoreAble.setLoadingMore(false);
    }

    public boolean isLoadingMore() {
        return mLoadMoreAble.isLoadingMore();
    }

    public boolean isFullScreen() {
        return mCurrentVideoView != null &&
                mCurrentVideoView.getPlayScene() == PlayScene.SCENE_FULLSCREEN;
    }

    public boolean onBackPressed() {
        if (mCurrentVideoView != null) {
            final VideoLayerHost layerHost = mCurrentVideoView.layerHost();
            if (layerHost != null && layerHost.onBackPressed()) {
                return true;
            }
        }
        if (isDetail()) {
            // handled in detail
            return true;
        }
        return false;
    }

    public boolean isDetail() {
        DetailVideoFragment detailVideoFragment = (DetailVideoFragment) ((FragmentActivity) getContext())
                .getSupportFragmentManager()
                .findFragmentByTag(DetailVideoFragment.class.getSimpleName());
        return detailVideoFragment != null;
    }

    private void enterDetail(ShortVideoAdapter.ViewHolder holder) {
        FragmentActivity activity = (FragmentActivity) getContext();
        DetailVideoFragment detail = DetailVideoFragment.newInstance();
        detail.setContact(new DetailVideoFragment.VideoViewTransitionContract() {

            @Override
            public VideoView getSharedVideoView() {
                return holder.sharedVideoView;
            }

            @Override
            public void takeOverVideoView(VideoView videoView) {
                //itemView.setHasTransientState(true);
                if (videoView.getParent() != null) {
                    ViewGroup parent = (ViewGroup) videoView.getParent();
                    if (parent != null) {
                        parent.removeView(videoView);
                    }
                }
                if (holder.sharedVideoView != null && holder.sharedVideoView == videoView) {
                    holder.sharedVideoView = null;
                }
            }

            @Override
            public Rect videoViewTransitionRect() {
                final int[] location = UIUtils.getLocationInWindow(holder.videoViewContainer);
                int left = location[0];
                int top = location[1] - UIUtils.getStatusBarHeight(holder.itemView.getContext());
                int right = left + holder.videoViewContainer.getWidth();
                int bottom = top + holder.videoViewContainer.getHeight();
                return new Rect(left, top, right, bottom);
            }

            @Override
            public void givebackVideoView(VideoView videoView) {
                holder.sharedVideoView = videoView;
                holder.videoViewContainer.addView(videoView);
                videoView.setPlayScene(PlayScene.SCENE_SHORT);
                videoView.startPlayback();
                int position = holder.getAbsoluteAdapterPosition();
                if (position >= 0) {
                    mRecyclerView.smoothScrollToPosition(position);
                }
            }
        });
        activity.getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .add(android.R.id.content, detail, DetailVideoFragment.class.getName())
                .commit();
    }

    private void exitDetail() {
        if (isDetail()) {
            FragmentActivity activity = (FragmentActivity) getContext();
            activity.getSupportFragmentManager().popBackStack();
        }
    }
}
