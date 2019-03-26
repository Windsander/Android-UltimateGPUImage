//
// Created by willow li on 2019/3/26.
//

#include <jni.h>
#include <cstring>
#include <cstdlib>
#include <string>
#include "jstring_complier.h"

using namespace std;

jstring jstring_complier::string_to_jstring(JNIEnv *env, const string &str) {
    return char_to_jstring(env, str.c_str());
}

string jstring_complier::jstring_to_string(JNIEnv *env, jstring jstr) {
    return string(jstring_to_char(env, jstr));
}

jstring jstring_complier::char_to_jstring(JNIEnv *env, const char *pat) {
    jclass strClass = (env)->FindClass("Ljava/lang/String;");
    jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    auto strLength = jsize(strlen(pat));
    jbyteArray bytes = (env)->NewByteArray(strLength);
    (env)->SetByteArrayRegion(bytes, 0, strLength, (jbyte *) pat);
    jstring encoding = (env)->NewStringUTF("GB2312");
    return (jstring) (env)->NewObject(strClass, ctorID, bytes, encoding);
}

char *jstring_complier::jstring_to_char(JNIEnv *env, jstring jstr) {
    char *rtn = nullptr;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("GB2312");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    auto barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    auto alen = size_t(env->GetArrayLength(barr));
    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char *) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}
