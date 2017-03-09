package cn.co.willow.android.ultimate.gpuimage.ui;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.View;

/**
 * 核心：视频录制相机界面提供者
 * <p>
 * Created by willow.li on 2016/10/26.
 */

public class FilterRecoderView extends GLSurfaceView implements View.OnTouchListener {

    public static final int ORIENTATION_LEFT = 1;
    public static final int ORIENTATION_RIGHT = 2;

    /*关键变量======================================================================================*/
    private Context mContext;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector mainsGestureDetector;
    private int touchPoints;

    public FilterRecoderView(Context context) {
        this(context, null);
    }

    public FilterRecoderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initScaleListener();
        initTouchListener();
        setOnTouchListener(this);
    }


    /*手势逻辑======================================================================================*/
    /** 初始化缩放监听 */
    private void initScaleListener() {
        scaleGestureDetector = new ScaleGestureDetector(mContext,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        if (mSwitchFilterListener != null) {
                            mSwitchFilterListener.focusLength(-1, detector.getScaleFactor());
                        }
                        return true;
                    }
                    @Override
                    public boolean onScaleBegin(ScaleGestureDetector detector) {
                        return true;
                    }
                });
    }

    /** 初始化手势监听 */
    private void initTouchListener() {
        mainsGestureDetector = new GestureDetector(mContext,
                new GestureDetector.OnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public void onShowPress(MotionEvent e) {
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        if (mSwitchFilterListener != null) {
                            mSwitchFilterListener.handFocusTo(e);
                        }
                        return true;
                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                        if (touchPoints > 1) {
                            return scaleGestureDetector.onTouchEvent(e2);
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                        if (e1 == null || e2 == null) {
                            return true;
                        }
                        float x = e2.getX() - e1.getX();
                        if (mSwitchFilterListener != null && Math.abs(x) > 150 && touchPoints == 1) {
                            if (x > 0) {
                                mSwitchFilterListener.switchFilter(ORIENTATION_LEFT);
                            } else if (x < 0) {
                                mSwitchFilterListener.switchFilter(ORIENTATION_RIGHT);
                            }
                        }
                        return true;
                    }
                });

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (fusionCallBack != null && fusionCallBack.isMenuOpen()) {
            fusionCallBack.closeFilterMenu();
            return true;
        }
        touchPoints = event.getPointerCount();
        return mainsGestureDetector.onTouchEvent(event);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mSwitchFilterListener != null) {
            mSwitchFilterListener.releaseCamera();
        }
        super.surfaceDestroyed(holder);
    }


    /*对外暴露监听==================================================================================*/
    /** 菜单状态监听 */
    private FusionCallBack fusionCallBack;

    public interface FusionCallBack {
        boolean isMenuOpen();
        void closeFilterMenu();
    }

    public void setCallback(FusionCallBack fusionCallBack) {
        this.fusionCallBack = fusionCallBack;
    }

    /** 手势操作监听 */
    private SwitchFilterListener mSwitchFilterListener;

    public interface SwitchFilterListener {
        void switchFilter(int orientation);                         //切换滤镜
        void handFocusTo(MotionEvent e);                            //手动对焦
        void focusLength(int progress, float factor);              //调节焦距
        void releaseCamera();                                       //释放相机资源
    }

    public void setSwitchFilterListener(SwitchFilterListener switchFilterListener) {
        mSwitchFilterListener = switchFilterListener;
    }

}
