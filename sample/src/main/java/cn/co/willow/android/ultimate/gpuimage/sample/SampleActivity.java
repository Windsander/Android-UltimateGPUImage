package cn.co.willow.android.ultimate.gpuimage.sample;

import android.content.pm.PackageManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import java.io.File;

import cn.co.willow.android.ultimate.gpuimage.core_record_18.VideoRecorderRenderer;
import cn.co.willow.android.ultimate.gpuimage.sample.function_holder.VideoControlHolder;
import cn.co.willow.android.ultimate.gpuimage.sample.function_holder.VideoRecordHolder;
import cn.co.willow.android.ultimate.gpuimage.sample.util.UIUtils;
import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class SampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        initFunctionContainer();
        initFuncOperatePannel();
        bindControlToRecorder();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoRecordHolder.openCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoRecordHolder.releaseCamera();
    }


    /*how to use UltimateGPUImage with a holder module==============================================*/
    private FrameLayout mFunctionContainer;                 // use as a container for video preview
    private FrameLayout mFuncOperatePannel;                 // use to control video operate
    private VideoRecordHolder mVideoRecordHolder;           // module: video recorder preview
    private VideoControlHolder videoControlHolder;          // module: video recorder controller

    private void initFunctionContainer() {
        mFunctionContainer = (FrameLayout) findViewById(R.id.cl_function_container);
        mVideoRecordHolder = new VideoRecordHolder(this);
        mFunctionContainer.addView(mVideoRecordHolder.getRootView());
        requestPermission(CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE);
        mVideoRecordHolder.openCamera();
    }

    /** 初始化历史视屏栏 */
    private void initFuncOperatePannel() {
        mFuncOperatePannel = (FrameLayout) findViewById(R.id.control_pannel);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mFuncOperatePannel.getLayoutParams();
        params.height = (UIUtils.getScreenHeight() - (int) (UIUtils.getScreenWidth() * 4 / 3f)) - UIUtils.dip2px(8);
        mFuncOperatePannel.setLayoutParams(params);
        videoControlHolder = new VideoControlHolder(this);
        mFuncOperatePannel.addView(videoControlHolder.getRootView());
    }

    private void bindControlToRecorder() {
        mVideoRecordHolder.setOnRecordStateListener(new VideoRecorderRenderer.OnRecordStateListener() {
            @Override
            public void onStartReady() {
                videoControlHolder.switchRecordState(false);
            }
            @Override
            public void onStopsReady() {
                videoControlHolder.switchRecordState(true);
            }
            @Override
            public void onRecordFinish(File mOutputRecFile) {
                LogUtil.w("Video final Path::" + mOutputRecFile.getAbsolutePath());
                // TODO 视频录制完成后，截帧生成gif
            }
        });
        videoControlHolder.setOnRecordStateListener(new VideoControlHolder.RecordControlCallBack() {
            @Override
            public void startRecord() {
                mVideoRecordHolder.startRecord();
            }
            @Override
            public void stopRecord() {
                mVideoRecordHolder.stopRecord();
            }
            @Override
            public void switchCamera() {
                mVideoRecordHolder.switchCamera();
            }
        });
    }


    /*权限处理 Premission Handler===================================================================*/
    /** 申请权限 request premission */
    public void requestPermission(String... permissions) {
        if (checkPremission(permissions)) return;
        ActivityCompat.requestPermissions(this, permissions, 114);
    }

    /** 权限检测 check premission */
    public boolean checkPremission(String... permissions) {
        boolean allHave = true;
        PackageManager pm = getPackageManager();
        for (String permission : permissions) {
            switch (pm.checkPermission(permission, SampleApplication.getApplication().getPackageName())) {
                case PERMISSION_GRANTED:
                    allHave = allHave && true;
                    continue;
                case PERMISSION_DENIED:
                    allHave = allHave && false;
                    continue;
            }
        }
        return allHave;
    }

    /** 权限处理 premission result dealer */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 114 && permissions != null && permissions.length > 0) {
            String permission = "";
            for (int i = 0; i < permissions.length; i++) {
                permission = permissions[i];
                grantedResultDeal(
                        permission,
                        grantResults.length > i && grantResults[i] == PERMISSION_GRANTED);
            }
        }
    }

    /** 权限返回值处理 */
    protected void grantedResultDeal(String permission, boolean isGranted) {
        switch (permission) {
            case CAMERA:
                if (isGranted) {
                    mVideoRecordHolder.openCamera();
                }
                break;
        }
    }

}
