package cn.co.willow.android.face;

import android.content.Context;

import java.io.File;
import java.util.List;

/**
 * Description: $data$
 * <p>
 * Created by Willow.Li on 2019/3/25
 */
public class FaceDetectorManager {

    private final static String PREDICTOR_PATH = "file:///android_asset/shape_predictor_68_face_landmarks.dat";

    static {
        System.loadLibrary("face_lib");
    }

    public FaceDetectorManager(Context context) {
        File file = new File(PREDICTOR_PATH);
        initFaceDetector(PREDICTOR_PATH);
    }

    private static native void initFaceDetector(String predictor_path);

    public static native FaceInfo[] doFaceDetectAction(byte[] image_data,
                                                       int image_height,
                                                       int image_widht);
}
