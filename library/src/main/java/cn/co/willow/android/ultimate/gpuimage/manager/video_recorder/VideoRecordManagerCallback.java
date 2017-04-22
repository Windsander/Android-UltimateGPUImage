package cn.co.willow.android.ultimate.gpuimage.manager.video_recorder;

import cn.co.willow.android.ultimate.gpuimage.core_config.RecorderMessageState;
import cn.co.willow.android.ultimate.gpuimage.core_looper.meta.MetaData;
import cn.co.willow.android.ultimate.gpuimage.ui.FilterRecoderView;

public interface VideoRecordManagerCallback {

    void setCurrentItem(MetaData currentItemMetaData, FilterRecoderView currentRecorder);

    void setVideoRecorderState(VideoFilterManager mFilteManager, RecorderMessageState recorderMessageState);

    RecorderMessageState getCurrentPlayerState();
}
