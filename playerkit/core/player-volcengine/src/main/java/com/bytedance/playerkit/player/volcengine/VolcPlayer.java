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
import static com.ss.ttvideoengine.TTVideoEngine.PLAYBACK_STATE_PAUSED;
import static com.ss.ttvideoengine.TTVideoEngine.PLAYER_TYPE_OWN;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bytedance.applog.AppLog;
import com.bytedance.playerkit.player.adapter.PlayerAdapter;
import com.bytedance.playerkit.player.cache.CacheKeyFactory;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Quality;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.utils.L;
import com.ss.ttm.player.IMediaDataSource;
import com.ss.ttm.player.PlaybackParams;
import com.ss.ttvideoengine.PlayerEventListener;
import com.ss.ttvideoengine.PlayerEventSimpleListener;
import com.ss.ttvideoengine.Resolution;
import com.ss.ttvideoengine.SeekCompletionListener;
import com.ss.ttvideoengine.SubInfoListener;
import com.ss.ttvideoengine.SubInfoSimpleCallBack;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.VideoEngineCallback;
import com.ss.ttvideoengine.VideoEngineInfoListener;
import com.ss.ttvideoengine.VideoEngineInfos;
import com.ss.ttvideoengine.VideoFormatInfo;
import com.ss.ttvideoengine.VideoInfoListener;
import com.ss.ttvideoengine.model.IVideoModel;
import com.ss.ttvideoengine.model.VideoModel;
import com.ss.ttvideoengine.utils.Error;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class VolcPlayer implements PlayerAdapter {
    private final Context mContext;
    private final CacheKeyFactory mCacheKeyFactory;
    private final TTVideoEngine mPlayer;
    private final PlaybackParams mPlaybackParams = new PlaybackParams();
    private final ListenerAdapter mListenerAdapter;
    private PlayerAdapter.Listener mListener;
    private long mStartTime;
    private MediaSource mSource;

    private final SparseArray<Track> mCurrentTrack = new SparseArray<>();
    private final SparseArray<Track> mPendingTrack = new SparseArray<>();

    private boolean mPrepared;
    private boolean mPausedWhenChangeVideoTrack;
    private boolean mHlsSeamlessQualitySwitching = false;

    public static class Factory implements PlayerAdapter.Factory {
        private final Context mContext;
        private final CacheKeyFactory mCacheKeyFactory;

        public Factory(Context context, CacheKeyFactory cacheKeyFactory) {
            this.mContext = context;
            this.mCacheKeyFactory = cacheKeyFactory;
        }

        @Override
        public PlayerAdapter create() {
            return new VolcPlayer(mContext, mCacheKeyFactory);
        }
    }

    private VolcPlayer(final Context context, CacheKeyFactory cacheKeyFactory) {
        L.v(this, "constructor", "DEVICE_ID", AppLog.getDid());
        mContext = context;
        mCacheKeyFactory = cacheKeyFactory;
        mListenerAdapter = new ListenerAdapter(this);
        final ListenerAdapter2 mListenerAdapter2 = new ListenerAdapter2(this);
        final ListenerAdapter3 mListenerAdapter3 = new ListenerAdapter3(this);

        mPlayer = createPlayer(mContext);
        mPlayer.setVideoEngineCallback(mListenerAdapter);
        mPlayer.setVideoEngineInfoListener(mListenerAdapter);
        mPlayer.setVideoInfoListener(mListenerAdapter);
        mPlayer.setSubInfoCallBack(mListenerAdapter2);
        mPlayer.setPlayerEventListener(mListenerAdapter3);
        initPlayer(mPlayer);
    }

    protected TTVideoEngine createPlayer(Context context) {
        Map<String, Object> params = new HashMap<>();
        if (VolcSettings.get().option(VolcSettings.PLAYER_OPTION_ASYNC_PLAYER).booleanValue()) {
            params.put("enable_looper", true);
        }
        return new TTVideoEngine(context, PLAYER_TYPE_OWN, params);
    }

    protected void initPlayer(TTVideoEngine player) {
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_DATALOADER, 1);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_USE_VIDEOMODEL_CACHE, 1);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_DEBUG_UI_NOTIFY, 1);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_DASH, 1);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_BASH, 1);

        player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABEL_HARDWARE_DECODE,
                VolcSettings.get().option(VolcSettings.PLAYER_OPTION_HARDWARE_DECODER).booleanValue() ? 1 : 0);

        String encoderType = VolcSettings.get().option(VolcSettings.PLAYER_OPTION_ENCODER_TYPE).stringValue();
        if (TextUtils.equals(encoderType, "h264")) {
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_h265, 0);
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_h266, 0);
        } else if (TextUtils.equals(encoderType, "h265")) {
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_h265, 1);
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_h266, 0);
        } else if (TextUtils.equals(encoderType, "h266")) {
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_h265, 0);
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_h266, 1);
        }
        if (VolcSettings.get().option(VolcSettings.PLAYER_OPTION_OUTPUT_LOG).booleanValue()) {
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_OUTPUT_LOG, 1);
        }
        if (VolcSettings.get().option(VolcSettings.PLAYER_OPTION_ENABLE_HLS_SEAMLESS_SWITCH).booleanValue()) {
            mHlsSeamlessQualitySwitching = true;
            mPlayer.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_HLS_SEAMLESS_SWITCH, 1);
        }
        if (VolcSettings.get().option(VolcSettings.PLAYER_OPTION_HARDWARE_DECODER_ASYNC_INIT).booleanValue()) {
            player.setAsyncInit(true, -1);
        }
    }

    @Override
    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void setSurface(final Surface surface) {
        mPlayer.setSurface(surface);
    }

    @Override
    public void setDisplay(SurfaceHolder display) {
        mPlayer.setSurfaceHolder(display);
    }

    @Override
    public void setVideoScalingMode(@PlayerAdapter.ScalingMode int mode) {
        int scalingMode = TTVideoEngine.IMAGE_LAYOUT_TO_FILL;
        switch (mode) {
            case PlayerAdapter.SCALING_MODE_DEFAULT:
                scalingMode = TTVideoEngine.IMAGE_LAYOUT_TO_FILL;
                break;
            case PlayerAdapter.SCALING_MODE_ASPECT_FIT:
                scalingMode = TTVideoEngine.IMAGE_LAYOUT_ASPECT_FIT;
                break;
            case PlayerAdapter.SCALING_MODE_ASPECT_FILL:
                scalingMode = TTVideoEngine.IMAGE_LAYOUT_ASPECT_FILL;
                break;
        }
        mPlayer.setIntOption(TTVideoEngine.PLAYER_OPTION_IMAGE_LAYOUT, scalingMode);
    }

    @Override
    public void setDataSource(@NonNull MediaSource source) throws IOException {
        mSource = source;
        final int sourceType = source.sourceType();
        final int mediaType = source.getMediaType();
        if (sourceType == MediaSource.SOURCE_TYPE_ID) {
            final String mediaId = source.getMediaId();
            final String playAuthToken = source.getPlayAuthToken();
            if (mediaId == null || playAuthToken == null) {
                throw new IOException(new NullPointerException(
                        "mediaId = " + mediaId + "; playAuthToken = " + playAuthToken));
            }
            mPlayer.setVideoID(mediaId);
            mPlayer.setPlayAuthToken(playAuthToken);
        } else if (sourceType == MediaSource.SOURCE_TYPE_URL) {
            @Track.TrackType final int trackType = mediaType == MediaSource.MEDIA_TYPE_AUDIO ?
                    TRACK_TYPE_AUDIO : TRACK_TYPE_VIDEO;
            //mListener.onSourceInfoLoadComplete(this, trackType, mSource);
            IVideoModel model = Mapper.mediaSource2VideoModel(source, mCacheKeyFactory);
            if (model == null) {
                throw new IOException("media source 2 video model error");
            }
            mPlayer.setVideoModel(model);
            selectPlayTrackInTrackInfoReady(source, trackType);
        } else {
            throw new IllegalArgumentException("unsupported sourceType " + sourceType);
        }

        setHeaders(mSource.getHeaders());
    }

    private void selectPlayTrackInTrackInfoReady(@NonNull MediaSource source, @Track.TrackType int trackType) {
        List<Track> tracks = source.getTracks(trackType);
        if (tracks != null && !tracks.isEmpty()) {
            // You should select track in this callback.
            // IllegalStateException will be throw if not selected
            if (mListener != null) {
                mListener.onTrackInfoReady(this, trackType, tracks);
            }
            final Track track = getPendingTrack(trackType);

            if (track == null) {
                throw new IllegalStateException("Video track not selected!");
            }
        }
    }

    @Override
    public void setDataSource(@NonNull String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        final Uri uri;
        File file = null;
        if (path.startsWith("/")) {
            file = new File(path);
        }
        if (file != null && file.exists()) {
            uri = Uri.fromFile(file);
        } else {
            uri = Uri.parse(path);
        }
        setDataSource(mContext, uri, null);
    }

    @Override
    public void setDataSource(@NonNull Context context, @NonNull Uri uri, @Nullable Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        final String cacheKey = mCacheKeyFactory.generateCacheKey(uri.toString());
        mPlayer.setDirectUrlUseDataLoader(uri.toString(), cacheKey);
        setHeaders(headers);
    }

    @Override
    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        mPlayer.setDataSource(fd, 0, 0);
    }

    @Override
    public void setDataSource(final MediaDataSource dataSource) throws IllegalArgumentException, IllegalStateException {
        mPlayer.setDataSource(new IMediaDataSource() {

            @Override
            public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
                return dataSource.readAt(position, buffer, offset, size);
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public long getSize() throws IOException {
                return dataSource.getSize();
            }

            @Override
            public void close() throws IOException {
                dataSource.close();
            }
        });
    }

    private void setHeaders(@Nullable Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null && value != null) {
                    // compatible with ffmpeg
                    mPlayer.setCustomHeader(key, " " + value);
                }
            }
        }
    }

    @Override
    public boolean isSupportSmoothTrackSwitching(@Track.TrackType int trackType) {
        if (mPlayer != null && mSource != null) {
            int contentType = mSource.getMediaProtocol();
            if (contentType == MediaSource.MEDIA_PROTOCOL_DASH) {
                return true;
            } else if (contentType == MediaSource.MEDIA_PROTOCOL_HLS) {
                IVideoModel videoModel = mPlayer.getIVideoModel();
                if (videoModel != null && videoModel.isSupportHLSSeamlessSwitch()) {
                    return mHlsSeamlessQualitySwitching;
                }
            }
        }
        return false;
    }

    @Override
    public void selectTrack(@Track.TrackType int trackType, @Nullable Track track) throws IllegalStateException {
        if (trackType == TRACK_TYPE_VIDEO) {
            if (track == null) {
                List<Track> tracks = getTracks(trackType);
                if (tracks != null && !tracks.isEmpty()) {
                    track = tracks.get(0);
                }
            }

            if (track == null) return; //TODO abr auto support

            Resolution resolution = Mapper.track2Resolution(track);
            if (resolution == null) return;

            setPendingTrack(trackType, track);

            if (mListener != null) {
                mListener.onTrackWillChange(this, trackType, getCurrentTrack(trackType), track);
            }

            mPausedWhenChangeVideoTrack = mPlayer.getPlaybackState() == PLAYBACK_STATE_PAUSED;
            mPlayer.configResolution(resolution);
        } else {
            throw new IllegalArgumentException("Only support video track for now. " + Track.mapTrackType(trackType));
        }
    }

    @Override
    public Track getPendingTrack(@Track.TrackType int trackType) throws IllegalStateException {
        return mPendingTrack.get(trackType);
    }

    private Track removePendingTrack(@Track.TrackType int type) {
        final Track track = mPendingTrack.get(type);
        if (track != null) {
            mPendingTrack.remove(type);
        }
        return track;
    }

    private void setPendingTrack(@Track.TrackType int type, Track track) {
        mPendingTrack.put(type, track);
    }

    private void setCurrentTrack(@Track.TrackType int type, Track track) {
        this.mCurrentTrack.put(type, track);
    }

    @Override
    public Track getCurrentTrack(@Track.TrackType int trackType) throws IllegalStateException {
        return mCurrentTrack.get(trackType);
    }

    @Override
    public List<Track> getTracks(@Track.TrackType int trackType) throws IllegalStateException {
        return mSource.getTracks(trackType);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        mPlayer.prepare();
    }

    @Override
    public void start() {
        mPlayer.play();
    }

    @Override
    public boolean isPlaying() {
        return mPlayer.getPlaybackState() == TTVideoEngine.PLAYBACK_STATE_PLAYING;
    }

    @Override
    public void pause() {
        mPlayer.pause();
    }

    @Override
    public void stop() {
        mStartTime = 0;
        mPrepared = false;
        mPlayer.stop();
    }

    @Override
    public void setStartTime(long startTime) {
        mStartTime = startTime;
        mPlayer.setStartTime((int) startTime);
    }

    @Override
    public long getStartTime() {
        return mStartTime;
    }

    @Override
    public void seekTo(final long seekTo) {
        mPlayer.seekTo((int) seekTo, mListenerAdapter);
    }

    @Override
    public void reset() {
        L.e(this, "reset", "unsupported reset method, stop instead");
        mPlayer.stop();
        resetInner();
    }

    @Override
    public void release() {
        mPlayer.setIsMute(true);
        mPlayer.releaseAsync();
        resetInner();
    }

    private void resetInner() {
        mStartTime = 0;
        mPrepared = false;
        mPausedWhenChangeVideoTrack = false;
        mHlsSeamlessQualitySwitching = false;
        mSource = null;
        mCurrentTrack.clear();
        mPendingTrack.clear();
        mListener = null;
    }

    @Override
    public long getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public long getCurrentPosition() {
        return mPlayer.getCurrentPlaybackTime();
    }

    @Override
    public int getBufferPercentage() {
        return mPlayer.getLoadedProgress();
    }

    @Override
    public int getVideoWidth() {
        return mPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return mPlayer.getVideoHeight();
    }

    @Override
    public void setLooping(final boolean looping) {
        mPlayer.setLooping(looping);
    }

    @Override
    public boolean isLooping() {
        return mPlayer.isLooping();
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        //mPlayer.setIntOption(TTVideoEngine.PLAYER_OPTION_SET_TRACK_VOLUME, 1);
        float maxVolume = mPlayer.getMaxVolume();
        float left = maxVolume * leftVolume;
        float right = maxVolume * rightVolume;
        mPlayer.setVolume(left, right);
        mVolume[0] = left;
        mVolume[1] = right;
    }

    final float[] mVolume = new float[]{1f, 1f};

    @Override
    public float[] getVolume() {
        //mPlayer.setIntOption(TTVideoEngine.PLAYER_OPTION_SET_TRACK_VOLUME, 1);
        float maxVolume = mPlayer.getMaxVolume();
        float volume = mPlayer.getVolume();
        if (volume >= 0 && volume < maxVolume) {
            volume = volume / maxVolume;
        }
        mVolume[0] = volume;
        mVolume[1] = volume;
        return mVolume;
    }

    @Override
    public void setSpeed(float speed) {
        mPlaybackParams.setSpeed(speed);
        mPlayer.setPlaybackParams(mPlaybackParams);
    }

    @Override
    public float getSpeed() {
        float speed = mPlaybackParams.getSpeed();
        return speed == -1f ? 1f : speed;
    }

    @Override
    public void setAudioPitch(float audioPitch) {
        mPlaybackParams.setPitch(audioPitch);
        mPlayer.setPlaybackParams(mPlaybackParams);
    }

    @Override
    public float getAudioPitch() {
        return mPlaybackParams.getPitch();
    }

    @Override
    public void setAudioSessionId(int audioSessionId) {
        mPlayer.setIntOption(TTVideoEngine.PLAYER_OPTION_AUDIOTRACK_SESSIONID, audioSessionId);
    }

    @Override
    public int getAudioSessionId() {
        return mPlayer.getIntOption(TTVideoEngine.PLAYER_OPTION_AUDIOTRACK_SESSIONID);
    }

    private static class ListenerAdapter2 extends SubInfoSimpleCallBack implements SubInfoListener {

        private final WeakReference<VolcPlayer> mPlayerRef;

        ListenerAdapter2(VolcPlayer player) {
            this.mPlayerRef = new WeakReference<>(player);
        }

        @Override
        public void onSubPathInfo(String subPathInfo, Error error) {
        }

        @Override
        public void onSubSwitchCompleted(int success, int subId) {
        }

        @Override
        public void onSubLoadFinished(int success) {
        }

        @Override
        public void onSubInfoCallback(int code, int pts, String info) {
        }

        @Override
        public void onSubInfoCallback(int code, String info) {

        }
    }

    private static class ListenerAdapter3 extends PlayerEventSimpleListener {

        private final WeakReference<VolcPlayer> mPlayerRef;

        ListenerAdapter3(VolcPlayer player) {
            this.mPlayerRef = new WeakReference<>(player);
        }

        @Override
        public void onMediaOpened(VideoFormatInfo format, long start, long end) {
        }

        @Override
        public void onVideoDecoderOpened(int codecName, long start, long end) {
        }

        @Override
        public void onVideoDecodedFirstFrame(long timeStamp) {
        }

        @Override
        public void onVideoInputFormatChanged(VideoFormatInfo format) {
        }

        @Override
        public void onVideoRenderOpened(int renderType, long start, long end) {
        }

        @Override
        public void onAudioDecoderOpened(int codecName, long start, long end) {
        }

        @Override
        public void onAudioRenderOpened(int renderType, long start, long end) {
            final VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onInfo(player, Info.MEDIA_INFO_AUDIO_RENDERING_START, 0);
        }

        @Override
        public void onAudioInputFormatChanged(VideoFormatInfo format) {
        }
    }

    private static class ListenerAdapter
            implements VideoEngineCallback,
            SeekCompletionListener,
            VideoInfoListener,
            VideoEngineInfoListener,
            PlayerEventListener {

        private final WeakReference<VolcPlayer> mPlayerRef;

        ListenerAdapter(VolcPlayer player) {
            this.mPlayerRef = new WeakReference<>(player);
        }

        @Override
        public void onPrepared(final TTVideoEngine engine) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;
            MediaSource source = player.mSource;
            if (source == null) return;

            player.mPrepared = true; // mark prepared

            int mediaType = source.getMediaType();
            @Track.TrackType
            int trackType = mediaType == MediaSource.MEDIA_TYPE_AUDIO ? TRACK_TYPE_AUDIO : TRACK_TYPE_VIDEO;

            //TODO dash abr
            final Track current = player.getCurrentTrack(trackType);
            final Track pending = player.removePendingTrack(trackType);

            if (pending != null) {
                player.setCurrentTrack(trackType, pending);
                listener.onTrackChanged(player, trackType, current, pending);
            }

            if (current == null || player.isSupportSmoothTrackSwitching(trackType)) {
                listener.onPrepared(player);
            } else {
                if (player.mPausedWhenChangeVideoTrack) {
                    player.mPausedWhenChangeVideoTrack = false;
                } else {
                    player.start();
                }
            }
        }

        @Override
        public void onBufferStart(final int reason, final int afterFirstFrame, final int action) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onInfo(player, PlayerAdapter.Info.MEDIA_INFO_BUFFERING_START, 0);
        }

        @Override
        public void onBufferEnd(final int code) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onInfo(player, PlayerAdapter.Info.MEDIA_INFO_BUFFERING_END, 0);
        }

        @Override
        public void onBufferingUpdate(final TTVideoEngine engine, final int percent) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onBufferingUpdate(player, percent);
        }

        @Override
        public void onCompletion(final TTVideoEngine engine) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onCompletion(player);
        }

        @Override
        public void onError(final Error error) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            player.mPrepared = false;
            listener.onError(player, error.code, error.toString());
        }

        @Override
        public void onRenderStart(final TTVideoEngine engine) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onInfo(player, PlayerAdapter.Info.MEDIA_INFO_VIDEO_RENDERING_START, 0/*TODO*/);
        }

        @Override
        public void onReadyForDisplay(TTVideoEngine engine) {
        }

        @Override
        public void onVideoSizeChanged(final TTVideoEngine engine, final int width, final int height) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onVideoSizeChanged(player, width, height);
        }

        @Override
        public void onPlaybackStateChanged(TTVideoEngine engine, int playbackState) {
        }

        @Override
        public void onLoadStateChanged(TTVideoEngine engine, int loadState) {
        }

        @Override
        public void onPrepare(TTVideoEngine engine) {
        }

        @Override
        public void onStreamChanged(TTVideoEngine engine, int type) {
        }

        @Override
        public void onVideoStatusException(int status) {
        }

        @Override
        public void onABRPredictBitrate(int mediaType, int bitrate) {
        }

        @Override
        public void onSARChanged(int num, int den) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onSARChanged(player, num, den);
        }

        @Override
        public void onVideoURLRouteFailed(Error error, String url) {
        }

        @Override
        public void onVideoStreamBitrateChanged(Resolution resolution, int bitrate) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            Track current = player.getCurrentTrack(TRACK_TYPE_VIDEO);
            Track track = player.getPendingTrack(TRACK_TYPE_VIDEO);

            final Quality quality = Mapper.resolution2Quality(resolution);

            if (track != null && Objects.equals(track.getQuality(), quality)) {
                player.removePendingTrack(TRACK_TYPE_VIDEO);
                player.setCurrentTrack(TRACK_TYPE_VIDEO, track);
                listener.onTrackChanged(player, TRACK_TYPE_VIDEO, current, track);
            }
        }

        @Override
        public void onFrameDraw(int frameCount, Map map) {
        }

        /**
         * Seek completion
         *
         * @param seekable current stream is seekable. true for seekable otherwise not.
         */
        @Override
        public void onCompletion(boolean seekable) {
        }

        @Override
        public boolean onFetchedVideoInfo(VideoModel videoModel) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return false;
            Listener listener = player.mListener;
            if (listener == null) return false;

            // NOTE: fix onFetchedVideoInfo callback multi times
            if (player.mPrepared) return false;

            MediaSource source = player.mSource;
            if (source == null) return false;

            Mapper.updateMediaSource(source, videoModel);

            listener.onSourceInfoLoadComplete(player, SourceLoadInfo.SOURCE_INFO_PLAY_INFO_FETCHED, source);

            @Track.TrackType
            int trackType = source.getMediaType() == MediaSource.MEDIA_TYPE_AUDIO ? TRACK_TYPE_AUDIO : TRACK_TYPE_VIDEO;

            player.selectPlayTrackInTrackInfoReady(source, trackType);
            return false;
        }

        @Override
        public void onVideoEngineInfos(VideoEngineInfos videoEngineInfos) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            switch (videoEngineInfos.getKey()) {
                case VideoEngineInfos.USING_RENDER_SEEK_COMPLETE:
                    listener.onSeekComplete(player);
                    return;
                case VideoEngineInfos.USING_MDL_HIT_CACHE_SIZE:
                    String taskKey = videoEngineInfos.getUsingMDLPlayTaskKey(); // 使用的 key 信息
                    long hitCacheSize = videoEngineInfos.getUsingMDLHitCacheSize(); // 命中缓存文件 size
                    listener.onCacheHint(player, hitCacheSize);
                    return;
                case VideoEngineInfos.USING_MDL_CACHE_END:
                    return;
            }
        }

        @Override
        public void onMediaOpened(VideoFormatInfo format, long start, long end) {

        }

        @Override
        public void onVideoDecoderOpened(int codecName, long start, long end) {

        }

        @Override
        public void onVideoDecodedFirstFrame(long timeStamp) {

        }

        @Override
        public void onVideoInputFormatChanged(VideoFormatInfo format) {

        }

        @Override
        public void onVideoRenderOpened(int renderType, long start, long end) {

        }

        @Override
        public void onAudioDecoderOpened(int codecName, long start, long end) {

        }

        @Override
        public void onAudioRenderOpened(int renderType, long start, long end) {

        }

        @Override
        public void onAudioInputFormatChanged(VideoFormatInfo format) {

        }
    }
}
