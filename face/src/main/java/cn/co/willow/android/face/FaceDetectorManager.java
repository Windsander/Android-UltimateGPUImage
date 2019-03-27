package cn.co.willow.android.face;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Description: $data$
 * <p>
 * Created by Willow.Li on 2019/3/25
 */
public class FaceDetectorManager {

    private final static String PREDICTOR_NAME = "shape_predictor_68_face_landmarks.mp3";

    private String mPredictorStorePath;

    private static FaceDetectorManager mInstance;

    public static FaceDetectorManager getInstance() {
        return mInstance;
    }

    private FaceDetectorManager(final Context context) {
        File externalFilesDir = context.getExternalFilesDir("");
        if (null != externalFilesDir) {
            mPredictorStorePath = externalFilesDir.getAbsolutePath() + "/face_predictor_store/";
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String currentPath = mPredictorStorePath + PREDICTOR_NAME;
                File file = new File(mPredictorStorePath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                AssetsUtil.copyFileFromAssets(context, PREDICTOR_NAME, currentPath);
                Log.d("loadLibrary", "face_lib start");
                System.loadLibrary("face_lib");
                initFaceDetector(currentPath);
                Log.d("loadLibrary", "face_lib finish");
            }
        }).start();
    }

    public static void register(final Context context) {
        if (null == mInstance) {
            synchronized (FaceDetectorManager.class) {
                if (null == mInstance) {
                    mInstance = new FaceDetectorManager(context);
                }
            }
        }
    }

    public FaceInfo[] doFaceDetect(int[] image_data,
                                   int image_height,
                                   int image_widht) {
        return doFaceDetectAction(image_data, image_height, image_widht);
    }

    private static native void initFaceDetector(String predictor_path);

    private static native FaceInfo[] doFaceDetectAction(int[] image_data,
                                                        int image_height,
                                                        int image_widht);
}
