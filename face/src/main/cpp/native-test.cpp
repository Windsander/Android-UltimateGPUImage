#include <jni.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>

extern "C"
JNIEXPORT void JNICALL
Java_cn_co_willow_android_face_FaceDetectorDemoActivity_HelloWorld(JNIEnv *env,
                                                                   jobject instance) {

    dlib::frontal_face_detector detector;
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_co_willow_android_face_FaceDetectorDemoActivity_HelloWorld2(JNIEnv *env,
                                                                    jobject instance) {

}