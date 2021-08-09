package com.bytedance.volc.voddemo.videoview.layers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Message;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.utils.TimeUtils;
import com.bytedance.volc.voddemo.utils.UIUtils;
import com.bytedance.volc.voddemo.utils.WeakHandler;
import com.bytedance.volc.voddemo.videoview.Transformer;
import com.bytedance.volc.voddemo.videoview.VideoController;
import com.bytedance.volc.voddemo.videoview.layer.BaseVideoLayer;
import com.bytedance.volc.voddemo.videoview.layer.CommonLayerCommand;
import com.bytedance.volc.voddemo.videoview.layer.CommonLayerEvent;
import com.bytedance.volc.voddemo.videoview.layer.ILayer;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerCommand;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerEvent;
import com.bytedance.volc.voddemo.widget.ByteSeekBar;
import com.ss.ttvideoengine.Resolution;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;
import java.util.ArrayList;
import java.util.List;

import static com.bytedance.volc.voddemo.videoview.layer.IVideoLayerEvent.VIDEO_LAYER_EVENT_STOP_TRACK;

public class ToolbarLayer extends BaseVideoLayer
        implements WeakHandler.IHandler, ByteSeekBar.OnByteSeekBarChangeListener {

    private static final int TIME_ANIM_ALPHA = 160;
    private static final int MSG_DISMISS_TOOLBAR = 1001;
    private static final int DISMISS_TOOLBAR_DELAY = 3 * 1000;

    private ByteSeekBar mSeekBar;
    private TextView mCurrentTv, mDurationTv;
    private ImageView mPlayIv;
    private Animator mShowAnimator, mDismissAnimator;

    private float mSeekToPercent;
    private final WeakHandler handler = new WeakHandler(this);
    private boolean mToolbarCanShow;

    private final List<Integer> mSupportEvents = new ArrayList<Integer>() {
        {
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_RELEASE);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_PAUSE);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_PLAYING);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_BUFFER_UPDATE);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_PROGRESS_CHANGE);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_COMPLETE);

            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_CALL_PLAY);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_FULLSCREEN_CHANGE);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_VIEW_CLICK);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_SHOW_SPEED);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_SHOW_RESOLUTION);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_SHOW_DOWNLOAD);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_PREPARE_DETAIL);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_ENTER_DETAIL);
        }
    };

    @SuppressLint("InflateParams")
    @Override
    protected View getLayerView(final Context context, @NonNull final LayoutInflater inflater) {
        return inflater.inflate(R.layout.layer_toolbar, null);
    }

    @Override
    protected void setupViews() {
        mCurrentTv = mLayerView.findViewById(R.id.current_tv);
        mDurationTv = mLayerView.findViewById(R.id.duration_tv);
        mPlayIv = mLayerView.findViewById(R.id.play_btn);
        mSeekBar = mLayerView.findViewById(R.id.seek_bar);
        ImageView backIv = mLayerView.findViewById(R.id.back_iv);
        ImageView fullScreenIv = mLayerView.findViewById(R.id.fullscreen_iv);

        fullScreenIv.setOnClickListener(this::onClick);
        backIv.setOnClickListener(this::onClick);
        mLayerView.setOnClickListener(this::onClick);
        mPlayIv.setOnClickListener(this::onClick);

        mSeekBar.setOnByteSeekBarChangeListener(this);
        mLayerView.setVisibility(View.GONE);
    }

    @Override
    public int getZIndex() {
        return ILayer.TOOLBAR_Z_INDEX;
    }

    @NonNull
    @Override
    public List<Integer> getSupportEvents() {
        return mSupportEvents;
    }

    @Override
    public void refresh() {
        mCurrentTv.setText(TimeUtils.milliSecondsToTimer(0));
        long duration = mHost.getDuration();
        mDurationTv.setText(TimeUtils.milliSecondsToTimer(duration));
    }

    @SuppressLint("NonConstantResourceId")
    private void onClick(View v) {
        if (mHost == null) {
            return;
        }
        Transformer transformer = mHost.getTransformer();
        switch (v.getId()) {
            case R.id.play_btn:
                togglePlayOrPause();
                break;
            case R.id.fullscreen_iv:
                if (transformer == null) {
                    return;
                }

                if (transformer.isFullScreen()) {
                    mHost.execCommand(new CommonLayerCommand(
                            IVideoLayerCommand.VIDEO_HOST_CMD_EXIT_FULLSCREEN));
                } else {
                    mHost.execCommand(new CommonLayerCommand(
                            IVideoLayerCommand.VIDEO_HOST_CMD_ENTER_FULLSCREEN));
                }
                break;
            case R.id.back_iv:
                if (transformer == null) {
                    return;
                }

                if (transformer.isFullScreen()) {
                    mHost.execCommand(new CommonLayerCommand(
                            IVideoLayerCommand.VIDEO_HOST_CMD_EXIT_FULLSCREEN));
                } else {
                    mHost.execCommand(
                            new CommonLayerCommand(IVideoLayerCommand.VIDEO_HOST_CMD_EXIT_DETAIL));
                }
                break;
            default:
                showToolbar(false);
                break;
        }
    }

    @Override
    public boolean handleVideoEvent(@NonNull IVideoLayerEvent event) {
        switch (event.getType()) {
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_CALL_PLAY:
                mToolbarCanShow = true;
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_PAUSE:
                cancelDismissToolbar();
                updatePlayBtn();
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_PLAYING:
                updatePlayBtn();
                autoDismissToolbar();
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_PROGRESS_CHANGE:
                //noinspection unchecked
                Pair<Integer, Integer> pair = (Pair<Integer, Integer>) event.getParam();
                updatePlayProcess(pair.first, pair.second);
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_RELEASE:
                mToolbarCanShow = false;
                showToolbar(false);
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_COMPLETE:
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_SHOW_SPEED:
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_SHOW_RESOLUTION:
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_SHOW_DOWNLOAD:
                showToolbar(false);
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_VIEW_CLICK:
                showToolbar(mToolbarCanShow);
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_BUFFER_UPDATE:
                int percent = event.getParam(Integer.class);
                updateBufferProcess(percent);
                break;
            default:
                break;
        }
        return true;
    }

    private int getSeekPos(float percent) {
        int duration = mHost.getVideoController().getDuration();
        int seekPos = 0;
        if (duration > 0) {
            seekPos = (int) (percent * duration * 1.0f / 100.0f);
        }
        return seekPos;
    }

    @Override
    public void onProgressChanged(ByteSeekBar seekBar, float progress, boolean fromUser,
            float xVelocity) {
        if (fromUser) {
            mSeekToPercent = progress;
            int current = getSeekPos(progress);
            if (mCurrentTv != null) {
                mCurrentTv.setText(TimeUtils.milliSecondsToTimer(current));
            }
        }
    }

    @Override
    public void onStartTrackingTouch(ByteSeekBar seekBar) {
        cancelDismissToolbar();
    }

    @Override
    public void onStopTrackingTouch(ByteSeekBar seekBar) {
        autoDismissToolbar();
        int seekTo = getSeekPos(mSeekToPercent);
        mHost.execCommand(new CommonLayerCommand(IVideoLayerCommand.VIDEO_HOST_CMD_SEEK, seekTo));
    }

    private void showToolbar(boolean show) {
        if (show) {
            updatePlayBtn();
            if (mLayerView.getVisibility() != View.VISIBLE) {
                UIUtils.setViewVisibility(mLayerView, View.VISIBLE);
                getShowAnimator().start();
            }
        } else {
            if (mLayerView.getVisibility() == View.VISIBLE) {
                getDismissAnimator().start();
            }
        }
        if (show) {
            if (mHost.getVideoController().isPlaying()) {
                autoDismissToolbar();
            } else {
                cancelDismissToolbar();
            }
        }
    }

    private void updateBufferProcess(int percent) {
        mSeekBar.setSecondaryProgress(percent);
    }

    private void updatePlayProcess(long current, long duration) {
        if (mDurationTv != null) {
            mDurationTv.setText(TimeUtils.milliSecondsToTimer(duration));
        }
        if (mCurrentTv != null) {
            mCurrentTv.setText(TimeUtils.milliSecondsToTimer(current));
        }
        if (mSeekBar != null) {
            mSeekBar.setProgress(TimeUtils.timeToPercent(current, duration));
        }
    }

    private void cancelDismissToolbar() {
        if (handler != null) {
            handler.removeMessages(MSG_DISMISS_TOOLBAR);
        }
    }

    private void autoDismissToolbar() {
        if (handler != null) {
            handler.removeMessages(MSG_DISMISS_TOOLBAR);
            Message msg = handler.obtainMessage(MSG_DISMISS_TOOLBAR);
            handler.sendMessageDelayed(msg, DISMISS_TOOLBAR_DELAY);
        }
    }

    private void updatePlayBtn() {
        boolean isVideoPlaying = mHost.getVideoController().isPlaying();
        mPlayIv.setImageResource(isVideoPlaying ? R.drawable.btn_pause : R.drawable.btn_play);
    }

    private void togglePlayOrPause() {
        final VideoController videoController = mHost.getVideoController();
        if (videoController != null) {
            if (videoController.isPaused()) {
                mHost.execCommand(new CommonLayerCommand(IVideoLayerCommand.VIDEO_HOST_CMD_PLAY));
            } else {
                mHost.execCommand(new CommonLayerCommand(IVideoLayerCommand.VIDEO_HOST_CMD_PAUSE));
            }
        }
    }

    private Animator getShowAnimator() {
        if (mShowAnimator == null) {
            mShowAnimator = ObjectAnimator.ofFloat(mLayerView, "alpha", 0.0f, 1.0f)
                    .setDuration(TIME_ANIM_ALPHA);
        }
        return mShowAnimator;
    }

    private Animator getDismissAnimator() {
        if (mDismissAnimator == null) {
            mDismissAnimator = ObjectAnimator.ofFloat(mLayerView, "alpha", 1.0f, 0.0f)
                    .setDuration(TIME_ANIM_ALPHA);

            mDismissAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    UIUtils.setViewVisibility(mLayerView, View.GONE);
                }
            });
        }
        return mDismissAnimator;
    }

    @Override
    public void handleMsg(Message msg) {
        if (msg.what == MSG_DISMISS_TOOLBAR) {
            showToolbar(false);
        }
    }
}
