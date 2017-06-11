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
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.lang.ref.WeakReference;

import cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig;
import cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder.XMediaMuxer;
import cn.co.willow.android.ultimate.gpuimage.core_record_18.gles.EGLCore;
import cn.co.willow.android.ultimate.gpuimage.core_record_18.gles.WindowSurface;
import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;

/**
 * 视频同步合成流程队列式控制类
 * this main purpose is to deal with texture action in a query way
 * <p>
 * Created by willow.li on 2016/10/21.
 */
public class TextureMovieEncoder implements Runnable {

    private static final int MSG_START_RECORDING = 0;
    private static final int MSG_FRAME_AVAILABLE = 1;
    private static final int MSG_STOP_RECORDING  = 2;

    // ----- accessed by multiple threads -----
    private volatile EncoderHandler mHandler;

    // ----- AV config -----
    private OutputConfig.VideoOutputConfig mVideoConfig;
    private OutputConfig.AudioOutputConfig mAudioConfig;
    private File                           mOutputFile;

    // ----- lock & mark -----
    private final Object  mReadyFence  = new Object();       // guards ready/running
    private       boolean mLooperReady = false;           // 循环器就绪标识
    private       boolean mMuxersReady = false;           // 混合器就绪标识
    private       boolean mRecrRunning = false;           // 录制器录制标识

    // ----- accessed exclusively by encoder thread -----
    private WindowSurface mInputWindowSurface;
    private EGLCore       mEGLCore;
    private XMediaMuxer   mTMsCoreMuxer;

    public TextureMovieEncoder() {
    }

    public void setOutputFile(File outputFile) {
        mOutputFile = outputFile;
    }

    public void setAVConfig(OutputConfig.VideoOutputConfig videoConfig,
            OutputConfig.AudioOutputConfig audioConfig) {
        mVideoConfig = videoConfig;
        mAudioConfig = audioConfig;
    }


    /*录制流程控制==================================================================================*/
    /** 1.开启录制 */
    public void startRecording() {
        synchronized (mReadyFence) {
            if (mRecrRunning) {
                return;
            }
            mRecrRunning = true;
            new Thread(this).start();
            while (!mLooperReady) {
                try {
                    mReadyFence.wait();
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_START_RECORDING));
    }

    /** 2.通知新帧 */
    public void frameAvailable(SurfaceTexture st) {
        synchronized (mReadyFence) {
            if (!mLooperReady) {
                return;
            }
        }
        if (mHandler == null) return;
        mHandler.sendMessage(mHandler.obtainMessage(MSG_FRAME_AVAILABLE, st.getTimestamp()));
    }

    /** 3.停止录制 */
    public void stopRecording() {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_STOP_RECORDING));
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
            TextureMovieEncoder encoder = mWeakEncoder.get();
            if (encoder == null) return;
            switch (inputMessage.what) {
                case MSG_START_RECORDING:
                    encoder.handleStartRecording();
                    break;
                case MSG_FRAME_AVAILABLE:
                    encoder.handleFrameAvailable((long) inputMessage.obj);
                    break;
                case MSG_STOP_RECORDING:
                    encoder.handleStopRecording();
                    break;
                default:
                    throw new RuntimeException("Unhandled msg what=" + inputMessage.what);
            }
        }
    }


    /*录制消息处理==================================================================================*/
    /** 开启录制 */
    private void handleStartRecording() {
        if (!mMuxersReady) {
            mTMsCoreMuxer = new XMediaMuxer(mVideoConfig, mAudioConfig, mOutputFile);
            mTMsCoreMuxer.setOnFinishListener(mOnFinishListener);
            mEGLCore = new EGLCore(EGL14.eglGetCurrentContext(), EGLCore.FLAG_RECORDABLE);
            mInputWindowSurface = new WindowSurface(mEGLCore, mTMsCoreMuxer.getInputSurface(), true);
            mInputWindowSurface.makeCurrent();
            mTMsCoreMuxer.startMuxer();
            mMuxersReady = true;
        }
    }

    /** 通知视频帧更新，设置视频更新时间戳 */
    private void handleFrameAvailable(long timestamp) {
        if (mMuxersReady) {
            if (mInputWindowSurface == null) return;
            mInputWindowSurface.setPresentationTime(timestamp);
            mInputWindowSurface.swapBuffers();
        }
    }

    /** 停止录制 */
    private void handleStopRecording() {
        mTMsCoreMuxer.stopMuxer();
        if (mInputWindowSurface != null) {
            mInputWindowSurface.release();
            mInputWindowSurface = null;
        }
        if (mEGLCore != null) {
            mEGLCore.release();
            mEGLCore = null;
        }
        mMuxersReady = false;
        Looper looper = Looper.myLooper();
        if (looper != null) {
            looper.quit();
        }
    }


    /*对外暴露监听==================================================================================*/
    /** 最终结果返回监听 */
    private XMediaMuxer.OnFinishListener mOnFinishListener;

    public void setOnFinishListener(XMediaMuxer.OnFinishListener mOnFinishListener) {
        if (mOnFinishListener != null) {
            this.mOnFinishListener = mOnFinishListener;
        }
    }
}