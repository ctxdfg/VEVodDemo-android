package com.bytedance.volc.voddemo.longvideo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.data.VideoViewModel;
import com.bytedance.volc.voddemo.videoview.layer.ILayerHost;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerCommand;
import com.bytedance.volc.voddemo.videoview.layer.LayerRoot;

public class LongVideoFragment extends Fragment
        implements LayerRoot.VideoViewCommandListener {
    public static final String TAG = "LongVideoFragment";

    private static final int ITEMS_LIMIT = 100;

    private VideoViewModel mVideoViewModel;
    private LongVideoAdapter mAdapter;

    public static LongVideoFragment newInstance() {
        return new LongVideoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVideoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_long_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView rvVideo = view.findViewById(R.id.rv_video);
        int horizontalDividerSize = getResources().getDimensionPixelSize(R.dimen.qb_px_10);
        int verticalDividerSize = getResources().getDimensionPixelSize(R.dimen.qb_px_6);
        mAdapter = new LongVideoAdapter(this);
        rvVideo.setAdapter(mAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(final int position) {
                if (position == 0) {
                    return 2;
                }
                return 1;
            }
        });
        rvVideo.setLayoutManager(gridLayoutManager);

        LongVideoGridDecoration decor = new LongVideoGridDecoration(horizontalDividerSize,
                verticalDividerSize, 2);
        rvVideo.addItemDecoration(decor);
        mVideoViewModel.getVideoListLiveData(ITEMS_LIMIT).observe(getViewLifecycleOwner(),
                videoItems -> {
                    if (videoItems != null && videoItems.size() > 0) {
                        mAdapter.replaceAll(videoItems);
                    }
                });
    }

    @Override
    public boolean onVideoViewCommand(ILayerHost layerHost, IVideoLayerCommand action) {
        if (action.getCommand() == IVideoLayerCommand.VIDEO_HOST_CMD_PLAY) {
            DetailActivity.intentInto(getActivity(), layerHost.getVideoItem());
            return true;
        }
        return false;
    }
}