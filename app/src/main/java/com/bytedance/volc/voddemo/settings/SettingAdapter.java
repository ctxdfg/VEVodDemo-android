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
 * Create Date : 2021/2/26
 */
package com.bytedance.volc.voddemo.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.base.BaseAdapter;
import java.util.List;

public class SettingAdapter extends BaseAdapter<SettingItem> {

    public SettingAdapter(final List<SettingItem> datas) {
        super(datas);
    }

    @NonNull
    @Override
    public ViewHolder<SettingItem> onCreateViewHolder(@NonNull final ViewGroup parent,
            final int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_setting_bool, parent, false);
        return new BoolSettingViewHolder(view);
    }

    @Override
    public int getItemViewType(final int position) {
        SettingItem item = getItem(position);
        return item.getType();
    }

    private static class BoolSettingViewHolder extends ViewHolder<SettingItem> {
        public BoolSettingViewHolder(@NonNull final View itemView) {
            super(itemView);
        }

        @Override
        public void setupViews(final int position, final SettingItem data) {
            BoolSettingItem settingItem = (BoolSettingItem) data;
            ((TextView) getView(R.id.txt_test_text)).setText(settingItem.getText());
            SwitchCompat switchCompat = (SwitchCompat) getView(R.id.test_switcher);
            switchCompat.setEnabled(settingItem.isEnable());
            switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
                settingItem.setDefaultValue(isChecked);
                settingItem.getFunction().onSave(isChecked);
            });

            settingItem.setEnableListener(enable -> {
                switchCompat.setChecked(false);
                switchCompat.setEnabled(enable);
            });
            switchCompat.setChecked(settingItem.isDefaultValue());
        }
    }
}
