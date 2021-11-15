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

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface Settings {

    class Provider {
        private static final Map<String, Settings> sMap = new HashMap<>();

        public static synchronized void put(String key, Settings settings) {
            sMap.put(key, settings);
        }

        public static synchronized Settings get(String key) {
            return sMap.get(key);
        }
    }

    interface RemoteValues {
        @Nullable
        Object getValue(Option option);
    }

    interface UserValues {

        @Nullable
        Object getValue(Option option);

        void saveValue(Option option, @Nullable Object value);
    }

    Option option(String key);

    List<Option> options();

    UserValues userValues();

    RemoteValues remoteValues();
}
