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

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class SettingItem {
    public static final int TYPE_CATEGORY_TITLE = 1;
    public static final int TYPE_OPTION = 2;

    public final int type;
    public String category;
    public Option option;

    public SettingItem(Option option) {
        this.type = TYPE_OPTION;
        this.option = option;
    }

    public SettingItem(String category) {
        this.type = TYPE_CATEGORY_TITLE;
        this.category = category;
    }

    public static List<SettingItem> resolveSettingItems(Settings settings) {
        List<SettingItem> settingItems = new ArrayList<>();
        List<Option> allOptions = settings.options();
        Map<String, List<Option>> optionMap = new LinkedHashMap<>();
        for (Option option : allOptions) {
            List<Option> options = optionMap.get(option.category);
            if (options == null) {
                options = new ArrayList<>();
                optionMap.put(option.category, options);
            }
            options.add(option);
        }
        String category = null;
        for (Map.Entry<String, List<Option>> entry : optionMap.entrySet()) {
            List<Option> options = entry.getValue();
            Option first = options.get(0);
            if (!TextUtils.equals(category, first.category)) {
                category = first.category;
                settingItems.add(new SettingItem(category));
            }
            for (Option option : options) {
                settingItems.add(new SettingItem(option));
            }
        }
        return settingItems;
    }
}
