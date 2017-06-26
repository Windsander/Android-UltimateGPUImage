package cn.co.willow.android.ultimate.gpuimage.core_looper;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.record_messages.BaseMessage;
import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;

/**
 * This class is designed to process a message queue.
 * It calls very specific methods of {@link BaseMessage} in very specific times.
 * <p>
 * 1. When message is polled from queue it calls {@link BaseMessage#polledFromQueue()}
 * 2. When message should be run it calls {@link BaseMessage#runMessage()}
 * 3. When message finished running it calls {@link BaseMessage#messageFinished()}
 */
public class MessagesHandlerThread {

    private static final String TAG = MessagesHandlerThread.class.getSimpleName();

    private final Queue<BaseMessage> mPlayerMessagesQueue = new ConcurrentLinkedQueue<>();
    private final PlayerQueueLock mQueueLock = new PlayerQueueLock();
    private final Executor mQueueProcessingThread = Executors.newSingleThreadExecutor();

    private AtomicBoolean mTerminated = new AtomicBoolean(false); // TODO: use it
    private BaseMessage mLastMessage;

    public MessagesHandlerThread() {
        mQueueProcessingThread.execute(new Runnable() {
            @Override
            public void run() {
                do {
                    mQueueLock.lock(TAG);
                    if (mPlayerMessagesQueue.isEmpty()) {
                        try {
                            mQueueLock.wait(TAG);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            throw new RuntimeException("InterruptedException");
                        }
                    }
                    mLastMessage = mPlayerMessagesQueue.poll();
                    mLastMessage.polledFromQueue();
                    mQueueLock.unlock(TAG);

                    mLastMessage.runMessage();

                    mQueueLock.lock(TAG);
                    mLastMessage.messageFinished();
                    mQueueLock.unlock(TAG);

                } while (!mTerminated.get());

            }
        });
    }

    /**
     * Use it if you need to NotifyDataChanged a single message
     */
    public void addMessage(BaseMessage message) {
        mQueueLock.lock(TAG);
        mPlayerMessagesQueue.add(message);
        mQueueLock.notify(TAG);
        mQueueLock.unlock(TAG);
    }

    public void addMessages(List<? extends BaseMessage> messages) {
        mQueueLock.lock(TAG);
        mPlayerMessagesQueue.addAll(messages);
        mQueueLock.notify(TAG);
        mQueueLock.unlock(TAG);
    }

    public void pauseQueueProcessing(String outer) {
        mQueueLock.lock(outer);
    }

    public void resumeQueueProcessing(String outer) {
        mQueueLock.unlock(outer);
    }

    public void clearAllPendingMessages(String outer) {
        if (mQueueLock.isLocked(outer)) {
            mPlayerMessagesQueue.clear();
        } else {
            throw new RuntimeException("cannot perform action, you are not holding a lockVideoThread");
        }
    }

    public void terminate() {
        mTerminated.set(true);
    }
}
