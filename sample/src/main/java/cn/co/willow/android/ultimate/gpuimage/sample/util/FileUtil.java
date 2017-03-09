package cn.co.willow.android.ultimate.gpuimage.sample.util;

import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;

import cn.co.willow.android.ultimate.gpuimage.sample.SampleApplication;

/**
 * 简单的文件操作类
 * a simple file operator
 * <p>
 * Created by willow.li on 16/9/29.
 */

public class FileUtil {

    /*路径获取======================================================================================*/
    private static String VIDEO_PATH = "/sample_video/";

    public static String getVideoSavePath() {
        return getRootCachePath() + VIDEO_PATH;
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
            mCacheRootPath = SampleApplication.getApplication().getCacheDir().getPath();
        }
        return mCacheRootPath;
    }

    @NonNull
    public static File computeMD5ForFile(String fileRootPath) {
        return computeMD5ForFile(fileRootPath, null);
    }


    /*文件存储======================================================================================*/
    /**
     * 获取要保存图片的MD5时序文件名，并创建该文件
     *
     * @param fileRootPath 文件保存目录
     * @param extra        额外附加字段
     * @return 保存文件
     */
    @NonNull
    public static File computeMD5ForFile(String fileRootPath, String extra) {
        String imageMd5 = "" + System.currentTimeMillis();
        String filename = fileRootPath + imageMd5 + (extra == null ? "" : extra);
        filename = filename + ".mp4";
        File temp = new File(filename);
        if (!temp.getParentFile().exists()) {
            temp.getParentFile().mkdirs();
        }
        if (!temp.exists()) {
            try {
                temp.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return temp;
    }


}
