package cn.co.willow.android.ultimate.gpuimage.sample.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * 简单的时间格式化工具
 * a simple time format util
 * <p>
 * Created by willow.li on 17/10/3.
 */

public class TimeUtil {

    /**
     * 标准时间格式化
     *
     * @param startTime 开始时间（单位：ms）
     * @return 格式化时长
     */
    public static String formatDuration(long startTime) {
        if (startTime == 0) {
            return "--:--";
        }
        long duration = System.currentTimeMillis() / 1000 - startTime;
        long hours    = duration / 3600;
        long minutes  = (duration % 3600) / 60;
        long seconds  = duration % 60;
        if (duration < 0) {
            return "--:--";
        }
        if (hours >= 100) {
            return String.format(Locale.CHINA, "%d:%02d:%02d", hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format(Locale.CHINA, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.CHINA, "%02d:%02d", minutes, seconds);
        }
    }

    /**
     * 视频播放进度格式化
     *
     * @param position 时长（单位：秒）
     * @return 格式化时长
     */
    public static String generateTime(long position) {
        if (position == 0) {
            return "--:--";
        }

        int totalSeconds = (int) ((position / 1000.0) - 0.5);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        if (hours > 0) {
            return String.format(Locale.CHINA, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.CHINA, "%02d:%02d", minutes, seconds);
        }
    }
}
