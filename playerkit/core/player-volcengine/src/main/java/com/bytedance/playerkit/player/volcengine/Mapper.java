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

import static com.bytedance.playerkit.player.source.Track.TRACK_TYPE_AUDIO;
import static com.bytedance.playerkit.player.source.Track.TRACK_TYPE_VIDEO;

import android.text.TextUtils;

import com.bytedance.playerkit.player.cache.CacheKeyFactory;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Quality;
import com.bytedance.playerkit.player.source.Track;
import com.ss.ttvideoengine.Resolution;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.model.BareVideoInfo;
import com.ss.ttvideoengine.model.BareVideoModel;
import com.ss.ttvideoengine.model.IVideoModel;
import com.ss.ttvideoengine.model.VideoInfo;
import com.ss.ttvideoengine.model.VideoModel;
import com.ss.ttvideoengine.model.VideoRef;
import com.ss.ttvideoengine.source.Source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class Mapper {

    private static final Map<Resolution, Quality> map = new LinkedHashMap<>();

    static {
        map.put(Resolution.Standard,
                new Quality(Quality.QUALITY_RES_360,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "360P",
                        Resolution.Standard
                ));

        map.put(Resolution.High,
                new Quality(Quality.QUALITY_RES_480,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "480P",
                        Resolution.High
                ));

        map.put(Resolution.SuperHigh,
                new Quality(Quality.QUALITY_RES_720,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "720P",
                        Resolution.SuperHigh
                ));

        map.put(Resolution.ExtremelyHigh,
                new Quality(Quality.QUALITY_RES_1080,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "1080P",
                        Resolution.ExtremelyHigh));

        map.put(Resolution.FourK,
                new Quality(Quality.QUALITY_RES_4K,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "4K",
                        Resolution.FourK));

        //Resolution.HDR,
        //Resolution.Auto,

        map.put(Resolution.L_Standard,
                new Quality(Quality.QUALITY_RES_240,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "240P",
                        Resolution.L_Standard));

        map.put(Resolution.H_High,
                new Quality(Quality.QUALITY_RES_540,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "540P",
                        Resolution.H_High));


        map.put(Resolution.TwoK,
                new Quality(Quality.QUALITY_RES_2K,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "2K",
                        Resolution.H_High));


        map.put(Resolution.ExtremelyHigh_50F,
                new Quality(Quality.QUALITY_RES_1080,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_50,
                        "1080P 50FPS",
                        Resolution.ExtremelyHigh_50F));

        map.put(Resolution.TwoK_50F,
                new Quality(Quality.QUALITY_RES_2K,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_50,
                        "2K 50FPS",
                        Resolution.TwoK_50F));

        map.put(Resolution.FourK_50F,
                new Quality(Quality.QUALITY_RES_4K,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_50,
                        "4K 50FPS",
                        Resolution.FourK_50F));

        map.put(Resolution.ExtremelyHigh_60F,
                new Quality(Quality.QUALITY_RES_1080,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_60,
                        "1080P 60FPS",
                        Resolution.ExtremelyHigh_60F));

        map.put(Resolution.TwoK_60F,
                new Quality(Quality.QUALITY_RES_2K,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_60,
                        "2K 60FPS",
                        Resolution.TwoK_60F));

        map.put(Resolution.FourK_60F,
                new Quality(Quality.QUALITY_RES_4K,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_60,
                        "4K 60FPS",
                        Resolution.FourK_60F));

        map.put(Resolution.ExtremelyHigh_120F,
                new Quality(Quality.QUALITY_RES_1080,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_120,
                        "1080P 120FPS",
                        Resolution.ExtremelyHigh_120F));

        map.put(Resolution.TwoK_120F,
                new Quality(Quality.QUALITY_RES_2K,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_120,
                        "2K 120FPS",
                        Resolution.TwoK_120F));

        map.put(Resolution.FourK_120F,
                new Quality(Quality.QUALITY_RES_4K,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "4K 120FPS",
                        Resolution.FourK_120F));

        map.put(Resolution.L_Standard_HDR,
                new Quality(Quality.QUALITY_RES_240,
                        Quality.QUALITY_COLOR_RANGE_HDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "240P HDR",
                        Resolution.L_Standard_HDR));


        map.put(Resolution.Standard_HDR,
                new Quality(Quality.QUALITY_RES_360,
                        Quality.QUALITY_COLOR_RANGE_HDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "360P HDR",
                        Resolution.Standard_HDR));

        map.put(Resolution.High_HDR,
                new Quality(Quality.QUALITY_RES_480,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "480P HDR",
                        Resolution.High_HDR));

        map.put(Resolution.H_High_HDR,
                new Quality(Quality.QUALITY_RES_540,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "540P HDR",
                        Resolution.H_High_HDR));

        map.put(Resolution.SuperHigh_HDR,
                new Quality(Quality.QUALITY_RES_720,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "720P HDR",
                        Resolution.SuperHigh_HDR));


        map.put(Resolution.ExtremelyHigh_HDR,
                new Quality(Quality.QUALITY_RES_1080,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "1080P HDR",
                        Resolution.ExtremelyHigh_HDR));

        map.put(Resolution.TwoK_HDR,
                new Quality(Quality.QUALITY_RES_2K,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "2K HDR",
                        Resolution.TwoK_HDR));

        map.put(Resolution.FourK_HDR,
                new Quality(Quality.QUALITY_RES_4K,
                        Quality.QUALITY_COLOR_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "4K HDR",
                        Resolution.FourK_HDR));
    }

    static Quality resolution2Quality(Resolution resolution) {
        Quality quality = map.get(resolution);
        if (quality == null) return null;

        Quality q = new Quality();
        q.setQualityRes(quality.getQualityRes());
        q.setQualityDynamicRange(quality.getQualityDynamicRange());
        q.setQualityFps(quality.getQualityFps());
        q.setQualityDesc(quality.getQualityDesc());
        q.setQualityTag(resolution);
        return q;
    }

    static Resolution track2Resolution(Track track) {
        return quality2Resolution(track.getQuality());
    }

    static Resolution quality2Resolution(Quality quality) {
        if (quality != null) {
            Resolution resolution = (Resolution) quality.getQualityTag();
            if (resolution != null) return resolution;

            for (Map.Entry<Resolution, Quality> entry : map.entrySet()) {
                Quality value = entry.getValue();
                if (Objects.equals(value, quality)) {
                    return entry.getKey();
                }
            }
        }
        return Resolution.Standard;
    }

    public static IVideoModel mediaSource2VideoModel(MediaSource source, CacheKeyFactory cacheKeyFactory) {
        List<VideoInfo> videoInfos = new ArrayList<>();
        List<Track> videoTracks = source.getTracks(TRACK_TYPE_VIDEO);
        if (videoTracks != null && !videoTracks.isEmpty()) {
            for (Track track : videoTracks) {
                videoInfos.add(
                        new BareVideoInfo.Builder()
                                .mediaType(trackType2VideoModelMediaType(track.getTrackType()))
                                .urls(Arrays.asList(track.getUrl()))
                                .fileId(track.getFileId())
                                .fileHash(cacheKeyFactory.generateCacheKey(source, track))
                                .size(track.getFileSize())
                                .bitrate(track.getBitrate())
                                .spadea(track.getEncryptedKey())
                                .resolution(track2Resolution(track))
                                .vWidth(track.getVideoWidth())
                                .vHeight(track.getVideoHeight())
                                .format(trackFormat2VideoModelFormat(track.getFormat()))
                                .codecType(trackEncodeType2VideoModelEncodeType(track.getEncoderType()))
                                .build()
                );
            }
        }
        List<Track> audioTracks = source.getTracks(TRACK_TYPE_AUDIO);
        if (audioTracks != null && !audioTracks.isEmpty()) {
            for (Track track : audioTracks) {
                videoInfos.add(
                        new BareVideoInfo.Builder()
                                .mediaType(trackType2VideoModelMediaType(track.getTrackType()))
                                .urls(Arrays.asList(track.getUrl()))
                                .fileId(track.getFileId())
                                .fileHash(cacheKeyFactory.generateCacheKey(source, track))
                                .size(track.getFileSize())
                                .bitrate(track.getBitrate())
                                .spadea(track.getEncryptedKey())
                                .resolution(track2Resolution(track))
                                .format(trackFormat2VideoModelFormat(track.getFormat()))
                                .codecType(trackEncodeType2VideoModelEncodeType(track.getEncoderType()))
                                .build()
                );
            }
        }
        return new BareVideoModel.Builder()
                .vid(source.getMediaId())
                .setVideoInfos(videoInfos)
                .duration(source.getDuration())
                .build();
    }

    public static void updateMediaSource(MediaSource mediaSource, VideoModel videoModel) {
        mediaSource.setTracks(videoInfoList2TrackList(videoModel));
        mediaSource.setMediaProtocol(videoModelFormat2MediaSourceMediaProtocol(videoModel));

        long duration = videoModel.getVideoRefInt(VideoRef.VALUE_VIDEO_REF_VIDEO_DURATION) * 1000L;
        if (mediaSource.getDuration() <= 0) { // using app server
            mediaSource.setDuration(duration);
        }
        String coverUrl = videoModel.getVideoRefStr(VideoRef.VALUE_VIDEO_REF_POSTER_URL);
        if (TextUtils.isEmpty(mediaSource.getCoverUrl())) { // using app server
            mediaSource.setCoverUrl(coverUrl);
        }
    }

    private static List<Track> videoInfoList2TrackList(VideoModel videoModel) {
        List<Track> tracks = new ArrayList<>();
        List<VideoInfo> videoInfos = videoModel.getVideoInfoList();
        for (VideoInfo videoInfo : videoInfos) {
            tracks.add(videoInfo2Track(videoModel, videoInfo));
        }
        return tracks;
    }

    private static Track videoInfo2Track(VideoModel videoModel, VideoInfo info) {
        Track track = new Track();
        track.setMediaId(videoModel.getVideoRefStr(VideoRef.VALUE_VIDEO_REF_VIDEO_ID));
        track.setTrackType(videoModelMediaType2TrackType(info.getMediatype()));
        track.setUrl(info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_MAIN_URL));
        track.setBackupUrls(transBackupUrls(info));
        track.setFileId(info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_FILEID));
        track.setFileHash(info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_FILE_HASH));
        track.setFileSize(info.getValueLong(VideoInfo.VALUE_VIDEO_INFO_SIZE));
        track.setBitrate(info.getValueInt(VideoInfo.VALUE_VIDEO_INFO_BITRATE));
        //track.setPreloadSize();

        track.setEncryptedKey(info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_PLAY_AUTH));
        track.setEncryptedKeyId(info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_KID));

        track.setFormat(videoModelFormat2TrackFormat(info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_FORMAT_TYPE)));
        track.setEncoderType(videoModelEncodeType2TrackEncodeType(info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_CODEC_TYPE)));
        track.setVideoWidth(info.getValueInt(VideoInfo.VALUE_VIDEO_INFO_VWIDTH));
        track.setVideoHeight(info.getValueInt(VideoInfo.VALUE_VIDEO_INFO_VHEIGHT));
        track.setQuality(resolution2Quality(info.getResolution()));
        return track;
    }

    private static int videoModelFormat2TrackFormat(String format) {
        if (format != null) {
            switch (format) {
                case TTVideoEngine.FORMAT_TYPE_DASH:
                    return Track.FORMAT_DASH;
                case TTVideoEngine.FORMAT_TYPE_HLS:
                    return Track.FORMAT_HLS;
                case TTVideoEngine.FORMAT_TYPE_MP4:
                    return Track.FORMAT_MP4;
            }
        }
        return Track.FORMAT_MP4;
    }

    public static String trackFormat2VideoModelFormat(@Track.Format int format) {
        switch (format) {
            case Track.FORMAT_DASH:
                return TTVideoEngine.FORMAT_TYPE_DASH;
            case Track.FORMAT_HLS:
                return TTVideoEngine.FORMAT_TYPE_HLS;
            case Track.FORMAT_MP4:
                return TTVideoEngine.FORMAT_TYPE_MP4;
        }
        throw new IllegalArgumentException("unsupported format " + format);
    }

    public static int videoModelEncodeType2TrackEncodeType(String encodeType) {
        if (encodeType != null) {
            switch (encodeType) {
                case Source.EncodeType.H264:
                    return Track.ENCODER_TYPE_H264;
                case Source.EncodeType.h265:
                    return Track.ENCODER_TYPE_H265;
                case Source.EncodeType.h266:
                    return Track.ENCODER_TYPE_H266;
            }
        }
        return Track.ENCODER_TYPE_H264;
    }

    public static String trackEncodeType2VideoModelEncodeType(@Track.Encoder int encodeType) {
        switch (encodeType) {
            case Track.ENCODER_TYPE_H264:
                return Source.EncodeType.H264;
            case Track.ENCODER_TYPE_H265:
                return Source.EncodeType.h265;
            case Track.ENCODER_TYPE_H266:
                return Source.EncodeType.h266;
        }
        throw new IllegalArgumentException("unsupported encoderType " + encodeType);
    }

    private static int videoModelFormat2MediaSourceMediaProtocol(VideoModel videoModel) {
        if (videoModel.isDashSource()) {
            return MediaSource.MEDIA_PROTOCOL_DASH;
        } else if (videoModel.isHlsSource()) {
            return MediaSource.MEDIA_PROTOCOL_HLS;
        } else {
            return MediaSource.MEDIA_PROTOCOL_DEFAULT;
        }
    }

    private static int videoModelMediaType2TrackType(int mediaType) {
        if (mediaType == VideoRef.TYPE_AUDIO) {
            return TRACK_TYPE_AUDIO;
        } else if (mediaType == VideoRef.TYPE_VIDEO) {
            return TRACK_TYPE_VIDEO;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static int trackType2VideoModelMediaType(@Track.TrackType int trackType) {
        if (trackType == TRACK_TYPE_VIDEO) {
            return VideoRef.TYPE_VIDEO;
        } else if (trackType == TRACK_TYPE_AUDIO) {
            return VideoRef.TYPE_AUDIO;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static List<String> transBackupUrls(VideoInfo info) {
        String backUpUrl = info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_BACKUP_URL_1);
        List<String> urls = new ArrayList<>();
        if (!TextUtils.isEmpty(backUpUrl)) {
            urls.add(backUpUrl);
        }
        return urls;
    }
}