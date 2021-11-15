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

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.data.model.VideoItem;
import com.bytedance.volc.voddemo.data.page.Book;
import com.bytedance.volc.voddemo.data.page.Page;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.api2.RemoteApi2;
import com.bytedance.volc.voddemo.ui.video.scene.VideoFragment;

import java.util.List;


public class ShortVideoFragment extends VideoFragment {


    private RemoteApi mRemoteApi;
    private final Book<VideoItem> mBook = new Book<>(10);
    private ShortVideoPageView mPageView;

    public ShortVideoFragment() {
        // Required empty public constructor
    }

    public static ShortVideoFragment newInstance() {
        return new ShortVideoFragment();
    }

    @Override
    public boolean onBackPressed() {
        if (mPageView.onBackPressed()) {
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteApi = new RemoteApi2();
    }

    @Override
    protected void initBackPressedHandler() {
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.short_video_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPageView = view.findViewById(R.id.shortVideoPageView);
        mPageView.setLifeCycle(getLifecycle());
        mPageView.setRefreshListener(this::refresh);
        mPageView.setLoadMoreListener(this::loadMore);
        refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRemoteApi.cancel();
    }

    private void refresh() {
        L.d(this, "refresh", "start", 0, mBook.pageSize());
        mPageView.showRefreshing();
        mRemoteApi.getFeedStreamWithPlayAuthToken("small-video", 0, mBook.pageSize(), new RemoteApi.Callback<Page<VideoItem>>() {
            @Override
            public void onSuccess(Page<VideoItem> page) {
                L.d(this, "refresh", "success");
                List<VideoItem> videoItems = mBook.first(page);
                mPageView.dismissRefreshing();
                if (!videoItems.isEmpty()) {
                    mPageView.setList(videoItems);
                }
            }

            @Override
            public void onError(Exception e) {
                L.d(this, "refresh", e, "error");
                mPageView.dismissRefreshing();
                Toast.makeText(getActivity(), e.getMessage() + "", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadMore() {
        if (mBook.hasMore()) {
            if (mPageView.isLoadingMore()) return;
            mPageView.showLoadingMore();
            L.d(this, "loadMore", "start", mBook.nextPageIndex(), mBook.pageSize());
            mRemoteApi.getFeedStreamWithPlayAuthToken("small-video", mBook.nextPageIndex(), mBook.pageSize(), new RemoteApi.Callback<Page<VideoItem>>() {
                @Override
                public void onSuccess(Page<VideoItem> page) {
                    L.d(this, "loadMore", "success", mBook.nextPageIndex());
                    List<VideoItem> videoItems = mBook.add(page);
                    mPageView.dismissLoadingMore();
                    mPageView.append(videoItems);
                }

                @Override
                public void onError(Exception e) {
                    L.d(this, "loadMore", e, "error", mBook.nextPageIndex());
                    mPageView.dismissLoadingMore();
                    Toast.makeText(getActivity(), e.getMessage() + "", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            mBook.end();
            L.d(this, "loadMore", "end");
        }
    }
}
