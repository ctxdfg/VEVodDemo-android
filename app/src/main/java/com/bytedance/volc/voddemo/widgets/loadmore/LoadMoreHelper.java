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

package com.bytedance.volc.voddemo.widgets.loadmore;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class LoadMoreHelper {

    public static LoadMoreAble help(RecyclerView recyclerView) {

        return new LoadMoreAble(recyclerView);
    }

    public static class LoadMoreAble {

        private boolean isLoadingMore;

        private RecyclerView recyclerView;

        private OnLoadMoreListener mOnLoadMoreListener;

        public LoadMoreAble(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
            recyclerView.addOnScrollListener(onScrollListener);
        }

        public LoadMoreAble setOnLoadMoreListener(OnLoadMoreListener listener) {
            this.mOnLoadMoreListener = listener;
            return this;
        }

        public void removeOnLoadMoreListener() {
            recyclerView.removeOnScrollListener(onScrollListener);
            mOnLoadMoreListener = null;
        }

        private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (canTriggerLoadMore() && mOnLoadMoreListener != null) {
                    mOnLoadMoreListener.onLoadMore();
                }
            }
        };

        public void setLoadingMore(boolean loadingMore) {
            isLoadingMore = loadingMore;
        }

        public boolean isLoadingMore() {
            return isLoadingMore;
        }

        protected boolean canTriggerLoadMore() {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
            return lastVisiblePosition + 1 >= recyclerView.getAdapter().getItemCount()
                    && !isLoadingMore;
        }
    }
}
