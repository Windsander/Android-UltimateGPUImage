package cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig;
import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;

import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.TIMEOUT_USEC;
import static cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder.XMediaMuxer.TRACK_VIDEO;

/**
 * Created by willow.li on 2016/11/4.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class VideoEncoder extends Thread {

    private XMediaMuxer           mMediaMuxer;
    private Surface               mInputSurface;
    private MediaCodec            mVideoEncoder;
    private MediaCodec.BufferInfo mVideoBufferInfo;

    private boolean isExit        = false;
    private boolean isMuxerStart  = false;
    private double  prevOutputPTS = 0;

    private OutputConfig.VideoOutputConfig mVideoConfig;

    VideoEncoder(OutputConfig.VideoOutputConfig videoConfig, XMediaMuxer mMediaMuxer) {
        try {
            this.mVideoConfig = videoConfig;
            this.mMediaMuxer = mMediaMuxer;
            this.mVideoBufferInfo = new MediaCodec.BufferInfo();
            initVideoEncoder(videoConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initVideoEncoder(OutputConfig.VideoOutputConfig videoConfig) throws IOException {
        MediaFormat videoFormat =
                MediaFormat.createVideoFormat(
                        videoConfig.getVideoType(),
                        videoConfig.getVideoWidth(),
                        videoConfig.getVideoHight()
                );
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, videoConfig.getBpsBitRate());
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, videoConfig.getVideoFrame());
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, videoConfig.getIFrameRate());
        mVideoEncoder = MediaCodec.createEncoderByType(videoConfig.getVideoType());
        mVideoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mVideoEncoder.createInputSurface();
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
            for (String type : types) {
                if (type.equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }


    /*对外暴露控制==================================================================================*/
    /** 返回当前设定的WindowSurface录制层 */
    Surface getInputSurface() {
        LogUtil.i("VideoEncoder", "mInputSurface isEmpty? " + (mInputSurface == null));
        return mInputSurface;
    }

    @Override
    public synchronized void start() {
        isExit = false;
        super.start();
    }

    public void exit() {
        isExit = true;
    }


    /*视频流程======================================================================================*/
    @Override
    public void run() {
        try {
            startMediaCodec();
            autoEncodeFrame();
        } finally {
            stopMediaCodec();
        }
    }

    private void startMediaCodec() {
        if (mVideoEncoder != null) {
            mVideoEncoder.start();
        }
    }

    private void stopMediaCodec() {
        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
        isMuxerStart = false;
    }

    private void autoEncodeFrame() {
        while (!isExit) {
            if (mVideoBufferInfo.presentationTimeUs < prevOutputPTS) return;
            synchronized (this) {
                if (mVideoBufferInfo.presentationTimeUs < prevOutputPTS) return;
                prevOutputPTS = mVideoBufferInfo.presentationTimeUs;
                int                inputBufferIndex = mVideoEncoder.dequeueOutputBuffer(mVideoBufferInfo, TIMEOUT_USEC);
                final ByteBuffer[] inputBuffers     = mVideoEncoder.getInputBuffers();
                if (inputBufferIndex >= 0) {
                    int lastIndex = (inputBufferIndex < inputBuffers.length) ? inputBufferIndex : inputBuffers.length - 1;
                    for (int i = 0; i <= lastIndex; i++) {
                        inputBuffers[i].clear();
                    }
                }
                switch (inputBufferIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        LogUtil.i("VideoEncoder", "INFO_OUTPUT_BUFFERS_CHANGED");
                        break;
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        if (isMuxerStart) {
                            throw new IllegalStateException("output format already changed!");
                        }
                        MediaFormat newFormat = mVideoEncoder.getOutputFormat();
                        mMediaMuxer.addMediaTrack(TRACK_VIDEO, newFormat);
                        isMuxerStart = true;
                        LogUtil.i("VideoEncoder", "New format " + newFormat);
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        try {
                            Thread.sleep(10);       // wait 10ms
                            LogUtil.i("VideoEncoder", "dequeueOutputBuffer timed out! Insufficient Buffer!!");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        if (inputBufferIndex >= 0) {
                            ByteBuffer outputBuffer = mVideoEncoder.getOutputBuffers()[inputBufferIndex];
                            LogUtil.i("VideoEncoder", "We can't use this buffer but render it due to the API limit, " + outputBuffer);
                            if ((mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                mVideoBufferInfo.size = 0;
                            }
                            if (mVideoBufferInfo.size == 0) {
                                LogUtil.d("VideoEncoder", "info.size == 0, drop it.");
                                outputBuffer = null;
                            } else {
                                LogUtil.d("VideoEncoder", "got buffer, info: size=" + mVideoBufferInfo.size
                                        + ", presentationTimeUs=" + mVideoBufferInfo.presentationTimeUs
                                        + ", offset=" + mVideoBufferInfo.offset);
                            }
                            if (outputBuffer != null && mMediaMuxer != null) {
                                LogUtil.i("VideoEncoder", "| timestamp:: " + mVideoBufferInfo.presentationTimeUs / 1000 + "ms |");
                                //outputBuffer.position(mVideoBufferInfo.offset);
                                //outputBuffer.limit(mVideoBufferInfo.offset + mVideoBufferInfo.size);
                                mMediaMuxer.addMuxerData(TRACK_VIDEO, outputBuffer, mVideoBufferInfo);
                            }
                            mVideoEncoder.releaseOutputBuffer(inputBufferIndex, false);
                        } else {
                            LogUtil.i("VideoEncoder", "OutputBuffer's cur-index less than zero");
                        }
                        break;
                }
            }
        }
    }

}
