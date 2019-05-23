//
// Created by willow li on 2019/3/26.
//
#ifndef ANDROID_ULTIMATEGPUIMAGE_JSTRING_COMPLIER_H
#define ANDROID_ULTIMATEGPUIMAGE_JSTRING_COMPLIER_H

#include <jni.h>
#include <cstring>
#include <cstdlib>
#include <string>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>
#include "jdata_complier.h"

using namespace std;
using namespace dlib;

class jstring_complier {

public:
    static string jstring_to_string(JNIEnv *env, jstring jstr);

    static jstring string_to_jstring(JNIEnv *env, const string &str);

    static jstring char_to_jstring(JNIEnv *env, const char *pat);

    static char *jstring_to_char(JNIEnv *env, jstring jstr);

    static string int2str(const int &int_temp);

    static int jbyteArray_to_int(JNIEnv *env, jbyteArray bytes);

    static jintArray jbyteArray_to_jintArray(JNIEnv *env, jbyteArray bytes);

    static array2d<rgb_pixel> jintArray_to_array2d(JNIEnv *env,
                                                    jintArray ints_data,
                                                    jint image_height,
                                                    jint image_widht);

    static array2d<rgb_pixel> jbyteArray_to_array2d(JNIEnv *env,
                                                    jbyteArray bytes_data,
                                                    jint image_height,
                                                    jint image_widht);

};

#endif //ANDROID_ULTIMATEGPUIMAGE_JSTRING_COMPLIER_H
