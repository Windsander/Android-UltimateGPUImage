package cn.co.willow.android.ultimate.gpuimage.core_config;

/**
 * 消息状态枚举类
 * 模式：消息开始标志 -> 录制状态执行 -> 消息结束
 * <p>
 * Created by willow.li on 2016/10/27.
 */
public enum RecorderMessageState {
    CLEAR_ALL,
    CLEAR_FINISH,
    IDLE,
    INITIALIZING,
    INITIALIZED,
    PREPARING,
    PREPARED,
    START_RECORD,
    RECORDING,
    STOPING,
    STOPED,
    RELEASE,
    ERROR
}
