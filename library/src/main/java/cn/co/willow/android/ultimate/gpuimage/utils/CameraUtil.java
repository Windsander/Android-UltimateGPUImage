/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.co.willow.android.ultimate.gpuimage.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.support.annotation.RequiresPermission;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.co.willow.android.ultimate.gpuimage.utils.gif_core.AnimatedGifEncoder;
import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Camera related utilities.
 * 主要用来做适配
 */
public class CameraUtil {

    /** 视频随机取单帧 */
    public static Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(200);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /** 视频随机取单帧 */
    public static Bitmap getVideoThumbnailFF(String filePath) {
        Bitmap bitmap = null;
        FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
        try {
            fmmr.setDataSource(filePath);
            bitmap = fmmr.getFrameAtTime();
            if (bitmap != null) {
                Bitmap b2 = fmmr.getFrameAtTime(
                        1000 * 1000,
                        FFmpegMediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                if (b2 != null) {
                    bitmap = b2;
                }
                if (bitmap.getWidth() > 640) {// 如果图片宽度规格超过640px,则进行压缩
                    bitmap = ThumbnailUtils.extractThumbnail(bitmap,
                            640, 480,
                            ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                }
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } finally {
            fmmr.release();
        }
        return bitmap;
    }

    /** 截取多帧, 使用第三方 */
    public static List<Bitmap> getVideoThumbnailFF(String filePath, int frameNum) {
        List<Bitmap> bitmaps = new ArrayList<>();
        FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
        try {
            fmmr.setDataSource(filePath);
            String timeStr = fmmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
            long durationUs = Long.valueOf(timeStr) * 1000;
            long gapTimeUs;
            if (durationUs > 1000 * 1000 * 2) {
                gapTimeUs = 1000 * 200;
            } else {
                gapTimeUs = durationUs / frameNum;
            }
            for (int i = 0; i < frameNum; i++) {
                long targetFrameTimeUs = 200 + i * gapTimeUs;
                Bitmap singleBitmap = fmmr.getFrameAtTime(targetFrameTimeUs, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                if (singleBitmap == null) continue;
                singleBitmap = compressImageByImgs(singleBitmap, 128, 96);
                bitmaps.add(singleBitmap);
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } finally {
            fmmr.release();
        }
        return bitmaps;
    }

    /**
     * 使用第三方取帧, 指定帧数
     * create gif with bitmaps u put in.
     */
    public static File createGif(List<Bitmap> bitmaps, File gifFile) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(gifFile);
            AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
            gifEncoder.start(fos);
            gifEncoder.setDelay(200);
            gifEncoder.setRepeat(0);
            for (Bitmap oriBitmap : bitmaps) {
                gifEncoder.addFrame(oriBitmap);
            }
            gifEncoder.finish();

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return gifFile;
    }


    /**图片压缩=====================================================================================*/
    /** 图片压缩：按质量压缩 */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while (baos.size() / 1024 > 100) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }


    /** 图片压缩：按比例大小压缩（根据路径获取图片并压缩） */
    public static Bitmap compressImageByPath(String srcPath, float hh, float ww) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        newOpts.inJustDecodeBounds = false;

        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        int be = 1;                                 // be=1表示不缩放
        if (w > h && w > ww) {                      // 如果宽度大的话根据宽度固定大小缩放
            be = Math.round(w / ww);
        } else if (w < h && h > hh) {               // 如果高度高的话根据宽度固定大小缩放
            be = Math.round(h / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);
    }

    /** 图片压缩：按比例大小压缩（根据Bitmap图片压缩） */
    public static Bitmap compressImageByImgs(Bitmap image, float hh, float ww) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.size() / 1024 > 1024) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();

        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        int be = 1;
        if (w > h && w > ww) {                      // 如果宽度大的话根据宽度固定大小缩放
            be = Math.round(w / ww);
        } else if (w < h && h > hh) {               // 如果高度高的话根据宽度固定大小缩放
            be = Math.round(h / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;

        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);
    }

    /** 图片裁剪：按指定大小裁剪 */
    public static Bitmap tailorImageByImgs(String srcPath, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath);

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int retX = w > width ? (w - width) / 2 : 0;       // 基于原图，取正方形左上角x坐标
        int retY = h > height ? (h - height) / 2 : 0;

        width = Math.min(width, w);
        height = Math.min(height, h);

        //下面这句是关键
        return Bitmap.createBitmap(bitmap, retX, retY, width, height, null, false);

    }


    /*像素适配算法==================================================================================*/
    /** 获取预览的像素 */
    public static Camera.Size getPreviewRuleSize(Camera.Parameters parameters, int limit, float ratio) {
        //相机支持的拍照size
        List<Camera.Size> list = parameters.getSupportedPreviewSizes();
        Camera.Size size_asc = null;
        int temp_asc = 0;
        for (int i = 0; i < list.size(); i++) {
            size_asc = list.get(i);
            float dev = size_asc.width / (size_asc.height * 1f);
            temp_asc = size_asc.width * size_asc.height;
            if (temp_asc <= limit && dev == (ratio))
                break;
        }
        Camera.Size size_desc = null;
        int temp_desc = 0;
        for (int i = list.size() - 1; i >= 0; i--) {
            size_desc = list.get(i);
            float dev = size_desc.width / (size_desc.height * 1f);
            temp_desc = size_desc.width * size_desc.height;
            if (temp_desc <= limit && dev == (ratio))
                break;
        }
        return temp_asc > temp_desc ? size_asc : size_desc;
    }

    /** 标准预览像素筛选 */
    public static Camera.Size getOptimalVideoSize(List<Camera.Size> supportedVideoSizes,
                                                  List<Camera.Size> previewSizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;

        List<Camera.Size> videoSizes;
        if (supportedVideoSizes != null) {
            videoSizes = supportedVideoSizes;
        } else {
            videoSizes = previewSizes;
        }

        for (Camera.Size size : videoSizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff && previewSizes.contains(size)) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : videoSizes) {
                if (Math.abs(size.height - targetHeight) < minDiff && previewSizes.contains(size)) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }


    /*获取摄像头====================================================================================*/
    /** 获取系统默认摄像头 */
    @RequiresPermission(Manifest.permission.CAMERA)
    public static Camera getDefaultCameraInstance() {
        return Camera.open();
    }

    /** 获取后置摄像头 */
    public static Camera getDefaultBackFacingCameraInstance() {
        return getDefaultCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    /** 获取前置摄像头 */
    public static Camera getDefaultFrontFacingCameraInstance() {
        return getDefaultCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    /** 获取当前手机指定位置摄像头 */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static Camera getDefaultCamera(int position) {
        int mNumberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == position) {
                return Camera.open(i);
            }
        }
        return null;
    }

}
