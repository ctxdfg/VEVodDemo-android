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

package com.bytedance.volc.voddemo.ui.video.scene.longvideo;

import static com.bytedance.volc.voddemo.ui.video.scene.longvideo.LongVideoAdapter.Item;
import static com.bytedance.volc.voddemo.ui.video.scene.longvideo.LongVideoAdapter.OnItemClickListener;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bytedance.playerkit.player.ui.scene.PlayScene;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.data.model.VideoItem;
import com.bytedance.volc.voddemo.data.page.Book;
import com.bytedance.volc.voddemo.data.page.Page;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.api2.RemoteApi2;
import com.bytedance.volc.voddemo.ui.base.BaseFragment;
import com.bytedance.volc.voddemo.ui.video.scene.VideoActivity;
import com.bytedance.volc.voddemo.ui.video.scene.detail.DetailVideoFragment;
import com.bytedance.volc.voddemo.widgets.loadmore.LoadMoreHelper;
import com.bytedance.volc.voddemo.widgets.loadmore.OnLoadMoreListener;

import java.util.List;


public class LongVideoFragment extends BaseFragment {
    private static final boolean ENTER_DETAIL_ACTIVITY = true;

    private RemoteApi mRemoteApi;
    private final Book<VideoItem> mBook = new Book<>(20);
    private LongVideoAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private LoadMoreHelper.LoadMoreAble mLoadMoreAble;
    private LongVideoDataTrans mDataTrans;

    public static Fragment newInstance() {
        return new LongVideoFragment();
    }

    @Override
    public boolean onBackPressed() {
        if (isDetail()) {
            // handled in detail
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteApi = new RemoteApi2();
        mDataTrans = new LongVideoDataTrans(requireActivity());
        mAdapter = new LongVideoAdapter(new OnItemClickListener() {
            @Override
            public void onItemClick(Item item, RecyclerView.ViewHolder holder) {
                if (item.type == Item.TYPE_VIDEO_ITEM) {
                    enterDetail(item.videoItem);
                }
            }

            @Override
            public void onHeaderItemClick(VideoItem item, RecyclerView.ViewHolder holder) {
                enterDetail(item);
            }
        });
    }

    private void enterDetail(VideoItem item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(DetailVideoFragment.EXTRA_VIDEO_ITEM, item);
        if (ENTER_DETAIL_ACTIVITY) {
            VideoActivity.intentInto(requireActivity(), PlayScene.SCENE_DETAIL, bundle);
        } else {
            FragmentActivity activity = requireActivity();
            DetailVideoFragment detail = DetailVideoFragment.newInstance(bundle);
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right,
                            R.anim.slide_in_right, R.anim.slide_out_right)
                    .addToBackStack(null)
                    .add(android.R.id.content, detail, DetailVideoFragment.class.getName())
                    .commit();
        }
    }

    public boolean isDetail() {
        if (ENTER_DETAIL_ACTIVITY) {
            return false;
        }
        DetailVideoFragment detailVideoFragment = (DetailVideoFragment) requireActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(DetailVideoFragment.class.getSimpleName());
        return detailVideoFragment != null;
    }

    @Override
    protected void initBackPressedHandler() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout frameLayout = new FrameLayout(requireActivity());
        mRefreshLayout = new SwipeRefreshLayout(requireActivity());
        mRecyclerView = new RecyclerView(requireActivity());
        GridLayoutManager layoutManager = new GridLayoutManager(requireActivity(), 2);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new LongVideoItemDecoration());
        mRefreshLayout.addView(mRecyclerView);
        frameLayout.addView(mRefreshLayout);
        mLoadMoreAble = LoadMoreHelper.help(mRecyclerView);
        return frameLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        mLoadMoreAble.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        });
        refresh();
    }

    private void refresh() {
        showRefreshing();
        mRemoteApi.getFeedStreamWithPlayAuthToken("small-video", 0, mBook.pageSize(), new RemoteApi.Callback<Page<VideoItem>>() {
            @Override
            public void onSuccess(Page<VideoItem> page) {
                List<VideoItem> videoItems = mBook.first(page);
                dismissRefreshing();
                mDataTrans.setList(mAdapter, videoItems);
            }

            @Override
            public void onError(Exception e) {
                dismissRefreshing();
                Toast.makeText(getActivity(), e.getMessage() + "", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isRefreshing() {
        return mRefreshLayout.isRefreshing();
    }

    private boolean isLoadingMore() {
        return mLoadMoreAble.isLoadingMore();
    }

    private void dismissRefreshing() {
        mRefreshLayout.setRefreshing(false);
    }

    private void showRefreshing() {
        mRefreshLayout.setRefreshing(true);
    }

    private void loadMore() {
        if (mBook.hasMore()) {
            if (isLoadingMore()) return;
            showLoadingMore();
            mRemoteApi.getFeedStreamWithPlayAuthToken("small-video", mBook.nextPageIndex(), mBook.pageSize(), new RemoteApi.Callback<Page<VideoItem>>() {
                @Override
                public void onSuccess(Page<VideoItem> page) {
                    List<VideoItem> videoItems = mBook.add(page);
                    dismissLoadingMore();
                    mDataTrans.append(mAdapter, videoItems);
                }

                @Override
                public void onError(Exception e) {
                    dismissLoadingMore();
                    Toast.makeText(getActivity(), e.getMessage() + "", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            mBook.end();
            L.d(this, "loadMore", "end");
        }
    }

    public void showLoadingMore() {
        mLoadMoreAble.setLoadingMore(true);
    }

    public void dismissLoadingMore() {
        mLoadMoreAble.setLoadingMore(false);
    }
}
