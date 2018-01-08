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

    /*默认输出配置==================================================================================*/
    // 输出视频参数配置
    public static final  String MIME_VIDEO_TYPE    = "video/avc";            // H.264 Advanced Video Coding
    public static final  int    VIDEO_FRAME_RATE   = 25;                     // fps
    public static final  int    IFRAME_INTERVAL    = 1;                      // 5 seconds between I-frames
    public static final  int    VIDEO_BIT_RATE     = 180000;                 // 默认 bps = videoFrameRate * width * height * 1.5f;
    public static final  int    VIDEO_RECORD_WIDTH = 480;
    public static final  int    VIDEO_RECORD_HEIGH = 640;
    public static final  float  VIDEO_ASPECT_RATIO = 4 / 3f;                 // 宽高比
    /** 其余的视频参数，由当前机型动态适配算法提供 */

    // 输出音频参数配置
    private static final String MIME_AUDIO_TYPE    = "audio/mp4a-latm";
    private static final int    AUDIO_FORMAT       = AudioFormat.ENCODING_PCM_16BIT;
    private static final int    SAMPLES_PER_FRAME  = 1024;                   // 单位帧率，单位音频帧容量 AAC, frameBytes/frame/channel
    private static final int    FRAMES_PER_BUFFER  = 25;                     // AAC, frame/buffer/sec
    private static final int    AUDIO_SAMPLE_RATE  = 44100;                  // 采样质量，单位音频采样hz
    private static final int    AUDIO_BIT_RATE     = 44100;                  // 音频质量，单位音频处理hz
    private static final int    CHANNEL_CONFIG     = CHANNEL_IN_MONO;        // CHANNEL_IN_STEREO 立体声
    private static final int    CHANNEL_COUNT      = 1;                      // 1 channel


    /*可定义输出配置================================================================================*/

    /** 视频输出配置 */
    public static class VideoOutputConfig {

        String videoType   = MIME_VIDEO_TYPE;
        int    videoFrame  = VIDEO_FRAME_RATE;
        int    IFrameRate  = IFRAME_INTERVAL;
        int    bpsBitRate  = VIDEO_BIT_RATE;
        int    videoWidth  = VIDEO_RECORD_WIDTH;
        int    videoHight  = VIDEO_RECORD_HEIGH;
        float  videoAspect = VIDEO_ASPECT_RATIO;

        public String getVideoType() {
            return videoType;
        }
        public void setVideoType(String videoType) {
            this.videoType = videoType;
        }
        public int getVideoFrame() {
            return videoFrame;
        }
        public void setVideoFrame(int videoFrame) {
            this.videoFrame = videoFrame;
        }
        public int getIFrameRate() {
            return IFrameRate;
        }
        public void setIFrameRate(int IFrameRate) {
            this.IFrameRate = IFrameRate;
        }
        public int getBpsBitRate() {
            return bpsBitRate;
        }
        public void setBpsBitRate(int bpsBitRate) {
            this.bpsBitRate = bpsBitRate;
        }
        public int getVideoWidth() {
            return videoWidth;
        }
        public void setVideoWidth(int videoWidth) {
            this.videoWidth = videoWidth;
        }
        public int getVideoHight() {
            return videoHight;
        }
        public void setVideoHight(int videoHight) {
            this.videoHight = videoHight;
        }
        public float getVideoAspect() {
            return videoAspect;
        }
        public void setVideoAspect(float videoAspect) {
            this.videoAspect = videoAspect;
        }

        public VideoOutputConfig() {
        }

        public VideoOutputConfig(
                String videoType,
                int videoFrame,
                int IFrameRate,
                int bpsBitRate,
                int videoWidth,
                int videoHight,
                float videoAspect) {
            this.videoType = videoType;
            this.videoFrame = videoFrame;
            this.IFrameRate = IFrameRate;
            this.bpsBitRate = bpsBitRate;
            this.videoWidth = videoWidth;
            this.videoHight = videoHight;
            this.videoAspect = videoAspect;
        }

        public int getVideoSizeLimit() {
            return videoWidth * videoHight;
        }
    }

    /** 音频输出配置 */
    public static class AudioOutputConfig {

        String audioType      = MIME_AUDIO_TYPE;
        int    audioFormat    = AUDIO_FORMAT;
        int    samplePerFrame = SAMPLES_PER_FRAME;
        int    sampleRate     = AUDIO_SAMPLE_RATE;
        int    bpsBitRate     = AUDIO_BIT_RATE;
        int    channelType    = CHANNEL_CONFIG;
        int    channelNums    = CHANNEL_COUNT;

        public String getAudioType() {
            return audioType;
        }
        public void setAudioType(String audioType) {
            this.audioType = audioType;
        }
        public int getAudioFormat() {
            return audioFormat;
        }
        public void setAudioFormat(int audioFormat) {
            this.audioFormat = audioFormat;
        }
        public int getSamplePerFrame() {
            return samplePerFrame;
        }
        public void setSamplePerFrame(int samplePerFrame) {
            this.samplePerFrame = samplePerFrame;
        }
        public int getSampleRate() {
            return sampleRate;
        }
        public void setSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
        }
        public int getBpsBitRate() {
            return bpsBitRate;
        }
        public void setBpsBitRate(int bpsBitRate) {
            this.bpsBitRate = bpsBitRate;
        }
        public int getChannelType() {
            return channelType;
        }
        public void setChannelType(int channelType) {
            this.channelType = channelType;
        }
        public int getChannelNums() {
            return channelNums;
        }
        public void setChannelNums(int channelNums) {
            this.channelNums = channelNums;
        }

        public AudioOutputConfig() {
        }

        public AudioOutputConfig(
                String audioType,
                int audioFormat,
                int samplePerFrame,
                int sampleRate,
                int bpsBitRate,
                int channelType,
                int channelNums) {
            this.audioType = audioType;
            this.audioFormat = audioFormat;
            this.samplePerFrame = samplePerFrame;
            this.sampleRate = sampleRate;
            this.bpsBitRate = bpsBitRate;
            this.channelType = channelType;
            this.channelNums = channelNums;
        }
    }

}
