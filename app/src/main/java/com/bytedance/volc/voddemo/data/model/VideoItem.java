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

package com.bytedance.volc.voddemo.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.ui.layer.TitleBarLayer;


public class VideoItem implements Parcelable {
    public static final String EXTRA_VIDEO_ITEM = "extra_video_item";

    public static final int SOURCE_TYPE_VID = 0;
    public static final int SOURCE_TYPE_URL = 1;

    private VideoItem() {
    }

    protected VideoItem(Parcel in) {
        vid = in.readString();
        playAuthToken = in.readString();
        videoModel = in.readString();
        duration = in.readLong();
        title = in.readString();
        cover = in.readString();
        subtitleAuthToken = in.readString();
        barrageMaskUrl = in.readString();
        url = in.readString();
        urlCacheKey = in.readString();
        h264Url = in.readString();
        h264CacheKey = in.readString();
        h265Url = in.readString();
        h265CacheKey = in.readString();
        h264PlayAuth = in.readString();
        h265PlayAuth = in.readString();
        sourceType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vid);
        dest.writeString(playAuthToken);
        dest.writeString(videoModel);
        dest.writeLong(duration);
        dest.writeString(title);
        dest.writeString(cover);
        dest.writeString(subtitleAuthToken);
        dest.writeString(barrageMaskUrl);
        dest.writeString(url);
        dest.writeString(urlCacheKey);
        dest.writeString(h264Url);
        dest.writeString(h264CacheKey);
        dest.writeString(h265Url);
        dest.writeString(h265CacheKey);
        dest.writeString(h264PlayAuth);
        dest.writeString(h265PlayAuth);
        dest.writeInt(sourceType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VideoItem> CREATOR = new Creator<VideoItem>() {
        @Override
        public VideoItem createFromParcel(Parcel in) {
            return new VideoItem(in);
        }

        @Override
        public VideoItem[] newArray(int size) {
            return new VideoItem[size];
        }
    };

    public static VideoItem createVidItem(
            String vid,
            String playAuthToken,
            String subtitleAuthToken,
            long duration,
            String cover,
            String title) {
        VideoItem videoItem = new VideoItem();
        videoItem.sourceType = SOURCE_TYPE_VID;
        videoItem.vid = vid;
        videoItem.playAuthToken = playAuthToken;
        videoItem.subtitleAuthToken = subtitleAuthToken;
        videoItem.duration = duration;
        videoItem.cover = cover;
        videoItem.title = title;
        return videoItem;
    }

    public static VideoItem createUrlItem(
            String vid,
            String url,
            String urlCacheKey,
            String barrageMaskUrl,
            String subtitleAuthToken,
            long duration,
            String cover,
            String title) {
        VideoItem videoItem = new VideoItem();
        videoItem.sourceType = SOURCE_TYPE_URL;
        videoItem.vid = vid;
        videoItem.url = url;
        videoItem.urlCacheKey = urlCacheKey;
        videoItem.barrageMaskUrl = barrageMaskUrl;
        videoItem.subtitleAuthToken = subtitleAuthToken;
        videoItem.duration = duration;
        videoItem.cover = cover;
        videoItem.title = title;
        return videoItem;
    }

    public static VideoItem createUrlItem(
            String vid,
            String h264Url,
            String h264CacheKey,
            String h264PlayAuth,
            String h265Url,
            String h265CacheKey,
            String h265PlayAuth,
            String barrageMaskUrl,
            String subtitleAuthToken,
            long duration,
            String cover,
            String title) {
        VideoItem videoItem = new VideoItem();
        videoItem.sourceType = SOURCE_TYPE_URL;
        videoItem.vid = vid;
        videoItem.h264Url = h264Url;
        videoItem.h264CacheKey = h264CacheKey;
        videoItem.h264PlayAuth = h264PlayAuth;
        videoItem.h265Url = h265Url;
        videoItem.h265CacheKey = h265CacheKey;
        videoItem.h265PlayAuth = h265PlayAuth;
        videoItem.barrageMaskUrl = barrageMaskUrl;
        videoItem.subtitleAuthToken = subtitleAuthToken;
        videoItem.duration = duration;
        videoItem.cover = cover;
        videoItem.title = title;
        return videoItem;
    }

    private String vid;

    private String playAuthToken;

    private String videoModel;

    private long duration;

    private String title;

    private String cover;

    private String subtitleAuthToken;

    private String barrageMaskUrl;

    private String url;

    private String urlCacheKey;

    private String h264Url;

    private String h264CacheKey;

    private String h265Url;

    private String h265CacheKey;

    private String h264PlayAuth;

    private String h265PlayAuth;

    private int sourceType;

    public String getVid() {
        return vid;
    }

    public String getPlayAuthToken() {
        return playAuthToken;
    }

    public String getVideoModel() {
        return videoModel;
    }

    public long getDuration() {
        return duration;
    }

    public String getTitle() {
        return title;
    }

    public String getCover() {
        return cover;
    }

    public String getSubtitleAuthToken() {
        return subtitleAuthToken;
    }

    public String getBarrageMaskUrl() {
        return barrageMaskUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlCacheKey() {
        return urlCacheKey;
    }

    public String getH264Url() {
        return h264Url;
    }

    public String getH264CacheKey() {
        return h264CacheKey;
    }

    public String getH265Url() {
        return h265Url;
    }

    public String getH265CacheKey() {
        return h265CacheKey;
    }

    public String getH264PlayAuth() {
        return h264PlayAuth;
    }

    public String getH265PlayAuth() {
        return h265PlayAuth;
    }

    public int getSourceType() {
        return sourceType;
    }

    @NonNull
    public static MediaSource toMediaSource(VideoItem videoItem, boolean sycProgress) {
        final MediaSource mediaSource;
        if (videoItem.sourceType == VideoItem.SOURCE_TYPE_VID) {
            mediaSource = MediaSource.createIdSource(videoItem.vid, videoItem.playAuthToken);
        } else if (videoItem.sourceType == VideoItem.SOURCE_TYPE_URL) {
            mediaSource = MediaSource.createUrlSource(videoItem.vid, videoItem.url, videoItem.urlCacheKey);
        } else {
            throw new IllegalArgumentException("unsupported source type! " + videoItem.sourceType);
        }
        mediaSource.setCoverUrl(videoItem.cover);
        mediaSource.setDuration(videoItem.duration);
        mediaSource.putExtra(EXTRA_VIDEO_ITEM, videoItem);
        mediaSource.putExtra(TitleBarLayer.EXTRA_TITLE, videoItem.title);
        if (sycProgress) {
            mediaSource.setSyncProgressId(videoItem.vid); // continues play
        }
        return mediaSource;
    }
}
