//
// Created by willow li on 2019/3/26.
//

#ifndef ANDROID_ULTIMATEGPUIMAGE_FACE_DETECTOR_H
#define ANDROID_ULTIMATEGPUIMAGE_FACE_DETECTOR_H

#include <jni.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>
#include "jdata_complier.h"

using namespace std;
using namespace dlib;

class face_detector {
private:

    // 全局参数（注意注销库时需要释放）global reference
    frontal_face_detector detector;
    shape_predictor predictor;

    jclass mPointClass;
    jclass mFaceInfoClass;

public:

    face_detector();

    // 检测器构造方法
    void init_face_detector(JNIEnv *env, jstring predictor_path);

    // 实现人脸检测算法
    jobjectArray do_face_detect_action(
            JNIEnv *env,
            jbyteArray image_data,
            jint image_height,
            jint image_widht
    );

    // 实现人脸检测算法
    jobjectArray do_face_detect_action_int(
            JNIEnv *env,
            jintArray image_data,
            jint image_height,
            jint image_widht
    );
};

namespace out_cast_detector {

    static void do_init(JNIEnv *env,
                        jobject obj,
                        jstring predictor_path);

    static jobjectArray do_detect(JNIEnv *env,
                                  jobject obj,
                                  jbyteArray image_data,
                                  jint image_height,
                                  jint image_widht
    );

    static jobjectArray do_detect_int(JNIEnv *env,
                                      jobject obj,
                                      jintArray image_data,
                                      jint image_height,
                                      jint image_widht
    );
}
#endif //ANDROID_ULTIMATEGPUIMAGE_FACE_DETECTOR_H
