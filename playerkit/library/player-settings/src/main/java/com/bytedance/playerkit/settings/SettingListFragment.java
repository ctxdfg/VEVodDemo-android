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
 * Create Date : 2021/12/3
 */

package com.bytedance.playerkit.settings;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class SettingListFragment extends Fragment {
    public static final String EXTRA_SETTINGS_KEY = "EXTRA_SETTINGS_KEY";

    public static Fragment newInstance(String settingsKey) {
        Fragment fragment = new SettingListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SETTINGS_KEY, settingsKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    private RecyclerView recyclerView;
    private Adapter mAdapter;
    private List<SettingItem> mItems;
    private Settings mSettings;

    public SettingListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String key = requireArguments().getString("EXTRA_SETTINGS_KEY");
        mSettings = Settings.Provider.get(key);
        mItems = SettingItem.resolveSettingItems(mSettings);
        mAdapter = new Adapter();
        if (mItems != null) {
            mAdapter.setList(mItems);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(requireActivity()).inflate(R.layout.setting_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerDecoration());
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    static class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<SettingItem> mItems = new ArrayList<>();

        void setList(List<SettingItem> items) {
            mItems.clear();
            mItems.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case SettingItem.TYPE_CATEGORY_TITLE:
                    return new CategoryViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.setting_item_catogory, parent, false));
                case SettingItem.TYPE_OPTION:
                    return new OptionViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.setting_item_option, parent, false));
            }
            throw new IllegalArgumentException("Unsupported view type!");
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            SettingItem settingItem = mItems.get(position);
            switch (holder.getItemViewType()) {
                case SettingItem.TYPE_CATEGORY_TITLE:
                    CategoryViewHolder categoryHolder = (CategoryViewHolder) holder;
                    categoryHolder.category.setText(settingItem.category);
                    return;
                case SettingItem.TYPE_OPTION:
                    OptionViewHolder optionHolder = (OptionViewHolder) holder;
                    optionHolder.name.setText(settingItem.option.key);
                    optionHolder.desc.setText(settingItem.option.des);
                    optionHolder.value.setText(String.valueOf(settingItem.option.value()));
                    int from = settingItem.option.valueFrom();
                    switch (from) {
                        case Option.VALUE_FROM_DEFAULT:
                            optionHolder.value.setTextColor(Color.DKGRAY);
                            break;
                        case Option.VALUE_FROM_REMOTE:
                            optionHolder.value.setTextColor(Color.GREEN);
                            break;
                        case Option.VALUE_FROM_USER:
                            optionHolder.value.setTextColor(Color.RED);
                            break;
                    }
                    optionHolder.itemView.setOnClickListener(v -> {
                        FragmentActivity activity = (FragmentActivity) v.getContext();
                        SettingDetailFragment fragment = new SettingDetailFragment(settingItem);
                        activity.getSupportFragmentManager().beginTransaction()
                                .addToBackStack(null)
                                .replace(android.R.id.content, fragment)
                                .commit();
                    });
                    return;
            }
            throw new IllegalArgumentException("Unsupported view type!");
        }

        @Override
        public int getItemViewType(int position) {
            return mItems.get(position).type;
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        static class CategoryViewHolder extends RecyclerView.ViewHolder {
            TextView category;

            CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                category = itemView.findViewById(R.id.category);
            }
        }

        static class OptionViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView desc;
            TextView value;

            OptionViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                desc = itemView.findViewById(R.id.desc);
                value = itemView.findViewById(R.id.value);
            }
        }
    }

    static class DividerDecoration extends RecyclerView.ItemDecoration {

        private final int dividerHeight;
        private final Paint dividerPaint;

        public DividerDecoration() {
            dividerPaint = new Paint();
            dividerPaint.setColor(Color.GRAY);
            dividerHeight = 1;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.bottom = dividerHeight;
        }

        @Override
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent,
                           @NonNull RecyclerView.State state) {
            int childCount = parent.getChildCount();
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            for (int i = 0; i < childCount - 1; i++) {
                View view = parent.getChildAt(i);
                float top = view.getBottom();
                float bottom = view.getBottom() + dividerHeight;
                c.drawRect(left, top, right, bottom, dividerPaint);
            }
        }
    }
}
