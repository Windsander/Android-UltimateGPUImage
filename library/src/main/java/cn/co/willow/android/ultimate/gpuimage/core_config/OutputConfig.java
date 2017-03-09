package cn.co.willow.android.ultimate.gpuimage.core_config;

import android.media.AudioFormat;

import static android.media.AudioFormat.CHANNEL_IN_MONO;

/**
 * 文件输出配置
 * <p>
 * Created by willow.li on 2016/10/27.
 */
public class OutputConfig {

    public static final int TIMEOUT_USEC = 10000;

    // 输出视频参数配置
    public static final String MIME_VIDEO_TYPE = "video/avc";       // H.264 Advanced Video Coding
    public static final int VIDEO_FRAME_RATE = 40;                  // fps
    public static final int IFRAME_INTERVAL = 1;                    // 5 seconds between I-frames
    public static final int VIDEO_BIT_RATE = 800000;                // 默认 kbps
    public static final int VIDEO_RECORD_WIDTH = 480;
    public static final int VIDEO_RECORD_HEIGH = 640;
    public static final float VIDEO_ASPECT_RATIO = 4 / 3f;         // 宽高比
    /** 其余的视频参数，由当前机型动态适配算法提供 */

    // 输出音频参数配置
    public static final String MIME_AUDIO_TYPE = "audio/mp4a-latm";
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int SAMPLES_PER_FRAME = 1024 * 2;               // AAC, frameBytes/frame/channel
    public static final int FRAMES_PER_BUFFER = 25;                 // AAC, frame/buffer/sec
    public static final int AUDIO_SAMPLE_RATE = 44100;              // 44100hz
    public static final int AUDIO_BIT_RATE = 128000;                 // bps  比特率 = 采样率 x 采用位数 x声道数
    public static final int CHANNEL_CONFIG = CHANNEL_IN_MONO;     //CHANNEL_IN_STEREO 立体声
    public static final int CHANNEL_COUNT = 1;                      // 1 channel

}
