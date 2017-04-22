package cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.record_messages;

import cn.co.willow.android.ultimate.gpuimage.core_config.RecorderMessageState;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoFilterManager;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoRecordManagerCallback;

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
