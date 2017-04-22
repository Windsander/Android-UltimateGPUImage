package cn.co.willow.android.ultimate.gpuimage.manager.video_recorder;

import java.io.File;

/**
 * 通用录制接口约束
 */
public interface VideoRecordConstrain {

    void startRecord(File mOutputRecFile);

    void stopRecord();

    void releaseRecord();

}
