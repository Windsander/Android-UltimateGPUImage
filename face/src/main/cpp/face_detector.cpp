//
// 检测器方法实现
// Created by willow li on 2019/3/26.
//

#include "config.h"
#include "face_detector.h"

using namespace out_cast_detector;

face_detector::face_detector() = default;

void face_detector::init_face_detector(JNIEnv *env, jstring predictor_path) {
    LOGE("init_face_detector start");
    string path = jstring_complier::jstring_to_string(env, predictor_path);
    frontal_face_detector detector = get_frontal_face_detector();
    shape_predictor predictor = shape_predictor();
    deserialize(path) >> predictor;
    // deserialize(predictor, path);

    jclass pointFClazz = env->FindClass("android/graphics/Point");
    jclass faceInfoClass = env->FindClass("cn/co/willow/android/face/FaceInfo");
    mPointClass = (jclass) env->NewGlobalRef(pointFClazz);
    mFaceInfoClass = (jclass) env->NewGlobalRef(faceInfoClass);
    LOGE("init_face_detector start");
}

jobjectArray face_detector::do_face_detect_action(JNIEnv *env,
                                                  jintArray image_data,
                                                  jint image_height,
                                                  jint image_widht) {

    // 数据转换
    LOGE("数据转换 start");
    auto size = (unsigned long) (image_height * image_widht);
    std::vector<int> image_datacpp(size);
    jsize len = env->GetArrayLength(image_data);
    jint *body = env->GetIntArrayElements(image_data, nullptr);
    for (jsize i = 0; i < len; i++) {
        image_datacpp[i] = (int) body[i];
    }
    LOGE("数据转换 finish");

    // 灰度取值
    LOGE("灰度取值 start");
    array2d<rgb_pixel> frame;
    frame.set_size(image_height, image_widht);
    for (int i = 0; i < image_height; i++) {
        for (int j = 0; j < image_widht; j++) {
            int clr = image_datacpp[i * image_widht + j];
            int red = (clr & 0x00ff0000) >> 16; // 取高两位
            int green = (clr & 0x0000ff00) >> 8; // 取中两位
            int blue = clr & 0x000000ff; // 取低两位
            frame[i][j] = dlib::rgb_pixel(
                    static_cast<unsigned char>(red * 0.299),
                    static_cast<unsigned char>(green * 0.587),
                    static_cast<unsigned char>(blue * 0.114)
            );
        }
    }
    LOGE("灰度取值 finish");

    // 人脸检测
    LOGE("人脸检测 start");
    jmethodID pointConstructID = (env)->GetMethodID(mPointClass, "<init>", "()V");
    jmethodID faceConstructID = (env)->GetMethodID(mFaceInfoClass, "<init>", "()V");

    std::vector<rectangle> rects = detector(frame, 1);
    jobjectArray final_result = (env)->NewObjectArray(jsize(rects.size()), mFaceInfoClass, nullptr);
    for (int i = 0; i < rects.size(); ++i) {
        full_object_detection faces_landmark = predictor(frame, rects[i]);
        jobjectArray keymarks = (env)->NewObjectArray(68, mPointClass, nullptr);
        for (int index = 0; index < 68; index++) {
            point p = faces_landmark.part((unsigned long) (index));
            if (OPEN_LOG) {
                cout << "第 " << index << " 个点的坐标： "
                     << faces_landmark.part((unsigned long) (index))
                     << endl;
            }
            jobject tempPoint = (env)->NewObject(mPointClass, pointConstructID);
            jfieldID px = (env)->GetFieldID(mPointClass, "x", "I");
            jfieldID py = (env)->GetFieldID(mPointClass, "y", "I");
            (env)->SetIntField(tempPoint, px, (int) p.x());
            (env)->SetIntField(tempPoint, py, (int) p.y());
            (env)->SetObjectArrayElement(keymarks, index, tempPoint);
            (env)->DeleteLocalRef(tempPoint);
        }
        jobject tempFaceInfo = (env)->NewObject(mFaceInfoClass, faceConstructID);
        jfieldID marksId = (env)->GetFieldID(mFaceInfoClass, "mKeyPoints", "Ljava/util/List;");
        (env)->SetObjectField(tempFaceInfo, marksId, keymarks);
        (env)->SetObjectArrayElement(final_result, i, tempFaceInfo);
        (env)->DeleteLocalRef(tempFaceInfo);
    }
    LOGE("人脸检测 finish");

    return final_result;
}

/*暴露方法=======================================================================================*/
face_detector *current_detector = nullptr;

static void out_cast_detector::do_init(JNIEnv *env,
                                       jobject job,
                                       jstring predictor_path) {
    LOGE("init_face_detector proxy start");
    if (current_detector != nullptr) {
        current_detector->init_face_detector(env, predictor_path);
    }
    LOGE("init_face_detector proxy finish");
}


static jobjectArray out_cast_detector::do_detect(JNIEnv *env,
                                                 jobject obj,
                                                 jintArray image_data,
                                                 jint image_height,
                                                 jint image_widht) {
    LOGE("do_detect proxy start");
    if (current_detector != nullptr) {
        return current_detector->do_face_detect_action(env, image_data, image_height, image_widht);
    }
    LOGE("do_detect proxy finish");
    return nullptr;
}

/*动态注册=======================================================================================*/

// 方法声明
static const char *jniClassName = "cn/co/willow/android/face/FaceDetectorManager";
static const JNINativeMethod provide_methods[] = {
        {"initFaceDetector",   "(Ljava/lang/String;)V",                       (void *) out_cast_detector::do_init},
        {"doFaceDetectAction", "([III)[Lcn/co/willow/android/face/FaceInfo;", (jobjectArray *) out_cast_detector::do_detect},
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