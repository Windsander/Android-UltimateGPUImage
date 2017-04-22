package cn.co.willow.android.ultimate.gpuimage.manager_pure_image.image_loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;

import java.io.IOException;

import cn.co.willow.android.ultimate.gpuimage.core_config.FilterConfig;
import cn.co.willow.android.ultimate.gpuimage.core_render.PureImageRenderer;

/**
 * 图片加载器基类
 * <p>
 * Created by willow.li on 2017/4/22.
 */
public abstract class BaseImageLoader extends AsyncTask<Void, Void, Bitmap> {

    protected Context                context;
    private   PureImageRenderer      mRenderer;
    private   int                    mOutputWidth;
    private   int                    mOutputHeight;
    private   FilterConfig.ScaleType mScaleType;

    @SuppressWarnings("deprecation")
    BaseImageLoader(Context context, final PureImageRenderer mRenderer) {
        this.context = context;
        this.mRenderer = mRenderer;
    }


    /*关键重载========================================================================================*/
    /** 图片解析 decode image */
    protected abstract Bitmap decode(BitmapFactory.Options options);

    /** 获取图片方向（系统） get image orientation */
    protected abstract int getImageOrientation() throws IOException;


    /*图片加载流======================================================================================*/
    @Override
    protected Bitmap doInBackground(Void... params) {
        if (mRenderer != null && mRenderer.getFrameWidth() == 0) {
            try {
                synchronized (mRenderer.mSurfaceChangedWaiter) {
                    mRenderer.mSurfaceChangedWaiter.wait(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mOutputWidth = mRenderer.getFrameWidth();
        mOutputHeight = mRenderer.getFrameHeight();
        mScaleType = mRenderer.getScaleType();
        return loadResizedImage();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        mRenderer.deleteImage();
        if (mFinishCallBack != null) {
            mFinishCallBack.onFinish(bitmap);
        }
    }

    private Bitmap loadResizedImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        decode(options);
        int scale = 1;
        while (checkSize(options.outWidth / scale > mOutputWidth, options.outHeight / scale > mOutputHeight)) {
            scale++;
        }

        scale--;
        if (scale < 1) {
            scale = 1;
        }
        options = new BitmapFactory.Options();
        options.inSampleSize = scale;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inPurgeable = true;
        options.inTempStorage = new byte[32 * 1024];
        Bitmap bitmap = decode(options);
        if (bitmap == null) {
            return null;
        }
        bitmap = rotateImage(bitmap);
        bitmap = scaleBitmap(bitmap);
        return bitmap;
    }


    /*图片处理 Predeal Image=========================================================================*/
    /**
     * Retrieve the scaling size for the image dependent on the ScaleType.<br>
     * <br>
     * If CROP: sides are same size or bigger than output's sides<br>
     * Else   : sides are same size or smaller than output's sides
     */
    private int[] getScaleSize(int width, int height) {
        float newWidth;
        float newHeight;

        float withRatio   = (float) width / mOutputWidth;
        float heightRatio = (float) height / mOutputHeight;

        boolean adjustWidth = mScaleType == FilterConfig.ScaleType.CENTER_CROP
                ? withRatio > heightRatio : withRatio < heightRatio;

        if (adjustWidth) {
            newHeight = mOutputHeight;
            newWidth = (newHeight / height) * width;
        } else {
            newWidth = mOutputWidth;
            newHeight = (newWidth / width) * height;
        }
        return new int[]{Math.round(newWidth), Math.round(newHeight)};
    }

    private boolean checkSize(boolean widthBigger, boolean heightBigger) {
        if (mScaleType == FilterConfig.ScaleType.CENTER_CROP) {
            return widthBigger && heightBigger;
        } else {
            return widthBigger || heightBigger;
        }
    }

    private Bitmap rotateImage(final Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Bitmap rotatedBitmap = bitmap;
        try {
            int orientation = getImageOrientation();
            if (orientation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, true);
                bitmap.recycle();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotatedBitmap;
    }

    private Bitmap scaleBitmap(Bitmap bitmap) {
        // resize to desired dimensions
        int    width      = bitmap.getWidth();
        int    height     = bitmap.getHeight();
        int[]  newSize    = getScaleSize(width, height);
        Bitmap workBitmap = Bitmap.createScaledBitmap(bitmap, newSize[0], newSize[1], true);
        if (workBitmap != bitmap) {
            bitmap.recycle();
            bitmap = workBitmap;
            System.gc();
        }

        if (mScaleType == FilterConfig.ScaleType.CENTER_CROP) {
            // Crop it
            int diffWidth  = newSize[0] - mOutputWidth;
            int diffHeight = newSize[1] - mOutputHeight;
            workBitmap = Bitmap.createBitmap(bitmap, diffWidth / 2, diffHeight / 2,
                    newSize[0] - diffWidth, newSize[1] - diffHeight);
            if (workBitmap != bitmap) {
                bitmap.recycle();
                bitmap = workBitmap;
            }
        }

        return bitmap;
    }


    /*加载成功回调====================================================================================*/
    private FinishCallBack mFinishCallBack;

    public interface FinishCallBack {
        void onFinish(Bitmap bitmap);
    }

    public BaseImageLoader setFinishCallBack(FinishCallBack mFinishCallBack) {
        if (mFinishCallBack != null) {
            this.mFinishCallBack = mFinishCallBack;
        }
        return this;
    }

}
