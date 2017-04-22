package cn.co.willow.android.ultimate.gpuimage.manager_pure_image.image_loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;

import java.io.File;
import java.io.IOException;

import cn.co.willow.android.ultimate.gpuimage.core_render.PureImageRenderer;

/**
 * get image from an exist file
 * 从已存在的文件来获取图片
 * <p>
 * Created by willow.li on 2017/4/22.
 */
public class FileImageLoader extends BaseImageLoader {

    private final File mImageFile;

    public FileImageLoader(Context context, final PureImageRenderer mRenderer, File file) {
        super(context, mRenderer);
        mImageFile = file;
    }

    @Override
    protected Bitmap decode(BitmapFactory.Options options) {
        return BitmapFactory.decodeFile(mImageFile.getAbsolutePath(), options);
    }

    @Override
    protected int getImageOrientation() throws IOException {
        ExifInterface exif        = new ExifInterface(mImageFile.getAbsolutePath());
        int           orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return 0;
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }
}