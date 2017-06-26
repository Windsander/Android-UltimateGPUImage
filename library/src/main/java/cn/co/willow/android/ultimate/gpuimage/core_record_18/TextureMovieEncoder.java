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

package cn.co.willow.android.ultimate.gpuimage.core_record_18;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;

import cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig;
import cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder.XMediaMuxer;
import cn.co.willow.android.ultimate.gpuimage.core_record_18.gles.EGLCore;
import cn.co.willow.android.ultimate.gpuimage.core_record_18.gles.WindowSurface;
import cn.co.willow.android.ultimate.gpuimage.core_render_filter.GPUImageFilter;

/**
 * 视频同步合成流程队列式控制类
 * this main purpose is to deal with texture action in a query way
 * <p>
 * Created by willow.li on 2016/10/21.
 */
public class TextureMovieEncoder implements Runnable {

    private static final int MSG_START_RECORDING       = 0;
    private static final int MSG_FRAME_AVAILABLE       = 1;
    private static final int MSG_SET_TEXTURE_ID        = 2;
    private static final int MSG_UPDATE_SHARED_CONTEXT = 3;
    private static final int MSG_STOP_RECORDING        = 4;
    private static final int MSG_QUIT                  = 5;

    // ----- accessed exclusively by encoder thread -----
    private WindowSurface  mInputWindowSurface;
    private EGLCore        mEGLCore;
    private GPUImageFilter mFilter;
    private int            mTextureId;
    private XMediaMuxer    mTMsCoreMuxer;

    // ----- accessed by multiple threads -----
    private volatile EncoderHandler mHandler;

    private Object  mReadyFence  = new Object();       // guards ready/running
    private boolean mLooperReady = false;           // 循环器就绪标识
    private boolean mMuxersReady = false;           // 混合器就绪标识
    private boolean mRecrRunning = false;           // 录制器录制标识

    private FloatBuffer mGLCubeBuffer;
    private FloatBuffer mGLTextureBuffer;


    /** Encoder configuration. */
    public static class EncoderConfig {
        final EGLContext mEglContext;
        final File       mOutputFile;

        public EncoderConfig(EGLContext sharedEglContext, File outputFile) {
            mEglContext = sharedEglContext;
            mOutputFile = outputFile;
        }
    }

    public TextureMovieEncoder(GPUImageFilter filter) {
        mFilter = filter;
    }


    /*关键参数设置====================================================================================*/
    public void setFilter(GPUImageFilter filter) {
        mFilter = filter;
    }
    public void setGLCubeBuffer(FloatBuffer buffer) {
        mGLCubeBuffer = buffer;
    }
    public void setGLTextureBuffer(FloatBuffer buffer) {
        mGLTextureBuffer = buffer;
    }


    /*录制流程控制====================================================================================*/
    /** 1.开启录制 */
    public void startRecording(EncoderConfig config) {
        synchronized (mReadyFence) {
            if (mRecrRunning) {
                return;
            }
            mRecrRunning = true;
            new Thread(this, "TextureMovieEncoder").start();
            while (!mLooperReady) {
                try {
                    mReadyFence.wait();
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_START_RECORDING, config));
    }

    /** 2.设置预览层TextureId，已经过基本纹理处理输出Texture的id（未加滤镜 not load filter） */
    public void setTextureId(int id) {
        synchronized (mReadyFence) {
            if (!mLooperReady) {
                return;
            }
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_TEXTURE_ID, id, 0, null));
    }

    /** 3.通知新帧 */
    public void frameAvailable(SurfaceTexture st) {
        synchronized (mReadyFence) {
            if (!mLooperReady) {
                return;
            }
        }

        float[] transform = new float[16];
        st.getTransformMatrix(transform);
        long timestamp = st.getTimestamp();
        if (timestamp == 0) {
            return;
        }

        if (mHandler == null) return;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_FRAME_AVAILABLE,
                (int) (timestamp >> 32), (int) timestamp, transform));
    }

    /** 4.EGL Surface 更新方法 */
    public void updateSharedContext(EGLContext sharedContext) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SHARED_CONTEXT, sharedContext));
    }

    /** 5.停止录制 */
    public void stopRecording() {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_STOP_RECORDING));
        mHandler.sendMessage(mHandler.obtainMessage(MSG_QUIT));
    }

    /** 是否正在录制 */
    public boolean isRecording() {
        synchronized (mReadyFence) {
            return mRecrRunning;
        }
    }


    /*消息逻辑======================================================================================*/
    /** 编码器流程消息控制线程入口，建立循环器，开启消息队列 */
    @Override
    public void run() {
        Looper.prepare();
        synchronized (mReadyFence) {
            mHandler = new EncoderHandler(this);
            mLooperReady = true;
            mReadyFence.notify();
        }
        Looper.loop();

        synchronized (mReadyFence) {
            mLooperReady = false;
            mRecrRunning = false;
            mHandler = null;
        }
    }


    /** 处理编码请求的handler,应该在Handler Thread中调用 */
    private static class EncoderHandler extends Handler {

        private WeakReference<TextureMovieEncoder> mWeakEncoder;

        EncoderHandler(TextureMovieEncoder encoder) {
            mWeakEncoder = new WeakReference<TextureMovieEncoder>(encoder);
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override  // runs on encoder thread
        public void handleMessage(Message inputMessage) {
            int    what = inputMessage.what;
            Object obj  = inputMessage.obj;

            TextureMovieEncoder encoder = mWeakEncoder.get();
            if (encoder == null) return;

            switch (what) {                               // 执行次数量级 frequency of execute
                case MSG_START_RECORDING:               // x1
                    encoder.handleStartRecording((EncoderConfig) obj);
                    break;
                case MSG_SET_TEXTURE_ID:                // x1
                    encoder.handleSetTexture(inputMessage.arg1);
                    break;
                case MSG_FRAME_AVAILABLE:               // xn
                    long timestamp = (((long) inputMessage.arg1) << 32) |
                            (((long) inputMessage.arg2) & 0xffffffffL);
                    encoder.handleFrameAvailable(timestamp);
                    break;
                case MSG_UPDATE_SHARED_CONTEXT:        // x2
                    encoder.handleUpdateSharedContext((EGLContext) inputMessage.obj);
                    break;
                case MSG_STOP_RECORDING:               // x1
                    encoder.handleStopRecording();
                    break;
                case MSG_QUIT:                          // x1
                    //Looper.myLooper().quit();
                    break;
                default:
                    throw new RuntimeException("Unhandled msg what=" + what);
            }
        }
    }


    /*录制消息处理==================================================================================*/
    /** 开启录制 */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleStartRecording(EncoderConfig config) {
        prepareEncoder(
                config.mEglContext,
                config.mOutputFile);
    }

    /** 设置视频帧来源SurfaceId */
    private void handleSetTexture(int id) {
        mTextureId = id;
    }

    /** 通知视频帧更新 */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleFrameAvailable(long timestampNanos) {
        if (!mMuxersReady) return;
        if (!mFilter.isInitialized()) mFilter.init();
        mFilter.onDraw(mTextureId, mGLCubeBuffer, mGLTextureBuffer);
        mInputWindowSurface.setPresentationTime(timestampNanos);
        mInputWindowSurface.swapBuffers();
    }

    /** 处理EGLSurface切换时，录屏监控对象的切换，以避免裂屏 */
    private void handleUpdateSharedContext(EGLContext newSharedContext) {
        mInputWindowSurface.releaseEglSurface();
        mEGLCore.release();
        mEGLCore = new EGLCore(newSharedContext, EGLCore.FLAG_RECORDABLE);
        mInputWindowSurface.recreate(mEGLCore);
        mInputWindowSurface.makeCurrent();
    }

    /** 停止录制 */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleStopRecording() {
        mTMsCoreMuxer.stopMuxer();
        mMuxersReady = false;
        releaseEncoder();
        Looper.myLooper().quit();
    }


    /*混合器逻辑====================================================================================*/
    /** 准备实时混合器 */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void prepareEncoder(final EGLContext sharedContext, File outputFile) {
        if (!mMuxersReady) {
            mTMsCoreMuxer = new XMediaMuxer(
                    new OutputConfig.VideoOutputConfig(),
                    new OutputConfig.AudioOutputConfig(),
                    outputFile);
            mTMsCoreMuxer.setOnFinishListener(mOnFinishListener);
            mEGLCore = new EGLCore(sharedContext, EGLCore.FLAG_RECORDABLE);
            mInputWindowSurface = new WindowSurface(mEGLCore, mTMsCoreMuxer.getInputSurface(), true);
            mInputWindowSurface.makeCurrent();
            mTMsCoreMuxer.startMuxer();
            mMuxersReady = true;
        }
    }

    /** 释放资源 */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void releaseEncoder() {
        if (mInputWindowSurface != null) {
            mInputWindowSurface.release();
            mInputWindowSurface = null;
        }
        if (mEGLCore != null) {
            mEGLCore.release();
            mEGLCore = null;
        }
    }


    /*对外暴露监听==================================================================================*/
    /** 最终结果返回监听 */
    private XMediaMuxer.OnFinishListener mOnFinishListener;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void setOnFinishListener(XMediaMuxer.OnFinishListener mOnFinishListener) {
        if (mOnFinishListener != null) {
            this.mOnFinishListener = mOnFinishListener;
        }
    }
}