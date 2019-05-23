//
// Created by willow li on 2019/3/26.
//

#include <jni.h>
#include <cstring>
#include <cstdlib>
#include <string>
#include <sstream>
#include "config.h"
#include "jdata_complier.h"

using namespace std;

jstring jstring_complier::string_to_jstring(JNIEnv *env, const string &str) {
    return char_to_jstring(env, str.c_str());
}

string jstring_complier::jstring_to_string(JNIEnv *env, jstring jstr) {
    return string(jstring_to_char(env, jstr));
}

jstring jstring_complier::char_to_jstring(JNIEnv *env, const char *pat) {
    jclass strClass = (env)->FindClass("java/lang/String");
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
    auto alen = (size_t) (env->GetArrayLength(barr));
    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char *) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

string jstring_complier::int2str(const int &int_temp) {
    stringstream stream;
    stream << int_temp;
    return stream.str();   //此处也可以用 stream>>string_temp
}

int jstring_complier::jbyteArray_to_int(JNIEnv *env, jbyteArray bytes_data) {
    jbyte *bytes = (env)->GetByteArrayElements(bytes_data, nullptr);
    int int_value = bytes[0] & 0xFF;
    int_value |= ((bytes[1] << 8) & 0xFF00);
    int_value |= ((bytes[2] << 16) & 0xFF0000);
    int_value |= ((bytes[3] << 24) & 0xFF000000);
    (env)->ReleaseByteArrayElements(bytes_data, bytes, 0);
    return int_value;
}

jintArray jstring_complier::jbyteArray_to_jintArray(JNIEnv *env, jbyteArray bytes_data) {
    jsize data_length = (env)->GetArrayLength(bytes_data);
    jsize true_length = data_length / 4;
    jbyte *bytes = (env)->GetByteArrayElements(bytes_data, nullptr);
    jintArray jintArray_result = (env)->NewIntArray(true_length);
    jint buf[true_length];
    for (int i = 0; i < true_length; ++i) {
        int start = i * 4;
        int int_value = bytes[start + 0] & 0xFF;
        int_value |= ((bytes[start + 1] << 8) & 0xFF00);
        int_value |= ((bytes[start + 2] << 16) & 0xFF0000);
        int_value |= ((bytes[start + 3] << 24) & 0xFF000000);
        buf[i] = int_value;
    }
    (env)->SetIntArrayRegion(jintArray_result, 0, true_length, buf);
    (env)->ReleaseByteArrayElements(bytes_data, bytes, 0);
    return jintArray_result;
}

array2d<rgb_pixel> jstring_complier::jbyteArray_to_array2d(JNIEnv *env,
                                                           jbyteArray bytes_data,
                                                           jint image_height,
                                                           jint image_widht) {
    jsize data_length = (env)->GetArrayLength(bytes_data);
    jbyte *bytes = (env)->GetByteArrayElements(bytes_data, nullptr);
    array2d<rgb_pixel> frame;
    frame.set_size(image_height, image_widht);
    for (int i = 0; i < image_height; i++) {
        for (int j = 0; j < image_widht; j++) {
            if (i * image_widht + j >= data_length) {
                break;
            }
            int index = i * image_widht + j;
            int start = index * 4;
            frame[i][j] = dlib::rgb_pixel(
                    (unsigned char) (bytes[start + 0] * 0.299),
                    (unsigned char) (bytes[start + 1] * 0.587),
                    (unsigned char) (bytes[start + 2] * 0.114)
            );
            /*frame[i][j] = dlib::rgb_pixel(
                    (unsigned char) (bytes[start + 0]),
                    (unsigned char) (bytes[start + 1]),
                    (unsigned char) (bytes[start + 2])
            );*/
            if (OPEN_LOG && (index == 0 || index == 1)) {
                std::string plog =
                        "(" + jstring_complier::int2str(i) + ","
                        + jstring_complier::int2str(j) + ") "
                        + jstring_complier::int2str((int) (frame[i][j].red)) + ","
                        + jstring_complier::int2str((int) (frame[i][j].green)) + ","
                        + jstring_complier::int2str((int) (frame[i][j].blue)) + "  alpha = "
                        + jstring_complier::int2str((int) (start + bytes[3]));
                char buf[plog.size()];
                strcpy(buf, plog.c_str());
                LOGI("%s", buf);
                plog.clear();
            }
        }
    }
    (env)->ReleaseByteArrayElements(bytes_data, bytes, 0);
    return frame;
}

array2d<rgb_pixel> jstring_complier::jintArray_to_array2d(JNIEnv *env,
                                                          jintArray ints_data,
                                                          jint image_height,
                                                          jint image_widht) {
    jsize data_length = (env)->GetArrayLength(ints_data);
    jint *datas = (env)->GetIntArrayElements(ints_data, nullptr);
    array2d<rgb_pixel> frame;
    frame.set_size(image_height, image_widht);
    for (int i = 0; i < image_height; i++) {
        for (int j = 0; j < image_widht; j++) {
            if (i * image_widht + j >= data_length) {
                break;
            }
            int index = i * image_widht + j;
            int start = index * 4;
            /* frame[i][j] = dlib::rgb_pixel(
                     (unsigned char) (datas[start + 0] * 0.299),
                     (unsigned char) (datas[start + 1] * 0.587),
                     (unsigned char) (datas[start + 2] * 0.114)
             );*/
            frame[i][j] = dlib::rgb_pixel(
                    (unsigned char) (datas[start + 0]),
                    (unsigned char) (datas[start + 1]),
                    (unsigned char) (datas[start + 2])
            );
            if (OPEN_LOG && (index == 0 || index == 1)) {
                std::string plog =
                        "(" + jstring_complier::int2str(i) + ","
                        + jstring_complier::int2str(j) + ") "
                        + jstring_complier::int2str((int) (frame[i][j].red)) + ","
                        + jstring_complier::int2str((int) (frame[i][j].green)) + ","
                        + jstring_complier::int2str((int) (frame[i][j].blue)) + "  alpha = "
                        + jstring_complier::int2str((int) (datas[start + 3]));
                char buf[plog.size()];
                strcpy(buf, plog.c_str());
                LOGI("%s", buf);
                plog.clear();
            }
        }
    }
    (env)->ReleaseIntArrayElements(ints_data, datas, 0);
    return frame;
}

