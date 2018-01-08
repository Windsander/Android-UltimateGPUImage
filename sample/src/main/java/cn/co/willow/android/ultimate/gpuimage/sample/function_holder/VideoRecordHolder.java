package cn.co.willow.android.ultimate.gpuimage.sample.function_holder;

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
import android.widget.TextView;

import cn.co.willow.android.ultimate.gpuimage.core_render.VideoRecorderRenderer;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;
import cn.co.willow.android.ultimate.gpuimage.manager.video_recorder.VideoRecordManager;
import cn.co.willow.android.ultimate.gpuimage.sample.R;
import cn.co.willow.android.ultimate.gpuimage.sample.self_defined_filter.FilterType;
import cn.co.willow.android.ultimate.gpuimage.sample.util.FileUtil;
import cn.co.willow.android.ultimate.gpuimage.ui.FilterRecoderView;
import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;

import static cn.co.willow.android.ultimate.gpuimage.ui.FilterRecoderView.ORIENTATION_LEFT;
import static cn.co.willow.android.ultimate.gpuimage.ui.FilterRecoderView.ORIENTATION_RIGHT;

/**
 * 组件：视频录制
 * <p>
 * Created by willow.li on 2016/10/22.
 */
public class VideoRecordHolder extends BaseHolder {

    /*关键变量=======================================================================================*/
    private FilterRecoderView  mRecorderViews;          // 显示视频的控件
    private TextView           mCurFilterName;          // 当前滤镜名称
    private VideoRecordManager mRecordManager;
    private int     mCurFilter = 420;
    private boolean isFinish   = true;

    public VideoRecordHolder(Activity context) {
        super(context);
        initWhenConstruct();
    }


    /*初始化 initialize ultimate gpuimage===========================================================*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.holder_video_recorder_gl, null);
        mRecorderViews = (FilterRecoderView) view.findViewById(R.id.vp_video_recorder_gl);
        mCurFilterName = (TextView) view.findViewById(R.id.tv_filter_name);
        mRecordManager = new VideoRecordManager(context, mRecorderViews);
        initGesture();
        return view;
    }

    /** 配置手势操作 set gesture operation */
    private void initGesture() {
        mRecorderViews.setSwitchFilterListener(new FilterRecoderView.SwitchFilterListener() {
            @Override
            public void switchFilter(int orientation) {
                doSwitchFilter(orientation);
            }

            @Override
            public void handFocusTo(MotionEvent e) {

            }

            @Override
            public void focusLength(int progress, float factor) {

            }

            @Override
            public void releaseCamera() {

            }
        });
    }


    /*手势逻辑：滤镜================================================================================*/
    /** 切换滤镜 switch camera filter */
    public synchronized void doSwitchFilter(int orientation) {
        switch (orientation) {
            case ORIENTATION_LEFT:
                mCurFilter = mCurFilter - 1;
                break;
            case ORIENTATION_RIGHT:
                mCurFilter = mCurFilter + 1;
                break;
        }
        int mAbsPosition = mCurFilter % FilterType.Type.getTypeSize();
        LogUtil.w("Video Filter::", "curIndex:" + mAbsPosition + " in " + FilterType.Type.getTypeSize());
        doShowFilterName(mAbsPosition, orientation);
        GPUImageFilter filter = FilterType.Type.getFilter(mAbsPosition).getFilter();
        mRecordManager.setFilter(filter);
    }

    /** 显示滤镜名称 show filter name with animation */
    private void doShowFilterName(int position, int orientation) {
        mCurFilterName.setText(FilterType.Type.getFilter(position).getName());
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator rAnim = ObjectAnimator.ofFloat(mCurFilterName, "translationX",
                orientation == ORIENTATION_RIGHT ? 150f : 0,
                orientation == ORIENTATION_RIGHT ? 0 : 180f);
        ObjectAnimator lAnim = ObjectAnimator.ofFloat(mCurFilterName, "translationX",
                orientation == ORIENTATION_RIGHT ? 0 : -150f,
                orientation == ORIENTATION_RIGHT ? -180f : 0);
        switch (orientation) {
            case ORIENTATION_RIGHT:
                rAnim.setInterpolator(new DecelerateInterpolator());
                lAnim.setInterpolator(new AccelerateInterpolator());
                set.play(lAnim).after(rAnim);
                break;
            case ORIENTATION_LEFT:
                lAnim.setInterpolator(new DecelerateInterpolator());
                rAnim.setInterpolator(new AccelerateInterpolator());
                set.play(rAnim).after(lAnim);
                break;
        }
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mCurFilterName.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCurFilterName.setVisibility(View.GONE);
            }
        });
        set.setDuration(200).start();
    }
    
    
    /*录制控制======================================================================================*/
    /** 开启相机 */
    public void openCamera() {
        mRecordManager.openCamera();
    }

    /** 释放相机 */
    public void releaseCamera() {
        mRecordManager.releaseCamera();
    }

    /** 开始录制 */
    public void startRecord() {
        isFinish = false;
        String videoSavePath = FileUtil.getVideoSavePath();
        LogUtil.w("Video save Path::" + videoSavePath);
        mRecordManager.startRecord(FileUtil.computeMD5ForVideoFile(videoSavePath));
    }

    /** 结束录制 */
    public void stopRecord() {
        isFinish = true;
        mRecordManager.stopRecord();
    }

    /** 切换前后摄像头 */
    public void switchCamera() {
        mRecordManager.switchCamera();
    }


    /*资源回收======================================================================================*/
    @Override
    protected void clearAllResource() {
        releaseCamera();
        mRecorderViews = null;
    }


    /*对外暴露监听==================================================================================*/
    /** 播放器状态监听 */
    public void setOnRecordStateListener(VideoRecorderRenderer.OnRecordStateListener mOnRecordStateListener) {
        mRecordManager.setOnRecordStateListener(mOnRecordStateListener);
    }

    public boolean isFinish() {
        return isFinish;
    }

}
