package cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.record_messages;

import cn.co.willow.android.ultimate.gpuimage.core_config.RecorderMessageState;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoFilterManager;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoRecordManagerCallback;

/**
 * This is generic interface for RecordMessage
 */
public abstract class BaseRecorderMessage implements BaseMessage {

    private final VideoFilterManager mFilteManager;
    private final VideoRecordManagerCallback mCallback;

    public BaseRecorderMessage(VideoFilterManager mFilteManager, VideoRecordManagerCallback callback) {
        this.mFilteManager = mFilteManager;
        this.mCallback = callback;
    }

    protected final RecorderMessageState getCurrentState() {
        return mCallback.getCurrentPlayerState();
    }

    @Override
    public final void polledFromQueue() {
        mCallback.setVideoRecorderState(mFilteManager, stateBefore());
    }

    @Override
    public final void messageFinished() {
        mCallback.setVideoRecorderState(mFilteManager, stateAfter());
    }

    public final void runMessage() {
        performAction(mFilteManager);
    }

    protected abstract void performAction(VideoFilterManager currentRecorder);
    protected abstract RecorderMessageState stateBefore();
    protected abstract RecorderMessageState stateAfter();

}
