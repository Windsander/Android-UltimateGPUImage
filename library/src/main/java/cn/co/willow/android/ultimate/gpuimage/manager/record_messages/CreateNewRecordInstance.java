package cn.co.willow.android.ultimate.gpuimage.manager.record_messages;

import android.media.CamcorderProfile;

import java.io.File;

import cn.co.willow.android.ultimate.gpuimage.core_config.RecorderMessageState;
import cn.co.willow.android.ultimate.gpuimage.manager.VideoFilterManager;
import cn.co.willow.android.ultimate.gpuimage.manager.VideoRecordManagerCallback;

public class CreateNewRecordInstance extends BaseRecorderMessage {

    private final File mOutputRecFile;
    private final CamcorderProfile mProfile;
    public CreateNewRecordInstance(VideoFilterManager currentRecorder,
                                   File mOutputRecFile, CamcorderProfile mProfile,
                                   VideoRecordManagerCallback callback) {
        super(currentRecorder, callback);
        this.mOutputRecFile = mOutputRecFile;
        this.mProfile = mProfile;
    }

    @Override
    protected void performAction(VideoFilterManager currentRecorder) {
        currentRecorder.createNewRecorderInstance(mOutputRecFile, mProfile);
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
