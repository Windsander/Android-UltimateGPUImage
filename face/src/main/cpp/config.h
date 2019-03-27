//
// 基础配置 Base Config
// Created by willow li on 2019/3/27.
//
#include <android/log.h>

#define OPEN_LOG true

#define  LOG_TAG    "native-face-detector"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
