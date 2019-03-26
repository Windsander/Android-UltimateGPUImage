package cn.co.willow.android.ultimate.gpuimage.sample.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.io.File;

import cn.co.willow.android.ultimate.gpuimage.core_render.VideoRecorderRenderer;
import cn.co.willow.android.ultimate.gpuimage.sample.R;
import cn.co.willow.android.ultimate.gpuimage.sample.SampleApplication;
import cn.co.willow.android.ultimate.gpuimage.sample.function_holder.VideoControlHolder;
import cn.co.willow.android.ultimate.gpuimage.sample.function_holder.VideoRecordHolder;
import cn.co.willow.android.ultimate.gpuimage.sample.interaction_logic.SamplePresenter;
import cn.co.willow.android.ultimate.gpuimage.sample.util.UIUtils;
import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class SampleVideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        initInteractionLogics();
        initFunctionContainer();
        initFuncOperatePannel();
        bindControlToRecorder();

        // set page animator
        initPageAnimator();
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

    @Override
    public void onBackPressed() {
        if (mVideoRecordHolder != null) {
            if (!mVideoRecordHolder.isFinish()) {
                UIUtils.showToastSafe(R.string.exit_unsafe_toast);
                return;
            } else {
                mFunctionContainer.removeAllViews();
            }
        }
        if (videoControlHolder != null) {
            videoControlHolder.exitAnim();
        }
        super.onBackPressed();
    }

    /*auxiliary method for this page================================================================*/
    private void initPageAnimator() {
        Transition enterTrans = TransitionInflater.from(this).inflateTransition(R.transition.trans_video_enter);
        Transition exitTrans = TransitionInflater.from(this).inflateTransition(R.transition.trans_video_exit);
        getWindow().setEnterTransition(enterTrans);
        getWindow().setSharedElementEnterTransition(initSharedElementEnterTransition());
        getWindow().setExitTransition(exitTrans);
    }

    private Transition initSharedElementEnterTransition() {
        final Transition shareTrans = TransitionInflater.from(this).inflateTransition(R.transition.trans_control_pannel);
        shareTrans.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                if (videoControlHolder == null) return;
                View mControlPannel = videoControlHolder.getRootView();
                mControlPannel.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
               /* View mRippleWave = findViewById(R.id.control_pannel);
                Animator circularReveal = ViewAnimationUtils.createCircularReveal(mRippleWave,
                        mRippleWave.getWidth() / 2,
                        mRippleWave.getHeight() / 2,
                        mRippleWave.getWidth() / 2,
                        Math.max(mRippleWave.getWidth(), mRippleWave.getHeight())
                );
                circularReveal.setDuration(600);
                circularReveal.start();*/

                if (videoControlHolder != null) {
                    videoControlHolder.enterAnim();
                }
                shareTrans.removeListener(this);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
        return shareTrans;
    }


    /*how to use UltimateGPUImage with a holder module==============================================*/
    private FrameLayout mFunctionContainer;                  // use as a container for video preview
    private FrameLayout mFuncOperatePannel;                  // use to control video operate
    private VideoRecordHolder mVideoRecordHolder;                  // module: video recorder preview
    private VideoControlHolder videoControlHolder;                  // module: video recorder controller
    private SamplePresenter mInteractionLogic;

    private void initInteractionLogics() {
        mInteractionLogic = SamplePresenter.init(new SamplePresenter.onCoverListener() {
            @Override
            public void onCoverFinish(String gifUrl) {
                LogUtil.w("Video Cover Url::" + gifUrl);
                videoControlHolder.setVideoCover(gifUrl);
            }
        });
    }

    private void initFunctionContainer() {
        mFunctionContainer = (FrameLayout) findViewById(R.id.cl_function_container);
        mVideoRecordHolder = new VideoRecordHolder(this);
        mFunctionContainer.addView(mVideoRecordHolder.getRootView());
        requestPermission(CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE);
        mVideoRecordHolder.openCamera();
    }

    /**
     * 初始化历史视屏栏
     */
    private void initFuncOperatePannel() {
        mFuncOperatePannel = (FrameLayout) findViewById(R.id.control_pannel);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mFuncOperatePannel.getLayoutParams();
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
                mInteractionLogic.doUpdateVideoData(mOutputRecFile);
            }
        });
        videoControlHolder.setOnRecordStateListener(new VideoControlHolder.RecordControlCallBack() {
            @Override
            public void playVideo() {
                mInteractionLogic.doPlayerVideo(SampleVideoActivity.this);
            }

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

    /**
     * 申请权限 request premission
     */
    public void requestPermission(String... permissions) {
        if (checkPremission(permissions)) return;
        ActivityCompat.requestPermissions(this, permissions, 114);
    }

    /**
     * 权限检测 check premission
     */
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

    /**
     * 权限处理 premission result dealer
     */
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

    /**
     * 权限返回值处理
     */
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
