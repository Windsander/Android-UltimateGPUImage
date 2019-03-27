package cn.co.willow.android.face;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class AssetsUtil {

    /**
     * 从assets目录下拷贝文件
     *
     * @param context            上下文
     * @param assetsFilePath     文件的路径名如：SBClock/0001cuteowl/cuteowl_dot.png
     * @param targetFileFullPath 目标文件路径如：/sdcard/SBClock/0001cuteowl/cuteowl_dot.png
     */
    static void copyFileFromAssets(Context context, String assetsFilePath, String targetFileFullPath) {
        InputStream assestsFileImputStream;
        try {
            assestsFileImputStream = context.getAssets().open(assetsFilePath);
            copyFile(assestsFileImputStream, targetFileFullPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从assets目录下拷贝整个文件夹，不管是文件夹还是文件都能拷贝
     *
     * @param context           上下文
     * @param rootDirFullPath   文件目录，要拷贝的目录如assets目录下有一个tessdata文件夹：
     * @param targetDirFullPath 目标文件夹位置如：/Download/tessdata
     */
    static void copyFolderFromAssets(Context context, String rootDirFullPath, String targetDirFullPath) {
        try {
            String[] listFiles = context.getAssets().list(rootDirFullPath);
            if (listFiles == null) {
                return;
            }
            for (String string : listFiles) {
                boolean isFileByName = string.contains(".");
                if (isFileByName) {
                    // 文件
                    copyFileFromAssets(context,
                            rootDirFullPath + "/" + string,
                            targetDirFullPath + "/" + string
                    );
                } else {
                    // 文件夹
                    String childRootDirFullPath = rootDirFullPath + "/" + string;
                    String childTargetDirFullPath = targetDirFullPath + "/" + string;
                    new File(childTargetDirFullPath).mkdirs();
                    copyFolderFromAssets(context,
                            childRootDirFullPath,
                            childTargetDirFullPath
                    );
                }
            }
        } catch (IOException e) {
            Log.d("Tag", "copyFolderFromAssets " + "IOException-" + e.getMessage());
            Log.d("Tag", "copyFolderFromAssets " + "IOException-" + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private static void copyFile(InputStream in, String targetPath) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(targetPath));
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = in.read(buffer)) != -1) {// 循环从输入流读取
                // buffer字节
                fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
            }
            fos.flush();// 刷新缓冲区
            in.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

