package cn.co.willow.android.ultimate.gpuimage.manager_pure_image;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;

import cn.co.willow.android.ultimate.gpuimage.core_config.FilterConfig;
import cn.co.willow.android.ultimate.gpuimage.core_render.BaseRenderer;
import cn.co.willow.android.ultimate.gpuimage.core_render.PureImageRenderer;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;
import cn.co.willow.android.ultimate.gpuimage.manager_pure_image.image_loader.BaseImageLoader;
import cn.co.willow.android.ultimate.gpuimage.manager_pure_image.image_loader.FileImageLoader;
import cn.co.willow.android.ultimate.gpuimage.manager_pure_image.image_loader.UriImageLoader;

/**
 * this is a pure image Operator, which is used to manipulate image (brightness, contrast, etc..).
 * Function is as same as android-gpuimage  GPUImage class.
 * <p>
 * 本类用于处理纯图片的渲染操作，如增量等滤镜效果实现。
 * <p>
 * Created by willow.li on 2017/4/22.
 */
public class PureImageManager {

    private Context           context;
    private PureImageRenderer mRenderer;
    private GLSurfaceView     mGlSurfaceView;
    private GPUImageFilter    mFilter;
    private Bitmap            mCurrentBitmap;           // The Image tou want to operate

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public PureImageManager(final Context context) {
        this.context = context;
        supportsOpenGLES3(context);
        mFilter = new GPUImageFilter();
        mRenderer = new PureImageRenderer(mFilter);
    }

    /*关键设置======================================================================================*/
    /** 检测是否支持OpenGl */
    private void supportsOpenGLES3(final Context context) {
        final ActivityManager   activityManager   = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        if (configurationInfo.reqGlEsVersion < 0x30000) {
            throw new IllegalStateException("OpenGL ES 3.0 is not supported on this phone.");
        }
    }

    /** 初始化GLSurfaceView */
    public void setGLSurfaceView(final GLSurfaceView view) {
        mGlSurfaceView = view;
        mGlSurfaceView.setEGLContextClientVersion(2);
        mGlSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGlSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        mGlSurfaceView.setRenderer(mRenderer);
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGlSurfaceView.requestRender();
    }

    /** 设置展示模式 */
    public void setScaleType(final FilterConfig.ScaleType scaleType) {
        mRenderer.setScaleType(scaleType);
    }

    /** 设置滤镜 */
    public void setFilter(final GPUImageFilter filter) {
        mFilter = filter;
        mRenderer.setFilter(mFilter);
        requestRender();
    }

    /** 请求刷新渲染器 */
    public void requestRender() {
        if (mGlSurfaceView != null) {
            mGlSurfaceView.requestRender();
        }
    }


    /*图片加载逻辑====================================================================================*/
    /** Sets the image on which the filter should be applied. */
    public void setImage(final Bitmap bitmap) {
        mCurrentBitmap = bitmap;
        mRenderer.setImageBitmap(bitmap, false);
        requestRender();
    }

    /** Sets the image on which the filter should be applied from a Uri. */
    public void setImage(final Uri uri) {
        new UriImageLoader(context, mRenderer, uri)
                .setFinishCallBack(new BaseImageLoader.FinishCallBack() {
                    @Override
                    public void onFinish(Bitmap bitmap) {
                        setImage(bitmap);
                    }
                })
                .execute();
    }

    /** Sets the image on which the filter should be applied from a File. */
    public void setImage(final File file) {
        new FileImageLoader(context, mRenderer, file)
                .setFinishCallBack(new BaseImageLoader.FinishCallBack() {
                    @Override
                    public void onFinish(Bitmap bitmap) {
                        setImage(bitmap);
                    }
                })
                .execute();
    }


    /*渲染流程关联性监听===============================================================================*/
    public void setOnSurfaceSetListener(BaseRenderer.OnSurfaceSetListener mOnSurfaceSetListener) {
        mRenderer.setOnSurfaceSetListener(mOnSurfaceSetListener);
    }

}
