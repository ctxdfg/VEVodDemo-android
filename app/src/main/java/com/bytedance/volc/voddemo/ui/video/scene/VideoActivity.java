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

package com.bytedance.volc.voddemo.ui.video.scene;

import static com.bytedance.playerkit.player.ui.scene.PlayScene.SCENE_DETAIL;
import static com.bytedance.playerkit.player.ui.scene.PlayScene.SCENE_LONG;
import static com.bytedance.playerkit.player.ui.scene.PlayScene.SCENE_SHORT;
import static com.bytedance.playerkit.player.ui.scene.PlayScene.SCENE_SMALL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.ui.base.BaseActivity;
import com.bytedance.volc.voddemo.ui.base.BaseFragment;
import com.bytedance.volc.voddemo.ui.video.scene.detail.DetailVideoFragment;
import com.bytedance.volc.voddemo.ui.video.scene.longvideo.LongVideoFragment;
import com.bytedance.volc.voddemo.ui.video.scene.shortvideo.ShortVideoFragment;
import com.bytedance.volc.voddemo.ui.video.scene.smallvideo.SmallVideoFragment;


public class VideoActivity extends BaseActivity {

    private static final String EXTRA_VIDEO_SCENE = "extra_video_scene";
    private static final String EXTRA_ARGS = "extra_args";
    private int mScene;
    private Bundle mArgs;

    public static void intentInto(Activity activity, int scene, Bundle args) {
        Intent intent = new Intent(activity, VideoActivity.class);
        intent.putExtra(EXTRA_VIDEO_SCENE, scene);
        intent.putExtra(EXTRA_ARGS, args);
        activity.startActivity(intent);
    }

    public static void intentInto(Activity activity, int scene) {
        intentInto(activity, scene, null);
    }

    @Override
    public void onBackPressed() {
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(getTag(mScene));
        if (fragment != null && fragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScene = getIntent().getIntExtra(EXTRA_VIDEO_SCENE, SCENE_SMALL);
        mArgs = getIntent().getBundleExtra(EXTRA_ARGS);

        sceneTheme(mScene);
        setContentView(R.layout.activity_video);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getActionBarTitle(mScene));
        }

        final String tag = getTag(mScene);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = createFragment(mScene, mArgs);
            fm.beginTransaction().add(R.id.content, fragment, tag).commit();
        } else {
            fm.beginTransaction().attach(fragment).commit();
        }
    }

    private Fragment createFragment(int scene, Bundle bundle) {
        switch (scene) {
            case SCENE_SMALL:
                return SmallVideoFragment.newInstance();
            case SCENE_SHORT:
                return ShortVideoFragment.newInstance();
            case SCENE_LONG:
                return LongVideoFragment.newInstance();
            case SCENE_DETAIL:
                return DetailVideoFragment.newInstance(bundle);
        }
        throw new IllegalArgumentException("unsupported " + scene);
    }

    private String getTag(int scene) {
        switch (scene) {
            case SCENE_SMALL:
                return SmallVideoFragment.class.getName();
            case SCENE_SHORT:
                return ShortVideoFragment.class.getName();
            case SCENE_LONG:
                return LongVideoFragment.class.getName();
            case SCENE_DETAIL:
                return DetailVideoFragment.class.getName();
        }
        throw new IllegalArgumentException("unsupported " + scene);
    }

    private String getActionBarTitle(int scene) {
        switch (scene) {
            case SCENE_SMALL:
                return getString(R.string.small_video);
            case SCENE_SHORT:
                return getString(R.string.short_video);
            case SCENE_LONG:
                return getString(R.string.long_video);
            case SCENE_DETAIL:
                return getString(R.string.detail_video);
        }
        return null;
    }

    private void sceneTheme(int scene) {
        final Window window = getWindow();
        switch (scene) {
            case SCENE_SMALL:
                int flags = window.getDecorView().getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                window.getDecorView().setSystemUiVisibility(flags);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                window.setNavigationBarColor(Color.BLACK);
                //window.setNavigationBarColor(Color.TRANSPARENT);
                break;
            default:
                window.setNavigationBarColor(Color.WHITE);
                break;
        }
    }
}