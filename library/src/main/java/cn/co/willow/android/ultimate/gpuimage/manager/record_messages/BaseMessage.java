package cn.co.willow.android.ultimate.gpuimage.manager.record_messages;

/**
 * This generic interface for messages
 */
public interface BaseMessage {
    void runMessage();
    void polledFromQueue();
    void messageFinished();
}
