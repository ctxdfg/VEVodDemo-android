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
 * Create Date : 2021/8/4
 */
package com.bytedance.volc.voddemo.videoview.pip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.videoview.layer.BaseVideoLayer;
import com.bytedance.volc.voddemo.videoview.layer.ILayer;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerEvent;

import java.util.List;

public class PipCloseLayer extends BaseVideoLayer {

    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, RelativeLayout parent) {
        View close = inflater.inflate(R.layout.layer_pip_close, parent, false);
        close.setOnClickListener(v -> close());
        return close;
    }

    protected void close() {
    }

    @NonNull
    @Override
    public List<Integer> getSupportEvents() {
        return null;
    }

    @Override
    public int getZIndex() {
        return ILayer.PIP_CLOSE_Z_INDEX;
    }

    @Override
    public boolean handleVideoEvent(@NonNull IVideoLayerEvent event) {
        return false;
    }
}
