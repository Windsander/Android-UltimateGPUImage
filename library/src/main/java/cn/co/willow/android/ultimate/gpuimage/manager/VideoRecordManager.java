package cn.co.willow.android.ultimate.gpuimage.manager;

import android.Manifest;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;
import android.view.Surface;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import cn.co.willow.android.ultimate.gpuimage.core_config.RecorderMessageState;
import cn.co.willow.android.ultimate.gpuimage.core_looper.MessagesHandlerThread;
import cn.co.willow.android.ultimate.gpuimage.core_looper.meta.MetaData;
import cn.co.willow.android.ultimate.gpuimage.core_record_18.VideoRecorderRenderer;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;
import cn.co.willow.android.ultimate.gpuimage.manager.record_messages.CreateNewRecordInstance;
import cn.co.willow.android.ultimate.gpuimage.manager.record_messages.RecordRelease;
import cn.co.willow.android.ultimate.gpuimage.manager.record_messages.RecordStart;
import cn.co.willow.android.ultimate.gpuimage.manager.record_messages.RecordStop;
import cn.co.willow.android.ultimate.gpuimage.ui.FilterRecoderView;
import cn.co.willow.android.ultimate.gpuimage.utils.CameraUtil;

import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.VIDEO_ASPECT_RATIO;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.VIDEO_FRAME_RATE;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.VIDEO_RECORD_HEIGH;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.VIDEO_RECORD_WIDTH;

/**
 * 高版本MediaRecorder控制器
 */
public class VideoRecordManager implements VideoRecordConstrain, VideoRecordManagerCallback {

    /*关键常数=======================================================================================*/
    private static final String TAG = VideoRecordManager.class.getSimpleName();
    private static final int PREVIEW_RULE_SIZE = VIDEO_RECORD_WIDTH * VIDEO_RECORD_HEIGH;

    /*关键变量=======================================================================================*/
    private final MessagesHandlerThread mRecordHandler = new MessagesHandlerThread();
    private RecorderMessageState mCurrentRecordState = RecorderMessageState.IDLE;
    private CamcorderProfile mProfile;              // 相机配置
    private FilterRecoderView mRecorderView;       // 显示视频的控件
    private Camera mCamera;                         // 相机对象
    private Surface mRenderSurface;                // 渲染层对象（暂时没用上）
    private boolean isFrontCame = true;           // 是否使用前置摄像头

    private VideoFilterManager mFilteManager;      // 视频渲染管理者

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public VideoRecordManager(Context context, FilterRecoderView videoRecordView) {
        mRecorderView = videoRecordView;
        mFilteManager = new VideoFilterManager(context);
        mFilteManager.setGLSurfaceView(videoRecordView);
    }


    /*相机控制======================================================================================*/
    /** 初始化相机预览 */
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

        // 2.配置录制参数 init recorder params
        mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mProfile.videoFrameWidth = optimalSize.width;
        mProfile.videoFrameHeight = optimalSize.height;
        mProfile.videoFrameRate = VIDEO_FRAME_RATE;
        mProfile.videoBitRate = (int) (mProfile.videoFrameRate * optimalSize.width * optimalSize.height * 1.5f);
        mProfile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
        mProfile.videoCodec = MediaRecorder.VideoEncoder.MPEG_4_SP;
        mProfile.audioCodec = MediaRecorder.AudioEncoder.AMR_NB;
        mCamera.setParameters(parameters);

        // 3.配置预览界面 init preview
        mRecorderView.getHolder().setFixedSize(optimalSize.height, optimalSize.width);

        // 4.关联渲染器  bond filter-manager
        mFilteManager.setUpCamera(mCamera, isFrontCame);
        mFilteManager.setOnSurfaceSetListener(new VideoRenderer.OnSurfaceSetListener() {
            @Override
            public void onSurfaceSet(SurfaceTexture mSurfaceTexture) {
                mRenderSurface = new Surface(mSurfaceTexture);
            }
        });
    }

    /** 释放照相机 */
    public boolean releaseCamera() {
        /*if (mCurrentRecordState != RecorderMessageState.IDLE)
            return false;*/
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return true;
    }

    /** 切换滤镜 */
    public void setFilter(GPUImageFilter filter) {
        if (mFilteManager == null) return;
        mFilteManager.setFilter(filter);
    }


    /*录制流程整体控制==============================================================================*/
    /** 开始录像（注意：方法必须在{@link VideoRecordManager#openCamera()} 相机完成初始化设置后，才能执行） */
    @Override
    public void startRecord(File mOutputRecFile) {
        mRecordHandler.pauseQueueProcessing(TAG);
        mRecordHandler.clearAllPendingMessages(TAG);
        mRecordHandler.addMessages(Arrays.asList(
                new CreateNewRecordInstance(mFilteManager, mOutputRecFile, mProfile, this),
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

    /** 相机翻转 */
    public void reverseCamera() {
       /* switch (mCurrentRecordState) {
            case START_RECORD:
            case RECORDING:
                stopRecord();
                break;
            case INITIALIZING:
            case INITIALIZED:
            case PREPARING:
            case PREPARED:
            case ERROR:
                releaseRecord();
                break;
        }*/
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
    /** MediaRecorder关键步骤监听 */
    public void setOnRecordStateListener(VideoRecorderRenderer.OnRecordStateListener mOnRecordStateListener) {
        mFilteManager.setOnRecordStateListener(mOnRecordStateListener);
    }
}
