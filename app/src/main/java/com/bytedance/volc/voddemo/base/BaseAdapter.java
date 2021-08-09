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
package com.bytedance.volc.voddemo.base;

import android.util.SparseArray;
import android.view.View;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseAdapter.ViewHolder<T>> {
    private final List<T> mDatas = new ArrayList<>();

    public BaseAdapter(List<T> datas) {
        if (datas != null) {
            mDatas.addAll(datas);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder<T> holder, final int position) {
        holder.setupViews(position, mDatas.get(position));
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public T getItem(int position) {
        return mDatas.get(position);
    }

    public void setData(List<T> datas) {
        mDatas.clear();
        mDatas.addAll(datas);
    }

    public void replaceItem(int index, T item) {
        mDatas.set(index, item);
    }

    public void replaceAll(List<T> datas) {
        mDatas.clear();
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    public void addAll(List<T> datas) {
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    public List<T> getAll() {
        return mDatas;
    }

    public static abstract class ViewHolder<T> extends RecyclerView.ViewHolder {
        private final View mItemView;
        private final SparseArray<View> mViews = new SparseArray<>();

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mItemView = itemView;
        }

        public View getView(@IdRes int resourceId) {
            View view = mViews.get(resourceId);
            if (view == null) {
                view = mItemView.findViewById(resourceId);
                mViews.put(resourceId, view);
            }
            return view;
        }

        public abstract void setupViews(final int position, T data);
    }
}



