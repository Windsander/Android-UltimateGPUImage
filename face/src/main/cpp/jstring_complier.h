//
// Created by willow li on 2019/3/26.
//
#ifndef ANDROID_ULTIMATEGPUIMAGE_JSTRING_COMPLIER_H
#define ANDROID_ULTIMATEGPUIMAGE_JSTRING_COMPLIER_H

#include <config.h>
#include <jni.h>
#include <cstring>
#include <cstdlib>
#include <string>
#include "jstring_complier.h"

using namespace std;

class jstring_complier {

public:
    static string jstring_to_string(JNIEnv *env, jstring jstr);

    static jstring string_to_jstring(JNIEnv *env, const string &str);

    static jstring char_to_jstring(JNIEnv *env, const char *pat);

    static char *jstring_to_char(JNIEnv *env, jstring jstr);

};

#endif //ANDROID_ULTIMATEGPUIMAGE_JSTRING_COMPLIER_H
