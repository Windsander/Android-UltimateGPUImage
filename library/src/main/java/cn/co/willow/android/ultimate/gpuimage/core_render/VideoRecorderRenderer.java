package cn.co.willow.android.ultimate.gpuimage.core_render;

import android.app.Activity;
import android.os.Build;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import javax.microedition.khronos.opengles.GL10;

import cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig;
import cn.co.willow.android.ultimate.gpuimage.core_config.RecordCoderState;
import cn.co.willow.android.ultimate.gpuimage.core_record_18.TextureMovieEncoder;
import cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder.XMediaMuxer;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;
import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;

import static cn.co.willow.android.ultimate.gpuimage.core_config.RecordCoderState.IDLE;
import static cn.co.willow.android.ultimate.gpuimage.core_config.RecordCoderState.PREPARED;
import static cn.co.willow.android.ultimate.gpuimage.core_config.RecordCoderState.START;
import static cn.co.willow.android.ultimate.gpuimage.core_config.RecordCoderState.STOP;

/**
 * 具有录制功能的视频渲染器
 * 注意：
 * 此渲染器只能录制单段视频，中途不能暂停
 * 流程为（开启 -> 录制 -> 暂停）
 * <p>
 * Created by willow.li on 2016/10/28.
 */
public class VideoRecorderRenderer extends BaseRenderer {

    private final byte[] lock = new byte[0];

    private final AtomicReference<RecordCoderState> mCoderStatus = new AtomicReference<>();
    private TextureMovieEncoder mTMEncoder;                     // 音视频建简易编码器
    private boolean mStartCoder = false;                     // 编码器启用标志

    public VideoRecorderRenderer(GPUImageFilter filter,
                                 OutputConfig.VideoOutputConfig videoConfig,
                                 OutputConfig.AudioOutputConfig audioConfig) {
        super(filter);
        initTMEncoder(videoConfig, audioConfig);
        mCoderStatus.set(IDLE);
    }

    /** 初始化编码器的控制器 */
    private void initTMEncoder(OutputConfig.VideoOutputConfig videoConfig,
                               OutputConfig.AudioOutputConfig audioConfig) {
        if (mTMEncoder == null) {
            mTMEncoder = new TextureMovieEncoder();
            mTMEncoder.setAVConfig(videoConfig, audioConfig);
            mTMEncoder.setOnFinishListener(new XMediaMuxer.OnFinishListener() {
                @Override
                public void onFinish(File mOutputFile) {
                    if (mOnRecordStateListener != null) {
                        mOnRecordStateListener.onRecordFinish(mOutputFile);
                    }
                }
            });
        }
    }


    /*参数配置====================================================================================*/
    /** 录制编码器开关 */
    public void changeCoderState(boolean startCodeing) {
        mStartCoder = startCodeing;
    }

    /** 滤镜设置 */
    @Override
    public void setFilter(GPUImageFilter filter) {
        super.setFilter(filter);
    }

    /** 获取当前编码器状态 */
    public RecordCoderState getCurrentState() {
        return mCoderStatus.get();
    }


    /*渲染录制逻辑==================================================================================*/
    @Override
    public void onDrawFrame(final GL10 gl) {
        super.onDrawFrame(gl);
        if (mStartCoder) {
            startCoder();
        } else {
            stopCoder();
        }
        mTMEncoder.frameAvailable(mSurfaceTexture);
    }


    /*编码器控制====================================================================================*/
    /** 2-1.编码器基本配置准备 */
    public void prepareCoder(File outputFile,
                             OutputConfig.VideoOutputConfig videoConfig,
                             OutputConfig.AudioOutputConfig audioConfig) {
        synchronized (mCoderStatus) {
            switch (mCoderStatus.get()) {
                case PREPARED:
                case STOP:
                case START:
                    throw new IllegalStateException("prepare, called from illegal state " + mCoderStatus);
                case IDLE:
                    mCoderStatus.set(PREPARED);
                    break;
                default:
                    throw new IllegalStateException("unknown status: " + mCoderStatus);
            }
            mTMEncoder.setAVConfig(videoConfig, audioConfig);
            mTMEncoder.setOutputFile(outputFile);
            if (mOnRecordStateListener != null) {
                mOnRecordStateListener.onStopsReady();
            }
        }
    }

    /** 2-2.编码器开始录制 */
    private void startCoder() {
        synchronized (mCoderStatus) {
            switch (mCoderStatus.get()) {
                case IDLE:
                case STOP:
                    throw new IllegalStateException("start, called from illegal state " + mCoderStatus);
                case START:
                    return;
                case PREPARED:
                    mCoderStatus.set(START);
                    break;
                default:
                    throw new IllegalStateException("unknown status: " + mCoderStatus);
            }
            mTMEncoder.startRecording();
        }
    }

    /** 2-3.编码器开始录制 */
    private void stopCoder() {
        synchronized (mCoderStatus) {
            switch (mCoderStatus.get()) {
                case IDLE:
                case PREPARED:
                case STOP:
                    return;
                case START:
                    mCoderStatus.set(STOP);
                    break;
                default:
                    throw new IllegalStateException("unknown status: " + mCoderStatus);
            }
            mTMEncoder.stopRecording();
            if (mOnRecordStateListener != null) {
                mOnRecordStateListener.onStartReady();
            }
        }
        releaseCoder();
    }

    /** 2-4.释放编码器上次录制资源 */
    public void releaseCoder() {
        synchronized (mCoderStatus) {
            switch (mCoderStatus.get()) {
                case IDLE:
                case START:
                    throw new IllegalStateException("release, called from illegal state " + mCoderStatus);
                case PREPARED:
                case STOP:
                    mCoderStatus.set(IDLE);
                    break;
                default:
                    throw new IllegalStateException("unknown status: " + mCoderStatus);
            }
        }
    }

    /** 当退出录制界面的时候{@link Activity#onDestroy()}，调用此方法 */
    public void clearAll() {
        mTMEncoder = null;
    }


    /*对外暴露监听==================================================================================*/
    /** 播放器状态监听 */
    private OnRecordStateListener mOnRecordStateListener;

    public interface OnRecordStateListener {
        void onStartReady();
        void onStopsReady();
        void onRecordFinish(File mOutputRecFile);
    }

    public void setOnRecordStateListener(OnRecordStateListener mOnRecordStateListener) {
        if (mOnRecordStateListener != null) {
            this.mOnRecordStateListener = mOnRecordStateListener;
        }
    }
}