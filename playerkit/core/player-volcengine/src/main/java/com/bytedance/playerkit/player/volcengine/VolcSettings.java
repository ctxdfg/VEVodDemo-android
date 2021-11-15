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

package com.bytedance.playerkit.player.volcengine;

import android.content.Context;

import com.bytedance.playerkit.settings.Option;
import com.bytedance.playerkit.settings.S;
import com.bytedance.playerkit.settings.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VolcSettings {
    public static final String KEY = "VolcSettings";

    public static final String CATEGORY_OPEN_API = "OpenAPI";
    public static final String CATEGORY_PLAYER = "Player";
    public static final String CATEGORY_MDL = "MediaLoader";

    public static final String PLAYER_OPTION_OUTPUT_LOG = "player_option_output_log";
    public static final String PLAYER_OPTION_ASYNC_PLAYER = "player_option_async_player";
    public static final String PLAYER_OPTION_ENABLE_HLS_SEAMLESS_SWITCH = "player_option_enable_hls_seamless_switch";
    public static final String PLAYER_OPTION_HARDWARE_DECODER_ASYNC_INIT = "player_option_hardware_decoder_async_init";
    public static final String PLAYER_OPTION_HARDWARE_DECODER = "player_option_hardware_decoder";
    public static final String PLAYER_OPTION_ENCODER_TYPE = "player_option_encoder_type";

    public static final String DATA_LOADER_KEY_MAX_CACHE_SIZE = "data_loader_key_int_max_cache_size";

    static void init(Context context) {
        final List<Option> options = new ArrayList<>();

        initOpenApiOptions(options);

        initPlayerOptions(options);

        initMDLOptions(options);

        Settings settings = new S(context, options, option -> {
            if (sRemoteValueProvider == null) {
                return null;
            }
            return sRemoteValueProvider.getValue(option);
        });

        Settings.Provider.put(KEY, settings);
    }

    private static Settings.RemoteValues sRemoteValueProvider;

    public static void setRemoteValueProvider(Settings.RemoteValues provider) {
        sRemoteValueProvider = provider;
    }

    public static Settings get() {
        return Settings.Provider.get(KEY);
    }

    static void initOpenApiOptions(List<Option> options) {
        options.add(new Option(CATEGORY_OPEN_API,
                PLAYER_OPTION_ENCODER_TYPE, "源编码类型", Option.STRATEGY_IMMEDIATELY, String.class, "h264",
                Arrays.asList("h264", "h265", "h266"),
                Arrays.asList("openapi", "encoder", "codec")));
    }

    static void initPlayerOptions(List<Option> options) {
        options.add(new Option(CATEGORY_PLAYER,
                PLAYER_OPTION_OUTPUT_LOG, "内核日志开关", Option.STRATEGY_IMMEDIATELY, Boolean.class, Boolean.FALSE,
                Arrays.asList(Boolean.TRUE, Boolean.FALSE),
                Arrays.asList("player", "log", "debug"))
        );

        options.add(new Option(CATEGORY_PLAYER,
                PLAYER_OPTION_ASYNC_PLAYER, "异步线程执行内核方法", Option.STRATEGY_IMMEDIATELY, Boolean.class, Boolean.TRUE,
                Arrays.asList(Boolean.TRUE, Boolean.FALSE),
                Arrays.asList("player", "init", "async")));

        options.add(new Option(CATEGORY_PLAYER,
                PLAYER_OPTION_ENABLE_HLS_SEAMLESS_SWITCH, "HLS 平滑切换开关", Option.STRATEGY_IMMEDIATELY, Boolean.class, Boolean.TRUE,
                Arrays.asList(Boolean.TRUE, Boolean.FALSE),
                Arrays.asList("player", "hls", "quality", "seamless")));

        options.add(new Option(CATEGORY_PLAYER,
                PLAYER_OPTION_HARDWARE_DECODER_ASYNC_INIT, "硬解异步初始化解码器", Option.STRATEGY_IMMEDIATELY, Boolean.class, Boolean.TRUE,
                Arrays.asList(Boolean.TRUE, Boolean.FALSE),
                Arrays.asList("player", "hardware", "decoder", "async_init")));

        options.add(new Option(CATEGORY_PLAYER,
                PLAYER_OPTION_HARDWARE_DECODER, "硬解", Option.STRATEGY_IMMEDIATELY, Boolean.class, true,
                Arrays.asList(Boolean.TRUE, Boolean.FALSE),
                Arrays.asList("player", "hardware", "decoder")));
    }

    static void initMDLOptions(List<Option> options) {
        options.add(new Option(CATEGORY_MDL,
                DATA_LOADER_KEY_MAX_CACHE_SIZE, "MDL 磁盘缓存大小", Option.STRATEGY_RESTART_APP, Integer.class, 300 * 1024 * 1024,
                Arrays.asList(0),
                Arrays.asList("mdl", "cache", "size", "file")));
    }
}
