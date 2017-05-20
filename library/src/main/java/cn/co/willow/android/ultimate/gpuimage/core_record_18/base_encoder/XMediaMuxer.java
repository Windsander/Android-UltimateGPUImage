package cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.RequiresApi;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Vector;

import cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig;
import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;

/**
 * 音视频同步混合器
 * <p>
 * Created by willow.li on 2016/11/4.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class XMediaMuxer extends Thread {

    public static final int TRACK_VIDEO = 0;
    public static final int TRACK_AUDIO = 1;

    private final Object lock = new Object();

    private android.media.MediaMuxer mMediaMuxer;
    private Vector<MuxerData>        mMuxerDatas;
    private int videoTrackIndex = -1;
    private int audioTrackIndex = -1;

    private volatile boolean isVideoAdd;
    private volatile boolean isAudioAdd;
    private volatile boolean isMuxerExit       = false;
    private          boolean isMediaMuxerStart = false;
    private          boolean isMediaDataFinish = false;

    private AudioEncoder audioThread;
    private VideoEncoder videoThread;
    private MediaFormat  videoMediaFormat;
    private MediaFormat  audioMediaFormat;
    private File         mOutputFile;

    private boolean isCheckFrame = false;

    public XMediaMuxer(OutputConfig.VideoOutputConfig mVideoConfig,
                       OutputConfig.AudioOutputConfig mAudioConfig,
                       File outputFile) {
        try {
            mOutputFile = outputFile;
            mMuxerDatas = new Vector<>();
            mMediaMuxer = new android.media.MediaMuxer(mOutputFile.getAbsolutePath(), android.media.MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            audioThread = new AudioEncoder(mAudioConfig, new WeakReference<XMediaMuxer>(this));
            videoThread = new VideoEncoder(mVideoConfig, new WeakReference<XMediaMuxer>(this));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*混合器流程控制================================================================================*/
    public void startMuxer() {
        this.start();
    }

    public void stopMuxer() {
        this.exit();
    }

    private void exit() {
        audioThread.exit();
        videoThread.exit();
        isMuxerExit = true;
        synchronized (lock) {
            lock.notify();
        }
    }


    /*混合器关键数据添加============================================================================*/
    /** 返回当前设定的WindowSurface录制层 */
    public Surface getInputSurface() {
        return videoThread.getInputSurface();
    }

    public void notifyVideoData() {
        audioThread.notifyDataChanged();
        videoThread.notifyDataChanged();
    }

    public void addMuxerData(MuxerData data) {
        if (mMuxerDatas == null) return;
        mMuxerDatas.add(data);
        if (!isCheckFrame) {
            switchSyncFrameToFirst();
            isCheckFrame = true;
        }
        isMediaDataFinish = false;
        synchronized (lock) {
            lock.notify();
        }
    }

    public void switchSyncFrameToFirst() {
        for (int i = 0; i < mMuxerDatas.size(); i++) {
            MuxerData muxerData = mMuxerDatas.get(i);
            if (muxerData.trackIndex == TRACK_VIDEO) {
                if (muxerData.bufferInfo.flags == MediaCodec.BUFFER_FLAG_SYNC_FRAME) {
                    return;
                } else {
                    mMuxerDatas.remove(i);
                    i = i - 1;
                }
            }
        }
    }

    public synchronized void addMediaTrack(@TrackIndex int index, MediaFormat mediaFormat) {
        if (mMediaMuxer == null) return;
        if (index == TRACK_VIDEO) {
            if (videoMediaFormat == null) {
                videoMediaFormat = mediaFormat;
                videoTrackIndex = mMediaMuxer.addTrack(mediaFormat);
                isVideoAdd = true;
            }
        } else {
            if (audioMediaFormat == null) {
                audioMediaFormat = mediaFormat;
                audioTrackIndex = mMediaMuxer.addTrack(mediaFormat);
                isAudioAdd = true;
            }
        }
        requestStart();
    }


    /*混合流程======================================================================================*/
    @Override
    public void run() {
        initMuxer();
        while (!isMuxerExit || !isMediaDataFinish) {
            if (isMediaMuxerStart) {
                if (mMuxerDatas.isEmpty()) {
                    lockMuxer();
                } else {
                    MuxerData data  = mMuxerDatas.remove(0);
                    int       track = (data.trackIndex == TRACK_VIDEO) ? videoTrackIndex : audioTrackIndex;
                    mMediaMuxer.writeSampleData(track, data.byteBuf, data.bufferInfo);
                    if (mMuxerDatas != null && mMuxerDatas.isEmpty()) {
                        isMediaDataFinish = true;
                    }
                }
            } else {
                lockMuxer();
            }
        }
        stopMediaMuxer();
    }

    private void initMuxer() {
        videoThread.start();
        audioThread.start();
    }


    private void requestStart() {
        synchronized (lock) {
            LogUtil.i("Muxer", "Check should start");
            if (isAudioAdd && isVideoAdd) {
                LogUtil.i("Muxer", "Muxer starting");
                mMediaMuxer.start();
                isMediaMuxerStart = true;
                LogUtil.i("Muxer", "Muxer started");
                lock.notify();
            }
        }
    }

    private void stopMediaMuxer() {
        if (mMediaMuxer != null) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            isAudioAdd = false;
            isVideoAdd = false;
            isMediaMuxerStart = false;
            mMediaMuxer = null;
            videoMediaFormat = null;
            audioMediaFormat = null;
        }
        if (mMuxerDatas != null) {
            mMuxerDatas.clear();
            mMuxerDatas = null;
            isCheckFrame = false;
        }
        if (mOnFinishListener != null) {
            mOnFinishListener.onFinish(mOutputFile);
        }
    }

    private void lockMuxer() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /*类型封装======================================================================================*/
    @IntDef({TRACK_VIDEO, TRACK_AUDIO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TrackIndex {
    }

    /**
     * 封装需要传输的数据类型
     */
    public static class MuxerData {
        int                   trackIndex;
        ByteBuffer            byteBuf;
        MediaCodec.BufferInfo bufferInfo;

        public MuxerData(@TrackIndex int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
            this.trackIndex = trackIndex;
            this.byteBuf = byteBuf;
            this.bufferInfo = bufferInfo;
        }
    }


    /*对外暴露监听==================================================================================*/
    /** 最终结果返回监听 */
    private OnFinishListener mOnFinishListener;

    public interface OnFinishListener {
        void onFinish(File mOutputFile);
    }

    public void setOnFinishListener(OnFinishListener mOnFinishListener) {
        if (mOnFinishListener != null) {
            this.mOnFinishListener = mOnFinishListener;
        }
    }

}
