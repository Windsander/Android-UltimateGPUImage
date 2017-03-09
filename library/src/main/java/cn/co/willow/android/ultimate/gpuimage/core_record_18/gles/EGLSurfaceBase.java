/*
 * Original work Copyright 2013 Google Inc.
 * Modified work Copyright 2016 Peter Lu
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

package cn.co.willow.android.ultimate.gpuimage.core_record_18.gles;

import android.graphics.Bitmap;
import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.opengl.GLES20;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import cn.co.willow.android.ultimate.gpuimage.utils.GlUtil;

/**
 * 共用携带GLES核心的基类EGLSurface
 * Common base class for EGL surfaces.
 * <p>
 * There can be multiple surfaces associated with a single context.
 * but if u do this thing, u should record it with an identification
 * <p>
 * Created by willow.li on 2016/10/19.
 */
public class EGLSurfaceBase {

    // EGLCore object we're associated with.  It may be associated with multiple surfaces.
    protected EGLCore mEGLCore;

    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
    private int mWidth = -1;
    private int mHeight = -1;

    protected EGLSurfaceBase(EGLCore eglCore) {
        mEGLCore = eglCore;
    }


    /*基本图层逻辑==================================================================================*/
    /**
     * 销毁一个EGL surface对象，用于清除之前配置的surface
     * Release the EGL surface.
     */
    public void releaseEglSurface() {
        mEGLCore.releaseSurface(mEGLSurface);
        mEGLSurface = EGL14.EGL_NO_SURFACE;
        mWidth = mHeight = -1;
    }

    /**
     * 1-1.创建图层，可实际显示在屏幕上类型
     * Creates a window surface.
     *
     * @param surface May be a Surface or SurfaceTexture.
     */
    public void createWindowSurface(Object surface) {
        if (mEGLSurface != EGL14.EGL_NO_SURFACE) {
            throw new IllegalStateException("surface already created");
        }
        mEGLSurface = mEGLCore.createWindowSurface(surface);

        // Don't cache width/height here, because the size of the underlying surface can change
        // out from under us (see e.g. HardwareScalerActivity).
        //mWidth = mEGLCore.querySurface(mEGLSurface, EGL14.EGL_WIDTH);
        //mHeight = mEGLCore.querySurface(mEGLSurface, EGL14.EGL_HEIGHT);
    }

    /**
     * 1-2.创建图层，不可实际显示在屏幕上类型
     * Creates an off-screen surface.
     */
    public void createOffscreenSurface(int width, int height) {
        if (mEGLSurface != EGL14.EGL_NO_SURFACE) {
            throw new IllegalStateException("surface already created");
        }
        mEGLSurface = mEGLCore.createOffscreenSurface(width, height);
        mWidth = width;
        mHeight = height;
    }

    /**
     * 2-1.将OpenGL context与surface绑定
     * Makes our EGL context and surface current.
     */
    public void makeCurrent() {
        mEGLCore.makeCurrent(mEGLSurface);
    }

    /**
     * 2-2.将OpenGL context与surface绑定，输入输出不同源（渲染后视频为输出）
     * Makes our EGL context and surface current for drawing, using the supplied surface for reading.
     */
    public void makeCurrentReadFrom(EGLSurfaceBase readSurface) {
        mEGLCore.makeCurrent(mEGLSurface, readSurface.mEGLSurface);
    }

    /**
     * 3.设置关键同步帧时间戳
     * Sends the presentation time stamp to EGL.
     */
    public void setPresentationTime(long nsecs) {
        mEGLCore.setPresentationTime(mEGLSurface, nsecs);
    }

    /**
     * 4.绘制完图形后用于显示的函数，数据压入缓存
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     */
    public boolean swapBuffers() {
        boolean result = mEGLCore.swapBuffers(mEGLSurface);
        return result;
    }


    /*图层数据操作==================================================================================*/
    /**
     * 获取图层宽度
     * Returns the surface's width, in pixels.
     * <p>
     * If this is called on a window surface, and the underlying surface is in the process
     * of changing size, we may not see the new size right away (e.g. in the "surfaceChanged"
     * callback).  The size should match after the next buffer swap.
     */
    public int getWidth() {
        if (mWidth < 0) {
            return mEGLCore.querySurface(mEGLSurface, EGL14.EGL_WIDTH);
        } else {
            return mWidth;
        }
    }

    /**
     * 获取图层高度
     * Returns the surface's height, in pixels.
     */
    public int getHeight() {
        if (mHeight < 0) {
            return mEGLCore.querySurface(mEGLSurface, EGL14.EGL_HEIGHT);
        } else {
            return mHeight;
        }
    }

    /**
     * 视频截屏（截取当前帧）
     * Saves the EGL surface to a file.
     * <p>
     * Expects that this object's EGL surface is current.
     */
    public void saveFrame(File file) throws IOException {
        if (!mEGLCore.isCurrent(mEGLSurface)) {
            throw new RuntimeException("Expected EGL context/surface is not current");
        }

        // glReadPixels fills in a "direct" ByteBuffer with what is essentially big-endian RGBA
        // data (i.e. a byte of red, followed by a byte of green...).  While the Bitmap
        // constructor that takes an int[] wants little-endian ARGB (blue/red swapped), the
        // Bitmap "copy pixels" method wants the same format GL provides.
        //
        // Ideally we'd have some way to re-use the ByteBuffer, especially if we're calling
        // here often.
        //
        // Making this even more interesting is the upside-down nature of GL, which means
        // our output will look upside down relative to what appears on screen if the
        // typical GL conventions are used.

        String filename = file.toString();

        int width = getWidth();
        int height = getHeight();
        ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        GLES20.glReadPixels(0, 0, width, height,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
        GlUtil.checkGlError("glReadPixels");
        buf.rewind();

        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filename));
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buf);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
            bmp.recycle();
        } finally {
            if (bos != null) bos.close();
        }
    }
}