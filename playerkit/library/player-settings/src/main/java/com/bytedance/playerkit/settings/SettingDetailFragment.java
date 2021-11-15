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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;


public class SettingDetailFragment extends Fragment {

    private SettingItem mSettingItem;

    private TextView mCategory;
    private TextView mKey;
    private TextView mDesc;
    private TextView mType;
    private TextView mStrategy;
    private RecyclerView mRecyclerView;
    private Adapter mAdapter;

    public SettingDetailFragment(SettingItem settingItem) {
        this.mSettingItem = settingItem;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new Adapter();
        mAdapter.setData(mSettingItem.option);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(requireActivity()).inflate(R.layout.setting_detail_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mCategory = view.findViewById(R.id.category);
        mKey = view.findViewById(R.id.key);
        mDesc = view.findViewById(R.id.desc);
        mType = view.findViewById(R.id.type);
        mStrategy = view.findViewById(R.id.strategy);
        mRecyclerView = view.findViewById(R.id.recyclerView);

        Option option = mSettingItem.option;
        mCategory.setText(option.category);
        mKey.setText(option.key);
        mDesc.setText(option.des);
        mType.setText(option.clazz.getSimpleName());
        mStrategy.setText(option.strategy == Option.STRATEGY_IMMEDIATELY ? "Take effect immediately" : "Take effect after app restart");

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
    }

    private static final class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        Option mOption;

        void setData(Option option) {
            this.mOption = option;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.setting_item_option_value, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            //TODO
            final Object defaultValue = mOption.defaultValue;
            final Object value = mOption.value();
            final int valueFrom = mOption.valueFrom();
            final int strategy = mOption.strategy;
            final List<Object> candidates = mOption.candidates;
            final Object remoteValue = mOption.remoteValue();
            final Object userValue = mOption.userValue();
            if (position == 0) {
                holder.text.setText(defaultValue + "[Default]");

                if (valueFrom == Option.VALUE_FROM_DEFAULT) {
                    holder.state.setText("Current");
                    holder.state.setVisibility(View.VISIBLE);
                } else {
                    holder.state.setText(null);
                    holder.state.setVisibility(View.GONE);
                }

            } else {
                Object o = mOption.candidates.get(position - 1);
                StringBuilder suffixBuilder = new StringBuilder();
                if (userValue != null && Objects.equals(o, userValue)) {
                    suffixBuilder.append("User");
                }
                if (remoteValue != null && Objects.equals(o, remoteValue)) {
                    if (suffixBuilder.length() > 0) {
                        suffixBuilder.append(", ");
                    }
                    suffixBuilder.append("Remote");
                }
                String suffix = suffixBuilder.length() > 0 ? "[" + suffixBuilder + "]" : "";
                holder.text.setText(String.valueOf(o) + suffix);

                if (valueFrom == Option.VALUE_FROM_USER) {
                    if (Objects.equals(value, o)) {
                        holder.state.setVisibility(View.VISIBLE);
                        holder.state.setText("Current");
                    } else {
                        holder.state.setVisibility(View.GONE);
                    }
                } else if (valueFrom == Option.VALUE_FROM_REMOTE) {
                    if (Objects.equals(value, o)) {
                        holder.state.setVisibility(View.VISIBLE);
                        holder.state.setText("Current");
                    } else {
                        holder.state.setVisibility(View.GONE);
                    }
                } else if (valueFrom == Option.VALUE_FROM_DEFAULT) {
                    holder.state.setVisibility(View.GONE);
                } else {
                    throw new IllegalStateException();
                }
            }

            holder.itemView.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition == 0) {
                    mOption.userValues().saveValue(mOption, null);
                } else {
                    Object o = candidates.get(adapterPosition - 1);
                    mOption.userValues().saveValue(mOption, o);
                }
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return mOption.candidates.size() + 1;
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;
            TextView state;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                text = itemView.findViewById(R.id.text);
                state = itemView.findViewById(R.id.state);
            }
        }
    }
}
