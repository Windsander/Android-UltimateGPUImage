/*
 * Copyright (C) 2017 Willow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.co.willow.android.ultimate.gpuimage.core_render;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView.Renderer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import cn.co.willow.android.ultimate.gpuimage.core_config.FilterConfig;
import cn.co.willow.android.ultimate.gpuimage.core_config.Rotation;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;
import cn.co.willow.android.ultimate.gpuimage.utils.GPUImageNativeLibrary;
import cn.co.willow.android.ultimate.gpuimage.utils.GlUtil;
import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;
import cn.co.willow.android.ultimate.gpuimage.utils.TextureRotationUtil;

import static cn.co.willow.android.ultimate.gpuimage.utils.TextureRotationUtil.TEXTURE_NO_ROTATION;

/**
 * 视频渲染器
 * 功能：
 * 基于GPUImage的单纯视频渲染，不提供其他功能项
 * <p>
 * Created by willow.li on 2016/10/22.
 */
@TargetApi(11)
public class BaseRenderer implements Renderer, PreviewCallback {
    public static final int    DEFAULT_NO_IMAGE      = -1;
    public final        byte[] mSurfaceChangedWaiter = new byte[0];       // 绘制锁
    public static final float  CUBE[]                = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
            };

    // 绘制处理流
    protected GPUImageFilter mFilter;
    protected int            mGLTextureId    = DEFAULT_NO_IMAGE;
    protected SurfaceTexture mSurfaceTexture = null;
    protected final FloatBuffer     mGLCubeBuffer;
    protected final FloatBuffer     mGLTextureBuffer;
    protected       IntBuffer       mGLRgbBuffer;
    private final   Queue<Runnable> mRunOnDraw;
    private final   Queue<Runnable> mEndOnDraw;

    // 预览参数
    protected int mOutputWidth;
    protected int mOutputHeight;
    protected int mImageWidth;
    protected int mImageHeight;
    protected int mAddedPadding;

    // 适配参数（图形基本变换）
    private Rotation mRotation;
    private boolean  mFlipHor;
    private boolean  mFlipVer;
    private FilterConfig.ScaleType mScaleType = FilterConfig.ScaleType.CENTER_CROP;

    private float mBGR = 0;
    private float mBGG = 0;
    private float mBGB = 0;

    public BaseRenderer(final GPUImageFilter filter) {
        mFilter = filter;
        mRunOnDraw = new LinkedList<Runnable>();
        mEndOnDraw = new LinkedList<Runnable>();

        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                                  .order(ByteOrder.nativeOrder())
                                  .asFloatBuffer();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                                     .order(ByteOrder.nativeOrder())
                                     .asFloatBuffer();
        setRotation(Rotation.NORMAL, false, false);
    }


    /*预览渲染监听==================================================================================*/
    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        GLES30.glClearColor(mBGR, mBGG, mBGB, 1);
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        mFilter.init();
    }

    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        mOutputWidth = width;
        mOutputHeight = height;
        GLES30.glViewport(0, 0, width, height);
        GLES30.glUseProgram(mFilter.getProgram());
        if (!mFilter.isInitialized()) mFilter.init();
        mFilter.onOutputSizeChanged(width, height);
        adjustImageScaling();
        synchronized (mSurfaceChangedWaiter) {
            mSurfaceChangedWaiter.notifyAll();
        }
    }

    @Override
    public void onDrawFrame(final GL10 gl) {
        LogUtil.i("Render", "onDrawFrame start");
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        runAll(mRunOnDraw);
        if (!mFilter.isInitialized()) mFilter.init();
        mFilter.onDraw(mGLTextureId, mGLCubeBuffer, mGLTextureBuffer);
        runAll(mEndOnDraw);
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
        }
        LogUtil.i("Render", "onDrawFrame finish");
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        LogUtil.i("Render", "onPreviewFrame start");
        final Size previewSize = camera.getParameters().getPreviewSize();
        if (mGLRgbBuffer == null) {
            mGLRgbBuffer = IntBuffer.allocate(previewSize.width * previewSize.height);
        }
        if (mRunOnDraw.isEmpty()) {
            runOnDraw(new Runnable() {
                @Override
                public void run() {
                    GPUImageNativeLibrary.YUVtoRBGA(
                            data,
                            previewSize.width,
                            previewSize.height,
                            mGLRgbBuffer.array());
                    mGLTextureId = GlUtil.createTexture(
                            mGLRgbBuffer,
                            previewSize.width,
                            previewSize.height,
                            mGLTextureId);
                    camera.addCallbackBuffer(data);

                    if (mImageWidth != previewSize.width) {
                        mImageWidth = previewSize.width;
                        mImageHeight = previewSize.height;
                        adjustImageScaling();
                    }
                }
            });
        }
        LogUtil.i("Render", "onPreviewFrame finish");
    }


    /*参数获取======================================================================================*/
    public Rotation getRotation() {
        return mRotation;
    }

    public boolean isFlippedHorizontally() {
        return mFlipHor;
    }

    public boolean isFlippedVertically() {
        return mFlipVer;
    }

    public int getFrameWidth() {
        return mOutputWidth;
    }

    public int getFrameHeight() {
        return mOutputHeight;
    }

    public FilterConfig.ScaleType getScaleType() {
        return mScaleType;
    }

    public void setScaleType(FilterConfig.ScaleType scaleType) {
        mScaleType = scaleType;
    }


    /*相机配置======================================================================================*/
    /** 设置相机 */
    public void setUpSurfaceTexture(final Camera camera) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                int[] textures = new int[1];
                GLES30.glGenTextures(1, textures, 0);
                mSurfaceTexture = new SurfaceTexture(textures[0]);
                if (mOnSurfaceSetListener != null) {
                    mOnSurfaceSetListener.onSurfaceSet(mSurfaceTexture);
                }
                try {
                    camera.setPreviewTexture(mSurfaceTexture);
                    camera.setPreviewCallback(BaseRenderer.this);
                    camera.startPreview();
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        });
    }

    /** 配置相机角度 */
    public void setRotation(Rotation rotation, boolean flipHorizontal, boolean flipVertical) {
        mFlipHor = flipHorizontal;
        mFlipVer = flipVertical;
        mRotation = rotation;
        adjustImageScaling();
    }


    /*滤镜配置======================================================================================*/
    /** 配置配置背景颜色 */
    public void setBackgroundColor(float red, float green, float blue) {
        mBGR = red;
        mBGG = green;
        mBGB = blue;
    }

    /** 设置滤镜 */
    public void setFilter(final GPUImageFilter filter) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                final GPUImageFilter oldFilter = mFilter;
                mFilter = filter;
                if (oldFilter != null) {
                    oldFilter.destroy();
                }
                mFilter.init();
                GLES30.glUseProgram(mFilter.getProgram());
                mFilter.onOutputSizeChanged(mOutputWidth, mOutputHeight);
            }
        });
    }


    /*适配算法======================================================================================*/
    /** 图像适配 */
    protected void adjustImageScaling() {
        float outputWidth  = mOutputWidth;
        float outputHeight = mOutputHeight;
        if (mRotation == Rotation.ROTATION_270 || mRotation == Rotation.ROTATION_90) {
            outputWidth = mOutputHeight;
            outputHeight = mOutputWidth;
        }

        float ratio1         = outputWidth / mImageWidth;
        float ratio2         = outputHeight / mImageHeight;
        float ratioMax       = Math.max(ratio1, ratio2);
        int   imageWidthNew  = Math.round(mImageWidth * ratioMax);
        int   imageHeightNew = Math.round(mImageHeight * ratioMax);

        float ratioWidth  = imageWidthNew / outputWidth;
        float ratioHeight = imageHeightNew / outputHeight;

        float[] cube         = CUBE;
        float[] textureCords = TextureRotationUtil.getRotation(mRotation, mFlipHor, mFlipVer);
        if (mScaleType == FilterConfig.ScaleType.CENTER_CROP) {
            float distHorizontal = (1 - 1 / ratioWidth) / 2;
            float distVertical   = (1 - 1 / ratioHeight) / 2;
            textureCords = new float[]{
                    addDistance(textureCords[0], distHorizontal), addDistance(textureCords[1], distVertical),
                    addDistance(textureCords[2], distHorizontal), addDistance(textureCords[3], distVertical),
                    addDistance(textureCords[4], distHorizontal), addDistance(textureCords[5], distVertical),
                    addDistance(textureCords[6], distHorizontal), addDistance(textureCords[7], distVertical),
                    };
        } else {
            cube = new float[]{
                    CUBE[0] / ratioHeight, CUBE[1] / ratioWidth,
                    CUBE[2] / ratioHeight, CUBE[3] / ratioWidth,
                    CUBE[4] / ratioHeight, CUBE[5] / ratioWidth,
                    CUBE[6] / ratioHeight, CUBE[7] / ratioWidth,
                    };
        }

        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(cube).position(0);
        mGLTextureBuffer.clear();
        mGLTextureBuffer.put(textureCords).position(0);
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }


    /*更新队列控制==================================================================================*/
    private void runAll(Queue<Runnable> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }

    /** 添加操作至：刷新队列 */
    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.add(runnable);
        }
    }

    /** 添加操作至：刷新结束队列 */
    protected void runOnDrawEnd(final Runnable runnable) {
        synchronized (mEndOnDraw) {
            mEndOnDraw.add(runnable);
        }
    }


    /*对外暴露监听==================================================================================*/
    /** 播放器状态监听 */
    private OnSurfaceSetListener mOnSurfaceSetListener;

    public interface OnSurfaceSetListener {
        void onSurfaceSet(SurfaceTexture mSurfaceTexture);
    }

    public void setOnSurfaceSetListener(OnSurfaceSetListener mOnSurfaceSetListener) {
        if (mOnSurfaceSetListener != null) {
            this.mOnSurfaceSetListener = mOnSurfaceSetListener;
        }
    }
}
