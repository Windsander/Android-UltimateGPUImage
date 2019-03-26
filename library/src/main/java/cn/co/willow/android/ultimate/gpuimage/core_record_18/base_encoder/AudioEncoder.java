package cn.co.willow.android.ultimate.gpuimage.core_record_18.base_encoder;

import android.annotation.TargetApi;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;

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

    private AudioRecordThread mAudioThread;
    private XMediaMuxer mMediaMuxer;
    private MediaCodec mAudioEncoder;                       // API >= 16(Android4.1.2)
    private AudioRecord mAudioRecorder;
    private MediaCodec.BufferInfo mAudioBufferInfo;         // API >= 16(Android4.1.2)
    private OutputConfig.AudioOutputConfig mAudioConfig;

    private boolean isExit = false;
    private long prevAudioPTS = 0;

    /*初始化流程======================================================================================*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    AudioEncoder(OutputConfig.AudioOutputConfig audioConfig, XMediaMuxer mMediaMuxer) {
        try {
            this.mAudioConfig = audioConfig;
            this.mMediaMuxer = mMediaMuxer;
            this.mAudioBufferInfo = new MediaCodec.BufferInfo();
            this.mAudioThread = new AudioRecordThread();
            initAudioRecorder();
            initAudioEncoder();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private void initAudioRecorder() {
        int buffer_size = AudioRecord.getMinBufferSize(
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, mAudioConfig.getChannelNums());
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
        mAudioEncoder = MediaCodec.createEncoderByType(mAudioConfig.getAudioType());
        mAudioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }


    /*对外暴露控制==================================================================================*/
    @Override
    public synchronized void start() {
        isExit = false;
        mAudioThread.start();
        super.start();
    }

    public void exit(OnFinishCallBack mOnFinishCallBack) {
        this.mOnFinishCallBack = mOnFinishCallBack;
        sendEOS();
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

    /**
     * 开始录音
     */
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private void startMediaCodec() {
        if (mAudioRecorder != null && mAudioEncoder != null) {
            mAudioRecorder.startRecording();
            mAudioEncoder.start();
        }
    }

    /**
     * 停止录音
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void stopMediaCodec() {
        if (mAudioRecorder != null) {
            mAudioRecorder.stop();
            mAudioRecorder.release();
        }
        if (mAudioEncoder != null) {
            mAudioEncoder.stop();
            mAudioEncoder.release();
        }
    }


    /*录音编码逻辑====================================================================================*/
    private class AudioRecordThread extends Thread {
        @Override
        public void run() {
            autoInputsFrame();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void autoInputsFrame() {
        final ByteBuffer byteBuffs = ByteBuffer.allocateDirect(mAudioConfig.getSamplePerFrame());
        while (!isExit) {
            if (mAudioRecorder != null) {
                byteBuffs.clear();
                int readBytes = mAudioRecorder.read(byteBuffs, mAudioConfig.getSamplePerFrame());
                if (readBytes == AudioRecord.ERROR_INVALID_OPERATION
                        || readBytes == AudioRecord.ERROR_BAD_VALUE) {
                    LogUtil.d("AudioEncoder", "audio record read error");
                } else if (readBytes > 0) {
                    byteBuffs.position(readBytes);
                    byteBuffs.flip();

                    byte[] bytes = new byte[byteBuffs.remaining()];
                    byteBuffs.get(bytes);

                    byteBuffs.position(readBytes);
                    byteBuffs.flip();

                    /*向编码器输入数据*/
                    final int inputBufferIndex = mAudioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
                    final ByteBuffer[] inputBuffers = mAudioEncoder.getInputBuffers();
                    if (inputBufferIndex >= 0) {
                        final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                        inputBuffer.clear();
                        inputBuffer.put(byteBuffs);
                        if (readBytes > 0) {
                            mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, readBytes, getPTSUs(), 0);
                        }
                    }
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void autoEncodeFrame() {
        while (true) {
            /*处理输出数据*/
            if (mAudioEncoder == null) {
                return;
            }
            int outputBufferId = mAudioEncoder.dequeueOutputBuffer(mAudioBufferInfo, TIMEOUT_USEC);
            //LogUtil.i("VideoEncoder", "outputBufferId is " + outputBufferId + " " + getEncoderState(outputBufferId));
            switch (outputBufferId) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    MediaFormat newFormat = mAudioEncoder.getOutputFormat();
                    mMediaMuxer.addMediaTrack(TRACK_AUDIO, newFormat);
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    try {
                        Thread.sleep(10);       // wait 10ms
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    if (outputBufferId >= 0) {
                        ByteBuffer outputBuffer = mAudioEncoder.getOutputBuffers()[outputBufferId];
                        if ((mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            mAudioBufferInfo.size = 0;
                        }
                        if ((mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            if (mOnFinishCallBack != null) {
                                mOnFinishCallBack.onFinish();
                            }
                            return;
                        }
                        if (mAudioBufferInfo.size == 0) {
                            LogUtil.d("VideoEncoder", "info.size == 0, drop it.");
                            outputBuffer = null;
                        } else {
                            LogUtil.d("VideoEncoder", "got buffer, info: size=" + mAudioBufferInfo.size
                                    + ", presentationTimeUs=" + mAudioBufferInfo.presentationTimeUs
                                    + ", offset=" + mAudioBufferInfo.offset);
                        }
                        if (outputBuffer != null && mMediaMuxer != null) {
                            LogUtil.d("AudioEncoder", "timestamp:: " + mAudioBufferInfo.presentationTimeUs / 1000 + "ms");
                            mMediaMuxer.addMuxerData(TRACK_AUDIO, outputBuffer, mAudioBufferInfo);
                            outputBuffer.clear();
                        }
                        mAudioEncoder.releaseOutputBuffer(outputBufferId, false);

                    } else {
                        LogUtil.i("AudioEncoder", "OutputBuffer's cur-index less than zero");
                    }
                    break;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendEOS() {
        LogUtil.w("AudioEncoder", "sending EOS");
        final int inputBufferIndex = mAudioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
        mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
    }

    private long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        if (result < prevAudioPTS) {
            result = (prevAudioPTS - result) + result;
        }
        prevAudioPTS = result;
        return result;
    }


    /*关键回掉========================================================================================*/
    private OnFinishCallBack mOnFinishCallBack;

    public interface OnFinishCallBack {
        void onFinish();
    }

    private String getEncoderState(int outputBufferId) {
        switch (outputBufferId) {
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                return "output buffers changed";
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                return "output format changed";
            case MediaCodec.INFO_TRY_AGAIN_LATER:
                return "dequeueOutputBuffer timed out! Insufficient Buffer!!";
            default:
                if (outputBufferId >= 0) {
                    return "Dealing with data!!";
                } else {
                    return "OutputBuffer's cur-index less than zero";
                }
        }
    }

}
