package cn.co.willow.android.ultimate.gpuimage.manager.record_messages;

import cn.co.willow.android.ultimate.gpuimage.core_config.RecorderMessageState;
import cn.co.willow.android.ultimate.gpuimage.manager.VideoFilterManager;
import cn.co.willow.android.ultimate.gpuimage.manager.VideoRecordManagerCallback;

public class RecordStop extends BaseRecorderMessage {

    public RecordStop(VideoFilterManager currentRecorder, VideoRecordManagerCallback callback) {
        super(currentRecorder, callback);
    }

    @Override
    protected void performAction(VideoFilterManager currentRecorder) {
        currentRecorder.stop();
    }

    @Override
    protected RecorderMessageState stateBefore() {
        return RecorderMessageState.STOPING;
    }

    @Override
    protected RecorderMessageState stateAfter() {
        return RecorderMessageState.IDLE;
    }

}
