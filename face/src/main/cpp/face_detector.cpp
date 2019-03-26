//
// Created by willow li on 2019/3/26.
//

#include "face_detector.h"

face_detector::face_detector(JNIEnv *env, jstring predictor_path) {
    string path = jstring_complier::jstring_to_string(env, predictor_path);
    frontal_face_detector detector = get_frontal_face_detector();
    shape_predictor predictor = shape_predictor();
    deserialize(path) >> predictor;

    jclass pointFClazz = env->FindClass("android/graphics/Point");
    jclass faceInfoClass = env->FindClass("cn/co/willow/android/face/FaceInfo");
    mPointClass = (jclass) env->NewGlobalRef(pointFClazz);
    mFaceInfoClass = (jclass) env->NewGlobalRef(faceInfoClass);

}

jobjectArray
face_detector::do_face_detect_action(JNIEnv *env,
                                     jintArray image_data,
                                     jint image_height,
                                     jint image_widht) {

    // 数据转换
    vector<int> image_datacpp(image_height * image_widht);
    jsize len = env->GetArrayLength(image_data);
    jint *body = env->GetIntArrayElements(image_data, nullptr);
    for (jsize i = 0; i < len; i++) {
        image_datacpp[i] = (int) body[i];
    }

    // 灰度取值
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

    // 人脸检测
    jmethodID pointConstructID = (env)->GetMethodID(mPointClass, "<init>", "()V");
    jmethodID faceConstructID = (env)->GetMethodID(mFaceInfoClass, "<init>", "()V");

    vector<rectangle> rects = detector(frame, 1);
    jobjectArray final_result = (env)->NewObjectArray(static_cast<jsize>(rects.size()),
                                                      mFaceInfoClass, NULL);
    for (int i = 0; i < rects.size(); ++i) {
        full_object_detection faces_landmark = predictor(frame, rects[i]);
        jobjectArray keymarks = (env)->NewObjectArray(68, mPointClass, NULL);
        for (int index = 0; index < 68; index++) {
            point p = faces_landmark.part(static_cast<unsigned long>(index));
            if (OPEN_LOG) {
                cout << "第 " << index << " 个点的坐标： "
                     << faces_landmark.part(static_cast<unsigned long>(index))
                     << endl;
            }
            jobject tempPoint = (env)->NewObject(mPointClass, pointConstructID);
            jfieldID px = (env)->GetFieldID(mPointClass, "x", "I");
            jfieldID py = (env)->GetFieldID(mPointClass, "y", "I");
            (env)->SetIntField(tempPoint, px, jint(p.x));
            (env)->SetIntField(tempPoint, py, jint(p.y));
            (env)->SetObjectArrayElement(keymarks, index, tempPoint);
            (env)->DeleteLocalRef(tempPoint);
        }
        jobject tempFaceInfo = (env)->NewObject(mFaceInfoClass, faceConstructID);
        jfieldID marksId = (env)->GetFieldID(mFaceInfoClass, "mKeyPoints", "Ljava/lang/String;");
        (env)->SetObjectField(tempFaceInfo, marksId, keymarks);
        (env)->SetObjectArrayElement(final_result, i, tempFaceInfo);
        (env)->DeleteLocalRef(tempFaceInfo);
    }

    return final_result;
}
