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
 * Create Date : 2021/6/10
 */
package com.bytedance.volc.voddemo.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.fragment.app.Fragment;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.longvideo.DetailFragment;
import com.bytedance.volc.voddemo.longvideo.LongVideoFragment;
import com.bytedance.volc.voddemo.settings.SettingActivity;
import com.bytedance.volc.voddemo.smallvideo.SmallVideoFragment;
import com.bytedance.volc.voddemo.videoview.pip.PipController;

import static com.bytedance.volc.voddemo.data.VideoItem.VIDEO_TYPE_LONG;
import static com.bytedance.volc.voddemo.data.VideoItem.VIDEO_TYPE_SMALL;

public class ShellActivity extends TransActivity {

    final private static String ARG_VIDEO_TYPE = "pages_shell_activity_arg_video_type";

    public static void startNewIntent(Activity from, int videoType) {
        Intent intent = new Intent(from, ShellActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_VIDEO_TYPE, videoType);
        intent.putExtras(bundle);
        from.startActivity(intent);
    }

    private int mVideoType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shell);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mVideoType = bundle.getInt(ARG_VIDEO_TYPE);
        }

        final View ivSettings = findViewById(R.id.iv_settings);
        ivSettings.setOnClickListener(v -> {
            final Intent intent = new Intent(ShellActivity.this, SettingActivity.class);
            startActivity(intent);
        });

        setFullScreenContainer(findViewById(android.R.id.content));
        PipController.init(this);

        Fragment fragment;
        if (mVideoType == VIDEO_TYPE_SMALL) {
            ivSettings.setVisibility(View.VISIBLE);
            fragment = getSupportFragmentManager().findFragmentByTag(SmallVideoFragment.TAG);
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().show(fragment).commit();
            } else {
                fragment = new SmallVideoFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, fragment, SmallVideoFragment.TAG)
                        .commit();
            }
        } else if (mVideoType == VIDEO_TYPE_LONG) {
            ivSettings.setVisibility(View.GONE);
            fragment = getSupportFragmentManager().findFragmentByTag(LongVideoFragment.TAG);
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().show(fragment).commit();
            } else {
                fragment = LongVideoFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, fragment, LongVideoFragment.TAG)
                        .commit();
            }
        } else {
            finish();
        }
    }
}
