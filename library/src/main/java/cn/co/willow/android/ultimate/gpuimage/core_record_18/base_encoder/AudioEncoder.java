package cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder;

import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig;
import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;

import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.TIMEOUT_USEC;
import static cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder.XMediaMuxer.TRACK_AUDIO;

/**
 * Created by willow.li on 16/11/2
 */
class AudioEncoder extends Thread {

    private final Object lock = new Object();

    private MediaCodec                 mAudioEncoder;                // API >= 16(Android4.1.2)
    private AudioRecord                mAudioRecorder;
    private MediaCodec.BufferInfo      mAudioBufferInfo;        // API >= 16(Android4.1.2)
    private WeakReference<XMediaMuxer> mediaMuxerRunnable;

    private volatile boolean isExit          = false;
    private volatile boolean isStart         = false;

    private OutputConfig.AudioOutputConfig mAudioConfig;
    private int                            buffer_size;

    AudioEncoder(OutputConfig.AudioOutputConfig audioConfig,
                 WeakReference<XMediaMuxer> mediaMuxerRunnable) {
        try {
            this.mAudioConfig = audioConfig;
            this.mediaMuxerRunnable = mediaMuxerRunnable;
            this.mAudioBufferInfo = new MediaCodec.BufferInfo();
            MediaFormat audioFormat =
                    MediaFormat.createAudioFormat(
                            mAudioConfig.getAudioType(),
                            mAudioConfig.getSampleRate(),
                            mAudioConfig.getChannelNums()
                    );
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, mAudioConfig.getChannelType());
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, mAudioConfig.getBpsBitRate());
            mAudioEncoder = MediaCodec.createEncoderByType(mAudioConfig.getAudioType());
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
        final ByteBuffer byteBuffs = ByteBuffer.allocateDirect(mAudioConfig.getSamplePerFrame());
        int              readBytes;
        while (!isExit) {
            if (!isStart) {
                startMediaCodec();
            } else if (mAudioRecorder != null) {
                byteBuffs.clear();
                readBytes = mAudioRecorder.read(byteBuffs, buffer_size);
                if (readBytes > 0) {
                    byteBuffs.position(readBytes);
                    byteBuffs.flip();
                    encode(byteBuffs, readBytes, System.nanoTime() / 1000L);
                }
            }
        }
        byteBuffs.clear();
        stopMediaCodec();
    }

    /** 开始录音 */
    private void startMediaCodec() {
        buffer_size = AudioRecord.getMinBufferSize(
                mAudioConfig.getSampleRate(),
                mAudioConfig.getChannelType(),
                mAudioConfig.getAudioFormat()
        );
        /*if (buffer_size < min_buffer_size)
            buffer_size = ((min_buffer_size / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;*/

        mAudioRecorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                mAudioConfig.getSampleRate(),
                mAudioConfig.getChannelType(),
                mAudioConfig.getAudioFormat(),
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
        final ByteBuffer[] inputBuffers     = mAudioEncoder.getInputBuffers();
        final int          inputBufferIndex = mAudioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
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
        final XMediaMuxer muxer                = mediaMuxerRunnable.get();
        ByteBuffer[]      encoderOutputBuffers = mAudioEncoder.getOutputBuffers();
        int               encoderStatus;

        do {
            encoderStatus = mAudioEncoder.dequeueOutputBuffer(mAudioBufferInfo, TIMEOUT_USEC);
            switch (encoderStatus) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    encoderOutputBuffers = mAudioEncoder.getOutputBuffers();
                    LogUtil.i("AudioEncoder", "INFO_OUTPUT_BUFFERS_CHANGED");
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    MediaFormat newFormat = mAudioEncoder.getOutputFormat();
                    muxer.addMediaTrack(TRACK_AUDIO, newFormat);
                    LogUtil.i("AudioEncoder", "New format " + newFormat);
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    LogUtil.i("AudioEncoder", "dequeueOutputBuffer timed out!");
                    break;
                default:
                    final ByteBuffer outputBuffer = encoderOutputBuffers[encoderStatus];
                    LogUtil.i("AudioEncoder", "We can't use this buffer but render it due to the API limit, " + outputBuffer);
                    if ((mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        mAudioBufferInfo.size = 0;
                    }
                    if (mAudioBufferInfo.size != 0 && muxer != null) {
                        LogUtil.i("AudioEncoder", "timestamp:: " + mAudioBufferInfo.presentationTimeUs / 1000 + "ms");
                        muxer.addMuxerData(new XMediaMuxer.MuxerData(
                                TRACK_AUDIO, outputBuffer, mAudioBufferInfo));
                    }
                    mAudioEncoder.releaseOutputBuffer(encoderStatus, false);
                    break;
            }
        } while ((mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0);
    }

}
