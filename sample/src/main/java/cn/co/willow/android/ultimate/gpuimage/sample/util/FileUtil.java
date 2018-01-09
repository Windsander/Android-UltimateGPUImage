package cn.co.willow.android.ultimate.gpuimage.sample.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;

/**
 * 简单的文件操作类
 * a simple file operator
 * <p>
 * Created by willow.li on 16/9/29.
 */

public class FileUtil {

    /*路径获取======================================================================================*/
    private static final String DEF_PREFIX = "ultra_GPUImage_";
    private static final String ROOT_PATH  = "/UlitmateGPUImage";
    private static final String VIDEO_PATH = ROOT_PATH + "/sample_video/";
    private static final String COVER_PATH = ROOT_PATH + "/sample_cover/";
    private static final int    MP4        = 1;
    private static final int    GIF        = 2;

    public static String getVideoSavePath() {
        return getRootCachePath() + VIDEO_PATH;
    }

    public static String getCoverSavePath() {
        return getRootCachePath() + COVER_PATH;
    }

    private static String getRootCachePath() {
        String mCacheRootPath = null;
        if (Environment.getExternalStorageDirectory().exists()) {
            mCacheRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        if (mCacheRootPath != null && mCacheRootPath.trim().length() != 0) {
            File testFile = new File(mCacheRootPath);
            if (!(testFile.exists() && testFile.canRead() && testFile.canWrite())) {
                mCacheRootPath = null;
            }
        }
        if (mCacheRootPath == null || mCacheRootPath.trim().length() == 0) {
            mCacheRootPath = UIUtils.getContext().getCacheDir().getPath();
        }
        return mCacheRootPath;
    }

    @NonNull
    public static File computeMD5ForVideoFile(String fileRootPath) {
        return computeMD5ForFile(fileRootPath, null, MP4);
    }

    @NonNull
    public static File computeMD5ForCoverFile(String fileRootPath) {
        return computeMD5ForFile(fileRootPath, null, GIF);
    }

    @NonNull
    public static File computeMD5ForImageFile(String fileRootPath, Bitmap bitmap) {
        return saveBitmap(fileRootPath, bitmap);
    }


    /*文件存储 store file logic=====================================================================*/
    /**
     * 获取要保存图片的MD5时序文件名，并创建该文件
     *
     * @param fileRootPath 文件保存目录
     * @param extra        额外附加字段
     * @return 保存文件
     */
    @NonNull
    private static File computeMD5ForFile(String fileRootPath, String extra, int type) {
        try {
            String imageMd5 = DEF_PREFIX + System.currentTimeMillis();
            String filename = fileRootPath + imageMd5 + (extra == null ? "" : extra);
            switch (type) {
                case MP4:
                    filename = filename + ".mp4";
                    break;
                case GIF:
                    filename = filename + ".gif";
                    break;
            }
            File temp = new File(filename);
            if (!temp.getParentFile().exists()) {
                temp.getParentFile().mkdirs();
            }
            if (!temp.exists()) {
                temp.createNewFile();
            }
            return temp;
        } catch (FileNotFoundException e) {
            throw new Resources.NotFoundException(e.getMessage());
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    /** save Bitmap to file */
    private static File saveBitmap(String fileRootPath, Bitmap bitmap) {
        try {
            String imageMd5 = DEF_PREFIX + "cover_" + System.currentTimeMillis();
            String filename = fileRootPath + imageMd5 + ".png";
            File   temp     = new File(filename);
            if (!temp.getParentFile().exists()) {
                temp.getParentFile().mkdirs();
            }
            if (!temp.exists()) {
                temp.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(temp);
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, fos);
            fos.flush();
            fos.close();
            return temp;
        } catch (FileNotFoundException e) {
            throw new Resources.NotFoundException(e.getMessage());
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

}
