package cn.co.willow.android.ultimate.gpuimage.sample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import cn.co.willow.android.ultimate.gpuimage.core_record_18.VideoRecorderRenderer;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;
import cn.co.willow.android.ultimate.gpuimage.manager.VideoRecordManager;
import cn.co.willow.android.ultimate.gpuimage.sample.self_defined_filter.FilterType;
import cn.co.willow.android.ultimate.gpuimage.sample.util.FileUtil;
import cn.co.willow.android.ultimate.gpuimage.sample.util.UIUtils;
import cn.co.willow.android.ultimate.gpuimage.ui.FilterRecoderView;
import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;

import static cn.co.willow.android.ultimate.gpuimage.ui.FilterRecoderView.ORIENTATION_LEFT;
import static cn.co.willow.android.ultimate.gpuimage.ui.FilterRecoderView.ORIENTATION_RIGHT;

/**
 * 组件：视频录制
 * <p>
 * Created by willow.li on 2016/10/22.
 */
public class VideoControlHolder extends BaseHolder implements View.OnClickListener {

    /*关键变量=======================================================================================*/
    private Button mBtnRecordsVideo;
    private Button mBtnSwitchCamera;
    private boolean isRecording;

    public VideoControlHolder(Activity context) {
        super(context);
        initWhenConstruct();
    }


    /*初始化 initialize ultimate gpuimage===========================================================*/
    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.holder_video_controller, null);
        mBtnRecordsVideo = (Button) view.findViewById(R.id.start_record_btn);
        mBtnSwitchCamera = (Button) view.findViewById(R.id.switch_camera_btn);
        mBtnRecordsVideo.setOnClickListener(this);
        mBtnSwitchCamera.setOnClickListener(this);
        return view;
    }

    @Override public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_record_btn:
                if (mRecordControlCallBack != null) {
                    if (isRecording) {
                        mRecordControlCallBack.stopRecord();
                    } else {
                        mRecordControlCallBack.startRecord();
                    }
                }
                break;
            case R.id.switch_camera_btn:
                if (mRecordControlCallBack != null) {
                    mRecordControlCallBack.switchCamera();
                }
                break;
        }
    }

    public void switchRecordState(final boolean isRecording) {
        this.isRecording = isRecording;
        UIUtils.runInMainThread(new Runnable() {
            @Override public void run() {
                if (isRecording) {
                    mBtnRecordsVideo.setText(R.string.control_stop_record);
                } else {
                    mBtnRecordsVideo.setText(R.string.control_start_record);
                }
            }
        });
    }


    /*资源回收======================================================================================*/
    @Override
    protected void clearAllResource() {
    }


    /*对外暴露监听==================================================================================*/
    /** 播放器状态监听 */
    private RecordControlCallBack mRecordControlCallBack;

    public interface RecordControlCallBack {
        void startRecord();
        void stopRecord();
        void switchCamera();
    }

    public void setOnRecordStateListener(RecordControlCallBack mRecordControlCallBack) {
        if (mRecordControlCallBack != null) {
            this.mRecordControlCallBack = mRecordControlCallBack;
        }
    }
}
