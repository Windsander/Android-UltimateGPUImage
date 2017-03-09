package cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder;

import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.AUDIO_BIT_RATE;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.AUDIO_FORMAT;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.AUDIO_SAMPLE_RATE;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.CHANNEL_CONFIG;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.CHANNEL_COUNT;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.MIME_AUDIO_TYPE;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.SAMPLES_PER_FRAME;
import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.TIMEOUT_USEC;
import static cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder.XMediaMuxer.TRACK_AUDIO;

/**
 * Created by willow.li on 16/11/2
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AudioEncoder extends Thread {

    private final Object lock = new Object();

    private MediaCodec mAudioEncoder;                // API >= 16(Android4.1.2)
    private AudioRecord mAudioRecorder;
    private MediaCodec.BufferInfo mAudioBufferInfo;        // API >= 16(Android4.1.2)
    private WeakReference<XMediaMuxer> mediaMuxerRunnable;

    private volatile boolean isExit = false;
    private volatile boolean isStart = false;
    private long prevOutputPTSUs = 0;
    private MediaFormat audioFormat;
    private int buffer_size;

    public AudioEncoder(WeakReference<XMediaMuxer> mediaMuxerRunnable) {
        try {
            this.mediaMuxerRunnable = mediaMuxerRunnable;
            mAudioBufferInfo = new MediaCodec.BufferInfo();
            audioFormat = MediaFormat.createAudioFormat(MIME_AUDIO_TYPE, AUDIO_SAMPLE_RATE, CHANNEL_COUNT);
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, CHANNEL_CONFIG);
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE);
            mAudioEncoder = MediaCodec.createEncoderByType(MIME_AUDIO_TYPE);
            mAudioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mAudioEncoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*对外暴露控制==================================================================================*/
    public void exit() {
        isExit = true;
    }

    public void notifyDataChanged() {
        synchronized (lock) {
            lock.notify();
        }
    }

    public void lockAudioThread() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
            }
        }
    }


    /*录音流程======================================================================================*/
    @Override
    public void run() {
        final ByteBuffer byteBuffs = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
        int readBytes;
        while (!isExit) {
            if (!isStart) {
                startMediaCodec();
            } else if (mAudioRecorder != null) {
                byteBuffs.clear();
                readBytes = mAudioRecorder.read(byteBuffs, buffer_size);
                if (readBytes > 0) {
                    byteBuffs.position(readBytes);
                    byteBuffs.flip();
                    encode(byteBuffs, readBytes, getPTSUs());
                }
            }
        }
        byteBuffs.clear();
        stopMediaCodec();
    }

    /** 开始录音 */
    private void startMediaCodec() {
        buffer_size = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        /*if (buffer_size < min_buffer_size)
            buffer_size = ((min_buffer_size / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;*/

        mAudioRecorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                AUDIO_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT,
                buffer_size);
        mAudioRecorder.startRecording();
        isStart = true;
    }

    /** 停止录音 */
    private void stopMediaCodec() {
        if (mAudioRecorder != null) {
            mAudioRecorder.stop();
            mAudioRecorder.release();
            mAudioRecorder = null;
        }
        if (mAudioEncoder != null) {
            mAudioEncoder.stop();
            mAudioEncoder.release();
            mAudioEncoder = null;
        }
        isStart = false;
    }


    private void encode(final ByteBuffer buffer, final int length, final long presentationTimeUs) {
        if (isExit) return;
        final ByteBuffer[] inputBuffers = mAudioEncoder.getInputBuffers();
        final int inputBufferIndex = mAudioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
            /*向编码器输入数据*/
        if (inputBufferIndex >= 0) {
            final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buffer);
            if (length <= 0) {
                mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, 0, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, length, presentationTimeUs, 0);
            }
        }

        /*获取解码后的数据*/
        final XMediaMuxer muxer = mediaMuxerRunnable.get();
        ByteBuffer[] encoderOutputBuffers = mAudioEncoder.getOutputBuffers();
        int encoderStatus;

        do {
            encoderStatus = mAudioEncoder.dequeueOutputBuffer(mAudioBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBuffers = mAudioEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat format = mAudioEncoder.getOutputFormat();
                muxer.addMediaTrack(TRACK_AUDIO, format);
                // lockAudioThread();
            } else if (encoderStatus < 0) {
            } else {
                final ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if ((mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mAudioBufferInfo.size = 0;
                }
                if (mAudioBufferInfo.size != 0 && muxer != null) {
                    mAudioBufferInfo.presentationTimeUs = getPTSUs();
                    muxer.addMuxerData(new XMediaMuxer.MuxerData(
                            TRACK_AUDIO, encodedData, mAudioBufferInfo));
                    //LogUtil.d("TimeStamp", "Audio_TimeStamp:" + mAudioBufferInfo.presentationTimeUs);
                    prevOutputPTSUs = mAudioBufferInfo.presentationTimeUs;
                }
                mAudioEncoder.releaseOutputBuffer(encoderStatus, false);
            }
        } while (encoderStatus >= 0);
    }

    /**
     * get next encoding presentationTimeUs
     */
    private long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        if (result < prevOutputPTSUs)
            result = (prevOutputPTSUs - result) + result;
        return result;
    }
}
