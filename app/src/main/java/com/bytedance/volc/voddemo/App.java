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

package com.bytedance.volc.voddemo;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.cache.CacheKeyFactory;
import com.bytedance.playerkit.player.source.Quality;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.source.TrackSelector;
import com.bytedance.playerkit.player.volcengine.VolcPlayerInit;
import com.bytedance.playerkit.utils.L;

import java.util.List;

public class App extends Application {

    private static final String APP_ID = "229234";
    private static final String APP_NAME = "VOLCVodDemo";
    private static final String APP_CHANNEL = "VOLCVodDemoAndroid";
    private static final String APP_REGION = "china";
    private static final String APP_VERSION = BuildConfig.VERSION_NAME;

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;

        L.ENABLE_LOG = true;

        VolcPlayerInit.AppInfo appInfo = new VolcPlayerInit.AppInfo.Builder()
                .setAppId(APP_ID)
                .setAppName(APP_NAME)
                .setAppRegion(APP_REGION)
                .setAppChannel(APP_CHANNEL)
                .setAppVersion(APP_VERSION)
                .setLicenseUri("assets:///license2/volc_vod_demo_license2.lic")
                .build();

        final int qualityRes = Quality.QUALITY_RES_720;

        final TrackSelector trackSelector = new TrackSelector() {
            @NonNull
            @Override
            public Track selectTrack(int type, int trackType, @NonNull List<Track> tracks) {
                for (Track track : tracks) {
                    Quality quality = track.getQuality();
                    if (quality != null) {
                        if (quality.getQualityRes() == qualityRes) {
                            return track;
                        }
                    }
                }
                return tracks.get(0);
            }
        };
        VolcPlayerInit.init(this, appInfo, CacheKeyFactory.DEFAULT, trackSelector);
    }

    public Context context() {
        return sContext;
    }
}
