package com.bytedance.volc.voddemo.videoview.layers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.utils.TimeUtils;
import com.bytedance.volc.voddemo.utils.UIUtils;
import com.bytedance.volc.voddemo.videoview.layer.BaseVideoLayer;
import com.bytedance.volc.voddemo.videoview.layer.CommonLayerCommand;
import com.bytedance.volc.voddemo.videoview.layer.ILayer;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerCommand;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerEvent;
import java.util.ArrayList;
import java.util.List;

public class BeforeLayer extends BaseVideoLayer {

    private final boolean mShowTitle;
    private TextView mTitleView;
    private TextView mDurationTv;
    private ImageView mCoverView;

    private Animator mDismissAnimator;

    private final List<Integer> mSupportEvents = new ArrayList<Integer>() {
        {
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_PRE_RELEASE);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_CALL_PLAY);
        }
    };

    public BeforeLayer(boolean showTitle) {
        this.mShowTitle = showTitle;
    }

    @Override
    public int getZIndex() {
        return ILayer.FORE_PLAY_Z_INDEX;
    }

    @Override
    protected void setupViews() {
        if (mShowTitle) {
            mTitleView = mLayerView.findViewById(R.id.title_tv);
        }
        mDurationTv = mLayerView.findViewById(R.id.duration_tv);
        mCoverView = mLayerView.findViewById(R.id.cover_view);
        mLayerView.setOnClickListener(v -> mHost.execCommand(
                new CommonLayerCommand(IVideoLayerCommand.VIDEO_HOST_CMD_PLAY)));
    }

    @NonNull
    @Override
    public List<Integer> getSupportEvents() {
        return mSupportEvents;
    }

    @Override
    public boolean handleVideoEvent(@NonNull IVideoLayerEvent event) {
        switch (event.getType()) {
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_PRE_RELEASE:
                showView();
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_CALL_PLAY:
                dismissView();
                break;
            default:
                break;
        }
        return false;
    }

    @SuppressLint("InflateParams")
    @Override
    protected View getLayerView(final Context context, @NonNull final LayoutInflater inflater) {
        return inflater.inflate(R.layout.layer_before_play, null);
    }

    @Override
    public void refresh() {
        final String url = mHost.getCover();
        Glide.with(mCoverView).load(url).into(mCoverView);
        if (mShowTitle) {
            String title = mHost.getTitle();
            mTitleView.setText(title);
        }
        mDurationTv.setText(TimeUtils.milliSecondsToTimer(mHost.getDuration()));
    }

    private void showView() {
        getDismissAnimator().cancel();
        UIUtils.setViewVisibility(mLayerView, View.VISIBLE);
        mLayerView.setAlpha(1);
    }

    private void dismissView() {
        getDismissAnimator().start();
    }

    private Animator getDismissAnimator() {
        if (mDismissAnimator == null) {
            mDismissAnimator = ObjectAnimator.ofFloat(mLayerView, "alpha", 1.0f, 0.0f)
                    .setDuration(30);
            mDismissAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    UIUtils.setViewVisibility(mLayerView, View.GONE);
                }
            });
        }
        return mDismissAnimator;
    }
}
