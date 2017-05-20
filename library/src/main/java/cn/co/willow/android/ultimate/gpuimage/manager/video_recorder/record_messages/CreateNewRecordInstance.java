package cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.record_messages;

import java.io.File;

import cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig;
import cn.co.willow.android.ultimate.gpuimage.core_config.RecorderMessageState;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoFilterManager;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoRecordManagerCallback;

public class CreateNewRecordInstance extends BaseRecorderMessage {

    private final File                           mOutputRecFile;
    private final OutputConfig.VideoOutputConfig mVideoConfig;
    private final OutputConfig.AudioOutputConfig mAudioConfig;

    public CreateNewRecordInstance(VideoFilterManager currentRecorder,
                                   VideoRecordManagerCallback callback,
                                   File mOutputRecFile,
                                   OutputConfig.VideoOutputConfig videoConfig,
                                   OutputConfig.AudioOutputConfig audioConfig) {
        super(currentRecorder, callback);
        this.mOutputRecFile = mOutputRecFile;
        this.mVideoConfig = videoConfig;
        this.mAudioConfig = audioConfig;
    }

    @Override
    protected void performAction(VideoFilterManager currentRecorder) {
        currentRecorder.create(mOutputRecFile, mVideoConfig, mAudioConfig);
    }

    @Override
    protected RecorderMessageState stateBefore() {
        return RecorderMessageState.INITIALIZING;
    }

    @Override
    protected RecorderMessageState stateAfter() {
        return RecorderMessageState.INITIALIZED;
    }
}
