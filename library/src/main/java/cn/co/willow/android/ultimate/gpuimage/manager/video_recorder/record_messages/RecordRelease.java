package cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.record_messages;

import cn.co.willow.android.ultimate.gpuimage.core_config.RecorderMessageState;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoFilterManager;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoRecordManagerCallback;

public class RecordRelease extends BaseRecorderMessage {

    public RecordRelease(VideoFilterManager currentRecorder, VideoRecordManagerCallback callback) {
        super(currentRecorder, callback);
    }

    @Override
    protected void performAction(VideoFilterManager currentRecorder) {
        currentRecorder.release();
    }

    @Override
    protected RecorderMessageState stateBefore() {
        return RecorderMessageState.RELEASE;
    }

    @Override
    protected RecorderMessageState stateAfter() {
        return RecorderMessageState.IDLE;
    }

}
