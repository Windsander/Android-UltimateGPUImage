package cn.co.willow.android.ultimate.gpuimage.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * 可拉伸的 VideoView
 * Created by willow.li on 17/1/18.
 */
public class ScalePlayerView extends VideoView {

    public ScalePlayerView(Context context) {
        super(context);
    }

    public ScalePlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScalePlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
