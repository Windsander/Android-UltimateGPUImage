package cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.Surface;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Vector;

import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;

import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.IFRAME_INTERVAL;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.MIME_VIDEO_TYPE;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.TIMEOUT_USEC;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.VIDEO_FRAME_RATE;
import static cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder.XMediaMuxer.TRACK_VIDEO;

/**
 * Created by willow.li on 2016/11/4.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class VideoEncoder extends Thread {

    private final Object lock = new Object();
    Vector<byte[]> frameBytes;

    private WeakReference<XMediaMuxer> mediaMuxerRunnable;
    private Surface mInputSurface;
    private MediaCodec mVideoEncoder;
    private MediaCodec.BufferInfo mVideoBufferInfo;

    private volatile boolean isExit = false;
    private MediaFormat videoFormat;


    public VideoEncoder(int width, int height, int bitRate, WeakReference<XMediaMuxer> mediaMuxerRunnable) {
        this.mediaMuxerRunnable = mediaMuxerRunnable;
        try {
            mVideoBufferInfo = new MediaCodec.BufferInfo();
            videoFormat = MediaFormat.createVideoFormat(MIME_VIDEO_TYPE, width, height);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_RATE);
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
            mVideoEncoder = MediaCodec.createEncoderByType(MIME_VIDEO_TYPE);
            mVideoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mInputSurface = mVideoEncoder.createInputSurface();
            mVideoEncoder.start();
        } catch (IOException e) {
            LogUtil.e("VideoEncoder:Failed to start encoder. Make sure the width and height are supported.");
        }
    }


    /*色泽适配逻辑（如果用生成Surface，那就不用这个）===============================================*/
    private static int selectColorFormat(String mimeType) {
        MediaCodecInfo codecInfo = selectCodec(mimeType);
        if (codecInfo == null) {
            return MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
        }
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            if (isRecognizedFormat(colorFormat)) {
                return colorFormat;
            }
        }
        return 0;
    }

    private static boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                return false;
        }
    }

    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }


    /*对外暴露控制==================================================================================*/
    /** 返回当前设定的WindowSurface录制层 */
    public Surface getInputSurface() {
        return mInputSurface;
    }

    public void exit() {
        isExit = true;
        notifyDataChanged();
    }

    public void notifyDataChanged() {
        synchronized (lock) {
            lock.notify();
        }
    }

    public void lockVideoThread() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
            }
        }
    }


    /*视频流程======================================================================================*/
    @Override
    public void run() {
        while (!isExit) {
            encodeFrame();
            //lockVideoThread();
        }
        stopMediaCodec();
    }

    private void stopMediaCodec() {
        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
    }

    private void encodeFrame() {
        final XMediaMuxer muxer = mediaMuxerRunnable.get();
        ByteBuffer[] encoderOutputBuffers = mVideoEncoder.getOutputBuffers();
        int encoderStatus;

        do {
            encoderStatus = mVideoEncoder.dequeueOutputBuffer(mVideoBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBuffers = mVideoEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                LogUtil.d("VideoRunner:video track add to muxer");
                MediaFormat newFormat = mVideoEncoder.getOutputFormat();
                muxer.addMediaTrack(TRACK_VIDEO, newFormat);
                // lockVideoThread();
            } else if (encoderStatus < 0) {
            } else {
                ByteBuffer outputBuffer = encoderOutputBuffers[encoderStatus];
                if ((mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mVideoBufferInfo.size = 0;
                }
                if (mVideoBufferInfo.size != 0 && muxer != null) {
                    outputBuffer.position(mVideoBufferInfo.offset);
                    outputBuffer.limit(mVideoBufferInfo.offset + mVideoBufferInfo.size);
                    muxer.addMuxerData(new XMediaMuxer.MuxerData(
                            TRACK_VIDEO, outputBuffer, mVideoBufferInfo));
                    //Log.d("TimeStamp", "Video_TimeStamp:" + mVideoBufferInfo.presentationTimeUs);
                }
                mVideoEncoder.releaseOutputBuffer(encoderStatus, false);
            }
        } while (encoderStatus >= 0);
    }

}
