package cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.record_messages;

import cn.co.willow.android.ultimate.gpuimage.core_config.RecorderMessageState;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoFilterManager;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoRecordManagerCallback;

@Deprecated
public class RecordPrepare extends BaseRecorderMessage {

    private RecorderMessageState resultState;

    public RecordPrepare(VideoFilterManager currentRecorder, VideoRecordManagerCallback callback) {
        super(currentRecorder, callback);
    }

    @Override
    protected void performAction(VideoFilterManager currentRecorder) {
        /*currentRecorder.prepare();
        RecorderState resultOfPrepare = currentRecorder.getCurrentState();
        switch (resultOfPrepare) {
            case IDLE:
            case INITIALIZED:
            case RECORD:
            case STOP:
                throw new RuntimeException("unhandled state " + resultOfPrepare);
            case PREPARED:
                resultState = RecorderMessageState.PREPARED;
                break;
            case ERROR:
                resultState = RecorderMessageState.ERROR;
                break;
        }*/
    }

    @Override
    protected RecorderMessageState stateBefore() {
        return RecorderMessageState.PREPARING;
    }

    @Override
    protected RecorderMessageState stateAfter() {
        return resultState;
    }

}
