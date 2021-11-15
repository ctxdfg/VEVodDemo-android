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

package com.bytedance.playerkit.player.playback;

import static com.bytedance.playerkit.player.source.MediaSource.mediaEquals;
import static com.bytedance.playerkit.utils.event.Dispatcher.EventListener;

import android.os.Looper;
import android.view.Surface;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.playback.event.ActionStartPlayback;
import com.bytedance.playerkit.player.playback.event.ActionStopPlayback;
import com.bytedance.playerkit.player.playback.event.StateBindPlayer;
import com.bytedance.playerkit.player.playback.event.StateBindVideoView;
import com.bytedance.playerkit.player.playback.event.StateUnbindPlayer;
import com.bytedance.playerkit.player.playback.event.StateUnbindVideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.utils.Asserts;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;

import java.lang.ref.WeakReference;

/**
 * Session controller of playback.
 *
 * <p> NOTE: All public method of {@code PlaybackController} must be called in <b>main thread</b>.
 * Otherwise, {@link IllegalThreadStateException} will be throw.
 *
 * <p>The main responsibility of PlaybackController is controlling playback pipeline.
 * <ol>
 *   <li>Hold {@link Player} and {@link VideoView} instance of current playback.</li>
 *   <li>Control the <b>Start</b> and <b>Stop</b> of the playback session.</li>
 *   <li>Set {@link VideoView}'s Surface instance to {@link Player} to render when Surface is ready</li>
 * </ol>
 *
 * <p>Calling {@link #bind(VideoView)} to bind the {@link VideoView} instance to PlaybackController.
 * Calling {@link #unbind()} to unbind the {@link VideoView} and {@link Player} instance.
 *
 * <p>{@link Player} instance will be bounded automatically when you calling
 * {@link #startPlayback()}. {@link Player} instance will be unbound and recycled when you calling
 * {@link #stopPlayback()}. A {@link PlayerPool} instance is required to fetch the player instance.
 * Using {@link PlayerPool#DEFAULT} if the PlayerPool is not passed by constructor.
 *
 * <p>You should always Calling {@link #startPlayback()} and {@link #stopPlayback()} to start or
 * stop a playback instead calling {@link Player#prepare(MediaSource)} + {@link Player#start()} to
 * start playback or {@link} calling {@link Player#stop()} or {@link Player#release()} to stop
 * playback.
 *
 * <p>PlaybackController only controls the <b>Start</b> and <b>Stop</b> of the playback session.
 * You should get the player instance {@link #player()} to control <b>Pause</b> <b>Seek</b> and
 * other behaviors or query player state during playback.
 *
 * <p> A simple demonstration of usage of PlayerKit.
 *
 * <pre>
 * {@code
 * public class MainActivity {
 *   VideoView videoView;
 *
 *   @Override
 *   public void onCreate() {
 *     // ...
 *     videoView = findViewById(R.id.videoView);
 *
 *     // bind layers
 *     VideoLayerHost layerHost = new VideoLayerHost(context);
 *     layerHost.addLayer(new CoverLayer());
 *     layerHost.addLayer(new LoadingLayer());
 *     layerHost.addLayer(new PauseLayer());
 *     layerHost.addLayer(new SimpleProgressBarLayer());
 *     layerHost.attachToVideoView(videoView);
 *
 *     // bind playback controller
 *     PlaybackController controller = new PlaybackController();
 *     controller.bind(videoView)
 *
 *     // select display mode
 *     videoView.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FIT);
 *     // select display view TextureView or SurfaceView
 *     videoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW);
 *
 *     // bind media data source
 *     videoView.bindDataSource(createSource())
 *   }
 *
 *    // create a media source
 *   MediaSource createSource() {
 *     MediaSource mediaSource = new MediaSource(UUID.randomUUID().toString(), MediaSource.SOURCE_TYPE_URL);
 *     Track track0 = new Track();
 *     track0.setTrackType(Track.TRACK_TYPE_VIDEO);
 *     track0.setUrl("https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_480_1_5MG.mp4"); // 480x270
 *     track0.setQuality(new Quality(Quality.QUALITY_RES_240, "240P"));
 *
 *     Track track1 = new Track();
 *     track1.setTrackType(Track.TRACK_TYPE_VIDEO);
 *     track1.setUrl("https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_640_3MG.mp4"); // 640x360
 *     track1.setQuality(new Quality(Quality.QUALITY_RES_360, "360P"));
 *
 *     Track track2 = new Track();
 *     track2.setTrackType(Track.TRACK_TYPE_VIDEO);
 *     track2.setUrl("https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_1280_10MG.mp4"); // 1280x720
 *     track2.setQuality(new Quality(Quality.QUALITY_RES_720, "720P"));
 *
 *     Track track3 = new Track();
 *     track3.setTrackType(Track.TRACK_TYPE_VIDEO);
 *     track3.setUrl("https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_1920_18MG.mp4"); // 1920x1080
 *     track3.setQuality(new Quality(Quality.QUALITY_RES_1080, "1080P"));
 *
 *     // You can switch quality of current playback by calling {@link Player#selectTrack(int, Track)}.
 *     // You can only add one track, If you don't have multi quality of tracks.
 *     mediaSource.setTracks(Arrays.asList(track0, track1, track2, track3));
 *     return source;
 *   }
 *
 *   @Override
 *   public void onResume() {
 *     videoView.startPlayback();
 *   }
 *
 *   @Override
 *   public void onPause() {
 *     videoView.pausePlayback();
 *   }
 *
 *   @Override
 *   public void onDestroy() {
 *     videoView.stopPlayback();
 *   }
 * }
 * }
 * </pre>
 *
 * @see PlayerPool
 * @see Player
 * @see MediaSource
 * @see VideoView
 */
public class PlaybackController {

    private VideoView mVideoView;
    private Player mPlayer;

    private final PlayerPool mPlayerPool;
    private final SurfaceListener mSurfaceListener;
    private final PlayerListener mPlayerListener;
    private final Dispatcher mDispatcher;

    private Runnable mStartOnReadyCommand;

    @MainThread
    public PlaybackController() {
        this(PlayerPool.DEFAULT);
    }

    @MainThread
    public PlaybackController(PlayerPool playerPool) {
        Asserts.checkMainThread();
        mPlayerPool = playerPool;
        mSurfaceListener = new SurfaceListener(this);
        mPlayerListener = new PlayerListener(this);
        mDispatcher = new Dispatcher(Looper.getMainLooper());
    }

    @MainThread
    public final void addPlaybackListener(EventListener listener) {
        Asserts.checkMainThread();
        mDispatcher.addEventListener(listener);
    }

    @MainThread
    public final void removePlaybackListener(EventListener listener) {
        Asserts.checkMainThread();
        mDispatcher.removeEventListener(listener);
    }

    @MainThread
    public final void removeAllPlaybackListeners() {
        Asserts.checkMainThread();
        mDispatcher.removeAllEventListener();
    }

    @MainThread
    public VideoView videoView() {
        Asserts.checkMainThread();
        return mVideoView;
    }

    @MainThread
    public Player player() {
        Asserts.checkMainThread();
        return mPlayer;
    }

    /**
     * bind/unbind videoView to playback.
     *
     * @param videoView bind videoView instance to PlaybackController. Passing null to unbind
     *                  the pre bound videoView.
     */
    @MainThread
    public void bind(@Nullable VideoView videoView) {
        Asserts.checkMainThread();
        L.d(this, "bind", mVideoView, videoView);
        if (mVideoView != null && mVideoView != videoView) {
            unbindVideoView();
        }
        bindVideoView(videoView);
    }

    /**
     * Unbind pre bound {@link Player} and {@link VideoView} instance. This is useful when your
     * PlaybackController instance is static. Calling this method unbind VideoView and Player to
     * avoid memory leak.
     *
     * <p>If there is a {@link Player} instance. The instance will be unbound.
     * {@link StateUnbindPlayer} will be emitted by {@link EventListener} added by
     * {@link #addPlaybackListener(EventListener)}.
     *
     * <p>If there is a {@link VideoView} instance. The instance will be unbound.
     * {@link StateUnbindVideoView} will be emitted by {@link EventListener} added by
     * {@link #addPlaybackListener(EventListener)}
     */
    @MainThread
    public void unbind() {
        L.d(this, "unbind", mVideoView, mPlayer);
        Asserts.checkMainThread();
        mStartOnReadyCommand = null;
        unbindPlayer(false);
        unbindVideoView();
    }

    private void bindPlayer(Player newPlayer) {
        if (mPlayer == null && newPlayer != null && !newPlayer.isReleased()) {
            L.d(this, "bindPlayer", mPlayer, newPlayer);
            mPlayer = newPlayer;
            mPlayer.addPlayerListener(mPlayerListener);
            mDispatcher.obtain(StateBindPlayer.class, this).init(newPlayer).dispatch();
        }
    }

    private void unbindPlayer(boolean recycle) {
        if (mPlayer != null) {
            L.d(this, "unbindPlayer", mPlayer, recycle);
            if (recycle) {
                mPlayer.setSurface(null);
                mPlayerPool.recycle(mPlayer);
            }
            mPlayer.removePlayerListener(mPlayerListener);
            final Player toUnbind = mPlayer;
            mPlayer = null;
            mDispatcher.obtain(StateUnbindPlayer.class, this).init(toUnbind).dispatch();
        }
    }

    private void bindVideoView(VideoView newVideoView) {
        if (mVideoView == null && newVideoView != null) {
            L.d(this, "bindVideoView", newVideoView);
            mVideoView = newVideoView;
            mVideoView.setSurfaceListener(mSurfaceListener);
            mVideoView.bindController(this);
            mDispatcher.obtain(StateBindVideoView.class, this).init(newVideoView).dispatch();
        }
    }

    private void unbindVideoView() {
        if (mVideoView != null) {
            L.d(this, "unbindVideoView", mVideoView);
            mVideoView.setSurfaceListener(null);
            VideoView toUnbind = mVideoView;
            mVideoView = null;
            toUnbind.unbindController(this);
            mDispatcher.obtain(StateUnbindVideoView.class, this).init(toUnbind).dispatch();
        }
    }

    /**
     * Starts playback session.
     * <p> Make sure {@link #bind(VideoView)} and {@link VideoView#bindDataSource(MediaSource)} is
     * called before calling {@code startPlayback()}. {@code startPlayback()} will take no effect
     * if {@link VideoView} and {@link MediaSource} haven't been bound.
     *
     * <p>If there is no {@link Player} instance is bound. A {@link Player} instance will be bound
     * automatically after calling.
     *
     * <p> If there is a {@link Player} instance has been bound already.
     * <ul>
     *   <li>If {@link Player#getDataSource()} and {@link VideoView#getDataSource()} is not
     *       the same media.The {@link Player} instance will be recycled by {@link PlayerPool}</li>
     *   <li>If {@link Player#getDataSource()} and {@link VideoView#getDataSource()} is same media.
     *      {@link Player} instance will be reused.
     *   </li>
     * </ul>
     *
     * <p> Anyway, a {@link Player} instance will be bound to {@link PlaybackController}
     * automatically after calling this method.
     *
     * <p> If {@link VideoView}'s Surface is not ready when calling this method
     * playback will be automatically started when Surface is ready.
     *
     * <p> Calling {@link #stopPlayback()} to stop playback anytime you want.
     *
     * <p>{@link ActionStartPlayback} will be emitted by {@link EventListener} added by
     * {@link #addPlaybackListener(EventListener)} after calling.
     */
    @MainThread
    public final void startPlayback() {
        Asserts.checkMainThread();

        if (mVideoView == null) {
            L.e(this, "startPlayback", "VideoView not bind!");
            return;
        }

        final MediaSource viewSource = mVideoView.getDataSource();
        if (viewSource == null) {
            L.e(this, "startPlayback", "Data source not bind to VideoView yet!");
            return;
        }

        L.d(this, "startPlayback");

        mDispatcher.obtain(ActionStartPlayback.class, this).dispatch();

        if (mPlayer != null) {
            if (mPlayer.isReleased() || mPlayer.isError()) {
                unbindPlayer(true);
            } else if (!mPlayer.isIDLE()
                    && !mediaEquals(mPlayer.getDataSource(), viewSource)) {
                unbindPlayer(true);
            }
        }

        if (mPlayer == null) {
            Player newPlayer = mPlayerPool.acquire(viewSource);
            bindPlayer(newPlayer);
            // new player，media source is not bind yet
        } /* else {
            // reuse player, same media source
        } */
        Asserts.checkNotNull(mPlayer);

        if (isReady()) {
            startPlaybackOnReady();
        } else {
            L.d(this, "startPlayback", "but resource not ready",
                    mPlayer, // player not bind
                    mVideoView, // view not bind
                    mVideoView == null ? null : mVideoView.getSurface(), // surface not ready
                    viewSource // data source not bind
            );
            mStartOnReadyCommand = () -> {
                mStartOnReadyCommand = null;
                startPlaybackOnReady();
            };
        }
    }

    private boolean isReady() {
        return mPlayer != null
                && mVideoView != null
                && mVideoView.getSurface() != null
                && mVideoView.getSurface().isValid()
                && mVideoView.getDataSource() != null;
    }

    private void startPlaybackOnReady() {
        if (mPlayer == null) {
            L.e(this, "startPlayback OnReady", "player == null");
            return;
        }
        if (mVideoView == null) {
            L.e(this, "startPlayback OnReady", "videoView == null");
            return;
        }
        final MediaSource mediaSource = mVideoView.getDataSource();
        if (mediaSource == null) {
            L.e(this, "startPlayback OnReady", "mediaSource == null");
            return;
        }
        final Surface surface = mVideoView.getSurface();
        if (surface == null) {
            L.e(this, "startPlayback OnReady", "surface == null");
        }

        L.d(this, "startPlayback OnReady", mPlayer, mVideoView, surface, mediaSource);

        mVideoView.setReuseSurface(true);

        // 1. update surface
        if (surface != mPlayer.getSurface()) {
            mPlayer.setSurface(surface);
        }

        // 2. start play
        if (mPlayer.isIDLE()) {
            mPlayer.setStartWhenPrepared(true);
            mPlayer.prepare(mediaSource);
        } else if (mPlayer.isCompleted()) {
            mPlayer.start();
        } else if (mPlayer.isPaused()) {
            mPlayer.start();
        } else if (mPlayer.isPrepared()) {
            mPlayer.start();
        } else if (mPlayer.isPreparing()) {
            if (!mPlayer.isStartWhenPrepared()) {
                mPlayer.setStartWhenPrepared(true);
            } else {
                L.d(this, "startPlayback OnReady",
                        "player is preparing, will be started automatically when prepared");
            }
        } else if (mPlayer.isPlaying()) {
            L.d(this, "startPlayback OnReady", "already started! nop~");
        } else {
            throw new IllegalStateException(mPlayer + " state is illegal. " + mPlayer.dump());
        }
    }

    /**
     * Stops playback session.
     * <p> {@link Player} instance will be unbind and released if there is one.
     *
     * <p>{@link ActionStopPlayback} will be emitted by {@link EventListener} added by
     * {@link #addPlaybackListener(EventListener)} after calling.
     */
    @MainThread
    public void stopPlayback() {
        Asserts.checkMainThread();

        if (mVideoView != null) {
            mVideoView.setReuseSurface(false);
        }

        if (mStartOnReadyCommand != null // startPlayback but surface not ready
                || mPlayer != null
        ) {
            L.d(this, "stopPlayback");

            mDispatcher.obtain(ActionStopPlayback.class, this).dispatch();

            mStartOnReadyCommand = null;
            unbindPlayer(true);
            L.d(this, "stopPlayback", "end");
        }
    }

    static final class SurfaceListener implements DisplayView.SurfaceListener {

        final WeakReference<PlaybackController> controllerRef;

        SurfaceListener(PlaybackController controller) {
            controllerRef = new WeakReference<>(controller);
        }

        @Override
        public void onSurfaceAvailable(Surface surface, int width, int height) {
            final PlaybackController controller = controllerRef.get();
            if (controller == null) return;

            L.d(controller, "onSurfaceAvailable", surface, width, height);

            if (controller.mStartOnReadyCommand != null) {
                if (controller.isReady()) {
                    controller.mStartOnReadyCommand.run();
                }
            } else {
                final Player player = controller.player();
                if (player != null && player.getSurface() != surface) {
                    player.setSurface(surface);
                }
            }
        }

        @Override
        public void onSurfaceSizeChanged(Surface surface, int width, int height) {
            final PlaybackController controller = controllerRef.get();
            if (controller == null) return;

            L.d(controller, "onSurfaceSizeChanged", surface, width, height);
        }

        @Override
        public void onSurfaceUpdated(Surface surface) {
            final PlaybackController controller = controllerRef.get();
            if (controller == null) return;

            //L.v(controller, "onSurfaceUpdated", surface);
        }

        @Override
        public void onSurfaceDestroy(Surface surface) {
            final PlaybackController controller = controllerRef.get();
            if (controller == null) return;

            L.d(controller, "onSurfaceDestroy", surface);

            final VideoView videoView = controller.videoView();
            if (videoView == null) return;

            final int type = videoView.getDisplayViewType();

            switch (type) {
                case DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW:
                    if (videoView.isReuseSurface()) {
                        return;
                    }
                case DisplayView.DISPLAY_VIEW_TYPE_SURFACE_VIEW:
                    final Player player = controller.player();
                    if (player != null && player.getSurface() == surface) {
                        player.setSurface(null);
                    }
                    return;
                default:
                    throw new IllegalArgumentException("unsupported displayViewType: " + type);
            }
        }
    }

    static final class PlayerListener implements EventListener {

        private final WeakReference<PlaybackController> controllerRef;

        public PlayerListener(PlaybackController controller) {
            controllerRef = new WeakReference<>(controller);
        }

        @Override
        public void onEvent(Event event) {
            PlaybackController controller = controllerRef.get();
            Dispatcher dispatcher = controller == null ? null : controller.mDispatcher;
            if (dispatcher != null) {
                dispatcher.dispatchEvent(event);
            }
        }
    }
}
