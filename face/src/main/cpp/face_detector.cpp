//
// 检测器方法实现
// Created by willow li on 2019/3/26.
//

#include "config.h"
#include "face_detector.h"

using namespace out_cast_detector;

face_detector::face_detector() = default;

void face_detector::init_face_detector(JNIEnv *env, jstring predictor_path) {
    LOGI("init_face_detector start");
    string path = jstring_complier::jstring_to_string(env, predictor_path);
    detector = get_frontal_face_detector();
    predictor = shape_predictor();
    deserialize(path) >> predictor;
    // deserialize(predictor, path);

    jclass pointFClazz = (env)->FindClass("android/graphics/PointF");
    jclass faceInfoClass = (env)->FindClass("cn/co/willow/android/face/FaceInfo");
    mPointClass = (jclass) (env)->NewGlobalRef(pointFClazz);
    mFaceInfoClass = (jclass) (env)->NewGlobalRef(faceInfoClass);
    LOGI("init_face_detector finish");
}

jobjectArray face_detector::do_face_detect_action(JNIEnv *env,
                                                  jbyteArray image_data,
                                                  jint image_height,
                                                  jint image_widht) {
    try {
        if (image_data == nullptr || image_height <= 0 || image_widht <= 0) {
            return nullptr;
        }
        // 灰度取值
        LOGI("数据转换 start");
        array2d<rgb_pixel> frame =
                jstring_complier::jbyteArray_to_array2d(
                        env,
                        image_data,
                        image_height,
                        image_widht
                );
        LOGI("数据转换 finish");

        // 人脸检测
        LOGI("人脸检测 start");
        jmethodID pointConstructID = (env)->GetMethodID(mPointClass, "<init>", "()V");
        jmethodID faceConstructID = (env)->GetMethodID(mFaceInfoClass, "<init>", "()V");
        LOGI("jmethodID ready");

        std::vector<dlib::rectangle> rects = detector(frame);
        if (rects.size() == 0) {
            LOGI("如果一张脸都没有，就创建一个默认全区域检索");
            dlib::rectangle default_test_face = rectangle(0, 0, image_widht, image_height);
            rects.push_back(default_test_face);
        }
        LOGI("至少有人脸? %i", (int) jsize(rects.size()));
        jobjectArray final_result =
                (env)->NewObjectArray(
                        jsize(rects.size()),
                        mFaceInfoClass,
                        nullptr
                );
        for (int i = 0; i < rects.size(); ++i) {
            full_object_detection faces_landmark = predictor(frame, rects[i]);
            jobjectArray keymarks = (env)->NewObjectArray(68, mPointClass, nullptr);
            for (int index = 0; index < 68; index++) {
                point p = faces_landmark.part((unsigned long) (index));
                /*if (OPEN_LOG) {
                    std::string plog = "第 " + jstring_complier::int2str(index) + " 个点的坐标： "
                                       + jstring_complier::int2str((int) p.x()) + ","
                                       + jstring_complier::int2str((int) p.y());
                    char buf[plog.size()];
                    strcpy(buf, plog.c_str());
                    LOGI("%s", buf);
                }*/
                jobject tempPoint = (env)->NewObject(mPointClass, pointConstructID);
                jfieldID px = (env)->GetFieldID(mPointClass, "x", "F");
                jfieldID py = (env)->GetFieldID(mPointClass, "y", "F");
                (env)->SetFloatField(tempPoint, px, (float) p.x());
                (env)->SetFloatField(tempPoint, py, (float) p.y());
                (env)->SetObjectArrayElement(keymarks, index, tempPoint);
                (env)->DeleteLocalRef(tempPoint);
            }
            jobject tempFaceInfo = (env)->NewObject(mFaceInfoClass, faceConstructID);
            jfieldID marksId = (env)->GetFieldID(mFaceInfoClass, "mKeyPoints",
                                                 "[Landroid/graphics/PointF;");
            (env)->SetObjectField(tempFaceInfo, marksId, keymarks);
            (env)->SetObjectArrayElement(final_result, i, tempFaceInfo);
            LOGI("单个人脸数据释放前");
            (env)->DeleteLocalRef(tempFaceInfo);
            (env)->DeleteLocalRef(keymarks);
        }
        LOGI("人脸检测 finish");
        rects.clear();
        frame.clear();
        return final_result;

    } catch (exception &e) {
        LOGE("%s", e.what());
    }
    return nullptr;
}

jobjectArray face_detector::do_face_detect_action_int(JNIEnv *env,
                                                      jintArray image_data,
                                                      jint image_height,
                                                      jint image_widht) {
    try {
        if (image_data == nullptr || image_height <= 0 || image_widht <= 0) {
            return nullptr;
        }
        // 灰度取值
        LOGI("数据转换 start");
        array2d<rgb_pixel> frame =
                jstring_complier::jintArray_to_array2d(
                        env,
                        image_data,
                        image_height,
                        image_widht
                );
        LOGI("数据转换 finish");

        // 人脸检测
        LOGI("人脸检测 start");
        jmethodID pointConstructID = (env)->GetMethodID(mPointClass, "<init>", "()V");
        jmethodID faceConstructID = (env)->GetMethodID(mFaceInfoClass, "<init>", "()V");
        LOGI("jmethodID ready");

        std::vector<dlib::rectangle> rects = detector(frame);
        if (rects.size() == 0) {
            LOGI("如果一张脸都没有，就创建一个默认全区域检索");
            dlib::rectangle default_test_face = rectangle(0, 0, image_widht, image_height);
            rects.push_back(default_test_face);
        }
        LOGI("至少有人脸? %i", (int) jsize(rects.size()));
        jobjectArray final_result =
                (env)->NewObjectArray(
                        jsize(rects.size()),
                        mFaceInfoClass,
                        nullptr
                );
        for (int i = 0; i < rects.size(); ++i) {
            full_object_detection faces_landmark = predictor(frame, rects[i]);
            jobjectArray keymarks = (env)->NewObjectArray(68, mPointClass, nullptr);
            for (int index = 0; index < 68; index++) {
                point p = faces_landmark.part((unsigned long) (index));
                /*if (OPEN_LOG) {
                    std::string plog = "第 " + jstring_complier::int2str(index) + " 个点的坐标： "
                                       + jstring_complier::int2str((int) p.x()) + ","
                                       + jstring_complier::int2str((int) p.y());
                    char buf[plog.size()];
                    strcpy(buf, plog.c_str());
                    LOGI("%s", buf);
                }*/
                jobject tempPoint = (env)->NewObject(mPointClass, pointConstructID);
                jfieldID px = (env)->GetFieldID(mPointClass, "x", "F");
                jfieldID py = (env)->GetFieldID(mPointClass, "y", "F");
                (env)->SetFloatField(tempPoint, px, (float) p.x());
                (env)->SetFloatField(tempPoint, py, (float) p.y());
                (env)->SetObjectArrayElement(keymarks, index, tempPoint);
                (env)->DeleteLocalRef(tempPoint);
            }
            jobject tempFaceInfo = (env)->NewObject(mFaceInfoClass, faceConstructID);
            jfieldID marksId = (env)->GetFieldID(mFaceInfoClass, "mKeyPoints",
                                                 "[Landroid/graphics/PointF;");
            (env)->SetObjectField(tempFaceInfo, marksId, keymarks);
            (env)->SetObjectArrayElement(final_result, i, tempFaceInfo);
            LOGI("单个人脸数据释放前");
            (env)->DeleteLocalRef(tempFaceInfo);
            (env)->DeleteLocalRef(keymarks);
        }
        LOGI("人脸检测 finish");
        rects.clear();
        frame.clear();
        return final_result;

    } catch (exception &e) {
        LOGE("%s", e.what());
    }
    return nullptr;
}

/*暴露方法=======================================================================================*/
face_detector *current_detector = nullptr;

static void out_cast_detector::do_init(JNIEnv *env,
                                       jobject job,
                                       jstring predictor_path) {
    LOGI("init_face_detector proxy start");
    if (current_detector != nullptr) {
        current_detector->init_face_detector(env, predictor_path);
    }
    LOGI("init_face_detector proxy finish");
}

static jobjectArray out_cast_detector::do_detect(JNIEnv *env,
                                                 jobject obj,
                                                 jbyteArray image_data,
                                                 jint image_height,
                                                 jint image_widht) {
    LOGI("do_detect proxy");
    if (current_detector != nullptr) {
        return current_detector->do_face_detect_action(
                env,
                image_data,
                image_height,
                image_widht
        );
    }
    return nullptr;
}

static jobjectArray out_cast_detector::do_detect_int(JNIEnv *env,
                                                     jobject obj,
                                                     jintArray image_data,
                                                     jint image_height,
                                                     jint image_widht) {
    LOGI("do_detect proxy");
    if (current_detector != nullptr) {
        return current_detector->do_face_detect_action_int(
                env,
                image_data,
                image_height,
                image_widht
        );
    }
    return nullptr;
}

/*动态注册=======================================================================================*/

// 方法声明
static const char *jniClassName = "cn/co/willow/android/face/FaceDetectorManager";
static const JNINativeMethod provide_methods[] = {
        {"initFaceDetector",      "(Ljava/lang/String;)V",                       (void *) out_cast_detector::do_init},
        {"doFaceDetectAction",    "([BII)[Lcn/co/willow/android/face/FaceInfo;", (jobjectArray *) out_cast_detector::do_detect},
        {"doFaceDetectActionInt", "([III)[Lcn/co/willow/android/face/FaceInfo;", (jobjectArray *) out_cast_detector::do_detect_int},
};

// 此函数通过调用RegisterNatives方法来注册我们的函数
static int registerNativeMethods(JNIEnv *env,
                                 const char *className,
                                 const JNINativeMethod *getMethods,
                                 int methodsNum) {
    jclass clazz;
    clazz = (env)->FindClass(className);
    if (clazz == nullptr) {
        return JNI_FALSE;
    }
    if ((env)->RegisterNatives(clazz, getMethods, methodsNum) < 0) {
        return JNI_FALSE;
    }
    if (current_detector == nullptr) {
        current_detector = new face_detector();
    }
    return JNI_TRUE;
}

// 通用动态注册函数
static int register_android_face_detector(JNIEnv *env) {
    return registerNativeMethods(env,
                                 jniClassName,
                                 provide_methods,
                                 sizeof(provide_methods) / sizeof(provide_methods[0]));
}

/*动态注册：生命周期===================================================================================*/
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    assert(env != nullptr);
    if (!register_android_face_detector(env)) {
        return -1;
    }
    return JNI_VERSION_1_6;
}


JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {

}