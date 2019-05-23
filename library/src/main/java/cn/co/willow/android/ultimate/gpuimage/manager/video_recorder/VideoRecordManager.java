package cn.co.willow.android.ultimate.gpuimage.manager.video_recorder;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.RequiresApi;
import android.view.Surface;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig;
import cn.co.willow.android.ultimate.gpuimage.core_config.RecorderMessageState;
import cn.co.willow.android.ultimate.gpuimage.core_looper.MessagesHandlerThread;
import cn.co.willow.android.ultimate.gpuimage.core_looper.meta.MetaData;
import cn.co.willow.android.ultimate.gpuimage.core_render.VideoRecorderRenderer;
import cn.co.willow.android.ultimate.gpuimage.core_render.BaseRenderer;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.record_messages.CreateNewRecordInstance;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.record_messages.RecordRelease;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.record_messages.RecordStart;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.record_messages.RecordStop;
import cn.co.willow.android.ultimate.gpuimage.ui.FilterRecoderView;
import cn.co.willow.android.ultimate.gpuimage.utils.CameraUtil;
import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;

import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.VIDEO_ASPECT_RATIO;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.VIDEO_FRAME_RATE;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.VIDEO_RECORD_HEIGH;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.VIDEO_RECORD_WIDTH;

/**
 * 高版本MediaRecorder控制器
 */
public class VideoRecordManager implements VideoRecordConstrain, VideoRecordManagerCallback {

    private static final String TAG = VideoRecordManager.class.getSimpleName();
    private static final int PREVIEW_RULE_SIZE = VIDEO_RECORD_WIDTH * VIDEO_RECORD_HEIGH;

    private final MessagesHandlerThread mRecordHandler = new MessagesHandlerThread();
    private RecorderMessageState mCurrentRecordState = RecorderMessageState.IDLE;
    private FilterRecoderView mRecorderView;        // 显示视频的控件
    private Camera mCamera;                         // 相机对象
    private Surface mRenderSurface;                 // 渲染层对象（暂时没用上）
    private boolean isFrontCame = true;             // 是否使用前置摄像头
    private OutputConfig.VideoOutputConfig mVideoConfig;
    private OutputConfig.AudioOutputConfig mAudioConfig;

    private VideoFilterManager mFilteManager;       // 视频渲染管理者

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public VideoRecordManager(Context context, FilterRecoderView videoRecordView) {
        mRecorderView = videoRecordView;
        mFilteManager = new VideoFilterManager(context);
        mFilteManager.setGLSurfaceView(videoRecordView);
    }

    /*相机控制======================================================================================*/

    /**
     * 初始化相机预览
     */
    @MainThread
    public void openCamera() {
        if (mCamera != null) return;
        // 1.配置相机 init camera
        mCamera = isFrontCame ?
                CameraUtil.getDefaultFrontFacingCameraInstance() :
                CameraUtil.getDefaultBackFacingCameraInstance();
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size optimalSize = CameraUtil.getPreviewRuleSize(
                parameters, PREVIEW_RULE_SIZE, VIDEO_ASPECT_RATIO);
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);
        // 设置对焦模式
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        LogUtil.w("Camera::", "size = " + optimalSize.width + "*" + optimalSize.height);

        // 2.配置录制参数 init recorder params
       /* mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mProfile.videoFrameWidth = optimalSize.width;
        mProfile.videoFrameHeight = optimalSize.height;
        mProfile.videoFrameRate = VIDEO_FRAME_RATE;
        mProfile.videoBitRate = (int) (mProfile.videoFrameRate * optimalSize.width * optimalSize.height * 1.5f);
        mProfile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
        mProfile.videoCodec = MediaRecorder.VideoEncoder.MPEG_4_SP;
        mProfile.audioCodec = MediaRecorder.AudioEncoder.AMR_NB;
        mCamera.setParameters(parameters);
        LogUtil.w("Camera::", "record size = " + mProfile.videoFrameWidth + "*" + mProfile.videoFrameHeight);*/

        // 3.配置预览界面 init preview
        mRecorderView.getHolder().setFixedSize(optimalSize.height, optimalSize.width);

        // 4.关联渲染器  bond filter-manager
        mFilteManager.setUpCamera(mCamera, isFrontCame);
        mFilteManager.setOnSurfaceSetListener(new BaseRenderer.OnSurfaceSetListener() {
            @Override
            public void onSurfaceSet(SurfaceTexture surfaceTexture) {
                mRenderSurface = new Surface(surfaceTexture);
            }
        });
    }

    /**
     * 释放照相机
     */
    public boolean releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return true;
    }

    /**
     * 切换滤镜
     */
    public void setFilter(GPUImageFilter filter) {
        if (mFilteManager == null) return;
        mFilteManager.setFilter(filter);
    }

    /**
     * 设置录屏输出参数
     */
    public void setAVConfig(
            OutputConfig.VideoOutputConfig videoConfig,
            OutputConfig.AudioOutputConfig audioConfig) {
        if (videoConfig != null) {
            mVideoConfig = videoConfig;
        }
        if (audioConfig != null) {
            mAudioConfig = audioConfig;
        }
    }


    /*录制流程整体控制==============================================================================*/

    /**
     * 开始录像（注意：方法必须在{@link VideoRecordManager#openCamera()} 相机完成初始化设置后，才能执行）
     */
    @Override
    public void startRecord(File mOutputRecFile) {

        mRecordHandler.pauseQueueProcessing(TAG);
        mRecordHandler.clearAllPendingMessages(TAG);
        mRecordHandler.addMessages(Arrays.asList(
                new CreateNewRecordInstance(
                        mFilteManager,
                        mOutputRecFile,
                        mVideoConfig,
                        mAudioConfig,
                        this),
                new RecordStart(mFilteManager, this)
        ));
        mRecordHandler.resumeQueueProcessing(TAG);
    }

    @Override
    public void stopRecord() {
        mRecordHandler.pauseQueueProcessing(TAG);
        mRecordHandler.clearAllPendingMessages(TAG);
        mRecordHandler.addMessage(new RecordStop(mFilteManager, this));
        mRecordHandler.resumeQueueProcessing(TAG);
        mRenderSurface = null;
    }

    @Override
    public void releaseRecord() {
        mRecordHandler.pauseQueueProcessing(TAG);
        mRecordHandler.clearAllPendingMessages(TAG);
        mRecordHandler.addMessage(new RecordRelease(mFilteManager, this));
        mRecordHandler.resumeQueueProcessing(TAG);
        mRenderSurface = null;
    }

    /**
     * 相机翻转
     */
    public void switchCamera() {
        releaseCamera();
        isFrontCame = !isFrontCame;
        openCamera();
    }


    /*状态回调控制==================================================================================*/
    @Override
    public void setCurrentItem(MetaData currentItemMetaData, FilterRecoderView videoRecordView) {
        mRecorderView = videoRecordView;
    }

    @Override
    public void setVideoRecorderState(VideoFilterManager mFilteManager, RecorderMessageState recorderMessageState) {
        mCurrentRecordState = recorderMessageState;
    }

    @Override
    public RecorderMessageState getCurrentPlayerState() {
        return mCurrentRecordState;
    }


    /*对外暴露监听==================================================================================*/

    /**
     * MediaRecorder关键步骤监听
     */
    public void setOnRecordStateListener(VideoRecorderRenderer.OnRecordStateListener mOnRecordStateListener) {
        mFilteManager.setOnRecordStateListener(mOnRecordStateListener);
    }
}
