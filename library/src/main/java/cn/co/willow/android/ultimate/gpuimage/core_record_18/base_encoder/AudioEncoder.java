package cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder;

import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig;
import cn.co.willow.android.ultimate.gpuimage.utils.LogUtil;

import static cn.co.willow.android.ultimate.gpuimage.core_config.OutputConfig.TIMEOUT_USEC;
import static cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder.XMediaMuxer.TRACK_AUDIO;

/**
 * Created by willow.li on 16/11/2
 */
class AudioEncoder extends Thread {

    private XMediaMuxer           mMediaMuxer;
    private MediaCodec            mAudioEncoder;            // API >= 16(Android4.1.2)
    private AudioRecord           mAudioRecorder;
    private MediaCodec.BufferInfo mAudioBufferInfo;         // API >= 16(Android4.1.2)

    private volatile boolean isExit = false;

    private OutputConfig.AudioOutputConfig mAudioConfig;
    private int                            buffer_size;

    AudioEncoder(OutputConfig.AudioOutputConfig audioConfig, XMediaMuxer mMediaMuxer) {
        try {
            this.mAudioConfig = audioConfig;
            this.mMediaMuxer = mMediaMuxer;
            this.mAudioBufferInfo = new MediaCodec.BufferInfo();
            initAudioRecorder();
            initAudioEncoder();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initAudioRecorder() {
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
    }

    private void initAudioEncoder() throws IOException {
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
    }


    /*对外暴露控制==================================================================================*/
    @Override
    public synchronized void start() {
        isExit = false;
        super.start();
    }

    public void exit() {
        isExit = true;
    }


    /*录音流程======================================================================================*/
    @Override
    public void run() {
        try {
            startMediaCodec();
            autoEncodeFrame();
        } finally {
            stopMediaCodec();
        }
    }

    /** 开始录音 */
    private void startMediaCodec() {
        if (mAudioRecorder != null && mAudioEncoder != null) {
            mAudioRecorder.startRecording();
            mAudioEncoder.start();
        }
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
    }

    private void autoEncodeFrame() {
        final ByteBuffer byteBuffs = ByteBuffer.allocateDirect(mAudioConfig.getSamplePerFrame());
        while (!isExit) {
            if (mAudioRecorder != null) {
                byteBuffs.clear();
                int readBytes = mAudioRecorder.read(byteBuffs, buffer_size);
                if (readBytes > 0) {
                    byteBuffs.position(readBytes);
                    byteBuffs.flip();
                    encode(byteBuffs, readBytes, System.nanoTime() / 1000L);
                }
            }
        }
        byteBuffs.clear();
    }


    /*录音编码逻辑====================================================================================*/
    private void encode(final ByteBuffer buffer, final int length, final long presentationTimeUs) {
        if (isExit) return;
        /*向编码器输入数据*/
        final ByteBuffer[] inputBuffers     = mAudioEncoder.getInputBuffers();
        final int          inputBufferIndex = mAudioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
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
        int encoderStatus = mAudioEncoder.dequeueOutputBuffer(mAudioBufferInfo, TIMEOUT_USEC);
        switch (encoderStatus) {
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                LogUtil.i("AudioEncoder", "INFO_OUTPUT_BUFFERS_CHANGED");
                break;
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                MediaFormat newFormat = mAudioEncoder.getOutputFormat();
                mMediaMuxer.addMediaTrack(TRACK_AUDIO, newFormat);
                LogUtil.i("AudioEncoder", "New format " + newFormat);
                break;
            case MediaCodec.INFO_TRY_AGAIN_LATER:
                try {
                    Thread.sleep(10);       // wait 10ms
                    LogUtil.i("AudioEncoder", "dequeueOutputBuffer timed out!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            default:
                final ByteBuffer outputBuffer = mAudioEncoder.getOutputBuffers()[encoderStatus];
                LogUtil.i("AudioEncoder", "We can't use this buffer but render it due to the API limit, " + outputBuffer);
                if ((mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mAudioBufferInfo.size = 0;
                }
                if (mAudioBufferInfo.size != 0 && mMediaMuxer != null) {
                    LogUtil.i("AudioEncoder", "timestamp:: " + mAudioBufferInfo.presentationTimeUs / 1000 + "ms");
                    mMediaMuxer.addMuxerData(TRACK_AUDIO, outputBuffer, mAudioBufferInfo);
                }
                mAudioEncoder.releaseOutputBuffer(encoderStatus, false);
                break;
        }
    }

}
