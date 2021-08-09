package com.bytedance.volc.voddemo.videoview;

public interface Transformer {

    boolean isFullScreen();

    void exitFullScreen(VOLCVideoView videoView);

    void enterFullScreen(VOLCVideoView videoView);
}
