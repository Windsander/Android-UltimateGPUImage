//
// Created by willow li on 2019/3/26.
//

#ifndef ANDROID_ULTIMATEGPUIMAGE_FACE_DETECTOR_H
#define ANDROID_ULTIMATEGPUIMAGE_FACE_DETECTOR_H
#define OPEN_LOG true

#include <jni.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>
#include <dlib/opencv/cv_image.h>
#include "jstring_complier.h"

using namespace std;
using namespace dlib;

class face_detector {
private:

    frontal_face_detector detector;
    shape_predictor predictor;
    vector<rectangle> detect_rects;

    /*全局参数（注意注销库时需要释放）global reference */
    jclass mPointClass;
    jclass mFaceInfoClass;

public:

    face_detector(JNIEnv *env, jstring predictor_path);

    // 实现人脸检测算法
    jobjectArray do_face_detect_action(
            JNIEnv *env,
            jintArray image_data,
            jint image_height,
            jint image_widht
    );

};

#endif //ANDROID_ULTIMATEGPUIMAGE_FACE_DETECTOR_H
