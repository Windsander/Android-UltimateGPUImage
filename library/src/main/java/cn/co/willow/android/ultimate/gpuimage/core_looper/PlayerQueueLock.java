package cn.co.willow.android.ultimate.gpuimage.core_looper;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;

public class PlayerQueueLock {

    private static final String TAG = PlayerQueueLock.class.getSimpleName();
    private static final boolean SHOW_LOGS = false;
    private final ReentrantLock mQueueLock = new ReentrantLock();
    private final Condition mProcessQueueCondition = mQueueLock.newCondition();

    public void lock(String owner) {
        if (SHOW_LOGS) LogUtil.v(TAG, ">> lockVideoThread, owner [" + owner + "]");
        mQueueLock.lock();
        if (SHOW_LOGS) LogUtil.v(TAG, "<< lockVideoThread, owner [" + owner + "]");
    }

    public void unlock(String owner) {
        if (SHOW_LOGS) LogUtil.v(TAG, ">> unlock, owner [" + owner + "]");
        mQueueLock.unlock();
        if (SHOW_LOGS) LogUtil.v(TAG, "<< unlock, owner [" + owner + "]");
    }

    public boolean isLocked(String owner) {
        boolean isLocked = mQueueLock.isLocked();
        if (SHOW_LOGS) LogUtil.v(TAG, "isLocked, owner [" + owner + "]");
        return isLocked;
    }

    public void wait(String owner) throws InterruptedException {
        if (SHOW_LOGS) LogUtil.v(TAG, ">> wait, owner [" + owner + "]");
        mProcessQueueCondition.await();
        if (SHOW_LOGS) LogUtil.v(TAG, "<< wait, owner [" + owner + "]");
    }

    public void notify(String owner) {
        if (SHOW_LOGS) LogUtil.v(TAG, ">> notify, owner [" + owner + "]");
        mProcessQueueCondition.signal();
        if (SHOW_LOGS) LogUtil.v(TAG, "<< notify, owner [" + owner + "]");
    }
}
