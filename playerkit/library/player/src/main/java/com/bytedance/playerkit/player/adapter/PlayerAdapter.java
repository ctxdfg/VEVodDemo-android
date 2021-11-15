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

package com.bytedance.playerkit.player.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.PlayerException;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;

public interface PlayerAdapter {
    /**
     * Scaling mode. One of
     * {@link #SCALING_MODE_DEFAULT},
     * {@link #SCALING_MODE_ASPECT_FIT},
     * {@link #SCALING_MODE_ASPECT_FILL}
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SCALING_MODE_DEFAULT,
            SCALING_MODE_ASPECT_FIT,
            SCALING_MODE_ASPECT_FILL})
    @interface ScalingMode {
    }

    /**
     * @see MediaPlayer#VIDEO_SCALING_MODE_SCALE_TO_FIT
     */
    int SCALING_MODE_DEFAULT = 0;
    int SCALING_MODE_ASPECT_FIT = 1;
    int SCALING_MODE_ASPECT_FILL = 2;

    class Info {
        /* Do not change these values without updating their counterparts
         * in include/media/mediaplayer.h!
         */
        /**
         * Unspecified media player info.
         *
         * @see Listener#onInfo(PlayerAdapter, int, int)
         */
        public static final int MEDIA_INFO_UNKNOWN = MediaPlayer.MEDIA_INFO_UNKNOWN;

        /**
         * The player just pushed the very first video frame for rendering.
         *
         * @see Listener#onInfo(PlayerAdapter, int, int)
         */
        public static final int MEDIA_INFO_VIDEO_RENDERING_START = MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;


        public static final int MEDIA_INFO_AUDIO_RENDERING_START = 4;

        /**
         * The video is too complex for the decoder: it can't decode frames fast
         * enough. Possibly only the audio plays fine at this stage.
         *
         * @see Listener#onInfo(PlayerAdapter, int, int)
         */
        public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING;

        /**
         * MediaPlayer is temporarily pausing playback internally in order to
         * buffer more data.
         *
         * @see Listener#onInfo(PlayerAdapter, int, int)
         */
        public static final int MEDIA_INFO_BUFFERING_START = MediaPlayer.MEDIA_INFO_BUFFERING_START;

        /**
         * MediaPlayer is resuming playback after filling buffers.
         *
         * @see Listener#onInfo(PlayerAdapter, int, int)
         */
        public static final int MEDIA_INFO_BUFFERING_END = MediaPlayer.MEDIA_INFO_BUFFERING_END;

        /**
         * Bad interleaving means that a media has been improperly interleaved or
         * not interleaved at all, e.g has all the video samples first then all the
         * audio ones. Video is playing but a lot of disk seeks may be happening.
         *
         * @see android.media.MediaPlayer.OnInfoListener
         */
        public static final int MEDIA_INFO_BAD_INTERLEAVING = MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING;

        /**
         * The media cannot be seeked (e.g live stream)
         *
         * @see android.media.MediaPlayer.OnInfoListener
         */
        public static final int MEDIA_INFO_NOT_SEEKABLE = MediaPlayer.MEDIA_INFO_NOT_SEEKABLE;

        /**
         * A new set of metadata is available.
         *
         * @see android.media.MediaPlayer.OnInfoListener
         */
        public static final int MEDIA_INFO_METADATA_UPDATE = MediaPlayer.MEDIA_INFO_METADATA_UPDATE;

        /**
         * Informs that audio is not playing. Note that playback of the video
         * is not interrupted.
         *
         * @see android.media.MediaPlayer.OnInfoListener
         */
        public static final int MEDIA_INFO_AUDIO_NOT_PLAYING = MediaPlayer.MEDIA_INFO_AUDIO_NOT_PLAYING;

        /**
         * Informs that video is not playing. Note that playback of the audio
         * is not interrupted.
         *
         * @see android.media.MediaPlayer.OnInfoListener
         */
        public static final int MEDIA_INFO_VIDEO_NOT_PLAYING = MediaPlayer.MEDIA_INFO_VIDEO_NOT_PLAYING;

        /**
         * Subtitle track was not supported by the media framework.
         *
         * @see android.media.MediaPlayer.OnInfoListener
         */
        public static final int MEDIA_INFO_UNSUPPORTED_SUBTITLE = MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE;

        /**
         * Reading the subtitle track takes too long.
         *
         * @see android.media.MediaPlayer.OnInfoListener
         */
        public static final int MEDIA_INFO_SUBTITLE_TIMED_OUT = MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE;
    }

    class SourceLoadInfo {
        public static final int SOURCE_INFO_PLAY_INFO_FETCHED = 0;

        public static final int SOURCE_INFO_SUBTITLE_INFO_FETCHED = 1;

        public static final int SOURCE_INFO_MASK_INFO_FETCHED = 2;
    }

    interface Factory {
        PlayerAdapter create();
    }

    /**
     * Same with {@link android.media.MediaDataSource} with no api limit.
     */
    abstract class MediaDataSource implements Closeable {

        public abstract int readAt(long position, byte[] buffer, int offset, int size)
                throws IOException;

        public abstract long getSize() throws IOException;
    }

    void setListener(final Listener listener);

    void setSurface(@Nullable Surface surface);

    void setDisplay(@Nullable SurfaceHolder display);

    void setVideoScalingMode(@ScalingMode int mode);

    void setDataSource(@NonNull MediaSource source) throws IOException;

    /**
     * @see android.media.MediaPlayer#setDataSource(String)
     */
    void setDataSource(@NonNull String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    /**
     * @see android.media.MediaPlayer#setDataSource(Context, Uri, Map)
     */
    void setDataSource(@NonNull Context context, @NonNull Uri uri, @Nullable Map<String, String> headers)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    /**
     * @see android.media.MediaPlayer#setDataSource(FileDescriptor)
     */
    void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException;

    /**
     * @see android.media.MediaPlayer#setDataSource(android.media.MediaDataSource)
     */
    void setDataSource(MediaDataSource dataSource) throws IllegalArgumentException, IllegalStateException;

    boolean isSupportSmoothTrackSwitching(@Track.TrackType int trackType);

    void selectTrack(@Track.TrackType int trackType, @Nullable Track track) throws IllegalStateException;

    Track getPendingTrack(@Track.TrackType int trackType) throws IllegalStateException;

    Track getCurrentTrack(@Track.TrackType int trackType) throws IllegalStateException;

    List<Track> getTracks(@Track.TrackType int trackType) throws IllegalStateException;

    void setStartTime(long startTime);

    long getStartTime();

    void prepareAsync() throws IllegalStateException;

    void start() throws IllegalStateException;

    boolean isPlaying();

    void pause() throws IllegalStateException;

    void stop() throws IllegalStateException;

    void reset();

    void release();

    void seekTo(long seekTo);

    long getDuration();

    long getCurrentPosition();

    int getBufferPercentage();

    int getVideoWidth();

    int getVideoHeight();

    void setLooping(boolean looping);

    boolean isLooping();

    void setVolume(float leftVolume, float rightVolume);

    float[] getVolume();

    void setSpeed(final float speed);

    float getSpeed();

    void setAudioPitch(final float audioPitch);

    float getAudioPitch();

    void setAudioSessionId(int audioSessionId);

    int getAudioSessionId();

    interface Listener extends SourceInfoListener, TrackListener {

        void onPrepared(@NonNull PlayerAdapter mp);

        void onCompletion(@NonNull PlayerAdapter mp);

        void onError(@NonNull PlayerAdapter mp, int code, @NonNull String msg);

        void onSeekComplete(@NonNull PlayerAdapter mp);

        void onVideoSizeChanged(@NonNull PlayerAdapter mp, int width, int height);

        void onSARChanged(@NonNull PlayerAdapter mp, int num, int den);

        void onBufferingUpdate(@NonNull PlayerAdapter mp, int percent);

        void onInfo(@NonNull PlayerAdapter mp, int what, int extra);

        void onCacheHint(PlayerAdapter mp, long cacheSize);
    }

    interface SourceInfoListener {
        void onSourceInfoLoadStart(PlayerAdapter mp, int type, MediaSource source);

        void onSourceInfoLoadComplete(PlayerAdapter mp, int type, MediaSource source);

        void onSourceInfoLoadError(PlayerAdapter mp, int type, PlayerException e);
    }

    interface TrackListener {

        void onTrackInfoReady(@NonNull PlayerAdapter mp, @Track.TrackType int trackType, @NonNull List<Track> tracks);

        void onTrackWillChange(@NonNull PlayerAdapter mp, @Track.TrackType int trackType, @Nullable Track current, @NonNull Track target);

        void onTrackChanged(@NonNull PlayerAdapter mp, @Track.TrackType int trackType, @NonNull Track pre, @NonNull Track current);
    }
}
