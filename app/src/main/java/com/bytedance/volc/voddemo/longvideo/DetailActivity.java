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
 * Create Date : 2021/8/1
 */
package com.bytedance.volc.voddemo.longvideo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.base.TransActivity;
import com.bytedance.volc.voddemo.data.VideoItem;

/**
 * Created by zhangfan.frank on 8/2/21
 */
public class DetailActivity extends TransActivity {

    private static final String EXTRA_VIDEO_ITEM = "extra_video_item";

    public static void intentInto(Context context, VideoItem videoItem) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_VIDEO_ITEM, videoItem);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setFullScreenContainer(findViewById(android.R.id.content));
        Intent intent = getIntent();
        VideoItem videoItem = (VideoItem) intent.getParcelableExtra(EXTRA_VIDEO_ITEM);

        DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DetailFragment.TAG);
        if (detailFragment == null) {
            detailFragment = DetailFragment.newInstance(videoItem);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, detailFragment, DetailFragment.TAG)
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .show(detailFragment)
                    .commit();
        }
    }
}
