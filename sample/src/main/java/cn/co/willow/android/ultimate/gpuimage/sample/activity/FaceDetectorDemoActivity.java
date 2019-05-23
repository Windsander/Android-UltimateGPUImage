package cn.co.willow.android.ultimate.gpuimage.sample.activity;

import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import cn.co.willow.android.ultimate.gpuimage.core_config.Rotation;
import cn.co.willow.android.ultimate.gpuimage.core_render.BaseRenderer;
import cn.co.willow.android.ultimate.gpuimage.sample.R;
import cn.co.willow.android.ultimate.gpuimage.ui.FilterRecoderView;
import cn.co.willow.android.ultimate.gpuimage.utils.CameraUtil;
import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;

import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.VIDEO_ASPECT_RATIO;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.VIDEO_RECORD_HEIGH;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.VIDEO_RECORD_WIDTH;

public class FaceDetectorDemoActivity extends AppCompatActivity {

    private static final int PREVIEW_RULE_SIZE = VIDEO_RECORD_WIDTH * VIDEO_RECORD_HEIGH;

    private FilterRecoderView mRecorderView;        // 显示视频的控件
    private Camera mCamera;                         // 相机对象
    private boolean isFrontCame = true;             // 是否使用前置摄像头

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detector);
        mRecorderView = findViewById(R.id.vp_video_recorder_gl);
        mRecorderView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {

            }

            @Override
            public void onDrawFrame(GL10 gl) {

            }
        });
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
        parameters.setPreviewSize(VIDEO_RECORD_WIDTH, VIDEO_RECORD_HEIGH);
        // 设置对焦模式
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

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
}
