package cn.co.willow.android.ultimate.gpuimage.core_render;

import android.app.Activity;
import android.opengl.EGL14;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.microedition.khronos.opengles.GL10;

import cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig;
import cn.co.willow.android.ultimate.gpuimage.core_config.RecordCoderState;
import cn.co.willow.android.ultimate.gpuimage.core_record_18.TextureMovieEncoder;
import cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder.XMediaMuxer;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;

import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.TIMEOUT_USEC;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.VIDEO_BIT_RATE;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.VIDEO_RECORD_HEIGH;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.VIDEO_RECORD_WIDTH;
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
    private boolean mStartCoder = false;                        // 编码器启用标志
    private File mOutputFile = null;
    private OutputConfig.VideoOutputConfig mVideoConfig = new OutputConfig.VideoOutputConfig();
    private OutputConfig.AudioOutputConfig mAudioConfig = new OutputConfig.AudioOutputConfig();

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public VideoRecorderRenderer(GPUImageFilter filter) {
        super(filter);
        initTMEncoder();
        mCoderStatus.set(IDLE);
    }

    /**
     * 初始化编码器的控制器
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initTMEncoder() {
        if (mTMEncoder == null) {
            mTMEncoder = new TextureMovieEncoder(mFilter);
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

    /**
     * 1.设置输出的滤镜视频文件位置
     */
    public void setOutputFile(File outputFile) {
        synchronized (lock) {
            mOutputFile = outputFile;
        }
    }

    /**
     * 2.录制开关，只能在{@link VideoRecorderRenderer#setOutputFile(File)}之后使用
     */
    public void changeCoderState(boolean startCodeing) {
        if (mOutputFile == null) return;
        mStartCoder = startCodeing;
    }

    public void resetOutputFile() {
        synchronized (lock) {
            mOutputFile = null;
        }
    }

    /**
     * 滤镜设置
     */
    @Override
    public void setFilter(GPUImageFilter filter) {
        super.setFilter(filter);
        mTMEncoder.setFilter(filter);
    }

    /**
     * 获取当前编码器状态
     */
    public RecordCoderState getCurrentState() {
        return mCoderStatus.get();
    }


    /*渲染录制逻辑==================================================================================*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onDrawFrame(final GL10 gl) {
        super.onDrawFrame(gl);
        if (mStartCoder) {
            startCoder();
        } else {
            stopCoder();
        }
        mTMEncoder.setTextureId(mGLTextureId);
        mTMEncoder.frameAvailable(mSurfaceTexture);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void adjustImageScaling() {
        super.adjustImageScaling();
        initVideoEncoder();
    }


    /*编码器控制====================================================================================*/

    /**
     * 1.初始化视频编码器（每次适配时都需调用）
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initVideoEncoder() {
        initTMEncoder();
        mTMEncoder.setGLCubeBuffer(mGLCubeBuffer);
        mTMEncoder.setGLTextureBuffer(mGLTextureBuffer);
    }

    /**
     * 2-1.编码器基本配置准备
     */
    public void prepareCoder(
            File outputFile,
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
            setOutputFile(outputFile);
            mVideoConfig = (videoConfig == null) ? mVideoConfig : videoConfig;
            mAudioConfig = (audioConfig == null) ? mAudioConfig : audioConfig;
            if (mOnRecordStateListener != null) {
                mOnRecordStateListener.onStopsReady();
            }
        }
    }

    /**
     * 2-2.编码器开始录制
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
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
            mTMEncoder.startRecording(
                    new TextureMovieEncoder.EncoderConfig(
                            EGL14.eglGetCurrentContext(),
                            mOutputFile,
                            mVideoConfig,
                            mAudioConfig
                    ));
        }
    }

    /**
     * 2-3.编码器开始录制
     */
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
            // wait 1 seconds to deal with record, seriously important action.
            Executors.newScheduledThreadPool(1)
                    .schedule(new Runnable() {
                        @Override
                        public void run() {
                            if (mOnRecordStateListener != null) {
                                mOnRecordStateListener.onStartReady();
                            }
                        }
                    }, 1, TimeUnit.SECONDS);
        }
        releaseCoder();
    }

    /**
     * 2-4.释放编码器上次录制资源
     */
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
            resetOutputFile();
        }
    }

    /**
     * 当退出录制界面的时候{@link Activity#onDestroy()}，调用此方法
     */
    public void clearAll() {
        mTMEncoder = null;
    }


    /*对外暴露监听==================================================================================*/
    /**
     * 播放器状态监听
     */
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