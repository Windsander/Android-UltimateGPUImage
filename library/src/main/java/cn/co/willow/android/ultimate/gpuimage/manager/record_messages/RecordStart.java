package cn.co.willow.android.ultimate.gpuimage.manager.record_messages;

import cn.co.willow.android.ultimate.gpuimage.core_config.RecorderMessageState;
import cn.co.willow.android.ultimate.gpuimage.manager.VideoFilterManager;
import cn.co.willow.android.ultimate.gpuimage.manager.VideoRecordManagerCallback;

public class RecordStart extends BaseRecorderMessage {

    public RecordStart(VideoFilterManager currentRecorder, VideoRecordManagerCallback callback) {
        super(currentRecorder, callback);
    }

    @Override
    protected void performAction(VideoFilterManager currentRecorder) {
        currentRecorder.start();
    }

    @Override
    protected RecorderMessageState stateBefore() {
        return RecorderMessageState.START_RECORD;
    }

    @Override
    protected RecorderMessageState stateAfter() {
        return RecorderMessageState.RECORDING;
    }

}
