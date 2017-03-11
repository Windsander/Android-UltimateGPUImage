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
import android.widget.TextView;

import cn.co.willow.android.ultimate.gpuimage.core_record_18.VideoRecorderRenderer;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;
import cn.co.willow.android.ultimate.gpuimage.manager.VideoRecordManager;
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
public class VideoControlHolder extends BaseHolder {

    /*关键变量=======================================================================================*/
    public VideoControlHolder(Activity context) {
        super(context);
        initWhenConstruct();
    }


    /*初始化 initialize ultimate gpuimage===========================================================*/
    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.holder_video_controller, null);
        return view;
    }


    /*资源回收======================================================================================*/
    @Override
    protected void clearAllResource() {
    }


    /*对外暴露监听==================================================================================*/

}
