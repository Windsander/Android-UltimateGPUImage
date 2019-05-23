package cn.co.willow.android.ultimate.gpuimage.sample.self_defined_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

import cn.co.willow.android.face.FaceInfo;

public class BoundingBoxView extends SurfaceView implements SurfaceHolder.Callback {

    protected SurfaceHolder mSurfaceHolder;
    private Paint mPaint;
    private boolean mIsCreated;

    public BoundingBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(5f);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mIsCreated = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mIsCreated = false;
    }

    public void setResults(List<FaceInfo> faces) {
        if (!mIsCreated) {
            return;
        }
        Canvas canvas = mSurfaceHolder.lockCanvas();
        //清除掉上一次的画框。
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawColor(Color.TRANSPARENT);

        for (FaceInfo face : faces) {
            for (String keyName : FaceInfo.FACE_MARK_LIST_68) {
                PointF lastPoint = null;
                for (PointF point : face.getCertainFacemark68(keyName)) {
                    if (null == lastPoint) {
                        lastPoint = point;
                    } else {
                        canvas.drawLine(lastPoint.x, lastPoint.y, point.x, point.y, mPaint);
                        lastPoint = point;
                    }
                }
            }
        }
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }
}

