package cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.record_messages;

import android.media.CamcorderProfile;

import java.io.File;

import cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig;
import cn.co.willow.android.ultimate.gpuimage.core_config.RecorderMessageState;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoFilterManager;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoRecordManager;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoRecordManagerCallback;

public class CreateNewRecordInstance extends BaseRecorderMessage {

    private final File                           mOutputRecFile;
    private final OutputConfig.VideoOutputConfig mVideoConfig;
    private final OutputConfig.AudioOutputConfig mAudioConfig;
    public CreateNewRecordInstance(
            VideoFilterManager currentRecorder,
            File mOutputRecFile,
            OutputConfig.VideoOutputConfig mVideoConfig,
            OutputConfig.AudioOutputConfig mAudioConfig,
            VideoRecordManagerCallback callback) {
        super(currentRecorder, callback);
        this.mOutputRecFile = mOutputRecFile;
        this.mVideoConfig = mVideoConfig;
        this.mAudioConfig = mAudioConfig;
    }

    @Override
    protected void performAction(VideoFilterManager currentRecorder) {
        currentRecorder.createNewRecorderInstance(mOutputRecFile, mVideoConfig, mAudioConfig);
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
