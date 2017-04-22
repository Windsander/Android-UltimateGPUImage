package cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.record_messages;

import cn.co.willow.android.ultimate.gpuimage.core_config.RecorderMessageState;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoFilterManager;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoRecordManagerCallback;

@Deprecated
public class ClearRecordInstance extends BaseRecorderMessage {

    public ClearRecordInstance(VideoFilterManager mFilteManager, VideoRecordManagerCallback callback) {
        super(mFilteManager, callback);
    }

    @Override
    protected void performAction(VideoFilterManager currentRecorder) {
        //currentRecorder.clearRecorderInstance();
    }

    @Override
    protected RecorderMessageState stateBefore() {
        return RecorderMessageState.CLEAR_ALL;
    }

    @Override
    protected RecorderMessageState stateAfter() {
        return RecorderMessageState.CLEAR_FINISH;
    }
}
